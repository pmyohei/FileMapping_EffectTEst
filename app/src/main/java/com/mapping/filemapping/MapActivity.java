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

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.GestureDetector;
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
import android.widget.Scroller;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;

public class MapActivity extends AppCompatActivity {

    /* 画面遷移-リクエストコード */
    public static final int REQ_NODE_CREATE = 100;
    public static final int REQ_NODE_EDIT = 101;

    /* 画面遷移-キー-種別（遷移先での遷移理由識別用） */
    public static final String INTENT_KIND_CREATE = "Create";

    /* 画面遷移-キー */
    public static String INTENT_MAP_PID = "MapPid";
    public static String INTENT_NODE_PID = "NodePid";
    public static String INTENT_NODE = "Node";

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
    private float mPinchShiftX = 0;
    private float mPinchShiftY = 0;
    //ピンチ操作発生フラグ
    private boolean mIsPinch = false;
    //フリング用Scroller
    private Scroller mFlingScroller;
    //DrawerLayoutのオープン状態
    private boolean mDrawerIsOpen = false;

    //マップ内ノードリスト
    private NodeArrayList<NodeTable> mNodes;

    /* 制御 */
    //ノード生成ができる状態か
    private boolean mEnableDrawNode = false;

    //ノード操作発生時の画面遷移ランチャー
    ActivityResultLauncher<Intent> mNodeOperationLauncher;


    /*
     * 画面生成
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("(仮)マップ名を入れる");
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //フリング用スクロール生成
        mFlingScroller = new Scroller(this, new DecelerateInterpolator());

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        //初期化
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


        //暫定--------------
        //背景色
        //暫定--------------

        //アクティビティ
        Activity activity = this;

        //DrawerLayout
        DrawerLayout dl_map = findViewById(R.id.dl_map);
        dl_map.addDrawerListener(new MapDrawerListener());
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

        //DBからデータを取得
        Intent intent = getIntent();
        int mapPid = intent.getIntExtra(ResourceManager.KEY_MAPID, 0);
        AsyncReadNodes db = new AsyncReadNodes(this, mapPid, new AsyncReadNodes.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(NodeArrayList<NodeTable> nodeList) {

                //マップ共通データ
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.setNodes(nodeList);

                //フィールド変数としても保持する
                mNodes = nodeList;

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

        //ルートノード
        RootNodeView rnv_rootnode = findViewById(R.id.rnv_rootnode);

        ViewTreeObserver observer = rnv_rootnode.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        rnv_rootnode.getViewTreeObserver().removeOnGlobalLayoutListener(this);

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
     * マップ内の全てのノード・ラインを生成
     */
    private void createMap() {

        //ノードの生成
        drawAllNodes();
    }


    /*
     * 全ノードの描画
     */
    private void drawAllNodes() {

        //マップレイアウト（ノード追加先）
        FrameLayout fl_map = findViewById(R.id.fl_map);

        //ライン描画種別
        int lineDrawKind = NodeGlobalLayoutListener.LINE_NONE;

        //全ノード数ループ
        int nodeNum = mNodes.size();
        for (int i = 0; i < nodeNum; i++) {

            //対象ノード
            NodeTable node = mNodes.get(i);

            //最後のノードなら、全てのラインを描画
            if (i == (nodeNum - 1)) {
                lineDrawKind = NodeGlobalLayoutListener.LINE_ALL;
            }

            //ノードを描画
            drawNode(fl_map, node, lineDrawKind);
        }

        //---
        //ノードをマップに追加
        //TextView moveNode = new TextView(this);
        //moveNode.setText("CHECK");
        //fl_map.addView(moveNode, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ////位置設定
        //ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) moveNode.getLayoutParams();
        //mlp.setMargins(0, 0, mlp.rightMargin, mlp.bottomMargin);
        //---

    }

    /*
     * ノード（単体）の描画
     */
    private void drawNode(FrameLayout fl_map, NodeTable node, int lineDrawKind) {

        //ルートノード
        if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
            //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
            RootNodeView rootNodeView = findViewById(R.id.rnv_rootnode);

            //ビューにノード情報を設定
            rootNodeView.setNode(node);
            //中心座標を設定
            rootNodeView.calcCenterPos();
            //ランチャーを設定
            rootNodeView.setNodeOperationLauncher( mNodeOperationLauncher );

            //★初期化時に各設定項目を設定する
            //rootNodeView.setNodeName(node.getNodeName());
            //rootNodeView.setBackgroundColor( getResources().getColor( R.color.cafe_2 ) );

/*
            //マージン座標を取得
            int left = rootNodeView.getLeft();
            int top  = rootNodeView.getTop();
            //中心座標を保持
            rootNodeView.setCenterPosX(left + (rootNodeView.getWidth() / 2f));
            rootNodeView.setCenterPosY(top + (rootNodeView.getHeight() / 2f));
*/

            //NodeTable側でノードビューを保持
            node.setRootNodeView(rootNodeView);

            //Log.i("drawNodes", "root centerx=" + (left + (rootNodeView.getWidth() / 2f)) + " left=" + left);
            //Log.i("drawNodes", "root centery=" + (top + (rootNodeView.getHeight() / 2f)) + " top=" + top);

            return;
        }

