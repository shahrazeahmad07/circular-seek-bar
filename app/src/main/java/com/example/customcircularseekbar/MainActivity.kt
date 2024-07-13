package com.example.customcircularseekbar

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.customcircularseekbar.custom.CircularSeekbar
import com.example.customcircularseekbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.circularSeekbar.maxProgress = 300f
        binding.circularSeekbar.progress = 100f
        binding.circularSeekbar.setOnCircularProgressChangeListener(object : CircularSeekbar.CircularProgressChangeListener {
            override fun onProgressChangeStart() {
                //TODO("Not yet implemented")
            }

            override fun onProgressChange(progress: Float) {
                Log.i("TAG", "onProgressChange: $progress")
            }

            override fun onProgressChangeStop(progress: Float) {
                //TODO("Not yet implemented")
            }

        })
    }
}