package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;

public class MapListActivity extends AppCompatActivity {

    /* 画面遷移-キー */
    public static String KEY_MAP = "map";               //マップ
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_CREATED = 100;
    public static final int RESULT_EDITED = 101;
    //許可リクエストコード
    public static final int REQUEST_EXTERNAL_STORAGE = 1;

    //
    private ArrayList<MapTable> mMaps;
    //
    private MapListAdapter mMapListAdapter;
    //編集対象マップのリスト位置
    private int mEditPosition;
    //マップ編集ランチャー
    ActivityResultLauncher<Intent> mEditMapLauncher;

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
        mEditMapLauncher =
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
                mMapListAdapter = new MapListAdapter(mMaps);
                rv_mapList.setAdapter(mMapListAdapter);
                rv_mapList.setLayoutManager(new LinearLayoutManager(context));

                //リスナー設定：マップオープン
                mMapListAdapter.setOpenMapListener(new MapListAdapter.openMapListener() {
                    @Override
                    public void onOpenMap(MapTable map) {
                        //指定マップを開く
                        openMap(map, false);
                    }
                });

                //リスナー設定：編集
                mMapListAdapter.setEditMapListener(new MapListAdapter.editMapListener() {
                    @Override
                    public void onEditMap(MapTable map, int index) {
                        editMap( map, index );
                    }
                });

                //リスナー設定：削除
                mMapListAdapter.setDeleteMapListener(new MapListAdapter.deleteMapListener() {
                    @Override
                    public void onDeleteMap(MapTable map, int index) {
                        removeMap( map, index );
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
                Intent intent = new Intent(MapListActivity.this, MapCreateActivity.class);
                createMapLauncher.launch(intent);
            }
        });

        //権限の確認
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //権限付与
            permissionsStorage();
        } else {
            //権限ありなら、即ヘルプダイアログの表示
            showFirstLaunchDialog();
        }
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
        AlertDialog dialog = new AlertDialog.Builder(this)
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

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.SERIF);
    }

    /*
     * マップ画面へ遷移
     */
    private void openMap(MapTable map, boolean isNew) {

        MapCommonData commonData = (MapCommonData) getApplication();
        //マップ情報
        commonData.setMap(map);

        //マップ画面へ遷移
        Intent intent = new Intent(MapListActivity.this, MapActivity.class);
        intent.putExtra(ResourceManager.KEY_NEW_MAP, isNew);
        startActivity(intent);
    }

    /*
     * マップ情報の編集
     */
    private void editMap( MapTable map, int index ){

        //Log.i("indexテスト", "index=" + index);

        //編集対象の位置を保持
        mEditPosition = index;

        //画面遷移
        Intent intent = new Intent(MapListActivity.this, MapEditActivity.class);
        intent.putExtra(MapListActivity.KEY_MAP, map);

        mEditMapLauncher.launch(intent);
    }

    /*
     * マップの削除
     */
    private void removeMap( MapTable map, int index ){

        Context context = this;

        //削除確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle( getString(R.string.alert_map_delete_title))
                .setMessage( getString(R.string.alert_map_delete_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //DBから自マップ削除
                        AsyncDeleteMap db = new AsyncDeleteMap(context, map, new AsyncDeleteMap.OnFinishListener() {
                            @Override
                            public void onFinish() {
                                //リストから削除して、アダプタを更新
                                mMaps.remove(index);
                                mMapListAdapter.notifyItemRemoved(index);
                            }
                        });
                        //非同期処理開始
                        db.execute();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.SERIF);
    }


    /*
     * 権限付与
     */
    private void permissionsStorage() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //API23未満なら、許可ダイアログは不要
            return;
        }

        //API23以降は、許可ダイアログ必須
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        //許可されていなければ、許可を要求
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    /*
     * 権限許可ダイアログの処理結果コールバック
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);

        if (grantResults.length <= 0) {
            return;
        }

        switch (requestCode) {
            //ピクチャノード生成時のリクエストコード
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do nothing
                } else {
                    //マップ上に画像を表示したい場合、許可に変更し
                    Toast.makeText(this, getString(R.string.toast_qeuestPermissionFirst), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }

        //ダイアログを表示
        showFirstLaunchDialog();
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
                MapTable map = (MapTable) intent.getSerializableExtra(MapCreateActivity.KEY_MAP);

                //マップリストアダプタに追加通知
                mMaps.add( map );
                mMapListAdapter.notifyItemInserted( mMaps.size() - 1 );

                //マップ画面を開く
                openMap( map, true );
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
                //編集されたマップを取得
                MapTable editMap = (MapTable) intent.getSerializableExtra(MapCreateActivity.KEY_MAP);

                //リストを更新して、アダプタに変更通知
                mMaps.set( mEditPosition, editMap );
                mMapListAdapter.notifyItemChanged( mEditPosition );
            }

        }
    }
}