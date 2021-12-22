package com.mapping.filemapping;

import android.app.Application;
import android.util.Log;

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
    //削除対象ノード
    private NodeArrayList<NodeTable> mDeleteNodes;
    //ツールアイコン表示中ノード
    private RootNodeView mToolOpeningNode = null;
    //編集対象ノード
    private NodeTable mEditNode = null;


    /*
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mNodes = new NodeArrayList<>();
        mMovedNodesQue = new NodeArrayList<>();
        mDeleteNodes = new NodeArrayList<>();
    }

    /**
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        mNodes = null;
        mMovedNodesQue = null;
        mToolOpeningNode = null;
        mEditNode = null;
        mDeleteNodes = null;
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
     * マップ内ノードリストから、指定ノードを返す
     */
    public NodeTable getNodeInMap(int pid) {
        return mNodes.getNode(pid);
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


    /*
     * ツールアイコン表示中ノードの取得／設定
     */
    public RootNodeView getToolOpeningNode() {
        return mToolOpeningNode;
    }
    public void setToolOpeningNode(RootNodeView mToolOpeningNode) {
        this.mToolOpeningNode = mToolOpeningNode;
    }
    public void closeToolOpeningNode() {
        this.mToolOpeningNode.operationToolIcon();
    }
    public boolean isToolOpening() {
        //ノードあるなら、開き中
        return (this.mToolOpeningNode != null );
    }

    /*
     * 編集対象ノード
     *   ※編集前/編集後のノードの受け渡しに使用する
     */
    public NodeTable getEditNode() {
        return mEditNode;
    }
    public void setEditNode(NodeTable mEditNode) {
        this.mEditNode = mEditNode;
    }

    /*
     * 削除対象ノードリスト取得
     */
    public NodeArrayList<NodeTable> getDeleteNodes() {
        return mDeleteNodes;
    }

    /*
     * 削除対象ノードリストの設定
     *   指定ノード配下のノード（指定ノード含む）を、削除リストに登録する
     */
    public void setDeleteNodes(int pid) {

        //念のた初期化
        mDeleteNodes.clear();

        //指定ノードの配下のノードを取得
        mDeleteNodes = mNodes.getUnderNodes( pid );

        //log
        for( NodeTable node: mDeleteNodes ){
            Log.i("setDeleteNodes", "nodeName=" + node.getNodeName());
        }
        //log
    }

    /*
     * 削除対象ノードの削除完了処理
     *   マップ上のノードリストから、削除対象ノードを削除する
     */
    public void finishDeleteNode() {

        //log---------------
        for( NodeTable node: mDeleteNodes ){
            Log.i("finishDeleteNodes", "削除対象ノード=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");
        for( NodeTable node: mNodes ){
            Log.i("finishDeleteNodes", "マップ内ノード(削除前)=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");
        //log---------------

        //マップ内ノードリストから、削除リストのノードを削除する
        mNodes.deleteNodes( mDeleteNodes );

        //log---------------
        for( NodeTable node: mNodes ){
            Log.i("finishDeleteNodes", "マップ内ノード(削除後)=" + node.getNodeName());
        }
        //log---------------

        //削除対象クリア
        mDeleteNodes.clear();
    }

}
