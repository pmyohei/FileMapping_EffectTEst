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

    /*
     * 取得：指定ピクチャノードに所属するピクチャを取得
     */
    @Query("SELECT * FROM picture WHERE pid_map=(:mapPid) AND pid_parent_node=(:parentPid)")
    List<PictureTable> getGallery(int mapPid, int parentPid);


    /*
     * 取得：サムネイル写真
     *   指定マップに所属する写真の内、サムネイルとして登録された写真を取得
     */
    @Query("SELECT * FROM picture WHERE pid_map=(:mapPid) AND is_thumbnail")
    List<PictureTable> getThumbnailPicture(int mapPid);

    @Insert
    long insert(PictureTable PictureTable);

    @Update
    void update(PictureTable PictureTable);

    @Delete
    void delete(PictureTable PictureTable);

    @Query("DELETE FROM picture")
    void deleteAll();


}
