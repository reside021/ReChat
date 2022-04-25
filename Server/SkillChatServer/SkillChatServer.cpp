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
const string AUTHTOKEN = "AUTHTOKEN::";
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
const string SET_AVATAR = "SETAVATAR::";
const string DELETE_AVATAR = "DELETEAVATAR::";
const string FRND = "FRND::";
const string ADD = "ADD::";
const string _DELETE = "DELETE::";
const string CNFRMADD = "CNFRMADD::";
const string ALLFRND = "ALLFRND::";
const string FIND = "FIND::";
const string COUNTMSG = "COUNTMSG::";
const string VISIONDATA = "VISIONDATA::";
const string GENDER = "GENDER::";
const string BIRTHDAY = "BIRTHDAY::";
const string SOCSTATUS = "SOCSTATUS::";
const string COUNTRY = "COUNTRY::";
const string ABOUTME = "ABOUTME::";
const string ALLINFOUSERS = "ALLINFOUSERS::";

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

bool isSetAvatar(string message) {
    return message.find(SET_AVATAR) == 0;
}

bool isDeleteAvatar(string message) {
    return message.find(DELETE_AVATAR) == 0;
}


bool isMessageTo(string message) {
    return message.find(MESSAGE_TO) == 0;
}


string generateUUID() {
    boost::uuids::random_generator uuid_gen;
    boost::uuids::uuid u = uuid_gen();
    return to_string(u).substr(0, 8);
}



