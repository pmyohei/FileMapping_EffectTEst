package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/*
 * 指定配下ノードの写真を一覧表示する
 */
public class PictureGalleryActivity extends AppCompatActivity {

    //表示指定されたノード
    private int mNodePid;
    //写真単体表示画面へのランチャー
    ActivityResultLauncher<Intent> mSinglePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_gallery);

        //選択ノードを取得
        Intent intent = getIntent();
        mNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);
        if (mNodePid == 0) {
            //ノード取得エラーはこのまま本画面を終了する
            //★
            Toast.makeText(this, "エラー", Toast.LENGTH_SHORT).show();
            finish();
        }

        //写真の単体表示画面ランチャー
        mSinglePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    /*
                     * 写真単体表示画面からの戻り処理
                     */
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("colorPickerLauncher", "onActivityResult()");

                        if (result.getResultCode() == SinglePictureDisplayActivity.RESULT_UPDATE) {

                            Intent intent = result.getData();
                            if (intent != null) {
                                //更新の有無
                                boolean isUpdate = intent.getBooleanExtra(SinglePictureDisplayActivity.UPDATE, false);

                                //更新ありの場合、DBから最新の情報を取得し、再表示
                                if (isUpdate) {
                                    readGallery();
                                }
                            }
                        }

                    }
                }
        );


        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        toolbar.setTitle("ギャラリー");
        setSupportActionBar(toolbar);

        //ギャラリー情報を取得し画面上に表示
        readGallery();
    }

    /*
     * ギャラリー用ページの設定
     */
    private void readGallery() {

        //指定ノード配下のピクチャノードPidをリスト化
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        List<Integer> pictureNodes = mapCommonData.getNodes().getPictureNodes(mNodePid);
        int mapPid = mapCommonData.getMapPid();

        //配下の写真を取得
        AsyncReadGallery db = new AsyncReadGallery(this, mapPid, pictureNodes, new AsyncReadGallery.OnFinishListener() {
            //DB読み取り完了
            @Override
            public void onFinish(List<PictureArrayList<PictureTable>> galleries, List<PictureTable> dbThumbnails) {

                //サムネイルのBitmapリスト
                List<Bitmap> thumbnailBitmaps = new ArrayList<>();

                //ViewPagerのページレイアウトリスト
                List<Integer> layoutIdList = new ArrayList<>();
                //「すべて」のページは必ず用意する
                layoutIdList.add(R.layout.page_grid_gallery);
                //ピクチャノード数だけページを用意
                int i = 0;
                for (Integer pid : pictureNodes) {
                    //ページレイアウト
                    layoutIdList.add(R.layout.page_grid_gallery);

                    //サムネイルのビットマップ
                    //※nullの場合は、なし用画像が設定される
                    PictureTable thumbnail = dbThumbnails.get(i);
                    thumbnailBitmaps.add(PictureNodeView.createThumbnail(getResources(), thumbnail));

                    i++;
                }

                //ViewPagerの設定
                setGalleryViewPager(galleries, thumbnailBitmaps, layoutIdList);

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
     * 写真ギャラリー表示用のViewPagerの設定
     *   para1：ギャラリーリスト
     *   para2：タブのサムネイルリスト
     *   para3：タブ内のレイアウトIDリスト
     */
    private void setGalleryViewPager(List<PictureArrayList<PictureTable>> galleries,
                                     List<Bitmap> thumbnailBitmaps,
                                     List<Integer> layoutIdList) {

        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);

        //レイアウト確定後、アダプタの設定を行う
        //※表示する写真サイズを確実に設定するため
        ViewTreeObserver observer = vp2_gallery.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    //レイアウト確定後は、不要なので本リスナー削除
                    vp2_gallery.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //アダプタの設定
                    vp2_gallery.setAdapter(new GalleryPageAdapter(layoutIdList, galleries));

                    //インジケータの設定
                    TabLayout tabLayout = findViewById(R.id.tab_layout);
                    new TabLayoutMediator(tabLayout, vp2_gallery,
                            new TabLayoutMediator.TabConfigurationStrategy() {
                                @Override
                                public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                                    //タブの設定
                                    if (position == 0) {
                                        //★string
                                        //先頭のタブはすべての写真を表示する
                                        tab.setText("すべて");
                                    } else {
                                        //先頭より後はピクチャノードのサムネイルを設定
                                        tab.setCustomView(R.layout.item_gallery_tab);

                                        //サムネイルのbitmap

                                        //アイコンとして設定
                                        ImageView iv_picture = tab.getCustomView().findViewById(R.id.iv_picture);
                                        iv_picture.setImageBitmap(thumbnailBitmaps.get(position - 1));
                                    }
                                }
                            }
                    ).attach();

                }
            }
        );
    }

    /*
     * 画面遷移用ランチャー（画像ギャラリー）を取得
     */
    public ActivityResultLauncher<Intent> getSinglePictureLauncher() {
        return mSinglePictureLauncher;
    }

    /*
     * 複数選択操作用のメニューを設定
     */
    public void setMultipleOptionMenu( boolean isOpen ) {

        //メニュー
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        Menu menu = toolbar.getMenu();

        if( isOpen ){
            //オープン指定
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_gallery, menu);

            //ツールバー
            toolbar.setBackgroundColor( Color.BLACK );
            toolbar.setTitleTextColor( Color.WHITE );
            toolbar.setTitle("複数選択");

        } else{
            //クローズ指定
            menu.clear();

            //ツールバー
            toolbar.setBackgroundColor( Color.WHITE );
            toolbar.setTitleTextColor( Color.BLACK );
            toolbar.setTitle("ギャラリー");
        }
    }

    /*
     * ツールバーオプションメニュー生成
     */
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_gallery, menu);

        return true;
    }*/

    /*
     * ツールバーアクション選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //格納先ノードの移動
            case R.id.action_move:

                return true;

            //格納先ノードからの削除
            case R.id.action_delete:

                return true;

            //複数選択状態を解除
            case R.id.action_close:
                //複数選択メニューを閉じる
                setMultipleOptionMenu(false);

                //表示中ギャラリーのGridView
                ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
                GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);

                //選択中の写真を非選択にする
                int count = gv_gallery.getCount();
                for( int i = 0; i < count; i++ ){
                    if( gv_gallery.isItemChecked(i) ){
                        //選択中のものを解除
                        gv_gallery.setItemChecked( i, false );
                        //Log.i("複数選択対応", "コールチェック　false");
                    }
                    //Log.i("複数選択対応", "コールチェック");
                }

                //表示中のGridViewの選択モードの変更は、レイアウト確定後に行う
                //※setItemChecked(false)の反映が完了する前にモードを変更すると、状態が変わらなくなるため
                ViewTreeObserver observer = gv_gallery.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            //レイアウト確定後は、不要なので本リスナー削除
                            gv_gallery.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            //ギャラリーの選択中状態を解除
                            GalleryPageAdapter galleryPageAdapter = (GalleryPageAdapter)vp2_gallery.getAdapter();
                            galleryPageAdapter.cancellationMultipleSelection(gv_gallery);
                        }
                    }
                );

                Log.i("複数選択対応", "写真の数？ count=" + count);

                //GalleryPageAdapter adapter = (GalleryPageAdapter)vp2_gallery.getAdapter();
                //adapter.notifyItemChanged( page);

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}