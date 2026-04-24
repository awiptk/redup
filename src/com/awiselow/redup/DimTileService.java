package com.awiselow.redup;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.QSTileService;
import android.service.quicksettings.Tile;

public class DimTileService extends QSTileService {
    private boolean active = false;

    @Override
    public void onTileAdded() { updateTile(); }
    @Override
    public void onStartListening() { updateTile(); }

    @Override
    public void onClick() {
        active = !active;
        Intent srv = new Intent(this, DimService.class);
        if (active) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(srv);
            else startService(srv);
        } else stopService(srv);
        updateTile();
    }

    private void updateTile() {
        Tile t = getQsTile();
        if (t != null) {
            t.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
            t.updateTile();
        }
    }
}
