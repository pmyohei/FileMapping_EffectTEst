package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   マップcreate用
 */
public class AsyncCreateMap {

    private final Context           mContext;
    private final AppDatabase       mDB;
    private final MapTable          mMap;
    private       int               mMapPid;
    private final OnFinishListener  mOnFinishListener;


    /*
     * コンストラクタ
     */
    public AsyncCreateMap(Context context, MapTable map, OnFinishListener listener) {
        mContext          = context;
        mDB               = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mMap              = map;
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
            insertDB();

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
        @SuppressLint("ResourceType")
        private void insertDB(){

            //MapDap
            MapTableDao mapDao = mDB.daoMapTable();
            //NodeDao
            NodeTableDao nodeDao = mDB.daoNodeTable();

            //マップを新規挿入
            mMapPid = (int)mapDao.insert( mMap );

            //ルートノードを生成
            NodeTable rootNode = new NodeTable();
            rootNode.setPidMap( mMapPid );
            rootNode.setNodeName( mMap.getMapName() );
            rootNode.setPidParentNode( NodeTable.NO_PARENT );
            rootNode.setKind( NodeTable.NODE_KIND_ROOT );

            //カラーパターン設定
            String[] colors = mMap.getDefaultColors();
            rootNode.setColorPattern( colors );
            //影の有無を設定
            rootNode.setShadow( mMap.isShadow() );

            //新規挿入
            nodeDao.insert( rootNode );
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
        mOnFinishListener.onFinish(mMapPid);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * ノード生成完了時、コールされる
         */
        void onFinish( int pid );
    }


}
