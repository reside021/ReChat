using System;
using WebSocket4Net;
using System.Data.SqlClient;
using Newtonsoft.Json;
using System.Collections.Generic;

namespace WSClientDB
{
    class SecureConnection
    {
        public string type, key;                                 
    }
    public class MsgFromServer
    {
        public string usefulMsgForDB { get; set; }
        public string typeMsg { get; set; }
        public string textMsg { get; set; }
    }
    public class SignUp
    {
        public string authorId { get; set; }
        public string loginUser { get; set; }
        public string nickName { get; set; }
        public string passUser { get; set; }
    }
    public class Auth
    {
        public string authorId { get; set; }
        public string loginUser { get; set; }
        public string passUser { get; set; }
    }
    public class DownLoadAllDlg
    {
        public string tagUser { get; set; }
    }
    public class DownLoadAllMsg
    {
        public List<string> dialog_ids { get; set; }
        public string authorId { get; set; }
    }
    public class DownLoadAllTagName
    {
        public List<string> dialog_ids { get; set; }
        public string authorId { get; set; }
    }
    public class ResultDB // for auth of user
    {
        public string type { get; set; } // RESULTBD
        public string oper{ get; set; } // type oper
        public string authorId { get; set; } // authorId
        public string nickName { get; set; } // name
        public string tag { get; set; } // uId
        public bool isVisible { get; set; } // isVisible for all
        public bool isAvatar { get; set; } // have avatar?
        public bool success { get; set; } // status
    }
    public class NewName
    {
        public string tagId { get; set; }
        public string newName { get; set; }
    }
    public class UpdateVisible
    {
        public string tagUser { get; set; }
        public bool isVisible { get; set; }
    }
    public class UpdateAvatar
    {
        public string tagId { get; set; }
    }
    public class SuccessUpdate
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string typeUpdate { get; set; } // where updating
        public bool success { get; set; }
        public string tagId { get; set; }
        public string newName { get; set; }
        public bool isVisible { get; set; }
    }
    public class NewUserDLG
    {
        public string userCompanion { get; set; }
        public string userManager { get; set; }
    }
    public class SuccessInsertMsgDlg
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public bool success { get; set; }
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string textMsg { get; set; }
        public string timeCreated { get; set; }
        public string receiverId { get; set; }
        public string nameSender { get; set; }
    }
    public class NewMsgDLG
    {
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string text { get; set; }
        public string receiverId { get; set; }
    }
    public class SuccessCreateUserDlg
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public bool success { get; set; }
        public string dialog_id { get; set; }
        public string userManager { get; set; }
        public string userCompanion { get; set; }
        public string enteredTime { get; set; }
    }
    public class DataOfDialog
    {
        public string dialog_id { get; set; }
        public string tagUser { get; set; }
        public string enteredTime { get; set; }
    }
    public class ListDataOfDialog
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string table { get; set; }
        public bool success { get; set; }
        public List<DataOfDialog> listOfData { get; set; }
        public string tagUser {get; set;}
    }
    public class DataOfMessage
    {
        public string dialog_id { get; set; }
        public string sender { get; set; }
        public string typeMsg { get; set; }
        public string textMsg { get; set; }
        public string timeCreated { get; set; }
    }
    public class ListDataOfMessage
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string table { get; set; }
        public bool success { get; set; }
        public List<DataOfMessage> listOfData { get; set; }
        public string tagUser { get; set; }
    }
    public class DataOfNickName
    {
        public string tagUser { get; set; }
        public string nickUser { get; set; }
    }

    public class DataOfTagName
    {
        public string type { get; set; }
        public string oper { get; set; } // type oper
        public string table { get; set; }
        public bool success { get; set; }
        public List<DataOfNickName> listOfData { get; set; }
        public string tagUser { get; set; }
    }
    class Program
    {
        const string FORDB = "FORDB::";
        const string INFO = "INFO::";
        const string SQL = "SQL::";
        const string INSERT = "INSERT::";
        const string SIGNUP = "SIGNUP::";
        const string SELECT = "SELECT::";
        const string AUTH = "AUTH::";
        const string RESULTDB = "RESULTDB::";
        const string UPDATE = "UPDATE::";
        const string NEWNAME = "NEWNAME::";
        const string VISIBLE = "VISIBLE::";
        const string SETAVATAR = "SETAVATAR::";
        const string NEWUSERDLG = "NEWUSERDLG::";
        const string CHAT = "CHAT#";
        const string NEWMSGDLG = "NEWMSGDLG::";
        const string DOWNLOAD = "DOWNLOAD::";
        const string ALLDLG = "ALLDLG::";
        const string ALLMSG = "ALLMSG::";
        const string ALLTAGNAME = "ALLTAGNAME::";
        const string GROUP = "GROUP#";

        static WebSocket webSocket;
        static SqlConnection sqlConnection;
        static SqlCommand sqlCommand;

        static void Main(string[] args)
        {

            webSocket = new WebSocket("ws://chatserv.sytes.net:9001/");
            webSocket.Opened += WebSocket_Opened;
            webSocket.Error += WebSocket_Error;
            webSocket.Closed += WebSocket_Closed;
            webSocket.MessageReceived += WebSocket_MessageReceived;
            webSocket.Open();
            Console.ReadKey();
        }

        private static void InsertDataSignUp(string loginUser, string passUser, string nickUser, string tagUser)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Insert into UsersData values(@loginuser, @passuser, @taguser)";
            SqlParameter sqlParameter = new SqlParameter("@loginuser", loginUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@passuser", passUser);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlParameter sqlParameter2 = new SqlParameter("@taguser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter2);
            sqlCommand.ExecuteNonQuery();
            sqlCommand.Parameters.Clear();
            sqlCommand.CommandText = "Insert into InfoUsers values(@taguser, @nickuser, @isVisible, @isAvatar)";
            SqlParameter sqlParameter3 = new SqlParameter("@taguser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter3);
            SqlParameter sqlParameter4 = new SqlParameter("@nickuser", nickUser);
            sqlCommand.Parameters.Add(sqlParameter4);
            SqlParameter sqlParameter5 = new SqlParameter("@isVisible", false);
            sqlCommand.Parameters.Add(sqlParameter5);
            SqlParameter sqlParameter6 = new SqlParameter("@isAvatar", false);
            sqlCommand.Parameters.Add(sqlParameter6);
            sqlCommand.ExecuteNonQuery();
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> SIGNUP^Insert into UsersData and InfoUsers");
        }
        private static void InsertDataNewMsgDLG(string dialog_id, string sender, string typeMsg, string text, string receiverId)
        {
            var nameSender = SlctAllTagNameHelper(sender);
            DateTime timeCreated = DateTime.UtcNow;
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
            sqlCommand.CommandText = "Insert into UserDlgTable values(@dialog_id, @tagUser, @enteredTime)";
            SqlParameter sqlParameter = new SqlParameter("@dialog_id", dialog_id);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@tagUser", userCompanion);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlParameter sqlParameter2 = new SqlParameter("@enteredTime", enteredTime);
            sqlCommand.Parameters.Add(sqlParameter2);
            SuccessCreateUserDlg successCreateUserDlg = new SuccessCreateUserDlg();
            try
            {
                sqlCommand.ExecuteNonQuery();
                sqlCommand.Parameters.Remove(sqlParameter1);
                sqlParameter1 = new SqlParameter("@tagUser", userManager);
                sqlCommand.Parameters.Add(sqlParameter1);
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
        private static void SelectDataForAllDlg(string tagUser)
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
                            dataOfDialogs.Add(new DataOfDialog() { dialog_id = _dialogId, tagUser = _tagUser, enteredTime = _enteredTime });
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
                            dataOfDialogs.Add(new DataOfDialog() { dialog_id = _dialogId, tagUser = _tagUser, enteredTime = _enteredTime });
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
            string jsonResult = JsonConvert.SerializeObject(listDataOfDialog);
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
                    string _timeCreated = sqlDataReader.GetDateTime(5).ToString();
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
        private static void SelectDataForAllMsg(List<string> dialog_ids, string authorId)
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
            Console.WriteLine(dialog_id);
            Console.WriteLine(notNeededTag);
            if (objectName != null) name = objectName.ToString();
            return name;
        }
        private static void SelectDataForAllTagName(List<string> dialog_ids, string authorId)
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
        private static void AddInGlobalChat(string userTag)
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
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> CreateDLG^Insert into UserDlgTable");
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
                    if (message.IndexOf(SIGNUP) != -1)
                    {
                        message = message.Substring(SIGNUP.Length);
                        SignUp signUp = JsonConvert.DeserializeObject<SignUp>(message);
                        InsertDataSignUp(signUp.loginUser, signUp.passUser, signUp.nickName, signUp.authorId);
                        AddInGlobalChat(signUp.authorId);
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
                    if(message.IndexOf(DOWNLOAD) != -1)
                    {
                        message = message.Substring(DOWNLOAD.Length);
                        if (message.IndexOf(ALLDLG) != -1)
                        {
                            message = message.Substring(ALLDLG.Length);
                            DownLoadAllDlg downLoadAllDlg = JsonConvert.DeserializeObject<DownLoadAllDlg>(message);
                            SelectDataForAllDlg(downLoadAllDlg.tagUser);
                        }
                        if(message.IndexOf(ALLMSG) != -1)
                        {
                            message = message.Substring(ALLMSG.Length);
                            DownLoadAllMsg downLoadAllMsg = JsonConvert.DeserializeObject<DownLoadAllMsg>(message);
                            SelectDataForAllMsg(downLoadAllMsg.dialog_ids, downLoadAllMsg.authorId);
                        }
                        if(message.IndexOf(ALLTAGNAME) != -1)
                        {
                            message = message.Substring(ALLTAGNAME.Length);
                            DownLoadAllTagName downLoadAllTagName = JsonConvert.DeserializeObject<DownLoadAllTagName>(message);
                            SelectDataForAllTagName(downLoadAllTagName.dialog_ids, downLoadAllTagName.authorId);
                        }
                    }
                }
                if (message.IndexOf(UPDATE) != -1)
                {
                    message = message.Substring(UPDATE.Length);
                    if(message.IndexOf(NEWNAME) != -1)
                    {
                        message = message.Substring(NEWNAME.Length);
                        NewName newName = JsonConvert.DeserializeObject<NewName>(message);
                        UpdateNameOfUser(newName.tagId, newName.newName);
                    }
                    if(message.IndexOf(VISIBLE) != -1)
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


