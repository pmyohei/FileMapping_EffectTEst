package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;

public class MapListActivity extends AppCompatActivity {

    private int mMapPid;
    private ArrayList<MapTable> mMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        Context context = this;

        //マップ情報取得
        AsyncReadMapsOperaion db = new AsyncReadMapsOperaion(this, new AsyncReadMapsOperaion.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(ArrayList<MapTable> maps) {

                mMaps = maps;

                //レイアウトからリストビューを取得
                RecyclerView rv_mapList = findViewById(R.id.rv_mapList);

                //アダプタの生成
                MapListAdapter adapter = new MapListAdapter(mMaps);

                //アダプタの設定
                rv_mapList.setAdapter(adapter);

                //レイアウトマネージャの設定
                rv_mapList.setLayoutManager( new LinearLayoutManager(context) );
            }
        });

        //非同期処理開始
        db.execute();




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