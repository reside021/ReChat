package com.example.myapplication.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.example.myapplication.ChatPeople
import com.example.myapplication.MainActivity.Companion.sqliteHelper
import com.example.myapplication.MyAdapterForChat
import com.example.myapplication.R
import java.lang.Exception


class ChatFragment : Fragment() {

    internal interface OnFragmentSendDataListener {
        fun onChatLoadView()
    }

    companion object{
        fun newInstance() = ChatFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
            return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSendDataListener?.onChatLoadView()
    }

    fun setUserData(myAdapterForChat: MyAdapterForChat){
        val listViewChat = requireView().findViewById<ListView>(R.id.listViewChat)
        listViewChat.adapter = myAdapterForChat
        listViewChat.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(activity, ChatPeople::class.java);
            val idUser = view.findViewById<TextView>(R.id.idUser)
            intent.putExtra("idTag", idUser.text)
            startActivity(intent)
        }
        listViewChat.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, view, position, id ->
            val builder = AlertDialog.Builder(activity)
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
}