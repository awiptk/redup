package com.awiptk.redup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int REQUEST_OVERLAY = 1;
    private Button btnToggle;
    private TextView tvOpacity;
    private SeekBar seekOpacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btn_toggle);
        seekOpacity = findViewById(R.id.seek_opacity);
        tvOpacity = findViewById(R.id.tv_opacity);
        RadioGroup rgColor = findViewById(R.id.rg_color);

        seekOpacity.setMax(90);
        syncUI();

        seekOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvOpacity.setText(progress + "%");
                FilterService.opacity = progress;
                if (FilterService.isRunning) {
                    sendBroadcast(new Intent(FilterService.ACTION_UPDATE));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        rgColor.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_normal) FilterService.colorMode = 0;
            else if (checkedId == R.id.rb_warm) FilterService.colorMode = 1;
            else if (checkedId == R.id.rb_cool) FilterService.colorMode = 2;
            if (FilterService.isRunning) {
                sendBroadcast(new Intent(FilterService.ACTION_UPDATE));
            }
        });

        btnToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
                return;
            }
            toggleFilter();
        });
    }

    private void toggleFilter() {
        if (FilterService.isRunning) {
            stopService(new Intent(this, FilterService.class));
            FilterService.isRunning = false;
        } else {
            startService(new Intent(this, FilterService.class));
            FilterService.isRunning = true;
        }
        updateButton();
    }

    private void syncUI() {
        seekOpacity.setProgress(FilterService.opacity);
        tvOpacity.setText(FilterService.opacity + "%");
        updateButton();
    }

    private void updateButton() {
        btnToggle.setText(FilterService.isRunning ? "Matikan Filter" : "Aktifkan Filter");
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY && Settings.canDrawOverlays(this)) {
            toggleFilter();
        }
    }
}
