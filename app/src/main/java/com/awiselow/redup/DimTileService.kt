package com.awiselow.redup

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class DimTileService : TileService() {

    override fun onTileAdded() {
        updateTile(isRunning())
    }

    override fun onStartListening() {
        updateTile(isRunning())
    }

    override fun onClick() {
        if (isRunning()) {
            sendBroadcast(Intent("com.awiselow.redup.EXIT"))
            updateTile(false)
        } else {
            val intent = Intent(this, DimService::class.java).apply {
                putExtra("alpha", 80)
            }
            startForegroundService(intent)
            updateTile(true)
        }
    }

    private fun updateTile(active: Boolean) {
        qsTile?.apply {
            state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Redup"
            updateTile()
        }
    }

    private fun isRunning(): Boolean {
        return getSharedPreferences("redup_prefs", MODE_PRIVATE)
            .getBoolean("is_active", false)
    }
}
