package com.example.hamburger.ui.home;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DebugUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hamburger.AskIfFallActivity;
import com.example.hamburger.DeviceControlActivity;
import com.example.hamburger.DummyActivity;
import com.example.hamburger.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {
    final static String CHANNEL_ID = "testNotify";
    private HomeViewModel homeViewModel;
    Button btn_newActivity;
    public HomeFragment()
    {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        btn_newActivity = root.findViewById(R.id.btn_newActivity);

        final Button btn_sendNotification = root.findViewById(R.id.btn_notify);
        btn_sendNotification.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                Log.i(TAG,"Clicked button");
                int sec = 4;
                Snackbar.make(getView(),sec+"秒後會顯示通知",Snackbar.LENGTH_LONG).show();
                btn_sendNotification.setEnabled(false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendNotification("發現您摔倒了","您還好嘛？",true, 0, getActivity(),AskIfFallActivity.class);
                        btn_sendNotification.setEnabled(true);
                    }
                }, sec * 1000);
            }
        });
        return root;
    }


    public static void sendNotification(String title, String notifyText, boolean useClickAction, int chnnelId, Activity activity, Class targetActivityClass)
    {
        NotificationChannel notifyChannel = new NotificationChannel(
                CHANNEL_ID,"channelName", NotificationManager.IMPORTANCE_HIGH);
        notifyChannel.setDescription("摔倒通知");
        notifyChannel.enableLights(true);
        notifyChannel.setLightColor(Color.RED);
        notifyChannel.enableVibration(true);
        notifyChannel.setVibrationPattern(new long[]{1000,1000,1000,1000});

        NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notifyChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.protobelt)
                .setContentTitle(title)
                .setContentText(notifyText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_ALL);
        if(useClickAction)
        {
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 1, new Intent(activity, targetActivityClass),0);
            builder.setContentIntent(pendingIntent)
                    .setAutoCancel(true);
        }
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, builder.build());
    }
}
