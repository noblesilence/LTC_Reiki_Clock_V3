package com.learnteachcenter.ltcreikiclockv3.contact

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContactActivity : AppCompatActivity() {

    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        configureToolbar()

        btn_send.setOnClickListener {
            if(isValidInput()) {
                disableSendButton()

                val name = input_name.text.toString().trim()
                val email = input_email.text.toString().trim()
                val subject = input_subject.text.toString().trim()
                val message = input_message.text.toString().trim()

                val call: Call<SendEmailResponse> = reikiApi.sendEmail(name, email, subject, message)
                call.enqueue(object: Callback<SendEmailResponse> {
                    override fun onFailure(call: Call<SendEmailResponse>, t: Throwable) {
                        displayMessage(false, "Server: ${t.message}")
                        enableSendButton()
                    }

                    override fun onResponse(call: Call<SendEmailResponse>, response: Response<SendEmailResponse>) {
                        val sendEmailResponse: SendEmailResponse? = response.body()

                        if(sendEmailResponse != null) {
                            if(sendEmailResponse.success) {
                                displayMessage(true, sendEmailResponse.message)
                                enableSendButton()
                            }
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            displayMessage(false, jObjError.toString())
                            enableSendButton()
                        }
                    }
                })
            }
        }
    }

    private fun configureToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.lbl_contact).toUpperCase()
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_chevron_left)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                onBackPressed()
                finish()
            }
        })
    }

    private fun displayMessage(success: Boolean, message: String) {

        messageTextView.text = message

        if(success) {
            messageTextView.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess))
        } else {
            messageTextView.setTextColor(ContextCompat.getColor(this, R.color.colorError))
        }
    }

    private fun enableSendButton() {
        btn_send.isEnabled = true
        btn_send.text = "Send"
    }

    private fun disableSendButton() {
        btn_send.isEnabled = false
        btn_send.text = "Sending..."
    }

    private fun isValidInput() : Boolean {
        if(!isNotEmpty(input_name.text.toString().trim())) {
            input_name.error = "Enter your name"
            return false
        }
        else if(!isEmailValid(input_email.text.toString().trim())) {
            input_email.error = "Enter valid email"
            return false
        } else if(!isNotEmpty(input_subject.text.toString().trim())) {
            input_subject.error = "Enter a subject"
            return false
        } else if(!isNotEmpty(input_message.text.toString().trim())) {
            input_message.error = "Enter a message"
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isNotEmpty(input: String): Boolean {
        return input.isNotEmpty()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
