package com.mapping.filemapping;

import android.graphics.Typeface;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/*
 * ArrayList：ノード用
 *   任意のマップに所属するノードを保持する目的で使用する
 */
public class NodeArrayList<E> extends ArrayList<NodeTable> implements Serializable {

    /* 定数 */
    public static final int NO_DATA = -1;   //データなし

    //トップ階層レベル（ルートノードの階層レベルに相当）
    private final int TOP_HIERARCHY_LEVEL = 1;

    //ノード数上限
    private final int UPPER_LIMIT_NODE_NUM = 5;           //ノード数上限
    private final int UPPER_LIMIT_PICTURE_NODE_NUM = 2;   //ピクチャノード数上限
    //階層上限
    private final int UPPER_LIMIT_HIERARCHY = 5;

    //全ノード変更の有無
    private boolean mIsAllChanged = false;

    /*
     * コンストラクタ
     */
    public NodeArrayList() {
        super();
    }

    /*
     *　ラストIndex取得
     */
    public int getLastIdx() {

        int size = size();

        if (size == 0) {
            return NO_DATA;
        }

        return size - 1;
    }

    /*
     * ノード全体のデザイン変更があったかどうか
     */
    public boolean isAllChanged() {
        return mIsAllChanged;
    }

    /*
     *　指定PIDのノードを取得
     */
    public NodeTable getNode(int pid) {

        int size = size();
        for (int i = 0; i < size; i++) {

            if (pid == get(i).getPid()) {
                //指定ノードPIDと一致するノードを返す
                return get(i);
            }
        }

        return null;
    }

    /*
     *　指定親Pidの子ノード（直下のみ）をリストとして取得
     */
    public NodeArrayList<NodeTable> getDirectlyChildNodes(int parentPid) {

        //検索結果
        NodeArrayList<NodeTable> result = new NodeArrayList<>();

        //ノード数分ループ
        for (NodeTable node : this) {
            //親ノード検索
            if (parentPid == node.getPidParentNode()) {
                //リストに追加
                result.add(node);
            }
        }

        return result;
    }

    /*
     *　同一名のノード保持判定
     *   指定ノードの子ノード（直下）の中に、指定ノード名と同じ名前のノードがあるかをチェックする
     */
/*    public boolean hasSameNodeNameAtParent(int parentPid, String nodeName) {

        //指定ノードの子ノードリスト
        NodeArrayList<NodeTable> nodes = getDirectlyChildNodes(parentPid);
        //実検索
        return doHasSameNodeNameAtParent( nodes, nodeName );
    }*/

    /*
     *　同一名のノード保持判定
     *   指定ノードの子ノード（直下）の中に、指定ノード名と同じ名前のノードがあるかをチェックする。
     *   ただし、para2で指定したノードは検索対象外とする。
     */
/*    public boolean hasSameNodeNameAtParent(int parentPid, int exclusionPid, String nodeName) {

        //指定ノードの子ノードリスト
        NodeArrayList<NodeTable> nodes = getDirectlyChildNodes(parentPid);

        //検索対象外ノードを、検索対象ノードから除外
        nodes.deleteNode( exclusionPid );

        //実検索
        return doHasSameNodeNameAtParent( nodes, nodeName );
    }*/

    /*
     *　同一名のノードを既に持っているか（実処理）
     */
/*    private boolean doHasSameNodeNameAtParent(NodeArrayList<NodeTable> nodes, String nodeName) {

        //ノード数分ループ
        for (NodeTable node : nodes) {

            //ノード名が同じ場合
            if ( (node.getKind() != NodeTable.NODE_KIND_PICTURE) && (node.getNodeName().equals(nodeName)) ) {
                return true;
            }
        }

        //ノード未保持
        return false;
    }*/

    /*
     *　指定ノードの直下のノード取得（孫ノードは対象外）
     */
/*    public NodeArrayList<NodeTable> getDirectlyBelow(int pid) {

        NodeArrayList<NodeTable> nodes = new NodeArrayList<>();

        //ノード数分ループ
        for (NodeTable node : this) {
            //指定ノードを親ノードとするノード
            if (pid == node.getPidParentNode()) {
                //このノードの配下ノードを取得
                nodes.add( node );
            }
        }

        return nodes;
    }*/

