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
import android.widget.TextView;

import com.mapping.NodeTouchListener;

import java.util.Objects;

public class MapActivity extends AppCompatActivity {

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
    private NodeArrayList<NodeTable> mNodeList;     //ノードリスト

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
        toolbar.setTitle("ツールバー");
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
        int mapPid = intent.getIntExtra("MapID", 0);
        AsyncReadNodeOperaion db = new AsyncReadNodeOperaion(this, mapPid, new AsyncReadNodeOperaion.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(NodeArrayList<NodeTable> nodeList) {

                mNodeList = nodeList;

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
        TextView tv_root = findViewById(R.id.tv_root);

        ViewTreeObserver observer = tv_root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        tv_root.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        if( mEnableDrawNode ){
                            //ノード生成可能なら、マップ上にノードを生成
                            createMap();
                        }

                        //レイアウト側は確定したため、フラグ更新
                        mEnableDrawNode = true;
                    }
                }
        );



        if (true) {
            return;
        }

/*        //マップ
        FrameLayout fl_map = findViewById(R.id.fl_map);

        //レイアウト確定待ち
        observer = tv_root.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onGlobalLayout() {
                int width = tv_root.getWidth();

                int x = tv_root.getLeft() + (tv_root.getWidth() / 2);
                int y = tv_root.getTop() + (tv_root.getHeight() / 2);

                Log.i("attach2", "センターの中心座標(親レイアウトのマージン) x=" + x + " y=" + y);
                Log.i("attach2", "センターの座標(親レイアウトのマージン) getLeft=" + tv_root.getLeft() + " getTop=" + tv_root.getTop());


                //ビューの生成----------------------------------------
                TextView kyotoNode = new TextView(tv_root.getContext());
                kyotoNode.setText("京都");

                fl_map.addView(kyotoNode, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                int tox = x + width * 2;
                int toy = y + width * 2;

                //レイアウトパラメータ
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) kyotoNode.getLayoutParams();
                mlp.setMargins(tox, toy, mlp.rightMargin, mlp.bottomMargin);

                Log.i("mlp", "rightMargin=" + mlp.rightMargin + " bottomMargin=" + mlp.bottomMargin);

                //線の描画----------------------------------------
                LineView pathView = new LineView(tv_root.getContext(), x, y, x + width * 2, y + width * 2);
                fl_map.addView(pathView);

                //タッチリスナー
                kyotoNode.setOnTouchListener(new NodeTouchListener(kyotoNode, pathView));

                //ビューの生成----------------------------------------
                TextView hNode = new TextView(tv_root.getContext());
                hNode.setText("北海道");

                fl_map.addView(hNode, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                ViewTreeObserver observer = hNode.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        int[] location = new int[2];
                        hNode.getLocationInWindow(location);

                        Log.i("hNode", "getLocationInWindow x=" + location[0]);
                        Log.i("hNode", "getLocationInWindow Y=" + location[1]);
                        Log.i("hNode", "getWidth=" + hNode.getWidth());
                    }
                });

                int htox = x + width * 4;
                int htoy = y - width * 2;

                //レイアウトパラメータ
                mlp = (ViewGroup.MarginLayoutParams) hNode.getLayoutParams();
                mlp.setMargins(htox, htoy, mlp.rightMargin, mlp.bottomMargin);


                //ビューの生成----------------------------------------
                TextView moveNode = new TextView(tv_root.getContext());
                moveNode.setText("北海道に移動");

                fl_map.addView(moveNode, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                int movex = x;
                int movey = y + width * 3;

                //レイアウトパラメータ
                mlp = (ViewGroup.MarginLayoutParams) moveNode.getLayoutParams();
                mlp.setMargins(movex, movey, mlp.rightMargin, mlp.bottomMargin);

                moveNode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //movep

                        //スクロール開始位置
                        float scrollStartX = fl_map.getTranslationX() + mPinchShiftX;
                        float scrollStartY = fl_map.getTranslationY() + mPinchShiftY;

                        Log.i("move", "スクロール開始位置 x=" + scrollStartX + " y=" + scrollStartY);

                        //ピンチ操作の差分反映は1度のみ。移動後はクリア
                        mPinchShiftX = 0;
                        mPinchShiftY = 0;

                        //現在のrootのTranslation座標（ピンチの調整を無効化）
                        float rootPosX = scrollStartX / pinchDistanceRatioX;
                        float rootPosY = scrollStartY / pinchDistanceRatioY;

                        Log.i("move", "現在のrootのTranslation座標 pinchDistanceRatioX=" + pinchDistanceRatioX + " x=" + rootPosX + " y=" + rootPosY);
                        Log.i("move", "現在のcenterのマージン座標 left=" + tv_root.getLeft() + " top=" + tv_root.getTop());

                        //現在のTranslation座標に対応する親レイアウトマージン=中心座標の親レイアウトマージン値（ピンチ考慮なし）
                        float rootMarginX = tv_root.getLeft() - rootPosX;
                        float rootMarginY = tv_root.getTop() - rootPosY;

                        //移動先の親レイアウトマージン
                        int toLeft = hNode.getLeft();
                        int toTop = hNode.getTop();

                        Log.i("move", "Left(移動先)=" + toLeft + " Left(センター)=" + tv_root.getLeft());
                        Log.i("move", "rootMarginX=" + rootMarginX + " rootMarginY=" + rootMarginY);

                        //移動量（ピンチ考慮なし）
                        //int MarginDiffX = toLeft - (int)rootMarginX;
                        //int MarginDiffY = toTop  - (int)rootMarginY;
//
                        //Log.i("move", "移動量 MarginDiffX=" + MarginDiffX + " MarginDiffY=" + MarginDiffY);

                        //移動量：スケール比率を考慮
                        float MarginPinchDiffX = (int) (pinchDistanceRatioX * (toLeft - rootMarginX));
                        float MarginPinchDiffY = (int) (pinchDistanceRatioY * (toTop - rootMarginY));

                        Log.i("move", "移動量(スケール考慮 比率取得 float) MarginPinchDiffX=" + MarginPinchDiffX + " MarginPinchDiffY=" + MarginPinchDiffY);

                        //スクローラー
                        final int MOVE_DURATION = 500;

                        Scroller scroller = new Scroller(v.getContext(), new DecelerateInterpolator());

                        // アニメーションを開始
                        scroller.startScroll(
                                (int) scrollStartX,
                                (int) scrollStartY,
                                (int) -MarginPinchDiffX,
                                (int) -MarginPinchDiffY,
                                MOVE_DURATION       // スクロールにかかる時間 [milliseconds]
                        );


                        ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0, 1).setDuration(MOVE_DURATION);
                        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                Log.i("Scroller", "onAnimationUpdate");

                                if (!scroller.isFinished()) {
                                    scroller.computeScrollOffset();
                                    //setPieRotation(scroller.getCurrY());

                                    fl_map.setTranslationX(scroller.getCurrX());
                                    fl_map.setTranslationY(scroller.getCurrY());

                                } else {
                                    scrollAnimator.cancel();
                                    //onScrollFinished();

                                    //jikken
                                    //root.setScaleX(preScale);
                                    //root.setScaleY(preScale);
                                    //
                                }
                            }
                        });
                        scrollAnimator.start();


                    }
                });


                //リスナー削除
                tv_root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });*/

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
        TextView    tv_root = findViewById(R.id.tv_root);    //ルートノード

        //インフレータ
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //全ノード数ループ
        int nodeNum = mNodeList.size();
        for ( int i = 0; i < nodeNum; i++ ) {

            NodeTable node = mNodeList.get(i);

            //ルートノード
            if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
                //元々レイアウト上にあるルートノード名を変更し、中心座標を保持するだけ
                tv_root.setText(node.getNodeName());

                //マージン座標を取得
                int left = tv_root.getLeft();
                int top  = tv_root.getTop();

                //中心座標を保持
                node.setCenterPosX( left + (tv_root.getWidth()  / 2f) );
                node.setCenterPosY( top  + (tv_root.getHeight() / 2f) );

                Log.i("drawNodes", "root centerx=" + ( left + (tv_root.getWidth()  / 2f) ));
                Log.i("drawNodes", "root centery=" + ( top  + (tv_root.getHeight() / 2f) ));

                continue;
            }

            //ノードレイアウト
            View v_node = inflater.inflate(R.layout.node, null);

            //ノード名設定
            TextView tv_node = v_node.findViewById(R.id.tv_node);
            tv_node.setText(node.getNodeName());

            //ノードをマップに追加
            fl_map.addView(v_node, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            //位置設定
            //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
            int left = node.getPosX();
            int top  = node.getPosY();

            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v_node.getLayoutParams();
            mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

            Log.i("createNode", "getWidth=" + v_node.getWidth() + " getHeight=" + v_node.getHeight());

            //無名クラス内参照用
            int finalI = i;

            ViewTreeObserver observer = v_node.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {

                            Log.i("createNode", "レイアウトが確定したノード=" + node.getNodeName() );

                            //レイアウト確定後は、不要なので本リスナー削除
                            v_node.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            //中心座標を保持
                            node.setCenterPosX( left + (v_node.getWidth()  / 2f) );
                            node.setCenterPosY( top  + (v_node.getHeight() / 2f) );

                            //最後に追加したビューの場合
                            if( finalI == (nodeNum - 1) ){
                                //ラインを描画する
                                drawLine();
                            }
                        }
                    }
            );

            //ノードタッチリスナー設定
            v_node.setOnTouchListener(new NodeTouchListener(v_node, node));
        }
    }

    /*
     * ラインの描画
     */
    private void drawLine() {

        //ビュー
        FrameLayout fl_map = findViewById(R.id.fl_map);     //マップレイアウト（ノード追加先）

        //全ノード数ループ
        for (NodeTable node : mNodeList) {

            //親ノードPid
            int pidParentNode    = node.getPidParentNode();
            if( pidParentNode == NodeTable.NO_PARENT ){
                //ルートノードはラインなし
                continue;
            }

            //親ノード
            NodeTable parentNode = mNodeList.getParentNode(pidParentNode);

            //親の中心座標を取得
            float parentCenterX = parentNode.getCenterPosX();
            float parentCenterY = parentNode.getCenterPosY();

            //自身の中心座標を取得
            float centerX = node.getCenterPosX();
            float centerY = node.getCenterPosY();

            //ラインを生成
            LineView lineView = new LineView(this, parentCenterX, parentCenterY, centerX, centerY);

            //ノードに保持させる
            node.setLineView( lineView );

            //レイアウトに追加
            fl_map.addView(lineView);
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
        View mtv_root;
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
            mfl_map = findViewById(R.id.fl_map);
            mtv_root = findViewById(R.id.tv_root);
            mv_base = findViewById(R.id.v_base);
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
            mtv_root.getLocationInWindow(locationRoot);
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
            mtv_root.getLocationInWindow(locationRoot);
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