        //ノード生成
        //NodeView nodeView = new NodeView(this, node, mNodeOperationLauncher);
        ChildNodeView nodeView;
        if (node.getKind() == NodeTable.NODE_KIND_NODE) {
            //ノード
            nodeView = new NodeView(this, node, mNodeOperationLauncher);
        }else{
            //ピクチャノード
            nodeView = new PictureNodeView(this, node, mNodeOperationLauncher);
        }

        //ノード名設定
        //nodeView.setNodeName(node.getNodeName());

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
        ViewTreeObserver observer = nodeView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new NodeGlobalLayoutListener(nodeView, lineDrawKind));

        //ノードビューを保持
        node.setChildNodeView(nodeView);
    }

    /*
     * ノード（単体）の再描画
     */
/*    private void redrawNode(FrameLayout fl_map, NodeTable node, int lineDrawKind) {



    }*/

    /*
     * 全ラインの描画
     */
    private void drawAllLines() {

        //ビュー
        FrameLayout fl_map = findViewById(R.id.fl_map);     //マップレイアウト（ノード追加先）

        //全ノード数ループ
        for (NodeTable node : mNodes) {

            //ラインの描画
            drawLine(fl_map, node);
        }
    }

    /*
     * ライン（単体）の描画
     */
    private void drawLine(FrameLayout fl_map, NodeTable node) {

        //親ノードPid
        int pidParentNode = node.getPidParentNode();
        if (pidParentNode == NodeTable.NO_PARENT) {
            //ルートノードはラインなし
            return;
        }

        //親ノード
        NodeTable parentNode = mNodes.getNode(pidParentNode);

        //親の中心座標を取得
        float parentCenterX = parentNode.getCenterPosX();
        float parentCenterY = parentNode.getCenterPosY();

        //自身の中心座標を取得
        ChildNodeView nodeView = node.getChildNodeView();

        //ラインを生成
        NodeView.LineView lineView = nodeView.createLine(parentCenterX, parentCenterY);

        //レイアウトに追加
        fl_map.addView(lineView);
    }

    /*
     * 画面遷移後の処理
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {

            //ノード生成からの戻り
            case REQ_NODE_CREATE:

                //ノード生成された場合
/*                if (resultCode == NodeEntryActivity.RES_CODE_NODE_POSITIVE) {
                    //生成されたノードを取得
                    NodeTable node = (NodeTable) intent.getSerializableExtra(NodeEntryActivity.KEY_CREATED_NODE);
                    //リストに追加
                    MapCommonData mapCommonData = (MapCommonData) getApplication();
                    mapCommonData.addNodes(node);

                    //ノードを描画
                    drawNode(findViewById(R.id.fl_map), node, NodeGlobalLayoutListener.LINE_SELF);
                }*/

                break;

            //ノード編集からの戻り
            case REQ_NODE_EDIT:

                //ノード生成された場合
