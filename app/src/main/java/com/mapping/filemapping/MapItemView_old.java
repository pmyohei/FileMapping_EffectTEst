package com.mapping.filemapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

public class MapItemView_old extends LinearLayout {

    //画面遷移ランチャー
    ActivityResultLauncher<Intent> mStartForResult;

    //自身のマップ情報
    MapTable mMap;

    MapListAdapter mAdapter;

    /*
     *  コンストラクタ
     */
    public MapItemView_old(Context context, MapTable map, MapListAdapter adapter) {
        super(context);

        //マップ情報
        mMap = map;
        mAdapter = adapter;

        //レイアウトを割り当て
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.map_item, this, true);

        //マップ新規作成・編集画面遷移ランチャー
        //※クリックリスナー内で定義しないこと！（ライフサイクルの関係でエラーになるため）
/*        mStartForResult = ((ComponentActivity)context).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    *//*
                     * 画面遷移先からの戻り処理
                     *//*
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("MapItemView", "onActivityResult()");

                        //インテント
                        Intent intent = result.getData();
                        //リザルトコード
                        int resultCode = result.getResultCode();

                        //マップ新規作成結果
                        if(resultCode == MapEntryActivity.RESULT_EDITED) {

                            //マップ情報画面からデータを受け取る
                            MapTable map = (MapTable) intent.getSerializableExtra(MapEntryActivity.KEY_MAP);
                            Log.i("MapItemView", "編集完了 map=" + map.getMapName());

                            //自身の情報を更新
                            mMap = map;

                            //アダプタに変更通知

                        }
                    }
                }
        );*/

        //レイアウト内のビューの設定
        setView();
    }


    /*
     * ビューの設定
     */
    private void setView(){

        //マップ情報設定
        //マップ名
        TextView tv_mapName = findViewById(R.id.tv_mapName);
        tv_mapName.setText( mMap.getMapName() );


        //マップオープンボタンリスナー
        findViewById( R.id.ib_map ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = view.getContext();

                //マップ画面へ遷移
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra(ResourceManager.KEY_MAPID, mMap.getPid());

                context.startActivity(intent);
            }
        });

        //編集ボタンリスナー
        findViewById( R.id.ib_edit ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //画面遷移
                Intent intent = new Intent(getContext(), MapEntryActivity.class);
                intent.putExtra(MapListActivity.KEY_ISCREATE, false );
                intent.putExtra(MapListActivity.KEY_MAP, mMap );

                Log.i("MapItemView", "map=" + mMap.getMapName());

                mAdapter.getEditLauncher().launch( intent );
                //mStartForResult.launch( intent );
            }
        });

        //削除ボタンリスナー
        findViewById( R.id.ib_delete ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //削除確認ダイアログを表示
                new AlertDialog.Builder(getContext())
                        .setTitle("マップ削除確認")
                        .setMessage("マップを削除します。\nなお、マップ内の写真は端末上からは削除されません。")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //DBから自マップ削除
                                AsyncDeleteMap db = new AsyncDeleteMap(getContext(), mMap, new AsyncDeleteMap.OnFinishListener() {
                                    @Override
                                    public void onFinish() {
                                        //アダプタへ自身を削除させる
                                        Log.i("deleteMap", "mMap.getMapName()=" + mMap.getMapName());
                                        mAdapter.deleteMap(mMap.getPid() );
                                    }
                                });
                                //非同期処理開始
                                db.execute();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

}
