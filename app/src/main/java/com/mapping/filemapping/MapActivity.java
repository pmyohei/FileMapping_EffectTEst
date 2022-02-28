package com.mapping.filemapping;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MapActivity extends AppCompatActivity {

    /* 画面遷移-レスポンスコード */
    public static final int RESULT_PICTURE_NODE = 200;
    public static final int RESULT_UPDATE_TUHMBNAIL = 201;
    public static final int RESULT_ADD_PICTURE = 202;

    /* 画面遷移-キー */
    public static String INTENT_MAP_PID = "MapPid";
    public static String INTENT_NODE_PID = "NodePid";
    public static String INTENT_COLORS = "colors";
    public static String INTENT_EDIT = "edit";

    //ノードフォーカス処理
    public static final int MOVE_CENTER = 0;
    public static final int MOVE_UPPER = 1;

    //無効ノードpid
    private static final int INVALID_PID = -1;

    /* マップ位置操作 */
    //GestureDetector
    private ScaleGestureDetector mPinchGestureDetector;
    private GestureDetector mScrollGestureDetector;
    //スクロール前マップ位置
    private float mPreTouchPosX;
    private float mPreTouchPosY;
    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;
    //ピンチ操作時のズレ値
    private float mPinchShiftX = 1.0f;
    private float mPinchShiftY = 1.0f;
    //スクリーン上部の中心座標(ノード生成ダイアログ表示時の残りの画面領域の中心)
    private int mTopScreanY = 0;
    //ピンチ操作発生フラグ
    private boolean mIsPinch = false;
    //フリング用Scroller
    private Scroller mFlingScroller;
    //DrawerLayoutのオープン状態
    private boolean mDrawerIsOpen = false;

    //マップテーブル
    MapTable mMap;

    //マップ内ノードリスト
    private NodeArrayList<NodeTable> mNodes;
    //private PictureArrayList<PictureTable> mThumbnails;

    /* 制御 */
    //ノード生成ができる状態か
    private boolean mEnableDrawNode = false;
    //親ノード変更モード
    private boolean mChangeParentMode = false;
    private int mChangeParentNodePid = INVALID_PID;

    //画面遷移ランチャー
    ActivityResultLauncher<Intent> mNodeOperationLauncher;
    ActivityResultLauncher<Intent> mGalleryLauncher;

    /*
     * 画面生成
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //遷移元からのデータ
        Intent intent = getIntent();
        mMap = (MapTable) intent.getSerializableExtra(MapListActivity.KEY_MAP);

        //ツールバー設定
        initToolBar();

        //フリング用スクロール生成
        mFlingScroller = new Scroller(this, new DecelerateInterpolator());

        //マップ共通データ初期化
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        mapCommonData.init();

        //ピンチ比率取得
        pinchDistanceRatioX = mapCommonData.getPinchDistanceRatioX();
        pinchDistanceRatioY = mapCommonData.getPinchDistanceRatioY();

        //リスナー生成
        mPinchGestureDetector = new ScaleGestureDetector(this, new PinchListener());
        mScrollGestureDetector = new GestureDetector(this, new ScrollListener());

        //画面遷移ランチャー（ノード操作関連）を作成
        mNodeOperationLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new NodeOperationResultCallback()
        );
        mGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new AddPictureResultCallback(this)
        );

        //画面上部の中心位置
        int screenHeight = ResourceManager.getScreenHeight(this);
        mTopScreanY = (int) (screenHeight * (1f - ResourceManager.NODE_DESIGN_DIALOG_RATIO)) / 2;

        //Log.d("移動", "mTopScreanX=" + mTopScreanX);
        //Log.d("移動", "mTopScreanY=" + mTopScreanY);

        //アクティビティ
        Activity activity = this;

        //DrawerLayout
        DrawerLayout dl_map = findViewById(R.id.dl_map);
        dl_map.addDrawerListener(new MapDrawerListener());
        //※アクティビティにタッチ処理を渡す設定
        //※LOCK_MODE_LOCKED_OPNEではダメ
        dl_map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (mDrawerIsOpen) {
                    //Drawerが開いているなら、Drawerを閉じる
                    return false;

                } else {
                    //Drawerが閉じているなら、アクティビティにタッチ処理を渡す
                    activity.onTouchEvent(motionEvent);
                    return true;
                }
            }
        });
        //スライドでは閉じないようにする
        dl_map.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        //DBからデータを取得
        AsyncReadNodes db = new AsyncReadNodes(this, mMap.getPid(), new AsyncReadNodes.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(NodeArrayList<NodeTable> nodeList, PictureArrayList<PictureTable> thumbnailList) {

                //マップ共通データ
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.setNodes(nodeList);
                mapCommonData.setThumbnails(thumbnailList);
                mapCommonData.createColorHistory(mMap, findViewById(R.id.fl_screenMap));

                //フィールド変数として保持
                mNodes = nodeList;
                //mThumbnails = thumbnailList;

                if (mEnableDrawNode) {
                    //ノード生成可能なら、マップ上にノードを生成
                    createMap();
                }

                //データは取得したため、フラグ更新
                mEnableDrawNode = true;
            }
        });

        //非同期処理開始
        db.execute();

        //マップ色を設定
        String firstColor = mMap.getFirstColor();
        if (firstColor != null) {
            setMapColor(firstColor);
        }

        //ルートノード
        RootNodeView v_rootnode = findViewById(R.id.v_rootnode);

        ViewTreeObserver observer = v_rootnode.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        v_rootnode.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if (mEnableDrawNode) {
                            //ノード生成可能なら、マップ上にノードを生成
                            createMap();
                        }

                        //レイアウト側は確定したため、フラグ更新
                        mEnableDrawNode = true;
                    }
                }
        );
    }

    /*
     * ツールバーの初期設定
     */
    private void initToolBar() {
        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_map);
        toolbar.setTitle( mMap.getMapName() );
        setSupportActionBar(toolbar);

        /*-- アニメーションにするならこの方法 --*/
        /*
        int colorFrom = getResources().getColor(R.color.transparent_20_white);
        int colorTo = getResources().getColor(R.color.transparent_0_white);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(1200);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                toolbar.setBackgroundColor((int)animator.getAnimatedValue());
                getWindow().setStatusBarColor( (int)animator.getAnimatedValue() );
            }
        });
        */

        //クリックリスナー
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //colorAnimation.start();

                //透明と半透明を切り替える
                int alpha = toolbar.getBackground().getAlpha();
                if( alpha == 0 ){
                    //ツールバー
                    toolbar.setBackgroundColor( getResources().getColor(R.color.transparent_20_white) );
                    //システムバー
                    //getWindow().setStatusBarColor( getResources().getColor(R.color.transparent_20_white) );
                } else {
                    //ツールバー
                    toolbar.setBackgroundColor( Color.TRANSPARENT );
                    //システムバー
                    //getWindow().setStatusBarColor( Color.TRANSPARENT );
                }
            }
        });

        //戻るボタンを有効化
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /*
     * マップ背景色の設定
     */
    public void setMapColor(String color) {

        int value = Color.parseColor(color);

        //マップ全体
        findViewById(R.id.fl_screenMap).setBackgroundColor(value);
        //システムバー
        getWindow().setStatusBarColor(value);
    }

    /*
     * マップのデフォルトカラーを取得
     */
    public String[] getMapDefaultColors() {
        return mMap.getDefaultColors();
    }

    /*
     * 画面遷移用ランチャー（トリミング画面）を取得
     */
    public ActivityResultLauncher<Intent> getTrimmingLauncher() {
        return mNodeOperationLauncher;
    }

    /*
     * 画面遷移用ランチャー（画像ギャラリー）を取得
     */
    public ActivityResultLauncher<Intent> getGalleryLauncher() {

        return mGalleryLauncher;
    }

    /*
     * ノードクリックリスナー処理
     */
    private void nodeClickListener(View node) {

        //開いているなら、何もしない
        if (((BaseNode) node).hasIconView()) {
            return;
        }

        //ボトムシートが開いている場合、ツールアイコンは開かせない

        //親ノード選択状態
        if (mChangeParentMode) {
            //親ノード変更処理
            confirmChangeParentNode((BaseNode) node);
        } else {
            //ツールアイコン表示処理
            showToolIcon(node);
        }
    }

    /*
     * ツールアイコンを表示
     */
    private void showToolIcon(View node) {

        //デザインボトムシートが開いているなら、ツールアイコンは表示しない
        DesignBottomSheet l_bottomSheet = findViewById(R.id.dbs_design);
        if (!l_bottomSheet.isCloseBottomSheet()) {
            return;
        }

        //ツールアイコンをレイアウトに追加
        FrameLayout fl_map = findViewById(R.id.fl_map);
        fl_map.addView(
                new ToolIconsView(this, (BaseNode) node, MapActivity.this),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /*
     * デザインボトムシートのオープン状態をチェック
     */
/*    private boolean isOpenDesignBottomSheet() {

        //BottomSheet
        DesignBottomSheet l_bottomSheet = findViewById(R.id.dbs_design);

        return ;
    }*/

    /*
     * 親ノード変更の確認
     *   para1：クリックされたノードビュー（変更先の親ノード）
     */
    private void confirmChangeParentNode(BaseNode baseNode) {

        NodeTable parentNode = baseNode.getNode();
        int parentPid = parentNode.getPid();

        //変更先が自分自身の場合、ピクチャノードの場合は、変更不可
        if ((mChangeParentNodePid == parentPid) || (parentNode.getKind() == NodeTable.NODE_KIND_PICTURE)) {
            Toast.makeText(this, getString(R.string.toast_errorChangeParent), Toast.LENGTH_SHORT).show();
            return;
        }

        //共通データ
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();
        NodeTable changeNode = nodes.getNode( mChangeParentNodePid );

        //現状の親ノードの場合、変更不可
        if ( changeNode.getPidParentNode() == parentPid ) {
            Toast.makeText(this, getString(R.string.toast_nowParent), Toast.LENGTH_SHORT).show();
            return;
        }

        //変更ノードの配下のノードの場合、変更不可
        NodeArrayList<NodeTable> childNodes = nodes.getUnderNodes( mChangeParentNodePid, true );
        if( childNodes.getNode(parentPid) != null ){
            Toast.makeText(this, getString(R.string.toast_childNode), Toast.LENGTH_SHORT).show();
            return;
        }

        //移動確認ダイアログを表示
        new AlertDialog.Builder(this)
            .setTitle( getString(R.string.alert_changeParent_title) )
            .setMessage( getString(R.string.alert_changeParent_message) )
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //マップ上ノードリストを更新
                    changeNode.setPidParentNode( parentNode.getPid() );
                    //ライン再描画
                    ((ChildNode)changeNode.getNodeView()).getLineView().reDrawParent( baseNode );

                    //更新対象キューに追加
                    mapCommonData.enqueUpdateNodeWithUnique( changeNode );

                    //親ノード選択状態を解除
                    disableChangeParentMode();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /*
     * マップ内の全てのノード・ラインを生成
     */
    private void createMap() {

        //初回起動ダイアログを表示
        showFirstLaunchDialog();

        //ノードの描画
        drawAllNodes();
    }

    /*
     * 初回起動時のヘルプダイアログを表示
     */
    private void showFirstLaunchDialog(){

        final String key = ResourceManager.SHARED_KEY_HELP_ON_MAP;

        //表示の有無を取得
        SharedPreferences spData = getSharedPreferences(ResourceManager.SHARED_DATA_NAME, MODE_PRIVATE);
        boolean isShow = spData.getBoolean( key, ResourceManager.INVALID_SHOW_HELP);

        Log.i("ヘルプ", "操作前=" + isShow);

        if( !isShow ){
            //表示なしが選択されていれば何もしない
            return;
        }

        //ヘルプダイアログを表示
        showHelpDialog();

        //一度表示した後は表示しない
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = spData.edit();
        editor.putBoolean( key, false );
        editor.apply();

        Log.i("ヘルプ", "操作後=" + spData.getBoolean( key, ResourceManager.INVALID_SHOW_HELP));
    }

    /*
     * ヘルプダイアログを表示
     */
    private void showHelpDialog() {
        //マップヘルプの表示
        DialogFragment helpDialog = new HelpDialog( HelpDialog.HELP_KIND_MAP );
        helpDialog.show( getSupportFragmentManager(), "");
    }

    /*
     * 全ノードの描画
     */
    private void drawAllNodes() {

        //マップレイアウト（ノード追加先）
        FrameLayout fl_map = findViewById(R.id.fl_map);

        //ライン描画種別
        //int lineDrawKind = NodeGlobalLayoutListener.LINE_NONE;

        //全ノード数ループ
        int nodeNum = mNodes.size();
        for (int i = 0; i < nodeNum; i++) {

            //対象ノード
            NodeTable node = mNodes.get(i);

            //最後のノードなら、全てのラインを描画
            //if (i == (nodeNum - 1)) {
            //    lineDrawKind = NodeGlobalLayoutListener.LINE_ALL;
            //}

            //ノードを描画
            drawNode(fl_map, node);
        }
    }

    /*
     * ノード（単体）の描画
     */
    public BaseNode drawNode(FrameLayout fl_map, NodeTable node) {

        //ノードビュー
        BaseNode nodeView;

        //ルートノード
        if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
            //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
            nodeView = findViewById(R.id.v_rootnode);

            //ビューにノード情報を設定
            nodeView.setNode(node);
            //中心座標を設定
            nodeView.addOnNodeGlobalLayoutListener();

        } else {

            //ビュー生成
            nodeView = ( (node.getKind() == NodeTable.NODE_KIND_NODE)
                            ? new NodeView(this, node)
                            : new PictureNodeView(this, node) );

            //ノードをマップに追加
            fl_map.addView(nodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                ViewGroup.LayoutParams.WRAP_CONTENT));

            //位置設定
            //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
            int left = node.getPosX();
            int top  = node.getPosY();

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) nodeView.getLayoutParams();
            mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

            //レイアウト確定後の処理を設定
            ((ChildNode) nodeView).addOnNodeGlobalLayoutListener();
        }

        //ノードクリックリスナー
        nodeView.setOnNodeClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nodeClickListener( view );
                }
            }
        );

        //ノードビューを保持
        node.setNodeView(nodeView);

