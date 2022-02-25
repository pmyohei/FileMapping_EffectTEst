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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.io.Serializable;

public class ChildNode extends BaseNode {

    //反転
    private final int FLIP_X = 0;   //左右反転
    private final int FLIP_Y = 1;   //上下反転

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;

    //前回のタッチ位置
    private int mPreTouchPosX;
    private int mPreTouchPosY;

    //親ノードとの接続線
    private LineView mLineView;

    //子ノードリスト
    private NodeArrayList<NodeTable> mChildNodes;

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public ChildNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public ChildNode(Context context, NodeTable node, int layoutID) {
        super(context, node, layoutID);

        Log.i("ChildNode", "3");

        //タッチリスナー
        setOnTouchListener(new NodeTouchListener());

        //ツールアイコン設定
        //setChildToolIcon();
    }


    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    @Override
    public void reflectViewNodeInfo() {
        //BaseNode
        super.reflectViewNodeInfo();

        Log.i("ChildNode", "reflectViewNodeInfo 1");

        if (mLineView == null) {
            //子ノードとしての処理が未完了なら、ここで終了
            return;
        }

        Log.i("ChildNode", "reflectViewNodeInfo 2");

        //★設定を追加した際に反映

        //ライン情報


        //レイアウト確定待ち
        //※ノードサイズが変わる可能性があるため、サイズが確定したタイミングでラインを再描画
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        Log.i("ChildNode", "reflectViewNodeInfo onGlobalLayout");

                        //ライン終端位置（自ノードの中心位置)
                        mCenterPosX = getLeft() + (getWidth() / 2f);
                        mCenterPosY = getTop() + (getHeight() / 2f);

                        //ライン再描画（終端位置のみ更新）
                        mLineView.reDraw();

                        //子ノード(直下のみ)のラインも再描画
                        reDrawChildNodeLine();
                    }
                }
        );
    }

    /*
     * ノードに設定中のノードサイズに設定
     */
    @Override
    public void setSetScale() {
        super.setSetScale();

        //ライン再描画
        if( mLineView != null ){
            mLineView.reDraw();
        }
    }

    /*
     * ノードサイズを設定
     */
    @Override
    public void setScale( float ratio ) {
        super.setScale( ratio );

        //ライン再描画
        if( mLineView != null ){
            mLineView.reDraw();
        }
    }

    /*
     * ラインカラーの設定・取得
     */
    public void setLineColor(String color) {
        mLineView.setColor(color);

        //テーブル側も更新
        mNode.setLineColor( color );
    }
    public String getLineColor() {
        return mLineView.getColor();
    }

    /*
     * ラインサイズ（太さ）の設定・取得
     */
    public void setLineSize(float thick) {
        mLineView.setSize(thick);

        //テーブル側も更新
        mNode.setLineSize( thick );
    }
    public float getLineSize() {
        return mLineView.getSize();
    }

    /*
     * 親ノード追随処理初期化
     *   ＠自分自身／親ノード からコールされる
     */
    public void initFollowParent() {

        //子ノード検索
        searchChildNodes();

        //子ノード内のツールアイコンクローズ
        closeIconInChildNodes();
    }

    /*
     * 子ノード検索
     *   自ノードを親とするノードを検索する
     */
    public void searchChildNodes() {

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //子ノードを検索
        mChildNodes = nodes.getChildNodes(mNode.getPid());

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            ChildNode v_node = (ChildNode) childNode.getNodeView();

            Log.i("test", "searchChildNodes 初期化対象の子ノード=" + v_node.getNode().getNodeName());

            //子ノード側も初期化
            v_node.initFollowParent();
        }
    }

    /*
     * 子ノードのツールアイコンクローズ
     *   自ノードの子ノードでアイコンを開いているノードがいれば閉じる
     */
    public void closeIconInChildNodes() {

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノード
            ChildNode v_node = (ChildNode) childNode.getNodeView();
            if( v_node.hasIconView() ){
                //開いているノードがいれば閉じて処理を終了
                v_node.closeIconView();
                return;
            }

            //子ノード側も同様
            v_node.closeIconInChildNodes();
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
            ChildNode v_node = (ChildNode) childNode.getNodeView();

            //子ノードの子ノードを移動させる
            v_node.move(movex, movey, mCenterPosX, mCenterPosY, true);
        }
    }

    /*
     * 子ノードの位置を反転
     *   ＠自分自身／親ノード からコールされる
     */
    public void flipChildNodes(float touchNodePos, int flip) {

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {
            //子ノードのノードビュー
            ChildNode v_node = (ChildNode) childNode.getNodeView();

            Log.i("test", "反転時の自ノード情報 自分=" + mNode.getNodeName() + " 自分のX位置=" + mCenterPosX);

            //移動先の位置を反映
            if (flip == FLIP_X) {
                v_node.placeX((int) touchNodePos);
            } else {
                v_node.placeY((int) touchNodePos);
            }

            //この子ノードの子ノードも対象
            v_node.flipChildNodes(touchNodePos, flip);
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
        int top = getTop() + (int) moveY;

        //レイアウトに反映
        layout(left, top, left + getWidth(), top + getHeight());

        //ライン終端位置（自ノードの中心位置）を計算
        mCenterPosX = left + (getWidth() / 2f);
        mCenterPosY = top + (getHeight() / 2f);

        Log.i("move", "move Node=" + mNode.getNodeName() + " posx=" + mCenterPosX + " posy=" + mCenterPosY);

        //ツールアイコンの移動
        moveToolIcon(moveX, moveY);

        //ライン再描画
        mLineView.reDraw();
/*        //親の移動の追従か
        if (isFollowParent) {
            //ライン再描画（始端位置も更新）
            mLineView.reDraw(parentPosX, parentPosY);
        } else {
            //ライン再描画（終端位置のみ更新）
            mLineView.reDraw();
        }*/

        //子ノード移動
        moveChildNodes(moveX, moveY);
    }

    /*
     * ツールアイコンの移動
     */
    public void moveToolIcon(float moveX, float moveY) {

        ToolIconsView toolIcon = getIconView();
        if( toolIcon == null ){
            //表示していないなら、何もしない
            return;
        }

        //今回イベントでのView移動先の位置
        int left = toolIcon.getLeft() + (int) moveX;
        int top  = toolIcon.getTop()  + (int) moveY;

        //レイアウトに反映
        toolIcon.layout(left, top, left + toolIcon.getWidth(), top + toolIcon.getHeight());
    }

    /*
     * ノードの配置
     *    指定されたX座標に自身を配置する
     */
    public void placeX(int pos) {

        //ノードの横幅半分
        int width = getWidth();
        float halfWidth = width / 2f;

        Log.i("place", "反転処理前 反転ノード=" + mNode.getNodeName() + " 自分のX位置=" + mCenterPosX);

        //反転後の中心位置＝「タッチノード位置」＋（「タッチノード位置」ー「自ノード位置」）
        mCenterPosX = pos + (pos - (int) mCenterPosX);
        //反転後の中心位置にノードを置くためのレフトマージン
        int reverceLeft = (int) (mCenterPosX - halfWidth);

        //トップマージン
        int top = getTop();

        //位置変更
        layout(reverceLeft, top, reverceLeft + width, top + getHeight());

        Log.i("place", "反転処理後 自分のX位置=" + mCenterPosX);

        //ライン再描画（終端位置のみ更新）
        mLineView.reDraw();
    }

    /*
     * ノードの配置
     *    指定されたY座標に自身を配置する
     */
    public void placeY(int pos) {

        //ノードの横幅半分
        int height = getHeight();
        float halfHeight = height / 2f;

        //反転後の中心位置＝「タッチノード位置」＋（「タッチノード位置」ー「自ノード位置」）
        mCenterPosY = pos + (pos - (int) mCenterPosY);
        //反転後の中心位置にノードを置くためのトップマージン
        int reverceTop = (int) (mCenterPosY - halfHeight);

        //左マージン
        int left = getLeft();

        //位置変更
        layout(left, reverceTop, left + getWidth(), reverceTop + height);

        //ライン再描画（終端位置のみ更新）
        mLineView.reDraw();
    }

    /*
     * レイアウトマージンの設定
     *    レイアウト上のマージンを設定する。
     * 　　※layout()はあくまで見えている位置を変えているだけ。
     *      本処理を行わないと、ダブルタップ発生時等で全てのノードが初期位置に戻る
     */
    public void setNodeLayoutMargin() {

        //現在の表示上位置にマージンを設定
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        mlp.setMargins(getLeft(), getTop(), 0, 0);

        //Nodetable側の位置情報を更新
        mNode.setPosX(getLeft());
        mNode.setPosY(getTop());

        //位置が変更されたため、自身(のNodeTable)を位置変更キューに追加
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        mapCommonData.enqueUpdateNodeWithUnique(mNode);

        //子ノードも同様
        setLayoutMarginChildNodes();
    }

    /*
     * 子ノードのレイアウトマージンの設定
     */
    public void setLayoutMarginChildNodes() {

        //子ノード分ループ
        for (NodeTable childNode : mChildNodes) {

            //子ノードのノードビュー
            //ChildNode v_node = childNode.getChildNodeView();
            ChildNode v_node = (ChildNode) childNode.getNodeView();

            //子ノードの子ノードを移動させる
            v_node.setNodeLayoutMargin();
        }
    }

    /*
     * 親ノードのX座標を取得
     */
    public float getParentPositionX() {

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //親ノード
        int parentPid = mNode.getPidParentNode();
        NodeTable parentNode = nodes.getNode(parentPid);

        return parentNode.getCenterPosX();
    }

    /*
     * 親ノードのY座標を取得
     */
    public float getParentPositionY() {

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //親ノード
        int parentPid = mNode.getPidParentNode();
        NodeTable parentNode = nodes.getNode(parentPid);

        return parentNode.getCenterPosY();
    }

    /*
     * 自身以下のノードをレイアウトから削除
     * 　　※自身の配下ノードも全て削除する
     */
    public void removeLayoutUnderSelf() {

        //子ノードリスト更新
        searchChildNodes();

        //子ノードをレイアウトから削除
        for (NodeTable node : mChildNodes) {
            //((ChildNode)node.getChildNodeView()).removeLayoutUnderSelf();
            ((ChildNode) node.getNodeView()).removeLayoutUnderSelf();
        }

        //自ノードとラインをレイアウトから削除
        ((ViewGroup) getParent()).removeView(getLineView());
        ((ViewGroup) getParent()).removeView(this);
    }

    /*
     * 子ノード(直下)のラインを再描画する
     */
    private void reDrawChildNodeLine() {

        //子ノードリスト更新
        searchChildNodes();

        //子ノードのラインを再描画
        for (NodeTable node : mChildNodes) {
            //node.getChildNodeView().getLineView().reDraw(mCenterPosX, mCenterPosY);
            //((ChildNode) node.getNodeView()).getLineView().reDraw(mCenterPosX, mCenterPosY);
            ((ChildNode) node.getNodeView()).getLineView().reDraw();
        }
    }


    /*
     * レイアウト確定後処理の設定（子ノード用）
     */
    public void addOnNodeGlobalLayoutListener() {
        //ノード共通の確定処理
        super.addOnNodeGlobalLayoutListener();

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();
        //親ノード
        NodeTable parentNode = nodes.getNode(mNode.getPidParentNode());

        //レイアウト確定処理
        childNodeGlobalLayoutProcess(parentNode);

    }

    /*
     * レイアウト確定後処理の設定（子ノード用）
     */
    public void addOnNodeGlobalLayoutListener( NodeTable parentNode ) {
        //ノード共通の確定処理
        super.addOnNodeGlobalLayoutListener();

        //レイアウト確定処理
        childNodeGlobalLayoutProcess(parentNode);
    }


    /*
     * レイアウト確定後処理の設定（子ノード用）
     */
    public void childNodeGlobalLayoutProcess( NodeTable parentNode ) {

        //レイアウト確定待ち処理
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        if (mLineView == null) {

                            //親の中心座標を取得
                            //float parentCenterX = parentNode.getCenterPosX();
                            float parentCenterY = parentNode.getCenterPosY();
                            if (parentCenterY == INIT_CENTER_POS) {
                                //親ノードのレイアウトが未確定なら、次のコールバックを待つ
                                Log.i("OnGlobalLayoutListener", "親未確定");
                                return;
                            }

                            //ライン未生成なら、生成
                            LineView line = createLine(parentNode.getNodeView());

                            Log.i("OnGlobalLayoutListener", "通過チェック");

                            //マップ上にラインを追加
                            ViewGroup vg = (ViewGroup) getRootView();
                            FrameLayout fl_map = vg.findViewById(R.id.fl_map);
                            fl_map.addView(line);

                        } else {
                            //ライン生成済みなら、再描画
                            mLineView.reDraw();
                        }

                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );

    }


    /*
     * ノードタッチリスナー
     */
    //private class NodeTouchListener implements View.OnTouchListener {
    private class NodeTouchListener extends RootNodeTouchListener implements Serializable {

        //方向ビット
        final int BIT_X = 0x01;    //自ノードが親ノードより右にいる場合
        final int BIT_Y = 0x10;    //自ノードが親ノードより上にいる場合
        final int OFF  = 0x00;    //オフ

        //クリックリスナーに制御を渡すかどうかを判定するMove回数
        //この回数よりも多くMoveを検出したとき、本Touchリスナーで制御を終了する
        final int MOVE_DETECTION_COUNT = 5;

        //親ノードに対する自ノードのX座標における相対位置（親ノードより正側か負側か）
        private int mParentRelative;

        //Move発生フラグ
        private boolean isMove;
        //Move発生回数
        private int isMoveCount;


        /*
         * コンストラクタ
         */
        public NodeTouchListener() {
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ルートノード側の共通処理をコール
            //boolean ret = super.onTouch( view, event );

/*            Log.i("NodeTouchListener", "super.onTouch=" + ret + " event.getAction()=" + event.getAction());

            //共通処理を行った場合、終了
            if( ret ){
                Log.i("NodeTouchListener", "ダブルタッチしたため終了");
                return true;
            }*/

            //タッチしている位置取得（スクリーン座標）
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            //
            //boolean ret = false;

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
                    mParentRelative = getParentRelativePosition(mCenterPosX, mCenterPosY);

                    //Moveフラグ初期化
                    isMove = false;

                    //Move発生回数初期化
                    isMoveCount = 0;

                    break;

                case MotionEvent.ACTION_MOVE:

                    Log.i("ムーブチェック", "ACTION_MOVE");

                    //ノードの移動量
                    float moveX = (x - mPreTouchPosX) / pinchDistanceRatioX;
                    float moveY = (y - mPreTouchPosY) / pinchDistanceRatioY;

                    //自身を移動
                    move(moveX, moveY, 0, 0, false);

                    //位置反転処理
                    flipNode();

                    //今回のタッチ位置を保持
                    mPreTouchPosX = x;
                    mPreTouchPosY = y;

                    //MoveフラグON
                    isMove = true;

                    //Move発生回数加算
                    isMoveCount++;

                    break;

                case MotionEvent.ACTION_UP:

                    //移動が発生していれば
                    if( isMove ){
                        //移動後の位置をレイアウトに反映させる
                        setNodeLayoutMargin();
                    }

                    break;
            }

            //ある程度のノード移動があれば、イベント処理はここで完了(クリックリスナーへは渡さない)
            //単純にMoveが発生したか否かでクリック検知に渡すと、ほとんどMove検出されるため、回数を設ける
            return ( isMoveCount > MOVE_DETECTION_COUNT );
        }

        /*
         * 親ノードに対する自ノードの位置を取得
         */
        public int getParentRelativePosition(float selfX, float selfY) {

            //親ノードの位置
            float parentX = getParentPositionX();
            float parentY = getParentPositionY();

            //親ノードとの位置を返す
            //X軸：親より右なら1bit目をON
            //Y軸：親より上なら2bit目をON
            return ((selfX > parentX) ? BIT_X : OFF) | ((selfY > parentY) ? BIT_Y : OFF);
        }

        /*
         * 親ノードに対して自ノードが左右上下反転したかどうか
         * 　※左右か上下かの判定は、para2にて制御
         */
        public boolean whetherFlip(int currentPos, int bit) {

            //1ビット目（X軸の方向の値）の値を比較
            //不一致なら、反転あり
            return ( (mParentRelative & bit) != (currentPos & bit) );
        }

        /*
         * ノード反転処理
         */
        public void flipNode() {

            boolean isFlip = false;

            int currentPos = getParentRelativePosition(mCenterPosX, mCenterPosY);
            if ( whetherFlip( currentPos, BIT_X) ) {

                //子ノードの位置を左右反転
                //タッチノードの位置を基準として、反転位置を計算する
                flipChildNodes(mCenterPosX, FLIP_X);

                isFlip = true;
            }
            if ( whetherFlip( currentPos, BIT_Y) ) {

                //子ノードの位置を上下反転
                //タッチノードの位置を基準として、反転位置を計算する
                flipChildNodes(mCenterPosY, FLIP_Y);

                isFlip = true;
            }

            if( isFlip ){
                //位置更新
                mParentRelative = currentPos;
            }
        }



    }

    /*
     * ラインビュー
     *   本ノードと親ノードとの接続ライン
     */
    public class LineView extends View {

        //親ノードと自ノードを両端とする2等辺三角形の両端の角度
        private final int CONTROL_POINT_DEGREES = 45;
        //制御点の位置の割合（親ノード自ノードの線分と直角頂点の垂直線の中で、制御点の位置とする割合）
        //(0＜f＜1 の範囲で指定)
        private final float CONTROL_POINT_POS_RATIO = 0.5f;

        //ペイント情報
        private final Paint mPaint;
        //親ノード
        private BaseNode mParentNode;
        //描画開始座標（親ノード位置）
        private float mStartPosX;
        private float mStartPosY;
        //描画終了座標（自ノード位置）
        private float mSelfPosX;
        private float mSelfPosY;

        /*
         * コンストラクタ
         */
        public LineView(Context context, BaseNode parentNode) {
            super(context);

            //親ノード
            mParentNode = parentNode;

            //ライン情報
            float thick = mNode.getLineSize();
            String color = mNode.getLineColor();

            //ペイント情報を生成
            mPaint = new Paint();
            mPaint.setStrokeWidth(thick);
            mPaint.setColor(Color.parseColor(color));
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            //ライン端点の計算
            calcSelfEdgePos();
            //ノードに対して背面になるようにする（デフォルト値は0のため、0未満の値を指定）
            setTranslationZ(-1);
        }


        /*
         * 自身のライン端点を計算
         */
        public void calcSelfEdgePos() {

            //親ノード位置
            float parentPosX = mParentNode.getCenterPosX();
            float parentPosY = mParentNode.getCenterPosY();

            //ノード間の線分の傾き
            float a = (parentPosY - mCenterPosY) / (parentPosX - mCenterPosX);
            //ラジアン角度を取得
            double radian = Math.atan(a);

            final float CROSS_RADIUS_RATIO = 1.2f;

            //交点に相当する座標を計算（自ノード側）
            //ノードよりも少し大きい半径を取得
            float radius = (getScaleNodeBodyWidth() / 2f) * CROSS_RADIUS_RATIO;
            int x = (int) (Math.cos(radian) * radius);
            int y = (int) (Math.sin(radian) * radius);

            //親の位置に応じて、端点を計算
            mSelfPosX = mCenterPosX + ((parentPosX > mCenterPosX) ? (x) : (-x));
            mSelfPosY = mCenterPosY + ((parentPosX > mCenterPosX) ? (y) : (-y));

            //交点に相当する座標を計算（親ノード側）
            //ノードよりも少し大きい半径を取得
            radius = (mParentNode.getScaleNodeBodyWidth() / 2f) * CROSS_RADIUS_RATIO;
            x = (int) (Math.cos(radian) * radius);
            y = (int) (Math.sin(radian) * radius);

            //親の位置に応じて、端点を計算
            mStartPosX = parentPosX + ((parentPosX > mCenterPosX) ? (-x) : (x));
            mStartPosY = parentPosY + ((parentPosX > mCenterPosX) ? (-y) : (y));

            //Log.i("三角関数", "傾き=" + a);
            //Log.i("三角関数", "Math.cos(radian)=" + Math.cos(radian));
            //Log.i("三角関数", "Math.sin(radian)=" + Math.sin(radian));
            //Log.i("ラインチェック", "radian=" + radian);
            //Log.i("ラインチェック", "parentPosX=" + parentPosX);
            //Log.i("ラインチェック", "parentPosY=" + parentPosY);
            //Log.i("ラインチェック", "mCenterPosX=" + mCenterPosX);
            //Log.i("ラインチェック", "mCenterPosY=" + mCenterPosY);
            //Log.i("ラインチェック", "x=" + x);
            //Log.i("ラインチェック", "y=" + y);
            //Log.i("ラインチェック", "mSelfPosX=" + mSelfPosX);
            //Log.i("ラインチェック", "mSelfPosY=" + mSelfPosY);
            //Log.i("ラインチェック", "---------------");
        }

        /*
         * 自身と親ノードとの距離（単純な直線距離）を算出
         */
        public float calcSelfParentDistance() {
            //距離を返す
            return (float) Math.sqrt(((mSelfPosX - mStartPosX) * (mSelfPosX - mStartPosX))
                    + ((mSelfPosY - mStartPosY) * (mSelfPosY - mStartPosY)));
        }

        /*
         * ラインカラーの設定
         */
        public void setColor(String color) {
            //色設定
            mPaint.setColor(Color.parseColor(color));
            //再描画
            invalidate();
        }

        /*
         * ラインカラーの取得
         */
        public String getColor() {

            return ("#" + Integer.toHexString(mPaint.getColor()));
        }

        /*
         * ラインサイズ（太さ）の設定
         */
        public void setSize(float thick) {
            //太さ設定
            mPaint.setStrokeWidth(thick);
            //再描画
            invalidate();
        }

        /*
         * ラインサイズ（太さ）の取得
         */
        public float getSize() {
            return mPaint.getStrokeWidth();
        }

        /*
         * ライン再描画(指定された親ノードの位置からラインを描画)
         */
        public void reDrawParent(BaseNode parentNode) {

            //親ノードを変更
            mParentNode = parentNode;
            //ライン端点再計算
            calcSelfEdgePos();
            //再描画
            invalidate();
        }

        /*
         * ライン再描画
         */
        public void reDraw() {

            //ライン端点再計算
            calcSelfEdgePos();
            //再描画
            invalidate();
        }

        /*
         * 1次ベジェ曲線のPathを作成
         */
        public Path createFirstOrderBezierCurve() {

            //Path生成
            //★初期生成させる
            @SuppressLint("DrawAllocation") Path path = new Path();

            //スタート地点を移動
            path.moveTo(mStartPosX, mStartPosY);

            //親との距離を取得
            float distance = calcSelfParentDistance();

            //制御点のX座標を取得
            //※この時点では角度0の場合の座標
            double ControlPointRadians = Math.toRadians(CONTROL_POINT_DEGREES);
            float baseX = (float) Math.cos(ControlPointRadians) * distance;
            //float baseY = (float)Math.sin(ControlPointRadians) * distance;

            Log.i("ライン調査", "distance=" + distance);
            Log.i("ライン調査", "親 baseX=" + mStartPosX + "\ty=" + mStartPosY);
            Log.i("ライン調査", "自分 baseX=" + mSelfPosX + "\ty=" + mSelfPosY);

            //必要な回転角度を取得
            double radian = getRotaionRadian();

            //回転後の座標を取得
            float rotationX = baseX * (float) Math.cos(radian);// - 0 * (float) Math.sin(radian);
            float rotationY = baseX * (float) Math.sin(radian);// + 0 * (float) Math.cos(radian);

            //自分の座標を基準に直角点座標を算出
            rotationX += mSelfPosX;
            rotationY = mSelfPosY - rotationY;

            //親ノードとノードの中間点（直角点から垂線を引いた時の交点座標）
            float middleX = (mStartPosX + mSelfPosX) / 2;
            float middleY = (mStartPosY + mSelfPosY) / 2;

            Log.i("距離参考", "distance=" + distance);

            //制御点座標（垂線上の指定割合位置をその座標とする）
            float controlX = (middleX + rotationX) * CONTROL_POINT_POS_RATIO;
            float controlY = (middleY + rotationY) * CONTROL_POINT_POS_RATIO;

            //Log.i("制御点", "rotationX=" + rotationX + "\trotationY=" + rotationY);
            //Log.i("制御点", "middleX=" + middleX + "\tmiddleY=" + middleY);
            //Log.i("制御点", "controlX=" + controlX + "\tcontrolY=" + controlY);

            //path
            path.quadTo(controlX, controlY, mSelfPosX, mSelfPosY);
            return path;
        }

        /*
         * 回転角度を取得
         */
        public float getRotaionRadian() {

            //線分の傾きの角度を取得
            float parentPosX = mParentNode.getCenterPosX();
            float parentPosY = mParentNode.getCenterPosY();
            float slope = (parentPosY - mCenterPosY) / (parentPosX - mCenterPosX);

            //左上が原点（Y軸方向が反対）のため、－1する（「一般的な2次元座標と同じように考えるため）
            slope *= -1;

            //傾きの角度を保持
            //※傾き→ラジアン角度→角度
            double radian = Math.atan(slope);
            double degrees = Math.toDegrees(radian);
            //double slopeDegrees = degrees;

            Log.i("ライン調査", "傾き=" + slope);
            Log.i("ライン調査", "傾き（角度）=" + degrees);

            //回転角度
            if (mSelfPosX >= mStartPosX) {
                //親よりも右にいる場合
                degrees = 180 + degrees - CONTROL_POINT_DEGREES;
            } else {
                //親よりも左にいる場合
                degrees = CONTROL_POINT_DEGREES + degrees;
            }

            //回転角度をラジアン角度に変換
            radian = Math.toRadians(degrees);

            Log.i("ライン調査", "回転角度=" + degrees);

            return (float)radian;
        }

        /*
         * 2次ベジェ曲線の作成
         */
        public Path createSecondOrderBezierCurve() {

            //Path生成
            //★初期生成させる
            @SuppressLint("DrawAllocation") Path path = new Path();

            //スタート地点を移動
            path.moveTo(mStartPosX, mStartPosY);

            //制御点
            float controlX1;
            float controlX2;
            float controlY1;
            float controlY2;

/*            float distanceX = Math.abs( mStartPosX - mCenterPosX );
            float distanceY = Math.abs( mStartPosY - mCenterPosY );
            if( distanceX > distanceY ){
                //X方向の方が距離がある
                controlX = ( mStartPosX + mCenterPosX ) / 2f;
                controlY = mStartPosY + distanceX;
            } else {
                //Y方向の方が距離がある
                controlX = mStartPosX + distanceY;
                controlY = ( mStartPosY + mCenterPosY ) / 2f;
            }*/

            controlX1 = ( mStartPosX + mCenterPosX ) / 2f;
            //controlY1 = ( mStartPosY + mCenterPosY ) * 0.3f;
            //controlY2 = ( mStartPosY + mCenterPosY ) * 0.6f;

            //controlY1 = Math.min(mStartPosY, mCenterPosY) + (Math.abs( mStartPosY - mCenterPosY ) * 0.3f);
            //controlY2 = Math.min(mStartPosY, mCenterPosY) + (Math.abs( mStartPosY - mCenterPosY ) * 0.6f);
            controlY1 = Math.min(mStartPosY, mSelfPosY);
            controlY2 = Math.max(mStartPosY, mSelfPosY);

            //Log.i("制御点", "mStartPosX=" + mStartPosX);
            //Log.i("制御点", "mStartPosY=" + mStartPosY);
            //Log.i("制御点", "mCenterPosX=" + mCenterPosX);
            //Log.i("制御点", "mCenterPosY=" + mCenterPosY);
            //Log.i("制御点", "controlX1=" + controlX1);
            //Log.i("制御点", "controlY1=" + controlY1);
            //Log.i("制御点", "controlY2=" + controlY2);
            //Log.i("制御点", "-----------");

            //制御点X, 制御点Y, 終点X, 終点Y
            //path.quadTo( (mStartPosX + mCenterPosX) / 2, (mStartPosY + mCenterPosY) / 2, mCenterPosX, mCenterPosY);
            //path.quadTo(mStartPosX, (mStartPosY + mCenterPosY) / 2, mCenterPosX, mCenterPosY);
            //path.quadTo(mStartPosX, (mStartPosY + mSelfPosY) / 2, mSelfPosX, mSelfPosY);
            //path.quadTo(controlX, controlY, mSelfPosX, mSelfPosY);
            path.cubicTo(controlX1, controlY1, controlX1, controlY2, mSelfPosX, mSelfPosY);

            Log.i("onDraw", "mParentPosX=" + mStartPosX + " mParentPosY=" + mStartPosY);

            return path;
        }


        @Override
        protected void onDraw(Canvas canvas) {

            super.onDraw(canvas);

            //2次ベジェ曲線のパスを生成
            //Path path = createSecondOrderBezierCurve();

            //1次ベジェ曲線のパスを生成
            Path path = createFirstOrderBezierCurve();

            //描画
            canvas.drawPath(path, mPaint);
        }

    }

    /*---- getter／setter ----*/

    public LineView getLineView() {
        return mLineView;
    }

    public LineView createLine(BaseNode parentNode) {
        this.mLineView = new LineView( getContext(), parentNode );
        return this.mLineView;
    }

}
