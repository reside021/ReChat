package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.adapters.MyAdapterForMsg
import com.example.myapplication.dataClasses.Msg
import com.example.myapplication.dataClasses.UpdateCountMsg
import com.example.myapplication.interfaces.UploadImgMsg
import com.example.myapplication.ui.UserFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.time.LocalDateTime
import java.util.*


class ChatPeople :
    AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener{
    companion object{
        lateinit var mainWindowOuter: LinearLayout
        lateinit var scrollView: ScrollView
        const val CHOOSE_IMG = 1
    }
    private lateinit var animAlpha: Animation
    private lateinit var editTextMessage : EditText
    private lateinit var listViewChat : ListView
    private lateinit var idUser : String
    private lateinit var nameOfUser : String
    private lateinit var dialog_id: String
    private lateinit var sp : SharedPreferences
    private var hasImg : Boolean = false
    private lateinit var uriImg : Uri
    private lateinit var countNewMsg : String
    private lateinit var dataOfMsg : MutableList<Array<String>>
    private lateinit var adapterForChat : MyAdapterForMsg
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_window)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha)
        editTextMessage = findViewById(R.id.editTextMessage)
        listViewChat = findViewById(R.id.scrollChat)
        findViewById<ImageView>(R.id.choose_img_msg).setOnClickListener(chooseImgMsg)
        findViewById<ImageButton>(R.id.dltImgMsg).setOnClickListener(dltImgMsg)
        idUser = intent.extras?.getString("idTag").toString()
        nameOfUser = intent.extras?.getString("nameOfUser").toString()
        countNewMsg = intent.extras?.getString("countNewMsg").toString()

        workWithActionBar()

        dialog_id = sqliteHelper.getDialogIdWithUser(idUser)
        recoveryAllMsg(dialog_id)
    }

    private val openUserProfile = View.OnClickListener {
        if(idUser == "0" || idUser.startsWith("G")) return@OnClickListener
        val intent = Intent(this, FriendsProfile::class.java);
        intent.putExtra("idTag", idUser)
        intent.putExtra("nameOfUser", nameOfUser)
        startActivity(intent)
    }
    private val dltImgMsg = View.OnClickListener {
        it.startAnimation(animAlpha)
        val placeForImg = findViewById<ImageView>(R.id.addingImgMsg)
        placeForImg.setImageDrawable(null)
        findViewById<LinearLayout>(R.id.layoutAddImgMsg).visibility = View.GONE
        hasImg = false
        editTextMessage.isEnabled = true
    }
    private val chooseImgMsg = View.OnClickListener {
        it.startAnimation(animAlpha)
        ImagePicker.with(this)
            .crop()	    			//Crop image(Optional), Check Customization for more option
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start(CHOOSE_IMG)
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
        ed.putString("idActive", "closeChat")
        ed.apply()
    }

    override fun onRestart() {
        super.onRestart()
        if(webSocketClient.connection.isClosed) {
            val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
            val ed = sp.edit()
            ed.putString("onRestart", LocalDateTime.now().toString())
            ed.apply()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun recoveryAllMsg(dialog_id: String){
        val ourTag = sp.getString("tagUser", null)!!
        dataOfMsg = sqliteHelper.getMsgWithUser(dialog_id)
        adapterForChat = MyAdapterForMsg(this,dataOfMsg, dialog_id, ourTag)
        listViewChat.adapter = adapterForChat
        if(webSocketClient.connection.isClosed){
            Toast.makeText(
                this@ChatPeople, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT
            ).show()
        } else{
            val updateCountMsg =
                UpdateCountMsg("UPDATE::", "COUNTMSG::", dialog_id, idUser, countNewMsg)
            val dataServerName = Json.encodeToString(updateCountMsg)
            webSocketClient.send(dataServerName)
        }
    }

    private fun workWithActionBar(){
        val queryImg = sp.getString("queryImg","0")
        val toolBar : Toolbar = findViewById(R.id.toolbar)
        val nameOfUserView = toolBar.findViewById<TextView>(R.id.nameOfUserThisChat)
        nameOfUserView.setOnClickListener(openUserProfile)
        val avatarUserView = toolBar.findViewById<ImageView>(R.id.avatarUserDialog)
        avatarUserView.setOnClickListener(openUserProfile)
        val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$idUser.jpg?time=$queryImg"
        Picasso.get()
            .load(urlAvatar)
            .placeholder(R.drawable.user_photo_white)
            .into(avatarUserView)
        nameOfUserView.text = nameOfUser
        setSupportActionBar(toolBar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    fun onSendMsgClick(view: View) {
        view.startAnimation(animAlpha)
        try{
            if(webSocketClient.connection.isClosed){
                Toast.makeText(this, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                return
            }
            var textMSG = ""
            var msg = ""
            if(hasImg){
                val photoName = Calendar.getInstance().timeInMillis.toString()
                val chatName = sqliteHelper.getDialogIdWithUser(idUser)
                val addressImg = uriImg.toString().substringAfter("//")
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://imagerc.ddns.net:80/userImgMsg/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                val service = retrofit.create(UploadImgMsg::class.java)
                val file = File(addressImg)
                val requestFile: RequestBody =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"), file)
                val body =
                    MultipartBody.Part
                        .createFormData("image", file.name, requestFile)
                val response : Call<String> = service.newImgInMsg(chatName, photoName, body)
                response.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if(response.isSuccessful){
                            if(response.code() == 200){
                                if(webSocketClient.connection.isClosed){
                                    Toast.makeText(
                                        this@ChatPeople, "Отсутствует подключение к серверу",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else{
                                            val dataUser = Msg(
                                                "MESSAGE_TO::",
                                                dialog_id,
                                                "IMAGE",
                                                idUser,
                                                photoName)
                                            val msg = Json.encodeToString(dataUser)
                                            webSocketClient.send(msg)
                                    val placeForImg = findViewById<ImageView>(R.id.addingImgMsg)
                                    placeForImg.setImageDrawable(null)
                                    findViewById<LinearLayout>(R.id.layoutAddImgMsg)
                                        .visibility = View.GONE
                                    hasImg = false
                                    editTextMessage.isEnabled = true
                                }
                            }
                        }else{
                            Toast.makeText(this@ChatPeople, "Ошибка отправки сообщения", Toast.LENGTH_LONG).show()
                        }
                    }
                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(this@ChatPeople, "Ошибка отправки сообщения", Toast.LENGTH_LONG).show()
                    }
                })
            }else{
                textMSG = editTextMessage.text.trim().toString()
                val dataUser = Msg("MESSAGE_TO::",dialog_id,"TEXT", idUser, textMSG)
                msg = Json.encodeToString(dataUser)
            }

            if(textMSG.isBlank()) return
            if(msg.isBlank()) return

            webSocketClient.send(msg)
        } catch (ex : Exception){ }
        editTextMessage.text.clear()
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            if(requestCode == CHOOSE_IMG){
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        uriImg = data?.data!!
                        findViewById<LinearLayout>(R.id.layoutAddImgMsg)
                            .visibility = View.VISIBLE
                        val placeForImg = findViewById<ImageView>(R.id.addingImgMsg)
                        placeForImg.setImageURI(uriImg)
                        hasImg = true
                        editTextMessage.text.clear()
                        editTextMessage.isEnabled = false
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                    }
                }
            } else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        } catch (ex: Exception){ }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("newMsgForDisplay")){
            val timeCreated = sp.getString(key,"")!!.toInt()
            dataOfMsg.add(sqliteHelper.getLastMsgWithUser(dialog_id, timeCreated))
            adapterForChat.notifyDataSetChanged()
        }
    }
}