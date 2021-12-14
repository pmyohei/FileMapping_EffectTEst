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

    //位置変更ノードキュー
    private NodeArrayList<NodeTable> mMovedNodesQue;

    /**
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mNodes = new NodeArrayList<>();
        mMovedNodesQue = new NodeArrayList<>();
    }

    /**
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        mNodes = null;
        mMovedNodesQue = null;
    }

    /*
     * 共通データ初期化
     */
    public void init() {

        //リストクリア
        mNodes.clear();
        mMovedNodesQue.clear();

        //ピンチ比率
        pinchDistanceRatioX = 1.0f;
        pinchDistanceRatioY = 1.0f;
    }



    /*
     * ノードリストの取得・設定・追加
     */
    public void setNodes( NodeArrayList<NodeTable> nodes ) {
        mNodes = nodes;
    }
    public NodeArrayList<NodeTable> getNodes() {
        return mNodes;
    }
    public void addNodes( NodeTable node ) {
        this.mNodes.add( node );
    }

    /*
     * 位置移動ノードキュー取得・設定・クリア
     */
    public NodeArrayList<NodeTable> getMovedNodesQue() {
        return mMovedNodesQue;
    }
    public void setMovedNodesQue(NodeArrayList<NodeTable> mMovedNodesQue) {
        this.mMovedNodesQue = mMovedNodesQue;
    }
    public void clearMovedNodesQue() {
        this.mMovedNodesQue.clear();
    }

    /*
     * 位置移動ノードキューへエンキュー
     * 　※既に追加済みの場合は、エンキューしない
     */
    public void enqueMovedNodeWithUnique(NodeTable node) {

        //追加済みなら、何もしない
        if( mMovedNodesQue.getNode(node.getPid()) != null ){
            return;
        }

        //エンキュー
        this.mMovedNodesQue.add(node);
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
