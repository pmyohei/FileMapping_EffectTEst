package com.mapping.filemapping;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * ArrayList：ノード用
 *   任意のマップに所属する写真を保持する目的で使用する
 */
public class PictureArrayList<E> extends ArrayList<PictureTable> implements Serializable {

    /* 定数 */
    public static final int NO_DATA = -1;   //データなし

    /*
     * コンストラクタ
     */
    public PictureArrayList() {
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
     * 指定URI識別子を持つピクチャテーブルを取得
     */
    public PictureTable getThumbnail(int pid, String uriIdentify ) {

        for( PictureTable picture: this ){
            //指令されたURI識別子を持つテーブルを返す
            if( (pid == picture.getPidParentNode()) && (picture.getUriIdentify().equals( uriIdentify )) ){
                return picture;
            }
        }

        return null;
    }

}