/*        //ルートノード
        if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
            //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
            RootNodeView rootNodeView = findViewById(R.id.v_rootnode);

            Log.i("アイコン", "ルートノードの位置 左=" + rootNodeView.getLeft() + " 上=" + rootNodeView.getTop());

            //ビューにノード情報を設定
            rootNodeView.setNode(node);
            //中心座標を設定
            rootNodeView.addOnNodeGlobalLayoutListener();
            //NodeTable側でノードビューを保持
            node.setNodeView(rootNodeView);
            //ノードクリックリスナー
            rootNodeView.setOnNodeClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nodeClickListener( view );
                    }
                }
            );

            return rootNodeView;
        }

        //ノード生成
        ChildNode nodeView;
        if (node.getKind() == NodeTable.NODE_KIND_NODE) {
            //ノード
            nodeView = new NodeView(this, node);
        } else {
            //ピクチャノード
            nodeView = new PictureNodeView(this, node);
        }

        //ノードをマップに追加
        fl_map.addView(nodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //位置設定
        //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
        int left = node.getPosX();
        int top  = node.getPosY();

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) nodeView.getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

        Log.i("createNode", "setMargins left=" + left + " top=" + top + " mlp.rightMargin=" + mlp.rightMargin + " mlp.bottomMargin=" + mlp.bottomMargin);
        Log.i("createNode", "getWidth=" + nodeView.getWidth() + " getHeight=" + nodeView.getHeight());

        //レイアウト確定後の処理を設定
        ((ChildNode) nodeView).addOnNodeGlobalLayoutListener();

        //ノードクリックリスナー
        nodeView.setOnNodeClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nodeClickListener( view );
                }
            }
        );

        //ノードビューを保持
        node.setNodeView(nodeView);*/

        return nodeView;
    }

    /*
     *　ノードにフォーカスをあてる（画面中心に指定座標をもってくる）
     */
    public void focusNodeToCenterScreen(float nodeLeft, float nodeTop, int POS_KIND) {
        int height = 0;

        if (POS_KIND == MOVE_UPPER) {
            height = mTopScreanY;
        }

        //float nodeLeft = v_node.getCenterPosX();
        //float nodeTop = v_node.getCenterPosY();

        //スクロール開始位置（現在のマップ絶対位置）（ピンチ操作のずれを考慮）
        FrameLayout fl_map = findViewById(R.id.fl_map);
        float mapAbsX = fl_map.getTranslationX() + mPinchShiftX;
        float mapAbsY = fl_map.getTranslationY() + mPinchShiftY;

        //ピンチ倍率１倍のマップ座標位置
        float mapAbs1xX = mapAbsX / pinchDistanceRatioX;
        float mapAbs1xY = mapAbsY / pinchDistanceRatioY;

        //マップ絶対位置のマージンを取得（ルートノードを基準に算出）
        RootNodeView v_rootnode = findViewById(R.id.v_rootnode);
        float mapLeft = v_rootnode.getNodeLeft() - mapAbs1xX;      //※ルートノードが操作対象の場合を考慮し、getNodeLeft()を使用
        float mapTop = v_rootnode.getNodeTop() - mapAbs1xY;

        //移動量
        float moveDistanceX = (int) (pinchDistanceRatioX * (nodeLeft - mapLeft));
        float moveDistanceY = (int) (pinchDistanceRatioY * (nodeTop - mapTop + height));

        //スクロール時間 [milliseconds]
        final int MOVE_DURATION = 600;

        //Log.i("move中心", "移動先ノード nodeLeft=" + nodeLeft + " nodeTop=" + nodeTop);
        //Log.i("move中心", "mapLeft=" + mapLeft + " mapTop=" + mapTop);
        //Log.i("move中心", "スクロール開始位置 x=" + mapAbsX + " y=" + mapAbsY);

        //スクローラーを設定
        Scroller scroller = new Scroller(this, new DecelerateInterpolator());
        scroller.startScroll(
                (int) mapAbsX,
                (int) mapAbsY,
                (int) -moveDistanceX,
                (int) -moveDistanceY,
                MOVE_DURATION
        );

        //アニメーションを開始
        ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0, 1).setDuration(MOVE_DURATION);
        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                //Log.i("Scroller", "onAnimationUpdate");

                if (!scroller.isFinished()) {
                    scroller.computeScrollOffset();

                    fl_map.setTranslationX(scroller.getCurrX());
                    fl_map.setTranslationY(scroller.getCurrY());

                } else {
                    scrollAnimator.cancel();
                }
            }
        });
        scrollAnimator.start();

        //ピンチ操作のズレをクリア
        mPinchShiftX = 0;
        mPinchShiftY = 0;
    }

    /*
     * デザイン設定のBottomSheetを開く
     */
    public void openDesignBottomSheet( int designKind, View view, float nodeLeft, float nodeTop, int POS_KIND ) {
        //ボトムシートを開く
        openDesignBottomSheet( designKind, view );

        //ノードを画面上部の中心に移動させる
        focusNodeToCenterScreen(nodeLeft, nodeTop, MOVE_UPPER);
    }

    /*
     * デザイン設定のBottomSheetを開く
     */
    public void openDesignBottomSheet( int designKind, View view ) {
        //デザインボトムシートが既に開いているなら、開かない
        DesignBottomSheet l_bottomSheet = findViewById(R.id.dbs_design);
        if ( !l_bottomSheet.isCloseBottomSheet() ) {
            return;
        }

        //ボトムシートを開く
        l_bottomSheet.openBottomSheet(designKind, view);
    }

    /*
     * 親ノード変更モードを有効にする
     *   para1：親ノードを変更したいノードのpid
     */
    public void enableChangeParentMode(int changeParentNodePid ) {
        mChangeParentMode = true;
        mChangeParentNodePid = changeParentNodePid;

        //ツールバーを変更
        setToolbarOptionMenu(true);
    }

    /*
     * 親ノード変更モードを解除する
     */
    public void disableChangeParentMode() {
        mChangeParentMode = false;
        mChangeParentNodePid = INVALID_PID;

        //ツールバーを変更
        setToolbarOptionMenu(false);
    }

    /*
     * メニュー設定
     */
    public void setToolbarOptionMenu(boolean isChangeParent) {

        //メニューをクローズ
        Toolbar toolbar = findViewById(R.id.toolbar_map);
        Menu menu = toolbar.getMenu();
        menu.clear();

        //メニューインフレータ
        MenuInflater inflater = getMenuInflater();

        if ( isChangeParent ) {
            //親ノード変更モード
            inflater.inflate(R.menu.toolbar_close, menu);

            toolbar.setBackgroundColor(Color.WHITE);
            toolbar.setTitle( getString(R.string.toolbar_map_changeParant) );
        } else {
            //通常モード
            inflater.inflate(R.menu.toolbar_map, menu);

            toolbar.setBackgroundColor(Color.TRANSPARENT);
            //toolbar.setTitleTextColor(Color.BLACK);
            toolbar.setTitle( mMap.getMapName() );
        }
    }

    /*
     * onStop()
     */
    @Override
    protected void onStop() {
        //必須
        super.onStop();

        //位置情報を保存
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        NodeArrayList<NodeTable> nodeQue = mapCommonData.getUpdateNodeQue();

/*        Log.i("親変更", "nodeQue.size()=" + nodeQue.size());

        for( NodeTable node: nodeQue){
            Log.i("親変更", "ノード名=" + node.getNodeName() + " 親ノード=" + node.getPidParentNode());
        }*/

        //座標移動したノードがあれば
        if (nodeQue.size() > 0) {
            AsyncUpdateNode db = new AsyncUpdateNode(this, nodeQue, new AsyncUpdateNode.OnFinishListener() {
                //DB処理完了
                @Override
                public void onFinish() {
                    //更新完了後は、キュークリア
                    mapCommonData.clearUpdateNodeQue();
                }
            });

            //非同期処理開始
            db.execute();
        }
    }

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //初期メニュー
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_map, menu);

        return true;
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
     * ツールバーアクション選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_help:
                //マップヘルプの表示
                showHelpDialog();
                return true;

            case R.id.action_palette:
                //マップ全体デザイン変更
                openDesignBottomSheet(DesignBottomSheet.MAP, findViewById(R.id.fl_screenMap));
                return true;

            case R.id.action_folder_tree:
                //フォルダーツリー表示
                DrawerLayout drawer = findViewById(R.id.dl_map);
                drawer.openDrawer(GravityCompat.END);

                return true;

            case R.id.action_close:
                //親ノード変更モードの解除
                disableChangeParentMode();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /*
     * タッチイベント
     */
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        //現在のスクロールを停止
        mFlingScroller.forceFinished(true);

        //ピンチ操作リスナーをコール
        mPinchGestureDetector.onTouchEvent(motionEvent);

        //ピンチ操作があれば、ここで終了
        if (mIsPinch) {
            //指を離れれば、ピンチ操作OFFに戻す
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                mIsPinch = false;
            }
            return true;
        }

        //タッチ移動処理
        FrameLayout fl_map = findViewById(R.id.fl_map);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //タッチ開始時のタッチ位置
                mPreTouchPosX = motionEvent.getX();
                mPreTouchPosY = motionEvent.getY();
                break;

            case MotionEvent.ACTION_MOVE:

                float distanceX = motionEvent.getX() - mPreTouchPosX;
                float distanceY = motionEvent.getY() - mPreTouchPosY;

                //Log.i("MotionEvent", "preX=" + mPreTouchPosX + " preY=" + mPreTouchPosY);
                //Log.i("MotionEvent", "getX=" + motionEvent.getX() + " getY=" + motionEvent.getY());
                //Log.i("MotionEvent", "diffX=" + distanceX + " diffY=" + distanceY);

                //現在位置
                float x = fl_map.getTranslationX();
                float y = fl_map.getTranslationY();

                //前回からの移動量を反映
                fl_map.setTranslationX(x + distanceX);
                fl_map.setTranslationY(y + distanceY);

                mPreTouchPosX = motionEvent.getX();
                mPreTouchPosY = motionEvent.getY();

                break;

            case MotionEvent.ACTION_CANCEL:
                // something to do
                break;
        }

        //スクロール操作リスナーをコール
        mScrollGestureDetector.onTouchEvent(motionEvent);

        return false;
    }


    /*
     * ピンチ（拡大・縮小）操作リスナー
     */
    private class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        //スケールの上限下限
        final float LOWER_SCALE = 0.5f;
        final float UPPER_SCALE = 2.0f;

        //親レイアウト
        FrameLayout mfl_map;
        //ルートノード
        View rnv_rootnode;
        //ルートノードとの距離把握用
        View mv_base;

        //親レイアウト(マップ全体)のスケール比率
        float mPinchScaleX;
        float mPinchScaleY;

        //ピンチ操作開始時の距離参照用ビュー間距離
        float startDistanceX;
        float startDistanceY;

        //ピンチ操作開始時のルートノード位置
        //（ピンチ操作時の位置ズレ把握用）
        float startRootPosX;
        float startRootPosY;

        /*
         * コンストラクタ
         */
        public PinchListener() {
            mfl_map  = findViewById(R.id.fl_map);
            rnv_rootnode = findViewById(R.id.v_rootnode);
            mv_base  = findViewById(R.id.v_base);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            //Log.i("onScale", "onScaleBegin");

            //ピンチ操作発生
            mIsPinch = true;

            //ピンチ操作開始時点のマップスケールを保持
            mPinchScaleX = mfl_map.getScaleX();
            mPinchScaleY = mfl_map.getScaleY();

            //スクリーン座標を取得
            int[] locationRoot = new int[2];
            int[] locationBase = new int[2];
            rnv_rootnode.getLocationInWindow(locationRoot);
            mv_base.getLocationInWindow(locationBase);

            //Log.i("onScaleBegin", "mCenterNode location=" + locationRoot[0] + " location=" + locationRoot[1]);
            //Log.i("onScaleBegin", "v_tmp location=" + locationBase[0] + " location=" + locationBase[1]);

            //2点間のスクリーン座標上の距離を保持
            startDistanceX = locationBase[0] - locationRoot[0];
            startDistanceY = locationBase[1] - locationRoot[1];

            //ルートノードの位置を保持
            startRootPosX = locationRoot[0];
            startRootPosY = locationRoot[1];

            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float scaleFactor = detector.getScaleFactor();

            //Log.i("onScale", "getScaleFactor=" + scaleFactor);
            Log.i("ピンチ機能", "設定比率=" + mPinchScaleX * scaleFactor);

            //設定比率
            float setScaleValue = mPinchScaleX * scaleFactor;
            //上限下限に割り込んでいれば、限界値に設定
            if( setScaleValue < LOWER_SCALE ){
                setScaleValue = LOWER_SCALE;
            }else if( setScaleValue > UPPER_SCALE ){
                setScaleValue = UPPER_SCALE;
            }

            //ピンチ操作開始時の比率に、ピンチ操作中の比率を掛ける
            mfl_map.setScaleX(setScaleValue);
            mfl_map.setScaleY(setScaleValue);

            return super.onScale(detector);
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            //スクリーン座標を取得
            int[] locationRoot = new int[2];
            int[] locationBase = new int[2];
            rnv_rootnode.getLocationInWindow(locationRoot);
            mv_base.getLocationInWindow(locationBase);

            //ピンチ操作終了時の2点間距離を取得
            float endDistanceX = locationBase[0] - locationRoot[0];
            float endDistanceY = locationBase[1] - locationRoot[1];

            //マップのスケール比率(累積)
            pinchDistanceRatioX *= (endDistanceX / startDistanceX);
            pinchDistanceRatioY *= (endDistanceY / startDistanceY);

            //マップ情報を同期
            MapCommonData mapCommonData = (MapCommonData)getApplication();
            mapCommonData.setPinchDistanceRatio(pinchDistanceRatioX, pinchDistanceRatioY);

            //Log.i("onScaleEnd", "pinchDistanceRatioX=" + pinchDistanceRatioX + " pinchDistanceRatioY=" + pinchDistanceRatioY);

            //ピンチ開始時との位置のズレを保持
            //※連続で操作される可能性があるため、累計させる
            mPinchShiftX += (startRootPosX - locationRoot[0]);
            mPinchShiftY += (startRootPosY - locationRoot[1]);

            //Log.i("Pinch", "mPinchShiftX=" + mPinchShiftX + " mPinchShiftY=" + mPinchShiftY);

            super.onScaleEnd(detector);
        }

    }

    /*
     * スワイプ操作リスナー
     *   ・スクロール
     *   ・フリング
     */
    private class ScrollListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            //Log.i("onFling", "velocityX=" + velocityX + " velocityY=" + velocityY);


