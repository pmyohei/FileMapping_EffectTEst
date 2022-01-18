package com.mapping.filemapping;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

/*
 * マップ情報入力画面（新規作成／編集）
 */
public class MapEntryActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_CREATED = 100;
    public static final int RESULT_EDITED  = 101;

    /* 画面遷移-キー */
    public static String KEY_MAP = "map";         //マップ


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_entry);

        //遷移元からの情報
        Intent intent = getIntent();
        boolean isCreate = intent.getBooleanExtra( MapListActivity.KEY_ISCREATE, false );

        if( isCreate ){
            //OKボタンリスナー
            findViewById(R.id.bt_create).setOnClickListener( new PositiveClickListener(null) );

        } else {
            //編集対象のマップを取得
            MapTable map = (MapTable) intent.getSerializableExtra(MapListActivity.KEY_MAP);

            //登録済み情報として設定
            ((EditText)findViewById(R.id.et_mapName)).setText( map.getMapName() );

            //OKボタンリスナー
            findViewById(R.id.bt_create).setOnClickListener( new PositiveClickListener(map) );
        }



/*
        //ポジティブボタン
        Button bt_positive = findViewById(R.id.bt_positive);

        //ポジティブボタン押下
        bt_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //マップ名空チェック
                String mapName = ((EditText)findViewById(R.id.et_mapName)).getText().toString();
                if( mapName.isEmpty() ){
                    //空なら、メッセージ出力して終了
                    //★

                    return;
                }

                //ルートノード名空チェック
                String rootNodeName = ((EditText)findViewById(R.id.et_rootNodeName)).getText().toString();
                if( rootNodeName.isEmpty() ){
                    //空なら、メッセージ出力して終了
                    //★

                    return;
                }

                //マップを生成
                //★
                //※本マップ自体のpidはこの時点では確定していないため、DB処理完了後に設定
                MapTable map = new MapTable();
                map.setMapName( mapName );

                //DB保存処理
                AsyncCreateMap db = new AsyncCreateMap(view.getContext(), map, rootNodeName, new AsyncCreateMap.OnFinishListener() {

                    @Override
                    public void onFinish(int pid) {
                        //データ挿入されたため、レコードに割り当てられたpidをマップに設定
                        map.setPid( pid );

                        //resultコード設定
                        intent.putExtra(KEY_MAP, map );
                        setResult(RESULT_CREATED, intent );

                        Log.i("Map", "マップ生成完了。マップリスト画面へ戻る");

                        //元の画面へ戻る
                        finish();
                    }
                });
                //非同期処理開始
                db.execute();
            }
        });
*/



        //キャンセル
        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //resultコード設定
                setResult(RESULT_CANCELED);
                //元の画面へ戻る
                finish();
            }
        });


    }

    /*
     *
     * OKボタンクリックリスナー
     *
     */
    private class PositiveClickListener implements View.OnClickListener {

        //対象マップ
        private MapTable mMap;

        /*
         * コンストラクタ
         */
        public PositiveClickListener(MapTable map) {
            //対象マップ（新規生成の場合はnullが渡される）
            mMap = map;
        }

        @Override
        public void onClick(View view) {

            //入力チェック
            if (verifyInputData()) {
                return;
            }

            //新規生成
            boolean isCreate = false;

            //新規生成なら、ここでマップを生成
            if( mMap == null){
                mMap = new MapTable();

                isCreate = true;
            }

            //入力データを設定
            String mapName = ((EditText)findViewById(R.id.et_mapName)).getText().toString();
            mMap.setMapName( mapName );

            //非同期処理
            if( isCreate ){
                doAsyncCreateMap(view.getContext(), getIntent(), mMap);
            } else {
                doAsyncUpdateMap(view.getContext(), getIntent(), mMap);
            }
        }

        /*
         * 入力チェック
         *   true：問題あり
         */
        private boolean verifyInputData() {

            //マップ名空チェック
            String mapName = ((EditText)findViewById(R.id.et_mapName)).getText().toString();
            if( mapName.isEmpty() ){
                //空なら、メッセージ出力して終了
                //★

                return true;
            }

/*            //ルートノード名空チェック
            String rootNodeName = ((EditText)findViewById(R.id.et_rootNodeName)).getText().toString();
            if( rootNodeName.isEmpty() ){
                //空なら、メッセージ出力して終了
                //★

                return false;
            }*/

            //問題なし
            return false;
        }

        /*
         * 非同期処理-新規生成
         */
        private void doAsyncCreateMap(Context context, Intent intent, MapTable map){
            //DB保存処理
            AsyncCreateMap db = new AsyncCreateMap(context, map, new AsyncCreateMap.OnFinishListener() {

                @Override
                public void onFinish(int pid) {
                    //データ挿入されたため、レコードに割り当てられたpidをマップに設定
                    map.setPid( pid );

                    //resultコード設定
                    intent.putExtra(KEY_MAP, map );
                    setResult(RESULT_CREATED, intent );

                    Log.i("Map", "マップ生成完了。マップリスト画面へ戻る");

                    //元の画面へ戻る
                    finish();
                }
            });
            //非同期処理開始
            db.execute();
        }

        /*
         * 非同期処理-更新
         */
        private void doAsyncUpdateMap(Context context, Intent intent, MapTable map){
            //DB保存処理
            AsyncUpdateMap db = new AsyncUpdateMap(context, map, new AsyncUpdateMap.OnFinishListener() {

                @Override
                public void onFinish() {

                    //resultコード設定
                    intent.putExtra(KEY_MAP, map);
                    setResult(RESULT_EDITED, intent);

                    Log.i("Map", "マップ編集完了。リスト画面へ戻る");

                    //元の画面へ戻る
                    finish();
                }
            });
            //非同期処理開始
            db.execute();
        }

    }






}