package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class NodeView extends RootNodeView /*implements View.OnTouchListener*/ {

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;

    //前回のタッチ位置
    private int mPreTouchPosX;
    private int mPreTouchPosY;

    //自身のサイズ
    private int mWidth;
    private int mHeight;

    //親ノードとの接続線
    private LineView mLineView;

    //子ノードリスト
    private NodeArrayList<NodeTable> mChildNodes;

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, NodeTable node) {
        super(context, R.layout.node);

        Log.i("NodeView", "3");

        //ノード情報を保持
        mNode = node;

        init();
    }

/*    public NodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.i("NodeView", "2");

        init();
    }

    public NodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.i("NodeView", "3");

        init();
    }*/

    /*
     * 初期化処理
     */
    private void init() {

        Log.i("NodeView", "init");

        //タッチリスナー
        setOnTouchListener(new NodeTouchListener());
    }

    /*
     * 親ノード追随処理初期化
     *   ＠自分自身／親ノード からコールされる
     */
    public void initFollowParent() {

        //タッチ時点のサイズを保持
        mWidth  = getWidth();
        mHeight = getHeight();

        //子ノード検索
        searchChildNodes();
    }

    /*
     * 子ノード検索
     *   自ノードを親とするノードを検索する
     */
    public void searchChildNodes() {

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //子ノードを検索
        mChildNodes = nodes.getChildNodes(mNode.getPid());

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            NodeView v_node = childNode.getNodeView();

            Log.i("test", "searchChildNodes 初期化対象の子ノード=" + v_node.getNode().getNodeName());

            //子ノード側も初期化
            v_node.initFollowParent();
        }
    }

    /*
     * 子ノードの移動
     */
    public void moveChildNodes(float movex, float movey) {

        Log.i("test", "moveChildNodes 親=" + mNode.getNodeName() + " 子の数=" + mChildNodes.size());

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            NodeView v_node = childNode.getNodeView();

            //子ノードの子ノードを移動させる
            v_node.move(movex, movey, mCenterPosX, mCenterPosY, true);
        }
    }

    /*
     * 子ノードの位置(X座標)を反転
     *   ＠自分自身／親ノード からコールされる
     */
    public void reverceChildNodes(float touchNodePosX) {

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            NodeView v_node = childNode.getNodeView();

            Log.i("test", "反転時の自ノード情報 自分=" + mNode.getNodeName() + " 自分のX位置=" + mCenterPosX);

            //移動先の位置を反映
            v_node.place((int) touchNodePosX);

            //この子ノードの子ノードも対象
            v_node.reverceChildNodes(touchNodePosX);
        }
    }


    /*
     * ノードの移動
     * 　指定された移動量だけ移動させる
     *   ＠自分自身／親ノード からコールされる
     */
    public void move(float moveX, float moveY, float parentPosX, float parentPosY, boolean isFollowParent) {

        //今回イベントでのView移動先の位置
        //※移動量からピンチ操作率は取り除く
        int left = getLeft() + (int) moveX;
        int top  = getTop()  + (int) moveY;

        //レイアウトに反映
        layout(left, top, left + mWidth, top + mHeight);

        //ライン終端位置（自ノードの中心位置）を計算
        mCenterPosX = left + (mWidth / 2f);
        mCenterPosY = top + (mHeight / 2f);

        Log.i("move", "move Node=" + mNode.getNodeName() + " posx=" + mCenterPosX + " posy=" + mCenterPosY);

        //親の移動の追従か
        if (isFollowParent) {
            //ライン再描画（始端位置も更新）
            mLineView.reDraw(parentPosX, parentPosY);
        } else {
            //ライン再描画（終端位置のみ更新）
            mLineView.reDraw();
        }

        //子ノード移動
        moveChildNodes(moveX, moveY);
    }

    /*
     * ノードの配置
     *    指定されたX座標にノードを配置する
     */
    public void place(int touchNodePosX) {

        //ノードの横幅半分
        float halfWidth = mWidth / 2f;

        Log.i("place", "反転処理前 反転ノード=" + mNode.getNodeName() + " 自分のX位置=" + mCenterPosX);

        //反転後の中心位置＝「タッチノード位置」＋（「タッチノード位置」ー「自ノード位置」）
        mCenterPosX = touchNodePosX + (touchNodePosX - (int) mCenterPosX);
        //反転後の中心位置にノードを置くためのレフトマージン
        int revercePosX = (int) (mCenterPosX - halfWidth);

        //トップマージン
        int top = getTop();

        //位置変更
        layout(revercePosX, top, revercePosX + mWidth, top + mHeight);

        Log.i("place", "反転処理後 自分のX位置=" + mCenterPosX);

        //ライン再描画（終端位置のみ更新）
        mLineView.reDraw();
    }

    /*
     * レイアウトマージンの設定
     *    レイアウト上のマージンを設定する。
     * 　　※layout()はあくまで見えている位置を変えているだけ。
     *      本処理を行わないと、ダブルタップ発生時等で全てのノードが初期位置に戻る
     */
    public void setLayoutMargin() {

        //現在の表示上位置にマージンを設定
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)getLayoutParams();
        mlp.setMargins(getLeft(), getTop(), 0, 0);

        //子ノードも同様
        setLayoutMarginChildNodes();
    }

    /*
     * 子ノードの移動
     */
    public void setLayoutMarginChildNodes() {

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            NodeView v_node = childNode.getNodeView();

            //子ノードの子ノードを移動させる
            v_node.setLayoutMargin();
        }
    }

    /*
     * 親ノードのX座標を取得
     */
    public float getParentPositionX() {

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //親ノード
        int parentPid        = mNode.getPidParentNode();
        NodeTable parentNode = nodes.getNode(parentPid);

        return parentNode.getCenterPosX();
    }


    /*
     * ノードタッチリスナー
     */
    //private class NodeTouchListener implements View.OnTouchListener {
    private class NodeTouchListener extends RootNodeTouchListener {

        //親ノードに対する自ノードのX座標における相対位置（親ノードより正側か負側か）
        private int parentRelativePosition;

        /*
         * コンストラクタ
         */
        public NodeTouchListener() {
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ルートノード側の共通処理をコール
            boolean ret = super.onTouch( view, event );

            Log.i("NodeTouchListener", "super.onTouch=" + ret);

            //共通処理を行った場合、終了
            if( ret ){
                return true;
            }

            //タッチしている位置取得（スクリーン座標）
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    //親ノード追随処理初期化
                    initFollowParent();

                    //タッチ開始時のピンチ操作比率を取得
                    MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();
                    pinchDistanceRatioX = mapCommonData.getPinchDistanceRatioX();
                    pinchDistanceRatioY = mapCommonData.getPinchDistanceRatioY();

                    //今回のタッチ位置を保持
                    mPreTouchPosX = x;
                    mPreTouchPosY = y;

                    //親ノードに対する自ノードの位置
                    parentRelativePosition = getParentRelativePosition(mCenterPosX);

                    break;

                case MotionEvent.ACTION_MOVE:

                    //ノードの移動量
                    float moveX = (x - mPreTouchPosX) / pinchDistanceRatioX;
                    float moveY = (y - mPreTouchPosY) / pinchDistanceRatioY;

                    //自身を移動
                    move(moveX, moveY, 0, 0, false);

                    //位置反転判定
                    int currentPos = getParentRelativePosition(mCenterPosX);
                    if (parentRelativePosition != currentPos) {

                        Log.i("ParentRelativePos", "switch");

                        //子ノードの位置を反転
                        //タッチノードの位置を基準として、反転位置を計算する
                        reverceChildNodes(mCenterPosX);

                        //位置更新
                        parentRelativePosition = currentPos;
                    }

                    //今回のタッチ位置を保持
                    mPreTouchPosX = x;
                    mPreTouchPosY = y;

                    //イベント処理完了
                    return true;

                case MotionEvent.ACTION_UP:

                    //移動後の位置をレイアウトに反映させる
                    setLayoutMargin();

                    break;
            }

            //イベント処理完了
            return false;
        }

        /*
         * 親ノードに対する自ノードの位置を取得
         */
        public int getParentRelativePosition(float selfX) {

            //親ノードの位置
            float parentX = getParentPositionX();

            Log.i("ParentRelativePos", "selfX=" + selfX + " parentX=" + parentX);

            //親ノードに対する自ノードの位置定数
            final int POSITIVE = 1;     //正側（右）
            final int NEGATIVE = -1;    //負側（左）

            //自ノードが親ノードより大きければ正、そうでなければ負を返す
            return ((selfX > parentX) ? POSITIVE : NEGATIVE);
        }

    }


    /*
     * ラインビュー
     *   本ノードと親ノードとの接続ライン
     */
    public class LineView extends View {

        //ペイント情報
        Paint mPaint;

        //描画開始座標（親ノード位置）
        private float mStartPosX;
        private float mStartPosY;

        //描画終端座標（自ノード位置）
        //※NodeView側で定義しているため、ここでは持たない

        /*
         * コンストラクタ
         */
        public LineView(Context context, float startPosX, float startPosY) {
            super(context);

            //描画開始・終了座標
            mStartPosX = startPosX;
            mStartPosY = startPosY;

            //ペイント情報を生成
            mPaint = new Paint();
            mPaint.setStrokeWidth(2f);
            mPaint.setColor(Color.LTGRAY);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);

            //ノードに対して背面になるようにする（デフォルト値は0のため、0未満の値を指定）
            setTranslationZ(-1);
        }



