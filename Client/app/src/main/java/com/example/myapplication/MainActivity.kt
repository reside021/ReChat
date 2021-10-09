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
        val ed = sp.edit()
        ed.putBoolean("isAuth", false)
        ed.apply()
    }

    override fun onStart() {
        super.onStart()
        sqliteHelper.clearOnlineTable()
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
                    messagePrint(msg.user, msg.authorId, msg.senderName, msg.text)
                }
            }
        }
    }

    private fun messagePrint(user : String, authorId : String, senderName: String, textMSG : String){
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        if(!sp.getBoolean("active",false)) return
        if(sp.getString("idActive","NONE") != authorId) return

            val nickname = sp.getString("nickname", resources.getString(R.string.user_name))
            val tagUser = sp.getString("tagUser", null)
        try{
            if(user == nickname + "_" + tagUser) return
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_from, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
            textInMessage.text = textMSG
            val sender = newView.findViewById<TextView>(R.id.senderName)
            sender.text = senderName
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            ChatPeople.mainWindowOuter.addView(newView, lp)
            val scrollView = ChatPeople.scrollView
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
            ed.apply()
            val confirmAuth = ConfirmAuth("AUTH::", true, obj.nickname, obj.tagUser, obj.isVisible)
            val msg = Json.encodeToString(confirmAuth)
            if(webSocketClient.connection.readyState.ordinal != 0){
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
    data class MessageFromUser(
            val user : String,
            val authorId : String,
            val senderName : String,
            val text : String
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
            val isVisible : Boolean
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