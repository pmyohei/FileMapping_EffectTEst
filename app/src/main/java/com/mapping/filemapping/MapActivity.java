package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

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

    //ノードフォーカス処理
    public static final int MOVE_CENTER = 0;
    public static final int MOVE_UPPER = 1;

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
    private int mTopScreanX = 0;
    private int mTopScreanY = 0;
    //ピンチ操作発生フラグ
    private boolean mIsPinch = false;
    //フリング用Scroller
    private Scroller mFlingScroller;
    //DrawerLayoutのオープン状態
    private boolean mDrawerIsOpen = false;

    //マップ内ノードリスト
    private NodeArrayList<NodeTable> mNodes;
    private PictureArrayList<PictureTable> mThumbnails;

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

        int screenWidth;
        int screenHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();

            screenWidth = windowMetrics.getBounds().width();
            screenHeight = windowMetrics.getBounds().height();
            Log.d("screenWidth=>>>", screenWidth + "");
            Log.d("screenHeight=>>", screenHeight + "");

        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
            screenHeight = displayMetrics.heightPixels;
        }

        //画面上部の中心位置
        mTopScreanX = screenWidth / 2;
        mTopScreanY = (int) (screenHeight * (1f - ResourceManager.NODE_DESIGN_DIALOG_RATIO)) / 2;

        Log.d("移動", "mTopScreanX=" + mTopScreanX);
        Log.d("移動", "mTopScreanY=" + mTopScreanY);

        //暫定--------------
        //背景色
        //暫定--------------

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
        Intent intent = getIntent();
        int mapPid = intent.getIntExtra(ResourceManager.KEY_MAPID, 0);
        AsyncReadNodes db = new AsyncReadNodes(this, mapPid, new AsyncReadNodes.OnReadListener() {

            //DB読み取り完了
            @Override
            public void onRead(NodeArrayList<NodeTable> nodeList, PictureArrayList<PictureTable> thumbnailList) {

                //マップ共通データ
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.setNodes(nodeList);

                //フィールド変数として保持
                mNodes = nodeList;
                mThumbnails = thumbnailList;

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

                        //ノードを円形にする
                        CardView cv_node = v_rootnode.findViewById(R.id.cv_node);
                        makeNodeCircle(cv_node);
                    }
                }
        );
    }


    /*
     * ノードの形を円形にする
     */
    //★ノードビューに作ったため廃止予定
    private void makeNodeCircle(CardView cv_node) {

        //CardView cv_node = findViewById(R.id.cv_node);
        Log.i("Card", "width=" + cv_node.getWidth() + " height=" + cv_node.getHeight());

        int max;
        int width = cv_node.getWidth();
        int height = cv_node.getHeight();
        if (width > height) {
            cv_node.setMinimumHeight(width);
            max = width;
        } else {
            cv_node.setMinimumWidth(height);
            max = height;
        }

        //int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setRadius(max / 2.0f);
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
    }

    /*
     * ノード（単体）の描画
     */
    private BaseNode drawNode(FrameLayout fl_map, NodeTable node, int lineDrawKind) {

        //ルートノード
        if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
            //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
            RootNodeView rootNodeView = findViewById(R.id.v_rootnode);

            //ビューにノード情報を設定
            rootNodeView.setNode(node);
            //中心座標を設定
            //rootNodeView.calcCenterPos();
            rootNodeView.addOnNodeGlobalLayoutListener();
            //ランチャーを設定
            rootNodeView.setNodeOperationLauncher(mNodeOperationLauncher);
            //ノード生成／編集クリックリスナー
            rootNodeView.setOnNodeDesignClickListener(new NodeDesignClickListener());

            //NodeTable側でノードビューを保持
            //node.setRootNodeView(rootNodeView);
            node.setNodeView(rootNodeView);

            //Log.i("drawNodes", "root centerx=" + (left + (rootNodeView.getWidth() / 2f)) + " left=" + left);
            //Log.i("drawNodes", "root centery=" + (top + (rootNodeView.getHeight() / 2f)) + " top=" + top);

            return rootNodeView;
        }

        //ノード生成
        //NodeView nodeView = new NodeView(this, node, mNodeOperationLauncher);
        ChildNode nodeView;
        if (node.getKind() == NodeTable.NODE_KIND_NODE) {
            //ノード
            nodeView = new NodeView(this, node, mNodeOperationLauncher);
        } else {
            //ピクチャノード
            //該当サムネイル取得
            //★nullの場合の考慮を行う
            PictureTable thumbnail = mThumbnails.getThumbnail(node.getPidParentNode(), node.getUriIdentify());

            nodeView = new PictureNodeView(this, node, thumbnail, mNodeOperationLauncher);
        }

        //ノードをマップに追加
        fl_map.addView(nodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //位置設定
        //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
        int left = node.getPosX();
        int top = node.getPosY();

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) nodeView.getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

        Log.i("createNode", "setMargins left=" + left + " top=" + top + " mlp.rightMargin=" + mlp.rightMargin + " mlp.bottomMargin=" + mlp.bottomMargin);
        Log.i("createNode", "getWidth=" + nodeView.getWidth() + " getHeight=" + nodeView.getHeight());

        //レイアウト確定後の処理を設定
        //ViewTreeObserver observer = nodeView.getViewTreeObserver();
        //observer.addOnGlobalLayoutListener(new NodeGlobalLayoutListener(nodeView, lineDrawKind));
        ((ChildNode) nodeView).addOnNodeGlobalLayoutListener();

        //ノード生成／編集クリックリスナー
        nodeView.setOnNodeDesignClickListener(new NodeDesignClickListener());

        //ノードビューを保持
        node.setNodeView(nodeView);

        return nodeView;
    }

    /*
     * ノード（単体）の再描画
     */
