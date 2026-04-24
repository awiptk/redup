package com.awiptk.redup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.RadioGroup;

public class MainActivity extends Activity {

    private static final int REQUEST_OVERLAY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnToggle = findViewById(R.id.btn_toggle);
        SeekBar seekOpacity = findViewById(R.id.seek_opacity);
        TextView tvOpacity = findViewById(R.id.tv_opacity);
        RadioGroup rgColor = findViewById(R.id.rg_color);

        seekOpacity.setMax(90);
        seekOpacity.setProgress(FilterService.opacity);
        tvOpacity.setText(FilterService.opacity + "%");

        seekOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvOpacity.setText(progress + "%");
                FilterService.opacity = progress;
                if (FilterService.isRunning) {
                    sendBroadcast(new Intent("com.awiptk.redup.UPDATE"));
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
                sendBroadcast(new Intent("com.awiptk.redup.UPDATE"));
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

        updateButton(btnToggle);
    }

    private void toggleFilter() {
        Button btnToggle = findViewById(R.id.btn_toggle);
        if (FilterService.isRunning) {
            stopService(new Intent(this, FilterService.class));
        } else {
            startService(new Intent(this, FilterService.class));
        }
        updateButton(btnToggle);
    }

    private void updateButton(Button btn) {
        btn.setText(FilterService.isRunning ? "Matikan Filter" : "Aktifkan Filter");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButton(findViewById(R.id.btn_toggle));
    }
}
