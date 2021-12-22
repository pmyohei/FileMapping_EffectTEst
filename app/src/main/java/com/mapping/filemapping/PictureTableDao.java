package com.mapping.filemapping;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/*
 * DAO定義：ピクチャテーブル
 *   DB操作の仲介役
 */
@Dao
public interface PictureTableDao {

    @Query("SELECT * FROM picture")
    List<PictureTable> getAll();

    /*
     * 取得：レコード
     *   指定プライマリーキーのレコードを取得
     */
    @Query("SELECT * FROM picture WHERE pid=(:pid)")
    PictureTable getPicture(int pid);

    @Insert
    long insert(PictureTable PictureTable);

    @Update
    void update(PictureTable PictureTable);

    @Delete
    void delete(PictureTable PictureTable);

    @Query("DELETE FROM picture")
    void deleteAll();


}
