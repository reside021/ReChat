package com.example.myapplication


import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import java.lang.Exception

class MasterActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_TAKE_PHOTO = 0
        const val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
        const val GALLERY_REQUEST = 1
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

        sqliteHelper = MainActivity.sqliteHelper

        sqliteHelper.addUserInChat(Pair("0","Global Chat"))

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

    private fun profileTabActive(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.profile, null)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        profileTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        chatTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)
        view.findViewById<TextView>(R.id.nameofuser).text =
                sp.getString("nickname", resources.getString(R.string.user_name))
        view.findViewById<TextView>(R.id.tagofuser).text =
                sp.getString("tagUser", null)

        val switchBeOnline = view.findViewById<SwitchCompat>(R.id.switchBeOnline)
        switchBeOnline.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                Toast.makeText(this, "Виден всем",
                        Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Виден только друзьям",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun chatTabActive(){
        parentLinearLayout.removeView(view)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.chat, null)
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        chatTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        friendsTab.setBackgroundColor(0)

        val listUserChat = sqliteHelper.getAllUsersChat()
        val myAdapterForChat = MyAdapterForChat(listUserChat)
        val listViewChat = findViewById<ListView>(R.id.listViewChat)
        listViewChat.adapter = myAdapterForChat
        listViewChat.onItemClickListener = AdapterView.OnItemClickListener {
            parent, view, position, id ->
            val intent = Intent(this, ChatPeople::class.java);
            val idUser = view.findViewById<TextView>(R.id.idUser)
            intent.putExtra("idTag", idUser.text)
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
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        parentLinearLayout.addView(view, lp)
        friendsTab.background = ContextCompat.getDrawable(this, R.drawable.bottom_line)
        profileTab.setBackgroundColor(0)
        chatTab.setBackgroundColor(0)

        myAdapterForFriends = MyAdapterForFriends()
        val listViewFriends = findViewById<ListView>(R.id.listViewFriends)
        listViewFriends.adapter = myAdapterForFriends
    }

    fun onNameClick(view: View) {
//        setName()
        Toast.makeText(this, "Твое имя", Toast.LENGTH_SHORT).show()
    }

//    fun setName(){
//        val builder = AlertDialog.Builder(this)
//        val inflater = this.layoutInflater
//        val view  = inflater.inflate(R.layout.dialog_setname, null);
//        val nameuser = view.findViewById<EditText>(R.id.nameuser)
//        builder.setView(view)
//                .setPositiveButton("OK") { dialog, id ->
//                    setNameSuccess(nameuser.text.toString()
//                    )
//                }
//                .setNegativeButton("Отмена") {
//                    dialog, id -> dialog.dismiss();
//                    if(findViewById<TextView>(R.id.nameofuser).text ==
//                            resources.getString(R.string.user_name)) {
//                        Toast.makeText(this, "Перед началом работы необходимо задать имя!",
//                                Toast.LENGTH_LONG).show()
//                        setName()
//                    }
//                }
//        builder.show()
//    }
//
//    private fun setNameSuccess(name : String){
//        if(name.isEmpty()){
//            Toast.makeText(this, "Перед началом работы необходимо задать имя!",
//                    Toast.LENGTH_LONG).show()
//            setName()
//            return
//        } else if(name.length > 10){
//            Toast.makeText(this, "Имя короче 10 символов",
//                    Toast.LENGTH_LONG).show()
//            setName()
//            return
//        }
//        try {
//            this@MasterActivity.runOnUiThread {
//                val nameofuser = findViewById<TextView>(R.id.nameofuser)
//                testact.webSocketClient.send("SET_NAME::$name")
//                nameofuser.text = name;
//                Toast.makeText(this, "Имя успешно изменено",
//                        Toast.LENGTH_SHORT).show()
//            }
//
//        } catch (ex : Exception){
//            Toast.makeText(this, "Ошибка изменения имени",
//                    Toast.LENGTH_LONG).show()
//        }
//    }
    fun changePhotoClick(view: View) {
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view  = inflater.inflate(R.layout.dialog_change_photo, null)
        builder.setView(view)
        val alertDialog = builder.create();
        alertDialog.show()
        view.findViewById<TextView>(R.id.changeAvatar).setOnClickListener(){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, GALLERY_REQUEST)
            }
            alertDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try{
            if(requestCode === GALLERY_REQUEST){
                if(resultCode === Activity.RESULT_OK){

                    val imageUri = data?.data as Uri
                    val imageBitmap: Bitmap
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
        }

    }
}