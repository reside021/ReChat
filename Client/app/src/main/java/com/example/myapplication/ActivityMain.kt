package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.ChatPeople.Companion.mainWindowOuter
import com.example.myapplication.ChatPeople.Companion.scrollView
import com.example.myapplication.adapters.MyAdapterForChat
import com.example.myapplication.adapters.MyAdapterForFriends
import com.example.myapplication.adapters.MyAdapterForRequest
import com.example.myapplication.adapters.MyAdapterForUsers
import com.example.myapplication.dataClasses.*
import com.example.myapplication.interfaces.*
import com.example.myapplication.ui.*
import com.gauravk.bubblenavigation.BubbleNavigationLinearView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.net.URI
import java.security.Key
import java.time.LocalDateTime


class ActivityMain :
        AppCompatActivity(),
        UserFragment.OnFragmentSendDataListener,
        ChatFragment.OnFragmentSendDataListener,
        FriendsFragment.OnFragmentSendDataListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        FirstDisplayFragment.OnFragmentSendDataListener,
        AuthFragment.OnFragmentSendDataListener,
        FriendListFragment.OnFragmentSendDataListener,
        UserListFragment.OnFragmentSendDataListener,
        FrndListRequestFragment.OnFragmentSendDataListener{

    companion object {
        const val WEB_SOCKET_URL = "ws://servchat.ddns.net:9001"
        const val IMAGE_REQUEST = 1
        const val VERSION_APP = "0.3"
        lateinit var webSocketClient : WebSocketClient
        lateinit var sqliteHelper: SqliteHelper
    }

    private lateinit var sp : SharedPreferences
    private lateinit var tagUser : String
    private lateinit var bubbleNav : BubbleNavigationLinearView
    private var needLoad : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_nav_bar)

        sqliteHelper = SqliteHelper(this)
        supportActionBar?.title = resources.getString(R.string.rechat)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        bubbleNav = findViewById(R.id.bottom_navigation_view_linear)

        isActVersion()

        loadFragment(-1)
        bubbleNav.setNavigationChangeListener { view, position ->
            loadFragment(position)
        }
    }

    private fun isActVersion(){
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://imagerc.ddns.net:80/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(GetActualVersion::class.java)
        val response : Call<ActualVersion> = service.getVersison()
        response.enqueue(object : Callback<ActualVersion>{
            override fun onResponse(call: Call<ActualVersion>, response: Response<ActualVersion>) {
                if(!response.isSuccessful) return
                if(response.code() != 200) return
                if(VERSION_APP == response.body()?.version) {
                    initWebSocket()
                }else{
                    Toast.makeText(this@ActivityMain,
                        "Необходимо установить актуальную версию приложения",
                        Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ActualVersion>, t: Throwable) {
                Toast.makeText(this@ActivityMain,
                    "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPostResume() {
        super.onPostResume()
        if(needLoad){
            loadFragment(-1)
            Toast.makeText(this@ActivityMain,
                "Потеряно соединение с сервером\nПожалуйста, перезапустите приложение",
                Toast.LENGTH_LONG).show()
        }
        needLoad = false
    }
    override fun onRestart() {
        super.onRestart()
        if(webSocketClient.connection.isClosed) {
            needLoad = true
        }
    }

    private fun loadFragment(position : Int){
        val toolbar = supportActionBar
        val fragment: Fragment
        when (position) {
            0 -> {
                bubbleNav.visibility = View.VISIBLE
                toolbar?.title = resources.getString(R.string.profile)
                fragment = UserFragment()
            }
            1 -> {
                bubbleNav.visibility = View.VISIBLE
                toolbar?.title = resources.getString(R.string.chat)
                fragment = ChatFragment()
            }
            2 -> {
                bubbleNav.visibility = View.VISIBLE
                toolbar?.title = resources.getString(R.string.people)
                fragment = FriendsFragment()
            }
            3 ->{
                bubbleNav.visibility = View.GONE
                fragment = AuthFragment()
                toolbar?.title = resources.getString(R.string.rechat)
            }
            else -> {
                bubbleNav.visibility = View.GONE
                fragment = FirstDisplayFragment()
                toolbar?.title = resources.getString(R.string.rechat)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.host_fragment, fragment)
            .commit()
    }


    override fun onUserLoadView() {
        val userName = sp.getString("nickname", resources.getString(R.string.user_name))!!
        val isAvatar = sp.getBoolean("isAvatar", false)
        val queryImg  =sp.getString("queryImg","0")
        val urlAvatar = if(isAvatar){
            "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg?time=$queryImg"
        } else{
            ""
        }
        val isVisible = sp.getBoolean("isVisible", false)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
        fragment.setUserData(tagUser, userName, isAvatar, urlAvatar, isVisible)
    }
    override fun onChatLoadView() {
        val queryImg  = sp.getString("queryImg","0")!!
        val myAdapterForChat = MyAdapterForChat(tagUser, queryImg)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as ChatFragment
        fragment.setUserData(myAdapterForChat)
    }
    override fun onFriendsLoadView() {}
    override fun onFirstDisplayLoadView() {}
    override fun onAuthLoadView() {}
    override fun onFriendsListLoadView() {
        val queryImg  = sp.getString("queryImg","0")!!
        val myAdapterForFriends = MyAdapterForFriends(tagUser, queryImg)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragmentListFriends) as FriendListFragment
        fragment.setUserData(myAdapterForFriends)
    }
    override fun onUserListLoadView() {
        val queryImg  = sp.getString("queryImg","0")!!
        val myAdapterForUsers = MyAdapterForUsers(queryImg)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragmentListFriends) as UserListFragment
        fragment.setUserData(myAdapterForUsers)
    }
    override fun onFrndListRequestLoadView() {
        val queryImg  = sp.getString("queryImg","0")!!
        val myAdapterForRequest = MyAdapterForRequest(tagUser, queryImg)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragmentListFriends) as FrndListRequestFragment
        fragment.setUserData(myAdapterForRequest)
    }

    fun changePhotoClick(view : View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_change_photo, null)
        builder.setView(view)
        val alertDialog = builder.create()
        alertDialog.show()
        view.findViewById<TextView>(R.id.changeAvatar).setOnClickListener{
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(IMAGE_REQUEST)
            alertDialog.dismiss()
        }
        view.findViewById<TextView>(R.id.deleteAvatar).setOnClickListener {
            val isAvatar = sp.getBoolean("isAvatar", false)
            if (isAvatar) {
                try {
                    if (!webSocketClient.connection.isClosed) {
                        val retrofit = Retrofit.Builder()
                                .baseUrl("http://imagerc.ddns.net:80/avatar/")
                                .addConverterFactory(ScalarsConverterFactory.create())
                                .build()
                        val service = retrofit.create(DeleteAvatar::class.java)
                        val response: Call<String> = service.deleteProfile(tagUser)
                        response.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if (response.isSuccessful) {
                                    if (response.code() == 200) {
                                        val queryDeleteAvatar = DeleteUserAvatar("DELETEAVATAR::")
                                        val dataServerName = Json.encodeToString(queryDeleteAvatar)
                                        webSocketClient.send(dataServerName)
                                        alertDialog.dismiss()
                                    }
                                } else {
                                    Toast.makeText(this@ActivityMain,
                                            "Ошибка смены изображения",
                                            Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(this@ActivityMain,
                                        "Ошибка смены изображения",
                                        Toast.LENGTH_LONG).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@ActivityMain,
                                "Отсутствует подключение к серверу",
                                Toast.LENGTH_LONG).show()
                    }
                } catch (ex: Exception) {
                    Toast.makeText(
                            this@ActivityMain, ex.message.toString(),
                            Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            if(requestCode == IMAGE_REQUEST){
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val uri: Uri = data?.data!!
                        val retrofit = Retrofit.Builder()
                                .baseUrl("http://imagerc.ddns.net:80/avatar/")
                                .addConverterFactory(ScalarsConverterFactory.create())
                                .build()
                        val service = retrofit.create(UploadAvatar::class.java)
                        val addressImg = uri.toString().substringAfter("//")
                        val file = File(addressImg)
                        val requestFile: RequestBody =
                                RequestBody.create(
                                        MediaType.parse("multipart/form-data"), file)
                        val body =
                                MultipartBody.Part
                                        .createFormData("image", file.getName(), requestFile)
                        val response : Call<String> = service.updateProfile(tagUser,body)
                        response.enqueue(object : Callback<String> {

                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if(response.isSuccessful){
                                    if(response.code() == 200){
                                        if(webSocketClient.connection.isClosed){
                                            Toast.makeText(
                                                    this@ActivityMain, "Отсутствует подключение к серверу",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                        } else{
                                            val querySetAvatar =
                                                SuccessSetAvatar("SETAVATAR::", true)
                                            val dataServerName = Json.encodeToString(querySetAvatar)
                                            webSocketClient.send(dataServerName)
                                        }
                                    }
                                }else{
                                    Toast.makeText(this@ActivityMain, "Ошибка смены изображения", Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(this@ActivityMain, "Ошибка смены изображения", Toast.LENGTH_LONG).show()
                            }
                        })
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
        if(key.equals("changeNickName")){
            val userName = sp.getString("nickname", resources.getString(R.string.user_name))!!
            val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
            if(fragment.isVisible){
                fragment.setNewUserName(userName)
            }
        }
        if(key.equals("changeVisible")){
            val isVisible = sp.getBoolean("isVisible", false)!!
            val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
            if(fragment.isVisible){
                fragment.setNewVisible(isVisible)
            }
        }
        if(key.equals("changeAvatar")){
            val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
            val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
            if(fragment.isVisible){
                fragment.setNewUserImage(urlAvatar)
            }
        }
        if(key.equals("onRestart")){
           onRestart()
        }
        if(key.equals("changeChatList")){
            val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as ChatFragment
            if(fragment.isVisible){
                fragment.onStart()
            }
        }
    }

    private fun initWebSocket(){
        sqliteHelper.clearTable()
//        val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val chatservUri = URI(WEB_SOCKET_URL)
        createWebSocketClient(chatservUri)
//        webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun createWebSocketClient(chatservURI: URI?){
        webSocketClient = object : WebSocketClient(chatservURI){
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("__CHAT__", "onOpen")

                val tokenAuth = sp.getString("token", "")
                if (tokenAuth != null) {
                    if (tokenAuth.isEmpty()) {
                        runOnUiThread {
                            loadFragment(3)
                        }
                    } else{
                        val successAuthToken = SuccessAuthToken("AUTHTOKEN::", tokenAuth)
                        val msg = Json.encodeToString(successAuthToken)
                        if(!webSocketClient.connection.isClosed){
                            webSocketClient.send(msg)
                        }
                    }
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i("__CHAT__", "onClose")
            }

            override fun onMessage(message: String?) {
                Log.i("__CHAT__", "onMessage: $message");
                this@ActivityMain.runOnUiThread{
                    parseMessage(message)
                }
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
                Toast.makeText(
                    this@ActivityMain,
                    msg,
                    Toast.LENGTH_SHORT
                ).show()
            }
            "RESULTDB" -> {
                val typeOperWithMsg = message.substringAfter("::")
                val typeOper = typeOperWithMsg.substringBefore("::")
                val statusWithMsg = typeOperWithMsg.substringAfter("::")
                val status = statusWithMsg.substringBefore("::")
                val msg = statusWithMsg.substringAfter("::")
                if (typeOper == "AUTH") {
                    if (status == "SUCCESS") {
                        Toast.makeText(
                            this@ActivityMain,
                            "Успешная авторизация",
                            Toast.LENGTH_SHORT
                        ).show()
                        authorization(msg)

                    }
                    if (status == "ERROR") {
                        Toast.makeText(
                            this@ActivityMain,
                            "Ошибка авторизации\nНеверные данные для входа",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                if (typeOper == "AUTHTOKEN") {
                    if (status == "SUCCESS") {
                        deviceAuth(msg)
                    }
                    if (status == "ERROR") {
                        loadFragment(3)
                    }
                }
                if (typeOper == "UPDATE") {
                    if (status == "SUCCESS") {
                        if (msg.substringBefore("::") == "NEWNAME") {
                            val newName = msg.substringAfter("::")
                            val ed = sp.edit()
                            ed.putString("nickname", newName)
                            ed.putString("changeNickName", LocalDateTime.now().toString())
                            LocalDateTime.now().chronology
                            ed.apply()
                            val confirmSetname = ConfirmSetName("SETNAME::", true, newName)
                            val msg = Json.encodeToString(confirmSetname)
                            if (!webSocketClient.connection.isClosed) {
                                webSocketClient.send(msg)
                                Toast.makeText(
                                    this@ActivityMain,
                                    "Имя успешно изменено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (msg.substringBefore("::") == "VISIBLE") {
                            val isVisible = msg.substringAfter("::").toBoolean()
                            val ed = sp.edit()
                            ed.putBoolean("isVisible", isVisible)
                            ed.putString("changeVisible", LocalDateTime.now().toString())
                            ed.apply()
                            val confirmVisible = ConfirmUpVisible("VISIBLE::", true, isVisible)
                            val msg = Json.encodeToString(confirmVisible)
                            if (!webSocketClient.connection.isClosed) {
                                webSocketClient.send(msg)
                            }
                            if (isVisible) {
                                Toast.makeText(
                                    this@ActivityMain, "Виден всем",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this@ActivityMain, "Включен режим призрака",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (msg.substringBefore("::") == "SETAVATAR") {
                            val ed = sp.edit()
                            ed.putBoolean("isAvatar", true)
                            ed.putString("changeAvatar", LocalDateTime.now().toString())
                            ed.apply()
                            Toast.makeText(
                                this@ActivityMain, "Изображение успешно установлено",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (msg.substringBefore("::") == "DELETEAVATAR") {
                            val ed = sp.edit()
                            ed.putBoolean("isAvatar", false)
                            ed.putString("changeAvatar", LocalDateTime.now().toString())
                            ed.apply()
                            Toast.makeText(
                                this@ActivityMain, "Изображение успешно удалено",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (msg.substringBefore("::") == "COUNTMSG") {
                            val jsonData = msg.substringAfter("::")
                            val newData = Json.decodeFromString<ConfirmUpdateCountMsg>(jsonData)
                            sqliteHelper.UpdateCountMsg(newData)
                        }
                    }
                    if (status == "ERROR") {
                        if (msg == "NEWNAME::") {
                            Toast.makeText(
                                this@ActivityMain,
                                "Ошибка изменения имени. Попробуйте позже.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (msg == "VISIBLE::") {
                            Toast.makeText(
                                this@ActivityMain, "Ошибка изменения статуса",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                if (typeOper == "INSERT") {
                    if (status == "SUCCESS") {
                        if (msg.substringBefore("::") == "NEWUSERDLG") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ConfirmInsertNewUserDlg>(jsonData)
                            if (msg.Icreater) {
                                sqliteHelper.addUserInDLG(
                                    msg.dialog_id,
                                    msg.userCompanion,
                                    msg.enteredTime,
                                    msg.countMsg
                                )
                                Toast.makeText(
                                    this, "С пользователем создан чат",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                sqliteHelper.addUserInDLG(
                                    msg.dialog_id,
                                    msg.userManager,
                                    msg.enteredTime,
                                    msg.countMsg
                                )
                                val dialog_ids : MutableList<String> = mutableListOf(msg.dialog_id)
                                val queryAllTagName = QueryAllTagName(
                                    "DOWNLOAD::",
                                    "ALLTAGNAME::",
                                    dialog_ids,
                                    sp.getString("token","")!!
                                )
                                if (!webSocketClient.connection.isClosed) {
                                    val dataServerName = Json.encodeToString(queryAllTagName)
                                    webSocketClient.send(dataServerName)
                                }
                            }
                        }
                        if (msg.substringBefore("::") == "NEWMSGDLG") {
                            val jsonData = msg.substringAfter("::")
                            messageToUser(jsonData)
                        }
                        if (msg.substringBefore("::") == "SIGNUP"){
                            Toast.makeText(
                                this@ActivityMain,
                                "Вы были успешно зарегистрированы!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    if (status == "ERROR") {
                        if (msg.substringBefore("::") == "NEWUSERDLG") {
                            Toast.makeText(
                                this, "Не удалось создать чат",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        if (msg.substringBefore("::") == "SIGNUP"){
                            Toast.makeText(
                                this@ActivityMain,
                                "Ошибка регистрации.\nПопробуйте еще раз.",
                                Toast.LENGTH_SHORT
                            ).show()
                            webSocketClient.close()
                            initWebSocket()
                        }
                    }
                }
                if (typeOper == "DOWNLOAD") {
                    if (status == "SUCCESS") {
                        if (msg.substringBefore("::") == "ALLDLG") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfDlg>(jsonData)
                            if (!checkVerifyJWT(msg.token)) return;
                            val dataOfDialog: List<DataOfDialog> = msg.listOfData
                            for (el in dataOfDialog) {
                                sqliteHelper.addUserInDLG(
                                    el.dialog_id,
                                    el.tagUser,
                                    el.enteredTime,
                                    el.countMsg
                                )
                            }
                            val dialog_ids = sqliteHelper.getAllDlgFromDLG()
                            val queryAllTagName = QueryAllTagName(
                                "DOWNLOAD::",
                                "ALLTAGNAME::",
                                dialog_ids,
                                sp.getString("tokenQuery","")!!
                            )
                            if (!webSocketClient.connection.isClosed) {
                                val dataServerName = Json.encodeToString(queryAllTagName)
                                webSocketClient.send(dataServerName)
                                val queryAllFriends = QueryAllFriends("DOWNLOAD::", "ALLFRND::", sp.getString("tokenQuery","")!!)
                                val dataServerFriends = Json.encodeToString(queryAllFriends)
                                webSocketClient.send(dataServerFriends)
                                val queryAllMsg = QueryAllMsg("DOWNLOAD::", "ALLMSG::", dialog_ids, sp.getString("tokenQuery","")!!)
                                val dataServerMsg = Json.encodeToString(queryAllMsg)
                                webSocketClient.send(dataServerMsg)
                            }

                        }
                        if (msg.substringBefore("::") == "ALLMSG") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfMsg>(jsonData)
                            if (!checkVerifyJWT(msg.token)) return;
                            val dataOfMessage: List<DataOfMessage> = msg.listOfData
                            for (el in dataOfMessage) {
                                sqliteHelper.addMsgInTable(
                                    el.dialog_id,
                                    el.sender,
                                    el.typeMsg,
                                    el.textMsg,
                                    el.timeCreated
                                )
                            }
                        }
                        if (msg.substringBefore("::") == "ALLTAGNAME") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListTagName>(jsonData)
                            if (!checkVerifyJWT(msg.token)) return;
                            val dataOfNickname: List<DataOfNickName> = msg.listOfData
                            for (el in dataOfNickname) {
                                if (sqliteHelper.checkUserInChat(el.tagUser)) break
                                sqliteHelper.addUserInChat(el.tagUser to el.nickUser)
                            }
                            val ed = sp.edit()
                            ed.putString("changeUserDlg", LocalDateTime.now().toString())
                            ed.apply()
                        }
                        if (msg.substringBefore("::") == "ALLFRND") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfFriends>(jsonData)
                            if (!checkVerifyJWT(msg.token)) return;
                            val dataOfFriends: List<DataOfFriends> = msg.listOfData
                            for (el in dataOfFriends) {
                                sqliteHelper.addUserInFriendDW(el)
                            }
                        }
                    }
                    if (status == "ERROR") {
                    }
                }
                if (typeOper == "FRND"){
                    if (status == "SUCCESS"){
                        if (msg.substringBefore("::") == "ADD"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ResultActionWithFrnd>(jsonData)
                            sqliteHelper.addUserInFriend(msg)
                            val ed = sp.edit()
                            ed.putString("changeStatus", LocalDateTime.now().toString())
                            ed.apply()
                            if(tagUser == msg.tagUserSender){
                                Toast.makeText(
                                    this@ActivityMain,
                                    "Заявка на добавление отправлена",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (msg.substringBefore("::") == "DELETE"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ResultDeleteFrined>(jsonData)
                            sqliteHelper.deleteFriend(msg)
                            val ed = sp.edit()
                            ed.putString("changeStatus", LocalDateTime.now().toString())
                            if(msg.typeDelete == "DELFROMREQ"){
                                ed.putString("changeStatusReq", LocalDateTime.now().toString())
                            }
                            ed.apply()
                            if(tagUser == msg.tagUserOur){
                                Toast.makeText(
                                    this@ActivityMain,
                                    "Удалено",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (msg.substringBefore("::") == "CNFRMADD"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ResultCnfrmAddFriend>(jsonData)
                            sqliteHelper.updateStatusFriend(msg)
                            val ed = sp.edit()
                            ed.putString("changeStatusCNFRM", LocalDateTime.now().toString())
                            ed.putString("changeStatus", LocalDateTime.now().toString())
                            ed.apply()
                            if(tagUser == msg.tagUserOur){
                                Toast.makeText(
                                    this@ActivityMain,
                                    "Пользователь добавлен в список друзей",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (msg.substringBefore("::") == "FIND"){
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ResultFindUser>(jsonData)
                            val ed = sp.edit()
                            ed.putString("nameUserFind", msg.nameUserFriend)
                            ed.putString("tagUserFind", msg.tagUserFriend)
                            ed.putString("changeStatusFind", LocalDateTime.now().toString())
                            ed.apply()
                        }
                    }
                    if (status == "ERROR"){
                        if (msg.substringBefore("::") == "FIND"){
                            Toast.makeText(
                                this@ActivityMain,
                                "Данный пользователь не найден",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            "ONLINE" -> {
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                val name = idWithName.substringAfter("::")
                if (name != "UNNAMED" && id != sp.getString("tagUser", "NONE")) {
                    sqliteHelper.addUserInOnline(id, name)
                    sqliteHelper.updateNameInUserChat(id, name)
                    sqliteHelper.updateNameInFriends(id, name)
                }
            }
            "OFFLINE" -> {
                val idWithName = message.substringAfter("::")
                val id = idWithName.substringBefore("::")
                sqliteHelper.deleteUserFromOnline(id)
            }
            "MESSAGE_FROM" -> {
                if (sp.getBoolean("isAuth", false)) {
                    val jsonData = message.substringAfter("::")
                    messagePrint(jsonData)
                }
            }
        }
    }
    private fun messageToUser(jsonData: String){
        try{
            val msg = Json.decodeFromString<ConfirmInsertNewMsgDlg>(jsonData)
                sqliteHelper.addMsgInTable(
                    msg.dialog_id,
                    msg.sender,
                    msg.typeMsg,
                    msg.textMsg,
                    msg.timeCreated
                )
            if(!sp.getBoolean("active", false)) return
            if(sp.getString("idActive", "NONE") != msg.receiverId) return
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var newView = inflater.inflate(R.layout.message_to, null)
            when (msg.typeMsg) {
                "TEXT" -> {
                    val textInMessage = newView.findViewById<TextView>(R.id.msgTO)
                    textInMessage.text = msg.textMsg
                }
                "IMAGE" -> {
                    newView = inflater.inflate(R.layout.message_to_image, null)
                    val imageInMessage = newView.findViewById<ImageView>(R.id.msgToImage)
                    val nameImg = msg.textMsg
                    var chatName = msg.dialog_id.replace("#", "%23")
                    chatName = chatName.replace("::", "--")
                    val urlImg = "http://imagerc.ddns.net:80/userImgMsg/$chatName/$nameImg.jpg"
                    Picasso.get()
                        .load(urlImg)
                        .placeholder(R.drawable.error_image)
                        .into(imageInMessage)
                }
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            mainWindowOuter.addView(newView, lp)
            scrollView.post(Runnable() {
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
        } catch (ex: Exception){ }
    }
    private fun messagePrint(jsonData: String){
        try{
            var sender = "";
            val msg = Json.decodeFromString<MessageFromUser>(jsonData)
            val ed = sp.edit()
            if(msg.dialog_id.substringBefore("#") == "GROUP")
            { // для групп
                sender = msg.receiverId;
                if(msg.sender == sp.getString("tagUser", "NONE")) return // скип добавления в бд, если это отправили мы
                sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)
                ed.putString("changeChatList", LocalDateTime.now().toString())
                ed.apply()
                if(msg.receiverId != sp.getString("idActive", "NONE")) return // скип отрисовки, если мы не в чате
            }
            else
            { // для лс
                sender = msg.sender;
                sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)
                ed.putString("changeChatList", LocalDateTime.now().toString())
                ed.apply()
                if(msg.sender != sp.getString("idActive", "NONE")) return // скип отрисовки, если мы не в чате
            }
            if(!sp.getBoolean("active", false)) return
            var nameOfSender = sqliteHelper.getNameInUserChat(msg.sender)
            if (nameOfSender.isEmpty())
            {
                nameOfSender = resources.getString(R.string.user_name)
            }
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            var newView = inflater.inflate(R.layout.message_from, null)
            when (msg.typeMsg) {
                "IMAGE" -> {
                    newView = inflater.inflate(R.layout.message_from_image, null)
                    val nameImg = msg.textMsg
                    var chatName = msg.dialog_id.replace("#", "%23")
                    chatName = chatName.replace("::", "--")
                    val urlImg = "http://imagerc.ddns.net:80/userImgMsg/$chatName/$nameImg.jpg"
                    val imageInMessage = newView.findViewById<ImageView>(R.id.msgFromImage)
                    Picasso.get()
                        .load(urlImg)
                        .placeholder(R.drawable.error_image)
                        .into(imageInMessage)
                }
                "TEXT" -> {
                    newView.findViewById<TextView>(R.id.msgFrom).text = msg.textMsg
                }
            }
            newView.findViewById<TextView>(R.id.senderName).text = nameOfSender
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            mainWindowOuter.addView(newView, lp)
            scrollView.post(Runnable() {
                scrollView.fullScroll(View.FOCUS_DOWN)
            })
            if(webSocketClient.connection.isClosed){
                Toast.makeText(
                    this@ActivityMain, "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                val updateCountMsg =
                    UpdateCountMsg("UPDATE::", "COUNTMSG::", msg.dialog_id, sender, "1")
                val dataServerName = Json.encodeToString(updateCountMsg)
                webSocketClient.send(dataServerName)
            }
        } catch (ex: Exception){ }
    }

    private fun deviceAuth(data: String){
        try {
            val obj = Json.decodeFromString<DataUserDeviceAuth>(data)
            val ed = sp.edit()
            ed.putString("nickname", obj.nickname)
            ed.putString("tagUser", obj.tagUser)
            ed.putBoolean("isVisible", obj.isVisible)
            ed.putBoolean("isAvatar", obj.isAvatar)
            ed.putBoolean("isAuth", true)
            ed.apply()
            val confirmAuth = ConfirmAuth(
                "AUTH::",
                true,
                obj.nickname,
                obj.tagUser,
                obj.isVisible,
                getJwt(obj.tagUser))
            var msg = Json.encodeToString(confirmAuth)
            tagUser = sp.getString("tagUser", null)!!

            if(bubbleNav.currentActiveItemPosition in 0..2){
                loadFragment(bubbleNav.currentActiveItemPosition)
            } else{
             loadFragment(0)
            }

            if(!webSocketClient.connection.isClosed){
                webSocketClient.send(msg)
                val queryAllDlg = QueryAllDlg("DOWNLOAD::", "ALLDLG::", obj.tagUser, sp.getString("tokenQuery","")!!)
                msg = Json.encodeToString(queryAllDlg)
                webSocketClient.send(msg)
            }
        } catch (ex: Exception){
            Toast.makeText(
                this@ActivityMain,
                ex.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun authorization(data: String){
        try {
            val obj = Json.decodeFromString<DataOfUser>(data)
            val ed = sp.edit()
            ed.putString("nickname", obj.nickname)
            ed.putString("tagUser", obj.tagUser)
            ed.putBoolean("isVisible", obj.isVisible)
            ed.putBoolean("isAvatar", obj.isAvatar)
            ed.putString("token", obj.token)
            ed.putBoolean("isAuth", true)
            ed.apply()
            val confirmAuth = ConfirmAuth(
                "AUTH::",
                true,
                obj.nickname,
                obj.tagUser,
                obj.isVisible,
                getJwt(obj.tagUser))
            var msg = Json.encodeToString(confirmAuth)

            tagUser = sp.getString("tagUser", null)!!

            if(bubbleNav.currentActiveItemPosition in 0..2){
                loadFragment(bubbleNav.currentActiveItemPosition)
            } else{
                loadFragment(0)
            }

            if(!webSocketClient.connection.isClosed){
                webSocketClient.send(msg)
                val queryAllDlg = QueryAllDlg(
                    "DOWNLOAD::",
                    "ALLDLG::",
                    obj.tagUser,
                    sp.getString("tokenQuery","")!!
                )
                msg = Json.encodeToString(queryAllDlg)
                webSocketClient.send(msg)
            }
        } catch (ex: Exception){
            Toast.makeText(
                this@ActivityMain,
                ex.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun getJwt(tagUser : String) : String{
        val key: Key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        val time = LocalDateTime.now().toString()
        val platform = "Mobile"
        val jws = Jwts.builder()
                .setSubject(time)
                .setIssuer(tagUser)
                .setId(platform)
                .signWith(key)
                .compact()
        val ed = sp.edit()
        ed.putString("tokenQuery", jws)
        ed.apply()
        return jws
    }
    private fun checkVerifyJWT(token : String) : Boolean{
        return (sp.getString("tokenQuery","")!! == token)
    }
}


