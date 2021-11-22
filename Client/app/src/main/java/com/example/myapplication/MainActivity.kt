package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapplication.ChatPeople.Companion.mainWindowOuter
import com.example.myapplication.ChatPeople.Companion.scrollView
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    companion object {
        const val WEB_SOCKET_URL = "ws://chatserv.sytes.net:9001"
        lateinit var webSocketClient: WebSocketClient
        lateinit var sqliteHelper: SqliteHelper
    }
    private lateinit var sp : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firstactivity)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sqliteHelper = SqliteHelper(this)
        sqliteHelper.clearTable()
        val ed = sp.edit()
        ed.putBoolean("isAuth", false)
        ed.apply()
    }

    override fun onStart() {
        super.onStart()
        initWebSocket()
    }





    private fun initWebSocket(){
//        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

        val chatservUri: URI? = URI(WEB_SOCKET_URL)
        createWebSocketClient(chatservUri)
//        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()



    }

    private fun createWebSocketClient(chatservURI: URI?){
        webSocketClient = object : WebSocketClient(chatservURI){
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("__CHAT__", "onOpen");
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i("__CHAT__", "onClose");
            }

            override fun onMessage(message: String?) {
                Log.i("__CHAT__", "onMessage: $message");
                this@MainActivity.runOnUiThread{
                    parseMessage(message)
                }

            }

            override fun onError(ex: Exception?) {
                Log.i("__CHAT__", "onException: $ex");
            }
        }
    }

    fun parseMessage(message: String?){
        when (message?.substringBefore("::")) {
            "DBNOTACTIVE" -> {
                val msg = message.substringAfter("::")
                    Toast.makeText(this@MainActivity,
                            msg,
                            Toast.LENGTH_SHORT).show()
            }
            "RESULTDB" -> {
                val typeOperWithMsg = message.substringAfter("::")
                val typeOper = typeOperWithMsg.substringBefore("::")
                val statusWithMsg = typeOperWithMsg.substringAfter("::")
                val status = statusWithMsg.substringBefore("::")
                val msg = statusWithMsg.substringAfter("::")
                if(typeOper == "AUTH"){
                    if(status == "SUCCESS"){
                        Toast.makeText(this@MainActivity,
                                "Успешная авторизация",
                                Toast.LENGTH_SHORT).show()
                        authorization(msg)

                    }
                    if(status == "ERROR"){
                        Toast.makeText(this@MainActivity,
                                "Ошибка авторизации\nНеверные данные для входа",
                                Toast.LENGTH_SHORT).show()
                    }
                }
                if(typeOper == "UPDATE"){
                    if(status == "SUCCESS"){
                        if(msg.substringBefore("::") == "NEWNAME"){
                            val newName = msg.substringAfter("::")
                            val ed = sp.edit()
                            ed.putString("nickname", newName)
                            ed.apply()
                            val confirmSetname = ConfirmSetName("SETNAME::", true, newName)
                            val msg = Json.encodeToString(confirmSetname)
                            if(webSocketClient.connection.readyState.ordinal != 0){
                                webSocketClient.send(msg)
                                Toast.makeText(this@MainActivity,
                                        "Имя успешно изменено",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
                        if(msg.substringBefore("::") == "VISIBLE"){
                            val isVisible = msg.substringAfter("::").toBoolean()
                            val ed = sp.edit()
                            ed.putBoolean("isVisible", isVisible)
                            ed.apply()
                            val confirmVisible = ConfirmUpVisible("VISIBLE::", true, isVisible)
                            val msg = Json.encodeToString(confirmVisible)
                            if(webSocketClient.connection.readyState.ordinal != 0) {
                                webSocketClient.send(msg)
                            }
                            if(isVisible){
                                Toast.makeText(this@MainActivity, "Виден всем",
                                        Toast.LENGTH_SHORT).show()
                            } else{
                                Toast.makeText(this@MainActivity, "Включен режим призрака",
                                        Toast.LENGTH_SHORT).show()
                            }
                        }
                        if(msg.substringBefore("::") == "SETAVATAR"){
                            val ed = sp.edit()
                            ed.putBoolean("isAvatar", true)
                            ed.apply()
                            Toast.makeText(this@MainActivity, "Изображение успешно установлено",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                    if(status == "ERROR"){
                        if(msg == "NEWNAME::"){
                            Toast.makeText(this@MainActivity,
                                    "Ошибка изменения имени. Попробуйте позже.",
                                    Toast.LENGTH_SHORT).show()
                        }
                        if(msg == "VISIBLE::"){
                            Toast.makeText(this@MainActivity, "Ошибка изменения статуса",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if(typeOper == "INSERT"){
                    if(status == "SUCCESS"){
                        if(msg.substringBefore("::") == "NEWUSERDLG"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ConfirmInsertNewUserDlg>(jsonData)
                            if(msg.Icreater){
                                sqliteHelper.addUserInDLG(
                                        msg.dialog_id,
                                        msg.userCompanion,
                                        msg.enteredTime
                                )
                                Toast.makeText(this, "С пользователем создан чат",
                                        Toast.LENGTH_SHORT).show()
                            } else{
                                sqliteHelper.addUserInDLG(
                                        msg.dialog_id,
                                        msg.userManager,
                                        msg.enteredTime
                                )
                                val dialog_ids = sqliteHelper.getAllDlgFromDLG()
                                val queryAllTagName = QueryAllTagName("DOWNLOAD::", "ALLTAGNAME::", dialog_ids)
                                val dataServerName = Json.encodeToString(queryAllTagName)
                                webSocketClient.send(dataServerName)
                            }
                        }
                        if(msg.substringBefore("::") == "NEWMSGDLG"){
                            val jsonData = msg.substringAfter("::")
                            messageToUser(jsonData)
                        }
                    }
                    if(status == "ERROR"){
                        if(msg.substringBefore("::") == "NEWUSERDLG"){
                            Toast.makeText(this, "Не удалось создать чат",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if(typeOper == "DOWNLOAD"){
                    if(status == "SUCCESS"){
                        if(msg.substringBefore("::") == "ALLDLG"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfDlg>(jsonData)
                            val dataOfDialog : List<DataOfDialog> = msg.listOfData
                            for(el in dataOfDialog){
                                sqliteHelper.addUserInDLG(
                                        el.dialog_id,
                                        el.tagUser,
                                        el.enteredTime
                                )
                            }
                            val dialog_ids = sqliteHelper.getAllDlgFromDLG()
                            val queryAllTagName = QueryAllTagName("DOWNLOAD::", "ALLTAGNAME::", dialog_ids)
                            val dataServerName = Json.encodeToString(queryAllTagName)
                            webSocketClient.send(dataServerName)
                            val queryAllMsg = QueryAllMsg("DOWNLOAD::", "ALLMSG::", dialog_ids )
                            val dataServerMsg = Json.encodeToString(queryAllMsg)
                            webSocketClient.send(dataServerMsg)
                        }
                        if(msg.substringBefore("::") == "ALLMSG"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfMsg>(jsonData)
                            val dataOfMessage : List<DataOfMessage> = msg.listOfData
                            for(el in dataOfMessage){
                                sqliteHelper.addMsgInTable(
                                        el.dialog_id,
                                        el.sender,
                                        el.typeMsg,
                                        el.textMsg,
                                        el.timeCreated
                                )
                            }
                        }
                        if(msg.substringBefore("::") == "ALLTAGNAME"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListTagName>(jsonData)
                            val dataOfNickname : List<DataOfNickName> = msg.listOfData
                            for(el in dataOfNickname){
                                if(sqliteHelper.checkUserInChat(el.tagUser)) break
                                sqliteHelper.addUserInChat(el.tagUser to el.nickUser)
                            }
                        }
                    }
                    if(status == "ERROR"){
                    }
                }
            }
            "ONLINE" -> {
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                val name = idWithName.substringAfter("::")
                sqliteHelper.addUserInOnline(id, name)

            }
            "OFFLINE" ->{
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                sqliteHelper.deleteUserFromOnline(id)
            }
            "MESSAGE_FROM" -> {
                if(sp.getBoolean("isAuth", false)) {
                    val jsonData = message.substringAfter("::")
                    val msg = Json.decodeFromString<MessageFromUser>(jsonData)
//                    Toast.makeText(this, jsonData,
//                            Toast.LENGTH_SHORT).show()
                    messagePrint(
                            msg.dialog_id,
                            msg.sender,
                            msg.typeMsg,
                            msg.textMsg,
                            msg.timeCreated,
                            msg.receiverId,
                            msg.nameSender
                    )
                }
            }
        }
    }

    private fun messageToUser(jsonData : String){
        val msg = Json.decodeFromString<ConfirmInsertNewMsgDlg>(jsonData)
        if(!sp.getBoolean("active",false)) return
        if(sp.getString("idActive","NONE") != msg.receiverId) return
        try{
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_to, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
            textInMessage.text = msg.textMsg
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mainWindowOuter.addView(newView, lp)
            sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)
//            if(sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)){
//                Toast.makeText(this@MainActivity,
//                        "сообщение добавлено",
//                        Toast.LENGTH_SHORT).show()
//            } else{
//                Toast.makeText(this@MainActivity,
//                        "сообщение не добавлено",
//                        Toast.LENGTH_SHORT).show()
//            }

            scrollView.post(Runnable(){
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
        } catch (ex : Exception){
        }
    }
    private fun messagePrint(dialog_id: String, sender: String, typeMsg: String, textMsg: String,
                             timeCreated: String, receiverId: String, nameSender: String){


        if(!sp.getBoolean("active",false)) return
        if(dialog_id.substringBefore("#") == "GROUP"){
            if(receiverId != sp.getString("idActive","NONE")){
                return
            }
            if(sender == sp.getString("tagUser", "NONE")) return
        }else{
            if(sp.getString("idActive","NONE") != sender) return
        }

        sqliteHelper.addMsgInTable(dialog_id, sender, typeMsg, textMsg, timeCreated)
            val nickname = sp.getString("nickname", resources.getString(R.string.user_name))
            val tagUser = sp.getString("tagUser", null)
        try{
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_from, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
            textInMessage.text = textMsg
            val senderView = newView.findViewById<TextView>(R.id.senderName)
            senderView.text = nameSender
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mainWindowOuter.addView(newView, lp)
            scrollView.post(Runnable(){
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
        } catch (ex : Exception){
        }
    }

    private fun authorization(data : String){
        try {
            val obj = Json.decodeFromString<DataOfUser>(data)
            val intent = Intent(this, MasterActivity::class.java);
            val ed = sp.edit()
            ed.putString("nickname", obj.nickname)
            ed.putString("tagUser", obj.tagUser)
            ed.putBoolean("isVisible", obj.isVisible)
            ed.putBoolean("isAvatar", obj.isAvatar)
            ed.apply()
            val confirmAuth = ConfirmAuth("AUTH::", true, obj.nickname, obj.tagUser, obj.isVisible)
            var msg = Json.encodeToString(confirmAuth)
            if(webSocketClient.connection.readyState.ordinal != 0){
                webSocketClient.send(msg)
                val queryAllDlg = QueryAllDlg("DOWNLOAD::","ALLDLG::", obj.tagUser)
                msg = Json.encodeToString(queryAllDlg)
                webSocketClient.send(msg)
                startActivity(intent)
            }
        } catch (ex : Exception){
            Toast.makeText(this@MainActivity,
                    "Произошла непредвиденная ошибка",
                    Toast.LENGTH_LONG).show()
        }

    }

    fun onSignUpClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialogsignup, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<Button>(R.id.cancelBtnSignup).setOnClickListener {
            alertDialog.dismiss()
        }
        view.findViewById<Button>(R.id.signupBtn).setOnClickListener {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val loginsign : String = view.findViewById<EditText>(R.id.loginSign).text.toString()
            val usernamesign : String = view.findViewById<EditText>(R.id.usernameSign).text.toString()
            val pass1sign : String = view.findViewById<EditText>(R.id.pass1Sign).text.toString()
            val pass2sign : String = view.findViewById<EditText>(R.id.pass2Sign).text.toString()
            if(loginsign.trim().isEmpty() ||
                usernamesign.trim().isEmpty() ||
                pass1sign.trim().isEmpty() ||
                pass2sign.trim().isEmpty()){
                Toast.makeText(this@MainActivity,
                    "Все поля должны быть заполнены!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(pass1sign != pass2sign){
                Toast.makeText(this@MainActivity,
                    "Пароли не совпадают!",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(!view.findViewById<CheckBox>(R.id.checkBoxPrivacy).isChecked){
                Toast.makeText(this@MainActivity,
                    "Вы не дали согласия на обработку данных",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dataSignUpUser = SignUpUser("SIGNUP::", loginsign, pass1sign, usernamesign)
            val msg = Json.encodeToString(dataSignUpUser)
            if(webSocketClient.connection.readyState.ordinal == 0){
                Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
                webSocketClient.send(msg)
                alertDialog.dismiss()
        }
        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
    }

    @Serializable
    data class ListTagName(
            val listOfData : List<DataOfNickName>
    )
    @Serializable
    data class DataOfNickName(
            val tagUser : String,
            val nickUser : String
    )
    @Serializable
    data class ListDataOfMsg(
            val listOfData : List<DataOfMessage>
    )
    @Serializable
    data class DataOfMessage(
            val dialog_id : String,
            val sender : String,
            val typeMsg : String,
            val textMsg : String,
            val timeCreated : String
    )
    @Serializable
    data class ListDataOfDlg(
            val listOfData : List<DataOfDialog>
    )
    @Serializable
    data class DataOfDialog(
            val dialog_id : String,
            val tagUser : String,
            val enteredTime: String
    )
    @Serializable
    data class QueryAllMsg(
            val type : String,
            val table: String,
            val dialog_ids: List<String>
    )
    @Serializable
    data class QueryAllTagName(
            val type: String,
            val table: String,
            val dialog_ids: List<String>
    )
    @Serializable
    data class QueryAllDlg(
            val type : String,
            val table : String,
            val tagUser : String
    )
    @Serializable
    data class ConfirmInsertNewMsgDlg(
            val dialog_id : String,
            val sender : String,
            val typeMsg : String,
            val textMsg : String,
            val timeCreated : String,
            val receiverId : String,
            val nameSender : String
    )
    @Serializable
    data class ConfirmInsertNewUserDlg(
            val Icreater : Boolean,
            val dialog_id : String,
            val userManager : String,
            val enteredTime : String,
            val userCompanion : String
    )
    @Serializable
    data class MessageFromUser(
            val dialog_id: String,
            val sender: String,
            val typeMsg: String,
            val textMsg: String,
            val timeCreated: String,
            val receiverId : String,
            val nameSender : String
    )
    @Serializable
    data class ConfirmUpVisible(
            val type : String,
            val confirmUpVisible : Boolean,
            val isVisible: Boolean
    )
    @Serializable
    data class ConfirmSetName(
            val type : String,
            val confirmSetname: Boolean,
            val newUserName : String
    )
    @Serializable
    data class ConfirmAuth(
            val type : String,
            val confirmAuth : Boolean,
            val nickname : String,
            val tagUser : String,
            val isVisible: Boolean
    )
    @Serializable
    data class DataOfUser(
            val nickname : String,
            val tagUser : String,
            val isVisible : Boolean,
            val isAvatar : Boolean
    )
    @Serializable
    data class SignUpUser(
            val type : String,
            val loginSignUp : String,
            val passSignUp : String,
            val userNameSignUp : String
    )
    @Serializable
    data class LoginDataUser(
            val type : String,
            val confirmAuth : Boolean,
            val loginAuth : String,
            val passAuth : String
    )

    fun onLoginUserClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialoglogin, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()
        view.findViewById<Button>(R.id.cancelBtnAuth).setOnClickListener {
            alertDialog.dismiss()
        }
        view.findViewById<Button>(R.id.loginBtnAuth).setOnClickListener {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val loginAuth = view.findViewById<EditText>(R.id.loginAuth).text.toString()
            val passAuth = view.findViewById<EditText>(R.id.passAuth).text.toString()

            if(loginAuth.trim().isEmpty() || passAuth.trim().isEmpty()){
                return@setOnClickListener
            }
            val dataUser = LoginDataUser("AUTH::", false, loginAuth, passAuth)
            val msg = Json.encodeToString(dataUser)
            if(webSocketClient.connection.readyState.ordinal == 0){
                Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            webSocketClient.send(msg)
            alertDialog.dismiss()
        }
        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT).show()
        }
    }

}