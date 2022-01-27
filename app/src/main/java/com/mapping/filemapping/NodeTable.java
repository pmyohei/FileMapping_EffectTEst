package com.mapping.filemapping;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/*
 * ノードテーブル
 *   Serializable：intentによるデータの受け渡しを行うために実装
 */
@Entity(tableName = "node",
        foreignKeys = { @ForeignKey(entity = MapTable.class,     parentColumns = "pid", childColumns  = "pid_map",     onDelete = ForeignKey.CASCADE),},
        indices     = { @Index(value = {"pid_map"})}
                    //@Index(value = {"pid_picture"})}
)
public class NodeTable implements Serializable {

    //主キー
    @PrimaryKey(autoGenerate = true)
    private int pid;

    //外部キー：プライマリーID-所属マップ
    @ColumnInfo(name = "pid_map")
    private int pidMap;

    //プライマリーID-親ノード
    @ColumnInfo(name = "pid_parent_node")
    private int pidParentNode;

    //Uri識別子-ピクチャ（ピクチャノードのみ）
    //※エラーになるため、外部キーとして（Pid）は持たない
    @ColumnInfo(name = "uri_identify")
    private String uriIdentify;

    //ノード種別
    @ColumnInfo(name = "kind")
    private int kind;

    //ノード名
    @ColumnInfo(name = "node_name")
    private String nodeName;

    //x座標
    @ColumnInfo(name = "pos_x")
    private int posX;

    //y座標
    @ColumnInfo(name = "pos_y")
    private int posY;

    //ノードサイズ（デフォルトに対する比率）
    @ColumnInfo(name = "size_ratio")
    private float sizeRatio;

    //ノードの形状
    @ColumnInfo(name = "node_shape")
    private int nodeShape;

    //ノード背景色
    @ColumnInfo(name = "node_color")
    private String nodeColor;

    //ノード文字色
    @ColumnInfo(name = "text_color")
    private String textColor;

    //ノード枠色
    @ColumnInfo(name = "border_color")
    private String borderColor;

    //ノード枠太さ
    @ColumnInfo(name = "border_size")
    private int borderSize;

    //ノード影色
    @ColumnInfo(name = "shadow_color")
    private String shadowColor;

    //ライン-形状


    //ライン-太さ
    @ColumnInfo(name = "line_size")
    private float lineSize;

    //ライン-色
    @ColumnInfo(name = "line_color")
    private String lineColor;

    /*-- 定数 --*/
    //ノード種別
    public static final int NODE_KIND_ROOT = 0;
    public static final int NODE_KIND_NODE = 1;
    public static final int NODE_KIND_PICTURE = 2;

    //親ノードIDなし
    public static final int NO_PARENT = -1;

    //ノードサイズ比率：初期値
    public static final float DEFAULT_SIZE_RATIO = 1f;
    //ノードサイズ幅
    public static final int NODE_MAX_SIZE = 1300;
    public static final int NODE_MIN_SIZE = 100;
    public static final int PICTURE_MAX_SIZE = 600;
    public static final int PICTURE_MIN_SIZE = 100;

    //ノード形
    public static final int CIRCLE = 0;
    public static final int SQUARE = 1;

    //デフォルトカラー
    public static final String DEFAULT_COLOR_WHITE = "#FFFFFF";
    public static final String DEFAULT_COLOR_BLACK = "#000000";
    public static final String DEFAULT_COLOR_GRAY = "#66000000";

    //サイズ
    public static final float DEFAULT_THICK_LINE = 2f;
    public static final int DEFAULT_THICK_BORDER = 3;


    //中心座標の連想配列文字列
/*
    public static String CENTER_POS_X = "X";
    public static String CENTER_POS_Y = "X";
*/


    /*-- 非レコードフィールド --*/
    @Ignore
    private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_TABLE;
    /*
        @Ignore
        private HashMap<String, Float> centerPos = new HashMap<String, Float>();        //ノード中心座標
       @Ignore
        private float centerPosX;                   //ノード中心座標X
        @Ignore
        private float centerPosY;                   //ノード中心座標Y*/
/*    @Ignore
    private ChildNode childNodeView;        //ノードビュー
    @Ignore
    private RootNodeView rootNodeView;          //ルートノードビュー*/
    @Ignore
    private BaseNode baseNode;          //ルートノードビュー


