package com.mapping.filemapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/*
 * マップ新規作成画面
 */
public class MapCreateActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_CREATED = 100;

    /* 画面遷移-キー */
    public static String KEY_MAP = "map";         //マップ

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_create);

        //マップ入力ページレイアウト
        setupCreateMapPage();

        //ツールバー設定
        setToolBar();

        mContext = this;

        //OKボタンリスナー
        findViewById(R.id.bt_create).setOnClickListener( new PositiveClickListener(null) );

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

        //ヘルプダイアログの表示
        showFirstLaunchDialog();
    }

    /*
     * ツールバーの初期設定
     */
    private void setToolBar() {
/*        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_map);
        toolbar.setTitle( mMap.getMapName() );
        setSupportActionBar(toolbar);

        //戻るボタンを有効化
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        //システムバー
        getWindow().setStatusBarColor( Color.BLACK );
    }

    /*
     * 初回起動時のヘルプダイアログを表示
     */
    private void showFirstLaunchDialog(){

        final String key = ResourceManager.SHARED_KEY_HELP_ON_MAPENTRY;

        //表示の有無を取得
        SharedPreferences spData = getSharedPreferences(ResourceManager.SHARED_DATA_NAME, MODE_PRIVATE);
        boolean isShow = spData.getBoolean( key, ResourceManager.INVALID_SHOW_HELP);

        if( !isShow ){
            //表示なしが選択されていれば何もしない
            return;
        }

        //ガイドダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle( getString(R.string.alert_launch_mapEntry_title) )
            .setMessage( getString(R.string.alert_launch_mapEntry_message) )
            .setPositiveButton(getString(R.string.do_not_show_this_message), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = spData.edit();
                    editor.putBoolean( key, false );
                    editor.apply();
                }
            })
            //.setNegativeButton("Cancel", null)
            .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView)dialog.findViewById(android.R.id.message)).setTypeface( Typeface.SERIF );
    }

    /*
     * マップデザイン用のレイアウトを設定
     */
    private void setupCreateMapPage() {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.page_map_create_0);
        layoutIdList.add(R.layout.page_map_create_1);
        layoutIdList.add(R.layout.page_map_create_2);

        //サンプルマップ
        FrameLayout fl_map = findViewById(R.id.fl_map);

        //ViewPager2にアダプタを割り当て
        ViewPager2 vp = findViewById(R.id.vp2_createMap);
        CreateMapPageAdapter adapter = new CreateMapPageAdapter(layoutIdList, fl_map);
        vp.setAdapter(adapter);

        String[] titles = new String[]{
                getString(R.string.map_create_tab1),
                getString(R.string.map_create_tab2),
                getString(R.string.map_create_tab3),
        };

        //インジケータの設定
        TabLayout tabLayout = findViewById(R.id.tab_createMap);
        new TabLayoutMediator(tabLayout, vp,
                (tab, position) -> tab.setText(titles[position])
        ).attach();
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
            //対象マップ
            mMap = map;
        }

        @Override
        public void onClick(View view) {

            //入力チェック
            if (verifyInputData(view)) {
                return;
            }

            //ここでマップを生成
            if( mMap == null){
                mMap = new MapTable();

                //カラーパターンは新規生成のみ
                SampleMapView smv = findViewById(R.id.fl_map);
                String[] colors = smv.getCurrentColors();

                //マップ色関連
                mMap.setMapColor( colors[0] );
                mMap.setMapGradationColor( colors[0] );
                mMap.setGradation( false );
                mMap.setGradationDirection( MapTable.GRNDIR_TOP_BOTTOM );

                //デフォルトカラー
                mMap.setFirstColor( colors[0] );
                mMap.setSecondColor( colors[1] );
                mMap.setThirdColor( colors[2] );

                //影の有無
                mMap.setShadow( smv.isMapShadow() );
            }

            //入力マップ名を設定
            SampleMapView sampleMapView = view.getRootView().findViewById( R.id.fl_map );
            String mapName = sampleMapView.getMapName();

            mMap.setMapName( mapName );

            //非同期処理
            doAsyncCreateMap(getIntent(), mMap);
        }

        /*
         * 入力チェック
         *   true：問題あり
         */
        private boolean verifyInputData(View view) {

            SampleMapView sampleMapView = view.getRootView().findViewById( R.id.fl_map );

            //マップ名空チェック
            String mapName = sampleMapView.getMapName();
            if( (mapName == null) || (mapName.isEmpty()) ){
                //空なら、メッセージ出力して終了
                Toast.makeText(view.getContext(), getString(R.string.toast_errorMapName), Toast.LENGTH_SHORT).show();
                return true;
            }

            //問題なし
            return false;
        }

        /*
         * 非同期処理-新規生成
         */
        private void doAsyncCreateMap(Intent intent, MapTable map){
            //DB保存処理
            AsyncCreateMap db = new AsyncCreateMap(mContext, map, new AsyncCreateMap.OnFinishListener() {
                @Override
                public void onFinish(int pid) {
                    //データ挿入されたため、レコードに割り当てられたpidをマップに設定
                    map.setPid( pid );

                    //resultコード設定
                    intent.putExtra(KEY_MAP, map );
                    setResult(RESULT_CREATED, intent );

                    //元の画面へ戻る
                    finish();
                }
            });
            //非同期処理開始
            db.execute();
        }

    }

}
