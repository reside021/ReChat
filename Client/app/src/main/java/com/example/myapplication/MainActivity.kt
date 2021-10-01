package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory
import kotlin.coroutines.coroutineContext

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
//                parseMessage(message)
            }

            override fun onError(ex: Exception?) {
                Log.i("__CHAT__", "onException: $ex");

            }
        }
    }



    fun onSignUpClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialogsignup, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
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

            val msgToSrv : String = "signup::$loginsign::$pass1sign::$usernamesign"
            webSocketClient.send(msgToSrv)
//            Toast.makeText(this@MainActivity,
//                    msgToSrv,
//                    Toast.LENGTH_SHORT).show()
        }


        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
    }
    fun onLoginClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialoglogin, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            alertDialog.dismiss()
        }
        view.findViewById<Button>(R.id.loginBtn).setOnClickListener {

        }
        if(webSocketClient.connection.readyState.ordinal == 0){
            Toast.makeText(this@MainActivity, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
    }

}