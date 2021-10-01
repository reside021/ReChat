package com.example.myapplication

import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM
import androidx.appcompat.widget.SwitchCompat
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.io.Serializable
import java.lang.Exception
import java.net.URI
import java.text.FieldPosition
import javax.net.ssl.SSLSocketFactory
import kotlin.random.Random.Default.Companion

class MasterActivity : AppCompatActivity() {
    companion object {
        const val WEB_SOCKET_URL = "ws://chatserv.sytes.net:9001"
        const val REQUEST_TAKE_PHOTO = 0
        const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
        lateinit var webSocketClient: WebSocketClient
        lateinit var sqliteHelper: SqliteHelper
        private var OTHER_MSG = 0
    }

    private lateinit var profileTab : TextView
    private  lateinit var chatTab : TextView
    private lateinit var friendsTab : TextView
    private  lateinit var parentLinearLayout: LinearLayout
    private lateinit var view : View
    private  lateinit var listOnlineUser : MutableMap<String, String>
    private lateinit var switchBeOnline : SwitchCompat
    private lateinit var myAdapterForFriends : MyAdapterForFriends
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master);
        listOnlineUser = mutableMapOf()
        parentLinearLayout = findViewById(R.id.masterLayout)
        sqliteHelper = SqliteHelper(this)
        sqliteHelper.addUser(Pair("0","Global Chat"))
        sqliteHelper.createMsgDlgTable()
        val actionBar = this.supportActionBar
        actionBar?.displayOptions = DISPLAY_SHOW_CUSTOM
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setCustomView(R.layout.actionbar)
        view = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.clearlayout, null)
        profileTab = findViewById(R.id.profile)
        chatTab = findViewById(R.id.chat)
        friendsTab = findViewById(R.id.friends)
        profileTab.setOnClickListener { profileTabClick() }
        chatTab.setOnClickListener { chatTabClick() }
        friendsTab.setOnClickListener { friendsTabClick() }
        profileTabClick()

        switchBeOnline = findViewById(R.id.switchBeOnline)
        switchBeOnline.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                Toast.makeText(this, "Виден всем",
                    Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Виден только друзьям",
                    Toast.LENGTH_SHORT).show()
            }
        }
//        initWebSocket()
    }


    private fun profileTabClick(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.profile, null)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        profileTab.background = resources.getDrawable(R.drawable.bottom_line)
        chatTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)
    }

    private fun chatTabClick(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.chat, null)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        chatTab.background = resources.getDrawable(R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)
        val listUserChat = sqliteHelper.getAllUsers()
        val myAdapterForChat = MyAdapterForChat(listUserChat)
        val listViewChat = findViewById<ListView>(R.id.listViewChat)
        listViewChat.adapter = myAdapterForChat
        listViewChat.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            val intent = Intent(this, ChatPeople::class.java);
            val idUser = view.findViewById<TextView>(R.id.idUser)
            intent.putExtra("id", idUser.text)
            startActivity(intent)
        }
        listViewChat.onItemLongClickListener = AdapterView.OnItemLongClickListener {
                parent, view, position, id ->
            val builder = AlertDialog.Builder(this)
            val dialogInflater = this.layoutInflater
            val dialogView  = dialogInflater.inflate(R.layout.dialog_actions_with_chatchannel, null)
            val idUser = view.findViewById<TextView>(R.id.idUser)
            builder.setView(dialogView)
            val alertDialog = builder.create();
            alertDialog.show()
            dialogView.findViewById<Button>(R.id.delThisChat).setOnClickListener(){
                sqliteHelper.DelTable(idUser.text.toString())
                alertDialog.dismiss()
            }
            return@OnItemLongClickListener true
        }
    }

    private  fun friendsTabClick(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.friends, null)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        friendsTab.background = resources.getDrawable(R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        chatTab.setBackgroundColor(0)

        myAdapterForFriends = MyAdapterForFriends(listOnlineUser, sqliteHelper)
        val listViewFriends = findViewById<ListView>(R.id.listViewFriends)
        listViewFriends.adapter = myAdapterForFriends
    }


//    override fun onPause() {
//        super.onPause()
//        webSocketClient.close()
//        sqliteHelper.DelTable()
//    }


//    private fun initWebSocket(){
//        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
//
//        val chatservUri: URI? = URI(WEB_SOCKET_URL)
//        createWebSocketClient(chatservUri)
////        webSocketClient.setSocketFactory(socketFactory)
//        webSocketClient.connect()
//    }

