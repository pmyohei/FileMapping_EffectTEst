package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ピクチャテーブルからの写真の削除
 */
public class AsyncDeletePicture {

    private final AppDatabase mDB;
    private final PictureTable mPicture;
    private final OnCreateListener mOnCreateListener;

    /*
     * コンストラクタ
     */
    public AsyncDeletePicture(Context context, PictureTable picture, OnCreateListener listener) {
        mDB               = AppDatabaseManager.getInstance(context);
        mOnCreateListener = listener;
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
            operationDB();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * DB操作
         */
        private void operationDB(){

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();




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
        //挿入完了
        mOnCreateListener.onCreate();
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnCreateListener {
        /*
         * 完了時、コールされる
         */
        void onCreate();
    }


}
