package com.learnteachcenter.ltcreikiclockv3.position.edit

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.api.responses.Position.UpdatePositionResponse
import com.learnteachcenter.ltcreikiclockv3.app.Injection
import com.learnteachcenter.ltcreikiclockv3.app.IntentExtraNames
import com.learnteachcenter.ltcreikiclockv3.reiki.ReikiGenerator
import kotlinx.android.synthetic.main.activity_edit_position.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPositionActivity : AppCompatActivity() {

    private var positionId: String = ""
    private var seqNo: Int = 0
    private var reikiId: String = ""
    private var title: String = ""
    private var duration: String = ""
    private lateinit var viewModel: EditPositionViewModel

    private var reikiApi = Injection.provideReikiApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_position)

        configureUI()

        val i = intent

        if(i.hasExtra(IntentExtraNames.EXTRA_POSITION_ID)) {
            positionId = i.getStringExtra(IntentExtraNames.EXTRA_POSITION_ID)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_POSITION_SEQ_NO)) {
            seqNo = i.getIntExtra(IntentExtraNames.EXTRA_POSITION_SEQ_NO, 0)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_REIKI_ID)) {
            reikiId = i.getStringExtra(IntentExtraNames.EXTRA_REIKI_ID)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_POSITION_TITLE)) {
            title = i.getStringExtra(IntentExtraNames.EXTRA_POSITION_TITLE)
        }

        if(i.hasExtra(IntentExtraNames.EXTRA_POSITION_DURATION)) {
            duration = i.getStringExtra(IntentExtraNames.EXTRA_POSITION_DURATION)
        }

        viewModel = ViewModelProviders.of(this).get(EditPositionViewModel::class.java)

        et_title.setText(title)
        et_duration.setText(duration)

        btn_update_position.setOnClickListener {
            if(isValidInput()) {
                disableButton()

                val title = et_title.text.toString().trim()
                val duration = et_duration.text.toString().trim()

                val generator = ReikiGenerator()

                val position = generator.generatePosition(positionId, seqNo, reikiId, title, duration)

                // Update in the local database
                viewModel.updatePosition(position)

                // Update in the network
                val call: Call<UpdatePositionResponse> = reikiApi.updatePosition(reikiId, positionId, title, duration)

                call.enqueue(object: Callback<UpdatePositionResponse> {
                    override fun onFailure(call: Call<UpdatePositionResponse>, t: Throwable) {
                        displayMessage("Server error: ${t.message}")
                        enableButton()
                    }

                    override fun onResponse(
                        call: Call<UpdatePositionResponse>,
                        response: Response<UpdatePositionResponse>
                    ) {
                        val updateResponse: UpdatePositionResponse? = response.body()

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

    private fun configureUI() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar_title.text = getString(R.string.edit_position)
        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_chevron_left)
        toolbar.setNavigationOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                onBackPressed()
                finish()
            }
        })
    }

    private fun displayMessage(message: String) {
        Log.wtf("Reiki", "Message: ${message}")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun enableButton() {
        btn_update_position.isEnabled = true
        btn_update_position.text = "Save"
    }

    private fun disableButton() {
        btn_update_position.isEnabled = false
        btn_update_position.text = "Saving..."
    }

    private fun isValidInput(): Boolean = et_title.text.toString().trim().isNotEmpty() and et_duration.text.toString().trim().isNotEmpty()
}
