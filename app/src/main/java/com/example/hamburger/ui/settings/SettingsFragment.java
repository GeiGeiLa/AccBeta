package com.example.hamburger.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamburger.AskIfFallActivity;
import com.example.hamburger.DummyActivity;
import com.example.hamburger.MainActivity;
import com.example.hamburger.MyAdapter;
import com.example.hamburger.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class SettingsFragment extends PreferenceFragmentCompat
{
    private ArrayList<String> mData = new ArrayList<>();
    private View currentView;
    EditTextPreference pref_phoneno;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Indicate here the XML resource you created above that holds the preferences
        addPreferencesFromResource(R.xml.preferences);
        pref_phoneno = findPreference("pref_text_yourContact");
        pref_phoneno.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newno = (String) newValue;
                Log.i("old PHONENO",pref_phoneno.getText());
                Log.i("new PHONENO",newno);
                boolean legal = true;
                for(char c: newno.toCharArray())
                {
                    if(!Character.isDigit(c))
                    {
                        legal = false;
                        break;
                    }
                }
                if(newno.length() != 10 || !legal )
                {
                    Log.e("Wrong value0","phoneno");
                    AskIfFallActivity.DisplayDiaglog(getContext(),"電話號碼格式錯誤","已還原成剛剛的號碼","OK","Close");
                    return false;
                }
                pref_phoneno.setText(newno);

                return true;
            }
        });
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        this.currentView = root;
        Preference preference = findPreference("btn_test");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), DummyActivity.class);
                startActivity(intent);
                return false;
            }
        });
        return root;
    }

}
