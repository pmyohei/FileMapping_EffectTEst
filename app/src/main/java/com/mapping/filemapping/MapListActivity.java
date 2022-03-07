package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_CREATED = 100;
    public static final int RESULT_EDITED = 101;

    private ArrayList<MapTable> mMaps;
    private MapListAdapter mMapListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        //システムバーの色を設定
        getWindow().setStatusBarColor(Color.BLACK);

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

                //リスナー設定：マップオープン
                mMapListAdapter.setOpenMapListener(new MapListAdapter.openMapListener() {
                    @Override
                    public void onOpenMap(MapTable map) {
                        //指定マップを開く
                        openMap( map );
                    }
                });



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
                intent.putExtra(KEY_ISCREATE, true);

                createMapLauncher.launch(intent);
            }
        });

        //ヘルプダイアログの表示
        showFirstLaunchDialog();


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
     * 初回起動時のヘルプダイアログを表示
     */
    private void showFirstLaunchDialog() {

        final String key = ResourceManager.SHARED_KEY_HELP_ON_MAPLIST;

        //表示の有無を取得
        SharedPreferences spData = getSharedPreferences(ResourceManager.SHARED_DATA_NAME, MODE_PRIVATE);
        boolean isShow = spData.getBoolean(key, ResourceManager.INVALID_SHOW_HELP);

        if (!isShow) {
            //表示なしが選択されていれば何もしない
            return;
        }

        //ガイドダイアログを表示
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_launch_mapList_title))
                .setMessage(getString(R.string.alert_launch_mapList_message))
                .setPositiveButton(getString(R.string.do_not_show_this_message), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = spData.edit();
                        editor.putBoolean(key, false);
                        editor.apply();
                    }
                })
                //.setNegativeButton("Cancel", null)
                .show();
    }

    /*
     * マップ画面へ遷移
     */
    private void openMap( MapTable map ){

        MapCommonData commonData = (MapCommonData)getApplication();
        //初期化
        commonData.init();
        //マップ情報
        commonData.setMap( map );

        //マップ画面へ遷移
        Intent intent = new Intent(MapListActivity.this, MapActivity.class);
        startActivity(intent);
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
            if(resultCode == RESULT_CREATED) {

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

            //マップ
            if( resultCode == RESULT_EDITED) {
                //

            }






            //マップ編集結果
            if( resultCode == RESULT_EDITED) {

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