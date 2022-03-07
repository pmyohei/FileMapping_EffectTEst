package com.mapping.filemapping;

import android.app.Application;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;

/*
 * マップ共通情報
 */
public class MapCommonData extends Application {

    //ピンチ操作後のビュー間の距離の比率
    private float pinchDistanceRatioX = 1.0f;
    private float pinchDistanceRatioY = 1.0f;

    //マップ内のノードリスト
    private MapTable mMap;
    //マップ内のノードリスト
    private NodeArrayList<NodeTable> mNodes;
    //マップ内のサムネイルリスト
    //★不要な見込み
    private PictureArrayList<PictureTable> mThumbnails;
    //位置変更ノードキュー
    private NodeArrayList<NodeTable> mUpdateNodeQue;
    //削除対象ノード
    private NodeArrayList<NodeTable> mDeleteNodes;
    //ツールアイコン表示中ノード
    //private BaseNode mToolOpeningNode = null;
    //編集対象ノード
    private NodeTable mEditNode = null;
    //色履歴
    private ArrayList<String> mColorHistory;
    //全ノードエンキュー有無
    private boolean mIsAllNodeEnque;

    /*
     * アプリケーションの起動時に呼び出される
     */
    @Override
    public void onCreate() {
        super.onCreate();

        mNodes = new NodeArrayList<>();
        mUpdateNodeQue = new NodeArrayList<>();
        mThumbnails = new PictureArrayList<>();
        mDeleteNodes = new NodeArrayList<>();
        mColorHistory = new ArrayList<>();
    }

    /*
     * アプリケーション終了時に呼び出される
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        mMap = null;
        mNodes = null;
        mUpdateNodeQue = null;
        mThumbnails = null;
        mEditNode = null;
        mDeleteNodes = null;
        mColorHistory = null;
    }

    /*
     * 共通データ初期化
     */
    public void init() {

        //リストクリア
        mNodes.clear();
        mThumbnails.clear();
        mUpdateNodeQue.clear();
        mDeleteNodes.clear();
        mColorHistory.clear();

        //状態リセット
        mIsAllNodeEnque = false;

        //ピンチ比率
        pinchDistanceRatioX = 1.0f;
        pinchDistanceRatioY = 1.0f;
    }

    public void setMap( MapTable map ){
        mMap = map;
    }
    public MapTable getMap(){
        return mMap;
    }

