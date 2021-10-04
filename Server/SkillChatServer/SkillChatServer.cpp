#include <iostream>
#include <uwebsockets/App.h>
#include <map>
#include <nlohmann/json.hpp>

// [UUID] для создания уникального uuid
#include <boost/uuid/uuid.hpp>
#include <boost/uuid/random_generator.hpp>
#include <boost/uuid/uuid_io.hpp>

// [SQL] Для работы с бд
#include <windows.h>
#include <sqlext.h>
#include <sqltypes.h>
#include <sql.h>
#define SQL_RESULT_LEN 255
#define SQL_RETURN_CODE_LEN 1000


using namespace std;
using json = nlohmann::json;


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
// ограничить длину имени ( для безопасности памяти)


// [SERVER] - константы для сервера
const string BROADCAST_CHANNEL = "broadcast";
const string MESSAGE_TO = "MESSAGE_TO::";
const string SET_NAME = "SET_NAME::";
const string OFFLINE = "OFFLINE::";
const string ONLINE = "ONLINE::";
const string SIGNUP = "SIGNUP::";
const string DBSERVER = "DBSERVER";
const string F2A = "2FA";
const string FORDB = "FORDB::";
const string INFO = "INFO::";
const string SQL = "SQL::";
const string DBNOTACTIVE = "DBNOTACTIVE::";
const string INSERT = "INSERT::";
const string AUTH = "AUTH::";
const string SELECT = "SELECT::";
const string RESULTDB = "RESULTDB";

// Какую информацию о пользователе мы храним
struct PerSocketData {
    string name; // имя юзерра
    string uId; // уникально-уникальный идентификатор
};


