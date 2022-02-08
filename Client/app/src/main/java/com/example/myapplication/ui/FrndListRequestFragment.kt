package com.example.myapplication.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.adapters.MyAdapterForRequest

class FrndListRequestFragment() : Fragment() {
    internal interface OnFragmentSendDataListener {
        fun onFrndListRequestLoadView()
    }

    companion object{
        fun newInstance() = FrndListRequestFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

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
        fragmentSendDataListener?.onFrndListRequestLoadView()
    }
    fun setUserData(myAdapterForRequest: MyAdapterForRequest){
        val listViewFrndRequest = requireView().findViewById<ListView>(R.id.listViewFrndRequest)
        listViewFrndRequest.adapter = myAdapterForRequest
        listViewFrndRequest.setOnItemClickListener { parent, view, position, id ->
        }
    }
}