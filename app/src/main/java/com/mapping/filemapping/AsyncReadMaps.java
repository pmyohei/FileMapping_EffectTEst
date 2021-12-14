package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   read用
 */
public class AsyncReadMaps {

    private final AppDatabase         mDB;
    private final OnReadListener      mOnReadListener;
    private       ArrayList<MapTable> mMaps;

    /*
     * コンストラクタ
     */
    public AsyncReadMaps(Context context, OnReadListener listener) {
        mDB             = AppDatabaseManager.getInstance(context);
        mOnReadListener = listener;

        mMaps = new ArrayList<>();
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

            //MapDao
            MapTableDao mapDao = mDB.daoMapTable();

            //指定マップに所属するノードを取得
            List<MapTable> maps = mapDao.getAll();
            mMaps.addAll( maps );
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

        Log.i("AsyncReadNodeOperaion", "onPostExecute=" + mMaps.size());

        //読み取り完了
        mOnReadListener.onRead(mMaps);
    }

    /*
     * データ読み取り完了リスナー
     */
    public interface OnReadListener {
        void onRead( ArrayList<MapTable> mapList );
    }


}
