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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * 指定配下ノードの写真を一覧表示する
 */
public class PictureGalleryActivity extends AppCompatActivity implements PictureNodesBottomSheetDialog.NoticeDialogListener {

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

        //ツールバー設定
        setToolBar();

        //Admobロード
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //選択ノードを取得
        Intent intent = getIntent();
        mNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);
        if (mNodePid == 0) {
            //ノード取得エラーはこのまま本画面を終了する
            Toast.makeText(this, getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
            finish();
        }

        //resultコード設定
        setResult(MapActivity.RESULT_GALLERY, getIntent());
        //アダプタ更新リスト
        mUpdatePages = new ArrayList<>();

        //ストレージとの同期注意事項
        showCautionAboutSyncStorage();

        //タブスクロール時の処理設定
        setupGalleryPageScroll();

        //ピクチャノード情報リストを生成
        createPictureNodeInfos();

        //写真の単体表示画面ランチャー
        setupSinglePictureLauncher();

        //ギャラリー情報を取得し画面上に表示
        readGallery();
    }

    /*
     * ツールバーの設定
     */
    private void setToolBar() {
        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //戻るボタン
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //システムバー
        getWindow().setStatusBarColor(Color.BLACK);
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
                List<PictureTable> thumbnails = new ArrayList<>();

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
                    thumbnails.add(dbThumbnails.get(i));

                    i++;
                }

                //ViewPagerの設定
                setGalleryViewPager(galleries, thumbnails, layoutIdList);

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
                                     //List<Bitmap> thumbnailBitmaps,
                                     List<PictureTable> thumbnails,
                                     List<Integer> layoutIdList) {

        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);

        //レイアウト確定後、アダプタの設定を行う
        //※表示する写真サイズを確実に設定するため
        vp2_gallery.post(() -> {
            //アダプタの設定
            vp2_gallery.setAdapter(new GalleryPageAdapter(layoutIdList, galleries));

            //タブの設定
            TabLayout tabLayout = findViewById(R.id.tab_pictureNode);
            new TabLayoutMediator(tabLayout, vp2_gallery,
                    new TabLayoutMediator.TabConfigurationStrategy() {
                        @Override
                        public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                            //タブの設定
                            if (position == 0) {
                                //先頭のタブはすべての写真を表示する
                                tab.setText(getString(R.string.tab_all));

                            } else {
                                //先頭より後はピクチャノードのサムネイルを設定
                                tab.setCustomView(R.layout.item_gallery_tab);

                                //タブに表示するピクチャノードサイズ
                                int viewSize = (int) getResources().getDimension(R.dimen.gallery_tab_size);

                                //サムネイルのpath
                                PictureTable thumbnail = thumbnails.get(position - 1);
                                String path = ((thumbnail == null) ? "" : thumbnail.getPath());

                                //アイコンとして設定
                                //※画質を担保するため、resize()である程度画像の大きさを確保してからtransform()に渡す
                                ImageView iv_picture = tab.getCustomView().findViewById(R.id.iv_picture);
                                Picasso.get()
                                        .load(new File(path))
                                        .resize(ThumbnailTransformation.RESIZE, ThumbnailTransformation.RESIZE)
                                        .transform(new ThumbnailTransformation(thumbnail, viewSize))
                                        .error(R.drawable.baseline_no_image)
                                        .into(iv_picture);
                            }
                        }
                    }
            ).attach();

        });
    }

    /*
     * 画面遷移用ランチャー（画像ギャラリー）を取得
     */
    public ActivityResultLauncher<Intent> getSinglePictureLauncher() {
        return mSinglePictureLauncher;
    }

    /*
     * ギャラリーをストレージと同期
     */
    //★リリース後対応
    private void synchronizeGalleries() {

        //キャッシュクリア
        boolean isCache = clearRemoveImageCache();

        //キャッシュクリアがあれば
        if (isCache) {
            //ギャラリー再更新
            ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
            GalleryPageAdapter galleryPageAdapter = (GalleryPageAdapter) vp2_gallery.getAdapter();
            if (galleryPageAdapter != null) {
                //全更新
                galleryPageAdapter.notifyDataSetChanged();
            }

            //タブのサムネイルを更新

        }
    }

    /*
     * 端末から削除された画像のキャッシュをクリア
     *   return：キャッシュ対象がなければ、falseを返す
     */
    //★リリース後対応
    private boolean clearRemoveImageCache() {

        boolean isCache = false;

        for (PictureTable picture : mGalleries.get(0)) {

            if (picture == null) {
                //画像情報なしは対象外
                continue;
            }
            String path = picture.getPath();
            if (path.isEmpty()) {
                //path情報なしなら対象外
                continue;
            }
            File file = new File(path);
            if (file.isFile()) {
                //ファイル生成できているなら対象外
                continue;
            }

            //★現状は、端末から削除された画像だけ更新対象になる
            //リネームとかは対象外

            //pathの実体がない画像のキャッシュをクリア
            Picasso.get().invalidate(path);

            isCache = true;
        }

        return isCache;
    }

    /*
     * ツールバーオプションメニュー生成
     */
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_gallery, menu);

        Log.i("menu★", "");

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
/*
            //★リリース後対応
            //ギャラリー同期
            case R.id.action_refresh:
                //ギャラリーをストレージの保存状態と同期する
                synchronizeGalleries();

                return true;*/

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
     * ツールバー 戻るボタン押下処理
     */
    @Override
    public boolean onSupportNavigateUp() {
        //アクティビティ終了
        finish();

        return super.onSupportNavigateUp();
    }

    /*
     * 選択中の写真をリストで取得
     */
    private PictureArrayList<PictureTable> getSelectedPictures() {
        //選択写真
        PictureArrayList<PictureTable> selectedPictures = new PictureArrayList<>();

        //表示中ギャラリーのGridView
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        GridView gv_gallery = vp2_gallery.findViewById(R.id.gv_gallery);

        //参照中タブ
        int page = vp2_gallery.getCurrentItem();

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
     * 指定されたリスト内にサムネイルがある場合、除外する
     */
    private void removeThumbnail(PictureArrayList<PictureTable> pictures) {
        int i = 0;
        for (PictureTable picture : pictures) {
            if (picture.isThumbnail()) {
                //サムネイルがあれば、リストから除外
                pictures.remove(i);
                break;
            }
            i++;
        }
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
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures();

        //選択写真がサムネイルしかなければ、移動不可
        if ((selectedPictures.size() == 1) && (selectedPictures.get(0).isThumbnail())) {
            Toast.makeText(this, getString(R.string.toast_cannotThumbnailMove), Toast.LENGTH_SHORT).show();
            return;
        }

        //選択写真の中から、サムネイル写真を除外
        removeThumbnail(selectedPictures);

        //マップ上のピクチャノードを移動先候補として表示
        PictureNodesBottomSheetDialog bottomSheetDialog = PictureNodesBottomSheetDialog.newInstance(mPictureNodeInfo);
        bottomSheetDialog.show(getSupportFragmentManager(), "");
    }

    /*
     * 写真の削除（格納先から除外）確認
     */
    private void confirmDeletePicture() {

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures();

        //選択写真がサムネイルしかなければ、削除不可
        if ((selectedPictures.size() == 1) && (selectedPictures.get(0).isThumbnail())) {
            Toast.makeText(this, getString(R.string.toast_cannotThumbnailDelete), Toast.LENGTH_SHORT).show();
            return;
        }

        //メッセージ（複数選択用）
        String message = getString(R.string.alert_deletePicture_message) + getString(R.string.alert_deletePicture_messageAdd);

        //削除確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_deletePicture_title))
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //選択中の写真を削除
                        deletePicturesOnDB();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.SERIF);
    }


    /*
     * 写真の削除（格納先から除外）
     */
    private void deletePicturesOnDB() {

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures();

        //選択写真の中から、サムネイル写真を除外
        removeThumbnail(selectedPictures);

        //DBから写真を削除
        AsyncDeletePicture db = new AsyncDeletePicture(this, selectedPictures, new AsyncDeletePicture.OnFinishListener() {
            @Override
            public void onFinish(boolean isThumbnail, int srcPictureNodePid) {
                //アダプタを更新
                removeGallery(selectedPictures, isThumbnail);
                //削除メッセージ
                Context context = findViewById(R.id.vp2_gallery).getContext();
                Toast.makeText(context, getString(R.string.toast_deletePicture), Toast.LENGTH_SHORT).show();
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
    public void setToolbarOptionMenu(boolean isMulti) {

        //メニュー
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        Menu menu = toolbar.getMenu();

        //タイトル
        TextView tv_toolbarGalleryTitle = toolbar.findViewById(R.id.tv_toolbarGalleryTitle);

        if (isMulti) {
            //複数選択用メニュー
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_gallery_multi, menu);

            //ツールバー
            toolbar.setBackgroundColor(Color.BLACK);
            tv_toolbarGalleryTitle.setTextColor(Color.WHITE);
            tv_toolbarGalleryTitle.setText(R.string.toolbar_titleGalleryMulti);

        } else {
            if (!menu.hasVisibleItems()) {
                //既に閉じていれば何もしない
                return;
            }

            //クローズ指定
            menu.clear();
/*
            //★リリース後対応
            //通常メニュー
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.toolbar_gallery, menu);
*/

            //ツールバー
            toolbar.setBackgroundColor(Color.WHITE);
            tv_toolbarGalleryTitle.setTextColor(Color.BLACK);
            tv_toolbarGalleryTitle.setText(R.string.toolbar_titleGallery);
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

                        Log.i("複数選択確定", "addOnGlobalLayoutListener");

                        //ギャラリーの選択中状態を解除
                        GalleryPageAdapter galleryPageAdapter = (GalleryPageAdapter) vp2_gallery.getAdapter();
                        galleryPageAdapter.cancellationMultipleSelection(gv_gallery);
                    }
                }
        );


        vp2_gallery.post(() -> {

            Log.i("複数選択確定", "post");

            //ギャラリーの選択中状態を解除
            GalleryPageAdapter galleryPageAdapter = (GalleryPageAdapter) vp2_gallery.getAdapter();
            galleryPageAdapter.cancellationMultipleSelection(gv_gallery);
        });
    }

    /*
     * ギャラリーの削除
     *   引数に指定された写真をリストから削除し、アダプタに更新通知を送る
     *   para1：削除された写真リスト
     */
    public void removeGallery(PictureArrayList<PictureTable> selectedPictures, boolean isThumbnail) {

        //タブで表示しているサムネイルの更新
        disableTabThumbnail(isThumbnail);

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

        //選択状態を解除
        closeMultipleOptionMenu();

        //タブで表示しているサムネイルの更新
        disableTabThumbnail(isThumbnail);

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
    private void disableTabThumbnail(boolean isThumbnail) {

        //サムネイルがなければ何もしない
        if (!isThumbnail) {
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
                BitmapFactory.decodeResource(getResources(), R.drawable.baseline_no_image)
        );
    }

    /*
     * 操作対象（複数選択された時のタブ）ページのギャラリーを更新
     *   para1：移動された写真リスト
     */
    private void updateSourceGallery(PictureArrayList<PictureTable> selectedPictures) {

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
        GalleryGridAdapter adapter = (GalleryGridAdapter) gv_gallery.getAdapter();
        adapter.notifyDataSetChanged();
    }


    /*
     * ノード移動先のギャラリーの更新
     *   para1：移動された写真リスト
     */
    private void updateDestinationGallery(PictureArrayList<PictureTable> selectedPictures, int toPicutureNodePid) {

        //移動先ノードが表示されていなければ、終了
        int pageIndex = isDisplayTab(toPicutureNodePid);
        if (pageIndex == NOTHING) {
            return;
        }

        Log.i("ページ更新", "移動先のページ=" + pageIndex);

        //移動先ノードのギャラリーに追加
        PictureArrayList<PictureTable> gallery = mGalleries.get(pageIndex);
        gallery.addAll(selectedPictures);

        //アダプタ更新リストに追加（あれば不要）
        if (!mUpdatePages.contains(pageIndex)) {
            mUpdatePages.add(pageIndex);

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
        for (int i = 1; i < size; i++) {
            mGalleries.get(0).addAll(mGalleries.get(i));
        }

        //アダプタ更新リストに追加（あれば不要）
        if (!mUpdatePages.contains(ALL_PAGE_INDEX)) {
            mUpdatePages.add(ALL_PAGE_INDEX);

            Log.i("ページ更新", "更新対象追加=" + ALL_PAGE_INDEX);
        }

        //--
        for (PictureTable tmp : mGalleries.get(0)) {
            Log.i("ページ更新", "「すべて」" + tmp.getPid());
        }
        for (Integer tmp : mUpdatePages) {
            Log.i("ページ更新", "「mUpdatePages」" + tmp);
        }
        //--

    }

    /*
     * 表示中のタブにあるかどうか
     *   return：ページindex（ページ先頭には「すべて」があるため、１加算する）
     */
    private int isDisplayTab(int toPicutureNodePid) {

        final int TOP_PAGE = 1;

        int i = 0;
        for (Integer pid : mPictureNodePids) {
            if (pid == toPicutureNodePid) {
                return (i + TOP_PAGE);
            }
            i++;
        }

        return NOTHING;
    }

    /*
     * 外部ストレージとギャラリーの同期に関する注意事項の表示
     */
    private void showCautionAboutSyncStorage() {

        final String key = ResourceManager.SHARED_KEY_HELP_ON_GALLERY;

        //表示の有無を取得
        SharedPreferences spData = getSharedPreferences(ResourceManager.SHARED_DATA_NAME, MODE_PRIVATE);
        boolean isShow = spData.getBoolean(key, ResourceManager.INVALID_SHOW_HELP);

        if (!isShow) {
            //表示なしが選択されていれば何もしない
            return;
        }

        //ダイアログ
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_launch_gallery_title))
                .setMessage(getString(R.string.alert_launch_gallery_message))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = spData.edit();
                        editor.putBoolean(key, false);
                        editor.apply();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.SERIF);
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
                    GalleryGridAdapter adapter = (GalleryGridAdapter) gv_gallery.getAdapter();
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

    /*
     * 格納先候補のボトムシートのリスナー
     *   サムネイルクリックリスナー
     */
    @Override
    public void onThumbnailClick(BottomSheetDialogFragment dialog, int toPicutureNodePid) {

        //複数選択状態になっていない状態なら、メッセージを表示するだけ(画面向き変更対策)
        Toolbar toolbar = findViewById(R.id.toolbar_gallery);
        Menu menu = toolbar.getMenu();
        if ( !menu.hasVisibleItems() ) {
            Toast.makeText(this, getString(R.string.toast_reSelectPicture), Toast.LENGTH_SHORT).show();
            return;
        }

        //選択ノードチェック
        boolean isSame = checkSameNode(toPicutureNodePid);
        if( isSame ){
            Toast.makeText(this, getString(R.string.toast_samePicture), Toast.LENGTH_SHORT).show();
            return;
        }

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures();

        //選択写真の中に、サムネイル写真が含まれているか
        boolean containThumbnail = false;
        for( PictureTable picture: selectedPictures ){
            if( picture.isThumbnail() ){
                //サムネイル写真あれば、チェック終了
                containThumbnail = true;
                break;
            }
        }

        //移動確認のダイアログを表示
        confirmMoveDialog(dialog, toPicutureNodePid, containThumbnail );
    }

    /*
     * 格納先ノードが現在格納中ノードと同じか
     *   para1：移動先として選択されたピクチャノードpid
     */
    private boolean checkSameNode(int toPicutureNodePid) {

        //参照中タブ
        ViewPager2 vp2_gallery = findViewById(R.id.vp2_gallery);
        int page = vp2_gallery.getCurrentItem();

        //参照中のピクチャノードのpid
        //※先頭は「すべて」タブであるため、参照indexは１小さい位置になる
        int tabPictureNodePid = mPictureNodePids.get(page - 1);

        //移動先に選択されたノードが、移動元ノードと同じであれば、終了
        if (toPicutureNodePid == tabPictureNodePid) {
            return true;
        }

        return false;
    }

    /*
     * 移動確認用ダイアログの表示
     */
    private void confirmMoveDialog(BottomSheetDialogFragment dialog, int toPicutureNodePid, boolean isThumbnail) {

        //表示メッセージ
        String message = getString(R.string.alert_movePicture_message) + getString(R.string.alert_movePicture_messageAdd);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
            .setTitle( getString(R.string.alert_movePicture_title) )
            .setMessage( message )
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //格納先変更処理
                    moveMultiplePicture(toPicutureNodePid);
                    //閉じる
                    dialog.dismiss();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView)alertDialog.findViewById(android.R.id.message)).setTypeface( Typeface.SERIF );
    }

    /*
     * ノード移動処理（複数写真）
     */
    private void moveMultiplePicture( int toPicutureNodePid ) {

        Context context = this;

        //選択写真
        PictureArrayList<PictureTable> selectedPictures = getSelectedPictures();

        //ピクチャテーブルの所属を更新
        AsyncUpdateBelongsPicture db = new AsyncUpdateBelongsPicture(this, toPicutureNodePid, selectedPictures,
                new AsyncUpdateBelongsPicture.OnFinishListener() {
                    @Override
                    public void onFinish(boolean isThumbnail) {
                    }
                    @Override
                    public void onFinish(boolean isThumbnail, PictureArrayList<PictureTable> movedPictures) {
                        //移動結果をギャラリーに反映
                        updateGallery( movedPictures, toPicutureNodePid, isThumbnail );

                        //複数選択モードを解除
                        cancellationSelected();

                        //移動完了メッセージを表示
                        if( movedPictures.size() == 0 ){
                            //移動写真なし
                            Toast.makeText(context, getString(R.string.toast_notMoves), Toast.LENGTH_SHORT).show();
                        } else{
                            //移動写真あり
                            Toast.makeText(context, getString(R.string.toast_moved), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //非同期処理開始
        db.execute();
    }

}
