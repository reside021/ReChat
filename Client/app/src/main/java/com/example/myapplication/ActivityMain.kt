package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.ui.ChatFragment
import com.example.myapplication.ui.FriendsFragment
import com.example.myapplication.ui.UserFragment
import com.gauravk.bubblenavigation.BubbleNavigationLinearView
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.java_websocket.client.WebSocketClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File

class ActivityMain :
        AppCompatActivity(),
        UserFragment.OnFragmentSendDataListener,
        ChatFragment.OnFragmentSendDataListener,
        FriendsFragment.OnFragmentSendDataListener{

    @Serializable
    data class NewName(
            val type: String,
            val confirmSetname: Boolean,
            val newUserName: String
    )
    @Serializable
    data class SuccessSetAvatar(
            val type : String,
            val successSet : Boolean
    )

    companion object {
        const val IMAGE_REQUEST = 1
    }

    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var sp : SharedPreferences
    private lateinit var tagUser : String
    private lateinit var webSocketClient : WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bottom_nav_bar)

        sp = getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        val ed = sp.edit()
        ed.putBoolean("isAuth", true)
        ed.apply()
        tagUser = sp.getString("tagUser", null)!!
        sqliteHelper = MainActivity.sqliteHelper
        webSocketClient = MainActivity.webSocketClient

        val bubbleNav = findViewById<BubbleNavigationLinearView>(R.id.bottom_navigation_view_linear)
        loadFragment(UserFragment.newInstance())
        bubbleNav.setNavigationChangeListener { view, position ->
            val fragment: Fragment
            when (position) {
                0 -> {
                    fragment = UserFragment()
                    loadFragment(fragment)
                }
                1 -> {
                    fragment = ChatFragment()
                    loadFragment(fragment)
                }
                2 -> {
                    fragment = FriendsFragment()
                    loadFragment(fragment)
                }
            }
        }
    }

    private fun loadFragment(fragment : Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.host_fragment, fragment)
            .commit()
    }

    override fun onSendData(data: String?) {
        TODO("Not yet implemented")
    }

    override fun onUserLoadView() {
        val userName = sp.getString("nickname", resources.getString(R.string.user_name))!!
        val isAvatar = sp.getBoolean("isAvatar", false)
        val urlAvatar = if(isAvatar){
            "http://imagerc.ddns.net:80/avatarImg/$tagUser.jpg"
        } else{
            ""
        }
        val isVisible = sp.getBoolean("isVisible", false)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
        fragment.setUserData(tagUser, userName, isAvatar, urlAvatar, isVisible,  sp)
    }

    override fun onNewUserImage() {
        val isAvatar = sp.getBoolean("isAvatar", false)
        val urlAvatar = if(isAvatar){
            "http://imagerc.ddns.net:80/avatarImg/$tagUser.jpg"
        } else{
            ""
        }
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as UserFragment
        fragment.setNewUserImage(urlAvatar, isAvatar)
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
//                    val imageUser = findViewById<ImageView>(R.id.imageofuser)
//                    imageUser.setImageURI(uri)
                    val retrofit = Retrofit.Builder()
                            .baseUrl("http://imagerc.ddns.net:80/")
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
                                        val querySetAvatar = SuccessSetAvatar("SETAVATAR::", true)
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

    override fun onChatLoadView() {
        val listUserChat = sqliteHelper.getAllUsersChat()
        val myAdapterForChat = MyAdapterForChat(listUserChat)
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as ChatFragment
        fragment.setUserData(myAdapterForChat)
    }

    override fun onFriendsLoadView() {
        val myAdapterForFriends = MyAdapterForFriends()
        val fragment = supportFragmentManager.findFragmentById(R.id.host_fragment) as FriendsFragment
        fragment.setUserData(myAdapterForFriends)
    }

}


