package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/*
 * マップ編集画面
 */
public class MapEditActivity extends AppCompatActivity {

    //編集対象マップ
    private MapTable mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_edit);

        //ツールバー
        setToolBar();

        //遷移元からの情報
        Intent intent = getIntent();
        //編集対象のマップを取得
        mMap = (MapTable) intent.getSerializableExtra(MapListActivity.KEY_MAP);

        //登録済みの情報を設定
        setInputtedData();

        //保存リスナー
        findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TextInputEditText tiet_mapName = findViewById(R.id.tiet_mapName);
                TextInputEditText tiet_memo = findViewById(R.id.tiet_memo);

                String mapName = tiet_mapName.getText().toString();
                if( mapName.isEmpty() ){
                    //保存不可
                    Toast.makeText(view.getContext(), getString(R.string.toast_errorMapName), Toast.LENGTH_SHORT).show();
                    return;
                }

                //入力情報を更新
                mMap.setMapName( mapName );
                mMap.setMemo( tiet_memo.getText().toString() );

                //DB保存処理
                AsyncUpdateMap db = new AsyncUpdateMap(view.getContext(), mMap, new AsyncUpdateMap.OnFinishListener() {

                    @Override
                    public void onFinish() {

                        //resultコード設定
                        intent.putExtra(MapListActivity.KEY_MAP, mMap);
                        setResult(MapListActivity.RESULT_EDITED, intent);

                        //完了メッセージを表示
                        Toast.makeText(view.getContext(), getString(R.string.toast_finishSave), Toast.LENGTH_SHORT).show();
                    }
                });
                //非同期処理開始
                db.execute();
            }
        });

    }

    /*
     * ツールバーの初期設定
     */
    private void setToolBar() {
        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_mapEdit);
        toolbar.setTitle( "" );
        setSupportActionBar(toolbar);

        //戻るボタンを有効化
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //システムバー
        getWindow().setStatusBarColor(Color.BLACK);
    }

    /*
     * 登録済み情報の設定
     */
    private void setInputtedData() {

        //入力ビュー
        TextInputEditText tiet_mapName = findViewById(R.id.tiet_mapName);
        TextInputEditText tiet_memo = findViewById(R.id.tiet_memo);

        //マップ名
        tiet_mapName.setText( mMap.getMapName() );
        //メモ
        tiet_memo.setText( mMap.getMemo() );
    }

    /*
     * ツールバー 戻るボタン押下処理
     */
    @Override
    public boolean onSupportNavigateUp() {
        //アクティビティ終了
        finish();
        return super.onSupportNavigateUp();
    }

}