void updateName(PerSocketData* data) {
    userNames[data->uId] = data->name;
}
void updateUid(PerSocketData* data) {
    userNames[data->uId] = data->uId;
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

string messageFromUser(string user_id, string sender, string message) {
    return "MESSAGE_FROM::" + user_id + "::[" + sender + "] " + message;
}
string messageFromGlobal(string user_id, string sender, string message) {
    return "MESSAGE_FROM::" + user_id + "::[" + sender + "] " + message;
}

string generateUUID() {
    boost::uuids::random_generator uuid_gen;
    boost::uuids::uuid u = uuid_gen();
    return to_string(u).substr(0, 8);
}



bool isSignNewUser(string message) {
    return message.find(SIGNUP) == 0;
}

string parseNewUserLogin(string message) {
    string rest = message.substr(SIGNUP.size());
    int pos = rest.find("::");
    return rest.substr(0, pos); 
}

string parseNewUserPassword(string message) {
    string rest = message.substr(SIGNUP.size());
    int pos = rest.find("::"); 
    rest = rest.substr(pos + 2);
    pos = rest.find("::");
    return rest.substr(0, pos);
}

string parseNewUserNickname(string message) {
    string rest = message.substr(SIGNUP.size());
    int pos = rest.find("::");
    rest = rest.substr(pos + 2);
    pos = rest.find("::");
    return rest.substr(pos + 2);
}

bool isConnectionServerDB(string message) {
    return message.find(DBSERVER) == 0;
}
bool isTrustServer(string message) {
    return message.find(F2A) == 0;
}

bool IsServerDBNotActive() {
    return userNames.find("999") == userNames.end();
}

bool isAuthUser(string message) {
    return message.find(AUTH) == 0;
}
string parseUserLogin(string message) {
    string rest = message.substr(AUTH.size());
    int pos = rest.find("::");
    return rest.substr(0, pos);
}

string parseUserPass(string message) {
    string rest = message.substr(AUTH.size());
    int pos = rest.find("::");
    return rest.substr(pos + 2);
}
bool isResultFromDB(string message) {
    return message.find(RESULTDB) == 0;
}
string parseResultDB(string message) {
    string rest = message.substr(RESULTDB.size());
    int pos = rest.find("::");
    return rest.substr(0, pos);
}
string parseResultDBAuthor(string message) {
    string rest = message.substr(RESULTDB.size());
    int pos = rest.find("::");
    rest = rest.substr(pos + 2);
    pos = rest.find("::");
    return rest.substr(0, pos);
}
string parseResultDBName(string message) {
    int pos = message.rfind("::");
    string rest = message.substr(0, pos);
    pos = rest.rfind("::");
    return rest.substr(pos + 2);
}
string parseResultDBuId(string message) {
    int pos = message.rfind("::");
    return message.substr(pos + 2);
}


int main() {
   
    // unsigned int last_user_id = 10; // последний идентификатор пользователя
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
                auto jsonData = json::parse(strMessage);
                
                PerSocketData* userData = (PerSocketData*)ws->getUserData();
                string authorId = userData->uId;
                //ws->send(message, opCode, true); обратная отправка сообщений
                // вызывается при получении сообщения от пользователя

                if (isMessageTo(strMessage)) {
                    // подготовить данные и отправить их
                    string receiverId = parseUserId(strMessage);
                    string text = parseUserText(strMessage);
                    // отправить получателю
                    if (receiverId == "0") {
                        // userData->user_id == отправитель
                        string outgoingMessage = messageFromGlobal("0", userData->name, text);
                        ws->publish(BROADCAST_CHANNEL, outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else {
                        // userData->user_id == отправитель
                        string outgoingMessage = messageFromUser(authorId, userData->name, text);
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
                if (isSignNewUser(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE + "Ошибка соединения с сервером данных", uWS::OpCode::TEXT, false);
                        return;
                    }
                    string loginUser = jsonData["loginSignUp"];
                    string passUser = jsonData["passSignUp"];
                    string nickName = jsonData["userNameSignUp"];
                    json jsonOut = {
                            {"loginUser", loginUser},
                            {"passUser", passUser},
                            {"nickName", nickName},
                            {"authorId", authorId}
                    };
                    string outgoingMessage = FORDB + SQL + INSERT + SIGNUP + (string)jsonOut.dump();
                    cout << endl << outgoingMessage << endl;
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isAuthUser(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE + "Ошибка соединения с сервером данных", uWS::OpCode::TEXT, false);
                        return;
                    }
                    string loginUser = jsonData["loginAuth"];
                    string passUser = jsonData["passAuth"];
                    json jsonOut = {
                            {"loginUser", loginUser},
                            {"passUser", passUser},
                            {"authorId", authorId}
                    };
                    string outgoingMessage = FORDB + SQL + SELECT + AUTH + (string)jsonOut.dump();
                    cout << endl << outgoingMessage << endl;
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isConnectionServerDB(jsonData["type"])) {
                    if (isTrustServer(jsonData["key"])) {
                        PerSocketData* userData = (PerSocketData*)ws->getUserData();
                        deleteName(userData);
                        userData->name = "ServerDB";
                        userData->uId = "999";
                        updateName(userData);
                        string userChannel = "user#" + userData->uId;
                        ws->subscribe(userChannel);
                        string outgoingMessage = FORDB + INFO + "The database server has been checked and connected successfully";
                        ws->publish(userChannel, outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                }
                if (isResultFromDB(jsonData["type"])) {
                    if (jsonData["success"]) {
                        string authorId = jsonData["authorId"];
                        string name = jsonData["nickName"];
                        string uId = jsonData["tag"];
                        cout << endl << authorId;
                        cout << endl << name;
                        cout << endl << uId;
                        string outgoingMsg = "SUCCESS::Успешная авторизация";
                        ws->publish("user#" + authorId, RESULTDB + outgoingMsg, uWS::OpCode::TEXT, false);
                    }
                    else {
                        string authorId = jsonData["authorId"];
                        cout << endl << authorId;
                        string outgoingMsg = "ERROR::Ошибка авторизации\nНеверные данные для входа";
                        ws->publish("user#" + authorId, RESULTDB + outgoingMsg, uWS::OpCode::TEXT, false);
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
