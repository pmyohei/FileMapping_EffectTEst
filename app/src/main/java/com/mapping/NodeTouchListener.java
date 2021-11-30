package com.mapping;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.mapping.filemapping.LineView;
import com.mapping.filemapping.MapInfoManager;

/*
 * ノードタッチリスナー
 */
public class NodeTouchListener implements View.OnTouchListener {

    //マップ情報管理
    private final MapInfoManager mMapInfoManager;

    //ドラッグ対象のView
    private final View     mNode;
    private final LineView mParentLine;

    //前回のタッチ位置
    private int mPreTouchPosX;
    private int mPreTouchPosY;

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX;
    private float pinchDistanceRatioY;

    //ダブルタップ検知用
    private final GestureDetector mGestureDetector;

    /*
     * コンストラクタ
     */
    public NodeTouchListener(View dragNode, LineView parentLine) {
        mNode       = dragNode;
        mParentLine = parentLine;

        //※空のクリック処理をオーバーライドしないと、タッチ処理が検出されないため、空処理を入れとく
        mNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("kyotoNode", "onClick");
            }
        });

        //マップ情報管理
        mMapInfoManager = MapInfoManager.getInstance(false);

        //ダブルタップリスナーを実装したGestureDetector
        mGestureDetector = new GestureDetector(dragNode.getContext(), new DoubleTapListener());
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
                int left = mNode.getLeft() + (int)((x - mPreTouchPosX) / pinchDistanceRatioX);
                int top  = mNode.getTop()  + (int)((y - mPreTouchPosY) / pinchDistanceRatioY);

                //ノードの移動
                mNode.layout(left, top, left + mNode.getWidth(), top + mNode.getHeight());

                //接続線の描画を更新
                float endPosx = left + (mNode.getWidth()  / 2f);
                float endPosy = top  + (mNode.getHeight() / 2f);

                mParentLine.moveEndPos( endPosx, endPosy );

                //今回のタッチ位置を保持
                mPreTouchPosX = x;
                mPreTouchPosY = y;

                //イベント処理完了
                return true;
        }

        //イベント処理完了
        return false;
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


}
