package com.mapping.filemapping;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/*
 * 写真単体表示アクティビティ
 */
public class SinglePictureDisplayActivity extends AppCompatActivity implements PictureNodesBottomSheetDialog.NoticeDialogListener {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_UPDATE = 100;
    /* 画面遷移-キー */
    public static String UPDATE = "update";;
    /* Bundle-キー */
    public static String IS_UPDATE = "isUpdate";

    //ページ送りボタン未押下
    public static final int NO_TOUCH_PAGE_FEED_ICON = -1;

    //表示する写真リスト
    ArrayList<PictureTable> mGalley;
    //マップ上のピクチャノード情報
    private ArrayList<PictureNodesBottomSheetDialog.PictureNodeInfo> mPictureNodeInfo;
    //写真の格納先変更フラグ
    private boolean mIsUpdate;
    //ページ遷移アイコン押下時のページindex
    private int mIconTouchPage;
    //表示中の写真が拡大しているか否か
    private boolean mIsImagePinchUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture_display);

        Intent intent = getIntent();
        //選択されたギャラリーのリスト
        mGalley = (ArrayList) intent.getSerializableExtra("pictures");
        //表示開始位置
        int showPosition = intent.getIntExtra("position", 0);
        ;

        //格納先更新フラグ（削除か格納先の移動が発生したとき、フラグを更新する）
        mIsUpdate = false;

        //写真ピンチ操作有無
        mIsImagePinchUp = false;

        //ツールバー設定
        setToolBar();

        //ピクチャノード情報リストを生成
        createPictureNodeInfos();

        Log.i("単体表示", "galley.size()=" + mGalley.size());

        //各写真の表示設定
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        SinglePictureAdapter adapter = new SinglePictureAdapter(this, mGalley);
        vp2_singlePicture.setAdapter(adapter);

        //表示開始位置を設定
        vp2_singlePicture.setCurrentItem(showPosition);

        //ページスクロールリスナー
        vp2_singlePicture.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            //ページ遷移前のページindex
            private int prePage = 0;

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i("コール順確認", "onPageScrollStateChanged state=" + state);

                //スクロール状態
                switch (state) {

                    /*------------------
                       ドラッグ開始
                       ※タッチでのページ送りのみこの状態がコールされる
                        アイコンでの遷移時、この状態ではコールされない
                        --------------------------*/
                    case SCROLL_STATE_DRAGGING:
                        //ページ遷移前のページindexを取得
                        prePage = vp2_singlePicture.getCurrentItem();

                        //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（ドラッグ）=" + prePage);
                        //Log.i("コール順確認", "どらっぎんぐタイミングの写真ビューページ=" + preMatrixImageView.page);

                        break;

                    /*------------------
                        SCROLL_STATE_DRAGGING の次にコールされる
                        ※アイコン操作によるページ送りの場合、SCROLL_STATE_DRAGGINGがコールされることなく
                        本状態がコールされる
                      -------------------------*/
                    case SCROLL_STATE_SETTLING:

                        if (mIconTouchPage == NO_TOUCH_PAGE_FEED_ICON) {
                            //アイコン操作されていないなら、この時点のページ数を取得
                            prePage = vp2_singlePicture.getCurrentItem();
                            //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング スクロール）=" + prePage);
                        } else {
                            //アイコン操作されている場合、getCurrentItem()では正しい値を取得できないため、
                            //アイコンタッチ時に保持していたページを設定
                            prePage = mIconTouchPage;
                            //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング アイコン）=" + prePage);
                        }

                        //押下時の処理が完了したため、初期状態に戻す
                        mIconTouchPage = NO_TOUCH_PAGE_FEED_ICON;

                        //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング）=" + preCurrent);

                        break;

                    /*------------------
                        スクロールが安定（停止）して完全に次のページが表示された時
                        前回参照していたページと別のページになっており、
                        少しでも写真が拡大されているなら、
                        次に参照されたときは初期状態とするために、更新通知を送る
                        -------------------------------*/
                    case SCROLL_STATE_IDLE:
                        //遷移後のページを取得
                        int nextPage = vp2_singlePicture.getCurrentItem();

                        //アダプタ
                        RecyclerView.Adapter adapter = vp2_singlePicture.getAdapter();

                        //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（アイドル）=" + nextPage);
                        //Log.i("ページ更新チェック", "更新チェック preCurrent=" + prePage);
                        //Log.i("ページ更新チェック", "更新チェック current=" + nextPage);

                        int pageDiff = Math.abs(prePage - nextPage);

                        //ページ遷移している場合
                        if (pageDiff == 1) {
                            //ページ遷移が１ページだけ

                            //Log.i("ページ更新チェック", "★更新発生 preCurrent=" + prePage);
                            Log.i("ページ更新チェック", "★更新発生 通知テスト ピンチフラグ 通知判定直前 preCurrent→" + prePage);
                            Log.i("ページ更新チェック", "★更新発生 通知テスト ピンチフラグ 通知判定直前 current→" + nextPage);

                            //参照していた写真が拡大された状態で画面遷移された場合
                            //if (preMatrixImageView != null && preMatrixImageView.isPinchUp()) {
                            if (mIsImagePinchUp) {
                                Log.i("ページ更新チェック", "★更新発生 通知テスト ピンチフラグ 拡大しているので更新通知を送る→" + prePage);

                                //参照していたページを更新
                                adapter.notifyItemChanged(prePage);
                            }
                        }

                        //ピンチ状態リセット
                        mIsImagePinchUp = false;

                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.i("コール順確認", "onPageSelected ページ=" + position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("コール順確認", "onPageScrolled ページ=" + position);
            }
        });

        //アイコンタッチページ初期化
        mIconTouchPage = NO_TOUCH_PAGE_FEED_ICON;

        //ページ送りリスナー（右）
        findViewById(R.id.iv_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //次の写真へ
                int current = vp2_singlePicture.getCurrentItem();
                if (current == (vp2_singlePicture.getAdapter().getItemCount() - 1)) {
                    //一応ガード
                    return;
                }

                //遷移前のページとして保持
                mIconTouchPage = current;
                //ページを変更
                vp2_singlePicture.setCurrentItem(current + 1);

                //ページ送り後はアイコン非表示
                view.setVisibility(View.GONE);
                //スクロールでのページ送りをリセット（ページ送り有効化）
                vp2_singlePicture.setUserInputEnabled(true);
            }
        });

        //ページ送りリスナー（左）
        findViewById(R.id.iv_pre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //前の写真へ
                int current = vp2_singlePicture.getCurrentItem();
                if (current == 0) {
                    //一応ガード
                    return;
                }

                //遷移前のページとして保持
                mIconTouchPage = current;
                //ページを変更
                vp2_singlePicture.setCurrentItem(current - 1);

                //ページ送り後は非表示に
                view.setVisibility(View.GONE);
                //スクロールでのページ送りをリセット（ページ送り有効化）
                vp2_singlePicture.setUserInputEnabled(true);
            }
        });

        //写真削除リスナー
        findViewById(R.id.iv_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //サムネイル写真は選択不可
                if (isDisplayedPictureThumbnail()) {
                    Toast.makeText(view.getContext(), getString(R.string.toast_cannotThumbnailDelete), Toast.LENGTH_SHORT).show();
                    return;
                }

                //確認ダイアログを表示
                confirmDeletePicture();
            }
        });

        //写真移動リスナー
        findViewById(R.id.iv_changeNode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //サムネイル写真は選択不可
                if (isDisplayedPictureThumbnail()) {
                    Toast.makeText(view.getContext(), getString(R.string.toast_cannotThumbnailMove), Toast.LENGTH_SHORT).show();
                    return;
                }

                //移動先候補を表示
                showMoveDestination();
            }
        });
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
     * 表示中の写真を取得
     */
    private PictureTable getDisplayedPicture() {
        //表示中の写真のindex
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        int index = vp2_singlePicture.getCurrentItem();

        //表示中の写真
        return mGalley.get(index);
    }

    /*
     * 表示中写真のサムネイル判定
     */
    private boolean isDisplayedPictureThumbnail() {
        //表示中の写真
        PictureTable picture = getDisplayedPicture();
        return picture.isThumbnail();
    }

    /*
     * 格納ノードからの削除確認
     */
    private void confirmDeletePicture() {

        Context context = this;

        //削除確認ダイアログを表示
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.alert_deletePicture_title))
            .setMessage(getString(R.string.alert_deletePicture_message))
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //削除
                    deletePicturesOnDB(context);
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
    private void deletePicturesOnDB(Context context) {

        //ノードから削除対象のピクチャ
        PictureTable picture = getDisplayedPicture();

        //DBからノード削除
        AsyncDeletePicture db = new AsyncDeletePicture(context, picture, new AsyncDeletePicture.OnFinishListener() {
            @Override
            public void onFinish(boolean isThumbnail, int srcPictureNodePid) {
                //アダプタから削除
                removePictureInAdapter();

                //削除メッセージ
                Toast.makeText(context, getString(R.string.toast_deletePicture), Toast.LENGTH_SHORT).show();
            }
        });
        //非同期処理開始
        db.execute();

        //写真削除後は、ページ送り有効にする
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.setUserInputEnabled(true);
    }


    /*
     * 所属するピクチャノードの変更先をダイアログで表示
     */
    private void showMoveDestination() {
        //マップ上のピクチャノードを移動先候補として表示
        PictureNodesBottomSheetDialog bottomSheetDialog = PictureNodesBottomSheetDialog.newInstance(mPictureNodeInfo);
        bottomSheetDialog.show(getSupportFragmentManager(), "");
    }

    /*
     * 単体表示中の写真をアダプタから削除
     */
    public void removePictureInAdapter() {

        //Intentに更新ありの情報を設定
        setUpdateIntent();

        //最後の写真であれば、本アクティビティ終了
        //※サムネイルは対象外のため、すべて削除されることは仕様上ないため、フェールセーフ
        if( mGalley.size() == 1 ){
            finish();
            return;
        }

        //表示中の写真をリストから削除
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        int index = vp2_singlePicture.getCurrentItem();

        //削除通知
        SinglePictureAdapter adapter = (SinglePictureAdapter) vp2_singlePicture.getAdapter();
        if( adapter != null ){
            adapter.removeItem( index );
        }
    }

    /*
     * Intentに更新ありを設定
     *   ※一度のみ設定
     */
    private void setUpdateIntent() {
        //更新なしの場合
        if( !mIsUpdate ){
            //resultコード設定
            Intent intent = getIntent();
            intent.putExtra(UPDATE, true );
            setResult(RESULT_UPDATE, intent );

            //更新あり
            mIsUpdate = true;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //画面向き変更等で画面が再生成されるとき、現在の更新状態を保存
        outState.putBoolean(IS_UPDATE, mIsUpdate);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //再生成前の更新状態を取得
        mIsUpdate = savedInstanceState.getBoolean(IS_UPDATE );
        if( mIsUpdate ){
            //resultコード設定
            Intent intent = getIntent();
            intent.putExtra(UPDATE, true );
            setResult(RESULT_UPDATE, intent );

            //更新あり
            mIsUpdate = true;
        }
    }

    /*
     * 写真が拡大されているか否かを設定する
     *   ※本メソッドは、単体写真用ビューから、ピンチ操作があった時に
     * 　　直接コールされる想定
     */
    public void setIsImagePinchUp( boolean isPinchUp ) {
        mIsImagePinchUp = isPinchUp;
    }


    /*
     * 格納先候補のボトムシートのリスナー
     *   サムネイルクリックリスナー
     */
    @Override
    public void onThumbnailClick(BottomSheetDialogFragment dialog, int toPicutureNodePid) {

        //選択先に同じ写真があるかチェック
        checkHasSamePicture(dialog, toPicutureNodePid);
    }

    /*
     * 格納先ノード判定（単体写真）
     *   para1：移動先として選択されたピクチャノードpid
     */
    private void checkHasSamePicture(BottomSheetDialogFragment dialog, int toPicutureNodePid) {

        //参照中の写真
        PictureTable showPicture = getDisplayedPicture();

        Context context = this;

        //確認する写真パス
        String path = showPicture.getPath();
        //ピクチャテーブルを参照し、写真があるかどうかチェック
        AsyncHasPicture db = new AsyncHasPicture(this, toPicutureNodePid, path, new AsyncHasPicture.OnFinishListener() {
            @Override
            public void onFinish(boolean hasPicture) {

                if (hasPicture) {
                    //既に写真があれば、トースト表示して終了
                    Toast.makeText(context, getString(R.string.toast_samePicture), Toast.LENGTH_SHORT).show();
                    return;
                }

                //なければ移動確認のダイアログを表示
                confirmMoveDialog(dialog, toPicutureNodePid, showPicture.isThumbnail());
            }
        });

        db.execute();
    }

    /*
     * 移動確認用ダイアログの表示
     */
    private void confirmMoveDialog(BottomSheetDialogFragment dialog, int toPicutureNodePid, boolean isThumbnail) {

        //表示メッセージ
        String message = getString(R.string.alert_movePicture_message);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle( getString(R.string.alert_movePicture_title) )
                .setMessage( message )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                        //格納先変更処理
                        movePicture(toPicutureNodePid);
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
     * 格納先変更
     */
    private void movePicture(int toPicutureNodePid ) {

        //参照中の写真
        PictureTable showPicture = getDisplayedPicture();

        Context context = this;

        //ピクチャテーブルの所属を更新
        AsyncUpdateBelongsPicture db = new AsyncUpdateBelongsPicture(this, toPicutureNodePid, showPicture,
            new AsyncUpdateBelongsPicture.OnFinishListener() {
                @Override
                public void onFinish( boolean isThumbnail ) {
                    //移動した写真を単体表示中のアダプタから削除
                    removePictureInAdapter();
                    //移動写真あり
                    Toast.makeText(context, getString(R.string.toast_moved), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFinish(boolean isThumbnail, PictureArrayList<PictureTable> movedPictures){}
            });

        //非同期処理開始
        db.execute();
    }

}