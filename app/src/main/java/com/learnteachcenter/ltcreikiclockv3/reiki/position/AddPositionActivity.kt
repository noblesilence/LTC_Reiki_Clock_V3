package com.learnteachcenter.ltcreikiclockv3.reiki.position

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.AddPositionResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiGenerator
import com.learnteachcenter.ltcreikiclockv3.reiki.session.ReikiSessionActivity
import kotlinx.android.synthetic.main.activity_add_position.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPositionActivity : AppCompatActivity() {

    private var repository = Injection.provideReikiRepository()
    private var reikiApi = Injection.provideReikiApi()
    private var generator = ReikiGenerator()
    private var reikiId = ""
    private var reikiTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_position)

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID) &&
                i.hasExtra(IntentExtraNames.EXTRA_REIKI_TITLE)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
            reikiTitle = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_TITLE)
        } else {
            Toast.makeText(this, "Missing Reiki ID name.", Toast.LENGTH_LONG).show()
            finish()
        }

        configureUI()
        configureClickListeners()
    }

    private fun configureUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Add Position"
    }

    private fun configureClickListeners() {
        btn_add_position.setOnClickListener {
            if(isValidInput()) {

                disableButton()

                val title = et_position_title.text.toString().trim()
                val duration = et_position_duration.text.toString().trim()

                // Add to remote db

                val call: Call<AddPositionResponse> = reikiApi.addPosition(reikiId, title, duration)

                call.enqueue(object: Callback<AddPositionResponse> {
                    override fun onFailure(call: Call<AddPositionResponse>, t: Throwable) {
                        displayMessage("Server error: $t.message")

                        enableButton()
                    }

                    override fun onResponse(call: Call<AddPositionResponse>, response: Response<AddPositionResponse>) {
                        val addResponse: AddPositionResponse? = response.body()

                        if(addResponse != null) {

                            if(addResponse.id != "") {
                                val position = generator.generatePosition(
                                    addResponse.id,
                                    addResponse.seqNo,
                                    reikiId, addResponse.title,
                                    addResponse.duration)

                                Log.wtf("Reiki", "Position, id: ${position.id}, title: ${position.title}, duration: ${position.duration}")

                                // Insert into local db

                                repository.insertPosition(position)

                                displayMessage("Add Success")

                                val i = Intent(this@AddPositionActivity, ReikiSessionActivity::class.java)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_ID, reikiId)
                                i.putExtra(IntentExtraNames.EXTRA_REIKI_TITLE, reikiTitle)
                                startActivity(i)
                                finish()
                            }

                        } else {
                            val jObjError = JSONObject(response.errorBody()!!.string())
                            displayMessage("Message from the server: " + jObjError.toString())
                            enableButton()
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
        btn_add_position.isEnabled = true
        btn_add_position.text = "Save"
    }

    private fun disableButton() {
        btn_add_position.isEnabled = false
        btn_add_position.text = "Saving..."
    }

    private fun isValidInput(): Boolean {
        return isValidTitle() && isValidDuration()
    }

    private fun isValidTitle(): Boolean = et_position_title.text.toString().trim().isNotEmpty()

    private fun isValidDuration(): Boolean {

        var isValid = false

        if(et_position_duration.text.toString().trim().isEmpty()) {
            return isValid
        } else {
            isValid = et_position_duration.text.toString().trim().matches("\\d+(\\:\\d+)?".toRegex())
            return isValid
        }
    }
}
