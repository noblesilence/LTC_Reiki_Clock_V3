package com.learnteachcenter.ltcreikiclockv3.reiki.add

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Reiki.AddReikiResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.position.list.PositionListActivity
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiGenerator
import kotlinx.android.synthetic.main.activity_add_reiki.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddReikiActivity : AppCompatActivity() {

    private var reikiApi = Injection.provideReikiApi()
    private var repository = Injection.provideReikiRepository()
    private var generator = ReikiGenerator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_reiki)

        configureUI()

        // On Save click
        btn_add_reiki.setOnClickListener {
            if(isValidInput()) {

                disableButton()

                val title = input_reiki_title.text.toString().trim()
                val description = input_reiki_description.text.toString().trim()
                val playMusic = ckb_play_music.isChecked

                val call: Call<AddReikiResponse> = reikiApi.addReiki(title, description, playMusic, null)

                call.enqueue(object: Callback<AddReikiResponse> {
                    override fun onFailure(call: Call<AddReikiResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")

                        enableButton()
                    }

                    override fun onResponse(call: Call<AddReikiResponse>, response: Response<AddReikiResponse>) {
                        val addResponse: AddReikiResponse? = response.body()

                        Log.wtf("Reiki", "[AddReikiActivity] addResponse: ${addResponse}")

                        if(addResponse != null) {
                            if(addResponse.id != "") {
                                val reiki = generator.generateReiki(addResponse.id, addResponse.seqNo, addResponse.title, addResponse.description, addResponse.playMusic, addResponse.positions)

                                // ID got back from the remote database. Add the Reiki to local database
                                repository.addReiki(reiki)

                                displayMessage("Add Success")

                                // Redirect to PositionListActivity
                                val i = Intent(this@AddReikiActivity, PositionListActivity::class.java)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reiki.id)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reiki.title)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_DESCRIPTION, reiki.description)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_PLAY_MUSIC, reiki.playMusic)
                                startActivity(i)

                                finish()
                            }
                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            displayMessage(jObjError.toString())
                            enableButton()
                        }
                    }
                })
            }
        }
    }

    // This code is duplicate with EditReikiActivity
    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.add_reiki)
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
        btn_add_reiki.isEnabled = true
        btn_add_reiki.text = "Save"
    }

    private fun disableButton() {
        btn_add_reiki.isEnabled = false
        btn_add_reiki.text = "Saving..."
    }

    private fun isValidInput(): Boolean = input_reiki_title.text.toString().trim().isNotEmpty()
}