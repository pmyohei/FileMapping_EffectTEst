package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   read用
 */
public class AsyncReadThumbnail {

    private final AppDatabase                       mDB;
    private final OnFinishListener                  mOnFinishListener;
    private final int                               mMapPid;
    private       PictureArrayList<PictureTable>    mThumbnailList;

    /*
     * コンストラクタ
     */
    public AsyncReadThumbnail(Context context,int mapPid, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mMapPid = mapPid;

        mThumbnailList  = new PictureArrayList<>();
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
            readDB();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DBからデータを取得
         */
        private void readDB(){

            //PictureDao
            PictureTableDao pictureDao = mDB.daoPictureTable();

            //マップ内のサムネイル写真のみを取得
            List<PictureTable> thumbnailPictureList = pictureDao.getThumbnailPicture(mMapPid);
            mThumbnailList.addAll( thumbnailPictureList );
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
        mOnFinishListener.onFinish( mThumbnailList );
    }

    /*
     * データ読み取り完了リスナー
     */
    public interface OnFinishListener {
        void onFinish(PictureArrayList<PictureTable> thumbnailList );
    }


}
