package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            //show start activity
            startActivity(Intent(this@MainActivity, Activity2::class.java))
            Toast.makeText(this@MainActivity, "First Run", Toast.LENGTH_LONG)
                .show()
        }


        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bNav.selectedItemId = R.id.item1
        binding.bNav.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.item1 -> {
                    Toast.makeText(this, "Главная", Toast.LENGTH_SHORT).show()

                }
                R.id.item2 -> {
                    Toast.makeText(this, "Поиск", Toast.LENGTH_SHORT).show()
                }
                R.id.item3 -> {
                    Toast.makeText(this, "Анализ", Toast.LENGTH_SHORT).show()
                }
                R.id.item4 -> {
                    Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()
                }
                R.id.item5 -> {
                    Toast.makeText(this, "Профиль", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        Log.d("MyLogMAct", "OnCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("MyLogMAct", "onStart")
    }

    override fun onResume() {
        super.onResume()


        Log.d("MyLogMAct", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MyLogMAct", "onPause")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLogMAct", "onDestroy")
    }


    override fun onStop() {
        super.onStop()
        Log.d("MyLogMAct", "onStop")
    }


    override fun onRestart() {
        super.onRestart()
        Log.d("MyLogMAct", "onRestart")
    }
}