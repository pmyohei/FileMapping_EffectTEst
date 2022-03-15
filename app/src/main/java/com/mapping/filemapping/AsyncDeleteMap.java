package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   マップdelete用
 */
public class AsyncDeleteMap extends AsyncShowProgress  {

    private final AppDatabase mDB;
    private final MapTable mMap;
    private final OnFinishListener mOnFinishListener;

    /*
     * コンストラクタ
     */
    public AsyncDeleteMap(Context context, MapTable map, OnFinishListener listener) {
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
            doOperation();

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
        private void doOperation(){
            //MapDap
            MapTableDao mapDao = mDB.daoMapTable();
            //指定されたマップを削除
            mapDao.delete( mMap );
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

        //処理完了
        mOnFinishListener.onFinish();
    }

    /*
     * 処理完了リスナー
     */
    public interface OnFinishListener {
        /*
         * ノード生成完了時、コールされる
         */
        void onFinish();
    }
}
