package com.learnteachcenter.ltcreikiclockv3.authentication.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.database.AuthenticationPrefs
import com.learnteachcenter.ltcreikiclockv3.reiki.all.AllReikisActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpActivity
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // If redirected after sign up, show sign up success message
        val message = intent.getStringExtra("MESSAGE")

        if(message != null) {
            messageTextView.text = message
        }

        // Login button clicked
        btn_login.setOnClickListener {
            if(isValidInput()) {

                disableLoginButton()

                val email = input_email.text.toString().trim()
                val password = input_password.text.toString().trim()

                val call: Call<LoginResponse> = reikiApi.logIn(email, password)

                call.enqueue(object: Callback<LoginResponse> {
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")

                        enableLoginButton()
                    }

                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        val loginResponse: LoginResponse? = response.body()

                        if(loginResponse != null) {
                            if(loginResponse.success) {
                                displayMessage("Login Success")

                                AuthenticationPrefs.saveAuthToken(loginResponse.token)
                                val i = Intent(this@LoginActivity, AllReikisActivity::class.java)
                                startActivity(i)
                                finish()
                            }
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            displayMessage(jObjError.toString())
                            enableLoginButton()
                        }
                    }
                })
            }
        }

        link_signup.setOnClickListener {
            val signup = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(signup)
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun enableLoginButton() {
        btn_login.isEnabled = true
        btn_login.text = "Log In"
    }

    private fun disableLoginButton() {
        btn_login.isEnabled = false
        btn_login.text = "Logging in..."
    }

    private fun isValidInput(): Boolean {
        if(!isEmailValid(input_email.text.toString().trim())) {
            input_email.error = resources.getString(R.string.enter_valid_email)
            return false
        } else if(!isPasswordValid(input_password.text.toString().trim())) {
            input_password.error = resources.getString(R.string.enter_valid_password)
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }
}