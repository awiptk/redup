package com.awiselow.redup

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQ_OVERLAY = 1001
    }

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
                Toast.makeText(this, "Izinkan tampil di atas aplikasi lain", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, REQ_OVERLAY)
                return@setOnClickListener
            }
            startDimService(seekBar.progress)
        }

        btnOff.setOnClickListener {
            stopService(Intent(this, DimService::class.java))
            Toast.makeText(this, "Redup dimatikan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_OVERLAY) {
            if (Settings.canDrawOverlays(this)) {
                val seekBar = findViewById<SeekBar>(R.id.seekBarDim)
                startDimService(seekBar.progress)
            } else {
                Toast.makeText(this, "Izin diperlukan untuk mengaktifkan redup", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startDimService(progress: Int) {
        val intent = Intent(this, DimService::class.java)
        intent.putExtra("alpha", progress)
        startService(intent)
        Toast.makeText(this, "Redup aktif", Toast.LENGTH_SHORT).show()
    }
}
