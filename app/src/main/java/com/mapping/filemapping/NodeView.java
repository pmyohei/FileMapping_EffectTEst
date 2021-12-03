package com.mapping.filemapping;

import android.annotation.SuppressLint;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.room.Ignore;

import com.mapping.NodeTouchListener;

public class NodeView extends RootNodeView implements View.OnTouchListener {

    //マップ情報管理
    public MapInfoManager mMapInfoManager;

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;

    //前回のタッチ位置
    private int mPreTouchPosX;
    private int mPreTouchPosY;

    //親ノードとの接続線
    private LineView mLineView;

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context) {
        super(context);

        init();
    }

    public NodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public NodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    /*
     * 初期化処理
     */
    private void init() {

        Log.i("NodeView", "init");

        //マップ情報管理
        mMapInfoManager = MapInfoManager.getInstance(false);

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from( getContext() );
        inflater.inflate(R.layout.root_node, this, true);
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

        Log.i("NodeView", "onTouch");

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









    /*-- getter／setter --*/
    public LineView getLineView() {
        return mLineView;
    }
    public void setLineView(LineView lineView) {
        this.mLineView = lineView;
    }


}
