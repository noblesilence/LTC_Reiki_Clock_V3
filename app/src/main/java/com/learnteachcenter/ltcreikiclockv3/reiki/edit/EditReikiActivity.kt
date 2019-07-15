package com.learnteachcenter.ltcreikiclockv3.reiki.edit

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.UpdateReikiResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiGenerator
import kotlinx.android.synthetic.main.activity_edit_reiki.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditReikiActivity : AppCompatActivity() {

    private var id: String = ""
    private var seqNo: Int = 0
    private var title: String = ""
    private var description: String = ""
    private var playMusic: Boolean = false

    private var reikiApi = Injection.provideReikiApi()
    private var repository = Injection.provideReikiRepository()
    private var generator = ReikiGenerator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_reiki)

        configureUI()

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            id = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_SEQ_NO)) {
            seqNo = i.getIntExtra(IntentExtraNames.EXTRA_REIKI_SEQ_NO, 0)
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

                val reiki = generator.generateReiki(id, seqNo, title, description, playMusic)

                // Update in local database
                repository.updateReikis(reiki)

                // Update in remote database

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

    // This method is duplicate with AddReikiActivity
    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.edit_reiki)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_chevron_left)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                onBackPressed()
                finish()
            }
        })
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
