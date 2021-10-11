package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
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
        lateinit var scrollView: ScrollView
    }
    private lateinit var animAlpha: Animation
    private lateinit var editTextMessage : EditText
    private lateinit var mainWindowInclude : LinearLayout
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var idUser : String
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var dialog_id: String
    private lateinit var sp : SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_window)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha)
        editTextMessage = findViewById(R.id.editTextMessage)
        mainWindowInclude = findViewById(R.id.mainChatWindow)
        webSocketClient = MainActivity.webSocketClient
        sqliteHelper = MainActivity.sqliteHelper
        idUser = intent.extras?.getString("idTag").toString()
        scrollView = findViewById(R.id.scrollChat)
        mainWindowOuter = mainWindowInclude
        dialog_id = sqliteHelper.getDialogIdWithUser(idUser)
        val nameOfUser = sqliteHelper.getNameInUserChat(idUser)
        recoveryAllMsg(dialog_id, nameOfUser)
    }


    private fun recoveryAllMsg(dialog_id: String, nameOfUser : String){
        val ourTag = sp.getString("tagUser", null)
        val dataOfMsg = sqliteHelper.getMsgWithUser(dialog_id)
        for(el in dataOfMsg){
            if(el[0] != ourTag){
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val newView = inflater.inflate(R.layout.message_from, null)
                val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
                textInMessage.text = el[1]
                val sender = newView.findViewById<TextView>(R.id.senderName)
                sender.text = nameOfUser
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                mainWindowInclude.addView(newView, lp)
            } else{
                val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val newView = inflater.inflate(R.layout.message_to, null)
                val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
                textInMessage.text = el[1]
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                mainWindowInclude.addView(newView, lp)
            }
            scrollView.post(Runnable(){
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
        }
    }
    override fun onStart() {
        super.onStart()
        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", true)
        ed.putString("idActive", idUser)
        ed.apply()
    }


    override fun onStop() {
        super.onStop()

        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", false)
        ed.apply()
    }
    @Serializable
    data class Msg(
        val type : String,
        val dialog_id : String,
        val typeMsg : String,
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
            val textMSG = editTextMessage.text.toString()
            if(textMSG.isEmpty()) return
//            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//            val newView = inflater.inflate(R.layout.message_to, null)
//            val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
//            textInMessage.text = textMSG
            val dataUser = Msg("MESSAGE_TO::",dialog_id,"TEXT", idUser, textMSG)
            val msg = Json.encodeToString(dataUser)
            webSocketClient.send(msg)
//            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
//            mainWindowInclude.addView(newView, lp)
//            scrollView.post(Runnable(){
//                scrollView.fullScroll(View.FOCUS_DOWN)
//            })
        } catch (ex : Exception){

        }
        editTextMessage.text.clear()
    }
}