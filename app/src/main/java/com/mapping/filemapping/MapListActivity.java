package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

public class MapListActivity extends AppCompatActivity {

    /* 画面遷移-キー */
    public static String KEY_ISCREATE = "isCreate";
    public static String KEY_MAP = "map";               //マップ


    private int mMapPid;
    private ArrayList<MapTable> mMaps;
    private MapListAdapter mMapListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        //システムバーの色を設定
        getWindow().setStatusBarColor( Color.BLACK );

        //画面遷移ランチャー（マップ新規生成用）
        ActivityResultLauncher<Intent> createMapLauncher =
                registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new CreateMapResultCallback());
        //画面遷移ランチャー（マップ編集用）
        ActivityResultLauncher<Intent> editMapLauncher =
                registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new EditMapResultCallback());


        //AdMob初期化
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                //※本画面では何もしない
            }
        });

        //コンテキスト
        Context context = this;

        //マップ情報取得
        AsyncReadMaps db = new AsyncReadMaps(this, new AsyncReadMaps.OnReadListener() {
            @Override
            public void onRead(ArrayList<MapTable> maps) {

                //DBからの取得結果を保持
                mMaps = maps;

                //レイアウトからリストビューを取得
                RecyclerView rv_mapList = findViewById(R.id.rv_mapList);
                //アダプタ設定
                mMapListAdapter = new MapListAdapter(mMaps, editMapLauncher);
                rv_mapList.setAdapter(mMapListAdapter);
                rv_mapList.setLayoutManager(new LinearLayoutManager(context));

                //リサイクラービューの上下にスペースを設定
                rv_mapList.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                        super.getItemOffsets(outRect, view, parent, state);

                        int position = parent.getChildAdapterPosition(view);
                        //先頭と最後尾に適当な大きさのスペースを設定
                        //※以下の値は適当（ちょうどボタン分の高さには別にしない。）
                        if (position == 0) {
                            outRect.top = 200;
                        } else if (position == state.getItemCount() - 1) {
                            outRect.bottom = 300;
                        }
                    }
                });
            }
        });
        //非同期処理開始
        db.execute();

        //Createリスナー
        findViewById(R.id.tv_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapListActivity.this, MapEntryActivity.class);
                intent.putExtra(KEY_ISCREATE, true );

                createMapLauncher.launch( intent );
            }
        });


/*        //疑似-動作確認用----------------------------------------------------------------
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

                        //mMapListAdapter.notifyDataSetChanged();
                        mMapListAdapter.notifyAll();
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

                //mMapListAdapter.notifyAll();
            }
        });
        //-----------------------------------------------------------------*/

    }




    /*
     * 画面遷移からの戻りのコールバック通知ーマップ新規生成
     */
    private class CreateMapResultCallback implements ActivityResultCallback<ActivityResult>{

        /*
         * 画面遷移先からの戻り処理
         */
        @Override
        public void onActivityResult(ActivityResult result) {

            //インテント
            Intent intent = result.getData();
            //リザルトコード
            int resultCode = result.getResultCode();

            //マップ新規作成結果
            if(resultCode == MapEntryActivity.RESULT_CREATED) {

                //マップ入力画面からデータを受け取る
                MapTable map = (MapTable) intent.getSerializableExtra(MapEntryActivity.KEY_MAP);
                Log.i("MapListActivity", "新規生成 map=" + map.getMapName());

                //マップリストアダプタに追加通知
                mMaps.add( map );
                mMapListAdapter.notifyItemInserted( mMaps.size() - 1 );

                //マップ画面へ遷移
                intent = new Intent(MapListActivity.this, MapActivity.class);
                //intent.putExtra(ResourceManager.KEY_MAPID, map.getPid());
                intent.putExtra(KEY_MAP, map );
                intent.putExtra(ResourceManager.KEY_NEW_MAP, true);

                Log.i("Map", "マップ生成完了。マップ画面へ");

                startActivity(intent);
            }
        }
    }

    /*
     * 画面遷移からの戻りのコールバック通知ーマップ編集
     */
    private class EditMapResultCallback implements ActivityResultCallback<ActivityResult>{

        /*
         * 画面遷移先からの戻り処理
         */
        @Override
        public void onActivityResult(ActivityResult result) {

            //インテント
            Intent intent = result.getData();
            //リザルトコード
            int resultCode = result.getResultCode();

            //マップ編集結果
            if( resultCode == MapEntryActivity.RESULT_EDITED) {

                //マップ入力画面からデータを受け取る
                MapTable map = (MapTable) intent.getSerializableExtra(MapEntryActivity.KEY_MAP);
                Log.i("MapListActivity", "編集 map=" + map.getMapName());

                //リスト上のマップを更新
                int i = 0;
                for( MapTable mapData: mMaps ){
                    if( mapData.getPid() == map.getPid() ){
                        mapData.setMapName( map.getMapName() );
                        break;
                    }
                    i++;
                }

                //アダプタに変更通知
                mMapListAdapter.notifyItemChanged( i );
            }
        }
    }
}