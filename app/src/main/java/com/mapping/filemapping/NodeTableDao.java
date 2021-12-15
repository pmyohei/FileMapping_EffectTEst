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
     * 指定マップに所属するノードをすべて取得
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

    @Query("UPDATE node set pos_x=(:posX), pos_y=(:posY) WHERE pid=(:pid)")
    void updateNodePosition(int pid, int posX, int posY);

    @Update
    void updateNodes(NodeTable... nodes);

    @Delete
    void delete(NodeTable node);

    @Delete
    void deleteNodes(NodeTable... nodes);

    @Query("DELETE FROM node")
    void deleteAll();

}

