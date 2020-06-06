package com.example.hamburger;


/**
 * @author 許劼忞
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.accessibilityservice.FingerprintGestureController;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.hamburger.ui.home.HomeFragment;

public class SimpleDataDisplay extends AppCompatActivity {
    static boolean notificationSent = false;
    private static Context context;
    public static Context getThisContext()
    {
        return context;
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO: displayData
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getThisContext());
                if (pref == null) {
                    Toast.makeText(getThisContext(), "pref is null", Toast.LENGTH_SHORT).show();
                }
                boolean onOff = pref.getBoolean("pref_switch_sendNotify", true);
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (onOff)
                {
                    SenseToBLENotification(data);
                }
            }
        }
    };
    public void SenseToBLENotification(String data)
    {
        if(data != null)
        {
            Log.i("DATA:", data);
            HomeFragment.sendNotification("哎呀","你摔倒了？",true,0,this,AskIfFallActivity.class);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        }
        catch(Exception ex)
        {

        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_simple_data_display);
        SimpleDataDisplay.context = getApplicationContext();
        Log.e("", "Into line chart");
    }
}