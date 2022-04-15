using System;
using WebSocket4Net;
using System.Data.SqlClient;
using Newtonsoft.Json;
using System.Collections.Generic;
using JWT.Builder;
using JWT.Algorithms;
using WSClientDB.dataClasses;

namespace WSClientDB
{
    class Program
    {
        const string FORDB = "FORDB::";
        const string INFO = "INFO::";
        const string SQL = "SQL::";
        const string INSERT = "INSERT::";
        const string SIGNUP = "SIGNUP::";
        const string SELECT = "SELECT::";
        const string AUTH = "AUTH::";
        const string AUTHTOKEN = "AUTHTOKEN::";
        const string RESULTDB = "RESULTDB::";
        const string UPDATE = "UPDATE::";
        const string NEWNAME = "NEWNAME::";
        const string VISIBLE = "VISIBLE::";
        const string SETAVATAR = "SETAVATAR::";
        const string DELETEAVATAR = "DELETEAVATAR::";
        const string NEWUSERDLG = "NEWUSERDLG::";
        const string CHAT = "CHAT#";
        const string NEWMSGDLG = "NEWMSGDLG::";
        const string DOWNLOAD = "DOWNLOAD::";
        const string ALLDLG = "ALLDLG::";
        const string ALLMSG = "ALLMSG::";
        const string ALLTAGNAME = "ALLTAGNAME::";
        const string GROUP = "GROUP#";
        const string FRND = "FRND::";
        const string ADD = "ADD::";
        const string DELETE = "DELETE::";
        const string CNFRMADD = "CNFRMADD::";
        const string ALLFRND = "ALLFRND::";
        const string FIND = "FIND::";
        const string COUNTMSG = "COUNTMSG::";

        static WebSocket webSocket;
        static SqlConnection sqlConnection;
        static SqlCommand sqlCommand;

        static void Main(string[] args)
        {

            webSocket = new WebSocket("ws://servchat.ddns.net:9001/");
            webSocket.Opened += WebSocket_Opened;
            webSocket.Error += WebSocket_Error;
            webSocket.Closed += WebSocket_Closed;
            webSocket.MessageReceived += WebSocket_MessageReceived;
            webSocket.Open();
            Console.ReadKey();
        }

