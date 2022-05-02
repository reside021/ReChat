package com.example.myapplication.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import com.example.myapplication.ActivityMain
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.dataClasses.*
import com.example.myapplication.ui.AddFriendBottomDialog
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MyAdapterForGroupInfo(
    private val listUsers: MutableList<DataGroupDB>,
    private val rangAccess: Int,
    private val dialog_id: String):BaseAdapter() {

    override fun getCount(): Int {
        return listUsers.size
    }

    override fun getItem(position: Int): Any {
        return listUsers[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context = parent?.context!!
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val newView : View = layoutInflater.inflate(R.layout.group_info_element, parent, false)
        val userName = newView.findViewById<TextView>(R.id.userName)
        userName.text = listUsers[position].nickName
        val imageOfUser = newView.findViewById<ImageView>(R.id.avatarUser)
        val tagUser = listUsers[position].tagUser
        val idOfUser = newView.findViewById<TextView>(R.id.idUser)
        idOfUser.text = tagUser
        val urlAvatar = "http://imagerc.ddns.net:80/avatar/avatarImg/$tagUser.jpg"
        Picasso.get()
            .load(urlAvatar)
            .placeholder(R.drawable.user_profile_photo)
            .into(imageOfUser)
        val deleteUser = newView.findViewById<TextView>(R.id.deleteUserFromGroup)
        deleteUser.apply {
            if (rangAccess < 2) visibility = View.GONE
            setOnClickListener {
                val animAlpha = AnimationUtils.loadAnimation(context, R.anim.alpha)
                it.startAnimation(animAlpha)
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Удаление пользователя")
                    .setMessage("Вы уверены?")
                    .setCancelable(true)
                    .setPositiveButton("Да") { dialog, id ->
                        if (webSocketClient.connection.isClosed){
                            Toast.makeText(context, "Отсутствует подключение к серверу",
                                Toast.LENGTH_SHORT).show()
                        }
                        else
                        {
                            val deleteUserFromDlg = DeleteUserFromDlg(
                                "UPDATE::",
                                "DLTUSERDLG::",
                                dialog_id,
                                tagUser
                            )
                            val msg = Json.encodeToString(deleteUserFromDlg)
                            webSocketClient.send(msg)
                        }
                    }
                    .setNegativeButton("Нет"){ dialog, id -> }
                builder.create().show()
            }
        }
        val spinnerRangAccess = newView.findViewById<Spinner>(R.id.spinnerAccess)
        val adapterRang = ArrayAdapter.createFromResource(context, R.array.rangAccess,R.layout.spinner_element)
        adapterRang.setDropDownViewResource(R.layout.spinner_dropdown_item)
        val posElem = position
        var prevRang = listUsers[posElem].rang
        spinnerRangAccess.apply {
            adapter = adapterRang
            setSelection(prevRang)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (prevRang == position) {
                        return
                    }
                    if (rangAccess < 2){
                        setSelection(listUsers[posElem].rang)
                        return
                    }
                    if (rangAccess == 2 && position >= 2){
                        setSelection(listUsers[posElem].rang)
                        return
                    }
                    if(webSocketClient.connection.isClosed){
                        Toast.makeText(
                            parent?.context, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else{
                        prevRang = position
                        val updateRangUser =
                            UpdateRangUser(
                                "UPDATE::",
                                "RANGUSER::",
                                position,
                                dialog_id,
                                listUsers[posElem].tagUser)
                        val dataServerName = Json.encodeToString(updateRangUser)
                        webSocketClient.send(dataServerName)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }
        return newView
    }
}