//    private fun createWebSocketClient(chatservURI: URI?){
//        webSocketClient = object : WebSocketClient(chatservURI){
//            override fun onOpen(handshakedata: ServerHandshake?) {
//                Log.i("__CHAT__", "onOpen");
//                this@MasterActivity.runOnUiThread { setName() }
//            }
//
//            override fun onClose(code: Int, reason: String?, remote: Boolean) {
//                Log.i("__CHAT__", "onClose");
//            }
//
//            override fun onMessage(message: String?) {
//                Log.i("__CHAT__", "onMessage: $message");
//                parseMessage(message)
//            }
//
//            override fun onError(ex: Exception?) {
//                Log.i("__CHAT__", "onException: $ex");
//            }
//        }
//    }


    private fun func(idUser : String, textMSG : String){
        val sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        if(!sp.getBoolean("active",false)) return
        if(sp.getString("idActive","NONE") != idUser ||
            sp.getString("idActive","NONE") != "0") return
        try{
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val newView = inflater.inflate(R.layout.message_from, null)
            val textInMessage = newView.findViewById<TextView>(R.id.msgFrom)
            textInMessage.text = textMSG
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            ChatPeople.mainWindowOuter.addView(newView, lp)
        } catch (ex : Exception){
        }
    }

    fun parseMessage(message: String?){
        when (message?.substringBefore("::")) {
            "FC" -> {
                val tag = message.substringAfter("::")
                Log.d("QQQQQQQQ","PARSEMESSAGE")
                setTagProfile(tag)

            }
            "MESSAGE_FROM" -> {
                val idWithMsg = message.substringAfter("::")
                val id = idWithMsg.substringBefore("::")
                val msg = idWithMsg.substringAfter("::")
                sqliteHelper.addMsgInTable(id, OTHER_MSG, msg,"0")
                this@MasterActivity.runOnUiThread {
                    func(id, msg)
                }
            }
            "ONLINE" -> {
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                val name = idWithName.substringAfter("::")
                listOnlineUser[id] = name

            }
            "OFFLINE" ->{
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                val name = idWithName.substringAfter("::")
                listOnlineUser.remove(id)
            }
            else -> {
                //что-то сделать
            }
        }
    }


    private fun setTagProfile(tag : String){
        this@MasterActivity.runOnUiThread {
            val tagTextView = findViewById<TextView>(R.id.tagofuser)
            tagTextView.text = tag
        }
    }


    fun setName(){
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_setname, null);
        val nameuser = view.findViewById<EditText>(R.id.nameuser)
        builder.setView(view)
            .setPositiveButton("OK") { dialog, id ->
                setNameSuccess(nameuser.text.toString()
                )
            }
            .setNegativeButton("Отмена") {
                    dialog, id -> dialog.dismiss();
                if(findViewById<TextView>(R.id.nameofuser).text ==
                    resources.getString(R.string.user_name)) {
                    Toast.makeText(this, "Перед началом работы необходимо задать имя!",
                        Toast.LENGTH_LONG).show()
                    setName()
                }
            }
        builder.show()
    }

    fun onNameClick(view: View) {
        setName()
    }

    private fun setNameSuccess(name : String){
        if(name.isEmpty()){
            Toast.makeText(this, "Перед началом работы необходимо задать имя!",
                Toast.LENGTH_LONG).show()
            setName()
            return
        } else if(name.length > 10){
            Toast.makeText(this, "Имя короче 10 символов",
                Toast.LENGTH_LONG).show()
            setName()
            return
        }
        try {
            this@MasterActivity.runOnUiThread {
                Log.d("QQQQQQ", "SETNAME")
                val nameofuser = findViewById<TextView>(R.id.nameofuser)
                webSocketClient.send("SET_NAME::$name")
                nameofuser.text = name;
                Toast.makeText(this, "Имя успешно изменено",
                    Toast.LENGTH_SHORT).show()
            }

        } catch (ex : Exception){
            Log.d("QQQQq","$ex")
            Toast.makeText(this, "Ошибка изменения имени",
                Toast.LENGTH_LONG).show()
        }
    }

    fun changePhotoClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_change_photo, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<TextView>(R.id.changeAvatar).setOnClickListener(){
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
            }
            alertDialog.dismiss()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            if(requestCode === REQUEST_SELECT_IMAGE_IN_ALBUM || requestCode === REQUEST_TAKE_PHOTO){
                if(resultCode === Activity.RESULT_OK){
                    val imageUri = data?.data as Uri
                    var imageBitmap: Bitmap
                    imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
                    } else {
                        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    }
                    findViewById<ImageView>(R.id.imageofuser).setImageBitmap(imageBitmap)
                }
            } else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        } catch (ex : Exception){
            Log.d("QQQQQQQQ","$ex")
        }

    }



}