/*                if (resultCode == NodeEntryActivity.RES_CODE_NODE_POSITIVE) {
                    //共通データから、編集ノードを取得
                    MapCommonData mapCommonData = (MapCommonData) getApplication();
                    NodeTable node = mapCommonData.getEditNode();

                    //ノード情報をビューに反映
                    NodeView nodeView = node.getNodeView();
                    nodeView.reflectNodeInformation();
                }*/

                break;

            default:
                break;
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
        MapCommonData mapCommonData = (MapCommonData)getApplication();
        NodeArrayList<NodeTable> nodeQue = mapCommonData.getMovedNodesQue();

        Log.i("onStop", "nodeQue.size()=" + nodeQue.size());

        //座標移動したノードがあれば
        if( nodeQue.size() > 0 ){
            AsyncUpdateNodePosition db = new AsyncUpdateNodePosition(this, nodeQue, new AsyncUpdateNodePosition.OnFinishListener() {
                //DB処理完了
                @Override
                public void onFinish() {
                    //更新完了後は、キュークリア
                    mapCommonData.clearMovedNodesQue();
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
            case R.id.action_palette:
                return true;

            case R.id.action_search:
                return true;

            case R.id.action_folder_tree:

                Log.i("MenuItem", "action_folder_tree");

                DrawerLayout drawer = findViewById(R.id.dl_map);
                drawer.openDrawer(GravityCompat.END);


                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    /*
     * タッチイベントの実装
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

                Log.i("MotionEvent", "preX=" + mPreTouchPosX + " preY=" + mPreTouchPosY);
                Log.i("MotionEvent", "getX=" + motionEvent.getX() + " getY=" + motionEvent.getY());
                Log.i("MotionEvent", "diffX=" + distanceX + " diffY=" + distanceY);

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
     * OnGlobalLayoutListener（ノード用）
     */
    private class NodeGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

        /* ライン描画指定 */
        public static final int LINE_NONE = 0;
        public static final int LINE_ALL  = 1;
        public static final int LINE_SELF = 2;

        /* フィールド変数 */
        private final ChildNodeView mv_node;
        private final int mLineDrawKind;

        public NodeGlobalLayoutListener( ChildNodeView v_node, int lineDrawKind ){
            mv_node = v_node;
            mLineDrawKind = lineDrawKind;
        }

        @Override
        public void onGlobalLayout() {
            Log.i("NodeGlobal", "レイアウト確定ノード=" + mv_node.getNode().getNodeName());

            //レイアウト確定後は、不要なので本リスナー削除
            mv_node.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            //レイアウトが確定したため、このタイミングで中心座標を設定
            mv_node.calcCenterPos();
            //mv_node.setCenterPosX(mv_node.getLeft() + (mv_node.getWidth() / 2f));
            //mv_node.setCenterPosY(mv_node.getTop()  + (mv_node.getHeight() / 2f));
            //Log.i("NodeGlobal", "getLeft=" + mv_node.getLeft() + " getTop()=" + mv_node.getTop());

            if(mLineDrawKind == LINE_ALL){
                //全ラインを描画
                drawAllLines();
            } else if( mLineDrawKind == LINE_SELF ){
                //本ノードのラインを描画
                drawLine( findViewById(R.id.fl_map), mv_node.getNode() );
            }
        }
    }

    /*
     * ピンチ（拡大・縮小）操作リスナー
     */
    private class PinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

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
            rnv_rootnode = findViewById(R.id.rnv_rootnode);
            mv_base  = findViewById(R.id.v_base);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            Log.i("onScale", "onScaleBegin");

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

            Log.i("onScaleBegin", "mCenterNode location=" + locationRoot[0] + " location=" + locationRoot[1]);
            Log.i("onScaleBegin", "v_tmp location=" + locationBase[0] + " location=" + locationBase[1]);

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

            Log.i("onScale", "getScaleFactor=" + scaleFactor);

            //ピンチ操作開始時の比率に、ピンチ操作中の比率を掛ける
            mfl_map.setScaleX(mPinchScaleX * scaleFactor);
            mfl_map.setScaleY(mPinchScaleY * scaleFactor);

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

            Log.i("onScaleEnd", "pinchDistanceRatioX=" + pinchDistanceRatioX + " pinchDistanceRatioY=" + pinchDistanceRatioY);

            //↓改修が必要
            //mDragViewListener.setTestScaleX( testScaleX );
            //mDragViewListener.setTestScaleY( testScaleY );

            //ピンチ開始時との位置のズレを保持
            //※連続で操作される可能性があるため、累計させる
            mPinchShiftX += (startRootPosX - locationRoot[0]);
            mPinchShiftY += (startRootPosY - locationRoot[1]);

            Log.i("Pinch", "mPinchShiftX=" + mPinchShiftX + " mPinchShiftY=" + mPinchShiftY);

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

            Log.i("onFling", "velocityX=" + velocityX + " velocityY=" + velocityY);


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
                    -2000,
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
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            Log.i("DrawerListener", "onDrawerClosed");

            mDrawerIsOpen = false;
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            Log.i("DrawerListener", "onDrawerStateChanged newState=" + newState);
        }

        @Override
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            Log.i("DrawerListener", "onDrawerSlide");

        }

    }

    /*
     * 画面遷移からの戻りのコールバック通知
     *   ・ノード新規作成
     *   ・ノード新規作成（ピクチャ）
     *   ・ノード編集
     */
    private class NodeOperationResultCallback implements ActivityResultCallback<ActivityResult> {

        /*
         * 画面遷移先からの戻り処理
         */
        @Override
        public void onActivityResult(ActivityResult result) {

            //インテント
            Intent intent = result.getData();
            //リザルトコード
            int resultCode = result.getResultCode();

            //ノード新規作成完了
            if( resultCode == NodeEntryActivity.RESULT_CREATED) {

                //生成されたノードを取得
                NodeTable node = (NodeTable) intent.getSerializableExtra(ResourceManager.KEY_CREATED_NODE);
                //リストに追加
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.addNodes(node);

                //ノードを描画
                drawNode(findViewById(R.id.fl_map), node, NodeGlobalLayoutListener.LINE_SELF);

            //ノード編集完了
            } else if( resultCode == NodeEntryActivity.RESULT_EDITED) {

                //共通データから、編集ノードを取得
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                NodeTable node = mapCommonData.getEditNode();

                //ノード情報をビューに反映
                ChildNodeView nodeView = node.getChildNodeView();
                nodeView.reflectNodeInformation();

            //ピクチャノード生成完了
            } else if( resultCode == PictureNodeSelectActivity.RESULT_PICTURE_NODE) {

                //新規ピクチャノードを取得
                NodeTable pictureNode = (NodeTable)intent.getSerializableExtra(ResourceManager.KEY_CREATED_NODE);
                //URI識別子を取得
                //String uriIdentify = intent.getStringExtra( ResourceManager.KEY_URI );
                //URIを生成
                //Uri uri = Uri.parse( ResourceManager.URI_PATH + uriIdentify );

                //ノードを描画
                //Log.i("Callback", "uri=" + uri);
                drawNode(findViewById(R.id.fl_map), pictureNode, NodeGlobalLayoutListener.LINE_SELF);


            } else {


            }
        }
    }



}
