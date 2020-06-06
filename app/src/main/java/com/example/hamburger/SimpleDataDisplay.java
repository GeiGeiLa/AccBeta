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
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import com.example.hamburger.ui.home.HomeFragment;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.components.MarkerView;
//import com.github.mikephil.charting.components.
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.snackbar.Snackbar;


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