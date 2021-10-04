package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.firstactivity)
        initWebSocket()
    }


    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }


    private fun initWebSocket(){
        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

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
                parseMessage(message)

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
//            "RESULTDB" ->{
//                val statusWithMsg = message.substringAfter("::")
//                val status = statusWithMsg.substringBefore("::")
//                val msg = statusWithMsg.substringAfter("::")
//                if(status == "SUCCESS"){
//                    Toast.makeText(this@MainActivity,
//                        msg,
//                        Toast.LENGTH_SHORT).show()
//                }
//                if(status == "ERROR"){
//                    Toast.makeText(this@MainActivity,
//                        msg,
//                        Toast.LENGTH_SHORT).show()
//                }
//
//            }
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
            val msg =   Json.encodeToString(dataSignUpUser)
            webSocketClient.send(msg)
            alertDialog.dismiss()
        }
        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
//        val intent = Intent(this, MasterActivity::class.java);
//        startActivity(intent)
    }

    @Serializable
    data class SignUpUser(
        val type : String,
        val loginSignUp : String,
        val passSignUp : String,
        val userNameSignUp : String
    )
    @Serializable
    data class LoginDataUser(val type : String, val loginAuth : String, val passAuth : String)

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
//            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            val loginAuth = view.findViewById<EditText>(R.id.loginAuth).text.toString()
            val passAuth = view.findViewById<EditText>(R.id.passAuth).text.toString()

            if(loginAuth.trim().isEmpty() || passAuth.trim().isEmpty()){
                return@setOnClickListener
            }
            val dataUser = LoginDataUser("AUTH::", loginAuth, passAuth)
            val msg =   Json.encodeToString(dataUser)
            webSocketClient.send(msg)
            alertDialog.dismiss()
        }
        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT).show()
        }
    }

}