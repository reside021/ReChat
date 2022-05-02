package com.example.myapplication.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.ActivityMain.Companion.webSocketClient
import com.example.myapplication.R
import com.example.myapplication.dataClasses.*
import com.squareup.picasso.Picasso
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class UserFragment : Fragment(){


    internal interface OnFragmentSendDataListener {
        fun onUserLoadView()
        fun exitFromAccount()
    }

    companion object{
        fun newInstance() = UserFragment()
    }

    private var fragmentSendDataListener: OnFragmentSendDataListener? = null
    private lateinit var birthDay : TextView
    private lateinit var socStatus : TextView
    private lateinit var country : TextView
    private lateinit var dateReg : TextView
    private lateinit var aboutMe : TextView
    private lateinit var spinnerVision : Spinner
    private lateinit var spinnerGender : Spinner
    private var spinnerVisionValue = 0
    private var spinnerGenderValue = 0
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
        birthDay = requireView().findViewById(R.id.birthday)
        requireView().findViewById<TextView>(R.id.birthdayClick).setOnClickListener {
            val dateAndTime = Calendar.getInstance()
            DatePickerDialog(
                requireContext(), dateListner,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        socStatus = requireView().findViewById(R.id.socStatus)
        requireView().findViewById<TextView>(R.id.socStatusClick).setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val newView = this.layoutInflater.inflate(R.layout.edit_text_helper, null)
            val editTextHelper = newView.findViewById<EditText>(R.id.editTextHelper)
            editTextHelper.hint = resources.getString(R.string.social_status)
            editTextHelper.setText(socStatus.text)
            builder.setView(newView)
                .setPositiveButton("OK") { dialog, id ->
                    if (editTextHelper.text == socStatus.text) return@setPositiveButton
                    if(webSocketClient.connection.isClosed){
                        Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else{
                        val updateSocStatus =
                            UpdateDataString(
                                "UPDATE::",
                                "SOCSTATUS::",
                                editTextHelper.text.toString())
                        val dataServerName = Json.encodeToString(updateSocStatus)
                        webSocketClient.send(dataServerName)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss()
                }
            builder.show()
        }
        country = requireView().findViewById(R.id.country)
        requireView().findViewById<TextView>(R.id.countryClick).setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val newView = this.layoutInflater.inflate(R.layout.edit_text_helper, null)
            val editTextHelper = newView.findViewById<EditText>(R.id.editTextHelper)
            editTextHelper.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            editTextHelper.hint = resources.getString(R.string.country)
            editTextHelper.setText(country.text)
            builder.setView(newView)
                .setPositiveButton("OK") { dialog, id ->
                    if (editTextHelper.text == country.text) return@setPositiveButton
                    if(webSocketClient.connection.isClosed){
                        Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else{
                        val updateCountry =
                            UpdateDataString(
                                "UPDATE::",
                                "COUNTRY::",
                                editTextHelper.text.toString())
                        val dataServerName = Json.encodeToString(updateCountry)
                        webSocketClient.send(dataServerName)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss()
                }
            builder.show()
            true
        }
        aboutMe = requireView().findViewById(R.id.aboutme)
        requireView().findViewById<TextView>(R.id.aboutMeClick).setOnClickListener {
            val builder = AlertDialog.Builder(activity)
            val newView = this.layoutInflater.inflate(R.layout.edit_text_helper_about, null)
            val editTextHelper = newView.findViewById<EditText>(R.id.editTextHelper_about)
            editTextHelper.hint = resources.getString(R.string.about_me)
            editTextHelper.setText(aboutMe.text)
            builder.setView(newView)
                .setPositiveButton("OK") { dialog, id ->
                    if (editTextHelper.text == aboutMe.text) return@setPositiveButton
                    if(webSocketClient.connection.isClosed){
                        Toast.makeText(
                            activity, "Отсутствует подключение к серверу",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else{
                        val updateAboutMe =
                            UpdateDataString(
                                "UPDATE::",
                                "ABOUTME::",
                                editTextHelper.text.toString())
                        val dataServerName = Json.encodeToString(updateAboutMe)
                        webSocketClient.send(dataServerName)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss()
                }
            builder.show()
            true
        }
        spinnerVision = requireView().findViewById(R.id.spinnerVisible)
        val adapterVision = ArrayAdapter.createFromResource(requireActivity(), R.array.isVisibleData,R.layout.spinner_element)
        adapterVision.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerVision.adapter = adapterVision
        spinnerVision.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (spinnerVisionValue == position) return
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        activity, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                } else{
                    val updateSpinner =
                        UpdateSpinner("UPDATE::", "VISIONDATA::", position)
                    val dataServerName = Json.encodeToString(updateSpinner)
                    webSocketClient.send(dataServerName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        spinnerGender = requireView().findViewById(R.id.spinnerGender)
        val adapterGender = ArrayAdapter.createFromResource(requireActivity(), R.array.gender,R.layout.spinner_element)
        adapterGender.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerGender.adapter = adapterGender
        spinnerGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(spinnerGenderValue == position) return
                if(webSocketClient.connection.isClosed){
                    Toast.makeText(
                        activity, "Отсутствует подключение к серверу",
                        Toast.LENGTH_SHORT
                    ).show()
                } else{
                    val updateSpinner =
                        UpdateSpinner("UPDATE::", "GENDER::", position)
                    val dataServerName = Json.encodeToString(updateSpinner)
                    webSocketClient.send(dataServerName)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }
        dateReg = requireView().findViewById(R.id.dateReg)
        requireView().findViewById<Button>(R.id.exitFromAcc).setOnClickListener {
            fragmentSendDataListener?.exitFromAccount()
        }
        fragmentSendDataListener?.onUserLoadView()
    }

    private val dateListner = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
        val date = if(month < 9){
            "${dayOfMonth}.0${month + 1}.${year}"
        }else{
            "${dayOfMonth}.${month + 1}.${year}"
        }
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dateCurrent = LocalDate.now()
        val dateSet = LocalDate.parse(date, formatter)
        if (dateCurrent > dateSet){
            if(webSocketClient.connection.isClosed){
                Toast.makeText(
                    activity, "Отсутствует подключение к серверу",
                    Toast.LENGTH_SHORT
                ).show()
            } else{
                val updateBirthday =
                    UpdateDataString("UPDATE::", "BIRTHDAY::", date)
                val dataServerName = Json.encodeToString(updateBirthday)
                webSocketClient.send(dataServerName)
            }
        }else{
            Toast.makeText(
                activity, "Невозможно установить эту дату",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun setUserData(data: Data, urlAvatar : String){
        requireView().findViewById<TextView>(R.id.nameOfUser).text = data.nickname
        requireView().findViewById<TextView>(R.id.tagOfUser).text = data.tagUser
        birthDay.text = data.birthday
        socStatus.text = data.socStatus
        country.text = data.country
        dateReg.text = data.dateReg
        aboutMe.text = data.aboutMe
        spinnerVisionValue = data.isVisionData
        spinnerGenderValue = data.gender
        spinnerVision.setSelection(spinnerVisionValue)
        spinnerGender.setSelection(spinnerGenderValue)
        val switchBeOnline = requireView().findViewById<SwitchCompat>(R.id.switchBeOnline)
        switchBeOnline.isChecked = data.isVisible
        if(data.isAvatar){
            val imageOfUser = requireView().findViewById<ImageView>(R.id.imageOfUser)
            Picasso.get()
                    .load(urlAvatar)
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
                .placeholder(R.drawable.user_profile_photo)
                .into(imageOfUser)
    }

    fun setNewUserName(newName : String){
        requireView().findViewById<TextView>(R.id.nameOfUser).text = newName
    }

    fun setNewVisible(newVisible : Boolean){
        requireView().findViewById<SwitchCompat>(R.id.switchBeOnline).isChecked = newVisible
    }
    fun setNewVisionData(newData : Int){
        spinnerVisionValue = newData
        spinnerVision.setSelection(spinnerVisionValue)
    }
    fun setNewGender(newData : Int){
        spinnerGenderValue = newData
        spinnerGender.setSelection(spinnerGenderValue)
    }
    fun setBirthday(newData : String){
        birthDay.text = newData
    }
    fun setSocStatus(newData : String){
        socStatus.text = newData
    }
    fun setCountry(newData : String){
        country.text = newData
    }
    fun setAboutMe(newData : String){
        aboutMe.text = newData
    }
}