    /*
     *　指定ノード配下のノード取得(子・孫全て)
     *   param：指定ノードもリストに含めるかどうか
     *          true ：含める
     *          false：含めない
     */
    public NodeArrayList<NodeTable> getAllChildNodes(int pid, boolean isMyself) {

        NodeArrayList<NodeTable> nodes = new NodeArrayList<>();

        //ノード数分ループ
        for (NodeTable node : this) {

            //指定ノードもリストに追加
            if (isMyself && (pid == node.getPid())) {
                nodes.add(node);
            }

            //指定ノードを親ノードとするノード
            if (pid == node.getPidParentNode()) {
                //このノードの配下ノードを取得
                //※ここでのgetUnderNodes()は再帰処理になるため、true指定となる（trueにしないと、このルートに入ったノードが追加されない）
                NodeArrayList<NodeTable> tmp = getAllChildNodes(node.getPid(), true);
                nodes.addAll(tmp);
            }
        }

        return nodes;
    }

    /*
     * 上限到達判定（配下ノード数）
     *   para1:判定対象のノードpid（このノードの子ノードの数がチェック対象となる）
     *   para2:上限判定対象のノード種別
     *
     *   return：true -上限に達している
     *         ：false-上限未到達（まだ余裕あり）
     */
    public boolean isUpperLimitNum(int pid, int nodeKind) {

        //上限値
        int limit = ( nodeKind == NodeTable.NODE_KIND_NODE ? UPPER_LIMIT_NODE_NUM : UPPER_LIMIT_PICTURE_NODE_NUM );
        int count = 0;

        //ノード数分ループ
        for (NodeTable node : this) {
            //指定ノードを親とするノードで、かつ、指定された種別のノードの場合
            if ( (pid == node.getPidParentNode()) && (nodeKind == node.getKind()) ) {
                //カウントアップ
                count++;
            }
        }

        //Log.i("上限チェック", "数=" + count);

        return (count >= limit );
    }

    /*
     * 上限到達判定（階層）
     *   para1：チェック対象のノード
     */
    public boolean isUpperLimitHierarchy(NodeTable checkNode) {

        //指定ノードの階層レベル
        int level = getHierarchyLevel( checkNode );

        Log.i("上限チェック", "階層数=" + level);

        return (level >= UPPER_LIMIT_HIERARCHY );
    }

    /*
     * 上限到達判定（階層）
     *   親ノード変更用
     *   para1：チェック対象のノード（変更先の親ノード）
     *   para2：変更対象のノード
     */
    public boolean isUpperLimitHierarchy(NodeTable checkNode, NodeTable moveNode) {

        //変更先の親ノードの階層レベル
        int level = getHierarchyLevel( checkNode );
        //変更対象ノードの階層レベル
        int levelMoveNode = getHierarchyLevel( moveNode );

        //一番階層レベルの低い（階層が深い）ノードのレベルを取得
        int levelDeepest = levelMoveNode;
        NodeArrayList<NodeTable> allChildNodes = getAllChildNodes( moveNode.getPid() ,false );
        for( NodeTable childNode: allChildNodes ){
            if( childNode.getKind() == NodeTable.NODE_KIND_PICTURE ){
                //ピクチャノードは判定対象外
                continue;
            }

            //階層が深ければ更新
            int levelChild = getHierarchyLevel( childNode );
            levelDeepest = Math.max( levelChild, levelDeepest );
        }

        //変更するノードの階層数（※階層レベルではなく階層数）
        //例）階層レベル2
        //   階層レベル4
        //   ⇒階層数3
        int lovelNum = (levelDeepest - levelMoveNode) + 1;

        //Log.i("上限チェック", "-------------------");
        //Log.i("上限チェック", "移動先=" + level);
        //Log.i("上限チェック", "変更ノード=" + levelMoveNode);
        //Log.i("上限チェック", "変更ノードの最下層ノード=" + levelDeepest);
        //Log.i("上限チェック", "階層数=" + lovelNum);
        //Log.i("上限チェック", "最終階層レベル=" + (level + lovelNum));

        //連結した結果、上限を超えるかどうか
        return ( (level + lovelNum) > UPPER_LIMIT_HIERARCHY );
    }

