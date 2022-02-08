package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
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
        const val VERSION_APP = "0.1"
        lateinit var webSocketClient : WebSocketClient
        lateinit var sqliteHelper: SqliteHelper
    }

    private lateinit var sp : SharedPreferences
    private lateinit var tagUser : String
    private lateinit var bubbleNav : BubbleNavigationLinearView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_nav_bar)


        sqliteHelper = SqliteHelper(this)
        supportActionBar?.title = resources.getString(R.string.rechat)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        bubbleNav = findViewById(R.id.bottom_navigation_view_linear)
        sqliteHelper.clearTable()

        bubbleNav.visibility = View.GONE
        loadFragment(FirstDisplayFragment.newInstance())
        supportActionBar?.title = resources.getString(R.string.rechat)

        isActVersion()

        val toolbar = supportActionBar

        bubbleNav.setNavigationChangeListener { view, position ->
            val fragment: Fragment
            when (position) {
                0 -> {
                    toolbar?.title = resources.getString(R.string.profile)
                    fragment = UserFragment()
                    loadFragment(fragment)
                }
                1 -> {
                    toolbar?.title = resources.getString(R.string.chat)
                    fragment = ChatFragment()
                    loadFragment(fragment)
                }
                2 -> {
                    toolbar?.title = resources.getString(R.string.people)
                    fragment = FriendsFragment()
                    loadFragment(fragment)
                }
            }
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


    private fun loadFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.host_fragment, fragment)
            .commit()
    }


    override fun onUserLoadView() {
        val userName = sp.getString("nickname", resources.getString(R.string.user_name))!!
        val isAvatar = sp.getBoolean("isAvatar", false)
        val urlAvatar = if(isAvatar){
            "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
        } else{
            ""
        }
        val isVisible = sp.getBoolean("isVisible", false)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
        fragment.setUserData(tagUser, userName, isAvatar, urlAvatar, isVisible)
    }
    override fun onChatLoadView() {
        val listUserChat = sqliteHelper.getAllUsersChat()
        val myAdapterForChat = MyAdapterForChat(listUserChat)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as ChatFragment
        fragment.setUserData(myAdapterForChat)
    }
    override fun onFriendsLoadView() {}


    override fun onFirstDisplayLoadView() {}
    override fun onAuthLoadView() {}
    override fun onFriendsListLoadView() {
        val myAdapterForFriends = MyAdapterForFriends(tagUser)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragmentListFriends) as FriendListFragment
        fragment.setUserData(myAdapterForFriends)
    }
    override fun onUserListLoadView() {
        val myAdapterForUsers = MyAdapterForUsers()
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragmentListFriends) as UserListFragment
        fragment.setUserData(myAdapterForUsers)
    }
    override fun onFrndListRequestLoadView() {
        val myAdapterForRequest = MyAdapterForRequest(tagUser)
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
                    if (webSocketClient.connection.readyState.ordinal != 0) {
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
                                        if(webSocketClient.connection.readyState.ordinal == 0){
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
            fragment.setNewUserName(userName)
        }
        if(key.equals("changeAvatar")){
            val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
            val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
            fragment.setNewUserImage(urlAvatar)
        }
    }

    private fun initWebSocket(){
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
                        bubbleNav.visibility = View.GONE
                        loadFragment(AuthFragment.newInstance())
                    } else{
                        val successAuthToken = SuccessAuthToken("AUTHTOKEN::", tokenAuth)
                        val msg = Json.encodeToString(successAuthToken)
                        if(webSocketClient.connection.readyState.ordinal != 0){
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
                        bubbleNav.visibility = View.GONE
                        loadFragment(AuthFragment.newInstance())
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
                            if (webSocketClient.connection.readyState.ordinal != 0) {
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
                            ed.apply()
                            val confirmVisible = ConfirmUpVisible("VISIBLE::", true, isVisible)
                            val msg = Json.encodeToString(confirmVisible)
                            if (webSocketClient.connection.readyState.ordinal != 0) {
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
                                    msg.enteredTime
                                )
                                Toast.makeText(
                                    this, "С пользователем создан чат",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                sqliteHelper.addUserInDLG(
                                    msg.dialog_id,
                                    msg.userManager,
                                    msg.enteredTime
                                )
                                val dialog_ids = sqliteHelper.getAllDlgFromDLG()
                                val queryAllTagName = QueryAllTagName(
                                    "DOWNLOAD::",
                                    "ALLTAGNAME::",
                                    dialog_ids
                                )
                                val dataServerName = Json.encodeToString(queryAllTagName)
                                webSocketClient.send(dataServerName)
                            }
                        }
                        if (msg.substringBefore("::") == "NEWMSGDLG") {
                            val jsonData = msg.substringAfter("::")
                            messageToUser(jsonData)
                        }
                    }
                    if (status == "ERROR") {
                        if (msg.substringBefore("::") == "NEWUSERDLG") {
                            Toast.makeText(
                                this, "Не удалось создать чат",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                if (typeOper == "DOWNLOAD") {
                    if (status == "SUCCESS") {
                        if (msg.substringBefore("::") == "ALLDLG") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfDlg>(jsonData)
                            val dataOfDialog: List<DataOfDialog> = msg.listOfData
                            for (el in dataOfDialog) {
                                sqliteHelper.addUserInDLG(
                                    el.dialog_id,
                                    el.tagUser,
                                    el.enteredTime
                                )
                            }
                            val dialog_ids = sqliteHelper.getAllDlgFromDLG()
                            val queryAllTagName = QueryAllTagName(
                                "DOWNLOAD::",
                                "ALLTAGNAME::",
                                dialog_ids
                            )
                            val dataServerName = Json.encodeToString(queryAllTagName)
                            webSocketClient.send(dataServerName)
                            val queryAllFriends = QueryAllFriends("DOWNLOAD::", "ALLFRND::")
                            val dataServerFriends = Json.encodeToString(queryAllFriends)
                            webSocketClient.send(dataServerFriends)
                            val queryAllMsg = QueryAllMsg("DOWNLOAD::", "ALLMSG::", dialog_ids)
                            val dataServerMsg = Json.encodeToString(queryAllMsg)
                            webSocketClient.send(dataServerMsg)
                        }
                        if (msg.substringBefore("::") == "ALLMSG") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfMsg>(jsonData)
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
                            val dataOfNickname: List<DataOfNickName> = msg.listOfData
                            for (el in dataOfNickname) {
                                if (sqliteHelper.checkUserInChat(el.tagUser)) break
                                sqliteHelper.addUserInChat(el.tagUser to el.nickUser)
                            }
                        }
                        if (msg.substringBefore("::") == "ALLFRND") {
                            val jsonData = msg.substringAfter("::")
                            val msg = Json.decodeFromString<ListDataOfFriends>(jsonData)
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
                            val msg = Json.decodeFromString<ResultCnfrmAddFriend>(jsonData)
                            sqliteHelper.deleteFriend(msg)
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
                            if(tagUser == msg.tagUserOur){
                                Toast.makeText(
                                    this@ActivityMain,
                                    "Пользователь добавлен в список друзей",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    if (status == "ERROR"){

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
                val name = idWithName.substringAfter("::")
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
        val msg = Json.decodeFromString<MessageFromUser>(jsonData)
        if(msg.dialog_id.substringBefore("#") == "GROUP"){
            if(msg.sender == sp.getString("tagUser", "NONE")) return
            sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)
            if(msg.receiverId != sp.getString("idActive", "NONE")) return
        }else{
            sqliteHelper.addMsgInTable(msg.dialog_id, msg.sender, msg.typeMsg, msg.textMsg, msg.timeCreated)
            if(sp.getString("idActive", "NONE") != msg.sender) return
        }
        if(!sp.getBoolean("active", false)) return
            var nameOfSender = sqliteHelper.getNameInUserChat(msg.sender)
            if (nameOfSender.isEmpty()){
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
        } catch (ex: Exception){
        }
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
                obj.isVisible)
            var msg = Json.encodeToString(confirmAuth)
            tagUser = sp.getString("tagUser", null)!!

            bubbleNav.visibility = View.VISIBLE
            loadFragment(UserFragment.newInstance())
            supportActionBar?.title = resources.getString(R.string.profile)

            if(webSocketClient.connection.readyState.ordinal != 0){
                webSocketClient.send(msg)
                val queryAllDlg = QueryAllDlg("DOWNLOAD::", "ALLDLG::", obj.tagUser)
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
                obj.isVisible)
            var msg = Json.encodeToString(confirmAuth)

            tagUser = sp.getString("tagUser", null)!!

            bubbleNav.visibility = View.VISIBLE
            loadFragment(UserFragment.newInstance())
            supportActionBar?.title = resources.getString(R.string.profile)
            if(webSocketClient.connection.readyState.ordinal != 0){
                webSocketClient.send(msg)
                val queryAllDlg = QueryAllDlg("DOWNLOAD::", "ALLDLG::", obj.tagUser)
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




}