    /*
     * コンストラクタ
     */
    public NodeTable(String nodeName, int mapPid, int parentPid, int kind, int posX, int posY) {
        this();

        this.nodeName = nodeName;
        this.pidMap = mapPid;
        this.pidParentNode = parentPid;
        this.kind = kind;
        this.posX = posX;
        this.posY = posY;
    }

    /*
     * コンストラクタ
     */
    public NodeTable() {
        //形
        this.nodeShape = CIRCLE;
        //色
        this.nodeColor   = DEFAULT_COLOR_WHITE;
        this.shadowColor = DEFAULT_COLOR_GRAY;
        this.textColor   = DEFAULT_COLOR_BLACK;
        this.borderColor = DEFAULT_COLOR_BLACK;
        this.lineColor   = DEFAULT_COLOR_BLACK;
        //太さ
        this.lineSize   = DEFAULT_THICK_LINE;
        this.borderSize = DEFAULT_THICK_BORDER;
        //ノードサイズ
        this.sizeRatio = DEFAULT_SIZE_RATIO;
    }


    /*-- getter setter --*/

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPidMap() {
        return pidMap;
    }

    public void setPidMap(int pidMap) {
        this.pidMap = pidMap;
    }

    public int getPidParentNode() {
        return pidParentNode;
    }

    public void setPidParentNode(int pidParentNode) {
        this.pidParentNode = pidParentNode;
    }

    public int getKind() {
        return kind;
    }
    public void setKind(int kind) {
        this.kind = kind;
    }

    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public float getSizeRatio() {
        return sizeRatio;
    }
    public void setSizeRatio(float sizeRatio) {
        this.sizeRatio = sizeRatio;
    }

    public int getPosX() {
        return posX;
    }
    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }
    public void setPosY(int posY) {
        this.posY = posY;
    }
    public void setPos(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public String getNodeColor() {
        return nodeColor;
    }
    public void setNodeColor(String nodeColor) {
        this.nodeColor = nodeColor;
    }

    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getUriIdentify() {
        return uriIdentify;
    }
    public void setUriIdentify(String uriIdentify) {
        this.uriIdentify = uriIdentify;
    }

    public int getNodeShape() {
        return nodeShape;
    }
    public void setNodeShape(int nodeShape) {
        this.nodeShape = nodeShape;
    }

    public String getBorderColor() {
        return borderColor;
    }
    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public int getBorderSize() {
        return borderSize;
    }
    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public String getShadowColor() {
        return shadowColor;
    }
    public void setShadowColor(String shadowColor) {
        this.shadowColor = shadowColor;
    }

    public float getLineSize() {
        return lineSize;
    }
    public void setLineSize(float lineSize) {
        this.lineSize = lineSize;
    }

    public String getLineColor() {
        return lineColor;
    }
    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    /*-- getter setter（非レコードフィールド） --*/

    /*
     * 自身のノード種別に応じて、適切な値を返す
     */
    public float getCenterPosX() {
        return baseNode.getCenterPosX();
    }

    public float getCenterPosY() {
        return baseNode.getCenterPosY();
    }

    public BaseNode getNodeView() {
        return this.baseNode;
    }

    public void setNodeView(BaseNode node) {
        this.baseNode = node;
    }

    /*
     * 色パターンを設定
     */
    public void setColorPattern(String[] colors) {

        //カラーパターンなし
        if (colors[0] == null) {
            return;
        }

        //カラーパターンあり
        if (colors[2] == null) {
            //2色
            //ノード名
            this.textColor = colors[0];
            //ノード背景、枠、影、ライン
            this.nodeColor   = colors[1];
            this.borderColor = colors[1];
            this.shadowColor = colors[1];
            this.lineColor   = colors[1];
        } else {
            //3色
            //ノード名、枠、ライン
            this.textColor   = colors[1];
            this.borderColor = colors[1];
            this.lineColor   = colors[1];
            //ノード背景、影
            this.nodeColor   = colors[2];
            this.shadowColor = colors[2];
        }

    }

    /*
     * 設定中の色を返す（重複なし）
     */
    public ArrayList<String> getSettingColors(){

        ArrayList<String> colors = new ArrayList<>();

        //各色
        colors.add( borderColor );
        colors.add( lineColor );
        colors.add( shadowColor );

        //ピクチャノード以外は、以下の色も対象
        if( kind != NODE_KIND_PICTURE ){
            colors.add( nodeColor );
            colors.add( textColor );
        }

        //重複なしにして返す
        return new ArrayList<>(new LinkedHashSet<>(colors));
    }

}
