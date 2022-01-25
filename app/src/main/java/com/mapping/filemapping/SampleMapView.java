package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.List;

public class SampleMapView extends FrameLayout {

    private final int COLOR_SWITCH_MAX = 6;

    //サンプルマップ用の暫定ノードリスト
    private NodeArrayList<NodeTable> mTmpNodes;
    //カラーパターン
    private String[] mColors;
    //カラー配置変更回数
    private int mColorSwitchCount = 0;
    //カラー配置Index
    private final int[][] mColorTree = {
        {0, 1, 2},
        {0, 2, 1},
        {1, 0, 2},
        {1, 2, 0},
        {2, 0, 1},
        {2, 1, 0},
    };

    /*
     * コンストラクタ
     */
    public SampleMapView(Context context) {
        this(context, null);
    }

    public SampleMapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SampleMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    /*
     * 初期化
     */
    private void init(Context context) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.sample_map, this, true);

        //ルートノード
        RootNodeView v_rootnode = findViewById(R.id.v_rootNode);

        ViewTreeObserver observer = v_rootnode.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        v_rootnode.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //サンプル用のノードを作成
                        createSampleNode();

                        //ノード描画
                        drawAllNodes();
                    }
                }
        );

        //色配置変更アイコン
        findViewById(R.id.iv_colorSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //色の配置を変更
                changeColorPlacement();
            }
        });
    }

    /*
     * サンプルノードリストを作成
     */
    private void createSampleNode() {
        mTmpNodes = new NodeArrayList<>();

        //ノード生成
        NodeTable nodeR = new NodeTable();
        NodeTable nodeA = new NodeTable();
        NodeTable nodeB = new NodeTable();
        NodeTable nodeC = new NodeTable();
        NodeTable nodeD = new NodeTable();
        NodeTable nodeE = new NodeTable();
        //ノード名（仮）
        nodeR.setNodeName("Root");
        nodeA.setNodeName("A");
        nodeB.setNodeName("B");
        nodeC.setNodeName("C");
        nodeD.setNodeName("D");
        nodeE.setNodeName("E");
        //マップID（仮）
        nodeR.setPidMap(1);
        nodeA.setPidMap(1);
        nodeB.setPidMap(1);
        nodeC.setPidMap(1);
        nodeD.setPidMap(1);
        nodeE.setPidMap(1);
        //ノード種別（仮）
        nodeR.setKind(NodeTable.NODE_KIND_ROOT);
        nodeA.setKind(NodeTable.NODE_KIND_NODE);
        nodeB.setKind(NodeTable.NODE_KIND_NODE);
        nodeC.setKind(NodeTable.NODE_KIND_NODE);
        nodeD.setKind(NodeTable.NODE_KIND_NODE);
        nodeE.setKind(NodeTable.NODE_KIND_NODE);
        //位置
        //※中心に対するoffsetを指定
        nodeA.setPos(100, -300);
        nodeB.setPos(-400, -300);
        nodeC.setPos(100, 100);
        nodeD.setPos(-400, 100);
        nodeE.setPos(10, -300);
        //PID（仮）
        nodeR.setPid(1);
        nodeA.setPid(2);
        nodeB.setPid(3);
        nodeC.setPid(4);
        nodeD.setPid(5);
        nodeE.setPid(6);
        //親ノード
        nodeR.setPidParentNode(NodeTable.NO_PARENT);
        nodeA.setPidParentNode(1);
        nodeB.setPidParentNode(1);
        nodeC.setPidParentNode(1);
        nodeD.setPidParentNode(1);
        nodeE.setPidParentNode(1);

        //リストに追加
        mTmpNodes.add(nodeR);
        mTmpNodes.add(nodeA);
        mTmpNodes.add(nodeB);
        mTmpNodes.add(nodeC);
        mTmpNodes.add(nodeD);
        //mTmpNodes.add(nodeE);
    }

    /*
     * 全ノードの描画
     */
    private void drawAllNodes() {

        //マップレイアウト（ノード追加先）
        FrameLayout fl_map = findViewById(R.id.fl_map);

        //全ノード数ループ
        int nodeNum = mTmpNodes.size();
        for (int i = 0; i < nodeNum; i++) {
            //対象ノード
            NodeTable node = mTmpNodes.get(i);
            //ノードを描画
            drawNode(fl_map, node);
        }
    }

    /*
     * ノード（単体）の描画
     */
    private void drawNode(FrameLayout fl_map, NodeTable node) {

        //ルートノード
        if (node.getKind() == NodeTable.NODE_KIND_ROOT) {
            //元々レイアウト上にあるルートノード名を変更し、中心座標を保持
            RootNodeView rootNodeView = findViewById(R.id.v_rootNode);

            //ビューにノード情報を設定
            rootNodeView.setNode(node);
            //中心座標を設定
            rootNodeView.addOnNodeGlobalLayoutListener();

            //NodeTable側でノードビューを保持
            node.setNodeView(rootNodeView);

            return;
        }

        //ノード生成
        ChildNode nodeView = new NodeView(getContext(), node, null);

        //ノードをマップに追加
        fl_map.addView(nodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //ルートノード
        BaseNode rootNode = findViewById(R.id.v_rootNode);
        int centerX = rootNode.getLeft() + (rootNode.getWidth() / 2);
        int centerY = rootNode.getTop() + (rootNode.getHeight() / 2);

        Log.i("createNode", "centerX=" + centerX + " centerY=" + centerY + " getLeft=" + rootNode.getLeft() + " getTop=" + rootNode.getTop());

        //位置設定
        //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
        int left = centerX + node.getPosX();
        int top = centerY + node.getPosY();

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) nodeView.getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

        Log.i("createNode", "setMargins left=" + left + " top=" + top + " mlp.rightMargin=" + mlp.rightMargin + " mlp.bottomMargin=" + mlp.bottomMargin);
        Log.i("createNode", "getWidth=" + nodeView.getWidth() + " getHeight=" + nodeView.getHeight());

        //レイアウト確定後の処理を設定
        ((ChildNode) nodeView).addOnNodeGlobalLayoutListener(rootNode.getNode());

        //ノードビューを保持
        node.setNodeView(nodeView);
    }

    /*
     * カラーパターンの設定
     */
    public void setColorPattern( String[] colors ) {

        //配置回数初期化
        mColorSwitchCount = 0;
        //カラーパターン
        mColors = colors;

        //色の配置
        changeColorPlacement();
    }

    /*
     * 色の配置変更
     */
    private void changeColorPlacement() {

        if( mColors == null ){
            //カラーパターン未設定なら処理なし
            return;
        }

        //指定indexを取得
        int first  = mColorTree[mColorSwitchCount][0];
        int second = mColorTree[mColorSwitchCount][1];
        int third  = mColorTree[mColorSwitchCount][2];

        //Log.i("配色", "first=" + first + " second=" + second + " third=" + third);

        setColorToMap( first, second, third );

        //カウント更新
        mColorSwitchCount++;
        if( mColorSwitchCount >= COLOR_SWITCH_MAX ){
            mColorSwitchCount = 0;
        }
    }

    /*
     * 色をマップに設定
     */
    private void setColorToMap( int first, int second, int third ) {

        //マップ
        findViewById( R.id.fl_map ).setBackgroundColor( Color.parseColor(mColors[first]) );

        //ノード枠線、ライン、テキスト名
        mTmpNodes.setAllNodeBorderColor( mColors[second] );
        mTmpNodes.setAllNodeLineColor( mColors[second] );
        mTmpNodes.setAllNodeTxColor( mColors[second] );

        //ノード背景色
        mTmpNodes.setAllNodeBgColor( mColors[third] );
        mTmpNodes.setAllNodeShadowColor( mColors[third] );
    }

}
