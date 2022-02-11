package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.dataClasses.ActionsWithFrnd
import com.example.myapplication.dataClasses.ConfirmAddFriend
import com.example.myapplication.dataClasses.DeleteFriend
import com.example.myapplication.ui.UserFragment
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

class FriendsProfile :
    AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener{

    private lateinit var tagUser : String
    private lateinit var sp : SharedPreferences
    private lateinit var userInFriendsBtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friends_profile)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        val nameOfUserView = findViewById<TextView>(R.id.nameOfUser)
        val tagUserView = findViewById<TextView>(R.id.tagOfUser)
        userInFriendsBtn = findViewById(R.id.userInFriend)
        tagUser = intent.extras?.getString("idTag").toString()
        val nameOfUser = intent.extras?.getString("nameOfUser").toString()
        setTextStatus()
        if(tagUser == sp.getString("tagUser","")) userInFriendsBtn.visibility = View.GONE
        nameOfUserView.text = nameOfUser
        tagUserView.text = tagUser

        supportActionBar?.apply {
            title = nameOfUser
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
        val imageOfUser = findViewById<ImageView>(R.id.imageOfUser)
        Picasso.get()
            .load(urlAvatar)
            .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .placeholder(R.drawable.user_profile_photo)
            .into(imageOfUser)

        findViewById<ImageButton>(R.id.copyTagBtn).setOnClickListener {
            try {
                val clipboard: ClipboardManager? =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("TAG", tagUser)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    this, resources.getString(R.string.copy_tag),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (ex: Exception){}
        }
        userInFriendsBtn.setOnClickListener {
            if(webSocketClient.connection.isClosed){
                Toast.makeText(
                    this, "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                when (sqliteHelper.getStatusUser(tagUser)) {
                    -1 -> {
                        val dataUser = ActionsWithFrnd("FRND::", "ADD::", tagUser, nameOfUser)
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                    0 -> {
                        val dataUser = ConfirmAddFriend("FRND::", "CNFRMADD::", tagUser)
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                    1 -> {
                        return@setOnClickListener
                    }
                    2 -> {
                        val dataUser = DeleteFriend("FRND::", "DELETE::", tagUser, "DELFROMFRND")
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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

    private fun setTextStatus(){
        when (sqliteHelper.getStatusUser(tagUser)) {
            -1 -> {
                userInFriendsBtn.text = "Добавить в друзья"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            0 -> {
                userInFriendsBtn.text = "Принять запрос"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            1 -> {
                userInFriendsBtn.text = "Ожидает подтверждения"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            2 -> {
                userInFriendsBtn.text = "Удалить из друзей"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.green)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("changeStatus")){
            setTextStatus()
        }
    }

}