package com.mapping.filemapping;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/*
 * Database定義
 */
@Database(
        entities = {
                MapTable.class,         //マップテーブル
                NodeTable.class,        //ノードテーブル
        },
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    //DAO
    public abstract MapTableDao        daoMapTable();               //マップテーブル
    public abstract NodeTableDao       daoNodeTable();              //ノードテーブル

}
