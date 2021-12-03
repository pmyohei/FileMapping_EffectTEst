package com.mapping.filemapping;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/*
 * DAO定義：マップテーブル
 *   DB操作の仲介役
 */
@Dao
public interface MapTableDao {

    @Query("SELECT * FROM map")
    List<MapTable> getAll();

    /*
     * 取得：レコード
     *   指定プライマリーキーのレコードを取得
     */
    @Query("SELECT * FROM map WHERE pid=(:pid)")
    MapTable getMap(int pid);

    /*
     * 取得：レコード
     *   指定マップ名のレコードを取得
     */
    @Query("SELECT * FROM map WHERE map_name=(:mapName)")
    MapTable getMap(String mapName);

    @Insert
    long insert(MapTable mapTable);

    @Delete
    void delete(MapTable mapTable);

    @Query("DELETE FROM map")
    void deleteAll();


}