bool isSignNewUser(string message) {
    return message.find(SIGNUP) == 0;
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
bool isAuthToken(string message) {
    return message.find(AUTHTOKEN) == 0;
}
bool isCreateDlg(string message) {
    return message.find(NEWUSERDLG) == 0;
}
bool isFrnd(string message) {
    return message.find(FRND) == 0;
}
bool isDownLoadData(string message) {
    return message.find(DOWNLOAD) == 0;
}

bool isResultFromDB(string message) {
    return message.find(RESULTDB) == 0;
}
bool isUpdate(string message) {
    return message.find(UPDATE) == 0;
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
                //ws->publish(BROADCAST_CHANNEL, online(userData->uId));

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
                if (isDeleteAvatar(jsonData["type"])) {
                    json jsonOut = {
                        {"tagId", authorId}
                    };
                    string outgoingMessage = FORDB + SQL + UPDATE + DELETE_AVATAR + (string)jsonOut.dump();
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isSetAvatar(jsonData["type"])) {
                    if (jsonData["successSet"]) {
                        json jsonOut = {
                        {"tagId", authorId}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + SET_AVATAR + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
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
                }
                if (isAuthUser(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    if (jsonData["confirmAuth"]) {
                        ws->publish(BROADCAST_CHANNEL, offline(userData->uId));
                        deleteName(userData);
                        userData->name = jsonData["nickname"];
                        userData->uId = jsonData["tagUser"];                      
                        updateName(userData);
                        string userChannel = "user#" + userData->uId;
                        ws->subscribe(userChannel);
                        ws->subscribe(BROADCAST_CHANNEL);
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
                if (isAuthToken(jsonData["type"])) {
                    json jsonOut = {
                        {"tagUser", authorId},
                        {"token", jsonData["token"]}
                    };
                    string outgoingMessage = FORDB + SQL + SELECT + AUTHTOKEN + (string)jsonOut.dump();
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
                if (isUpdate(jsonData["type"])) {
                    if (jsonData["objectUpdate"] == COUNTMSG) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dialog", jsonData["dialog"]},
                            {"needTagUser", jsonData["tagUser"]},
                            {"countMsg", jsonData["countMsg"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + COUNTMSG + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == VISIONDATA) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdated", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + VISIONDATA + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == GENDER) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdated", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + GENDER + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == BIRTHDAY) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdatedString", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + BIRTHDAY + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == SOCSTATUS) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdatedString", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + SOCSTATUS + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == COUNTRY) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdatedString", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + COUNTRY + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["objectUpdate"] == ABOUTME) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"dataUpdatedString", jsonData["dataUpdated"]}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + ABOUTME + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
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
                    string userManager = authorId;
                    json jsonOut = {
                            {"userCompanion", jsonData["tagUsers"]},
                            {"userManager", userManager},
                            {"nameOfChat", jsonData["nameOfChat"]}
                    };
                    string outgoingMessage = FORDB + SQL + INSERT + NEWUSERDLG + (string)jsonOut.dump();
                    ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                }
                if (isFrnd(jsonData["type"])){
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    if (jsonData["typeAction"] == ADD) {
                        string authorName = userData->name;
                        json jsonOut = {
                                {"typeAction", jsonData["typeAction"]},
                                {"tagUserSender", authorId},
                                {"nameUserSender", authorName},
                                {"tagUserReceiver", jsonData["tagUserReceiver"]},
                                {"nameUserReceiver", jsonData["nameUserReceiver"]}
                        };
                        string outgoingMessage = FORDB + SQL + INSERT + FRND + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else if (jsonData["typeAction"] == CNFRMADD) {
                        string tagUserFriend = jsonData["tagUserFriend"];
                        json jsonOut = {
                            {"tagUserFriend", tagUserFriend},
                            {"tagUserOur", authorId}
                        };
                        string outgoingMessage = FORDB + SQL + UPDATE + FRND + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else if (jsonData["typeAction"] == _DELETE) {
                        string tagUserFriend = jsonData["tagUserFriend"];
                        string typeDelete = jsonData["typeDelete"];
                        json jsonOut = {
                            {"tagUserFriend", tagUserFriend},
                            {"tagUserOur", authorId},
                            {"typeDelete", typeDelete}
                        };
                        string outgoingMessage = FORDB + SQL + _DELETE + FRND + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    else if (jsonData["typeAction"] == FIND) {
                        string tagUserFriend = jsonData["tagUserFriend"];
                        json jsonOut = {
                            {"tagUserFriend", tagUserFriend},
                            {"tagUserOur", authorId}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + FRND + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                }
                if (isDownLoadData(jsonData["type"])) {
                    if (IsServerDBNotActive()) {
                        ws->publish("user#" + authorId, DBNOTACTIVE, uWS::OpCode::TEXT, false);
                        return;
                    }
                    if (jsonData["table"] == ALLDLG) {
                        json jsonOut = {
                            {"tagUser", jsonData["tagUser"]},
                            {"token", jsonData["token"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLDLG + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLMSG) {
                        json jsonOut = {
                            {"dialog_ids", jsonData["dialog_ids"]},
                            {"authorId", authorId},
                            {"token", jsonData["token"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLMSG + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLTAGNAME) {
                        json jsonOut = {
                            {"dialog_ids", jsonData["dialog_ids"]},
                            {"authorId", authorId},
                            {"token", jsonData["token"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLTAGNAME + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLFRND) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"token", jsonData["token"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLFRND + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                    if (jsonData["table"] == ALLINFOUSERS) {
                        json jsonOut = {
                            {"tagUser", authorId},
                            {"needTagUser", jsonData["tagUser"]},
                            {"isFriend", jsonData["isFriend"]}
                        };
                        string outgoingMessage = FORDB + SQL + SELECT + DOWNLOAD + ALLINFOUSERS + (string)jsonOut.dump();
                        ws->publish("user#999", outgoingMessage, uWS::OpCode::TEXT, false);
                    }
                }
                if (isResultFromDB(jsonData["type"])) {
                    if (jsonData["oper"] == SIGNUP) {
                        if (jsonData["success"]) {
                            string authorId = jsonData["authorId"];
                            string outgoingMsg = RESULTDB + INSERT + SUCCESS + SIGNUP;
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            cout << "User #" << authorId << " has registered" << endl;
                        }
                        else {
                            string authorId = jsonData["authorId"];
                            string outgoingMsg = RESULTDB + INSERT + _ERROR + SIGNUP;
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                    }
                    if (jsonData["oper"] == AUTH) {
                        if (jsonData["success"]) {
                            string authorId = jsonData["authorId"];
                            string token = jsonData["token"];
                            json jsonOut = {
                                {"dataUser", jsonData["dataUser"]},
                                {"token", token}
                            };
                            string outgoingMsg = RESULTDB + AUTH + SUCCESS + (string)jsonOut.dump();
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                        else {
                            string authorId = jsonData["authorId"];
                            string outgoingMsg = RESULTDB + AUTH + _ERROR + "none";
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                    }
                    if (jsonData["oper"] == AUTHTOKEN) {
                        if (jsonData["success"]) {
                            string authorId = jsonData["authorId"];
                            json jsonOut = {
                                {"dataUser", jsonData["dataUser"]}
                            };
                            string outgoingMsg = AUTHTOKEN + SUCCESS + (string)jsonOut.dump();
                            ws->publish("user#" + authorId, RESULTDB + outgoingMsg, uWS::OpCode::TEXT, false);
                        }
                        else {
                            string authorId = jsonData["authorId"];
                            string outgoingMsg = RESULTDB + AUTHTOKEN + _ERROR + "none";
                            ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
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
                        if (jsonData["typeUpdate"] == SET_AVATAR) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + SET_AVATAR;
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == DELETE_AVATAR) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + DELETE_AVATAR;
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == COUNTMSG) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dialog", jsonData["dialog"]},
                                   {"needTagUser", jsonData["needTagUser"]},
                                   {"countMsg", jsonData["countMsg"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + COUNTMSG + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == VISIONDATA) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataVisionOrGender", jsonData["dataVisionOrGender"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + VISIONDATA + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == GENDER) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataVisionOrGender", jsonData["dataVisionOrGender"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + GENDER + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == BIRTHDAY) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataUpdatedString", jsonData["dataUpdatedString"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + BIRTHDAY + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == SOCSTATUS) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataUpdatedString", jsonData["dataUpdatedString"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + SOCSTATUS + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == COUNTRY) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataUpdatedString", jsonData["dataUpdatedString"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + COUNTRY + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["typeUpdate"] == ABOUTME) {
                            if (jsonData["success"]) {
                                string authorId = jsonData["tagId"];
                                json jsonOut = {
                                   {"dataUpdatedString", jsonData["dataUpdatedString"]}
                                };
                                string outgoingMsg = RESULTDB + UPDATE + SUCCESS + ABOUTME + (string)jsonOut.dump();
                                ws->publish("user#" + authorId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                    }
                    if (jsonData["oper"] == NEWUSERDLG) {
                        if (jsonData["success"]) {
                            if (jsonData["typeOfDlg"] == 0) {
                                json jsonOut = {
                                    {"Icreater", true},
                                    {"dialog_id", jsonData["dialog_id"]},
                                    {"userManager", jsonData["userManager"]},
                                    {"enteredTime",jsonData["enteredTime"]},
                                    {"userCompanion", jsonData["userCompanion"]},
                                    {"countMsg", 0},
                                    {"lastTimeMsg", jsonData["lastTimeMsg"]},
                                    {"typeOfDlg", jsonData["typeOfDlg"]},
                                    {"rang", 1},
                                    {"nameOfChat", jsonData["nameOfChat"]}
                                };
                                string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut.dump();
                                string userManager = jsonData["userManager"];
                                string userCompanion = jsonData["userCompanion"][0];
                                ws->publish("user#" + userManager, outgoingMsg, uWS::OpCode::TEXT, false);
                                if (userManager != userCompanion) {
                                    json jsonOut = {
                                        {"Icreater", false},
                                        {"dialog_id", jsonData["dialog_id"]},
                                        {"userManager", jsonData["userManager"]},
                                        {"enteredTime",jsonData["enteredTime"]},
                                        {"userCompanion", jsonData["userCompanion"]},
                                        {"countMsg", 0},
                                        {"lastTimeMsg", jsonData["lastTimeMsg"]},
                                        {"typeOfDlg", jsonData["typeOfDlg"]},
                                        {"rang", 1},
                                        {"nameOfChat", jsonData["nameOfChat"]}
                                    };
                                    string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut.dump();
                                    ws->publish("user#" + userCompanion, outgoingMsg, uWS::OpCode::TEXT, false);
                                }
                            }
                            if (jsonData["typeOfDlg"] == 1) {
                                json jsonOut = {
                                    {"Icreater", true},
                                    {"dialog_id", jsonData["dialog_id"]},
                                    {"userManager", jsonData["userManager"]},
                                    {"enteredTime",jsonData["enteredTime"]},
                                    {"userCompanion", jsonData["userCompanion"]},
                                    {"countMsg", 0},
                                    {"lastTimeMsg", jsonData["lastTimeMsg"]},
                                    {"typeOfDlg", jsonData["typeOfDlg"]},
                                    {"rang", 3},
                                    {"nameOfChat", jsonData["nameOfChat"]}
                                };
                                string outgoingMsg = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut.dump();
                                string userManager = jsonData["userManager"];
                                ws->publish("user#" + userManager, outgoingMsg, uWS::OpCode::TEXT, false);
                                json jsonOut2 = {
                                        {"Icreater", false},
                                        {"dialog_id", jsonData["dialog_id"]},
                                        {"userManager", jsonData["userManager"]},
                                        {"enteredTime",jsonData["enteredTime"]},
                                        {"userCompanion", jsonData["userCompanion"]},
                                        {"countMsg", 0},
                                        {"lastTimeMsg", jsonData["lastTimeMsg"]},
                                        {"typeOfDlg", jsonData["typeOfDlg"]},
                                        {"rang", 1},
                                        {"nameOfChat", jsonData["nameOfChat"]}
                                };
                                string outgoingMsg2 = RESULTDB + INSERT + SUCCESS + NEWUSERDLG + (string)jsonOut2.dump();
                                for (string tagUser : jsonData["userCompanion"]) {
                                    if (userManager != tagUser) {
                                        ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                                    }
                                }
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
                            if (receiverId == "0") {
                                ws->publish(BROADCAST_CHANNEL, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else if (receiverId[0] == 'G') {
                                for (string tagUser : jsonData["listReceiverId"]) {
                                    ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                                }
                            }
                            else {
                                ws->publish("user#" + receiverId, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
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
                                    {"listOfData", jsonData["listOfData"]},
                                    {"token", jsonData["token"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLDLG + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
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
                                    {"listOfData", jsonData["listOfData"]},
                                    {"token", jsonData["token"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLMSG + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
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
                                    {"listOfData", jsonData["listOfData"]},
                                    {"token", jsonData["token"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLTAGNAME + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLTAGNAME;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["table"] == ALLFRND) {
                            if (jsonData["success"]) {
                                json jsonOut = {
                                    {"listOfData", jsonData["listOfData"]},
                                    {"token", jsonData["token"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLFRND + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLFRND;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        if (jsonData["table"] == ALLINFOUSERS) {
                            if (jsonData["success"]) {
                                json jsonOut = {
                                    {"dataUser", jsonData["dataUsers"]}
                                };
                                string outgoingMsg = RESULTDB + DOWNLOAD + SUCCESS + ALLINFOUSERS + (string)jsonOut.dump();
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                            else {
                                string outgoingMsg = RESULTDB + DOWNLOAD + _ERROR + ALLINFOUSERS;
                                string tagUser = jsonData["tagUser"];
                                ws->publish("user#" + tagUser, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                    }
                    if (jsonData["oper"] == FRND) {
                        if (jsonData["typeAction"] == ADD) {
                            if (jsonData["success"]) {
                                string tagUserSender = jsonData["tagUserSender"];
                                string nameUserSender = jsonData["nameUserSender"];
                                string tagUserReceiver = jsonData["tagUserReceiver"];
                                string nameUserReceiver = jsonData["nameUserReceiver"];
                                json jsonOut = {
                                {"tagUserSender", tagUserSender},
                                {"nameUserSender", nameUserSender},
                                {"tagUserReceiver", tagUserReceiver},
                                {"nameUserReceiver", nameUserReceiver},
                                };
                                string outgoingMsg = RESULTDB + FRND + SUCCESS + ADD + (string)jsonOut.dump();
                                ws->publish("user#" + tagUserSender, outgoingMsg, uWS::OpCode::TEXT, false);
                                ws->publish("user#" + tagUserReceiver, outgoingMsg, uWS::OpCode::TEXT, false);
                                cout << "User #" << tagUserSender << " added " << tagUserReceiver << " as a friend " << endl;
                            }
                            else {
                                string outgoingMsg = RESULTDB + FRND + _ERROR + ADD;
                                string tagUserSender = jsonData["tagUserSender"]; 
                                ws->publish("user#" + tagUserSender, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        else if (jsonData["typeAction"] == _DELETE) {
                            if (jsonData["success"]) {
                                string tagUserFriend = jsonData["tagUserReceiver"];
                                string tagUserOur = jsonData["tagUserSender"];
                                string typeDelete = jsonData["typeDelete"];
                                json jsonOut = {
                                {"tagUserFriend", tagUserFriend},
                                {"tagUserOur", tagUserOur},
                                {"typeDelete", typeDelete}
                                };
                                string outgoingMsg = RESULTDB + FRND + SUCCESS + _DELETE + (string)jsonOut.dump();
                                ws->publish("user#" + tagUserFriend, outgoingMsg, uWS::OpCode::TEXT, false);
                                ws->publish("user#" + tagUserOur, outgoingMsg, uWS::OpCode::TEXT, false);
                                cout << "User #" << tagUserOur << " deleted " << tagUserFriend << " from friends " << endl;
                            }
                            else {
                                string outgoingMsg = RESULTDB + FRND + _ERROR + CNFRMADD;
                                string tagUserOur = jsonData["tagUserOur"];
                                cout << endl << outgoingMsg << endl;
                                ws->publish("user#" + tagUserOur, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        else if (jsonData["typeAction"] == CNFRMADD) {
                            if (jsonData["success"]) {
                                string tagUserFriend = jsonData["tagUserReceiver"];
                                string tagUserOur = jsonData["tagUserSender"];
                                json jsonOut = {
                                {"tagUserFriend", tagUserFriend},
                                {"tagUserOur", tagUserOur},
                                };
                                string outgoingMsg = RESULTDB + FRND + SUCCESS + CNFRMADD + (string)jsonOut.dump();
                                ws->publish("user#" + tagUserFriend, outgoingMsg, uWS::OpCode::TEXT, false);
                                ws->publish("user#" + tagUserOur, outgoingMsg, uWS::OpCode::TEXT, false);
                                cout << "User #" << tagUserOur << " added " << tagUserFriend << " as a friend " << endl;
                            }
                            else {
                                string outgoingMsg = RESULTDB + FRND + _ERROR + CNFRMADD;
                                string tagUserOur = jsonData["tagUserOur"];
                                ws->publish("user#" + tagUserOur, outgoingMsg, uWS::OpCode::TEXT, false);
                            }
                        }
                        else if (jsonData["typeAction"] == FIND) {
                            if (jsonData["success"]) {
                                string tagUserFriend = jsonData["tagUserReceiver"];
                                string nameUserFriend = jsonData["nameUserReceiver"];
                                string tagUserSender = jsonData["tagUserSender"];
                                json jsonOut = {
                                {"tagUserFriend", tagUserFriend},
                                {"nameUserFriend", nameUserFriend}
                                };
                                string outgoingMsg = RESULTDB + FRND + SUCCESS + FIND + (string)jsonOut.dump();
                                ws->publish("user#" + tagUserSender, outgoingMsg, uWS::OpCode::TEXT, false);
                                cout << "User #" << tagUserSender << " added " << tagUserFriend << " as a friend " << endl;
                            }
                            else {
                                string outgoingMsg = RESULTDB + FRND + _ERROR + FIND;
                                string tagUserSender = jsonData["tagUserSender"];
                                ws->publish("user#" + tagUserSender, outgoingMsg, uWS::OpCode::TEXT, false);
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