        private static void InsertDataSignUp(string loginUser, string passUser, string nickUser, string tagUser)
        {
            ResultDB resultDB = new ResultDB();
            resultDB.type = RESULTDB;
            resultDB.oper = SIGNUP;
            resultDB.authorId = tagUser;
            resultDB.success = false;
            bool check_1 = false;
            bool check_2 = false;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText = "Insert into InfoUsers values(@taguser, @nickuser, @isVisible, @isAvatar)";
                SqlParameter sqlParameter = new SqlParameter("@taguser", tagUser);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter1 = new SqlParameter("@nickuser", nickUser);
                sqlCommand.Parameters.Add(sqlParameter1);
                SqlParameter sqlParameter2 = new SqlParameter("@isVisible", false);
                sqlCommand.Parameters.Add(sqlParameter2);
                SqlParameter sqlParameter3 = new SqlParameter("@isAvatar", false);
                sqlCommand.Parameters.Add(sqlParameter3);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                sqlConnection.Close();
                check_1 = true;
            }
            catch
            {
                sqlConnection.Close();
                check_1 = false;
            }
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText = "Insert into UsersData values(@loginuser, @passuser, @taguser,@deviceToken)";
                SqlParameter sqlParameter4 = new SqlParameter("@loginuser", loginUser);
                sqlCommand.Parameters.Add(sqlParameter4);
                SqlParameter sqlParameter5 = new SqlParameter("@passuser", passUser);
                sqlCommand.Parameters.Add(sqlParameter5);
                SqlParameter sqlParameter6 = new SqlParameter("@taguser", tagUser);
                sqlCommand.Parameters.Add(sqlParameter6);
                SqlParameter sqlParameter7 = new SqlParameter("@deviceToken", "");
                sqlCommand.Parameters.Add(sqlParameter7);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                sqlConnection.Close();
                check_2 = true;
            }
            catch
            {
                sqlConnection.Close();
                check_2 = false;
            }
            if ((check_1 && check_2) == true)
            {
                resultDB.success = true;
                AddInGlobalChat(tagUser);
            }
                string jsonResult = JsonConvert.SerializeObject(resultDB);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> SIGNUP^Insert into InfoUsers ({check_1}) and UsersData ({check_2}) - ({resultDB.success})");
        }
        private static void InsertDataNewMsgDLG(string dialog_id, string sender, string typeMsg, string text, string receiverId)
        {
            var nameSender = SlctAllTagNameHelper(sender);
            int timeCreated = (int)(DateTime.UtcNow - new DateTime(1970, 1, 1)).TotalSeconds;
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Insert into MsgDlgTable values(@dialog_id, @sender, @typeMsg, @textMsg, @timeCreated)";
            SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@sender", sender);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlParameter sqlParameter2 = new SqlParameter("@typeMsg", typeMsg);
            sqlCommand.Parameters.Add(sqlParameter2);
            SqlParameter sqlParameter3 = new SqlParameter("@textMsg", text);
            sqlCommand.Parameters.Add(sqlParameter3);
            SqlParameter sqlParameter4 = new SqlParameter("@timeCreated", timeCreated);
            sqlCommand.Parameters.Add(sqlParameter4);
            SuccessInsertMsgDlg successInsertMsgDlg = new SuccessInsertMsgDlg();
            try
            {
                sqlCommand.ExecuteNonQuery();
                sqlCommand.CommandText = "Update UserDlgTable set lastTimeMsg = @timeCreated where dialog_id = @dialog_id";
                sqlCommand.ExecuteNonQuery();
                successInsertMsgDlg.type = RESULTDB;
                successInsertMsgDlg.oper = NEWMSGDLG;
                successInsertMsgDlg.success = true;
                successInsertMsgDlg.dialog_id = dialog_id;
                successInsertMsgDlg.sender = sender;
                successInsertMsgDlg.typeMsg = typeMsg;
                successInsertMsgDlg.textMsg = text;
                successInsertMsgDlg.timeCreated = timeCreated.ToString();
                successInsertMsgDlg.receiverId = receiverId;
                successInsertMsgDlg.nameSender = nameSender;
            }
            catch
            {
                successInsertMsgDlg.type = RESULTDB;
                successInsertMsgDlg.oper = NEWMSGDLG;
                successInsertMsgDlg.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successInsertMsgDlg);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> InsertDLG^Insert into MsgDlgTable");
        }
        private static void InsertDataNewUserDLG(string userCompanion, string userManager)
        {
            string dialog_id = CHAT + userCompanion + "::" + userManager;
            DateTime enteredTime = DateTime.UtcNow;
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText =
                @"Insert into UserDlgTable values
                    (@dialog_id, @userCompanion, @enteredTime, 0, @enteredTime),
                    (@dialog_id, @userManager, @enteredTime, 0, @enteredTime)";
            SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@userCompanion", userCompanion);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlParameter sqlParameter2 = new SqlParameter("@enteredTime", enteredTime);
            sqlCommand.Parameters.Add(sqlParameter2);
            SqlParameter sqlParameter3 = new SqlParameter("@userManager", userManager);
            sqlCommand.Parameters.Add(sqlParameter3);
            SuccessCreateUserDlg successCreateUserDlg = new SuccessCreateUserDlg();
            try
            {
                sqlCommand.ExecuteNonQuery();
                successCreateUserDlg.type = RESULTDB;
                successCreateUserDlg.oper = NEWUSERDLG;
                successCreateUserDlg.success = true;
                successCreateUserDlg.dialog_id = dialog_id;
                successCreateUserDlg.userManager = userManager;
                successCreateUserDlg.userCompanion = userCompanion;
                successCreateUserDlg.enteredTime = enteredTime.ToString();
            }
            catch
            {
                successCreateUserDlg.type = RESULTDB;
                successCreateUserDlg.oper = NEWUSERDLG;
                successCreateUserDlg.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successCreateUserDlg);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> CreateDLG^Insert into UserDlgTable");
        }
        private static string getToken(string tagUser)
        {
            var token = JwtBuilder.Create()
                      .WithAlgorithm(new HMACSHA256Algorithm()) // symmetric
                      .WithSecret(tagUser)
                      .AddClaim("tagUser", tagUser)
                      .AddClaim("timeEntry", DateTime.Now)
                      .AddClaim("iss", "Server_ReChat")
                      .Encode();
            return token;
        }
        private static void SelectDataForAuth(string authorUser, string loginUser, string passUser)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = 
                @"Select u.loginUser, u.passUser, i.nickUser, u.tagUser, i.isVisible, i.isAvatar from UsersData as u 
                    inner join InfoUsers as i
                    on i.tagUser = u.tagUser
                    where loginUser = @login";
            SqlParameter sqlParameter = new SqlParameter("@login", loginUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
            string loginDB = "", passDB = "", nickDB = "", tagDB = "";
            bool isVisible = false;
            bool isAvatar = false;
            ResultDB result = new ResultDB();
            if (sqlDataReader.HasRows)
            {
                while (sqlDataReader.Read())
                {
                    loginDB = sqlDataReader.GetString(0);
                    passDB = sqlDataReader.GetString(1);
                    nickDB = sqlDataReader.GetString(2);
                    tagDB = sqlDataReader.GetString(3);
                    isVisible = sqlDataReader.GetBoolean(4);
                    isAvatar = sqlDataReader.GetBoolean(5);
                }
                if(passDB == passUser)
                {
                    result.type = RESULTDB;
                    result.oper = AUTH;
                    result.success = true;
                    result.authorId = authorUser;
                    result.tag = tagDB;
                    result.isVisible = isVisible;
                    result.isAvatar = isAvatar;
                    result.nickName = nickDB;
                    result.token = getToken(tagDB);
                    string jsonResult = JsonConvert.SerializeObject(result);
                    webSocket.Send(jsonResult);
                }
                else
                {
                    result.type = RESULTDB;
                    result.oper = AUTH;
                    result.success = false;
                    result.authorId = authorUser;
                    string jsonResult = JsonConvert.SerializeObject(result);
                    webSocket.Send(jsonResult);
                }
            }
            else
            {
                result.type = RESULTDB;
                result.oper = AUTH;
                result.success = false;
                result.authorId = authorUser;
                string jsonResult = JsonConvert.SerializeObject(result);
                webSocket.Send(jsonResult);
            }
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            if (result.success)
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText = "Update UsersData set deviceToken = @token where tagUser = @tagUser";
                sqlParameter = new SqlParameter("@token", result.token);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter1 = new SqlParameter("@tagUser", tagDB);
                sqlCommand.Parameters.Add(sqlParameter1);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                sqlConnection.Close();
            }
            Console.WriteLine($"[MSG] -> AUTH^{nickDB}_{tagDB} -> {result.success}");
        }
        private static List<string> GetInfoAboutDialogs(string tagUser)
        {
            List<string> listDlg = new List<string>();
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "select dialog_id from UserDlgTable where tagUser = @tagUser";
            SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
            if (sqlDataReader.HasRows)
            {
                while (sqlDataReader.Read())
                {
                    listDlg.Add(sqlDataReader.GetString(0));
                }
            }
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            return listDlg;
        }
        private static void SelectDataForAllDlg(string tagUser, string token)
        {
            var listDlg = GetInfoAboutDialogs(tagUser);
            ListDataOfDialog listDataOfDialog = new ListDataOfDialog();
            List<DataOfDialog> dataOfDialogs = new List<DataOfDialog>();
            foreach (var dlg in listDlg)
            {
                if(dlg.Substring(0, dlg.IndexOf("#") + 1) == GROUP)
                {
                    sqlConnection.Open();
                    sqlCommand.Connection = sqlConnection;
                    sqlCommand.CommandText = " select * from UserDlgTable where dialog_id = @dlgId and tagUser = @tagUser";
                    SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
                    sqlCommand.Parameters.Add(sqlParameter);
                    SqlParameter sqlParameter1 = new SqlParameter("@dlgId", dlg);
                    sqlCommand.Parameters.Add(sqlParameter1);
                    SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
                    if (sqlDataReader.HasRows)
                    {
                        while (sqlDataReader.Read())
                        {
                            string _dialogId = sqlDataReader.GetString(1);
                            string _tagUser = dlg.Substring(dlg.IndexOf("#") + 1);
                            string _enteredTime = sqlDataReader.GetDateTime(3).ToString();
                            int _countMsg = sqlDataReader.GetInt32(4);
                            int _lastTimeMsg = sqlDataReader.GetInt32(5);
                            dataOfDialogs.Add(new DataOfDialog() { dialog_id = _dialogId, tagUser = _tagUser, enteredTime = _enteredTime, countMsg = _countMsg, lastTimeMsg = _lastTimeMsg});
                        }
                    }
                    sqlCommand.Parameters.Clear();
                    sqlConnection.Close();
                }
                else
                {
                    sqlConnection.Open();
                    sqlCommand.Connection = sqlConnection;
                    sqlCommand.CommandText = " select * from UserDlgTable where dialog_id = @dlgId and tagUser <> @tagUser";
                    SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
                    sqlCommand.Parameters.Add(sqlParameter);
                    SqlParameter sqlParameter1 = new SqlParameter("@dlgId", dlg);
                    sqlCommand.Parameters.Add(sqlParameter1);
                    SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
                    if (sqlDataReader.HasRows)
                    {
                        while (sqlDataReader.Read())
                        {
                            string _dialogId = sqlDataReader.GetString(1);
                            string _tagUser = sqlDataReader.GetString(2);
                            string _enteredTime = sqlDataReader.GetDateTime(3).ToString();
                            int _countMsg = sqlDataReader.GetInt32(4);
                            int _lastTimeMsg = sqlDataReader.GetInt32(5);
                            dataOfDialogs.Add(new DataOfDialog() { dialog_id = _dialogId, tagUser = _tagUser, enteredTime = _enteredTime, countMsg = _countMsg, lastTimeMsg = _lastTimeMsg});
                        }
                    }
                    sqlCommand.Parameters.Clear();
                    sqlConnection.Close();
                }
            }
            listDataOfDialog.type = RESULTDB;
            listDataOfDialog.oper = DOWNLOAD;
            listDataOfDialog.table = ALLDLG;
            listDataOfDialog.success = true;
            listDataOfDialog.listOfData = dataOfDialogs;
            listDataOfDialog.tagUser = tagUser;
            listDataOfDialog.token = token;
            string jsonResult = JsonConvert.SerializeObject(listDataOfDialog);
            Console.WriteLine(jsonResult);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> DownLoadDialog^{tagUser}");
        }

        private static List<DataOfMessage> SlctAllMsgHelper(string dialog_id)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "select * from MsgDlgTable where dialog_id = @dialog_id";
            SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
            List<DataOfMessage> dataOfMessages = new List<DataOfMessage>();
            if (sqlDataReader.HasRows)
            {
                while (sqlDataReader.Read())
                {
                    string _dialogId = sqlDataReader.GetString(1);
                    string _sender = sqlDataReader.GetString(2);
                    string _typeMsg = sqlDataReader.GetString(3);
                    string _textMsg = sqlDataReader.GetString(4);
                    int _timeCreated = sqlDataReader.GetInt32(5);
                    dataOfMessages.Add(new DataOfMessage()
                    {
                        dialog_id = _dialogId,
                        sender = _sender,
                        typeMsg = _typeMsg,
                        textMsg = _textMsg,
                        timeCreated = _timeCreated
                    });
                }
            }
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            return dataOfMessages;
        }
        private static void SelectDataForAllMsg(List<string> dialog_ids, string authorId, string token)
        {
            foreach(var el in dialog_ids)
            {
                List<DataOfMessage> dataOfMessages = SlctAllMsgHelper(el);
                ListDataOfMessage listDataOfMessage = new ListDataOfMessage();
                if (dataOfMessages.Count != 0)
                {
                    listDataOfMessage.type = RESULTDB;
                    listDataOfMessage.oper = DOWNLOAD;
                    listDataOfMessage.table = ALLMSG;
                    listDataOfMessage.success = true;
                    listDataOfMessage.listOfData = dataOfMessages;
                    listDataOfMessage.tagUser = authorId;
                    listDataOfMessage.token = token;
                    string jsonResult = JsonConvert.SerializeObject(listDataOfMessage);
                    webSocket.Send(jsonResult);
                }
            }
            Console.WriteLine($"[MSG] -> DownLoadMsg^{authorId}");
        }
        private static string SearchNeedTag(string dialog_id, string notNeededTag)
        {
            string name = "";
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "select tagUser from UserDlgTable where dialog_id = @dialog_id and tagUser <> @notNeededTag";
            SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter2 = new SqlParameter("@notNeededTag", notNeededTag);
            sqlCommand.Parameters.Add(sqlParameter2);
            var objectName = sqlCommand.ExecuteScalar();
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            if (objectName != null) name = objectName.ToString();
            return name;
        }
        private static void SelectDataForAllTagName(List<string> dialog_ids, string authorId, string token)
        {
            List<DataOfNickName> listTagName = new List<DataOfNickName>();
            foreach (var el in dialog_ids)
            {
                if (el.Substring(0, el.IndexOf("#") + 1) == GROUP)
                {
                    listTagName.Add(new DataOfNickName() { nickUser = "Global Chat", tagUser = el.Substring(el.IndexOf("#") + 1) });
                }
                if (el.Substring(0, el.IndexOf("#") + 1) == CHAT)
                {
                    var tag_tag = el.Substring(CHAT.Length);
                    var tag1 = tag_tag.Substring(0, tag_tag.IndexOf("::"));
                    var tag2 = tag_tag.Substring(tag_tag.IndexOf("::") + 2);
                    string tag = "";
                    if (tag1 == tag2)
                    {
                        tag = SearchNeedTag(el, tag1);
                    }
                    else
                    {
                        if (tag1 == authorId)
                        {
                            tag = SearchNeedTag(el, tag1);
                        }
                        if (tag2 == authorId)
                        {
                            tag = SearchNeedTag(el, tag2);
                        }
                    }
                    string name = SlctAllTagNameHelper(tag);
                    if (!string.IsNullOrEmpty(name))
                    {
                        listTagName.Add(new DataOfNickName() { nickUser = name, tagUser = tag });
                    }
                }
            }
            DataOfTagName dataOfTagName = new DataOfTagName();
            dataOfTagName.token = token;
            if (listTagName.Count != 0)
            {
                dataOfTagName.type = RESULTDB;
                dataOfTagName.oper = DOWNLOAD;
                dataOfTagName.table = ALLTAGNAME;
                dataOfTagName.success = true;
                dataOfTagName.listOfData = listTagName;
                dataOfTagName.tagUser = authorId;
            }
            else
            {
                dataOfTagName.type = RESULTDB;
                dataOfTagName.oper = DOWNLOAD;
                dataOfTagName.table = ALLTAGNAME;
                dataOfTagName.success = false;
                dataOfTagName.tagUser = authorId;
            }
            string jsonResult = JsonConvert.SerializeObject(dataOfTagName);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> DownLoadNickUser^{authorId} ({dataOfTagName.success})");
        }
        private static string SlctAllTagNameHelper(string tag)
        {
            string name = "";
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "select nickUser from InfoUsers where tagUser = @tagUser";
            SqlParameter sqlParameter = new SqlParameter("@tagUser", tag);
            sqlCommand.Parameters.Add(sqlParameter);
            var objectName = sqlCommand.ExecuteScalar();
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            if (objectName != null) name = objectName.ToString();
            return name;
        }
        private static void UpdateNameOfUser(string tagId, string newName)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Update InfoUsers set nickUser = @newnickuser where tagUser = @tagId";
            SqlParameter sqlParameter = new SqlParameter("@newnickuser", newName);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@tagId", tagId);
            sqlCommand.Parameters.Add(sqlParameter1);
            SuccessUpdate successUpdate = new SuccessUpdate();
            successUpdate.type = RESULTDB;
            successUpdate.oper = UPDATE;
            successUpdate.typeUpdate = NEWNAME;
            successUpdate.tagId = tagId;
            try
            {
                sqlCommand.ExecuteNonQuery();
                sqlCommand.CommandText = "Update FriendsTable set nameFrnd = @newnickuser where tagReceiverFrnd = @tagId";
                sqlCommand.ExecuteNonQuery();
                successUpdate.success = true;
                successUpdate.newName = newName;
            }
            catch
            {
                successUpdate.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successUpdate);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> UpdateName^{newName}_{tagId} -> {successUpdate.success}");
        }
        private static void UpdateVisibleOfUser(string tagUser, bool isVisible)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Update InfoUsers set isVisible = @isVisible where tagUser = @tagUser";
            SqlParameter sqlParameter = new SqlParameter("@isVisible", isVisible);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@tagUser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter1);
            SuccessUpdate successUpdate = new SuccessUpdate();
            successUpdate.type = RESULTDB;
            successUpdate.oper = UPDATE;
            successUpdate.typeUpdate = VISIBLE;
            successUpdate.tagId = tagUser;
            try
            {
                sqlCommand.ExecuteNonQuery();
                successUpdate.success = true;
                successUpdate.isVisible = isVisible;
            }
            catch
            {
                successUpdate.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successUpdate);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> VisibleEdit^{tagUser}_{isVisible} -> {successUpdate.success}");
        }
        private static void UpdateAvatarOfUser(string tagUser)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Update InfoUsers set isAvatar = @isAvatar where tagUser = @tagUser";
            SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter2 = new SqlParameter("@IsAvatar", true);
            sqlCommand.Parameters.Add(sqlParameter2);
            SuccessUpdate successUpdate = new SuccessUpdate();
            successUpdate.type = RESULTDB;
            successUpdate.oper = UPDATE;
            successUpdate.typeUpdate = SETAVATAR;
            successUpdate.tagId = tagUser;
            try
            {
                sqlCommand.ExecuteNonQuery();
                successUpdate.success = true;
            }
            catch(Exception ex)
            {
                successUpdate.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successUpdate);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> AvatarEdit^{tagUser} -> {successUpdate.success}");
        }
        private static void DeleteAvatarOfUser(string tagUser)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Update InfoUsers set isAvatar = @isAvatar where tagUser = @tagUser";
            SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter2 = new SqlParameter("@IsAvatar", false);
            sqlCommand.Parameters.Add(sqlParameter2);
            SuccessUpdate successUpdate = new SuccessUpdate();
            successUpdate.type = RESULTDB;
            successUpdate.oper = UPDATE;
            successUpdate.typeUpdate = DELETEAVATAR;
            successUpdate.tagId = tagUser;
            try
            {
                sqlCommand.ExecuteNonQuery();
                successUpdate.success = true;
            }
            catch (Exception ex)
            {
                successUpdate.success = false;
            }
            string jsonResult = JsonConvert.SerializeObject(successUpdate);
            webSocket.Send(jsonResult);
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> AvatarDelete^{tagUser} -> {successUpdate.success}");
        }
        private static void AddInGlobalChat(string userTag)
        {
            bool success = false;
            try
            {
                string dialog_id = GROUP + "0";
                DateTime enteredTime = DateTime.UtcNow;
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText = "Insert into UserDlgTable values(@dialog_id, @tagUser, @enteredTime)";
                SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter1 = new SqlParameter("@tagUser", userTag);
                sqlCommand.Parameters.Add(sqlParameter1);
                SqlParameter sqlParameter2 = new SqlParameter("@enteredTime", enteredTime);
                sqlCommand.Parameters.Add(sqlParameter2);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                success = true;
            }
            catch
            {
                success = false;
            }
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> CreateDLG^Insert into UserDlgTable ({success})");
        }
        private static void SelectDeviceForAuth(string userTag, string token)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText =
                @"Select i.nickUser, u.tagUser, i.isVisible, i.isAvatar from UsersData as u 
                    inner join InfoUsers as i
                    on i.tagUser = u.tagUser
                    where u.deviceToken = @deviceToken";
            SqlParameter sqlParameter1 = new SqlParameter("@deviceToken", token);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
            string nickDB = "", tagDB = "";
            bool isVisible = false;
            bool isAvatar = false;
            ResultDB result = new ResultDB();
            if (sqlDataReader.HasRows)
            {
                while (sqlDataReader.Read())
                {
                    nickDB = sqlDataReader.GetString(0);
                    tagDB = sqlDataReader.GetString(1);
                    isVisible = sqlDataReader.GetBoolean(2);
                    isAvatar = sqlDataReader.GetBoolean(3);
                }
                result.type = RESULTDB;
                result.oper = AUTHTOKEN;
                result.success = true;
                result.authorId = userTag;
                result.tag = tagDB;
                result.isVisible = isVisible;
                result.isAvatar = isAvatar;
                result.nickName = nickDB;
                string jsonResult = JsonConvert.SerializeObject(result);
                webSocket.Send(jsonResult);
            }
            else
            {
                result.type = RESULTDB;
                result.oper = AUTHTOKEN;
                result.success = false;
                result.authorId = userTag;
                string jsonResult = JsonConvert.SerializeObject(result);
                webSocket.Send(jsonResult);
            }

            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> DeviceAuth^{tagDB}");
        }
        private static void WorkWithFrnd(ActionsWithFrnd actionsWithFrnd)
        {

            ResultActionFrnd resultActionFrnd = new ResultActionFrnd();
            resultActionFrnd.type = RESULTDB;
            resultActionFrnd.oper = FRND;
            resultActionFrnd.tagUserSender = actionsWithFrnd.tagUserSender;
            resultActionFrnd.nameUserSender = actionsWithFrnd.nameUserSender;
            resultActionFrnd.tagUserReceiver = actionsWithFrnd.tagUserReceiver;
            resultActionFrnd.nameUserReceiver = actionsWithFrnd.nameUserReceiver;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                switch (actionsWithFrnd.typeAction)
                {
                    case ADD:
                        {
                            resultActionFrnd.typeAction = ADD;
                            sqlCommand.CommandText =
                                @"Insert into FriendsTable values
                                (@tagUserSender, @tagUserReceiver, @nameFrndReceiver, 1),
                                (@tagUserReceiver, @tagUserSender, @nameFrndSender,  0)";
                            SqlParameter sqlParameter = new SqlParameter("@tagUserSender", actionsWithFrnd.tagUserSender);
                            sqlCommand.Parameters.Add(sqlParameter);
                            SqlParameter sqlParameter1 = new SqlParameter("@tagUserReceiver", actionsWithFrnd.tagUserReceiver);
                            sqlCommand.Parameters.Add(sqlParameter1);
                            SqlParameter sqlParameter2 = new SqlParameter("@nameFrndReceiver", actionsWithFrnd.nameUserReceiver);
                            sqlCommand.Parameters.Add(sqlParameter2);
                            SqlParameter sqlParameter3 = new SqlParameter("@nameFrndSender", actionsWithFrnd.nameUserSender);
                            sqlCommand.Parameters.Add(sqlParameter3);
                            sqlCommand.ExecuteNonQuery();
                            sqlCommand.Parameters.Clear();
                            break;
                        }
                    case DELETE:
                        {

                            break;
                        }
                }
                resultActionFrnd.success = true;
            }
            catch(Exception ex)
            {
                resultActionFrnd.success = false;
                Console.WriteLine(ex.Message);
            }
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(resultActionFrnd);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> CreateFrndRequest^{resultActionFrnd.success}");

        }
        private static void UpdateFriend(UpdateFriend updateFriend)
        {
            ResultActionFrnd resultActionFrnd = new ResultActionFrnd();
            resultActionFrnd.type = RESULTDB;
            resultActionFrnd.oper = FRND;
            resultActionFrnd.tagUserSender = updateFriend.tagUserOur;
            resultActionFrnd.tagUserReceiver = updateFriend.tagUserFriend;
            resultActionFrnd.typeAction = CNFRMADD;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText =
                    @"Update FriendsTable set status = 2 where 
                    (tagSenderFrnd = @tagUserFriend AND tagReceiverFrnd = @tagUserOur)
                    OR
                    (tagSenderFrnd = @tagUserOur AND tagReceiverFrnd = @tagUserFriend)";
                SqlParameter sqlParameter = new SqlParameter("@tagUserFriend", updateFriend.tagUserFriend);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter2 = new SqlParameter("@tagUserOur", updateFriend.tagUserOur);
                sqlCommand.Parameters.Add(sqlParameter2);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                resultActionFrnd.success = true;
            }
            catch
            {
                resultActionFrnd.success = false;
            }
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(resultActionFrnd);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> ConfirmAddFriend^{updateFriend.tagUserOur} -> {updateFriend.tagUserFriend} ^ {resultActionFrnd.success}");
        }

        private static void DeleteFriend(DeleteFriend deleteFriend)
        {
            ResultActionFrnd resultActionFrnd = new ResultActionFrnd();
            resultActionFrnd.type = RESULTDB;
            resultActionFrnd.oper = FRND;
            resultActionFrnd.tagUserSender = deleteFriend.tagUserOur;
            resultActionFrnd.tagUserReceiver = deleteFriend.tagUserFriend;
            resultActionFrnd.typeAction = DELETE;
            resultActionFrnd.typeDelete = deleteFriend.typeDelete;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText =
                    @"Delete From FriendsTable where 
                    (tagSenderFrnd = @tagUserFriend AND tagReceiverFrnd = @tagUserOur)
                    OR
                    (tagSenderFrnd = @tagUserOur AND tagReceiverFrnd = @tagUserFriend)";
                SqlParameter sqlParameter = new SqlParameter("@tagUserFriend", deleteFriend.tagUserFriend);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter2 = new SqlParameter("@tagUserOur", deleteFriend.tagUserOur);
                sqlCommand.Parameters.Add(sqlParameter2);
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Clear();
                resultActionFrnd.success = true;
            }
            catch
            {
                resultActionFrnd.success = false;
            }
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(resultActionFrnd);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> DeleteFriend^{deleteFriend.tagUserOur} -> {deleteFriend.tagUserFriend} ^ {resultActionFrnd.success}");
        }

        private static void SelectDataForAllFriends(string tagUser, string token)
        {

            DataOfFriendsList dataOfFriends = new DataOfFriendsList();
            dataOfFriends.type = RESULTDB;
            dataOfFriends.oper = DOWNLOAD;
            dataOfFriends.table = ALLFRND;
            dataOfFriends.tagUser = tagUser;
            dataOfFriends.token = token;
            List<DataOfFriendsTable> listFriends = new List<DataOfFriendsTable>();
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText = 
                    @"select * from FriendsTable where (tagSenderFrnd = @tagUser) OR (tagReceiverFrnd = @tagUser)";
                SqlParameter sqlParameter = new SqlParameter("@tagUser", tagUser);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
                if (sqlDataReader.HasRows)
                {
                    while (sqlDataReader.Read())
                    {
                        string _tagSenderFrnd = sqlDataReader.GetString(0);
                        string _tagReceiverFrnd = sqlDataReader.GetString(1);
                        string _nameFrnd = sqlDataReader.GetString(2);
                        int _status = sqlDataReader.GetInt16(3);
                        listFriends.Add(new DataOfFriendsTable() { 
                            tagSenderFrnd = _tagSenderFrnd,
                            tagReceiverFrnd = _tagReceiverFrnd,
                            nameFrnd = _nameFrnd,
                            status = _status});
                    }
                }
                sqlCommand.Parameters.Clear();
                dataOfFriends.listOfData = listFriends;
                dataOfFriends.success = true;
            }
            catch
            {
                dataOfFriends.success = false;
            }
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(dataOfFriends);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> DownLoadFriends^{tagUser} ({dataOfFriends.success})");
        }
        private static void FindFriend(UpdateFriend updateFriend)
        {
            ResultActionFrnd resultActionFrnd = new ResultActionFrnd();
            resultActionFrnd.type = RESULTDB;
            resultActionFrnd.oper = FRND;
            resultActionFrnd.tagUserSender = updateFriend.tagUserOur;
            resultActionFrnd.typeAction = FIND;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText =
                    @"select tagUser, nickUser from InfoUsers where tagUser = @tagUser";
                SqlParameter sqlParameter = new SqlParameter("@tagUser", updateFriend.tagUserFriend);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
                if (sqlDataReader.HasRows)
                {
                    while (sqlDataReader.Read())
                    {
                        resultActionFrnd.tagUserReceiver = sqlDataReader.GetString(0);
                        resultActionFrnd.nameUserReceiver = sqlDataReader.GetString(1);
                    }
                    resultActionFrnd.success = true;
                }
                else
                {
                    resultActionFrnd.success = false;
                }
                sqlCommand.Parameters.Clear();
            }
            catch
            {
                resultActionFrnd.success = false;
            }
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(resultActionFrnd);
            webSocket.Send(jsonResult);
            Console.WriteLine($"[MSG] -> FindedFriend^{updateFriend.tagUserOur} -> {updateFriend.tagUserFriend} ^ {resultActionFrnd.success}");
        }
        private static void UpdateCountMessage(UpdateCountMsg updateCountMsg)
        {
            var countMessge = Convert.ToInt32(updateCountMsg.countMsg);
            var dialog = updateCountMsg.dialog;
            SuccessUpdate successUpdate = new SuccessUpdate();
            successUpdate.type = RESULTDB;
            successUpdate.oper = UPDATE;
            successUpdate.typeUpdate = COUNTMSG;
            successUpdate.tagId = updateCountMsg.tagUser;
            successUpdate.dialog = dialog;
            successUpdate.needTagUser = updateCountMsg.needTagUser;
            successUpdate.countMsg = countMessge;
            try
            {
                sqlConnection.Open();
                sqlCommand.Connection = sqlConnection;
                sqlCommand.CommandText =
                    @"update UserDlgTable set countMsg = countMsg + @countMessage where dialog_id = @dialog AND tagUser = @needTagUser";
                SqlParameter sqlParameter = new SqlParameter("@countMessage", countMessge);
                sqlCommand.Parameters.Add(sqlParameter);
                SqlParameter sqlParameter2 = new SqlParameter("@dialog", dialog);
                sqlCommand.Parameters.Add(sqlParameter2);
                SqlParameter sqlParameter3 = new SqlParameter("@needTagUser", updateCountMsg.needTagUser);
                sqlCommand.Parameters.Add(sqlParameter3);
                sqlCommand.ExecuteNonQuery();
                if (dialog.Substring(0, dialog.IndexOf("#") + 1) == GROUP)
                {
                    sqlCommand.CommandText =
                    @"update UserDlgTable set countMsg = countMsg + @countMessage where dialog_id = @dialog AND tagUser = @ourTag;";
                    SqlParameter sqlParameter4 = new SqlParameter("@ourTag", updateCountMsg.tagUser);
                    sqlCommand.Parameters.Add(sqlParameter4);
                    sqlCommand.ExecuteNonQuery();
                }
                sqlCommand.Parameters.Clear();
                successUpdate.success = true;
            }
            catch
            {
                sqlCommand.Parameters.Clear();
                successUpdate.success = false;
            }
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            string jsonResult = JsonConvert.SerializeObject(successUpdate);
            webSocket.Send(jsonResult);
        }
        private static void WebSocket_MessageReceived(object sender, MessageReceivedEventArgs e)
        {
            if (e.Message.IndexOf(FORDB) == -1) return;
            string message = e.Message.Substring(FORDB.Length);

            if (message.IndexOf(INFO) != -1)
            {
                message = message.Substring(INFO.Length);
                Console.WriteLine($"[MSG] -> {message}");
            }
            if (message.IndexOf(SQL) != -1)
            {
                message = message.Substring(SQL.Length);
                if (message.IndexOf(INSERT) != -1)
                {
                    message = message.Substring(INSERT.Length);
                    if(message.IndexOf(SIGNUP) != -1)
                    {
                        message = message.Substring(SIGNUP.Length);
                        SignUp signUp = JsonConvert.DeserializeObject<SignUp>(message);
                        InsertDataSignUp(signUp.loginUser, signUp.passUser, signUp.nickName, signUp.authorId);
                    }
                    if(message.IndexOf(NEWUSERDLG) != -1)
                    {
                        message = message.Substring(NEWUSERDLG.Length);
                        NewUserDLG newUserDLG = JsonConvert.DeserializeObject<NewUserDLG>(message);
                        InsertDataNewUserDLG(newUserDLG.userCompanion, newUserDLG.userManager);
                    }
                    if(message.IndexOf(NEWMSGDLG) != -1)
                    {
                        message = message.Substring(NEWMSGDLG.Length);
                        NewMsgDLG newMsgDLG = JsonConvert.DeserializeObject<NewMsgDLG>(message);
                        InsertDataNewMsgDLG(newMsgDLG.dialog_id, newMsgDLG.sender, newMsgDLG.typeMsg, newMsgDLG.text, newMsgDLG.receiverId);
                    }
                    if(message.IndexOf(FRND) != -1)
                    {
                        message = message.Substring(FRND.Length);
                        ActionsWithFrnd actionsWithFrnd = JsonConvert.DeserializeObject<ActionsWithFrnd>(message);
                        WorkWithFrnd(actionsWithFrnd);
                    }
                }
                if (message.IndexOf(SELECT) != -1)
                {
                    message = message.Substring(SELECT.Length);
                    if (message.IndexOf(AUTH) != -1)
                    {
                        message = message.Substring(AUTH.Length);
                        Auth auth = JsonConvert.DeserializeObject<Auth>(message);
                        SelectDataForAuth(auth.authorId, auth.loginUser, auth.passUser);
                    }
                    if (message.IndexOf(AUTHTOKEN) != -1)
                    {
                        message = message.Substring(AUTHTOKEN.Length);
                        AuthToken authToken = JsonConvert.DeserializeObject<AuthToken>(message);
                        SelectDeviceForAuth(authToken.tagUser, authToken.token);

                    }
                    if (message.IndexOf(DOWNLOAD) != -1)
                    {
                        message = message.Substring(DOWNLOAD.Length);
                        if (message.IndexOf(ALLDLG) != -1)
                        {
                            message = message.Substring(ALLDLG.Length);
                            DownLoadAllDlg downLoadAllDlg = JsonConvert.DeserializeObject<DownLoadAllDlg>(message);
                            SelectDataForAllDlg(downLoadAllDlg.tagUser, downLoadAllDlg.token);
                        }
                        if (message.IndexOf(ALLMSG) != -1)
                        {
                            message = message.Substring(ALLMSG.Length);
                            DownLoadAllMsg downLoadAllMsg = JsonConvert.DeserializeObject<DownLoadAllMsg>(message);
                            SelectDataForAllMsg(downLoadAllMsg.dialog_ids, downLoadAllMsg.authorId, downLoadAllMsg.token);
                        }
                        if (message.IndexOf(ALLTAGNAME) != -1)
                        {
                            message = message.Substring(ALLTAGNAME.Length);
                            DownLoadAllTagName downLoadAllTagName = JsonConvert.DeserializeObject<DownLoadAllTagName>(message);
                            SelectDataForAllTagName(downLoadAllTagName.dialog_ids, downLoadAllTagName.authorId, downLoadAllTagName.token);
                        }
                        if (message.IndexOf(ALLFRND) != -1)
                        {
                            message = message.Substring(ALLFRND.Length);
                            DownLoadAllDlg downLoadAllDlg = JsonConvert.DeserializeObject<DownLoadAllDlg>(message);
                            SelectDataForAllFriends(downLoadAllDlg.tagUser, downLoadAllDlg.token);
                        }
                    }
                    if (message.IndexOf(FRND) != -1)
                    {
                        message = message.Substring(FRND.Length);
                        UpdateFriend updateFriend = JsonConvert.DeserializeObject<UpdateFriend>(message);
                        FindFriend(updateFriend);
                    }
                }
                if (message.IndexOf(UPDATE) != -1)
                {
                    message = message.Substring(UPDATE.Length);
                    if (message.IndexOf(NEWNAME) != -1)
                    {
                        message = message.Substring(NEWNAME.Length);
                        NewName newName = JsonConvert.DeserializeObject<NewName>(message);
                        UpdateNameOfUser(newName.tagId, newName.newName);
                    }
                    if (message.IndexOf(VISIBLE) != -1)
                    {
                        message = message.Substring(VISIBLE.Length);
                        UpdateVisible updateVisible = JsonConvert.DeserializeObject<UpdateVisible>(message);
                        UpdateVisibleOfUser(updateVisible.tagUser, updateVisible.isVisible);
                    }
                    if (message.IndexOf(SETAVATAR) != -1)
                    {
                        message = message.Substring(SETAVATAR.Length);
                        UpdateAvatar updateAvatar = JsonConvert.DeserializeObject<UpdateAvatar>(message);
                        UpdateAvatarOfUser(updateAvatar.tagId);

                    }
                    if (message.IndexOf(DELETEAVATAR) != -1)
                    {
                        message = message.Substring(DELETEAVATAR.Length);
                        UpdateAvatar updateAvatar = JsonConvert.DeserializeObject<UpdateAvatar>(message);
                        DeleteAvatarOfUser(updateAvatar.tagId);

                    }
                    if (message.IndexOf(FRND) != -1)
                    {
                        message = message.Substring(FRND.Length);
                        UpdateFriend updateFriend = JsonConvert.DeserializeObject<UpdateFriend>(message);
                        UpdateFriend(updateFriend);
                    }
                    if (message.IndexOf(COUNTMSG) != -1)
                    {
                        message = message.Substring(COUNTMSG.Length);
                        UpdateCountMsg updateCountMsg = JsonConvert.DeserializeObject<UpdateCountMsg>(message);
                        UpdateCountMessage(updateCountMsg);
                    }
                }
                if (message.IndexOf(DELETE) != -1)
                {
                    message = message.Substring(DELETE.Length);
                    if (message.IndexOf(FRND) != -1)
                    {
                        message = message.Substring(FRND.Length);
                        DeleteFriend deleteFriend = JsonConvert.DeserializeObject<DeleteFriend>(message);
                        DeleteFriend(deleteFriend);
                    }
                }
            }

        }

        private static void WebSocket_Closed(object sender, EventArgs e)
        {
            Console.WriteLine("Connection closed");
        }

        private static void WebSocket_Error(object sender, SuperSocket.ClientEngine.ErrorEventArgs e)
        {
            Console.WriteLine($"Error: {e.Exception}");
        }

        private static void WebSocket_Opened(object sender, EventArgs e)
        {
            Console.WriteLine("Successful connection");

            SecureConnection secureConnection = new SecureConnection();
            secureConnection.type = "DBSERVER";
            secureConnection.key = "2FA";
            string jsonMsg = JsonConvert.SerializeObject(secureConnection);

            webSocket.Send(jsonMsg);

            sqlConnection = new SqlConnection("server=HOME-PC;database=ReChat;integrated security=true");
            sqlCommand = new SqlCommand();
        }
    }
}


