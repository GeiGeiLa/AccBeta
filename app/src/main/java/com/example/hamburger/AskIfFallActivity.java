package com.example.hamburger;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.hamburger.ui.settings.SettingsFragment;
import com.google.android.material.snackbar.Snackbar;

public class AskIfFallActivity extends AppCompatActivity {
    Button btn_good, btn_sos;
    private static Context currentContext;
    @Override
    public void onCreate(Bundle sis)
    {
        super.onCreate(sis);
        AskIfFallActivity.currentContext = getApplicationContext();
        if(currentContext == null)
        {
            Log.e("NULL:","curentContext");
        }
        setContentView(R.layout.activity_confirmfall);
        btn_good = findViewById(R.id.btn_good);
        btn_sos = findViewById(R.id.btn_sos);
        btn_sos.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // I can't trust this, but I must!
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                if(pref == null)
                {
                    Toast.makeText(v.getContext(), "pref is null",Toast.LENGTH_SHORT).show();
                }
                String phoneNo = pref.getString("pref_text_yourContact", null);
                Log.i("phoneno:",phoneNo);
                /**
                 * broadcast ref site:
                 * https://stackoverflow.com/questions/9520277/practical-way-to-find-out-if-sms-has-been-sent
                 *
                 */
                try {
                    TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                    int sim_state = tm.getSimState();
                    if(sim_state != TelephonyManager.SIM_STATE_READY)
                    {
                        throw new Exception("Sim is not available");
                    }
                    String SENT = "SMS_SENT";
                    String DELIVERED = "SMS_DELIVERED";

                    PendingIntent sentPI = PendingIntent.getBroadcast(AskIfFallActivity.this, 0,
                            new Intent(SENT), 0);

                    PendingIntent deliveredPI = PendingIntent.getBroadcast(AskIfFallActivity.this,
                            0, new Intent(DELIVERED), 0);

                    // ---when the SMS has been sent---
                    final String string = "deprecation";
                    registerReceiver(new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode()) {
                                case Activity.RESULT_OK:
                                    Toast.makeText(AskIfFallActivity.this, "SMS sent",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    Toast.makeText(AskIfFallActivity.this, "Generic failure",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    Toast.makeText(AskIfFallActivity.this, "No service",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    Toast.makeText(AskIfFallActivity.this, "Null PDU",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    Toast.makeText(getBaseContext(), "Radio off",
                                            Toast.LENGTH_SHORT).show();
                                    break;

                            }
                        }
                    }, new IntentFilter(SENT));

                    // ---when the SMS has been delivered---
                    registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode()) {
                                case Activity.RESULT_OK:
                                    Toast.makeText(AskIfFallActivity.this, "SMS delivered",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                                case Activity.RESULT_CANCELED:
                                    Toast.makeText(AskIfFallActivity.this, "SMS not delivered",
                                            Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }, new IntentFilter(DELIVERED));

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, "老爸老媽好！", sentPI, deliveredPI);
                    Snackbar.make(v,"以季送通知！",Snackbar.LENGTH_LONG).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "沒有插入 SIM 卡！",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }


            }
        });
    }
    public static Context getCurrentContext()
    {
        return AskIfFallActivity.currentContext;
    }

    public void DisplayDiaglog()
    {
        new AlertDialog.Builder(AskIfFallActivity.this)
                .setTitle("一個Dialog")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("cancel",null).create()
                .show();
    }

}
