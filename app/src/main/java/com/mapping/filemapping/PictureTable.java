package com.mapping.filemapping;

import android.graphics.RectF;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/*
 * ノード所属ピクチャテーブル
 */
@Entity(tableName = "picture",
        foreignKeys = { @ForeignKey (entity = NodeTable.class, parentColumns = "pid", childColumns  = "pid_parent_node", onDelete = ForeignKey.CASCADE),
                        @ForeignKey (entity = MapTable.class,  parentColumns = "pid", childColumns  = "pid_map",         onDelete = ForeignKey.CASCADE)},
        indices = { @Index (value = {"pid_parent_node", "pid_map"})}
)
public class PictureTable implements Serializable {

    //主キー
    @PrimaryKey(autoGenerate = true)
    private int pid;

    //外部キー：プライマリーID-所属マップ
    @ColumnInfo(name = "pid_map")
    private int pidMap;

    //外部キー：プライマリーID-親ノード
    @ColumnInfo(name = "pid_parent_node")
    private int pidParentNode;

    //Uri-識別子
    @ColumnInfo(name = "uri_identify")
    private String uriIdentify;

    //トリミング開始x座標
    @ColumnInfo(name = "trg_left")
    private int trgLeft = INIT_TRIMMING;

    //トリミング開始y座標
    @ColumnInfo(name = "trg_top")
    private int trgTop = INIT_TRIMMING;

    //トリミング横幅
    @ColumnInfo(name = "trg_width")
    private int trgWidth = INIT_TRIMMING;

    //トリミング縦幅
    @ColumnInfo(name = "trg_height")
    private int trgHeight = INIT_TRIMMING;


    //トリミング情報初期値
    public static final int INIT_TRIMMING = -1;

    /*
     * トリミング情報設定
     */
    public void setTrimmingInfo(RectF rect) {
        this.trgLeft   = (int)rect.left;
        this.trgTop    = (int)rect.top;
        this.trgWidth  = (int)rect.width();
        this.trgHeight = (int)rect.height();
    }
    /*
     * トリミング情報取得
     */
    public RectF getTrimmingInfo() {
        return new RectF(this.trgLeft, this.trgTop, this.trgLeft + this.trgWidth, this.trgTop + this.trgHeight);
    }


    /*--  getter/setter  --*/
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPidMap() { return pidMap; }
    public void setPidMap(int pidMap) { this.pidMap = pidMap; }

    public int getPidParentNode() {
        return pidParentNode;
    }
    public void setPidParentNode(int pidParentNode) {
        this.pidParentNode = pidParentNode;
    }

    public String getUriIdentify() {
        return uriIdentify;
    }
    public void setUriIdentify(String uriIdentify) {
        this.uriIdentify = uriIdentify;
    }

    public int getTrgLeft() {
        return trgLeft;
    }
    public void setTrgLeft(int trgLeft) {
        this.trgLeft = trgLeft;
    }

    public int getTrgTop() {
        return trgTop;
    }
    public void setTrgTop(int trgTop) {
        this.trgTop = trgTop;
    }

    public int getTrgWidth() {
        return trgWidth;
    }
    public void setTrgWidth(int trgWidth) {
        this.trgWidth = trgWidth;
    }

    public int getTrgHeight() {
        return trgHeight;
    }
    public void setTrgHeight(int trgHeight) {
        this.trgHeight = trgHeight;
    }


}
