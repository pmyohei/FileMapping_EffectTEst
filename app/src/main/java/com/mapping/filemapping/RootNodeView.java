package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class RootNodeView extends FrameLayout implements View.OnClickListener {


    //ダブルタップ検知用
    public GestureDetector mGestureDetector;

    //データ
    public float mCenterPosX;        //ノード中心座標X
    public float mCenterPosY;        //ノード中心座標Y

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public RootNodeView(Context context) {
        super(context);

        Log.i("NodeView_new", "1");

        init();
    }

    public RootNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.i("NodeView_new", "2");

        init();
    }

    public RootNodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.i("NodeView_new", "3");

        init();
    }

    /*
     * 初期化処理
     */
    private void init() {

        Log.i("RootNodeView", "init");

        //ダブルタップリスナーを実装したGestureDetector
        mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from( getContext() );
        inflater.inflate(R.layout.node, this, true);
    }

    /*
     * ノード名の設定
     */
    public void setNodeName( String name ) {
        ((TextView)findViewById(R.id.tv_node)).setText(name);
    }

    @Override
    public void onClick(View v) {
        //※空のクリック処理をオーバーライドしないと、タッチ処理が検出されないため、空処理を入れとく
        //do nothing
    }

    /*
     * ダブルタップリスナー
     */
    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {

            Log.i("tap", "onDoubleTap");

            return super.onDoubleTap(event);
        }
    }

    /*-- getter／setter --*/
    public float getCenterPosX() {
        return mCenterPosX;
    }
    public void setCenterPosX(float centerPosX) {
        this.mCenterPosX = centerPosX;
    }

    public float getCenterPosY() {
        return mCenterPosY;
    }
    public void setCenterPosY(float centerPosY) {
        this.mCenterPosY = centerPosY;
    }

}
