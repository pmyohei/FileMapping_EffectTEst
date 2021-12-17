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

    //マップ背景色
    @ColumnInfo(name = "color")
    private int color;




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

    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }





}
