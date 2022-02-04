package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class PictureGalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_gallery);

        //選択ノードを取得
        Intent intent = getIntent();
        int nodePid = intent.getIntExtra( MapActivity.INTENT_NODE_PID, 0 );
        if( nodePid == 0 ){
            //ノード取得エラーはこのまま本画面を終了する
            //★
            Toast.makeText( this, "エラー", Toast.LENGTH_SHORT ).show();
            finish();
        }

        //指定ノード配下のピクチャノードをリスト化
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        List<Integer> pictureNodes = mapCommonData.getNodes().getPictureNodes( nodePid );
        int mapPid = mapCommonData.getMapPid();

        Log.i("ギャラリー確認", "ルートチェック1");
        for( Integer bbb: pictureNodes ){
            Log.i("ギャラリー確認", "getPictureNodes=" + bbb);
        }

        //対象ピクチャを取得
        AsyncReadGallery db = new AsyncReadGallery(this, mapPid, pictureNodes, new AsyncReadGallery.OnFinishListener() {
            //DB読み取り完了
            @Override
            public void onFinish( PictureArrayList<PictureTable> pictures ) {

                Log.i("ギャラリー確認", "ルートチェック2");
                for( PictureTable aa: pictures ){
                    Log.i("ギャラリー確認", "DB読み込み=" + aa.getPath());
                }

                //画像を表示
                RecyclerView rc_gallery = findViewById(R.id.rc_gallery);
                rc_gallery.setAdapter( new GalleryAdapter( pictures ) );
                rc_gallery.setLayoutManager(new GridLayoutManager(rc_gallery.getContext(), 2));
            }
        });

        //非同期処理開始
        db.execute();



    }
}