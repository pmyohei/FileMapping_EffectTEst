package com.mapping.filemapping;

import android.graphics.Typeface;
import android.widget.Switch;

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
     *　指定PIDのノードを取得
     */
    public NodeTable getNode(int pid) {

        int size = size();
        for (int i = 0; i < size; i++) {

            if (pid == get(i).getPid()) {
                //指定親ノードのPIDと一致するノードを返す
                return get(i);
            }
        }

        return null;
    }

    /*
     *　指定親Pidの子ノードをリストとして取得
     */
    public NodeArrayList<NodeTable> getChildNodes(int parentPid) {

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
    public boolean hasSameNodeNameAtParent(int parentPid, String nodeName) {

        //指定ノードの子ノードリスト
        NodeArrayList<NodeTable> nodes = getChildNodes(parentPid);

        //実検索
        return doHasSameNodeNameAtParent( nodes, nodeName );
    }

    /*
     *　同一名のノード保持判定
     *   指定ノードの子ノード（直下）の中に、指定ノード名と同じ名前のノードがあるかをチェックする。
     *   ただし、para2で指定したノードは検索対象外とする。
     */
    public boolean hasSameNodeNameAtParent(int parentPid, int exclusionPid, String nodeName) {

        //指定ノードの子ノードリスト
        NodeArrayList<NodeTable> nodes = getChildNodes(parentPid);

        //検索対象外ノードを、検索対象ノードから除外
        nodes.deleteNode( exclusionPid );

        //実検索
        return doHasSameNodeNameAtParent( nodes, nodeName );
    }

    /*
     *　同一名のノードを既に持っているか（実処理）
     */
    private boolean doHasSameNodeNameAtParent(NodeArrayList<NodeTable> nodes, String nodeName) {

        //ノード数分ループ
        for (NodeTable node : nodes) {

            //ノード名が同じ場合
            if ( (node.getKind() != NodeTable.NODE_KIND_PICTURE) && (node.getNodeName().equals(nodeName)) ) {
                return true;
            }
        }

        //ノード未保持
        return false;
    }

    /*
     *　指定ノードの直下のノード取得（孫ノードは対象外）
     */
    public NodeArrayList<NodeTable> getDirectlyBelow(int pid) {

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
    }

    /*
     *　指定ノード配下のノード取得
     *   param：指定ノードもリストに含めるかどうか
     *          true ：含める
     *          false：含めない
     */
    public NodeArrayList<NodeTable> getUnderNodes(int pid, boolean isMyself) {

        NodeArrayList<NodeTable> nodes = new NodeArrayList<>();

        //ノード数分ループ
        for (NodeTable node : this) {

            //指定ノードもリストに追加
            if( isMyself && (pid == node.getPid()) ){
                nodes.add(node);
            }

            //指定ノードを親ノードとするノード
            if (pid == node.getPidParentNode()) {
                //このノードの配下ノードを取得
                NodeArrayList<NodeTable> tmp = getUnderNodes(node.getPid(), isMyself);
                nodes.addAll(tmp);
            }
        }

        return nodes;
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
        NodeArrayList<NodeTable> directlyBelowNodes = getDirectlyBelow( node.getPid() );

        //直下ノード分ループ
        for( NodeTable childNode: directlyBelowNodes ){
            createHierarchyList( addList, childNode );
        }
    }
    
    /*
     *　ノードの階層を取得
     *   ＜階層レベル参考＞
     *     　・ルートノード　：１
     *       　・ノードA　　：２
     *       　　・ノードa　：３
     *       　　・ノードa　：３
     *       　・ノードB　　：２
     *       　　・ノードb　：３
     */
    public int getHierarchyLevel( NodeTable targetNode ) {

        //ルートノード
        if( targetNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //1階層を返す
            return 1;
        }

        //階層レベル
        int level = 1;

        //親の階層レベルを取得
        NodeTable parentNode = getNode( targetNode.getPidParentNode() );
        level += getHierarchyLevel( parentNode, level );

        //このルートは通らない想定（ルートノードは必ず存在するため）
        return level;
    }

    /*
     *　ノードの階層を取得
     */
    private int getHierarchyLevel( NodeTable targetNode, int level ) {

        //ルートノード
        if( targetNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //1階層を返す
            return 1;
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
            node.setNodeColor( color );
            node.getNodeView().setNodeBackgroundColor( color );
        }
    }

    /*
     *　全ノードのテキスト色の設定
     */
    public void setAllNodeTxColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            //★テーブルにも反映必要
            //node.
            node.getNodeView().setNodeTextColor( color );
        }
    }

    /*
     *　全ノードのテキストフォントの設定
     */
    public void setAllNodeFont(Typeface font ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            //★テーブルにも反映必要
            //node.
            node.getNodeView().setNodeFont( font );
        }
    }

    /*
     *　全ノードの形の設定
     */
    public void setAllNodeShape( int shapeKind ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            //★テーブルにも反映必要
            //node.
            node.getNodeView().setNodeShape( shapeKind );
        }
    }

    /*
     *　全ノードの枠色の設定
     */
    public void setAllNodeBorderColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            //★テーブルにも反映必要
            //node.
            node.getNodeView().setBorderColor( color );
        }
    }

    /*
     *　全ノードの枠サイズの設定
     */
    public void setAllNodeBorderSize( int thick ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            //★テーブルにも反映必要
            //node.
            node.getNodeView().setBorderSize( thick );
        }
    }

    /*
     *　全ノードの影色の設定
     */
    public void setAllNodeShadowColor( String color ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setShadowColor( color, node.getKind() );
        }
    }

    /*
     *　全ノードの影の有無の設定
     */
    public void setAllNodeShadow( boolean isShadow ) {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().setShadowOnOff( isShadow, node.getKind()  );
        }
    }

    /*
     *　全ノードの影の有無の切り替え
     */
    public void switchAllNodeShadow() {

        //リストの内のノードすべて
        for( NodeTable node: this ){
            node.getNodeView().switchShadow( node.getKind() );
        }
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
     *　指定ノード配下のピクチャノードのpidを返す
     *   ※指定ノードがピクチャノードの場合は、指定ノードのみが返る
     */
    public List<Integer> getPictureNodes(int pid ) {

        List<Integer> pisd = new ArrayList<>();

        NodeTable node = getNode(pid);

        switch( node.getKind() ){
            case NodeTable.NODE_KIND_ROOT:
                pisd = getAllPictureNodes();
                break;

            case NodeTable.NODE_KIND_NODE:
                pisd = getPictureNodesUnderNode(node.getPid() );
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
    private List<Integer> getAllPictureNodes() {

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
     *　指定一般ノードのすべてのピクチャノードを取得
     */
    private List<Integer> getPictureNodesUnderNode( int pid ) {

        List<Integer> pids = new ArrayList<>();

        //配下ノードをすべて取得
        NodeArrayList<NodeTable> childNodes = getUnderNodes( pid, false );

        //リストの内の子ノードすべて
        for( NodeTable node: childNodes ){
            if( node.getKind() == NodeTable.NODE_KIND_PICTURE ){
                pids.add( node.getPid() );
            }
        }

        return pids;
    }
}
