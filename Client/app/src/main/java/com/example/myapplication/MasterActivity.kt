package com.example.myapplication


import android.R.id
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.squareup.picasso.Picasso
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.java_websocket.client.WebSocketClient
import retrofit2.*
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File


class MasterActivity : AppCompatActivity() {
    companion object {
        const val IMAGE_REQUEST = 1
        private var OTHER_MSG = 0
    }
    private lateinit var profileTab : TextView
    private lateinit var chatTab : TextView
    private lateinit var friendsTab : TextView
    private lateinit var parentLinearLayout: LinearLayout
    private lateinit var view : View
    private lateinit var myAdapterForFriends : MyAdapterForFriends
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var sp : SharedPreferences
    private lateinit var webSocketClient : WebSocketClient
    private lateinit var tagUser : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.master)
        val actionBar = this.supportActionBar
        actionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setCustomView(R.layout.actionbar)

        view = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.clearlayout, null)
        parentLinearLayout = findViewById(R.id.masterLayout)

        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("isAuth", true)
        ed.apply()
        tagUser = sp.getString("tagUser", null)!!
        sqliteHelper = MainActivity.sqliteHelper
        webSocketClient = MainActivity.webSocketClient
//        sqliteHelper.addUserInChat(Pair("0","Global Chat"))

        profileTab = findViewById(R.id.profile)
        chatTab = findViewById(R.id.chat)
        friendsTab = findViewById(R.id.friends)
        profileTab.setOnClickListener { profileTabActive() }
        chatTab.setOnClickListener { chatTabActive() }
        friendsTab.setOnClickListener { friendsTabActive() }
    }


    override fun onStart() {
        super.onStart()
        profileTabActive()
    }

    override fun onDestroy() {
        super.onDestroy()
        sqliteHelper.clearTable()
    }

    private fun profileTabActive(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.profile, null)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLinearLayout.addView(view, lp)
        profileTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        chatTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)
        val nickname = sp.getString("nickname", resources.getString(R.string.user_name))
        view.findViewById<TextView>(R.id.nameofuser).text = nickname
        view.findViewById<TextView>(R.id.tagofuser).text = tagUser

        if(sp.getBoolean("isAvatar", false)){
            val imageofUser = view.findViewById<ImageView>(R.id.imageofuser)
            val tag = sp.getString("tagUser", "")
            val url = "http://imagerc.ddns.net:80/avatarImg/$tag.jpg"
            Picasso.get()
                    .load(url)
                    .into(imageofUser)
        }



        val switchBeOnline = view.findViewById<SwitchCompat>(R.id.switchBeOnline)
        switchBeOnline.isChecked = sp.getBoolean("isVisible", false)
        switchBeOnline.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(webSocketClient.connection.readyState.ordinal == 0){
                    Toast.makeText(
                        this, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                    switchBeOnline.isChecked = false
                    return@setOnCheckedChangeListener
                }
                val dataUser = UpdateVisible("VISIBLE::", false, isChecked)
                val msg = Json.encodeToString(dataUser)
                webSocketClient.send(msg)
            }else{
                if(webSocketClient.connection.readyState.ordinal == 0){
                    Toast.makeText(
                        this, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                    switchBeOnline.isChecked = true
                    return@setOnCheckedChangeListener
                }
                val dataUser = UpdateVisible("VISIBLE::", false, isChecked)
                val msg = Json.encodeToString(dataUser)
                webSocketClient.send(msg)
            }
        }
    }

    private fun chatTabActive(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.chat, null)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLinearLayout.addView(view, lp)
        chatTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)

        val listUserChat = sqliteHelper.getAllUsersChat()
        val myAdapterForChat = MyAdapterForChat(listUserChat)
        val listViewChat = findViewById<ListView>(R.id.listViewChat)
        listViewChat.adapter = myAdapterForChat
        listViewChat.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, ChatPeople::class.java);
            val idUser = view.findViewById<TextView>(R.id.idUser)
            intent.putExtra("idTag", idUser.text)
            startActivity(intent)
        }
        listViewChat.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val builder = AlertDialog.Builder(this)
            val dialogInflater = this.layoutInflater
            val dialogView  = dialogInflater.inflate(R.layout.dialog_actions_with_chatchannel, null)
            val idUser = view.findViewById<TextView>(R.id.idUser)
            builder.setView(dialogView)
            val alertDialog = builder.create();
            alertDialog.show()
            dialogView.findViewById<Button>(R.id.delThisChat).setOnClickListener(){
                sqliteHelper.deleteUserChat(idUser.text.toString())
                alertDialog.dismiss()
            }
            return@OnItemLongClickListener true
        }
    }
    private fun friendsTabActive(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.friends, null)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLinearLayout.addView(view, lp)
        friendsTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        chatTab.setBackgroundColor(0)

        myAdapterForFriends = MyAdapterForFriends()
        val listViewFriends = findViewById<ListView>(R.id.listViewFriends)
        listViewFriends.adapter = myAdapterForFriends
    }

    fun onNameClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_setname, null);
        val newNameUser = view.findViewById<EditText>(R.id.newnameuser).text
        builder.setView(view)
                .setPositiveButton("OK") { dialog, id ->
                    if(newNameUser.isEmpty()){
                        Toast.makeText(
                            this, "Вы не задали нового имени!",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setPositiveButton
                    }
                    if(webSocketClient.connection.readyState.ordinal == 0){
                        Toast.makeText(
                            this, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    }
                    val dataUser = NewName("SETNAME::", false, newNameUser.toString())
                    val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                        dialog.dismiss()

                }
                .setNegativeButton("Отмена") { dialog, id -> dialog.dismiss()
                }
        builder.show()
    }

    @Serializable
    data class NewName(
        val type: String,
        val confirmSetname: Boolean,
        val newUserName: String
    )
    @Serializable
    data class UpdateVisible(
        val type: String,
        val confirmUpVisible: Boolean,
        val isVisible: Boolean
    )
    @Serializable
    data class SuccessSetAvatar(
            val type : String,
            val successSet : Boolean
    )
    fun changePhotoClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_change_photo, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<TextView>(R.id.changeAvatar).setOnClickListener(){
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 1 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(IMAGE_REQUEST)
            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            if(requestCode == IMAGE_REQUEST){
                if(resultCode == Activity.RESULT_OK){

                    val uri: Uri = data?.data!!
                    val imageUser = findViewById<ImageView>(R.id.imageofuser)
                    imageUser.setImageURI(uri)
//                    val bitmap : Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
//                    } else {
//                        MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                    }
//
//                    imageUser.setImageBitmap(bitmap)
                    val retrofit = Retrofit.Builder()
                        .baseUrl("http://imagerc.ddns.net:80/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build()
                    val service = retrofit.create(UploadAvatar::class.java)
                    val addressImg = uri.toString().substringAfter("//")
                    val file = File(addressImg)
                    val requestFile: RequestBody =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file)
                    val id = sp.getString("tagUser", "")
                    val body =
                        MultipartBody.Part.createFormData("image", file.getName(), requestFile)
                    val response : Call<String> = service.updateProfile(id,body)
                    response.enqueue(object : Callback<String>{
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if(response.isSuccessful){
                                if(response.code() == 200){
                                    val querySetAvatar = SuccessSetAvatar("SETAVATAR::", true)
                                    val dataServerName = Json.encodeToString(querySetAvatar)
                                    webSocketClient.send(dataServerName)
                                }
                            }else{
                                Toast.makeText(this@MasterActivity, "Ошибка смены изображения", Toast.LENGTH_LONG).show()
                            }

                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Toast.makeText(this@MasterActivity, "Ошибка смены изображения", Toast.LENGTH_LONG).show()
                        }
                    }
                    )


//                    val imageUri = data?.data as Uri
//                    val imageBitmap: Bitmap
//                    imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))
//                    } else {
//                        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                    }
//                    findViewById<ImageView>(R.id.imageofuser).setImageBitmap(imageBitmap)
                } else if(resultCode == ImagePicker.RESULT_ERROR){
                    Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else{
                    Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            } else{
                super.onActivityResult(requestCode, resultCode, data)
            }
        } catch (ex: Exception){
            val kek = ex.message;
        }

    }
}