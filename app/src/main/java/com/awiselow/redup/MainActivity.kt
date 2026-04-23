package com.awiselow.redup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnDim = findViewById<Button>(R.id.btnDim)
        val btnOff = findViewById<Button>(R.id.btnOff)
        val seekBar = findViewById<SeekBar>(R.id.seekBarDim)
        val tvLevel = findViewById<TextView>(R.id.tvLevel)

        seekBar.max = 100
        seekBar.progress = 80

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvLevel.text = "Tingkat gelap: $progress%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnDim.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
                startActivity(intent)
                return@setOnClickListener
            }
            val intent = Intent(this, DimService::class.java)
            intent.putExtra("alpha", seekBar.progress)
            startService(intent)
        }

        btnOff.setOnClickListener {
            stopService(Intent(this, DimService::class.java))
        }
    }
}
