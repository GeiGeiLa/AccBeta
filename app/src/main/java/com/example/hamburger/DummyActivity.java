package com.example.hamburger;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hamburger.ui.home.HomeFragment;
import com.example.hamburger.ui.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class DummyActivity extends AppCompatActivity {
    private ArrayList<String> mData = new ArrayList<>();
    private ArrayList<String> sData = new ArrayList<>();
    private RecyclerView recycler_view;
    MyAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        Intent intent = getIntent();
        if(intent != null)
        {

        }
        else
        {

        }
        InitAdpater();
    }

    void InitAdpater()
    {
        final int size = 5;
        // 準備資料，塞50個項目到ArrayList裡
        for(int i = 0; i < size; i++) {
            mData.add("項目 "+i);
        }
        for(int i = 0; i < size; i++)
        {
            sData.add("子項目 "+i);

        }
        // 連結元件
        // 設置RecyclerView為列表型態
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        // 設置格線
//        recycler_view.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // 將資料交給adapter
        adapter = new MyAdapter(mData, sData);
        // 設置adapter給recycler_view
        recycler_view.setAdapter(adapter);
    }
}
