package com.example.myapplication.ui

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.example.myapplication.ActivityMain
import com.example.myapplication.ActivityMain.Companion.sqliteHelper
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.adapters.MyAdapterForCrtDlg
import com.example.myapplication.dataClasses.AddUserDLGTable
import com.example.myapplication.dataClasses.NewUserDLGTable
import com.example.myapplication.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime


class AddFriendBottomDialog(private val ourTag: String, val dialog_id: String, val sp: SharedPreferences): BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLayoutBinding
    private lateinit var myAdapterForCrtDlg: MyAdapterForCrtDlg
    private lateinit var listUserForDlg : MutableList<String>

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetLayoutBinding.bind(inflater.inflate(R.layout.bottom_sheet_layout, container))
        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        sp.edit().putString("changeAddFriendBottomDialog", LocalDateTime.now().toString()).apply()
    }

    override fun onStart() {
        super.onStart()
        listUserForDlg = mutableListOf()
        val dataForAdapter = sqliteHelper.getAllRemainFriends(ourTag).toList()
        myAdapterForCrtDlg = MyAdapterForCrtDlg(dataForAdapter)
        binding.apply {
            listViewAddUser.adapter = myAdapterForCrtDlg
            listViewAddUser.onItemClickListener = itemListClick
            searchField.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
                override fun onQueryTextChange(newText: String?): Boolean {
                    myAdapterForCrtDlg.filter.filter(newText)
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
        }

        val density = requireContext().resources.displayMetrics.density

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.state = BottomSheetBehavior.STATE_DRAGGING

            val coordinator = (it as BottomSheetDialog).findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
            val containerLayout = it.findViewById<FrameLayout>(com.google.android.material.R.id.container)

            val buttons = it.layoutInflater.inflate(R.layout.button, null)
            buttons.findViewById<Button>(R.id.button_addUser).setOnClickListener {
                if (listUserForDlg.size == 0) return@setOnClickListener
                addUserInGroupClick()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            buttons.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = (60 * density).toInt()
                gravity = Gravity.BOTTOM
            }
            containerLayout?.addView(buttons)

            buttons.post {
                (coordinator?.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    buttons.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    )
                    // Устраняем разрыв между кнопкой и скролящейся частью
                    this.bottomMargin = (buttons.measuredHeight - 8 * density).toInt()
                    containerLayout?.requestLayout()
                }
            }
        }
    }

    private val itemListClick = AdapterView.OnItemClickListener { parent, view, position, id ->
        val checkBox = view.findViewById<CheckBox>(R.id.checkedForDlg)
        checkBox.isChecked = !checkBox.isChecked
        val elementTagUser = (myAdapterForCrtDlg.getItem(position) as Pair<String, String>).first
        if(checkBox.isChecked)
        {
            listUserForDlg.add(elementTagUser)
        }
        else
        {
            listUserForDlg.remove(elementTagUser)
        }
        myAdapterForCrtDlg.updateListChecked(listUserForDlg)
    }
    private fun addUserInGroupClick(){
        if (webSocketClient.connection.isClosed){
            Toast.makeText(requireContext(), "Отсутствует подключение к серверу",
                Toast.LENGTH_SHORT).show()
        }
        else
        {
            val addUserDlg = AddUserDLGTable(
                "ADDUSERDLG::",
                listUserForDlg,
                dialog_id
            )
            val msg = Json.encodeToString(addUserDlg)
            webSocketClient.send(msg)
        }
    }
}