package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import java.text.SimpleDateFormat
import java.util.*


class ChatPeople : AppCompatActivity() {
    companion object{
        private val ME_MSG = 1
        private val OTHER_MSG = 0
        lateinit var mainWindowOuter: LinearLayout
    }
    private lateinit var animAlpha: Animation
    private lateinit var editTextMessage : EditText
    private lateinit var mainWindowInclude : LinearLayout
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var idUser : String
    private lateinit var sqliteHelper: SqliteHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_window)
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha)
        editTextMessage = findViewById(R.id.editTextMessage)
        mainWindowInclude = findViewById(R.id.mainChatWindow)
        webSocketClient = MainActivity.webSocketClient
        sqliteHelper = MainActivity.sqliteHelper
        idUser = intent.extras?.getString("idTag").toString()

    }

    override fun onStart() {
        super.onStart()
        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", true)
        ed.putString("idActive", idUser)
        ed.apply()
        mainWindowOuter = mainWindowInclude
    }

    override fun onStop() {
        super.onStop()

        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", false)
        ed.apply()
    }

//    fun onReceiveMsg(dialogID : String, textMSG : String){
//        if(dialogID != idUser) return
//
//        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
//        if(!sp.getBoolean("active",false)) return
//
//        try{
//            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            val newView = inflater.inflate(R.layout.message_from, null)
//            val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
//            textInMessage.text = textMSG
//            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            mainWindowInclude.addView(newView, lp)
//        } catch (ex : Exception){
//
//        }
//    }
    @Serializable
    data class Msg(
        val type : String,
        val id : String,
        val text : String
    )

    fun onSendMsgClick(view: View) {
        view.startAnimation(animAlpha)
        try{
            if(webSocketClient.connection.readyState.ordinal == 0){
                Toast.makeText(this, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                return
            }
            val textMSG = editTextMessage.text
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_to, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
            textInMessage.text = textMSG
            val dataUser = Msg("MESSAGE_TO::", idUser, textMSG.toString())
            val msg = Json.encodeToString(dataUser)
            webSocketClient.send(msg)
            val c: Calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val strDate: String = sdf.format(c.time)
//            sqliteHelper.addMsgInTable(idUser, ME_MSG, textMSG.toString(), strDate)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mainWindowInclude.addView(newView, lp)
        } catch (ex : Exception){

        }
        editTextMessage.text.clear()
    }
}