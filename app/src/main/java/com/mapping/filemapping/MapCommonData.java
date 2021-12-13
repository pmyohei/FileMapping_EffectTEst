package com.mapping.filemapping;

import android.app.Application;

/*
 * マップ共通情報
 */
public class MapCommonData extends Application {

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX = 1.0f;
    private float pinchDistanceRatioY = 1.0f;

    //マップ内のノードリスト
    private NodeArrayList<NodeTable> mNodes;

    /**
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mNodes = new NodeArrayList<>();
    }

    /**
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        mNodes = null;
    }

    /*
     * ノードリストの設定
     */
    public void setNodes( NodeArrayList<NodeTable> nodes ) {
        mNodes = nodes;
    }

    /*
     * ノードリストの取得
     */
    public NodeArrayList<NodeTable> getNodes() {
        return mNodes;
    }

    /*
     * ピンチ操作後のビュー間の距離の比率を設定
     */
    public void setPinchDistanceRatio( float x, float y ){
        pinchDistanceRatioX = x;
        pinchDistanceRatioY = y;
    }

    /*
     * ピンチ操作後のビュー間の距離の比率を取得
     */
    public float getPinchDistanceRatioX(){
        return pinchDistanceRatioX;
    }
    public float getPinchDistanceRatioY(){
        return pinchDistanceRatioY;
    }

}
