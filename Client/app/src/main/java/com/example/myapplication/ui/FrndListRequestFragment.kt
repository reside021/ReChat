package com.example.myapplication.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapplication.FriendsProfile
import com.example.myapplication.R
import com.example.myapplication.adapters.MyAdapterForRequest

class FrndListRequestFragment() : Fragment(),
    SharedPreferences.OnSharedPreferenceChangeListener{
    internal interface OnFragmentSendDataListener {
        fun onFrndListRequestLoadView()
    }

    companion object{
        fun newInstance() = FrndListRequestFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null
    private lateinit var sp : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_request_friends, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sp = requireActivity().getSharedPreferences("OURINFO", Context.MODE_PRIVATE)
        sp.registerOnSharedPreferenceChangeListener(this)
        val listViewFriends = requireView().findViewById<ListView>(R.id.listViewFrndRequest)
        listViewFriends.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(activity, FriendsProfile::class.java);
            val tagUser = view.findViewById<TextView>(R.id.idUser)
            val nameOfUser = view.findViewById<TextView>(R.id.userName)
            intent.putExtra("idTag", tagUser.text)
            intent.putExtra("nameOfUser", nameOfUser.text)
            startActivity(intent)
        }
    }
    fun setUserData(myAdapterForRequest: MyAdapterForRequest){
        val listViewFrndRequest = requireView().findViewById<ListView>(R.id.listViewFrndRequest)
        listViewFrndRequest.adapter = myAdapterForRequest
    }

    override fun onStart() {
        super.onStart()
        fragmentSendDataListener?.onFrndListRequestLoadView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(this.isVisible){
            if(key.equals("changeStatusReq") || key.equals("changeStatusCNFRM")){
                fragmentSendDataListener?.onFrndListRequestLoadView()
            }
        }
    }
}