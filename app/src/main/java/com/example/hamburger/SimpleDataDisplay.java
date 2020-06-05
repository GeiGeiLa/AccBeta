package com.example.hamburger;


/**
 * @author 許劼忞
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.FingerprintGestureController;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

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


public class SimpleDataDisplay extends AppCompatActivity implements
        OnChartValueSelectedListener{
    int year, month, day, hour, minute, second;
    int ax, ay, az;
    public static int counter = 0;
    public static int ccc=  0;
    List<Integer> AX = new ArrayList<Integer>();
    List<Integer> AY = new ArrayList<Integer>();
    List<Integer> AZ = new ArrayList<Integer>();
    TextView tv_data;
    Calendar calendar;
    LineChart chart;
    private final int X_ANIMATION_PERIOD = 750;
    private final int WINDOW_SIZE = 125;
    private final int DEVICE_FREQUENCY = 25;
    private final int REFRESH_PERIOD = WINDOW_SIZE / DEVICE_FREQUENCY;
    private final int OFFSET = -955;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                // Show all the supported services and characteristics on the user interface.
//                List<BluetoothGattService> gservices = mBluetoothLeService.getSupportedGattServices();
//                for(BluetoothGattService gs: gservices)
//                {
//                    Log.d(TAG,"gs:"+gs.getUuid().toString());
//                }
//                displayGattServices(gservices);
//
//            }
//            else
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO: displayData
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private int ZaxisData(String data)
    {
        String s = "";
        int i;
        for(i = 0; i < data.length(); i++)
        {
            if(data.charAt(i)!= '\n')
            {
                s += data.charAt(i);
            }
        }
        if(s == "") return -1;
        for(i = s.length()-1; i >= 0; i--)
        {
            if(s.charAt(i) == ',')
            {
                return Integer.valueOf(s.substring(i+2));
            }
        }
        return -1;
    }
    // 解析data
    private void displayData(String data) {
        if (data != null) {
            Log.e("EE", data);
            ccc = 0;
            String time = data.substring(11, 19);
            String acc = data.substring(21);
            String[] accs = acc.split(", ");
            String[] times = time.split(":");
            year = Integer.valueOf(data.substring(0, 4));
            month = Integer.valueOf(data.substring(5, 7));
            day = Integer.valueOf(data.substring(8, 10));
            hour = Integer.valueOf(times[0]);
            minute = Integer.valueOf(times[1]);
            second = Integer.valueOf(times[2]);


            ax = Integer.valueOf(accs[0]);
            ay = Integer.valueOf(accs[1]);
            az = Integer.valueOf(ZaxisData(data));
            //Log.e("PARSING",year + " "+ month+ " "+ day+ " "+hour+ " " + minute+ " "+second) ;
            tv_data.setText(data);
            counter++;

            Log.d("", ax+" "+accs[0]);
            Log.d("", ay+" "+accs[1]);
            Log.d("", az+" "+accs[2]);
            AX.add(ax);
            AY.add(ay);
            AZ.add(az);
            if (counter == WINDOW_SIZE)
            {
                setData(REFRESH_PERIOD, REFRESH_PERIOD);

                // draw points over time
                chart.animateX(X_ANIMATION_PERIOD);

                // get the legend (only possible after setting data)
                Legend l = chart.getLegend();

                // draw legend entries as lines
                l.setForm(Legend.LegendForm.LINE);
                AX.clear();
                AY.clear();
                AZ.clear();
                counter = 0;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

//    private SeekBar seekBarX, seekBarY;
//    private TextView tvX, tvY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_simple_data_display);
        tv_data = findViewById(R.id.tv_notification);
        setTitle("過去"+REFRESH_PERIOD+"秒的結果");

        Log.e("", "Into line chart");
        {   // // Chart Style // //
            chart = findViewById(R.id.mLineChart);
            // background color
            chart.setBackgroundColor(Color.WHITE);
            // disable description text
            chart.getDescription().setEnabled(false);
            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            //chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);

            // create marker to display box when values are selected
            //MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);

            // Set the marker to the chart
//            mv.setChartView(chart);
//            chart.setMarker(mv);

            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);
            // force pinch zoom along both axis
            chart.setPinchZoom(false);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(5f, 5f, 0f);

            // axis range
            yAxis.setAxisMaximum(4000);
            yAxis.setAxisMinimum(-10);
        }


//        {   // // Create Limit Lines // //
//            LimitLine llXAxis = new LimitLine(9f, "Index 10");
//            llXAxis.setLineWidth(2f);
//            llXAxis.enableDashedLine(10f, 10f, 0f);
//            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//            llXAxis.setTextSize(10f);
////            llXAxis.setTypeface(tfRegular);
//
//            LimitLine ll1 = new LimitLine(150f, "Upper Limit");
//            ll1.setLineWidth(4f);
//            ll1.enableDashedLine(10f, 10f, 0f);
//            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//            ll1.setTextSize(10f);
////            ll1.setTypeface(tfRegular);
//
//            LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//            ll2.setLineWidth(4f);
//            ll2.enableDashedLine(10f, 10f, 0f);
//            ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//            ll2.setTextSize(10f);
////            ll2.setTypeface(tfRegular);
//
//            // draw limit lines behind data instead of on top
//            yAxis.setDrawLimitLinesBehindData(true);
//            xAxis.setDrawLimitLinesBehindData(true);
//
//
//            // add limit lines
//            yAxis.addLimitLine(ll1);
//            yAxis.addLimitLine(ll2);
//            yAxis.setAxisLineWidth(5f);
//            //xAxis.addLimitLine(llXAxis);
//        }

        // add data
//        seekBarX.setProgress(45);
//        seekBarY.setProgress(180);
//        setData(7, 7);
//
//        // draw points over time
//        chart.animateX(1500);
//
//        // get the legend (only possible after setting data)
//        Legend l = chart.getLegend();
//
//        // draw legend entries as lines
//        l.setForm(Legend.LegendForm.LINE);
    }
    // this is how we provide data to chart
    private void setData(int count, float range) {

        try {
            ArrayList<Entry> values = new ArrayList<>();

            for (int i = 0; i < AX.size(); i++) {
                // modify my code here
                // square sum and take root
                float val = (float) sqrt( AX.get(i)*AX.get(i)+AY.get(i)*AY.get(i)+ AZ.get(i)*AZ.get(i)) + OFFSET;
                val = abs(val);
                Log.e("","Output value:" + val);
                values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
            }

            LineDataSet set1;

            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
                set1.setValues(values);
                set1.notifyDataSetChanged();
                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
            } else {
                // create a dataset and give it a type
                set1 = new LineDataSet(values, "活動量");

                set1.setDrawIcons(false);

                // draw dashed line
                set1.enableDashedLine(10f, 5f, 0f);

                // black lines and points
                set1.setColor(Color.BLACK);
                set1.setCircleColor(Color.BLACK);

                // line thickness and point size
                set1.setLineWidth(1f);
                set1.setCircleRadius(3f);

                // draw points as solid circles
                set1.setDrawCircleHole(false);

                // customize legend entry
                set1.setFormLineWidth(1f);
                set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
                set1.setFormSize(15.f);

                // text size of values
                set1.setValueTextSize(9f);

                // draw selection line as dashed
                set1.enableDashedHighlightLine(10f, 5f, 0f);

                // set the filled area
                set1.setDrawFilled(true);
                set1.setFillFormatter(new IFillFormatter() {
                    @Override
                    public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                        return chart.getAxisLeft().getAxisMinimum();
                    }
                });

                // set color of filled area
                if (Utils.getSDKInt() >= 18) {
                    // drawables only supported on api level 18 and above
                    Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                    set1.setFillDrawable(drawable);
                } else {
                    set1.setFillColor(Color.BLACK);
                }

                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(set1); // add the data sets

                // create a data object with the data sets
                LineData data = new LineData(dataSets);

                // set data
                chart.setData(data);
            }
        }
        catch(Exception ex)
        {

            StringWriter e = new StringWriter();
            ex.printStackTrace(new PrintWriter(e));
            Toast toast= Toast. makeText(getApplicationContext(), Thread.currentThread().getStackTrace().toString(),Toast. LENGTH_SHORT);
            toast.show();
        }

    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.line, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.viewGithub: {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/LineChartActivity1.java"));
//                startActivity(i);
//                break;
//            }
//            case R.id.actionToggleValues: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    set.setDrawValues(!set.isDrawValuesEnabled());
//                }
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleIcons: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    set.setDrawIcons(!set.isDrawIconsEnabled());
//                }
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleHighlight: {
//                if (chart.getData() != null) {
//                    chart.getData().setHighlightEnabled(!chart.getData().isHighlightEnabled());
//                    chart.invalidate();
//                }
//                break;
//            }
//            case R.id.actionToggleFilled: {
//
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    if (set.isDrawFilledEnabled())
//                        set.setDrawFilled(false);
//                    else
//                        set.setDrawFilled(true);
//                }
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleCircles: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    if (set.isDrawCirclesEnabled())
//                        set.setDrawCircles(false);
//                    else
//                        set.setDrawCircles(true);
//                }
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleCubic: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
//                            ? LineDataSet.Mode.LINEAR
//                            : LineDataSet.Mode.CUBIC_BEZIER);
//                }
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleStepped: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    set.setMode(set.getMode() == LineDataSet.Mode.STEPPED
//                            ? LineDataSet.Mode.LINEAR
//                            : LineDataSet.Mode.STEPPED);
//                }
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleHorizontalCubic: {
//                List<ILineDataSet> sets = chart.getData()
//                        .getDataSets();
//
//                for (ILineDataSet iSet : sets) {
//
//                    LineDataSet set = (LineDataSet) iSet;
//                    set.setMode(set.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
//                            ? LineDataSet.Mode.LINEAR
//                            : LineDataSet.Mode.HORIZONTAL_BEZIER);
//                }
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionTogglePinch: {
//                if (chart.isPinchZoomEnabled())
//                    chart.setPinchZoom(false);
//                else
//                    chart.setPinchZoom(true);
//
//                chart.invalidate();
//                break;
//            }
//            case R.id.actionToggleAutoScaleMinMax: {
//                chart.setAutoScaleMinMaxEnabled(!chart.isAutoScaleMinMaxEnabled());
//                chart.notifyDataSetChanged();
//                break;
//            }
//            case R.id.animateX: {
//                chart.animateX(2000);
//                break;
//            }
//            case R.id.animateY: {
//                chart.animateY(2000, Easing.EaseInCubic);
//                break;
//            }
//            case R.id.animateXY: {
//                chart.animateXY(2000, 2000);
//                break;
//            }
//            case R.id.actionSave: {
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    saveToGallery();
//                } else {
//                    requestStoragePermission(chart);
//                }
//                break;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//
//        tvX.setText(String.valueOf(seekBarX.getProgress()));
//        tvY.setText(String.valueOf(seekBarY.getProgress()));
//
//        setData(seekBarX.getProgress(), seekBarY.getProgress());
//
//        // redraw
//        chart.invalidate();
//    }

//    @Override
//    protected void saveToGallery() {
//        saveToGallery(chart, "LineChartActivity1");
//    }
//
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOW HIGH", "low: " + chart.getLowestVisibleX() + ", high: " + chart.getHighestVisibleX());
        Log.i("MIN MAX", "xMin: " + chart.getXChartMin() + ", xMax: " + chart.getXChartMax() + ", yMin: " + chart.getYChartMin() + ", yMax: " + chart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    // 需要修改 token
    class C2TL extends AsyncTask<String , Void, String>
    {
        private static final String TAG = "Chinese2TaiL";
        private static final String token = "eyJhbGciOiJSUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJ3bW1rcy5jc2llLmVkdS50dyIsInNlcnZpY2VfaWQiOiI5IiwibmJmIjoxNTc2MzA2NTM3LCJzY29wZXMiOiIwIiwidXNlcl9pZCI6Ijg4IiwiaXNzIjoiSldUIiwidmVyIjowLjEsImlhdCI6MTU3NjMwNjUzNywic3ViIjoiIiwiaWQiOjE2NywiZXhwIjoxNjM5Mzc4NTM3fQ.BxQuz6inWV25fJlzJLpdVyyFxMDVDqAULI8_tGt7-XC0wRWzL4tjVkdg33TkEitU9Z1U4FeUWN7AEajKyQXPaO_JQCO70NyD0nZu85QzZ3Ha3n2-n64LQgiGi1cn2jTQZahhaTTAZP0V5y5BjfAJwBxQFu4yJUsSdC8ucA0-yoo";
        private String result=null;

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG,strings[0]);

            String outmsg = token + "@@@" + strings[0];

            Socket socket = new Socket();
            InetSocketAddress isa = new InetSocketAddress("140.116.245.149", 27005);

            try {
                //將outmsg轉成byte[]
                byte[] token_et_s = outmsg.getBytes();
                //用於計算outmsg的byte數
                byte[] g = new byte[4];

                g[0] = (byte) ((token_et_s.length & 0xff000000) >>> 24);
                g[1] = (byte) ((token_et_s.length & 0x00ff0000) >>> 16);
                g[2] = (byte) ((token_et_s.length & 0x0000ff00) >>> 8);
                g[3] = (byte) ((token_et_s.length & 0x000000ff));

                socket.connect(isa, 10000);

                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                // 送出字串
                out.write(byteconcate(g, token_et_s));
                out.flush();

                // 接收字串
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                byte[] b = new byte[1024];
                while (in.read(b) > 0)// <=0的話就是結束了
                    result = new String(b, Charset.forName("UTF-8"));
                out.close();
                in.close();
                socket.close();
                return result;

            } catch (IOException ex) {
                Log.e(TAG, "doInBackground: request failed", ex);
                return ex.getMessage();
            } catch (NullPointerException ex) {
                Log.e(TAG, "doInBackground: received empty response", ex);
                return ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!=null)
            {
                Log.d(TAG,Integer.toString(s.length()));
                Log.d(TAG,s);
                String[] convert = s.split("\u0000");   //切除亂碼
                Log.d(TAG,convert[0]);
                // resultText.setText(convert[0]);
                return;
            }
        }

        private byte[] byteconcate(byte[] a, byte[] b)
        {
            byte[] result = new byte[a.length + b.length];
            System.arraycopy(a, 0, result, 0, a.length);
            System.arraycopy(b, 0, result, a.length, b.length);
            return result;
        }
    }
    class TVL extends AsyncTask<String , Void, String>
    {
        private static final String TAG = "TaiwaneseVoice";
        private String result=null;

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG,strings[0]);


            String outmsg = strings[0];

            Socket socket = new Socket();
            InetSocketAddress isa = new InetSocketAddress("140.116.245.147", 50006);

            try {
                //將outmsg轉成byte[]
                byte[] token_et_s = outmsg.getBytes(StandardCharsets.UTF_8);
                //用於計算outmsg的byte數

                socket.connect(isa, 10000);

                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                // 送出字串
                out.write(token_et_s);
                out.flush();

                // 接收
                BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                String path = getCacheDir() + "/output.wav";//"/storage/emulated/0/DCIM/output.wav";
                FileOutputStream fos = new FileOutputStream(path);
                byte[] b = new byte[1024];
                int count = 0 ;
                while ((count = in.read(b)) > 0)// <=0的話就是結束了
                {
                    Log.d("byte length : ", Integer.toString(b.length));
                    fos.write(b, 0, count);

                }

                out.close();
                in.close();
                socket.close();
                result = path;
                return result;

            } catch (IOException ex) {
                Log.e(TAG, "doInBackground: request failed", ex);
                return ex.getMessage();
            } catch (NullPointerException ex) {
                Log.e(TAG, "doInBackground: received empty response", ex);
                return ex.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!=null)
            {
                Log.d(TAG,Integer.toString(s.length()));
                Log.d(TAG,s);
                // resultText.setText(s);

                final MediaPlayer mediaPlayer;
                mediaPlayer = MediaPlayer.create(SimpleDataDisplay.this, Uri.parse(s));
                Log.d(TAG,s);
                mediaPlayer.start();
                Log.d(TAG,s);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer MP) {
                        mediaPlayer.release();
                    }
                });

                return;
            }
        }

        private byte[] byteconcate(byte[] a, byte[] b)
        {
            byte[] result = new byte[a.length + b.length];
            System.arraycopy(a, 0, result, 0, a.length);
            System.arraycopy(b, 0, result, a.length, b.length);
            return result;
        }
    }

}