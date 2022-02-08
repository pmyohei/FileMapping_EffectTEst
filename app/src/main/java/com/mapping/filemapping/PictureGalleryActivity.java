package com.mapping.filemapping;

import static java.security.AccessController.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
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

        //指定ノード配下のピクチャノードPidをリスト化
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        List<Integer> pictureNodes = mapCommonData.getNodes().getPictureNodes( nodePid );
        int mapPid = mapCommonData.getMapPid();

        //Log.i("ギャラリー確認", "ルートチェック1");
        //for( Integer bbb: pictureNodes ){
        //    Log.i("ギャラリー確認", "getPictureNodes=" + bbb);
        //}




        //配下の写真を取得
        AsyncReadGallery db = new AsyncReadGallery(this, mapPid, pictureNodes, new AsyncReadGallery.OnFinishListener() {
            //DB読み取り完了
            @Override
            public void onFinish(List<PictureArrayList<PictureTable>> galleries) {

                //本ノードのサムネイルを取得
                MapCommonData mapCommonData = (MapCommonData)getApplication();

                //サムネイルのBitmapリスト
                List<Bitmap> thumbnails = new ArrayList<>();

                //ViewPagerのページレイアウトリスト
                List<Integer> layoutIdList = new ArrayList<>();
                layoutIdList.add(R.layout.page_grid_gallery);       //「すべて」のページは必ず用意する
                for( Integer pid: pictureNodes  ){                  //ピクチャノード数だけページを用意
                    //ページレイアウト
                    layoutIdList.add(R.layout.page_grid_gallery);

                    //サムネイルのビットマップ
                    PictureTable thumbnail = mapCommonData.getThumbnails().getThumbnail( pid );
                    thumbnails.add( PictureNodeView.createThumbnail( thumbnail ) );
                }

                //ViewPagerの設定
                ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
                vp2_gallery.setAdapter( new GalleryPageAdapter(layoutIdList, galleries) );

                //インジケータの設定
                TabLayout tabLayout = findViewById(R.id.tab_layout);
                new TabLayoutMediator(tabLayout, vp2_gallery,
                        new TabLayoutMediator.TabConfigurationStrategy(){
                            @Override
                            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                                //タブの設定
                                if( position == 0 ){
                                    //★string
                                    //先頭のタブはすべての写真を表示する
                                    tab.setText("すべて");
                                } else {
                                    //先頭より後はピクチャノードのサムネイルを設定
                                    tab.setCustomView( R.layout.item_gallery_tab );

                                    //サムネイルのbitmap

                                    //アイコンとして設定
                                    ImageView iv_picture = tab.getCustomView().findViewById( R.id.iv_picture);
                                    iv_picture.setImageBitmap( thumbnails.get( position - 1 ) );
                                }
                            }
                        }
                ).attach();

                //Log.i("ギャラリー確認", "ルートチェック2");
                //for( PictureTable aa: pictures ){
                //    Log.i("ギャラリー確認", "DB読み込み=" + aa.getPath());
                //}

/*                //画像を表示
                GridView gv_gallery = findViewById(R.id.gv_gallery);
                gv_gallery.setAdapter( new GalleryAdapter( pictures ) );*/
            }
        });

        //非同期処理開始
        db.execute();

    }

    /*
     * ギャラリー用ページの設定
     */
    private void setupGalleryPage() {



    }
}