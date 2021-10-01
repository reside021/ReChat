package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
        webSocketClient = MasterActivity.webSocketClient
        sqliteHelper = MasterActivity.sqliteHelper
        idUser = intent.extras?.getString("id").toString()
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

        // Store our shared preference
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("active", false)
        ed.apply()
    }

    fun onReceiveMsg(dialogID : String, textMSG : String){
        if(dialogID != idUser) return

        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        if(!sp.getBoolean("active",false)) return

        try{
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_from, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
            textInMessage.text = textMSG
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mainWindowInclude.addView(newView, lp)
        } catch (ex : Exception){

        }
    }

    fun onSendMsgClick(view: View) {
        view.startAnimation(animAlpha)
        try{
            val textMSG = editTextMessage.text
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_to, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
            textInMessage.text = textMSG
            webSocketClient.send("MESSAGE_TO::$idUser::$textMSG")
            val c: Calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val strDate: String = sdf.format(c.time)
//            Toast.makeText(this, strDate,
//                Toast.LENGTH_SHORT).show()
            sqliteHelper.addMsgInTable(idUser, ME_MSG, textMSG.toString(), strDate)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mainWindowInclude.addView(newView, lp)
        } catch (ex : Exception){

        }
        editTextMessage.text.clear()
    }
}