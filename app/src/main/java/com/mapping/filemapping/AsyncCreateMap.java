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
    private       String            mRootNodeName;
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
     * コンストラクタ
     */
    public AsyncCreateMap(Context context, MapTable map, String rootNodeName, OnFinishListener listener) {
        mContext          = context;
        mDB               = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mMap              = map;
        mRootNodeName     = rootNodeName;
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
            rootNode.setNodeName( mContext.getString( R.string.default_rootNode ) );
            rootNode.setPidParentNode( NodeTable.NO_PARENT );
            rootNode.setKind( NodeTable.NODE_KIND_ROOT );

            //カラーパターン設定
            String[] colors = new String[3];
            colors[0] = mMap.getFirstColor();
            colors[1] = mMap.getSecondColor();
            colors[2] = mMap.getThirdColor();

            rootNode.setColorPattern( colors );

/*            String firstColor = mMap.getFirstColor();
            if( firstColor != null ){

                String secondColor = mMap.getSecondColor();
                String thirdColor  = mMap.getThirdColor();

                //デフォルトのカラー設定があれば、2色か3色かで設定対象を決める
                if( thirdColor == null ){
                    //2色

                    //ノード名
                    rootNode.setTextColor( firstColor );
                    //ノード背景、枠、影、ライン
                    rootNode.setNodeColor( secondColor );
                    rootNode.setBorderColor( secondColor );
                    rootNode.setShadowColor( secondColor );
                    rootNode.setLineColor( secondColor );

                } else{
                    //3色

                    //ノード名、枠、ライン
                    rootNode.setTextColor( secondColor );
                    rootNode.setBorderColor( secondColor );
                    rootNode.setLineColor( secondColor );
                    //ノード背景、影
                    rootNode.setNodeColor( secondColor );
                    rootNode.setShadowColor( secondColor );
                }
            }*/

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
