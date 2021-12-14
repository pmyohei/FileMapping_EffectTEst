package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/*
 * DB非同期処理
 *   read用
 */
public class AsyncReadNodes {

    private final AppDatabase               mDB;
    private final OnReadListener            mOnReadListener;
    private final int                       mMapPid;
    private       NodeArrayList<NodeTable>  mNodeList;

    /*
     * コンストラクタ
     */
    public AsyncReadNodes(Context context, int mapPid, OnReadListener listener) {
        mDB             = AppDatabaseManager.getInstance(context);
        mOnReadListener = listener;
        mMapPid         = mapPid;

        mNodeList       = new NodeArrayList<>();
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

            //NodeDao
            NodeTableDao nodeDao = mDB.daoNodeTable();

            //指定マップに所属するノードを取得
            List<NodeTable> nodeList = nodeDao.getMapNodes( mMapPid );
            mNodeList.addAll( nodeList );
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

        Log.i("AsyncReadNodeOperaion", "onPostExecute=" + mNodeList.size());

        //読み取り完了
        mOnReadListener.onRead( mNodeList );
    }

    /*
     * データ読み取り完了リスナー
     */
    public interface OnReadListener {
        void onRead( NodeArrayList<NodeTable> nodeList );
    }


}
