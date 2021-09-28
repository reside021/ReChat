#include <iostream>
#include <uwebsockets/App.h>
#include <map>
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/random_generator.hpp>
#include <boost/uuid/uuid_io.hpp>
using namespace std;


/*
* 10 = Петя
* 11 = Вася
*
* Клиент пишет сообщение другому пользователю
* MESSAGE_TO::11::Привет от Пети

* Сервер отправит получателю тоже сообщение
* MESSAGE_FROM::10::Привет Пети
* 
* клиент захочет представиться
* set_name = 
*
* клиент захочет написать всем
* Message_all:: всем привет
*
*
* Сервер будет сообщать статус пользователя
ws = new WebSocket("ws://localhost:9001");
ws.onmessage = ({data}) => console.log("From Server:", data);
ws.send("SET_NAME::vasya");
 */

map<string, string> userNames;
// ограничить длину имени (безопасность памяти)

const string BROADCAST_CHANNEL = "broadcast";
const string MESSAGE_TO = "MESSAGE_TO::";
const string SET_NAME = "SET_NAME::";
const string OFFLINE = "OFFLINE::";
const string ONLINE = "ONLINE::";

// Какую информацию о пользователе мы храним
struct PerSocketData {
    string name; // имя юзерра
    string uId; // уникально-уникальный идентификатор
};


void updateName(PerSocketData* data) {
    userNames[data->uId] = data->name;
}

void deleteName(PerSocketData* data) {
    userNames.erase(data->uId);
}
//ONLINE::19::vasya
string online(string user_id) {
    string name = userNames[user_id];
    // проверить, что такой userid в карте есть
    return ONLINE + user_id + "::" + name;
}

string offline(string user_id) {
    string name = userNames[user_id];
    // проверить, что такой userid в карте есть
    return OFFLINE + user_id + "::" + name;
}

bool isSetName(string message) {
    return message.find(SET_NAME) == 0;
}

string parseName(string message) {
    return message.substr(SET_NAME.size());
}


string parseUserId(string message) {
    string rest = message.substr(MESSAGE_TO.size());
    int pos = rest.find("::"); // pos = 2
    return rest.substr(0, pos); // example 11
}

string parseUserText(string message) {
    string rest = message.substr(MESSAGE_TO.size());
    int pos = rest.find("::"); // pos = 2
    return rest.substr(pos + 2); // example "Привет от пети"
}

bool isMessageTo(string message) {
    return message.find(MESSAGE_TO) == 0;
}

string messageFrom(string user_id, string sender, string message) {
    return "MESSAGE_FROM::" + user_id + "::[" + sender + "] " + message;
}

string generateUUID() {
    boost::uuids::random_generator uuid_gen;
    boost::uuids::uuid u = uuid_gen();
    return to_string(u).substr(0, 8);
}

int main() {
    
    //unsigned int last_user_id = 10; // последний идентификатор пользователя
    // сделать UUID / GUID

    int userID = 1;

    // Настраиваем сервер
    uWS::SSLApp(
        //{
        ///* There are tons of SSL options, see uSockets */
        //    .cert_file_name = "cert.pem",
        //    .key_file_name = "key.pem"
        //}
        ). // Создаем простое приложение без шифрования
        ws<PerSocketData>("/*", { // для каждого пользователя мы храним данные в виде PerSocketData
            /* Settings */
            .idleTimeout = 1200, // таймаут в секундах (отключает пользователя)
            .open = [&userID](auto* ws) {
                // функция open (лямбда функция)
                // вызывается при открытии соединения

                // 0. получить структуру PerSocketData
                PerSocketData* userData = (PerSocketData*) ws->getUserData();
                // 1. назначить пользователю уникальный идентификатор
                userData->name = "UNNAMED";
                userData->uId = generateUUID();
;                for (auto entry : userNames) {
                    ws->send(online(entry.first), uWS::OpCode::TEXT);
                }
                updateName(userData);
                ws->publish(BROADCAST_CHANNEL, online(userData->uId));

                cout << "New user connected, id = " << userData->uId << endl;
                cout << "Users connected: " << userNames.size() << endl;

                string userChannel = "user#" + userData->uId;

                ws->subscribe(userChannel); //  укаждого юзера есть личка
                ws->subscribe(BROADCAST_CHANNEL); // подписка юзера на общий канал
                // todo Сообщить всем пользователям, что кто-то онлайн

                ws->publish(userChannel, "FC::" + userData->uId, uWS::OpCode::TEXT, false);

            },
            .message = [](auto* ws, string_view message, uWS::OpCode opCode) {
                string strMessage = string(message);
                PerSocketData* userData = (PerSocketData*)ws->getUserData();
                string authorId = userData->uId;
                //ws->send(message, opCode, true); обратная отправка сообщений
                // вызывается при получении сообщения от пользователя

                if (isMessageTo(strMessage)) {
                    // подготовить данные и отправить их
                    string receiverId = parseUserId(strMessage);
                    string text = parseUserText(strMessage);
                    // userData->user_id == отправитель
                    string outgoingMessage = messageFrom(authorId, userData-> name, text);
                    // отправить получателю
                    if (receiverId == "0") {
                        ws->publish(BROADCAST_CHANNEL, outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else {
                        ws->publish("user#" + receiverId, outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    ws->send("Message sent", uWS::OpCode::TEXT);
                    cout << "User #" << authorId << " wrote message to " << receiverId << endl;
                    }
                if (isSetName(strMessage)) {
                    if (strMessage.size() < 20) {
                        string newName = parseName(strMessage);
                        userData->name = newName;
                        updateName(userData);
                        ws->publish(BROADCAST_CHANNEL, online(userData->uId));
                        cout << "User #" << authorId << " set their name" << endl;
                    }
                    else {
                        ws->publish("user#" + authorId, "ERROR SET NAME", uWS::OpCode::TEXT, false);
                    }
                }
                // сообщить, кто вообще онлайн
            },
            .close = [](auto* ws , int /*code*/, string_view /*message*/) {
                // вызывается при отключении от сервера
                PerSocketData* userData = (PerSocketData*)ws->getUserData();
                ws->publish(BROADCAST_CHANNEL, offline(userData->uId));
                deleteName(userData);
                cout << "Users connected: " << userNames.size() << endl;
            }
            })
            .listen(9001, [](auto* listen_socket) {
                if (listen_socket) {
                    // если все ок, вывести сообщение
                    cout << "Listening on port " << 9001 << std::endl;
                }
                }).run(); // запуск
}