    /*
     * マップPidを取得
     */
    public int getMapPid() {
        return mNodes.getRootNode().getPidMap();
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
     * サムネリストの取得・設定・追加
     */
    public void setThumbnails( PictureArrayList<PictureTable> nodes ) {
        mThumbnails = nodes;
    }
    public PictureArrayList<PictureTable> getThumbnails() {
        return mThumbnails;
    }
    public void addThumbnail( PictureTable thumbnail ) {
        this.mThumbnails.add( thumbnail );
    }

    /*
     * サムネイルリストを更新
     */
    public PictureTable updateThumbnail( PictureTable oldPicture, PictureTable newPicture ) {

        PictureTable currentThumbnail = null;

        if( oldPicture != null ){
            //現在サムネイルとして設定されている情報を更新

            if( oldPicture.isThumbnail() ){
                //サムネイルのままであれば、更新
                mThumbnails.updatePicture( oldPicture );
            } else{
                //サムネイルではなくなっていれば、リストから削除
                mThumbnails.deletePicture(oldPicture);
            }

            currentThumbnail = oldPicture;
        }

        if( newPicture != null ){
            mThumbnails.add( newPicture );

            currentThumbnail = newPicture;
        }

        return currentThumbnail;
    }

    /*
     * 色履歴リストの生成
     */
    public void createColorHistory( MapTable map, View v_map ) {

        //色履歴一時リスト（重複あり、大文字小文字混在）
        ArrayList<String> tmpColors = new ArrayList<>();

        //デフォルトカラーを一時リストに追加
        String[] colors = map.getDefaultColors();
        for( String defaultColor: colors ){
            if( defaultColor != null ){
                tmpColors.add( defaultColor );
            }
        }

        //現在のマップ背景色
        ColorDrawable colorDrawable = (ColorDrawable) v_map.getBackground();
        int colorInt = colorDrawable.getColor();
        String mapColor = "#" + Integer.toHexString(colorInt);
        //大文字変換
        mapColor = mapColor.toUpperCase(Locale.ROOT);

        //透明度の情報があれば、除外する
        final String TRANCEPARENT = "#FF";
        if( ( mapColor.length() == 9 ) && ( mapColor.contains(TRANCEPARENT)) ){
            //#FF001122→#001122 にする
            mapColor = mapColor.replace(TRANCEPARENT, "#");
        }

        //一時リストに追加
        tmpColors.add( mapColor );

        //マップ中のノードに設定されている色を取得
        tmpColors.addAll( mNodes.getAllNodeColors() );

        //色情報をすべて大文字にしてリストを再作成
        //※重複を排除するとき、大文字と小文字で別の色と判定されるのを回避するため
        ArrayList<String> tmpUpperColors = new ArrayList<>();
        for( String color: tmpColors ){
            tmpUpperColors.add( color.toUpperCase(Locale.ROOT) );
        }

        //重複なしで設定
        mColorHistory.addAll( new ArrayList<>(new LinkedHashSet<>(tmpUpperColors)) );
    }

    /*
     * 色履歴リストの取得
     */
    public ArrayList<String> getColorHistory() {
        return mColorHistory;
    }

    /*
     * 色履歴リストに色を追加
     */
    public int addColorHistory( String addColor ) {

        for( String color: mColorHistory ){

            if( color.equals( addColor ) ){
                //既に同じ色があれば、追加なし
                return -1;
            }
        }

        //新しい色であれば追加
        mColorHistory.add( addColor );
        return mColorHistory.size() - 1;
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
    public NodeArrayList<NodeTable> getUpdateNodeQue() {
        return mUpdateNodeQue;
    }
    public void clearUpdateNodeQue() {
        this.mUpdateNodeQue.clear();

        //キュークリアされたので、リセット
        mIsAllNodeEnque = false;
    }

    /*
     * 更新対象ノードキューへエンキュー
     * 　※既に追加済みの場合は、エンキューしない
     */
    public void enqueUpdateNodeWithUnique(NodeTable node) {

        //追加済みなら、何もしない
        if( mUpdateNodeQue.getNode(node.getPid()) != null ){
            return;
        }

        //エンキュー
        this.mUpdateNodeQue.add(node);
    }

    /*
     * 更新対象ノードキューへエンキュー（全ノード対象）
     * 　※既に追加済みの場合は、エンキューしない
     */
    public void enqueAllNodeWithUnique() {

        if( mIsAllNodeEnque ){
            //一度でも本処理をしていれば、2度目移行は不要
            return;
        }

        //ノード全変更されたか
        if( mNodes.isAllChanged() ){
            //マップ上のすべてのノードを更新対象にする
            this.mUpdateNodeQue.clear();
            this.mUpdateNodeQue.addAll( mNodes );

            //一度でも本処理をしていれば、2度目移行は不要
            //※新規ノードは、生成時に更新キューに格納されるため
            mIsAllNodeEnque = true;
        }
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
/*    public BaseNode getToolOpeningNode() {
        return mToolOpeningNode;
    }
    public void setToolOpeningNode(BaseNode mToolOpeningNode) {
        this.mToolOpeningNode = mToolOpeningNode;
    }
    public void closeToolOpeningNode() {
        this.mToolOpeningNode.operationToolIcon();
    }
    public boolean isToolOpening() {
        //ノードあるなら、開き中
        return (this.mToolOpeningNode != null );
    }*/

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
        mDeleteNodes = mNodes.getUnderNodes( pid, true );

        //log
        for( NodeTable node: mDeleteNodes ){
            Log.i("setDeleteNodes", "nodeName=" + node.getNodeName());
        }
        //log
    }

    /*
     * 削除対象ノードの削除完了処理
     *   ※本処理は、DBからノードの削除が完了した時にコールされる想定
     *   ・マップ上のノードリストから、削除対象ノードを削除する
     *   ・ノード更新対象キューから、削除対象ノードを削除する
     */
    public void finishDeleteNode() {

        //log---------------
/*        for( NodeTable node: mDeleteNodes ){
            Log.i("finishDeleteNodes", "削除対象ノード=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");
        for( NodeTable node: mNodes ){
            Log.i("finishDeleteNodes", "マップ内ノード(削除前)=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");
        for( NodeTable node: mUpdateNodeQue ){
            Log.i("finishDeleteNodes", "更新キュー(削除前)=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");*/
        //---------------

        //ノードリストから、削除リストのノードを削除する
        mNodes.deleteNodes( mDeleteNodes );
        mUpdateNodeQue.deleteNodes( mDeleteNodes );

        //log---------------
/*        for( NodeTable node: mNodes ){
            Log.i("finishDeleteNodes", "マップ内ノード(削除後)=" + node.getNodeName());
        }
        Log.i("finishDeleteNodes", "----------");
        for( NodeTable node: mUpdateNodeQue ){
            Log.i("finishDeleteNodes", "更新キュー(削除後)=" + node.getNodeName());
        }*/
        //---------------

        //削除対象クリア
        mDeleteNodes.clear();
    }

}
