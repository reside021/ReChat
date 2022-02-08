package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.dataClasses.Msg
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.interfaces.UploadImgMsg
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.*


class ChatPeople : AppCompatActivity() {
    companion object{
        lateinit var mainWindowOuter: LinearLayout
        lateinit var scrollView: ScrollView
        const val CHOOSE_IMG = 1
    }
    private lateinit var animAlpha: Animation
    private lateinit var editTextMessage : EditText
    private lateinit var mainWindowInclude : LinearLayout
    private lateinit var idUser : String
    private lateinit var dialog_id: String
    private lateinit var sp : SharedPreferences
    private var hasImg : Boolean = false
    private lateinit var uriImg : Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat_window)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        animAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha)
        editTextMessage = findViewById(R.id.editTextMessage)

        mainWindowInclude = findViewById(R.id.mainChatWindow)
        findViewById<ImageView>(R.id.choose_img_msg).setOnClickListener {
            it.startAnimation(animAlpha)
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start(CHOOSE_IMG)
        }
        findViewById<ImageButton>(R.id.dltImgMsg).setOnClickListener {
            it.startAnimation(animAlpha)
            val placeForImg = findViewById<ImageView>(R.id.addingImgMsg)
            placeForImg.setImageDrawable(null)
            findViewById<LinearLayout>(R.id.layoutAddImgMsg)
                .visibility = View.GONE
            hasImg = false
            editTextMessage.isEnabled = true
        }
        idUser = intent.extras?.getString("idTag").toString()
        val nameOfUser = intent.extras?.getString("nameOfUser").toString()

        supportActionBar?.apply {
            title = nameOfUser
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        scrollView = findViewById(R.id.scrollChat)
        mainWindowOuter = mainWindowInclude
        dialog_id = sqliteHelper.getDialogIdWithUser(idUser)
        recoveryAllMsg(dialog_id)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun recoveryAllMsg(dialog_id: String){
        val ourTag = sp.getString("tagUser", null)
        val dataOfMsg = sqliteHelper.getMsgWithUser(dialog_id)
        for(el in dataOfMsg){
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            var newView = inflater.inflate(R.layout.clearlayout, null)
            when {
                el[3] == "IMAGE" -> {
                    val nameImg = el[1]
                    var chatName = dialog_id.replace("#", "%23")
                    chatName = chatName.replace("::", "--")
                    val urlImg = "http://imagerc.ddns.net:80/userImgMsg/$chatName/$nameImg.jpg"
                    var imageInMessage: ImageView
                    if(el[0] != ourTag){
                        var nameOfUser = sqliteHelper.getNameInUserChat(el[0])
                        if (nameOfUser.isEmpty()){
                            nameOfUser = resources.getString(R.string.user_name)
                        }
                        newView = inflater.inflate(R.layout.message_from_image, null)
                        newView.findViewById<TextView>(R.id.senderName).text = nameOfUser
                        imageInMessage = newView.findViewById(R.id.msgFromImage)
                    } else{
                        newView = inflater.inflate(R.layout.message_to_image, null)
                        imageInMessage = newView.findViewById(R.id.msgToImage)
                    }
                    Picasso.get()
                        .load(urlImg)
                        .placeholder(R.drawable.error_image)
                        .into(imageInMessage)
                }
                el[3] == "TEXT" -> {
                    if(el[0] != ourTag){
                        var nameOfUser = sqliteHelper.getNameInUserChat(el[0])
                        if (nameOfUser.isEmpty()){
                            nameOfUser = resources.getString(R.string.user_name)
                        }
                        newView = inflater.inflate(R.layout.message_from, null)
                        newView.findViewById<TextView>(R.id.msgFrom).text = el[1]
                        newView.findViewById<TextView>(R.id.senderName).text = nameOfUser
                    } else{
                        newView = inflater.inflate(R.layout.message_to, null)
                        newView.findViewById<TextView>(R.id.msgTO).text = el[1]
                    }
                }
            }
            mainWindowInclude.addView(newView, lp)
            scrollView.post(Runnable(){
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
        }
    }



    fun onSendMsgClick(view: View) {
        view.startAnimation(animAlpha)
        try{
            if(webSocketClient.connection.readyState.ordinal == 0){
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
                                if(webSocketClient.connection.readyState.ordinal == 0){
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
                textMSG = editTextMessage.text.toString()
                val dataUser = Msg("MESSAGE_TO::",dialog_id,"TEXT", idUser, textMSG)
                msg = Json.encodeToString(dataUser)
            }

            if(textMSG.isEmpty()) return
            if(msg.isEmpty()) return

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
                        scrollView.post(Runnable(){
                            scrollView.fullScroll(View.FOCUS_DOWN)
                        })
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
}