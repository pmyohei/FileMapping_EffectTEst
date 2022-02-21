package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   read用
 */
public class AsyncUpdateNode {

    private final AppDatabase               mDB;
    private final OnFinishListener          mOnFinishListener;
    private       NodeArrayList<NodeTable>  mNodeList;          //保存対象ノードキュー

    /*
     * コンストラクタ
     */
    public AsyncUpdateNode(Context context, NodeArrayList<NodeTable> nodeQue, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mNodeList = nodeQue;
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
         * DB処理
         */
        private void operationDB(){

            //NodeDao
            NodeTableDao nodeDao = mDB.daoNodeTable();

            //指定ノードリストを更新
            for( NodeTable node: mNodeList ){
                //Log.i("updatePosition", "move Node=" + node.getNodeName() + " posx=" + node.getPosX() + " posy=" + node.getPosY());
                //nodeDao.updateNodePosition(node.getPid(), node.getPosX(), node.getPosY());
                nodeDao.updateNode(node);
            }
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
        mOnFinishListener.onFinish();
    }

    /*
     * データ読み取り完了リスナー
     */
    public interface OnFinishListener {
        void onFinish();
    }


}
