using System;
using WebSocket4Net;
using System.Data.SqlClient;
using Newtonsoft.Json;

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
    public class ResultDB
    {
        public string type { get; set; } // RESULTBD
        public string authorId { get; set; } // authorId
        public string nickName { get; set; } // name
        public string tag { get; set; } // uId
        public bool success { get; set; } // status
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
            sqlCommand.CommandText = "Insert into UsersData values(@loginuser, @passuser, @nickuser, @taguser)";
            SqlParameter sqlParameter = new SqlParameter("@loginuser", loginUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlParameter sqlParameter1 = new SqlParameter("@passuser", passUser);
            sqlCommand.Parameters.Add(sqlParameter1);
            SqlParameter sqlParameter2 = new SqlParameter("@nickuser", nickUser);
            sqlCommand.Parameters.Add(sqlParameter2);
            SqlParameter sqlParameter3 = new SqlParameter("@taguser", tagUser);
            sqlCommand.Parameters.Add(sqlParameter3);
            sqlCommand.ExecuteNonQuery();
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();
            Console.WriteLine($"[MSG] -> SIGNUP^Insert into UsersData");
        }

        private static void SelectDataForAuth(string authorUser, string loginUser, string passUser)
        {
            sqlConnection.Open();
            sqlCommand.Connection = sqlConnection;
            sqlCommand.CommandText = "Select * from UsersData where loginUser = @login";
            SqlParameter sqlParameter = new SqlParameter("@login", loginUser);
            sqlCommand.Parameters.Add(sqlParameter);
            SqlDataReader sqlDataReader = sqlCommand.ExecuteReader();
            string loginDB = "", passDB = "", nickDB = "", tagDB = "";
            ResultDB result = new ResultDB();
            if (sqlDataReader.HasRows)
            {
                while (sqlDataReader.Read())
                {
                    loginDB = sqlDataReader.GetString(0);
                    passDB = sqlDataReader.GetString(1);
                    nickDB = sqlDataReader.GetString(2);
                    tagDB = sqlDataReader.GetString(3);
                }
                if(passDB == passUser)
                {
                    result.type = RESULTDB;
                    result.success = true;
                    result.authorId = authorUser;
                    result.tag = tagDB;
                    result.nickName = nickDB;
                    string jsonResult = JsonConvert.SerializeObject(result);
                    webSocket.Send(jsonResult);
                }
                else
                {
                    result.type = RESULTDB;
                    result.success = false;
                    result.authorId = authorUser;
                    string jsonResult = JsonConvert.SerializeObject(result);
                    webSocket.Send(jsonResult);
                }
            }
            else
            {
                result.type = RESULTDB;
                result.success = false;
                result.authorId = authorUser;
                string jsonResult = JsonConvert.SerializeObject(result);
                webSocket.Send(jsonResult);
            }
            sqlCommand.Parameters.Clear();
            sqlConnection.Close();  
            Console.WriteLine($"[MSG] -> AUTH^{nickDB}_{tagDB}");

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


