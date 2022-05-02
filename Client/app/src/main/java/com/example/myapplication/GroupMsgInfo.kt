package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.adapters.MyAdapterForGroupInfo
import com.example.myapplication.dataClasses.DataAllGroupInfo
import com.example.myapplication.dataClasses.UpdateTitleDialog
import com.example.myapplication.interfaces.DeleteAvatar
import com.example.myapplication.interfaces.UploadAvatar
import com.example.myapplication.ui.AddFriendBottomDialog
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
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


class GroupMsgInfo:
    AppCompatActivity(),
    TextView.OnEditorActionListener,
    SharedPreferences.OnSharedPreferenceChangeListener{

    companion object{
        const val OUTCHAT = 1
        const val DELETECHAT = 2
    }

    private lateinit var idUser : String
    private lateinit var nameOfUser: String
    private lateinit var sp: SharedPreferences
    private lateinit var titleDialog: EditText
    private lateinit var avatarDialog: ImageView
    private lateinit var urlAvatar: String
    private var rangAccess: Int = 1
    private lateinit var loadingData: LinearLayout
    private lateinit var listUsers: LinearLayout
    private lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_about_msg)
        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        idUser = intent.extras?.getString("idTag")!!
        nameOfUser = intent.extras?.getString("nameOfUser")!!
        rangAccess = intent.extras?.getInt("rangAccess")!!

        loadingData = findViewById(R.id.loadingData)
        listUsers = findViewById(R.id.listUsers)
        listView = listUsers.findViewById(R.id.listViewInfoGroup)
        listView.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, FriendsProfile::class.java);
            val tagUser = view.findViewById<TextView>(R.id.idUser)
            val nameOfUser = view.findViewById<TextView>(R.id.userName)
            intent.putExtra("idTag", tagUser.text)
            intent.putExtra("nameOfUser", nameOfUser.text)
            startActivity(intent)
        }



        sp.edit().putString("queryImg", LocalDateTime.now().toString()).apply()

        titleDialog = findViewById(R.id.titleDialog)
        titleDialog.setText(nameOfUser)
        titleDialog.setOnEditorActionListener(this)

        avatarDialog = findViewById(R.id.avatarDialog)
        avatarDialog.setOnClickListener(avatarDialogClick())
        val queryImg = sp.getString("queryImg","0")
        urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$idUser.jpg?time=$queryImg"
        Picasso.get()
            .load(urlAvatar)
            .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
            .placeholder(R.drawable.user_photo)
            .into(avatarDialog)

        supportActionBar?.apply {
            title = resources.getString(R.string.chat)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        findViewById<Button>(R.id.logOutGroup).setOnClickListener(logOutClick())
        val deleteGroup = findViewById<Button>(R.id.deleteGroup)
        deleteGroup.apply {
            visibility = View.VISIBLE
            setOnClickListener(deleteClick())
        }
        findViewById<Button>(R.id.addUserInGroup).setOnClickListener(addUserInGroupClick())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onStart() {
        super.onStart()
        if(webSocketClient.connection.isClosed){
            Toast.makeText(
                this, "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT
            ).show()
        }
        else{
            val getAllDataGroup =
                DataAllGroupInfo(
                    "DOWNLOAD::",
                    "TAGUSERSGROUP::",
                    idUser)
            val dataServerName = Json.encodeToString(getAllDataGroup)
            webSocketClient.send(dataServerName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sqliteHelper.clearDataGroupInfo()
        sp.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun addUserInGroupClick() = View.OnClickListener{
        if (rangAccess == 0)
        {
            Toast.makeText(this, "Нет доступа",
                Toast.LENGTH_SHORT).show()
            return@OnClickListener
        }
        val ourTag = sp.getString("tagUser", "")!!
        val addFriendBottomDialog = AddFriendBottomDialog(ourTag, idUser, sp)
        addFriendBottomDialog.show(supportFragmentManager, "AddUserInGroup")
    }
    private fun deleteClick() = View.OnClickListener{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Удаление чата")
            .setMessage("Вы уверены?")
            .setCancelable(true)
            .setPositiveButton("Да") { dialog, id ->
                if (rangAccess > 2){
                    setResult(DELETECHAT)
                    onBackPressed()
                }
            }
            .setNegativeButton("Нет"){ dialog, id -> }
        builder.create().show()
    }

    private fun logOutClick() = View.OnClickListener{
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выход из чата")
            .setMessage("Вы уверены?")
            .setCancelable(true)
            .setPositiveButton("Да") { dialog, id ->
                setResult(OUTCHAT)
                onBackPressed()
            }
            .setNegativeButton("Нет"){ dialog, id -> }
        builder.create().show()
    }

    private fun avatarDialogClick() = View.OnClickListener {
        if (rangAccess == 0) return@OnClickListener
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
                .createIntent {
                    startForAvatarDialogResult.launch(it)
                }
            alertDialog.dismiss()
        }
        view.findViewById<TextView>(R.id.deleteAvatar).setOnClickListener {
            try {
                if (!webSocketClient.connection.isClosed) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://imagerc.ddns.net:80/avatar/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build()
                    val service = retrofit.create(DeleteAvatar::class.java)
                    val response: Call<String> = service.deleteProfile(idUser)
                    response.enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                if (response.code() == 200) {
                                    Picasso.get()
                                        .load(urlAvatar)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                        .placeholder(R.drawable.user_photo)
                                        .into(avatarDialog)
                                    alertDialog.dismiss()
                                    Toast.makeText(this@GroupMsgInfo,
                                        "Изображение удалено",
                                        Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this@GroupMsgInfo,
                                    "Ошибка смены изображения",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(this@GroupMsgInfo,
                                "Ошибка смены изображения",
                                Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this@GroupMsgInfo,
                        "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    this@GroupMsgInfo, ex.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        when (actionId) {
            EditorInfo.IME_ACTION_SEND -> {
                if (titleDialog.text.isNotBlank()){
                    titleDialog.clearFocus()
                    val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(titleDialog.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                    if(rangAccess > 1){
                        if(webSocketClient.connection.isClosed){
                            Toast.makeText(
                                this, "Отсутствует подключение к серверу",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else{
                            val updateTitleDialog =
                                UpdateTitleDialog(
                                    "UPDATE::",
                                    "TITLEDIALOG::",
                                    titleDialog.text.toString(),
                                    idUser)
                            val dataServerName = Json.encodeToString(updateTitleDialog)
                            webSocketClient.send(dataServerName)
                        }
                        return true
                    }else{
                        Toast.makeText(
                            this, "Недостаточно прав для этого действия",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
        return false
    }


    private val startForAvatarDialogResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            try
            {
                when (it.resultCode) {
                    Activity.RESULT_OK -> {
                        val uri: Uri = it.data?.data!!
                        val retrofit = Retrofit.Builder()
                            .baseUrl("http://imagerc.ddns.net:80/avatar/")
                            .addConverterFactory(ScalarsConverterFactory.create())
                            .build()
                        val service = retrofit.create(UploadAvatar::class.java)
                        val addressImg = uri.toString().substringAfter("//")
                        val file = File(addressImg)
                        val requestFile: RequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                        val body = MultipartBody.Part.createFormData("image", file.getName(), requestFile)
                        val response : Call<String> = service.updateProfile(idUser,body)
                        response.enqueue(object : Callback<String> {
                            override fun onResponse(call: Call<String>, response: Response<String>) {
                                if(response.isSuccessful){
                                    if(response.code() == 200){
                                        Toast.makeText(this@GroupMsgInfo, "Изображение установлено", Toast.LENGTH_SHORT).show()
                                        Picasso.get()
                                            .load(urlAvatar)
                                            .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                                            .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                                            .placeholder(R.drawable.user_photo)
                                            .into(avatarDialog)
                                    }
                                }else{
                                    Toast.makeText(this@GroupMsgInfo, "Ошибка смены изображения", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<String>, t: Throwable) {
                                Toast.makeText(this@GroupMsgInfo, "Ошибка смены изображения", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(this, ImagePicker.getError(it.data), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            catch (ex: Exception)
            {

            }
        }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("changeDataGroupInfo")){
            val allUserInfo = sqliteHelper.getDataGroupInfo()
            if (allUserInfo.isEmpty())
            {
                loadingData.visibility = View.GONE
                listUsers.visibility = View.VISIBLE
            }
            else
            {
                val myAdapterForGroupInfo = MyAdapterForGroupInfo(allUserInfo, rangAccess, idUser)
                listView.adapter = myAdapterForGroupInfo
                loadingData.visibility = View.GONE
                setListViewHeightBasedOnChildren(listView)
                listUsers.visibility = View.VISIBLE
            }
        }
        if (key.equals("changeAddFriendBottomDialog")){
            onBackPressed()
        }
    }


    private fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        val desiredWidth = MeasureSpec.makeMeasureSpec(listView.width, MeasureSpec.UNSPECIFIED)
        var totalHeight = 0
        var view: View? = null
        for (i in 0 until listAdapter.count) {
            view = listAdapter.getView(i, view, listView)
            if (i == 0) view.layoutParams =
                ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED)
            totalHeight += view.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + listView.dividerHeight * listAdapter.count
        listView.layoutParams = params
        listView.requestLayout()
    }

}