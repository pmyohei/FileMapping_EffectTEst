package com.mapping;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.mapping.filemapping.OldLineView;
import com.mapping.filemapping.MapInfoManager;
import com.mapping.filemapping.NodeTable;

/*
 * ノードタッチリスナー
 */
public class oldNodeTouchListener implements View.OnTouchListener {

    //マップ情報管理
    private final MapInfoManager mMapInfoManager;

    private final View      mvNode;         //ドラッグ対象のView
    private final NodeTable mNode;          //ノード
    private OldLineView mParentLine;    //親ノードとのライン

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
    public oldNodeTouchListener(View dragNode, NodeTable node) {
        mvNode = dragNode;
        mNode  = node;

        //※空のクリック処理をオーバーライドしないと、タッチ処理が検出されないため、空処理を入れとく
        mvNode.setOnClickListener(new View.OnClickListener() {
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

        Log.i("NodeTouchListener", "onTouch out");

        //ダブルタップ処理
        mGestureDetector.onTouchEvent( event );

        //タッチしている位置取得（スクリーン座標）
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                //ライン情報未保持なら、保持する
                if( mParentLine == null ){
                    mParentLine = mNode.getLineView();
                }

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
                int left = mvNode.getLeft() + (int)((x - mPreTouchPosX) / pinchDistanceRatioX);
                int top  = mvNode.getTop()  + (int)((y - mPreTouchPosY) / pinchDistanceRatioY);

                //ノードの移動
                mvNode.layout(left, top, left + mvNode.getWidth(), top + mvNode.getHeight());

                //接続線の描画を更新
                float endPosx = left + (mvNode.getWidth()  / 2f);
                float endPosy = top  + (mvNode.getHeight() / 2f);

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
