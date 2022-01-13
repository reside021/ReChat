package com.example.myapplication.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.MainActivity.Companion.webSocketClient
import com.example.myapplication.MasterActivity
import com.example.myapplication.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class UserFragment : Fragment(){

    @Serializable
    data class UpdateVisible(
            val type: String,
            val confirmUpVisible: Boolean,
            val isVisible: Boolean
    )

    internal interface OnFragmentSendDataListener {
        fun onSendData(data: String?)
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
    }


    fun setUserData(tag : String, userName : String, isAvatar : Boolean,
                    urlAvatar : String, isVisible : Boolean){
        requireView().findViewById<TextView>(R.id.nameofuser).text = userName
        requireView().findViewById<TextView>(R.id.tagofuser).text = tag
        val switchBeOnline = requireView().findViewById<SwitchCompat>(R.id.switchBeOnline)
        switchBeOnline.isChecked = isVisible
        if(isAvatar){
            val imageOfUser = requireView().findViewById<ImageView>(R.id.imageofuser)
            Picasso.get()
                    .load(urlAvatar)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                    .into(imageOfUser)
        }
        switchBeOnline.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(webSocketClient.connection.readyState.ordinal == 0){
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
                if(webSocketClient.connection.readyState.ordinal == 0){
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
        val imageOfUser = requireView().findViewById<ImageView>(R.id.imageofuser)
        imageOfUser.setImageDrawable(null)
        Picasso.get()
                .load(urlAvatar)
                .placeholder(R.drawable.user_profile_photo)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                .into(imageOfUser)
    }

    fun setNewUserName(newName : String){
        requireView().findViewById<TextView>(R.id.nameofuser).text = newName
    }

}
