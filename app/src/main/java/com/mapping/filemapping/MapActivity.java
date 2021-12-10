package com.mapping.filemapping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Scroller;

import java.util.Objects;

public class MapActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-リクエストコード */
    //ノード生成
    public static final int REQ_NODE_CREATE = 100;

    /* 画面遷移-キー */
    public static String INTENT_MAP_PID  = "MapPid";
    public static String INTENT_NODE_PID = "NodePid";



    //マップ情報管理
    private MapInfoManager mMapInfoManager;

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

    /* データ */
    public static NodeArrayList<NodeTable> mNodes;     //ノードリスト

    /* 制御 */
    //ノード生成ができる状態か
    private boolean mEnableDrawNode = false;


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

        //マップ管理マネージャを取得
        mMapInfoManager = MapInfoManager.getInstance(true);
        pinchDistanceRatioX = mMapInfoManager.getPinchDistanceRatioX();
        pinchDistanceRatioY = mMapInfoManager.getPinchDistanceRatioY();

        //リスナー生成
        mPinchGestureDetector = new ScaleGestureDetector(this, new PinchListener());
        mScrollGestureDetector = new GestureDetector(this, new ScrollListener());

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
        int mapPid = intent.getIntExtra(ResourceManager.INTENT_ID_MAPLIST_TO_MAP, 0);
        AsyncReadNodeOperaion db = new AsyncReadNodeOperaion(this, mapPid, new AsyncReadNodeOperaion.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(NodeArrayList<NodeTable> nodeList) {

                mNodes = nodeList;

                if( mEnableDrawNode ){
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

                        if( mEnableDrawNode ){
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
        drawNode();
    }


    /*
     * ノード生成
     */
    private void drawNode(){

        //ビュー
        FrameLayout fl_map  = findViewById(R.id.fl_map);     //マップレイアウト（ノード追加先）

        //全ノード数ループ
        int nodeNum = mNodes.size();
        for ( int i = 0; i < nodeNum; i++ ) {

            NodeTable node = mNodes.get(i);

            //ルートノード
            if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
                //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
                RootNodeView rootNodeView = findViewById(R.id.rnv_rootnode);
                rootNodeView.setNodeName( node.getNodeName() );

                //マージン座標を取得
                int left = rootNodeView.getLeft();
                int top  = rootNodeView.getTop();

                //中心座標を保持
                rootNodeView.setCenterPosX( left + (rootNodeView.getWidth()  / 2f) );
                rootNodeView.setCenterPosY( top  + (rootNodeView.getHeight() / 2f) );

                //NodeTable側でノードビューを保持
                node.setRootNodeView( rootNodeView );

                //ビュー側でもノード情報を保持
                rootNodeView.setNode( node );

                Log.i("drawNodes", "root centerx=" + ( left + (rootNodeView.getWidth()  / 2f) ));
                Log.i("drawNodes", "root centery=" + ( top  + (rootNodeView.getHeight() / 2f) ));

                continue;
            }

            //ノード生成
            NodeView nodeView = new NodeView(this, node);

            //ノード名設定
            nodeView.setNodeName( node.getNodeName() );

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

            //無名クラス内参照用
            int finalI = i;

            ViewTreeObserver observer = nodeView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            Log.i("createNode", "レイアウトが確定したノード=" + node.getNodeName() );

                            //レイアウト確定後は、不要なので本リスナー削除
                            nodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            //中心座標を保持
                            nodeView.setCenterPosX( left + (nodeView.getWidth()  / 2f) );
                            nodeView.setCenterPosY( top  + (nodeView.getHeight() / 2f) );

                            //最後に追加したビューの場合
                            if( finalI == (nodeNum - 1) ){
                                //ラインを描画する
                                drawLine();
                            }
                        }
                    }
            );

            //ノードビューを保持
            node.setNodeView( nodeView );
        }
    }

    /*
     * ラインの描画
     */
    private void drawLine() {

        //ビュー
        FrameLayout fl_map = findViewById(R.id.fl_map);     //マップレイアウト（ノード追加先）

        //全ノード数ループ
        for (NodeTable node : mNodes) {

            //親ノードPid
            int pidParentNode = node.getPidParentNode();
            if( pidParentNode == NodeTable.NO_PARENT ){
                //ルートノードはラインなし
                continue;
            }

            //親ノード
            NodeTable parentNode = mNodes.getNode(pidParentNode);

            //親の中心座標を取得
            float parentCenterX = parentNode.getCenterPosX();
            float parentCenterY = parentNode.getCenterPosY();

            //自身の中心座標を取得
            NodeView nodeView = node.getNodeView();
            //float centerX = nodeView.getCenterPosX();
            //float centerY = nodeView.getCenterPosY();

            //ラインを生成
            NodeView.LineView lineView = nodeView.createLine( parentCenterX, parentCenterY );

            //LineViewOld lineView = new LineViewOld(this, parentCenterX, parentCenterY, centerX, centerY);

            //ノードに保持させる
            //nodeView.setLineView( lineView );

            //レイアウトに追加
            fl_map.addView(lineView);
        }
    }

    /*
     * 画面遷移後の処理
     */
    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch( requestCode ){

            //ノード生成からの戻り
            case REQ_NODE_CREATE:

                //ノード生成された場合
                if( resultCode == NodeInformationActivity.RES_NODE_CREATE ){
                    //生成されたノードをレイアウトに追加
                    NodeTable node = (NodeTable)intent.getSerializableExtra(NodeInformationActivity.INTENT_CREATED_NODE);
                }

                break;

            default:
                break;
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
            mMapInfoManager.setPinchDistanceRatio(pinchDistanceRatioX, pinchDistanceRatioY);

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
            final int SCALE = 2;
            final int MOVE_DURATION = 5000;

            // アニメーションを開始
            mFlingScroller.fling(
                    (int) nowx,                          //scroll の開始位置 (X)
                    (int) nowy,                          //scroll の開始位置 (Y)
                    (int) velocityX / SCALE,     //初速
                    (int) velocityY / SCALE,     //初速
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

}