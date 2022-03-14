package com.mapping.filemapping;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;

/*
 * Database定義
 */
@Database(
        version = 1,
        entities = {
                MapTable.class,         //マップテーブル
                NodeTable.class,        //ノードテーブル
                PictureTable.class,     //ピクチャテーブル
        }
        //exportSchema = false
/*        autoMigrations = {
                @AutoMigration(from = 1, to = 1)
        }*/
)
public abstract class AppDatabase extends RoomDatabase {
    //DAO
    public abstract MapTableDao        daoMapTable();               //マップテーブル
    public abstract NodeTableDao       daoNodeTable();              //ノードテーブル
    public abstract PictureTableDao    daoPictureTable();           //ピクチャテーブル
}
