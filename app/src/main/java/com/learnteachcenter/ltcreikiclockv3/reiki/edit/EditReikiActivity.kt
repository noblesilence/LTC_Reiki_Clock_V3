package com.learnteachcenter.ltcreikiclockv3.reiki.edit

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.UpdateReikiResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.all.AllReikisActivity
import kotlinx.android.synthetic.main.activity_edit_reiki.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditReikiActivity : AppCompatActivity() {

    private var id: String = ""
    private var title: String = ""
    private var description: String = ""
    private var playMusic: Boolean = false

    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reiki)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            id = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_TITLE)) {
            title = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION)) {
            description = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC)) {
            playMusic = i.getBooleanExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, true)
        }

        et_title.setText(title)
        et_description.setText(description)
        ckb_play_music.setChecked(playMusic)

        btn_update_reiki.setOnClickListener {
            if(isValidInput()) {

                disableButton()

                val title = et_title.text.toString().trim()
                val description = et_description.text.toString().trim()
                val playMusic = ckb_play_music.isChecked

                val call: Call<UpdateReikiResponse> = reikiApi.updateReiki(id, title, description, playMusic)

                call.enqueue(object: Callback<UpdateReikiResponse>{
                    override fun onFailure(call: Call<UpdateReikiResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")

                        enableButton()
                    }

                    override fun onResponse(call: Call<UpdateReikiResponse>, response: Response<UpdateReikiResponse>) {
                        val updateResponse: UpdateReikiResponse? = response.body()

                        if(updateResponse != null) {
                            if(updateResponse.success) {
                                displayMessage("Update Success!")
                                finish()
                            } else {
                                val jObjError = JSONObject(response.errorBody()!!.string())
                                displayMessage(jObjError.toString())
                                enableButton()
                            }
                        }
                    }
                })
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun enableButton() {
        btn_update_reiki.isEnabled = true
        btn_update_reiki.text = "Save"
    }

    private fun disableButton() {
        btn_update_reiki.isEnabled = false
        btn_update_reiki.text = "Saving..."
    }

    private fun isValidInput(): Boolean = et_title.text.toString().trim().isNotEmpty()
}
