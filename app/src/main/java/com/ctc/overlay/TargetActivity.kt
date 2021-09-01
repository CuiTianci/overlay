package com.ctc.overlay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ctc.overlay.databinding.ActivityTargetBinding
import kotlin.random.Random

class TargetActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RANDOM_NUMBER = "extra_random_number"
    }

    private lateinit var binding: ActivityTargetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val randomInt = Random.nextInt()
        binding = ActivityTargetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnFeedback.text = "Feedback:$randomInt"
        binding.btnFeedback.setOnClickListener {
            val intent = Intent()
            intent.putExtra(EXTRA_RANDOM_NUMBER, randomInt)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}