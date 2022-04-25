package com.example.myapplication

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.dataClasses.*
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

    companion object{
        private val GENDERUSER = "genderUser"
        private val BIRTHDAYUSER = "birthdayUser"
        private val SOCSTATUSUSER = "socStatusUser"
        private val COUNTRYUSER = "countryUser"
        private val DATEREGUSER = "dateRegUser"
        private val ABOUTMEUSER = "aboutMeUser"
    }

    private lateinit var tagUser : String
    private lateinit var sp : SharedPreferences
    private lateinit var userInFriendsBtn : Button
    private var isFriend : Boolean = false
    private lateinit var loadingData : LinearLayout
    private lateinit var dataAboutUser : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.friends_profile)
        loadingData = findViewById(R.id.loadingData)
        dataAboutUser = findViewById(R.id.dataAboutUser)
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
                        isFriend = false
                        val dataUser = ActionsWithFrnd("FRND::", "ADD::", tagUser, nameOfUser)
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                    0 -> {
                        isFriend = false
                        val dataUser = ConfirmAddFriend("FRND::", "CNFRMADD::", tagUser)
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                    1 -> {
                        isFriend = false
                        return@setOnClickListener
                    }
                    2 -> {
                        isFriend = true
                        val dataUser = DeleteFriend("FRND::", "DELETE::", tagUser, "DELFROMFRND")
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                }
            }
        }
        if(!webSocketClient.connection.isClosed){
            val downloadDataUser =
                DownloadDataUser(
                "DOWNLOAD::",
                "ALLINFOUSERS::",
                    tagUser,
                    isFriend
                )
            val dataServerName = Json.encodeToString(downloadDataUser)
            webSocketClient.send(dataServerName)
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

    override fun onDestroy() {
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }
    private fun setTextStatus(){
        when (sqliteHelper.getStatusUser(tagUser)) {
            -1 -> {
                isFriend = false
                userInFriendsBtn.text = "Добавить в друзья"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            0 -> {
                isFriend = false
                userInFriendsBtn.text = "Принять запрос"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            1 -> {
                isFriend = false
                userInFriendsBtn.text = "Ожидает подтверждения"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.accentColor)
            }
            2 -> {
                isFriend = true
                userInFriendsBtn.text = "Удалить из друзей"
                userInFriendsBtn.backgroundTintList = AppCompatResources.getColorStateList(this, R.color.green)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("changeStatus")){
            setTextStatus()
        }
        if(key.equals("changeDataOfUser")){
            val allUserInfo = sqliteHelper.getAllUserInfo(tagUser)
            if (allUserInfo.isEmpty())
            {
                loadingData.visibility = View.GONE
            }
            else
            {
                val genderUser = dataAboutUser.findViewById<TextView>(R.id.genderUser)
                val birthdayUser = dataAboutUser.findViewById<TextView>(R.id.birthdayUser)
                val socStatusUser = dataAboutUser.findViewById<TextView>(R.id.socStatusUser)
                val countryUser = dataAboutUser.findViewById<TextView>(R.id.countryUser)
                val dateRegUser = dataAboutUser.findViewById<TextView>(R.id.dateRegUser)
                val aboutmeUser = dataAboutUser.findViewById<TextView>(R.id.aboutmeUser)
                val genderArray = resources.getStringArray(R.array.gender)
                genderUser.text = genderArray[allUserInfo[GENDERUSER]!!.toInt()]
                birthdayUser.text = allUserInfo[BIRTHDAYUSER]
                socStatusUser.text = allUserInfo[SOCSTATUSUSER]
                countryUser.text = allUserInfo[COUNTRYUSER]
                dateRegUser.text = allUserInfo[DATEREGUSER]
                aboutmeUser.text = allUserInfo[ABOUTMEUSER]
                loadingData.visibility = View.GONE
                dataAboutUser.visibility = View.VISIBLE
            }
            sqliteHelper.clearAllUserInfo()
        }
    }
}