    /*
     *　指定ノードをリストから削除
     */
    public void deleteNode(int pid) {

        //リストループ
        for( NodeTable node: this ){

            //指定ノードと同じノード発見
            if( node.getPid() == pid ){
                //リストから削除
                remove( node );
                return;
            }
        }
    }

    /*
     *　指定ノードをリストから削除
     */
    public void deleteNodes( NodeArrayList<NodeTable> nodes ) {
        //指定ノード分ループ
        for( NodeTable node: nodes ){
            //削除
            deleteNode(node.getPid() );
        }
    }

    /*
     *　階層化された順番のリストを取得
     *   例）ルートノード
     *     　ノードＡ
     *       ノードa1（親ノードＡ）
     *       ノードa2（親ノードＡ）
     *     　ノードＢ
     *       ノードb1（親ノードＢ）
     *     　ノードＣ
     *     　ノードＤ
     */
    public NodeArrayList<NodeTable> getHierarchyList() {

        //階層化リスト
        NodeArrayList<NodeTable> hierarchyList = new NodeArrayList<>();
        //ルートノード
        NodeTable root = getRootNode();

        //階層化リストを生成
        createHierarchyList( hierarchyList, root );

        return hierarchyList;
    }

    /*
     *　階層化リストを生成
     */
    public void createHierarchyList(NodeArrayList<NodeTable> addList, NodeTable node ) {

        //指定ノードをリストに追加
        addList.add( node );

        //指定ノードの直下ノードを取得
        NodeArrayList<NodeTable> directlyBelowNodes = getDirectlyChildNodes( node.getPid() );

        //直下ノード分ループ
        for( NodeTable childNode: directlyBelowNodes ){
            createHierarchyList( addList, childNode );
        }
    }
    
    /*
     *　ノードの階層を取得
     *   ＜参考：階層レベル＞
     *     　・ルートノード　：１
     *       　・ノードA　　：２
     *       　　・ノードa　：３
     *       　　・ノードa　：３
     *       　・ノードB　　：２
     *       　　・ノードb　：３
     *
     *   例）引数に「ノードb」を指定すると、3を返す
     */
    public int getHierarchyLevel( NodeTable targetNode ) {

        //ルートノード
        if( targetNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //1階層を返す
            return TOP_HIERARCHY_LEVEL;
        }

        //階層レベル
        int level = TOP_HIERARCHY_LEVEL;

        //親の階層レベルを取得
        NodeTable parentNode = getNode( targetNode.getPidParentNode() );
        level += getHierarchyLevel( parentNode, level );

        //このルートは通らない想定（ルートノードは必ず存在するため）
        return level;
    }

    /*
     *　ノードの階層を取得（再帰用）
     */
    private int getHierarchyLevel( NodeTable targetNode, int level ) {

        //ルートノード
        if( targetNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //1階層を返す
            return TOP_HIERARCHY_LEVEL;
        }

        //親ノード
        NodeTable parentNode = getNode( targetNode.getPidParentNode() );
        level += getHierarchyLevel( parentNode, level );

        //このルートは通らない想定（ルートノードは必ず存在するため）
        return level;
    }

    /*
     *　本リストからルートノードを取得
     */
    public NodeTable getRootNode() {
        for( NodeTable node: this ){
            if( node.getKind() == NodeTable.NODE_KIND_ROOT ){
                return node;
            }
        }

        //このルートは通らない想定（ルートノードは必ず存在するため）
        return null;
    }

    /*
     *　先頭の子ノードを返す
     */
    public NodeTable getTopChildNode() {

        for( NodeTable node: this ){
            if( node.getKind() != NodeTable.NODE_KIND_ROOT ){
                //ルート以外があれば、返す
                return node;
            }
        }

        return null;
    }

