package com.mapping.filemapping;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;
import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_SETTLING;

import androidx.appcompat.app.AppCompatActivity;
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

    //表示する写真リスト
    ArrayList<PictureTable> mGalley;
    //マップ上のピクチャノード情報
    private ArrayList<PictureNodesBottomSheetDialog.PictureNodeInfo> mPictureNodeInfo;
    //写真の格納先変更フラグ
    private boolean mIsUpdate;

    private int mIconTouchPage;

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

            boolean isStateSettingOn;
            int scrollingPage = 0;
            int prePage = 0;
            int selectedPage = 0;

            int preCurrent = 0;
            int current = 0;

            SingleMatrixImageView singleMatrixImageView;

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.i("コール順確認", "onPageScrollStateChanged state=" + state);

                if( state == SCROLL_STATE_DRAGGING ){
                    isStateSettingOn = false;
                    preCurrent = vp2_singlePicture.getCurrentItem();

                    Log.i("コール順確認", "onPageScrollStateChanged preCurrent（ドラッグ）=" + preCurrent);

                    singleMatrixImageView = vp2_singlePicture.findViewById( R.id.iv_singlePicture );

                    Log.i("コール順確認", "どらっぎんぐタイミングの写真ビューページ=" + singleMatrixImageView.page);

                }else if( state == SCROLL_STATE_SETTLING ){
                    isStateSettingOn = true;

                    if( mIconTouchPage < 0 ){
                        preCurrent = vp2_singlePicture.getCurrentItem();
                        Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング スクロール）=" + preCurrent);
                    }else{
                        preCurrent = mIconTouchPage;
                        Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング アイコン）=" + preCurrent);
                    }

                    mIconTouchPage = -1;


                    //ドラッグタイミングで取得できていない
                    if( singleMatrixImageView == null ){
                        singleMatrixImageView = vp2_singlePicture.findViewById( R.id.iv_singlePicture );
                    }

                    Log.i("コール順確認", "セットリングタイミングの写真ビューページ=" + singleMatrixImageView.page);

                    //Log.i("コール順確認", "onPageScrollStateChanged preCurrent（セットリング）=" + preCurrent);

                } else if( state == SCROLL_STATE_IDLE ){

                    current = vp2_singlePicture.getCurrentItem();

                    Log.i("コール順確認", "onPageScrollStateChanged preCurrent（アイドル）=" + current);


                    Log.i("ページ更新チェック", "更新チェック preCurrent=" + preCurrent);
                    Log.i("ページ更新チェック", "更新チェック current=" + current);

                    //ページ遷移している場合
                    if( preCurrent != current ){

                        Log.i("ページ更新チェック", "★更新発生 preCurrent=" + preCurrent);

                        Log.i("ページ更新チェック", "★更新発生 通知テスト 通知判定直前 preCurrent→" + preCurrent);
                        Log.i("ページ更新チェック", "★更新発生 通知テスト 通知判定直前 current→" + current);
                        Log.i("ページ更新チェック", "★更新発生 通知テスト 通知判定直前 isScaleUp→" + singleMatrixImageView.isPinchUp());

                        if( singleMatrixImageView != null && singleMatrixImageView.isPinchUp() ){
                            Log.i("ページ更新チェック", "★更新発生 通知テスト 拡大しているので更新通知を送る→" + preCurrent);

                            //前のページを更新
                            vp2_singlePicture.getAdapter().notifyItemChanged( preCurrent );
                        }
                    }

                    singleMatrixImageView = null;
                }

            }
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                Log.i("コール順確認", "onPageSelected ページ=" + position);

                //遷移先ページを保持
                selectedPage = position;

            }
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("コール順確認", "onPageScrolled ページ=" + position);

/*                //
                if( isStateSettingOn ){
                    prePage = position;

                    isStateSettingOn = false;
                }

                //スクロール中のページ
                scrollingPage = position;*/
            }
        });

        mIconTouchPage = -1;

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

                mIconTouchPage = current;
                vp2_singlePicture.setCurrentItem( current + 1 );

                //ページ送り後はアイコン非表示
                view.setVisibility( View.GONE );
                //スクロールでのページ送りをリセット（有効化）
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
                mIconTouchPage = current;
                vp2_singlePicture.setCurrentItem( current - 1 );


                //ページ送り後は非表示に
                view.setVisibility( View.GONE );
                //スクロールでのページ送りをリセット（有効化）
                vp2_singlePicture.setUserInputEnabled(true);
            }
        });

        //写真削除リスナー
        findViewById(R.id.iv_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //確認ダイアログを表示
                confirmDeletePicture();

                //お試し
                //vp2_singlePicture.getAdapter().notifyItemChanged( vp2_singlePicture.getCurrentItem() );
                //
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
     * ViewPagerのスクロール制御
     */
    public void controlScroll() {
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);

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
        //PictureNodesBottomSheetDialog bottomSheetDialog = PictureNodesBottomSheetDialog.newInstance(mPictureNodeInfo, showPicture);
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

}