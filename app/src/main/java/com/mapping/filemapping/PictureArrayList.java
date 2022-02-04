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
     * 指定パスを持つピクチャテーブルを取得
     */
    public PictureTable getPicture(int pictureNodePid, String path ) {

        for( PictureTable picture: this ){
            //指令されたパスを持つテーブルを返す
            if( (pictureNodePid == picture.getPidParentNode()) && (picture.getPath().equals( path )) ){
                return picture;
            }
        }

        return null;
    }

    /*
     * 指定ピクチャノードのサムネイルを取得
     */
    public PictureTable getThumbnail(int pictureNodePid) {

        for( PictureTable picture: this ){
            //指令されたパスを持つテーブルを返す
            if( (pictureNodePid == picture.getPidParentNode()) && picture.isThumbnail()  ){
                return picture;
            }
        }

        return null;
    }

    /*
     * 指定パスを持つピクチャテーブルを取得
     */
    public boolean hasPicture(String path ) {

        for( PictureTable picture: this ){
            //同じ絶対パスがあれば、true
            if( path.equals( picture.getPath() ) ){
                return true;
            }
        }

        return false;
    }

    /*
     * 指定されたピクチャ情報を更新する
     */
    public void updatePicture( PictureTable newPictureDate ) {

        for( PictureTable picture: this ){
            if( newPictureDate.getPid() == picture.getPid() ){
                picture = newPictureDate;
            }
        }
    }

    /*
     * 指定されたピクチャ情報を削除する
     */
    public void deletePicture( PictureTable deletePicture ) {

        int i = 0;
        for( PictureTable picture: this ){
            if( deletePicture.getPid() == picture.getPid() ){
                break;
            }
            i++;
        }

        if( i >= size() ){
            remove(i);
        }
    }
}
