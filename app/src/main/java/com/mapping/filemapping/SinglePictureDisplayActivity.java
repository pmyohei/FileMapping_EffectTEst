package com.mapping.filemapping;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/*
 * 写真単体表示アクティビティ
 */
public class SinglePictureDisplayActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_UPDATE = 100;
    /* 画面遷移-キー */
    public static String UPDATE = "update";

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
        int showPosition = intent.getIntExtra("position", 0);;

        //格納先更新フラグ（削除か格納先の移動が発生したとき、フラグを更新する）
        mIsUpdate = false;

        //
        mIsImagePinchUp = false;

        //ピクチャノード情報リストを生成
        createPictureNodeInfos();

        Log.i("単体表示", "galley.size()=" + mGalley.size());

        //各写真の表示設定
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        SinglePictureAdapter adapter = new SinglePictureAdapter(this, mGalley);
        vp2_singlePicture.setAdapter(adapter);

        //表示開始位置を設定
        vp2_singlePicture.setCurrentItem( showPosition );

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

                        int pageDiff = Math.abs( prePage - nextPage );

                        //ページ遷移している場合
                        if ( pageDiff == 1 ) {
                            //ページ遷移が１ページだけ

                            //Log.i("ページ更新チェック", "★更新発生 preCurrent=" + prePage);
                            Log.i("ページ更新チェック", "★更新発生 通知テスト ピンチフラグ 通知判定直前 preCurrent→" + prePage);
                            Log.i("ページ更新チェック", "★更新発生 通知テスト ピンチフラグ 通知判定直前 current→" + nextPage);

                            //参照していた写真が拡大された状態で画面遷移された場合
                            //if (preMatrixImageView != null && preMatrixImageView.isPinchUp()) {
                            if ( mIsImagePinchUp ) {
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
                if( current == (vp2_singlePicture.getAdapter().getItemCount() - 1) ){
                    //一応ガード
                    return;
                }

                //遷移前のページとして保持
                mIconTouchPage = current;
                //ページを変更
                vp2_singlePicture.setCurrentItem( current + 1 );

                //ページ送り後はアイコン非表示
                view.setVisibility( View.GONE );
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
                if( current == 0 ){
                    //一応ガード
                    return;
                }

                //遷移前のページとして保持
                mIconTouchPage = current;
                //ページを変更
                vp2_singlePicture.setCurrentItem( current - 1 );

                //ページ送り後は非表示に
                view.setVisibility( View.GONE );
                //スクロールでのページ送りをリセット（ページ送り有効化）
                vp2_singlePicture.setUserInputEnabled(true);
            }
        });

        //写真削除リスナー
        findViewById(R.id.iv_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //確認ダイアログを表示
                confirmDeletePicture();
            }
        });

        //写真移動リスナー
        findViewById(R.id.iv_changeNode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //移動先候補を表示
                showMoveDestination();
            }
        });
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
     * 格納ノードからの削除確認
     */
    private void confirmDeletePicture() {

        //削除確認ダイアログを表示
        new AlertDialog.Builder(this)
                .setTitle("写真の削除")
                .setMessage("ノードから写真を削除します。\n※端末上から写真は削除されません。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //表示中の写真のindex
                        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
                        int index = vp2_singlePicture.getCurrentItem();

                        //ノードから削除対象のピクチャ
                        PictureTable picture = mGalley.get(index);

                        //DBからノード削除
                        AsyncDeletePicture db = new AsyncDeletePicture( vp2_singlePicture.getContext(), picture, new AsyncDeletePicture.OnFinishListener() {
                            @Override
                            public void onFinish(boolean isThumbnail) {
                                //アダプタから削除
                                updatePictureAdapter();
                            }
                        });

                        //写真削除後は、ページ送り有効にする
                        vp2_singlePicture.setUserInputEnabled(true);

                        //非同期処理開始
                        db.execute();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    /*
     * 所属するピクチャノードの変更先をダイアログで表示
     */
    private void showMoveDestination() {

        //参照中の写真
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        int index = vp2_singlePicture.getCurrentItem();
        PictureTable showPicture = mGalley.get(index);

        //マップ上のピクチャノードを移動先候補として表示
        PictureNodesBottomSheetDialog bottomSheetDialog = new PictureNodesBottomSheetDialog(this, mPictureNodeInfo, showPicture);
        bottomSheetDialog.show(getSupportFragmentManager(), "");
    }

    /*
     * 単体表示中の写真をアダプタから削除
     */
    public void updatePictureAdapter() {

        //トースト表示
        Toast.makeText(this, "写真を削除しました", Toast.LENGTH_SHORT).show();

        //表示中の写真をリストから削除
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        int index = vp2_singlePicture.getCurrentItem();

        //削除通知
        SinglePictureAdapter adapter = (SinglePictureAdapter) vp2_singlePicture.getAdapter();
        if( adapter != null ){
            adapter.removeItem( index );
        }

        //Intentに更新ありの情報を設定
        setUpdateIntent();
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

    /*
     * 写真が拡大されているか否かを設定する
     *   ※本メソッドは、単体写真用ビューから、ピンチ操作があった時に
     * 　　直接コールされる想定
     */
    public void setIsImagePinchUp( boolean isPinchUp ) {
        mIsImagePinchUp = isPinchUp;
    }

}