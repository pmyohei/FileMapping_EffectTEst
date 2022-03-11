package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   マップ更新用
 */
public class AsyncUpdateMap extends AsyncShowProgress {

    private final AppDatabase mDB;
    private final MapTable mMap;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncUpdateMap(Context context, MapTable map, OnFinishListener listener) {
        super(context);

        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mMap = map;
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
         * DBへ保存
         */
        private void operationDB(){
            //MapDap
            MapTableDao mapDao = mDB.daoMapTable();
            //マップを更新
            mapDao.update( mMap );
        }
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {
        super.onPreExecute();
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
        super.onPostExecute();

        //完了
        mOnFinishListener.onFinish();
    }

    /*
     * 完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 完了時、コールされる
         */
        void onFinish();
    }


}