/*    @Override
    public boolean onTouchEvent(MotionEvent event) {

        xZahyou = event.getX();
        yZahyou = event.getY();

        Log.i("onTouchEvent", "xZahyou=" + xZahyou + " yZahyou=" + yZahyou);

        this.invalidate();

        return true;
    }*/


        /*
         * ライン再描画(始端位置更新あり)
         */
        public void reDraw(float startPosX, float startPosY) {

            //開始位置を更新
            mStartPosX = startPosX;
            mStartPosY = startPosY;

            //再描画
            invalidate();
        }

        /*
         * ライン再描画
         */
        public void reDraw() {

            //再描画
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            //Path生成
            @SuppressLint("DrawAllocation") Path path = new Path();

            //スタート地点を移動
            path.moveTo(mStartPosX, mStartPosY);

            //制御点X, 制御点Y, 終点X, 終点Y
            path.quadTo(mStartPosX, (mStartPosY + mCenterPosY) / 2, mCenterPosX, mCenterPosY);

            Log.i("onDraw", "mParentPosX=" + mStartPosX + " mParentPosY=" + mStartPosY);

            //描画
            canvas.drawPath(path, mPaint);
        }

    }




    /*---- getter／setter ----*/

    public LineView getLineView() {
        return mLineView;
    }
    public void setLineView(LineView lineView) {
        this.mLineView = lineView;
    }
    public LineView createLine(float startPosX, float startPosY) {
        this.mLineView = new LineView( getContext(), startPosX, startPosY );

        return this.mLineView;
    }

}
