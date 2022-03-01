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

    public boolean getIsShadow() { return isShadow; }
    public void setIsShadow(boolean isShadow) { this.isShadow = isShadow;}

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
