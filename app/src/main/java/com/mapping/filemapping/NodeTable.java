package com.mapping.filemapping;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

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

    //ノードサイズ

    //ノードの形状

    //色：ノード背景色
    @ColumnInfo(name = "node_color")
    private String nodeColor;

    //色：ノード文字色
    @ColumnInfo(name = "text_color")
    private String textColor;

    //ライン-形状

    //ライン-太さ

    //ライン-色



    /*-- 定数 --*/
    //ノード種別
    public static int NODE_KIND_ROOT    = 0;
    public static int NODE_KIND_NODE    = 1;
    public static int NODE_KIND_PICTURE = 2;

    //親ノードIDなし
    public static int NO_PARENT = -1;

    //ノード形
    public static final int CIRCLE = 0;
    public static final int SQUARE = 1;

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

    /*-- getter setter（非レコードフィールド） --*/

    /*
     * 自身のノード種別に応じて、適切な値を返す
     */
    public float getCenterPosX() {
        return baseNode.getCenterPosX();
/*
        if( kind == NODE_KIND_ROOT ){
            return rootNodeView.getCenterPosX();
        } else {
            return childNodeView.getCenterPosX();
        }
*/
    }
    public float getCenterPosY() {
        return baseNode.getCenterPosY();

/*        if( kind == NODE_KIND_ROOT ){
            return rootNodeView.getCenterPosY();
        } else {
            return childNodeView.getCenterPosY();
        }*/
    }

/*    public float getCenterPosX() {
        return centerPosX;
    }
    public void setCenterPosX(float centerPosX) {
        this.centerPosX = centerPosX;
    }

    public float getCenterPosY() {
        return centerPosY;
    }
    public void setCenterPosY(float centerPosY) {
        this.centerPosY = centerPosY;
    }*/


/*
    public HashMap<String, Float> getCenterPos() {
        return centerPos;
    }
    public void setCenterPos(HashMap<String, Float> centerPos) {
        this.centerPos = centerPos;
    }
*/

/*    public ChildNode getChildNodeView() {
        return childNodeView;
    }
    public void setChildNodeView(ChildNode childNodeView) {
        this.childNodeView = childNodeView;
    }

    public RootNodeView getRootNodeView() {
        return rootNodeView;
    }
    public void setRootNodeView(RootNodeView RootNodeView) {
        this.rootNodeView = RootNodeView;
    }*/

    public BaseNode getNodeView() {
        return this.baseNode;
    }
    public void setNodeView(BaseNode node) {
        this.baseNode = node;
    }

}
