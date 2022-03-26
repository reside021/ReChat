package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.dataClasses.NewName
import com.example.myapplication.dataClasses.UpdateVisible
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class UserFragment : Fragment(){


    internal interface OnFragmentSendDataListener {
        fun onUserLoadView()
    }

    companion object{
        fun newInstance() = UserFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSendDataListener?.onUserLoadView()

        val userName = requireView().findViewById<TextView>(R.id.nameOfUser)
        userName.setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val inflater = this.layoutInflater
            val view  = inflater.inflate(R.layout.dialog_setname, null)
            val newNameUser = view.findViewById<EditText>(R.id.newnameuser).text
            builder.setView(view)
                .setPositiveButton("OK") { dialog, id ->
                    if(newNameUser.isEmpty()){
                        Toast.makeText(
                            activity, "Вы не задали нового имени!",
                            Toast.LENGTH_LONG
                        ).show()
                        return@setPositiveButton
                    }
                    if(webSocketClient.connection.isClosed){
                        Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    } else{
                        val dataUser = NewName("SETNAME::", false, newNameUser.toString())
                        val msg = Json.encodeToString(dataUser)
                        webSocketClient.send(msg)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss()
                }
            builder.show()
        }
        val tagUser = requireView().findViewById<TextView>(R.id.tagOfUser)
        tagUser?.setOnLongClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT,"${resources.getString(R.string.msg_share)} ${tagUser.text}")
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, resources.getString(R.string.share_to)))
            return@setOnLongClickListener true
        }
        val copyTagBtn = requireView().findViewById<ImageButton>(R.id.copyTagBtn)
        copyTagBtn.setOnClickListener {
            try {
                val clipboard: ClipboardManager? =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("TAG", tagUser.text)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    activity, resources.getString(R.string.copy_tag),
                    Toast.LENGTH_SHORT
                ).show()
            } catch (ex: Exception){}
        }
    }


    fun setUserData(tag : String, userName : String, isAvatar : Boolean,
                    urlAvatar : String, isVisible : Boolean){
        requireView().findViewById<TextView>(R.id.nameOfUser).text = userName
        requireView().findViewById<TextView>(R.id.tagOfUser).text = tag
        val switchBeOnline = requireView().findViewById<SwitchCompat>(R.id.switchBeOnline)
        switchBeOnline.isChecked = isVisible
        if(isAvatar){
            val imageOfUser = requireView().findViewById<ImageView>(R.id.imageOfUser)
            Picasso.get()
                    .load(urlAvatar)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .placeholder(R.drawable.user_profile_photo)
                    .into(imageOfUser)
        }
        switchBeOnline.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                    ).show()
                    switchBeOnline.isChecked = false
                    return@setOnCheckedChangeListener
                } else{
                    val dataUser = UpdateVisible("VISIBLE::", false, isChecked)
                    val msg = Json.encodeToString(dataUser)
                    webSocketClient.send(msg)
                }
            }else{
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                    ).show()
                    switchBeOnline.isChecked = true
                    return@setOnCheckedChangeListener
                } else{
                    val dataUser = UpdateVisible("VISIBLE::", false, isChecked)
                    val msg = Json.encodeToString(dataUser)
                    webSocketClient.send(msg)
                }
            }
        }
    }

    fun setNewUserImage(urlAvatar: String){
        val imageOfUser = requireView().findViewById<ImageView>(R.id.imageOfUser)
        Picasso.get()
                .load(urlAvatar)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .placeholder(R.drawable.user_profile_photo)
                .into(imageOfUser)
    }

    fun setNewUserName(newName : String){
        requireView().findViewById<TextView>(R.id.nameOfUser).text = newName
    }

    fun setNewVisible(newVisible : Boolean){
        requireView().findViewById<SwitchCompat>(R.id.switchBeOnline).isChecked = newVisible
    }
}
