#include <iostream>
#include <uwebsockets/App.h>
#include <map>

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
const string SIGNUP = "signup::";

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


// [SQL] определение хендлов и переменных
SQLHANDLE sqlConnHandle;
SQLHANDLE sqlStmtHandle;
SQLHANDLE sqlEnvHandle;
SQLWCHAR retconstring[SQL_RETURN_CODE_LEN];
const string SQL_MSG = "[SQL_PART] ";

// [SQL] закрытие соединения и осовбождение ресурсов
void completed() {
    SQLFreeHandle(SQL_HANDLE_STMT, sqlStmtHandle);
    SQLDisconnect(sqlConnHandle);
    SQLFreeHandle(SQL_HANDLE_DBC, sqlConnHandle);
    SQLFreeHandle(SQL_HANDLE_ENV, sqlEnvHandle);
}

void connectToDB() {
    // [SQL] инициализация
    sqlConnHandle = NULL;
    sqlStmtHandle = NULL;
    // [SQL] allocations
    if (SQL_SUCCESS != SQLAllocHandle(SQL_HANDLE_ENV, SQL_NULL_HANDLE, &sqlEnvHandle)) completed();
    if (SQL_SUCCESS != SQLSetEnvAttr(sqlEnvHandle, SQL_ATTR_ODBC_VERSION, (SQLPOINTER)SQL_OV_ODBC3, 0)) completed();
    if (SQL_SUCCESS != SQLAllocHandle(SQL_HANDLE_DBC, sqlEnvHandle, &sqlConnHandle)) completed();

    // [SQL] Попытка подключения к sql server
    // [SQL] Используется защищенное подключение по порту 1433
    // [SQL] не используется подключение по имени/паролю из соображений безопасности

    switch (SQLDriverConnect(sqlConnHandle,
        NULL,
        //(SQLWCHAR*)L"DRIVER={SQL Server};SERVER=localhost, 1433;DATABASE=ReChat;UID=username;PWD=password;",
        (SQLWCHAR*)L"DRIVER={SQL Server};SERVER=localhost, 1433;DATABASE=ReChat;Trusted=true;",
        SQL_NTS,
        retconstring,
        1024,
        NULL,
        SQL_DRIVER_NOPROMPT)) {
    case SQL_SUCCESS:
        cout << SQL_MSG + "Successfully connected to SQL Server\n";
        break;
    case SQL_SUCCESS_WITH_INFO:
        cout << SQL_MSG + "Successfully connected to SQL Server\n";
        break;
    case SQL_INVALID_HANDLE:
        cout << SQL_MSG + "Could not connect to SQL Server\n";
        completed();
    case SQL_ERROR:
        cout << SQL_MSG + "Could not connect to SQL Server\n";
        completed();
    default:
        break;
    }
    // [SQL] Если присутствует проблема подключения, то приложение закроется
    if (SQL_SUCCESS != SQLAllocHandle(SQL_HANDLE_STMT, sqlConnHandle, &sqlStmtHandle)) completed();

    // [SQL] Выполение SQL-запроса, если бдует ошибка, то приложение закроется, иначе выдаст результат запроса
    if (SQL_SUCCESS != SQLExecDirect(sqlStmtHandle, (SQLWCHAR*)L"SELECT @@VERSION", SQL_NTS)) {
        cout << SQL_MSG + "Error querying SQL Server\n";
        completed();
    }
    else {
        // [SQL] объявление выходных данных
        SQLCHAR sqlResult[SQL_RESULT_LEN];
        SQLINTEGER ptrSqlVersion;
        while (SQLFetch(sqlStmtHandle) == SQL_SUCCESS) {
            SQLGetData(sqlStmtHandle, 1, SQL_CHAR, sqlResult, SQL_RESULT_LEN, &ptrSqlVersion);
        }
        cout << SQL_MSG + "Query Result:\n\n";
        cout << sqlResult << endl;
    }
}

std::wstring s2ws(const std::string& s)
{
    int len;
    int slength = (int)s.length() + 1;
    len = MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, 0, 0);
    wchar_t* buf = new wchar_t[len];
    MultiByteToWideChar(CP_ACP, 0, s.c_str(), slength, buf, len);
    std::wstring r(buf);
    delete[] buf;
    return r;
}

bool addNewUserInDB(string login, string pass, string nickname) {
    string query = "insert into UsersData values(" + login + "," + pass + "," + nickname + ")";
    return true;
   // std::wstring stemp = s2ws(query);
   // SQLWCHAR* result = (SQLWCHAR*)stemp.c_str();
    //wstring query = L"insert into UsersData values(" + to_wstring()

   // return SQL_SUCCESS == SQLExecDirect(sqlStmtHandle, (SQLWCHAR*)stemp, SQL_NTS));
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
                if (isSignNewUser(strMessage)) {
                    string loginUser = parseNewUserLogin(strMessage);
                    string passUser = parseNewUserPassword(strMessage);
                    string nickName = parseNewUserNickname(strMessage) + "_" + authorId;
                    cout << endl << loginUser;
                    cout << endl << passUser;
                    cout << endl << nickName;
                    cout << endl;
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
                connectToDB(); // подключение к бд
                }).run(); // запуск
}
