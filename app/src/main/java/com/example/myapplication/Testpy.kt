package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class Testpy : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testpy)

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this));
        }
        val python = Python.getInstance()
        val module = python.getModule("script")

        val num = module["number"]?.toInt()
        println("The value of num is $num")

        val text = module["text"]?.toString()
        println("The value of num is $text")


        val fact = module["factorial"]
        val a = fact?.call(5)

        println("The value of num is $a")


        val textView = findViewById<TextView>(R.id.textView3)
// Set text
        textView.text = "$a"
// Get text
        val textViewString = textView.text.toString()
    }
}