/*    private void redrawNode(FrameLayout fl_map, NodeTable node, int lineDrawKind) {



    }*/

    /*
     * 全ラインの描画
     */
    //★不要になる見込み
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
    //★不要になる見込み
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
        //ChildNode nodeView = node.getChildNodeView();
        ChildNode nodeView = (ChildNode) node.getNodeView();

        //ラインを生成
        NodeView.LineView lineView = nodeView.createLine(parentCenterX, parentCenterY);

        //レイアウトに追加
        fl_map.addView(lineView);
    }

    /*
     *　ノードにフォーカスをあてる（画面中心に指定座標をもってくる）
     */
    private void focusNodeToCenterScreen(float nodeLeft, float nodeTop, int POS_KIND) {

        int height = 0;
        
        if( POS_KIND == MOVE_UPPER ){
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
        float mapLeft = v_rootnode.getNodeLeft() - mapAbs1xX ;      //※ルートノードが操作対象の場合を考慮し、getNodeLeft()を使用
        float mapTop  = v_rootnode.getNodeTop()  - mapAbs1xY ;

        //移動量
        float moveDistanceX = (int)(pinchDistanceRatioX * (nodeLeft - mapLeft ));
        float moveDistanceY = (int)(pinchDistanceRatioY * (nodeTop  - mapTop + height ));

        //スクロール時間 [milliseconds]
        final int MOVE_DURATION = 600;

        Log.i("move中心", "移動先ノード nodeLeft=" + nodeLeft + " nodeTop=" + nodeTop);
        Log.i("move中心", "mapLeft=" + mapLeft + " mapTop=" + mapTop);
        Log.i("move中心", "スクロール開始位置 x=" + mapAbsX + " y=" + mapAbsY);

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

                Log.i("Scroller", "onAnimationUpdate");

                if ( !scroller.isFinished() ) {
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
     * onStop()
     */
    @Override
    protected void onStop() {
        //必須
        super.onStop();

        //位置情報を保存
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        NodeArrayList<NodeTable> nodeQue = mapCommonData.getMovedNodesQue();

        Log.i("onStop", "nodeQue.size()=" + nodeQue.size());

        //座標移動したノードがあれば
        if (nodeQue.size() > 0) {
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

                //ダイアログを開く
                DialogFragment dialog = new DesignDialog( (View)findViewById(R.id.fl_screenMap) );
                dialog.show( getSupportFragmentManager(), DesignDialog.TAG_MAP );

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
     * ノード生成／編集アイコンクリックリスナー
     */
    public class NodeDesignClickListener implements View.OnClickListener {

        //アイコン操作ノード
        private BaseNode mTouchNode;
        //新規生成 or 編集
        private boolean misNew;

        /*
         * コンストラクタ
         */
/*
        public NodeDesignClickListener( BaseNode node ){
            mTouchNode = node;
        }
*/

        public void setTouchNode( BaseNode node ){
            mTouchNode = node;
        }

        /*
         * アイコンクリック処理
         */
        public void onClickIcon(BaseNode node, View view, boolean isNew ){
            //操作対象ノード
            mTouchNode = node;
            //新規か編集か
            misNew = isNew;

            //本来のクリック処理
            this.onClick( view );
        }

        @Override
        public void onClick(View view) {

            //生成／編集対象のノード
            BaseNode v_node;

            //ノード位置
            float marginLeft;
            float marginTop;

            if( misNew ){
                //ノード新規生成

                //親ノード
                NodeTable parentNode = mTouchNode.getNode();

                //初期生成位置
                int posX = (int)parentNode.getCenterPosX() + ResourceManager.POS_NODE_INIT_OFFSET;
                int posY = (int)parentNode.getCenterPosY();

                //ノードを生成
                NodeTable newNode = new NodeTable();
                newNode.setNodeName("");
                newNode.setPidMap( parentNode.getPidMap() );
                newNode.setPidParentNode( parentNode.getPid() );
                newNode.setKind( NodeTable.NODE_KIND_NODE );
                newNode.setPos( posX, posY );
                newNode.setNodeColor( "#000000" );      //★初期値はデフォルト値がある形にしたい
                //ノードをマップに追加
                v_node = drawNode(findViewById(R.id.fl_map), newNode, NodeGlobalLayoutListener.LINE_SELF);

                marginLeft = posX;
                marginTop  = posY;

            } else {
                //ノード編集
                //タッチされたノードが編集対象
                v_node = mTouchNode;

                //ノード本体のマージンを取得
                //※このタイミングではツールアイコン込みのマージンが取得されるため、以下のメソッドを使用
                marginLeft = v_node.getNodeLeft();
                marginTop  = v_node.getNodeTop();
            }

            //ダイアログを開く
            DialogFragment dialog = new DesignDialog( v_node );
            dialog.show(((FragmentActivity) view.getContext()).getSupportFragmentManager(), DesignDialog.TAG_NODE);

            //画面上部中央にノードがくるようにする
            focusNodeToCenterScreen(marginLeft, marginTop, MOVE_UPPER);
        }
    }


    /*
     * OnGlobalLayoutListener（ノード用）
     */
    //★不要のため削除←レイアウト確定待ちはノード自身に行わせる
    private class NodeGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {

        /* ライン描画指定 */
        public static final int LINE_NONE = 0;
        public static final int LINE_ALL  = 1;
        public static final int LINE_SELF = 2;

        /* フィールド変数 */
        private final BaseNode mv_node;
        private final int mLineDrawKind;

        public NodeGlobalLayoutListener(BaseNode v_node, int lineDrawKind ){
            mv_node = v_node;
            mLineDrawKind = lineDrawKind;
        }

        @Override
        public void onGlobalLayout() {
            Log.i("NodeGlobal", "レイアウト確定ノード=" + mv_node.getNode().getNodeName());

            //レイアウト確定後は、不要なので本リスナー削除
            //mv_node.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            //ノードの形を設定
            CardView cv_node = mv_node.findViewById(R.id.cv_node);
            makeNodeCircle( cv_node );

            //レイアウトが確定したため、このタイミングで中心座標を設定
            mv_node.calcCenterPos();

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
            rnv_rootnode = findViewById(R.id.v_rootnode);
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
            final int MARGIN_SIZE = 100;

            for (NodeTable node : hierarchyNodes) {
                //1行分のレイアウトをビュー化
                ViewGroup item = (ViewGroup) inflater.inflate(R.layout.hierarchy_node_item, null);
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
            tv_node.setText( node.getNodeName() );
            //tv_node.setText("1234567890123456789012345678901234567890");

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
/*                //★微妙
                if( node.getKind() == NodeTable.NODE_KIND_ROOT ){
                    //node.getRootNodeView().reflectViewNodeInfo();
                    node.getNodeView().reflectViewNodeInfo();
                } else{
                    //node.getChildNodeView().reflectChildNodeInfo();
                    //((ChildNode)node.getNodeView()).reflectViewNodeInfo();
                    node.getNodeView().reflectViewNodeInfo();
                }*/
                //ChildNode nodeView = node.getChildNodeView();
                //nodeView.reflectChildNodeInfo();
                node.getNodeView().reflectViewNodeInfo();

            //ピクチャノード生成完了
            } else if( resultCode == PictureTrimmingActivity.RESULT_PICTURE_NODE) {

                //新規ピクチャノードを取得
                NodeTable pictureNode = (NodeTable)intent.getSerializableExtra(ResourceManager.KEY_CREATED_NODE);
                PictureTable picture  = (PictureTable)intent.getSerializableExtra(ResourceManager.KEY_THUMBNAIL);

                //リストに追加
                MapCommonData mapCommonData = (MapCommonData) getApplication();
                mapCommonData.addNodes(pictureNode);
                //リストに追加
                mThumbnails.add(picture);

                //ノードを描画
                drawNode(findViewById(R.id.fl_map), pictureNode, NodeGlobalLayoutListener.LINE_SELF);
            }

        }
    }


}
