package com.test.rummyTest

import `in`.glg.rummy.activities.RummyInstance
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initRummy()
    }

    private fun initRummy() {
        rummy_button.setOnClickListener {
            val rummyInstance = RummyInstance(this)
            rummyInstance.inIt("", "")
        }
    }
}
