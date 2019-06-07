package com.learnteachcenter.ltcreikiclockv3.authentication.signup

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.authentication.User
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    val TAG = "Reiki"

    private lateinit var user: User
    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // User clicks Sign up
        btn_signup.setOnClickListener {

            if(isValidInput()) {

                displaySignUpButton()

                user = User(
                    input_name.text.toString().trim(),
                    input_email.text.toString().trim(),
                    input_password.text.toString().trim(),
                    input_password_confirm.text.toString().trim(),
                    ""
                )

                val call: Call<SignUpResponse> = reikiApi.signUp(user.name, user.email, user.password, user.password2)

                call.enqueue(object: Callback<SignUpResponse> {
                    override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")
                        enableSignUpButton()
                    }

                    override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                        val signUpResponse: SignUpResponse? = response.body()

                        if(signUpResponse != null) {
                            Log.d(TAG, "signUpResponse: $signUpResponse.email")

                            displayMessage("Sign Up Success")
                            val i = Intent(this@SignUpActivity, LoginActivity::class.java)
                            i.putExtra("MESSAGE", "You have successfully signed up. Now you may log in.")
                            startActivity(i)
                            finish()
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            displayMessage(jObjError.toString())
                            enableSignUpButton()
                        }
                    }
                })
            }
        }

        // Link to Login activity
        link_login.setOnClickListener {
            val loginIntent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun enableSignUpButton() {
        btn_signup.isEnabled = true
        btn_signup.text = "Sign Up"
    }

    private fun displaySignUpButton() {
        btn_signup.isEnabled = false
        btn_signup.text = "Signing Up..."
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun isValidInput(): Boolean {
        if (!isNameValid(input_name.text.toString().trim())) {
            input_name.error = resources.getString(R.string.enter_valid_name)
            return false
        } else if (!isEmailValid(input_email.text.toString().trim())) {
            input_email.error = resources.getString(R.string.enter_valid_email)
            return false
        } else if (!isPasswordValid(input_password.text.toString().trim())) {
            input_password.error = resources.getString(R.string.enter_valid_password)
            return false
        } else if (!isPasswordValid(input_password_confirm.text.toString().trim())) {
            input_password_confirm.error = resources.getString(R.string.enter_valid_password)
            return false
        } else if (input_password.text.toString().trim() != input_password_confirm.text.toString().trim()) {
            input_password.error = resources.getString(R.string.error_mismatch_passwords)
            input_password_confirm.error = resources.getString(R.string.error_mismatch_passwords)
            return false
        }

        return true
    }

    private fun isNameValid(name: String): Boolean {
        return name.isNotEmpty()
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }
}
