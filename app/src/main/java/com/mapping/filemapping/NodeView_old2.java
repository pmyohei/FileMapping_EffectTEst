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

public class NodeView_old2 extends FrameLayout implements View.OnTouchListener, View.OnClickListener  {

    //マップ情報管理
    private MapInfoManager mMapInfoManager;

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;

    //前回のタッチ位置
    private int mPreTouchPosX;
    private int mPreTouchPosY;

    //ダブルタップ検知用
    private GestureDetector mGestureDetector;


    private float    mCenterPosX;        //ノード中心座標X
    private float    mCenterPosY;        //ノード中心座標Y
    private OldLineView mLineView;          //親ノードとの接続線

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView_old2(Context context) {
        super(context);

        init();
    }

    public NodeView_old2(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NodeView_old2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    /*
     * 初期化処理
     */
    private void init() {

        //タッチリスナー
        //setOnTouchListener( new NodeTouchListener() );

        //クリックリスナー
        //※空のクリック処理をオーバーライドしないと、タッチ処理が検出されないため、空処理を入れとく
/*        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });*/

        //マップ情報管理
        mMapInfoManager = MapInfoManager.getInstance(false);

        LayoutInflater inflater = LayoutInflater.from( getContext() );
        inflater.inflate(R.layout.node, this, true);

        //ダブルタップリスナーを実装したGestureDetector
        mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());
    }


    /*
     * ノード名の設定
     */
    public void setNodeName( String name ) {
        ((TextView)findViewById(R.id.tv_node)).setText(name);
    }


    /*
     * 追随
     */
    public void follow( float movex, float movey ) {

        Log.i("test", "aaaaa");

        //今回イベントでのView移動先の位置
        //※移動量からピンチ操作率は取り除く
        int left = getLeft() + (int)(movex / pinchDistanceRatioX);
        int top  = getTop()  + (int)(movey / pinchDistanceRatioY);

        layout(left, top, left + getWidth(), top + getHeight());

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        //ダブルタップ処理
        mGestureDetector.onTouchEvent( event );

        //タッチしている位置取得（スクリーン座標）
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                //タッチ開始時のピンチ操作比率を取得
                pinchDistanceRatioX = mMapInfoManager.getPinchDistanceRatioX();
                pinchDistanceRatioY = mMapInfoManager.getPinchDistanceRatioY();

                //今回のタッチ位置を保持
                mPreTouchPosX = x;
                mPreTouchPosY = y;

                break;

            case MotionEvent.ACTION_MOVE:

                //今回イベントでのView移動先の位置
                //※移動量からピンチ操作率は取り除く
                int left = getLeft() + (int)((x - mPreTouchPosX) / pinchDistanceRatioX);
                int top  = getTop()  + (int)((y - mPreTouchPosY) / pinchDistanceRatioY);

                //ノードの移動
                layout(left, top, left + getWidth(), top + getHeight());

                //接続線の描画を更新
                float endPosx = left + (getWidth()  / 2f);
                float endPosy = top  + (getHeight() / 2f);

                mLineView.moveEndPos( endPosx, endPosy );

                //今回のタッチ位置を保持
                mPreTouchPosX = x;
                mPreTouchPosY = y;

                //イベント処理完了
                return true;
        }

        //イベント処理完了
        return false;
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


    /*
     * ノードタッチリスナー
     */
/*
    private class NodeTouchListener implements View.OnTouchListener {
        //前回のタッチ位置
        private int mPreTouchPosX;
        private int mPreTouchPosY;


        //ダブルタップ検知用
        private final GestureDetector mGestureDetector;


        */
/*
         * コンストラクタ
         *//*

        public NodeTouchListener() {

            //ダブルタップリスナーを実装したGestureDetector
            mGestureDetector = new GestureDetector(getContext(), new NodeTouchListener.DoubleTapListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ダブルタップ処理
            mGestureDetector.onTouchEvent( event );

            //タッチしている位置取得（スクリーン座標）
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    //タッチ開始時のピンチ操作比率を取得
                    pinchDistanceRatioX = mMapInfoManager.getPinchDistanceRatioX();
                    pinchDistanceRatioY = mMapInfoManager.getPinchDistanceRatioY();

                    //今回のタッチ位置を保持
                    mPreTouchPosX = x;
                    mPreTouchPosY = y;

                    break;

                case MotionEvent.ACTION_MOVE:

                    //今回イベントでのView移動先の位置
                    //※移動量からピンチ操作率は取り除く
                    int left = getLeft() + (int)((x - mPreTouchPosX) / pinchDistanceRatioX);
                    int top  = getTop()  + (int)((y - mPreTouchPosY) / pinchDistanceRatioY);

                    //ノードの移動
                    layout(left, top, left + getWidth(), top + getHeight());

                    //接続線の描画を更新
                    float endPosx = left + (getWidth()  / 2f);
                    float endPosy = top  + (getHeight() / 2f);

                    mLineView.moveEndPos( endPosx, endPosy );

                    //今回のタッチ位置を保持
                    mPreTouchPosX = x;
                    mPreTouchPosY = y;

                    //イベント処理完了
                    return true;
            }

            //イベント処理完了
            return false;
        }

        */
/*
         * ダブルタップリスナー
         *//*

        private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
            @Override
            public boolean onDoubleTap(MotionEvent event) {

                Log.i("tap", "onDoubleTap");

                return super.onDoubleTap(event);
            }
        }

    }
*/



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

    public OldLineView getLineView() {
        return mLineView;
    }
    public void setLineView(OldLineView lineView) {
        this.mLineView = lineView;
    }


}
