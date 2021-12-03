package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MapListActivity extends AppCompatActivity {

    private int mMapPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);


        //仮；画面遷移
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapListActivity.this, MapActivity.class);
                intent.putExtra("MapID", mMapPid);

                startActivity(intent);
            }
        });

        //仮：疑似データ生成
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //疑似データ生成
                new GIJI_AsyncCreateDBOperaion(view.getContext(), false, new GIJI_AsyncCreateDBOperaion.OnCreateListener() {

                    @Override
                    public void onCreate(int mapPid) {
                        mMapPid = mapPid;
                    }
                }).execute();
            }
        });

        //仮：疑似データ削除
        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //疑似データ削除
                new GIJI_AsyncCreateDBOperaion(view.getContext(), true).execute();
            }
        });




    }
}