//            mFlingScroller.fling(currentX, currentY, velocityX / SCALE, velocityY / SCALE, minX, minY, maxX, maxY);
//            postInvalidate();

            FrameLayout root = findViewById(R.id.fl_map);

            float nowx = root.getTranslationX();
            float nowy = root.getTranslationY();

            //Log.i("onFling", "nowx=" + nowx + " nowy=" + nowy);

            //スクローラー
            final float SCALE = 1.5f;
            final int MOVE_DURATION = 5000;

            // アニメーションを開始
            mFlingScroller.fling(
                    (int) nowx,                    //scroll の開始位置 (X)
                    (int) nowy,                    //scroll の開始位置 (Y)
                    (int) (velocityX / SCALE),     //初速
                    (int) (velocityY / SCALE),     //初速
                    -2000,  //★最大値に合わせて可変にする必要あり
                    2000,
                    -2000,
                    2000
            );

            //フリング操作時、加速度をスクロールに反映
            ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0, 1).setDuration(MOVE_DURATION);
            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {

                    if (!mFlingScroller.isFinished()) {
                        mFlingScroller.computeScrollOffset();

                        root.setTranslationX(mFlingScroller.getCurrX());
                        root.setTranslationY(mFlingScroller.getCurrY());

                    } else {
                        scrollAnimator.cancel();
                        //onScrollFinished();
                    }
                }
            });
            scrollAnimator.start();

            return false;
        }

    }

    /*
     * マップ画面のDrawerListener
     */
    private class MapDrawerListener implements DrawerLayout.DrawerListener {

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            Log.i("DrawerListener", "onDrawerOpened");
            mDrawerIsOpen = true;

            //ノードを階層化して表示
            setNodeHierarchy();
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            Log.i("DrawerListener", "onDrawerClosed");

            mDrawerIsOpen = false;

            //階層化ノードを削除
            LinearLayout ll_toAdd = findViewById(R.id.ll_toAdd);
            ll_toAdd.removeAllViews();
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            //Log.i("DrawerListener", "onDrawerStateChanged newState=" + newState);
        }
        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            //Log.i("DrawerListener", "onDrawerSlide");
        }


        /*
         * ノードを階層化してレイアウトに追加
         */
        private void setNodeHierarchy() {

            //共通データ
            MapCommonData commonData = (MapCommonData) getApplication();
            NodeArrayList<NodeTable> nodes = commonData.getNodes();
            //階層化ノードを取得
            NodeArrayList<NodeTable> hierarchyNodes = nodes.getHierarchyList();

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //追加先
            LinearLayout ll_toAdd = findViewById(R.id.ll_toAdd);

            //階層毎のマージン差分
            final int MARGIN_SIZE = 60;

            for (NodeTable node : hierarchyNodes) {
                //1行分のレイアウトをビュー化
                ViewGroup item = (ViewGroup) inflater.inflate(R.layout.item_hierarchy_node, null);
                //追加
                ll_toAdd.addView(item);

                //ノード情報を設定
                setNodeItem( item, node );

                //階層に対応するマージン値を算出
                int marginLeft = MARGIN_SIZE * hierarchyNodes.getHierarchyLevel(node);

                //背景色
                ll_toAdd.setBackgroundColor( Color.TRANSPARENT );

                //レイアウトパラメータ
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) item.getLayoutParams();
                mlp.setMargins(marginLeft, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
            }
        }

        /*
         * 各ノードの設定
         */
        private void setNodeItem( ViewGroup vg_item, NodeTable node ){

            //ノード名を設定
            TextView tv_node = vg_item.findViewById(R.id.tv_node);

            String nodeName = node.getNodeName();
            if( nodeName.isEmpty() ){
                //ノード名未入力なら、名称ない旨の文言を設定
                //ピクチャノードとそれ以外で文言を変える
                nodeName = ( (node.getKind() == NodeTable.NODE_KIND_PICTURE)
                        ? getResources().getString(R.string.no_nodeNamePictureNode)
                        : getResources().getString(R.string.no_nodeName) );

            } else {
                //ノード名がある場合、改行をなくす
                nodeName = nodeName.replaceAll("\n", "");
            }
            tv_node.setText( nodeName );

            //クリックリスナー
            vg_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ノードを画面中央に持ってくる
                    focusNodeToCenterScreen( node.getNodeView().getLeft(), node.getNodeView().getTop(), MOVE_CENTER);
                }
            });
        }
    }

    /*
     * 画面遷移からの戻りのコールバック通知
     *   ・ノード新規作成（ピクチャ）
     */
    private class NodeOperationResultCallback implements ActivityResultCallback<ActivityResult> {

        /*
         * 画面遷移先からの戻り処理
         */
        @Override
        public void onActivityResult(ActivityResult result) {

            Log.i("NodeOpeResultCallback", "ルートチェック");

            //インテント
            Intent intent = result.getData();
            //リザルトコード
            int resultCode = result.getResultCode();

            if( resultCode == RESULT_PICTURE_NODE) {
                //新規ピクチャノードを取得
                NodeTable pictureNode = (NodeTable)intent.getSerializableExtra(ResourceManager.KEY_CREATED_NODE);
                PictureTable thumbnail  = (PictureTable)intent.getSerializableExtra(ResourceManager.KEY_THUMBNAIL);

                //リストに追加
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.addNodes(pictureNode);
                //★サムネリストは廃止予定
                mapCommonData.addThumbnail(thumbnail);

                //ノードを描画
                drawNode(findViewById(R.id.fl_map), pictureNode);

            } else if( resultCode == RESULT_UPDATE_TUHMBNAIL) {
                //サムネイル変更

                //新たなサムネイル画像と元々設定されていたサムネイル画像を取得(nullの場合あり)
                PictureTable newThumbnail = (PictureTable)intent.getSerializableExtra(ResourceManager.KEY_NEW_THUMBNAIL);
                PictureTable oldThumbnail = (PictureTable)intent.getSerializableExtra(ResourceManager.KEY_OLD_THUMBNAIL);

                //サムネイルリストを更新
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                PictureTable currentThumbnail = mapCommonData.updateThumbnail(oldThumbnail, newThumbnail);

                if( currentThumbnail == null ){
                    //ないはずだが、一応フェールセーフ
                    return;
                }

                //ピクチャノードの画像を更新
                int pictureNodePid = currentThumbnail.getPidParentNode();
                NodeTable node = mapCommonData.getNodes().getNode( pictureNodePid );
                ((PictureNodeView)node.getNodeView()).updateThumbnail( currentThumbnail );
            }

        }
    }

    /*
     * 画面遷移からの戻りのコールバック通知
     *   ・写真追加（ギャラリーからの戻り）
     */
    private static class AddPictureResultCallback implements ActivityResultCallback<ActivityResult> {

        private final Context mContext;

        public AddPictureResultCallback( Context context ) {
            mContext = context;
        }

        @Override
        public void onActivityResult(ActivityResult result) {

            //リザルトコード
            //int resultCode = result.getResultCode();

            if (result.getResultCode() == RESULT_OK) {

                Intent intent = result.getData();
                if (intent == null) {
                    return;
                }

                //選択されているピクチャノード情報
                MapCommonData mapCommonData = (MapCommonData) ((ComponentActivity)mContext).getApplication();
                NodeArrayList<NodeTable> nodes =  mapCommonData.getNodes();
                NodeTable pictureNode = nodes.getShowingIconNode().getNode();

                int mapPid = pictureNode.getPidMap();
                int nodePid = pictureNode.getPid();

                //絶対pathリスト
                PictureArrayList<PictureTable> pictures = new PictureArrayList<>();

                ClipData clipData = intent.getClipData();
                if( clipData == null ){
                    //選択枚数1枚

                    //絶対パスを取得
                    Uri contentUri = intent.getData();
                    String path = ResourceManager.getPathFromUri(mContext, contentUri);

                    //ピクチャデータをリストに追加
                    pictures.add(
                            new PictureTable(mapPid, nodePid, path, false)
                    );

                } else{
                    //選択写真数
                    int pictureNum = clipData.getItemCount();
                    for( int i = 0; i < pictureNum; i++ ){
                        //コンテンツURIを取得
                        Uri contentUri = intent.getClipData().getItemAt(i).getUri();

                        //絶対パスを取得
                        String path = ResourceManager.getPathFromUri(mContext, contentUri);
                        if (path == null ) {
                            //絶対パスの取得に失敗した場合
                            Log.i("URI", "絶対パス取得エラー");
                            continue;
                        }

                        //ピクチャデータをリストに追加
                        pictures.add(
                                new PictureTable(mapPid, nodePid, path, false)
                        );

                        Log.i("ギャラリーに追加", "path=" + path);
                    }
                }

                if( pictures.size() == 0 ){
                    //追加写真がなければ終了
                    return;
                }

                //DBにピクチャデータとして保存
                //DB保存処理
                AsyncCreateGallery db = new AsyncCreateGallery(mContext, pictures, new AsyncCreateGallery.OnCreateListener() {
                    @Override
                    public void onCreate() {
                    }
                });

                //非同期処理開始
                db.execute();
            }

        }
    }
}
