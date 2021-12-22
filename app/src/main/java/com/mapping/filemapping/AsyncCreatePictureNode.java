package com.mapping.filemapping;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ピクチャノードcreate用
 */
public class AsyncCreatePictureNode {

    private final AppDatabase       mDB;
    private final NodeTable         mNode;
    private final PictureTable      mPicture;
    private       int               mNodePid;
    private       int               mPicturePid;
    private final OnFinishListener  mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncCreatePictureNode(Context context, NodeTable node, PictureTable picture, OnFinishListener listener) {
        mDB               = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mNode             = node;
        mPicture          = picture;
    }

    /*
     * 非同期処理
     */
    private class AsyncRunnable implements Runnable {

        Handler handler = new Handler(Looper.getMainLooper());

        /*
         * バックグラウンド処理
         */
        @Override
        public void run() {

            //メイン処理
            dbOperation();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DB処理
         */
        private void dbOperation(){

            //NodeDao
            NodeTableDao nodeDao = mDB.daoNodeTable();
            //PictureDao
            PictureTableDao pictureDao = mDB.daoPictureTable();

            //ピクチャを挿入
            mPicturePid = (int)pictureDao.insert( mPicture );
            //ノードを挿入
            mNodePid = (int)nodeDao.insert( mNode );
        }
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {
        //
    }

    /*
     * 実行
     */
    void execute() {
        //バックグランド前処理
        onPreExecute();

        //シングルスレッド（キューなし）で動作するexecutorを作成
        ExecutorService executorService  = Executors.newSingleThreadExecutor();

        //非同期処理を送信
        executorService.submit(new AsyncRunnable());
    }

    /*
     * バックグランド処理終了後の処理
     */
    void onPostExecute() {

        //読み取り完了
        mOnFinishListener.onFinish(mNodePid, mPicturePid);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * ノード生成完了時、コールされる
         */
        void onFinish( int nodePid, int picturePid );
    }


}
