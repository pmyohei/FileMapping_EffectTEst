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
public interface NodeTableDao {

    @Query("SELECT * FROM nodeTable")
    List<NodeTable> getAll();

    /*
     * 取得：レコード
     *   指定されたプライマリーキーのレコードを取得
     */
    @Query("SELECT * FROM nodeTable WHERE pid=(:pid)")
    NodeTable getNode(int pid);

    @Insert
    void insert(NodeTable nodeTable);

    @Delete
    void delete(NodeTable nodeTable);

}

