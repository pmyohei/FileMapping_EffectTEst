package com.mapping.filemapping;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/*
 * 指定配下ノードの写真を一覧表示する
 */
public class PictureGalleryActivity extends AppCompatActivity {

    //「すべて」タブのページindex
    public static final int ALL_PAGE_INDEX = 0;
    //データなし
    private static final int NOTHING = -1;

    //表示指定されたノード
    private int mNodePid;
    //写真単体表示画面へのランチャー
    ActivityResultLauncher<Intent> mSinglePictureLauncher;
    //表示対象のギャラリーリスト
    List<PictureArrayList<PictureTable>> mGalleries;
    //タブに表示しているピクチャノードのpidリスト
    List<Integer> mPictureNodePids;
    //マップ上のピクチャノード情報
    private ArrayList<PictureNodesBottomSheetDialog.PictureNodeInfo> mPictureNodeInfo;
    //アダプタ更新リスト
    List<Integer> mUpdatePages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_gallery);

        //選択ノードを取得
        Intent intent = getIntent();
        mNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);
        if (mNodePid == 0) {
            //ノード取得エラーはこのまま本画面を終了する
            Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        //アダプタ更新リスト
        mUpdatePages = new ArrayList<>();

        //タブスクロール時の処理設定
        setupGalleryPageScroll();

        //ピクチャノード情報リストを生成
        createPictureNodeInfos();

        //写真の単体表示画面ランチャー
        setupSinglePictureLauncher();

        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        toolbar.setTitle(getString(R.string.toolbar_gallery_title));
        setSupportActionBar(toolbar);

        //ギャラリー情報を取得し画面上に表示
        readGallery();
    }

    /*
     * ピクチャノード情報リストの生成
     */
    private void createPictureNodeInfos() {

        //マップ上のピクチャノードpid
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();
        NodeArrayList<NodeTable> pictureNodePids = nodes.getAllPictureNodes();

        //マップ上のピクチャノード情報（写真移動先の選択用）
        mPictureNodeInfo = new ArrayList<>();

        //サムネイルを取得
        int mapPid = nodes.get(0).getPidMap();
        AsyncReadThumbnail db = new AsyncReadThumbnail(this, mapPid, new AsyncReadThumbnail.OnFinishListener() {
            @Override
            public void onFinish(PictureArrayList<PictureTable> thumbnails) {

                //全ピクチャノード数
                for (NodeTable node : pictureNodePids) {
                    //ピクチャノードのpid
                    int pid = node.getPid();
                    //ピクチャノードの親ノード名
                    NodeTable parentNode = nodes.getNode(node.getPidParentNode());
                    String parentNodeName = parentNode.getNodeName();
                    //ピクチャのノードのサムネイル写真（nullの場合あり）
                    PictureTable thumbnail = thumbnails.getThumbnail(pid);

                    //ピクチャノード情報を生成
                    mPictureNodeInfo.add(new PictureNodesBottomSheetDialog.PictureNodeInfo(
                            pid,
                            thumbnail,
                            parentNodeName
                    ));
                }
            }
        });

        //非同期処理開始
        db.execute();
    }

    /*
     * 写真の単体表示画面ランチャーを設定
     */
    private void setupSinglePictureLauncher() {

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
    }

    /*
     * ギャラリー用ページの設定
     */
    private void readGallery() {

        //指定ノード配下のピクチャノードPidをリスト化
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        mPictureNodePids = mapCommonData.getNodes().getPictureNodes(mNodePid);
        int mapPid = mapCommonData.getMapPid();

        //配下の写真を取得
        AsyncReadGallery db = new AsyncReadGallery(this, mapPid, mPictureNodePids, new AsyncReadGallery.OnFinishListener() {
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
                for (Integer pid : mPictureNodePids) {
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

                //ギャラリーリストを保持
                mGalleries = galleries;
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
                        TabLayout tabLayout = findViewById(R.id.tab_pictureNode);
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

        //参照中タブ
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();
        if (page == ALL_PAGE_INDEX) {
            //フェールセーフ
            //「すべて」ページに対して、複数選択機能はないが、仮に現在のページが先頭ページの場合、何もしない
            //※時々発生するため、ガードを入れる
            Log.i("ページ参照エラー", "「すべて」を対象に複数選択機能が発生");
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {

            //格納先ノードの移動
            case R.id.action_move:
                //移動先のダイアログを表示
                showMoveDestination();

                return true;

            //格納先ノードからの削除
            case R.id.action_delete:
                //削除確認
                confirmDeletePicture();

                return true;

            //複数選択状態を解除
            case R.id.action_close:
                //解除処理
                closeMultipleOptionMenu();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * 選択中の写真をリストで取得
     *   para：ページindex
     */
    private PictureArrayList<PictureTable> getSelectedPictures(int page) {
        //選択写真
        PictureArrayList<PictureTable> selectedPictures = new PictureArrayList<>();

        //表示中ギャラリーのGridView
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);

        //表示中ギャラリー
        PictureArrayList<PictureTable> gallery = mGalleries.get(page);

        //選択中の写真をリストに格納
        int count = gv_gallery.getCount();
        for (int i = 0; i < count; i++) {
            if (gv_gallery.isItemChecked(i)) {
                //選択中の場合、選択写真リストに格納
                selectedPictures.add(gallery.get(i));
            }
        }

        return selectedPictures;
    }

    /*
     * 格納先ノードの移動処理
     */
    private void showMoveDestination() {

        //参照中タブ
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();

        //参照中のピクチャノードのpid
        //※先頭は「すべて」タブであるため、参照indexは１小さい位置になる
        int tabPictureNodePid = mPictureNodePids.get(page - 1);

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures(page);

        //マップ上のピクチャノードを移動先候補として表示
        //PictureNodesBottomSheetDialog bottomSheetDialog = PictureNodesBottomSheetDialog.newInstance(mPictureNodeInfo, tabPictureNodePid, selectedPictures);
        PictureNodesBottomSheetDialog bottomSheetDialog = new PictureNodesBottomSheetDialog(this, mPictureNodeInfo, tabPictureNodePid, selectedPictures);
        bottomSheetDialog.show(getSupportFragmentManager(), "");
    }

    /*
     * 写真の削除（格納先から除外）確認
     */
    private void confirmDeletePicture() {

        //削除確認ダイアログを表示
        new AlertDialog.Builder(this)
                .setTitle( getString(R.string.alert_deletePicture_title) )
                .setMessage( getString(R.string.alert_deletePicture_message) )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //選択中の写真を削除
                        deletePicturesOnDB();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }


    /*
     * 写真の削除（格納先から除外）
     */
    private void deletePicturesOnDB() {

        //参照中タブ
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures(page);

        //DBから写真を削除
        AsyncDeletePicture db = new AsyncDeletePicture(this, selectedPictures, new AsyncDeletePicture.OnFinishListener() {
            @Override
            public void onFinish(boolean isThumbnail) {
                //アダプタを更新
                removeGallery(selectedPictures, isThumbnail);
            }
        });

        //非同期処理開始
        db.execute();
    }


    /*
     * 複数選択解除処理
     */
    private void closeMultipleOptionMenu() {

        Log.i("メニュー", "クローズ処理 closeMultipleOptionMenu()");

        //複数選択メニューを閉じる
        setToolbarOptionMenu(false);

        //アイテムを非選択状態に
        cancellationSelected();
    }

    /*
     * 複数選択操作用のメニューを設定
     */
    public void setToolbarOptionMenu(boolean isOpen) {

        //メニュー
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        Menu menu = toolbar.getMenu();

        if (isOpen) {
            //オープン指定
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_gallery, menu);

            //ツールバー
            toolbar.setBackgroundColor(Color.BLACK);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setTitle( getString(R.string.toolbar_gallery_multi) );

        } else {
            if (!menu.hasVisibleItems()) {
                //既に閉じていれば何もしない
                return;
            }

            //クローズ指定
            menu.clear();

            //ツールバー
            toolbar.setBackgroundColor(Color.WHITE);
            toolbar.setTitleTextColor(Color.BLACK);
            toolbar.setTitle( getString(R.string.toolbar_gallery_title) );
        }
    }

    /*
     * 選択中の写真を非選択状態にし、選択モードを解除させる
     */
    public void cancellationSelected() {

        //表示中ギャラリーのGridView
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);

        //複数選択状態でなければ、何もしない
        if (gv_gallery.getChoiceMode() != GridView.CHOICE_MODE_MULTIPLE) {
            return;
        }

        //選択中の写真を非選択にする
        int count = gv_gallery.getCount();
        for (int i = 0; i < count; i++) {
            if (gv_gallery.isItemChecked(i)) {
                //選択中のものを解除
                gv_gallery.setItemChecked(i, false);
            }
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
                        GalleryPageAdapter galleryPageAdapter = (GalleryPageAdapter) vp2_gallery.getAdapter();
                        galleryPageAdapter.cancellationMultipleSelection(gv_gallery);
                    }
                }
        );
    }

    /*
     * ギャラリーの削除
     *   引数に指定された写真をリストから削除し、アダプタに更新通知を送る
     *   para1：削除された写真リスト
     */
    public void removeGallery(PictureArrayList<PictureTable> selectedPictures, boolean isThumbnail) {

        Toast.makeText(this,
                getString(R.string.toast_deletePicture),
                Toast.LENGTH_LONG)
                .show();

        //タブで表示しているサムネイルの更新
        disableTabThumbnail(selectedPictures, isThumbnail);

        //選択状態を解除
        closeMultipleOptionMenu();

        //参照中のギャラリーを更新
        updateSourceGallery(selectedPictures);

        //「すべて」タブのギャラリーを更新
        updateAllTabGallery();
    }

    /*
     * ギャラリーの更新
     *   引数に指定された写真をリストから削除・追加し、アダプタに更新通知を送る
     *   para1：移動された写真リスト
     */
    public void updateGallery(PictureArrayList<PictureTable> selectedPictures, int toPicutureNodePid, boolean isThumbnail) {

        //--
        //for( PictureTable tmp: selectedPictures ){
        //    Log.i("ページ更新", "選択写真=" + tmp.getPid());
        //}
        //--

        //タブで表示しているサムネイルの更新
        disableTabThumbnail(selectedPictures, isThumbnail);

        //選択状態を解除
        closeMultipleOptionMenu();

        //参照中のギャラリーを更新
        updateSourceGallery(selectedPictures);

        //移動先のギャラリーを更新
        updateDestinationGallery(selectedPictures, toPicutureNodePid);

        //「すべて」タブのギャラリーを更新
        updateAllTabGallery();
    }

    /*
     * タブサムネイル写真の無効化
     *   指定された写真リストの中にサムネイルの写真があれば、
     *   タブ写真を無効用アイコンに変更する。
     *   para1：移動された写真リスト
     */
    private void disableTabThumbnail( PictureArrayList<PictureTable> selectedPictures, boolean isThumbnail ) {

        //サムネイルがなければ何もしない
        if( !isThumbnail ){
            return;
        }

        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();

        //表示中のタブアイコン
        TabLayout tabLayout = findViewById(R.id.tab_pictureNode);
        TabLayout.Tab tab = tabLayout.getTabAt(page);
        ImageView iv_picture = tab.getCustomView().findViewById(R.id.iv_picture);

        //無効アイコンを設定
        iv_picture.setImageBitmap(
                BitmapFactory.decodeResource( getResources(), R.drawable.baseline_no_thumbnail_24)
        );
    }

    /*
     * 操作対象（複数選択された時のタブ）ページのギャラリーを更新
     *   para1：移動された写真リスト
     */
    private void updateSourceGallery( PictureArrayList<PictureTable> selectedPictures ) {

        //表示中ギャラリー
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();
        PictureArrayList<PictureTable> gallery = mGalleries.get(page);

        //ギャラリーから削除
        for (PictureTable deleteTarget : selectedPictures) {
            gallery.deletePicture(deleteTarget);
        }

        //アダプタに変更を通知
        GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);
        GalleryAdapter adapter = (GalleryAdapter) gv_gallery.getAdapter();
        adapter.notifyDataSetChanged();
    }


    /*
     * ノード移動先のギャラリーの更新
     *   para1：移動された写真リスト
     */
    private void updateDestinationGallery( PictureArrayList<PictureTable> selectedPictures, int toPicutureNodePid ) {

        //移動先ノードが表示されていなければ、終了
        int pageIndex = isDisplayTab(toPicutureNodePid);
        if( pageIndex == NOTHING ){
            return;
        }

        Log.i("ページ更新", "移動先のページ=" + pageIndex);

        //移動先ノードのギャラリーに追加
        PictureArrayList<PictureTable> gallery = mGalleries.get(pageIndex);
        gallery.addAll( selectedPictures );

        //アダプタ更新リストに追加（あれば不要）
        if( !mUpdatePages.contains( pageIndex ) ){
            mUpdatePages.add( pageIndex );

            Log.i("ページ更新", "更新対象追加=" + pageIndex);
        }

        //--
        //for( PictureTable tmp: gallery ){
        //    Log.i("ページ更新", "「格納先」" + tmp.getPid());
        //}
        //for( Integer tmp: mUpdatePages ){
        //    Log.i("ページ更新", "「mUpdatePages」" + tmp);
        //}
        //--
    }

    /*
     * 「すべて」タブのギャラリーを更新
     */
    private void updateAllTabGallery() {

        //「すべて」タブのギャラリーを作り直す
        mGalleries.get(0).clear();

        //先頭より後のギャラリーを追加
        int size = mGalleries.size();
        for( int i = 1; i < size; i++ ){
            mGalleries.get(0).addAll( mGalleries.get(i) );
        }

        //アダプタ更新リストに追加（あれば不要）
        if( !mUpdatePages.contains( ALL_PAGE_INDEX ) ){
            mUpdatePages.add( ALL_PAGE_INDEX );

            Log.i("ページ更新", "更新対象追加=" + ALL_PAGE_INDEX);
        }

        //--
        for( PictureTable tmp: mGalleries.get(0) ){
            Log.i("ページ更新", "「すべて」" + tmp.getPid());
        }
        for( Integer tmp: mUpdatePages ){
            Log.i("ページ更新", "「mUpdatePages」" + tmp);
        }
        //--

    }

    /*
     * 表示中のタブにあるかどうか
     *   return：ページindex（ページ先頭には「すべて」があるため、１加算する）
     */
    private int isDisplayTab( int toPicutureNodePid ) {

        final int TOP_PAGE = 1;

        int i = 0;
        for (Integer pid : mPictureNodePids) {
            if( pid == toPicutureNodePid ){
                return (i + TOP_PAGE);
            }
            i++;
        }

        return NOTHING;
    }


    /*
     * タブスクロール時の処理設定
     */
    private void setupGalleryPageScroll() {

        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        vp2_gallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i("クローズ処理", "onPageScrollStateChanged state=" + state);

                //ページドラッグを検知した瞬間、複数選択状態を解除
                if( state == SCROLL_STATE_DRAGGING ){
                    closeMultipleOptionMenu();
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                Log.i("ページ更新", "onPageSelected=" + position);

                //--
                for( Integer tmp: mUpdatePages ){
                    Log.i("ページ更新", "onPageSelecteda 「mUpdatePages」" + tmp);
                }
                //--

                if( mUpdatePages.contains( position ) ){
                    //更新対象なら、アダプタに更新通知を送る
                    GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);
                    GalleryAdapter adapter = (GalleryAdapter) gv_gallery.getAdapter();
                    adapter.notifyDataSetChanged();

                    Log.i("ページ更新", "更新発生=" + position);

                    //更新リストから削除
                    int i = 0;
                    for( int page: mUpdatePages ){
                        if( page == position ){
                            mUpdatePages.remove(i);
                            break;
                        }
                        i++;
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //Log.i("クローズ処理", "onPageScrolled position=" + position);
                //解除処理
                //closeMultipleOptionMenu();
            }

        });
    }

}
