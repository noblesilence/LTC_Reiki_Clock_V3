package com.learnteachcenter.ltcreikiclockv3.view.reiki

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.learnteachcenter.ltcreikiclockv3.R
import com.learnteachcenter.ltcreikiclockv3.viewmodel.ReikiViewModel
import com.learnteachcenter.ltcreikiclockv3.databinding.ActivityReikiBinding
import kotlinx.android.synthetic.main.activity_reiki.*


class ReikiActivity : AppCompatActivity() {

    private lateinit var viewModel: ReikiViewModel

    lateinit var binding: ActivityReikiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reiki)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reiki)

        viewModel = ViewModelProviders.of(this).get(ReikiViewModel::class.java)
        binding.viewmodel = viewModel

        configureUI()
        configureLiveDataObservers()
    }

    private fun configureUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Add Reiki"
    }

    private fun configureLiveDataObservers() {
        viewModel.getReikiLiveData().observe(this, Observer { reiki ->
            reiki?.let {
                titleEditText.setText(reiki.title)
                descriptionEditText.setText((reiki.description))
                playMusicCheckBox.setChecked(reiki.playMusic)
            }
        })

        viewModel.getSaveLiveData().observe(this, Observer { saved ->
            saved?.let {
                if(saved) {
                    Toast.makeText(this,  getString(R.string.reiki_saved), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this,  getString(R.string.error_saving_reiki), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
