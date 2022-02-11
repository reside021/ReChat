package com.example.myapplication.ui

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.dataClasses.LoginDataUser
import com.example.myapplication.dataClasses.SignUpUser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthFragment : Fragment() {
    internal interface OnFragmentSendDataListener {
        fun onAuthLoadView()
    }
    companion object{
        fun newInstance() = AuthFragment()
    }
    private var fragmentSendDataListener: OnFragmentSendDataListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.auth_activity, container, false)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentSendDataListener = context as OnFragmentSendDataListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSendDataListener?.onAuthLoadView()

        val signBtn = requireView().findViewById<AppCompatButton>(R.id.onSignBtn)
        val loginBtn = requireView().findViewById<Button>(R.id.loginBtn)

        signBtn.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity())
            val inflater = this.layoutInflater
            val view  = inflater.inflate(R.layout.dialogsignup, null)
            builder.setView(view)
            val alertDialog = builder.create();
            alertDialog.show()
            view.findViewById<Button>(R.id.cancelBtnSignup).setOnClickListener {
                alertDialog.dismiss()
            }
            view.findViewById<Button>(R.id.signupBtn).setOnClickListener {
                val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                val loginsign : String = view.findViewById<EditText>(R.id.loginSign).text.toString()
                val usernamesign : String = view.findViewById<EditText>(R.id.usernameSign).text.toString()
                val pass1sign : String = view.findViewById<EditText>(R.id.pass1Sign).text.toString()
                val pass2sign : String = view.findViewById<EditText>(R.id.pass2Sign).text.toString()
                if(loginsign.trim().isEmpty() ||
                    usernamesign.trim().isEmpty() ||
                    pass1sign.trim().isEmpty() ||
                    pass2sign.trim().isEmpty()){
                    Toast.makeText(
                        activity,
                        "Все поля должны быть заполнены!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if(pass1sign != pass2sign){
                    Toast.makeText(
                        activity,
                        "Пароли не совпадают!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if(!view.findViewById<CheckBox>(R.id.checkBoxPrivacy).isChecked){
                    Toast.makeText(
                        activity,
                        "Вы не дали согласия на обработку данных",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val dataSignUpUser = SignUpUser("SIGNUP::", loginsign, pass1sign, usernamesign)
                val msg = Json.encodeToString(dataSignUpUser)
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        activity,
                        "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }else{
                    webSocketClient.send(msg)
                    alertDialog.dismiss()
                }
            }
            if(webSocketClient.connection.isClosed){
                Toast.makeText(
                    activity,
                    "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        loginBtn.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity())
            val inflater = this.layoutInflater
            val view  = inflater.inflate(R.layout.dialoglogin, null)
            builder.setView(view)
            val alertDialog = builder.create()
            alertDialog.show()
            view.findViewById<Button>(R.id.cancelBtnAuth).setOnClickListener {
                alertDialog.dismiss()
            }
            view.findViewById<Button>(R.id.loginBtnAuth).setOnClickListener {
                val imm = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                val loginAuth = view.findViewById<EditText>(R.id.loginAuth).text.toString()
                val passAuth = view.findViewById<EditText>(R.id.passAuth).text.toString()

                if(loginAuth.trim().isEmpty() || passAuth.trim().isEmpty()){
                    return@setOnClickListener
                }
                val dataUser = LoginDataUser("AUTH::", false, loginAuth, passAuth)
                val msg = Json.encodeToString(dataUser)
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        activity,
                        "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else{
                    webSocketClient.send(msg)
                    alertDialog.dismiss()
                }
            }
            if(webSocketClient.connection.isClosed){
                Toast.makeText(
                    activity,
                    "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }


}