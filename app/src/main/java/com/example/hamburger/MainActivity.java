package com.example.hamburger;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hamburger.ui.home.HomeFragment;
import com.example.hamburger.ui.records.RecordsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.hamburger.ui.settings.SettingsFragment;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawerLayout; // for settings new activity
    private NavigationView navigation_view; // for settings new activity
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String DEVICE_ADDRESS = "ED:43:7E:75:73:4B";
    public static boolean bleserviceValid = false;
    private Toolbar toolbar;
    private static View currentView;
    
    ///
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    
    
    ///
    // 特地為drawer的設定按鈕做出點擊事件
    public View getCurrentView()
    {
        return getWindow().getDecorView();
    }
    public static View retrieveCurrentView()
    {
        return currentView;
    }
    public static void setupCurrentView(View v)
    {
        currentView = v;
    }
    private void setOnClickForNavBar()
    {
        toolbar = findViewById(R.id.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        Log.i(TAG, "settings onclick for nav item");
        // is this legal?
        // 為navigatin_view設置點擊事件
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.i(TAG, "clicked nav item");
                Fragment fragment = null;

                // 點選時收起選單
                drawerLayout.closeDrawer(GravityCompat.START);
                // 取得選項id
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.nav_home:
                        fragment = new HomeFragment();
                        break;
                    case R.id.nav_records:
                        fragment = new RecordsFragment();
                        break;
                    case R.id.nav_settings:
                        //                    // 按下「使用說明」要做的事
//                    Toast.makeText(MainActivity.this, "使用說明", Toast.LENGTH_SHORT).show();
                        //navigation_view.getCheckedItem().setChecked(false);
                        FragmentManager fm = getSupportFragmentManager();
                        fm.beginTransaction().replace(R.id.nav_host_fragment, new SettingsFragment()).commit();
                        return true;
                }
                if(fragment != null) // maybe always taken?
                {
                    FragmentManager fm = getSupportFragmentManager();
                    Fragment fg = fm.findFragmentById(R.id.nav_home);
                    if(fg != null)
                    {

                    }
                    // 原本的View 必須是 content main的 fragment id
                    fm.beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack(null).commit();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            }
        });
    }
    private String mDeviceName, mDeviceId, mDeviceAddress;
    private String mService, mCharacteristic;
    void InitializeScanner()
    {
        askForPermission();
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        // FIXME:
        //setListAdapter(mLeDeviceListAdapter);
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            Log.e("","bad is null");
            finish();
            return;
        }
        scanLeDevice(true);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.setupCurrentView(view);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                InitializeScanner();
            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navigation_view = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_records, R.id.nav_settings)
                .setDrawerLayout(drawerLayout)
                .build();
        // Navigation page 的容器
        // 快速設定，但是會無法進一步設定個別點擊事件
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigation_view, navController);
        setOnClickForNavBar();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
        InitializeScanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    boolean askForPermission()
    {
        String[] permissions = {Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_PHONE_NUMBERS};
        for(String permission:permissions)
        {

            if(ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED)
            {
                Log.e(TAG, "Permission error"+permission);
                requestPermissions(permissions, 1);
            }
        }
        return true;
    }

    private BluetoothLeScanner bluetoothLeScanner;
    ArrayList<String> totalListViewData = new ArrayList<String>();
    ArrayAdapter listAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;
    boolean mScanning;

    BluetoothManager bluetoothManager;

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            findDevice(device);
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };


    ArrayList<String> GetDevices()
    {
        ArrayList<java.lang.String> allListViewData = new ArrayList<java.lang.String>();
        // TODO:
        allListViewData.add(null);

        return allListViewData;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    Log.e(TAG,"STOP SCANNING");
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, 10000);

            mScanning = true;
            if(mBluetoothAdapter == null)
            {
                Log.e("","Oh no");
                return;
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    // Adapter for holding devices found through scanning.
// Adapter for holding devices found through scanning.
   
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.e("SERVICE", "Android service connected");
            // TODO: assign value to mBluetoothLeService
            // here
            MainActivity.bleserviceValid = true;
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
            Log.e("DIS","disconnected");
            mBluetoothLeService = null;
        }
    };


    public final static String TARGET_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public final static String TARGET_CHAR_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;
        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }
        // Device scan callback.
        private BluetoothAdapter.LeScanCallback mLeScanCallback =
                new BluetoothAdapter.LeScanCallback() {

                    @Override
                    public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                        // edited
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLeDeviceListAdapter.addDevice(device);
                                mLeDeviceListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                };
        // TODO:
        // Filter device and stop scan
        public void addDevice(BluetoothDevice device) {
            if(device.getName() != null )
            {
                bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                String deviceName = device.getName();
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if(pref == null)
                {
                    Toast.makeText(MainActivity.this, "pref is null",Toast.LENGTH_SHORT).show();
                }
                String deviceId = pref.getString("pref_deviceName", "");
                Log.i("deviceId:",deviceId);
                if(deviceName.startsWith(deviceId) && !mLeDevices.contains(device)) {

                    Log.i("DEVICE NAME:",device.getName());
                    mLeDevices.add(device);
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    // edited
                    final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
                    mDeviceName = device.getName();
                    mDeviceAddress = device.getAddress();
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                    intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
                    if (mScanning) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }
                    Log.d(TAG,"Starting");
                    // OLD START ACTIVITY

                            mDeviceAddress = MainActivity.DEVICE_ADDRESS;

                    // Sets up UI references. KATSMIN
                    mGattServicesList = (ExpandableListView) new ExpandableListView(MainActivity.this);

                    Intent gattServiceIntent = new Intent(getBaseContext(), BluetoothLeService.class);
                    Log.e("", "will try to bind");
                    boolean bindresult;
                    getBaseContext().startService(gattServiceIntent);
                    bindresult = getBaseContext().bindService(gattServiceIntent, mServiceConnection, Context.BIND_ABOVE_CLIENT);
                    if (!bindresult) Log.e("", "Cannot bind");
                            }
                        }
                    }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }

    public static class ViewHolder {
        public TextView deviceName;
        public TextView deviceAddress;
    }
    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    private boolean displayGattServices(List<BluetoothGattService> gattServices, Context context)
    {
        if (gattServices == null) return false;
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

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
        {
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
        if(mGattServicesList == null)
        {
            Log.e("NULL","mgslist");
        }
        mGattServicesList.setAdapter(gattServiceAdapter);
        return true;
    }
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                HomeFragment.sendNotification("您已斷線","點這裡來重新連線",true,2,MainActivity.this,MainActivity.class);
                InitializeScanner();
                Snackbar.make(findViewById(R.id.content),"唉呀！你和工作帶失去連線了！",Snackbar.LENGTH_LONG);
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
                    }
                }

                boolean success = displayGattServices(gservices, context);
                if(success)
                {
                    Snackbar.make(findViewById(R.id.content), "已經成功連線",Snackbar.LENGTH_LONG).show();
                }

            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO: displayData
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                if (pref == null) {
                    Toast.makeText(MainActivity.this, "pref is null", Toast.LENGTH_SHORT).show();
                }
                boolean onOff = pref.getBoolean("pref_switch_sendNotify", true);
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                if (onOff) {
                    SenseToBLENotification(data);
                }
            }
        }
    };
    public void SenseToBLENotification(String data) {
        if (data != null) {
            Log.i("DATA:", data);
            HomeFragment.sendNotification("哎呀", "你摔倒了？", true, 0, this, AskIfFallActivity.class);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        try {
            unregisterReceiver(mGattUpdateReceiver);
        } catch (Exception ex) {

        }
        Log.e("REG","registering receiver");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        try
        {
            if(bleserviceValid)
            {
                final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        }
        catch(NullPointerException npe)
        {
            Log.e("NULL","CAUGHT NULL BLE");
            InitializeScanner();
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
        Log.e("DES","destroyed");
        try
        {
            unbindService(mServiceConnection);
        }
        catch(IllegalArgumentException ex)
        {

        }
        mBluetoothLeService = null;
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
    public void onBackPressed()
    {

        AskIfFallActivity.displayDialog(this, "請勿關閉此程式","若您確定要與工作帶斷線，請在多工處理把此程式滑掉。"," 好","繼續使用");

    }
}
