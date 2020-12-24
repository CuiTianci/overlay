package com.ctc.overlay

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, BlankFragment())
            .commit()
    }
}