package com.learnteachcenter.ltcreikiclockv3.authentication.forgotpassword

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.authentication.login.LoginActivity
import com.learnteachcenter.ltcreikiclockv3.authentication.signup.SignUpActivity
import kotlinx.android.synthetic.main.activity_forgot_password.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btn_reset.setOnClickListener {
            // TODO: get the email and send to backend

            if(input_email.text.toString() == "") {
                input_email.error = "Fill in your email."
            } else {
                disableResetButton()

                val email: String = input_email.text.toString()

                val call: Call<ForgotPasswordResponse> = reikiApi.forgotPassword(email)

                call.enqueue(object: Callback<ForgotPasswordResponse> {
                    override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")
                        enableResetButton()
                    }

                    override fun onResponse(
                        call: Call<ForgotPasswordResponse>,
                        response: Response<ForgotPasswordResponse>
                    ) {
                        val forgotResponse: ForgotPasswordResponse? = response.body()

                        if(forgotResponse != null) {
                            if(forgotResponse.success) {
                                messageTextView.text = forgotResponse.message
                                messageTextView.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.colorInfo))
                            }
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            messageTextView.text = jObjError.toString()
                            messageTextView.setTextColor(ContextCompat.getColor(this@ForgotPasswordActivity, R.color.colorSwipeBackground))
                        }

                        enableResetButton()
                    }
                })
            }
        }

        link_signup.setOnClickListener {
            val signup = Intent(this@ForgotPasswordActivity, SignUpActivity::class.java)
            startActivity(signup)
        }

        link_login.setOnClickListener {
            val reset = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
            startActivity(reset)
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun enableResetButton() {
        btn_reset.isEnabled = true
        btn_reset.text = resources.getText(R.string.lbl_reset_password)
    }

    private fun disableResetButton() {
        btn_reset.isEnabled = false
        btn_reset.text = "Please wait..."
    }
}
