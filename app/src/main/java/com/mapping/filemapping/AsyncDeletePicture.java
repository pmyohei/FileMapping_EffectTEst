package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ピクチャテーブルからの写真の削除
 */
public class AsyncDeletePicture {

    private final AppDatabase mDB;
    private final PictureTable mPicture;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncDeletePicture(Context context, PictureTable picture, OnFinishListener listener) {
        mDB               = AppDatabaseManager.getInstance(context);
        mPicture          = picture;
        mOnFinishListener = listener;
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

            //テーブルから削除
            dao.delete(mPicture);
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
        //完了
        mOnFinishListener.onFinish();
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 完了時、コールされる
         */
        void onFinish();
    }


}