    /*
     *　全ノードの背景色の設定
     */
    public void setAllNodeBgColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setNodeBackgroundColor( color );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードのテキスト色の設定
     */
    public void setAllNodeTxColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setNodeTextColor( color );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードのテキストフォントの設定
     */
    public void setAllNodeFont(Typeface font, String fontFileName ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setNodeFont( font, fontFileName );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの形の設定
     */
    public void setAllNodeShape( int shapeKind ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setNodeShape( shapeKind );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの枠色の設定
     */
    public void setAllNodeBorderColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setBorderColor( color );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの枠サイズの設定
     */
    public void setAllNodeBorderSize( int thick ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setBorderSize( thick );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの影色の設定
     */
    public void setAllNodeShadowColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setShadowColor( color, node.getKind() );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの影の有無の設定
     */
    public void setAllNodeShadow( boolean isShadow ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setShadowOnOff( isShadow  );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードの影の有無の切り替え
     */
    public void switchAllNodeShadow() {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().switchShadow();
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードのライン色の設定
     */
    public void setAllNodeLineColor( String color ) {

        //リストの内の子ノードすべて
        for( NodeTable node: this ){

            if( node.getKind() == NodeTable.NODE_KIND_ROOT ){
                //ルートはライン情報を持たないためスキップ
                continue;
            }

            ChildNode childNode = (ChildNode)node.getNodeView();
            childNode.setLineColor( color );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードのラインサイズの設定
     */
    public void setAllNodeLineSize( float thick ) {

        //リストの内の子ノードすべて
        for( NodeTable node: this ){

            if( node.getKind() == NodeTable.NODE_KIND_ROOT ){
                //ルートはライン情報を持たないためスキップ
                continue;
            }

            ChildNode childNode = (ChildNode)node.getNodeView();
            childNode.setLineSize( thick );
        }

        //全体デザイン変更あり
        mIsAllChanged = true;
    }

    /*
     *　全ノードに設定している色を取得（重複なしですべて一意になるリストを返す）
     */
    public ArrayList<String> getAllNodeColors() {

        ArrayList<String> colors = new ArrayList<>();

        //リストの内の子ノードすべて
        for( NodeTable node: this ){

            //ノードに設定中の色を取得
            ArrayList<String> tmpColors = node.getSettingColors();

            //追加
            colors.addAll( tmpColors );
        }

        //重複なしで返す
        return new ArrayList<>(new LinkedHashSet<>(colors));
    }

    /*
     *　ツールアイコンを開いているノードを取得
     */
    public BaseNode getShowingIconNode() {

        //リストの内の子ノードすべて
        for( NodeTable node: this ){
            //アイコンビューを持っているかチェック
            if( node.getNodeView().hasIconView() ){
                return node.getNodeView();
            }
        }

        //なければnull
        return null;
    }

    /*
     *　指定ノード配下のピクチャノードのpidリストを返す
     *   ※指定ノードがピクチャノードの場合は、指定ノードのみが返る
     */
    public List<Integer> getPictureNodes(int pid ) {

        List<Integer> pisd = new ArrayList<>();

        NodeTable node = getNode(pid);

        switch( node.getKind() ){
            case NodeTable.NODE_KIND_ROOT:
                pisd = getAllPictureNodePids();
                break;

            case NodeTable.NODE_KIND_NODE:
                pisd = getPictureNodesUnderNode(pid);
                break;

            case NodeTable.NODE_KIND_PICTURE:
                //指定Pidのみ
                pisd.add( node.getPid() );
                break;
        }

        return pisd;
    }

    /*
     *　マップ上のすべてのピクチャノードを取得
     */
    private List<Integer> getAllPictureNodePids() {

        List<Integer> pids = new ArrayList<>();

        //リストの内の子ノードすべて
        for( NodeTable node: this ){
            if( node.getKind() == NodeTable.NODE_KIND_PICTURE ){
                pids.add( node.getPid() );
            }
        }

        return pids;
    }

    /*
     *　リスト内のすべてのピクチャノードを取得
     */
    public NodeArrayList<NodeTable> getAllPictureNodes() {

        NodeArrayList<NodeTable> nodes = new NodeArrayList<>();

        //リストの内の子ノードすべて
        for( NodeTable node: this ){
            if( node.getKind() == NodeTable.NODE_KIND_PICTURE ){
                nodes.add( node );
            }
        }

        return nodes;
    }

    /*
     *　指定一般ノードの配下のすべてのピクチャノードを取得
     */
    private List<Integer> getPictureNodesUnderNode( int pid ) {

        List<Integer> pids = new ArrayList<>();

        //配下ノードをすべて取得
        NodeArrayList<NodeTable> childNodes = getAllChildNodes( pid, false );

        //リストの内の子ノードすべて
        for( NodeTable node: childNodes ){
            if( node.getKind() == NodeTable.NODE_KIND_PICTURE ){
                pids.add( node.getPid() );
            }
        }

        return pids;
    }

}
