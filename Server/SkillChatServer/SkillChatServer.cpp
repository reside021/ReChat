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
map<string, string> ghostUserNames;


// [SERVER] - константы для сервера
const string BROADCAST_CHANNEL = "broadcast";
const string MESSAGE_TO = "MESSAGE_TO::";
const string MESSAGEFROM = "MESSAGE_FROM::";
const string SET_NAME = "SETNAME::";
const string OFFLINE = "OFFLINE::";
const string ONLINE = "ONLINE::";
const string SIGNUP = "SIGNUP::";
const string DBSERVER = "DBSERVER";
const string CONFIRM = "CONFIRM";
const string F2A = "2FA";
const string FORDB = "FORDB::";
const string INFO = "INFO::";
const string SQL = "SQL::";
const string DBNOTACTIVE = "DBNOTACTIVE::";
const string INSERT = "INSERT::";
const string AUTH = "AUTH::";
const string SELECT = "SELECT::";
const string RESULTDB = "RESULTDB::";
const string UPDATE = "UPDATE::";
const string NEWNAME = "NEWNAME::";
const string SUCCESS = "SUCCESS::";
const string _ERROR = "ERROR::";
const string VISIBLE = "VISIBLE::";
const string NEWUSERDLG = "NEWUSERDLG::";
const string NEWMSGDLG = "NEWMSGDLG::";
const string DOWNLOAD = "DOWNLOAD::";
const string ALLDLG = "ALLDLG::";
const string ALLMSG = "ALLMSG::";
const string ALLTAGNAME = "ALLTAGNAME::";

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

void updateGhostUser(PerSocketData* data) {
    ghostUserNames[data->uId] = data->name;
}

