package com.mapping.filemapping;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

/*
 * マップテーブル
 */
@Entity(tableName = "map")
public class MapTable implements Serializable {

    //主キー
    @PrimaryKey(autoGenerate = true)
    private int pid;

    //マップ名
    @ColumnInfo(name = "map_name")
    private String mapName;

    //メモ
    @ColumnInfo(name = "memo")
    private String memo;

    //作成日
    @ColumnInfo(name = "created_date")
    private String createdDate;

    //更新日
    @ColumnInfo(name = "update_date")
    private String updateDate;

    //デフォルトカラー１
    @ColumnInfo(name = "first_color")
    private String firstColor;

    //デフォルトカラー２
    @ColumnInfo(name = "second_color")
    private String secondColor;

    //デフォルトカラー３
    @ColumnInfo(name = "third_color")
    private String thirdColor;

    //マップカラー
    @ColumnInfo(name = "map_color")
    private String mapColor;

    //デフォルト影onoff
    @ColumnInfo(name = "is_shadow")
    private boolean isShadow;


    //------------------------------------
    // エフェクト形状
    //------------------------------------
    //ハート
    static public final int HEART_NORMAL = 0x00;
    static public final int HEART_THIN = 0x01;
    static public final int HEART_INFLATED = 0x02;
    //尖鋭
    static public final int TRIANGLE = 0x10;
    static public final int DIA = 0x11;
    //星
    static public final int STAR = 0x20;
    //スパークル
    static public final int SPARKLE_SHORT = 0x30;
    static public final int SPARKLE_SHIN = 0x31;
    static public final int SPARKLE_LONG = 0x32;
    static public final int SPARKLE_RANDOM = 0x33;
    static public final int SPARCLE_CENTRAL_CIRCLE = 0x34;
    //花
    static public final int FLOWER = 0x40;
    static public final int SAKURA = 0x41;
    //円
    static public final int CIRCLE = 0x50;

    public static enum EffectShape {
        FILL(0),
        STROKE(1),
        FILL_AND_STROKE(2);

        // フィールドの定義
        private int id;

        EffectShape(int id) {
            this.id = id;
        }
    }

    //エフェクトアニメーション
    static public final int BLINK = 0;                      //明滅
    static public final int SPIN = 1;                       //回転
    static public final int SLOW_MOVE = 2;                  //ゆっくり移動
    static public final int SLOW_FLOAT = 3;                 //ゆっくり浮き上がる
    static public final int STROKE_GRADATION_ROTATE = 4;    //枠線のグラデーションの回転

    /*---  getter/setter  ---*/
    public int getPid() {
        return pid;
    }
    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getMapName() {
        return mapName;
    }
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getCreatedDate() {
        return createdDate;
    }
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdateDate() { return updateDate; }
    public void setUpdateDate(String updateDate) { this.updateDate = updateDate; }

    public String getFirstColor() {
        return firstColor;
    }
    public void setFirstColor(String firstColor) {
        this.firstColor = firstColor;
    }

    public String getSecondColor() {
        return secondColor;
    }
    public void setSecondColor(String secondColor) {
        this.secondColor = secondColor;
    }

    public String getThirdColor() {
        return thirdColor;
    }
    public void setThirdColor(String thirdColor) {
        this.thirdColor = thirdColor;
    }

    public String getMapColor() { return mapColor; }
    public void setMapColor(String mapColor) { this.mapColor = mapColor; }

    public boolean isShadow() { return isShadow; }
    public void setShadow(boolean isShadow) { this.isShadow = isShadow;}

    /*
     * デフォルトカラーを取得
     */
    public String[] getDefaultColors() {
        String[] colors = new String[3];
        colors[0] = firstColor;
        colors[1] = secondColor;
        colors[2] = thirdColor;

        return colors;
    }


}
