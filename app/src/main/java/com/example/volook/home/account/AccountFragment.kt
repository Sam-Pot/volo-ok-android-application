package com.example.volook.home.account

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentAccountBinding
import com.example.volook.login.LoginActivity
import com.example.volook.shared.api.user.Role
import com.example.volook.shared.api.user.User
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val accountViewModel =
            ViewModelProvider(this)[AccountViewModel::class.java]

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //GETTING FIELDS
        val emailAddressField: EditText = binding.accountEmailAddressField
        val nameField: EditText = binding.accountNameField
        val surnameField: EditText = binding.accountSurnameField
        val birthDateField: EditText = binding.accountBirthdate
        val customerCodeField: TextView = binding.accountCustomerCode
        val fidelityPointsField: TextView = binding.accountFidelityPoints
        val joinFidelityProgramCheck: CheckBox = binding.accountCheckboxJoinFidelityProgram
        val logOutButton: Button = binding.accountButtonLogout
        val saveButton: Button = binding.accountButtonSave
        val deleteButton: Button = binding.accountButtonDelete
        val swipeRefreshLayout: SwipeRefreshLayout = binding.swipeRefreshLayout
        //DISPLAY DATA
        accountViewModel.user.observe(viewLifecycleOwner){
            val userData: User = it
            emailAddressField.setText(userData.email)
            if(userData.customerCode!=null){
                joinFidelityProgramCheck.isChecked = true
                joinFidelityProgramCheck.isEnabled = false
                nameField.setText(userData.name)
                surnameField.setText(userData.surname)
                birthDateField.setText(dateFormat.format(Date(userData.birthDate.toString().toLong())))
                customerCodeField.setText(userData.customerCode.toString())
                fidelityPointsField.text = if(userData.residualPoints!=null){userData.residualPoints.toString()}else{"0"}
            }else{
                nameField.visibility = View.INVISIBLE
                surnameField.visibility = View.INVISIBLE
                birthDateField.visibility = View.INVISIBLE
                customerCodeField.visibility = View.INVISIBLE
                fidelityPointsField.visibility = View.INVISIBLE
            }

            joinFidelityProgramCheck.setOnClickListener{
                if(joinFidelityProgramCheck.isChecked){
                    nameField.visibility = View.VISIBLE
                    surnameField.visibility = View.VISIBLE
                    birthDateField.visibility = View.VISIBLE
                    customerCodeField.visibility = View.VISIBLE
                    fidelityPointsField.visibility = View.VISIBLE
                    if(userData.customerCode!=null){
                        nameField.setText(userData.name)
                        surnameField.setText(userData.surname)
                        birthDateField.setText(dateFormat.format(Date(userData.birthDate.toString().toLong())))
                        customerCodeField.setText(userData.customerCode.toString())
                        fidelityPointsField.text = if(userData.residualPoints!=null){userData.residualPoints.toString()}else{"0"}
                    }
                }else{
                    nameField.visibility = View.INVISIBLE
                    surnameField.visibility = View.INVISIBLE
                    birthDateField.visibility = View.INVISIBLE
                    customerCodeField.visibility = View.INVISIBLE
                    fidelityPointsField.visibility = View.INVISIBLE
                }
            }
            //ACTIONS
            logOutButton.setOnClickListener{
                accountViewModel.logout()
                val loginActivity = Intent(MainActivity.instance, LoginActivity::class.java)
                startActivity(loginActivity)
            }

            birthDateField.setOnClickListener{
                val currentDate = Calendar.getInstance()
                val year = currentDate.get(Calendar.YEAR)
                val month = currentDate.get(Calendar.MONTH)
                val day = currentDate.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(
                    MainActivity.instance,
                    DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                        val selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        birthDateField.setText(dateFormat.format(selectedDate.time))
                    },
                    year,
                    month,
                    day
                )
                datePickerDialog.show()
            }

            saveButton.setOnClickListener{
                val email: String = emailAddressField.text.toString()
                val name = nameField.text.toString()
                val surname = surnameField.text.toString()
                val birthDate = if(birthDateField.text.toString().isNotBlank()){dateFormat.parse(birthDateField.text.toString()).time}else{null}
                val role = if(email.isNotBlank() && name.isNotBlank() && surname.isNotBlank()){ Role.LOYALTY_CUSTOMER}else{ Role.CUSTOMER}
                //CHECK DATA
                if(email.isNullOrBlank() || ! Helpers.isValidEmail(email)){
                    Snackbar.make(root, R.string.login_invalid_email_format,Snackbar.LENGTH_SHORT).show()
                }else if(joinFidelityProgramCheck.isChecked
                    && (name.isNullOrBlank() || surname.isNullOrBlank())) {
                    Snackbar.make(root, R.string.fill_in_all_field_msg, Snackbar.LENGTH_SHORT)
                        .show()
                }else if(joinFidelityProgramCheck.isChecked
                    && (birthDate==null || birthDate >= Date().time)){
                    Snackbar.make(root, R.string.invalid_date_msg,Snackbar.LENGTH_SHORT).show()
                }else if(!joinFidelityProgramCheck.isChecked){
                    val userToUpdate: User = User(null, null,null, email,null,null,null,null)
                    accountViewModel.update(userToUpdate)
                }else{
                    //EXECUTE SAVE
                    val userToUpdate: User = User(name, surname,birthDate, email,null,userData.customerCode,null,role)
                    accountViewModel.update(userToUpdate)
                }
            }

            deleteButton.setOnClickListener{
                accountViewModel.delete()
                val loginActivity = Intent(MainActivity.instance, LoginActivity::class.java)
                startActivity(loginActivity)
            }

            swipeRefreshLayout.setOnRefreshListener {
                accountViewModel.getData()
                swipeRefreshLayout.isRefreshing = false
            }

            accountViewModel.statusMessage.observe(viewLifecycleOwner){
                if(it==StatusCode.SUCCESS){
                    Snackbar.make(root,R.string.account_updated,Snackbar.LENGTH_SHORT).show()
                    accountViewModel.statusMessage.value = StatusCode.NEUTRAL
                }else if(it!=StatusCode.NEUTRAL){
                    Snackbar.make(root,R.string.an_error_occurred,Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}