void deleteGhostUser(PerSocketData* data) {
    ghostUserNames.erase(data->uId);
}
void restoreDataUser(string olduId, string newName, string new_uId) {
    userNames.erase(olduId);
    userNames[new_uId] = newName;
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
bool isUpdateVisible(string message) {
    return message.find(VISIBLE) == 0;
}
bool IsServerDBNotActive() {
    return userNames.find("999") == userNames.end();
}

bool isAuthUser(string message) {
    return message.find(AUTH) == 0;
}
bool isCreateDlg(string message) {
    return message.find(NEWUSERDLG) == 0;
}
bool isDownLoadData(string message) {
    return message.find(DOWNLOAD) == 0;
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
string parseIsVisible(bool isVisible) {
    if (isVisible)
        return "true";
    else
        return "false";
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
                    if (ghostUserNames.find(entry.first) != ghostUserNames.end()) break;
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

                if (isMessageTo(jsonData["type"])) {
                    // подготовить данные и отправить их
                    // string receiverId = parseUserId(strMessage);
                    // string text = parseUserText(strMessage);
                    string receiverId = jsonData["id"];
                    string text = jsonData["text"];
                    string dialog_id = jsonData["dialog_id"];
                    string typeMsg = jsonData["typeMsg"];
                    // отправить получателю
                    if (receiverId == "0") {
                        //json jsonOut = {
                        //    {"dialog_id", dialog_id},
                        //    {"sender", authorId},
                        //    {"typeMsg", typeMsg},
                        //    {"text", text},
                        //    {"receiverId", receiverId}
                        //};
                        ////string outgoingMessage = MESSAGEFROM + (string)jsonOut.dump();
                        ////ws->publish(BROADCAST_CHANNEL, outgoingMessage, uWS::OpCode::TEXT, false);
                        //string outgoingMessage = FORDB + SQL + INSERT + NEWMSGDLG + (string)jsonOut.dump();
                        //ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else {
                        json jsonOut = {
                            {"dialog_id", dialog_id},
                            {"sender", authorId},
                            {"typeMsg", typeMsg},
                            {"text", text},
                            {"receiverId", receiverId}
                        };
                        string outgoingMessage = FORDB + SQL + INSERT + NEWMSGDLG + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                        }
                    }
                if (isSetName(jsonData["type"])) {
                    string newName = jsonData["newUserName"];
                    if (jsonData["confirmSetname"]) {
                        ws->publish(BROADCAST_CHANNEL, offline(userData->uId));
                        userData->name = newName;
                        updateName(userData);
                        ws->publish(BROADCAST_CHANNEL, online(userData->uId));
                        cout << "User #" << userData->uId << " set their name" << endl;
                        return;
                    }
                    json jsonOut = {
                        {"tagId", authorId},
                        {"newName", newName}
                    };
                    string outgoingMessage = FORDB + SQL + UPDATE + NEWNAME + (string)jsonOut.dump();
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }

                if (isSignNewUser(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
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
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    cout << "User #" << authorId << " has registered" << endl;
                }
                if (isAuthUser(jsonData["type"])) {
                    if (jsonData["confirmAuth"]) {
                        ws->publish(BROADCAST_CHANNEL, offline(userData->uId));
                        deleteName(userData);
                        userData->name = jsonData["nickname"];
                        userData->uId = jsonData["tagUser"];
                        updateName(userData);
                        string userChannel = "user#" + userData->uId;
                        ws->subscribe(userChannel);
                        if (!jsonData["isVisible"]) {
                            updateGhostUser(userData);
                        }
                        else {
                            ws->publish(BROADCAST_CHANNEL, online(userData->uId));
                        }
                        cout << "User #" << authorId << " has been authorized -> new id: " << userData->uId << endl;
                        cout << "Users connected: " << userNames.size() << endl;
                        return;
                    }
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
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
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isUpdateVisible(jsonData["type"])) {
                    if (jsonData["confirmUpVisible"]) {
                        if (jsonData["isVisible"]) {
                            deleteGhostUser(userData);
                            ws->publish(BROADCAST_CHANNEL, online(userData->uId));
                            cout << "User #" << authorId << " is visible to everyone" << endl;
                        }
                        else {
                            updateGhostUser(userData);
                            ws->publish(BROADCAST_CHANNEL, offline(userData->uId));
                            cout << "User #" << authorId << " became a ghost" << endl;
                        }
                        return;
                    }
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    bool isVisible = jsonData["isVisible"];
                    json jsonOut = {
                            {"tagUser", authorId},
                            {"isVisible", isVisible}
                    };
                    string outgoingMessage = FORDB + SQL + UPDATE + VISIBLE + (string)jsonOut.dump();
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isConnectionServerDB(jsonData["type"])) {
                    if (isTrustServer(jsonData["key"])) {
                        deleteName(userData);
                        userData->name = "ServerDB";
                        userData->uId = "999";
                        updateName(userData);
                        string userChannel = "user#" + userData->uId;
                        ws->subscribe(userChannel);
                        updateGhostUser(userData);
                        string outgoingMessage = FORDB + INFO + "The database server has been checked and connected successfully";
                        ws->publish(userChannel, outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                }
                if (isCreateDlg(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    string userCompanion = jsonData["tagUser"];
                    string userManager = authorId;
                    json jsonOut = {
                            {"userCompanion", userCompanion},
                            {"userManager", userManager}
                    };
                    string outgoingMessage = FORDB + SQL + INSERT + NEWUSERDLG + (string)jsonOut.dump();
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isDownLoadData(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    if (jsonData["table"] == ALLDLG) {
                        json jsonOut = {
                            {"tagUser", jsonData["tagUser"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLDLG + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLMSG) {
                        json jsonOut = {
                            {"dialog_ids", jsonData["dialog_ids"]},
                            {"authorId", authorId}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLMSG + (string)jsonOut.dump();
                        cout << endl << "DOWNLOADALLMSG" << endl << outgoingMessage << endl;
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLTAGNAME) {
                        json jsonOut = {
                            {"dialog_ids", jsonData["dialog_ids"]},
                            {"authorId", authorId}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLTAGNAME + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                }
                if (isResultFromDB(jsonData["type"])) {
                    if (jsonData["oper"] == AUTH) {
                        if (jsonData["success"]) {
                            string authorId = jsonData["authorId"];
                            string name = jsonData["nickName"];
                            string uId = jsonData["tag"];
                            bool isVisible = jsonData["isVisible"];
                            json jsonOut = {
                                {"nickname", name},
                                {"tagUser", uId},
                                {"isVisible", isVisible}
                            };
                            string outgoingMsg = AUTH + SUCCESS + (string)jsonOut.dump();
                            ws->publish("user#" + authorId, RESULTDB + outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                        else {
                            string authorId = jsonData["authorId"];
                            string outgoingMsg = AUTH + _ERROR + "none";
                            ws->publish("user#" + authorId, RESULTDB + outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                    }
                    if (jsonData["oper"] == UPDATE) {
                        if (jsonData["typeUpdate"] == NEWNAME) {
                            if (!jsonData["newName"].empty()) {
                                if (jsonData["success"]) {
                                    string newName = jsonData["newName"];
                                    string authorId = jsonData["tagId"];
                                    string outgoingMsg = RESULTDB + UPDATE + SUCCESS + NEWNAME + newName;
                                    ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                                }
                                else {
                                    string authorId = jsonData["tagId"];
                                    string outgoingMsg = RESULTDB + UPDATE + _ERROR + NEWNAME;
                                    ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                                }
                            }
                        }
                        if (jsonData["typeUpdate"] == VISIBLE) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                string isVisible = parseIsVisible(jsonData["isVisible"]);
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + VISIBLE + isVisible;
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string authorId = jsonData["tagId"];
                                string outgoingMsg = RESULTDB + UPDATE + _ERROR + VISIBLE;
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        
                    }
                    if (jsonData["oper"] == NEWUSERDLG) {
                        if (jsonData["success"]) {
                            json jsonOut = {
                            {"Icreater", true},
                            {"dialog_id", jsonData["dialog_id"]},
                            {"userManager", jsonData["userManager"]},
                            {"enteredTime",jsonData["enteredTime"]},
                            {"userCompanion", jsonData["userCompanion"]}
                            };
                            string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut.dump();
                            string userManager = jsonData["userManager"];
                            string userCompanion = jsonData["userCompanion"];
                            ws->publish("user#" + userManager, outgoingMsg, uWS::OpCode::TEXT, false);
                            if (userManager != userCompanion) {
                                json jsonOut = {
                                {"Icreater", false},
                                {"dialog_id", jsonData["dialog_id"]},
                                {"userManager", jsonData["userManager"]},
                                {"enteredTime",jsonData["enteredTime"]},
                                {"userCompanion", jsonData["userCompanion"]}
                                };
                                string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut.dump();
                                ws->publish("user#" + userCompanion, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        else {
                            string outgoingMsg = RESULTDB + INSERT + _ERROR + NEWUSERDLG;
                            string authorId = jsonData["userManager"];
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                    }
                    if (jsonData["oper"] == NEWMSGDLG) {
                        if (jsonData["success"]) {
                            json jsonOut = {
                            {"dialog_id", jsonData["dialog_id"]},
                            {"sender", jsonData["sender"]},
                            {"typeMsg", jsonData["typeMsg"]},
                            {"textMsg",jsonData["textMsg"]},
                            {"timeCreated", jsonData["timeCreated"]},
                            {"receiverId", jsonData["receiverId"]}
                            };
                            string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWMSGDLG + (string)jsonOut.dump();
                            string sender = jsonData["sender"];
                            ws->publish("user#" + sender, outgoingMsg, uWS::OpCode::TEXT, false);

                            outgoingMsg = MESSAGEFROM + (string)jsonOut.dump();
                            string receiverId = jsonData["receiverId"];
                            ws->publish("user#" + receiverId, outgoingMsg, uWS::OpCode::TEXT, false);
                            cout << "User #" << sender << " wrote message to " << receiverId << endl;
                        }
                        else {
                            string outgoingMsg = RESULTDB + INSERT + _ERROR + NEWMSGDLG;
                            string authorId = jsonData["receiverId"];
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                    }
                    if (jsonData["oper"] == DOWNLOAD) {
                        if (jsonData["table"] == ALLDLG) {
                            if (jsonData["success"]) {
                                json jsonOut = {
                                    {"listOfData", jsonData["listOfData"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLDLG + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                cout << endl << "ALLDLG" << endl << outgoingMsg << endl;
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLDLG;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["table"] == ALLMSG) {
                            if (jsonData["success"]) {
                                json jsonOut = {
                                    {"listOfData", jsonData["listOfData"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLMSG + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                cout << endl << "ALLMSG" << endl << outgoingMsg << endl;
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLMSG;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["table"] == ALLTAGNAME) {
                            if (jsonData["success"]) {
                                json jsonOut = {
                                    {"listOfData", jsonData["listOfData"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLTAGNAME + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                cout << endl << "ALLTAGNAME" << endl << outgoingMsg << endl;
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLTAGNAME;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
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
