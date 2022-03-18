package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class SampleMapView extends FrameLayout {

    private final int COLOR2_SWITCH_MAX = 2;
    private final int COLOR3_SWITCH_MAX = 6;

    //サンプルマップ用の暫定ノードリスト
    private NodeArrayList<NodeTable> mTmpNodes;
    //マップ名
    private String mMapName;
    //選択中のカラーパターン
    private String[] mSelectedColors;
    //設定中のカラーパターン
    private String[] mCurrentColors = {null, null, null};
    //カラー配置変更回数
    private int mColorSwitchCount = 0;

    //カラー配置Index（2色）
    private final int[][] m2ColorTree = {
            {0, 1},
            {1, 0},
    };
    //カラー配置Index（3色）
    private final int[][] m3ColorTree = {
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

        init();
    }

    /*
     * 初期化
     */
    private void init() {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.sample_map, this, true);

        //マップ名初期化
        mMapName = "";

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
                        //影なしに設定
                        setDisableShadowInMap();
                        //初期の配色パターンを設定
                        initColorPattern();
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

        //影の有無変更アイコン
        findViewById(R.id.iv_shadowSwitch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //影の有無を切り替え
                switchSampleNodeShadow();
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
        //ノード名（仮）
        nodeR.setNodeName("Sample");
        nodeA.setNodeName("A");
        //マップID（仮）
        nodeR.setPidMap(1);
        nodeA.setPidMap(1);
        //ノード種別（仮）
        nodeR.setKind(NodeTable.NODE_KIND_ROOT);
        nodeA.setKind(NodeTable.NODE_KIND_NODE);
        //位置
        //※中心に対するoffsetを指定
        float density = getResources().getDisplayMetrics().density;
        float sample_node_posx = getResources().getDimension(R.dimen.sample_node_posx) / density;
        float sample_node_posy = getResources().getDimension(R.dimen.sample_node_posy) / density;
        nodeA.setPos( (int)sample_node_posx, (int)-sample_node_posy);
        //PID（仮）
        nodeR.setPid(1);
        nodeA.setPid(2);
        //親ノード
        nodeR.setPidParentNode(NodeTable.NO_PARENT);
        nodeA.setPidParentNode(1);

        //リストに追加
        mTmpNodes.add(nodeR);
        mTmpNodes.add(nodeA);
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
            rootNodeView.addLayoutConfirmedListener();

            //NodeTable側でノードビューを保持
            node.setNodeView(rootNodeView);

            return;
        }

        //ルートノード
        BaseNode rootNode = findViewById(R.id.v_rootNode);

        //ノード生成
        ChildNode nodeView = new NodeView(getContext(), node);
        nodeView.setSampleRootNode( rootNode.getNode() );   //サンプルマップの場合、親ノードをフィールドに持たせる

        //ノードをマップに追加
        fl_map.addView(nodeView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //位置設定
        //※レイアウト追加後に行うこと（MarginLayoutParamsがnullになってしまうため）
        int centerX = rootNode.getLeft() + (rootNode.getWidth() / 2);
        int centerY = rootNode.getTop() + (rootNode.getHeight() / 2);
        int left = centerX + node.getPosX();
        int top  = centerY + node.getPosY();

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) nodeView.getLayoutParams();
        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

        //レイアウト確定後の処理を設定
        nodeView.addLayoutConfirmedListener();
        //ノードビューを保持
        node.setNodeView(nodeView);
        //タッチリスナーを削除（サンプルマップでは不要のため）
        nodeView.removeTouchListener();
    }


    /*
     * 全ノードの影をなしに設定
     */
    public void setDisableShadowInMap() {
        mTmpNodes.setAllNodeShadow(false);
    }

    /*
     * カラーパターン初期設定
     */
    public void initColorPattern() {

        BaseNode rootNode = findViewById(R.id.v_rootNode);
        String[] colors = {
                rootNode.getNodeTextColor(),
                rootNode.getNodeBackgroundColor(),
        };

        //配置回数初期化
        //※初期状態では既に設定されているため、カウント１からスタート
        mColorSwitchCount = 1;
        //選択中カラーパターン（初期色）
        mSelectedColors = colors;

        //現在の色を保持
        mCurrentColors[0] = mSelectedColors[0];
        mCurrentColors[1] = mSelectedColors[1];
    }

    /*
     * カラーパターンの設定
     */
    public void setColorPattern(String[] colors) {

        //配置回数初期化
        mColorSwitchCount = 0;
        //カラーパターン
        mSelectedColors = colors;

        //色の配置
        changeColorPlacement();
    }

    /*
     * 色の配置変更
     */
    private void changeColorPlacement() {

        //切り替え最大値
        int switchMaxNum = 0;

        if (mSelectedColors.length == 2) {
            //指定indexを取得
            int first = m2ColorTree[mColorSwitchCount][0];
            int second = m2ColorTree[mColorSwitchCount][1];

            //Log.i("配色", "first=" + first + " second=" + second + " third=" + third);

            //2色の配色でマップを色付け
            set2ColorToMap(first, second);

            switchMaxNum = COLOR2_SWITCH_MAX;

        } else if (mSelectedColors.length == 3) {
            //指定indexを取得
            int first = m3ColorTree[mColorSwitchCount][0];
            int second = m3ColorTree[mColorSwitchCount][1];
            int third = m3ColorTree[mColorSwitchCount][2];

            //Log.i("配色", "first=" + first + " second=" + second + " third=" + third);

            //3色の配色でマップを色付け
            set3ColorToMap(first, second, third);

            switchMaxNum = COLOR3_SWITCH_MAX;
        }

        //カウント更新
        mColorSwitchCount++;
        if (mColorSwitchCount >= switchMaxNum) {
            mColorSwitchCount = 0;
        }
    }

    /*
     * 色をマップに設定（2色）
     */
    private void set2ColorToMap(int first, int second) {

        //マップ、テキスト名
        findViewById(R.id.fl_map).setBackgroundColor(Color.parseColor(mSelectedColors[first]));
        mTmpNodes.setAllNodeTxColor(mSelectedColors[first]);

        //ノード枠線、ライン、ノード背景色、影色
        mTmpNodes.setAllNodeBorderColor(mSelectedColors[second]);
        mTmpNodes.setAllNodeLineColor(mSelectedColors[second]);
        mTmpNodes.setAllNodeBgColor(mSelectedColors[second]);
        mTmpNodes.setAllNodeShadowColor(mSelectedColors[second]);

        //現在の色を保持
        mCurrentColors[0] = mSelectedColors[first];
        mCurrentColors[1] = mSelectedColors[second];
        mCurrentColors[2] = null;
    }

    /*
     * 色をマップに設定（3色）
     */
    private void set3ColorToMap(int first, int second, int third) {

        //マップ
        findViewById(R.id.fl_map).setBackgroundColor(Color.parseColor(mSelectedColors[first]));

        //ノード枠線、ライン、テキスト名
        mTmpNodes.setAllNodeBorderColor(mSelectedColors[second]);
        mTmpNodes.setAllNodeLineColor(mSelectedColors[second]);
        mTmpNodes.setAllNodeTxColor(mSelectedColors[second]);

        //ノード背景色、影色
        mTmpNodes.setAllNodeBgColor(mSelectedColors[third]);
        mTmpNodes.setAllNodeShadowColor(mSelectedColors[third]);

        //現在の色を保持
        mCurrentColors[0] = mSelectedColors[first];
        mCurrentColors[1] = mSelectedColors[second];
        mCurrentColors[2] = mSelectedColors[third];
    }

    /*
     * 影の有無の切り替え
     */
    private void switchSampleNodeShadow() {

        //ノード背景色
        RootNodeView rootNodeView = findViewById(R.id.v_rootNode);
        String color = rootNodeView.getNodeBackgroundColor();
        //影色を更新
        mTmpNodes.setAllNodeShadowColor(color);

        //影設定を反転
        mTmpNodes.switchAllNodeShadow();
    }

    /*
     * 現在設定中のカラーパターン取得
     */
    public String[] getCurrentColors() {
        return mCurrentColors;
    }

    /*
     * 現在設定中の影の有無を取得
     */
    public boolean isMapShadow() {
        RootNodeView rootNodeView = findViewById(R.id.v_rootNode);
        return rootNodeView.isShadow();
    }

    public String getMapName() {
        return mMapName;
    }
    public void setMapName(String mapName) {
        this.mMapName = mapName;
    }
}
