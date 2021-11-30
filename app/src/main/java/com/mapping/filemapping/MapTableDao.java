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

    @Query("SELECT * FROM mapTable")
    List<MapTable> getAll();

    /*
     * 取得：レコード
     *   指定されたプライマリーキーのレコードを取得
     */
    @Query("SELECT * FROM mapTable WHERE pid=(:pid)")
    MapTable getMap(int pid);

    @Insert
    void insert(MapTable mapTable);

    @Delete
    void delete(MapTable mapTable);

}
