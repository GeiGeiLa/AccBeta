package com.example.hamburger;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.example.hamburger.ui.home.HomeFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public final static String TARGET_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String TARGET_CHAR_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_NAME = "";
    public static final String DEVICE_ADDRESS = "ED:43:7E:75:73:4B";
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    BluetoothGattService mService = null;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e("SERVICE", "Android service connected");
            // TODO: assign value to mBluetoothLeService
            // here
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.

            // here
            // FIXME: new Katsmin
            if (!mConnected) {
                if (mBluetoothLeService != null) {
                    Log.e("", "try connect");

                    mBluetoothLeService.connect(mDeviceAddress);
                } else {
                    Log.e("", "BLE service is null");
                }
            } else {
                Log.e("", "assert connected");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                clearUI();
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                // here
                List<BluetoothGattService> gservices = mBluetoothLeService.getSupportedGattServices();
                // here
                for (BluetoothGattService bgs : gservices) {
                    if (bgs.getUuid().toString().equals(TARGET_SERVICE_UUID)) {
                        mService = bgs;
                    }
                }
                displayGattServices(gservices);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO: displayData
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(DeviceControlActivity.this);
                if (pref == null) {
                    Toast.makeText(getThisContext(), "pref is null", Toast.LENGTH_SHORT).show();
                }
                boolean onOff = pref.getBoolean("pref_switch_sendNotify", true);
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (onOff) {
                    SenseToBLENotification(data);
                }
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.

    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    Log.d(TAG, "CHildclick");
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            Log.d(TAG, "Katsmin:Notification opened");
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, true);
                        }
                        // end
                        return true;
                    }
                    return false;
                }
            };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        Log.e("", "Info devctrl");
        final Intent intent = getIntent();
        mDeviceAddress = DEVICE_ADDRESS;

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);


        getSupportActionBar().setTitle("ACC");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(getBaseContext(), BluetoothLeService.class);
        Log.e("", "will try to bind");
        boolean bindresult;
        getBaseContext().startService(gattServiceIntent);
        bindresult = getBaseContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_ABOVE_CLIENT);
        if (!bindresult) Log.e("", "Cannot bind");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception ex) {

        }
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.gatt_services, menu);
//        // Katsmin: Auto connect here
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this caused disconnection when pressing back?
                //onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            Log.e("UUID:", uuid);
            currentServiceData.put(
                    LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic.getUuid().toString().equals(TARGET_CHAR_UUID)) {
                    charas.add(gattCharacteristic);
                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.e("CHAR UUID:", uuid);
                    currentCharaData.put(
                            LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                    currentCharaData.put(LIST_UUID, uuid);
                    gattCharacteristicGroupData.add(currentCharaData);
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        final BluetoothGattCharacteristic characteristic =
                mGattCharacteristics.get(2).get(0);
        final int charaProp = characteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            Log.d(TAG, "Katsmin:Notification opened");
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    mNotifyCharacteristic, true);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /////  from simpledatadisplay
    static boolean notificationSent = false;
    private static Context context;

    public static Context getThisContext() {
        return context;
    }

    public void SenseToBLENotification(String data) {
        if (data != null) {
            Log.i("DATA:", data);
            HomeFragment.sendNotification("哎呀", "你摔倒了？", true, 0, this, AskIfFallActivity.class);
        }
    }
}

