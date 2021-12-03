package com.mapping.filemapping;

import android.util.Log;

import java.util.ArrayList;

/*
 * ArrayList：ノード用
 */
public class NodeArrayList<E> extends ArrayList<NodeTable> {

    /* 定数 */
    public static final int NO_DATA = -1;   //データなし

    /*
     * コンストラクタ
     */
    public NodeArrayList() {
        super();
    }

    /*
     *　ラストIndex取得
     */
    public int getLastIdx() {

        int size = size();

        if (size == 0) {
            return NO_DATA;
        }

        return size - 1;
    }

    /*
     *　ラストIndex取得
     */
    public NodeTable getParentNode( int parentPid ) {

        int size = size();
        for (int i = 0; i < size; i++) {

            if (parentPid == get(i).getPid()) {
                //指定親ノードのPIDと一致するノードを返す
                return get(i);
            }
        }

        //ここには来ないはず
        Log.e("NodeArrayList", "error getparentNode() parentPid" + parentPid);
        return null;
    }


}
