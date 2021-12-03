package com.mapping.filemapping;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/*
 * DAO定義：マップテーブル
 *   DB操作の仲介役
 */
@Dao
public interface NodeTableDao {

    @Query("SELECT * FROM node")
    List<NodeTable> getAll();

    /*
     *
     */
    @Query("SELECT * FROM node WHERE pid_map=(:mapPid)")
    List<NodeTable> getMapNodes(int mapPid);

    /*
     * 取得：レコード
     *   指定されたプライマリーキーのレコードを取得
     */
    @Query("SELECT * FROM node WHERE pid=(:pid)")
    NodeTable getNode(int pid);

    @Insert
    long insert(NodeTable node);

    @Update
    void updateNode(NodeTable node);

    @Query("UPDATE node set pid_parent_node=(:pidParent) WHERE pid=(:pid)")
    void updateNodeTest(int pid, int pidParent);

    @Update
    void updateNodes(NodeTable... nodes);

    @Delete
    void delete(NodeTable node);

    @Query("DELETE FROM node")
    void deleteAll();

}

