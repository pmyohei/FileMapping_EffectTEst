package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ギャラリー写真用
 */
public class AsyncCreateGallery {

    private final AppDatabase                       mDB;
    private final PictureArrayList<PictureTable>    mPictures;
    private final OnCreateListener                  mOnCreateListener;

    /*
     * コンストラクタ
     */
    public AsyncCreateGallery(Context context, PictureArrayList<PictureTable> pictures, OnCreateListener listener) {
        mDB               = AppDatabaseManager.getInstance(context);
        mOnCreateListener = listener;
        mPictures         = pictures;
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
         * DBからデータを取得
         */
        private void insertDB(){

            if( mPictures.size() == 0 ){
                return;
            }

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            int mapPid = mPictures.get(0).getPidMap();
            int parentPid = mPictures.get(0).getPidParentNode();

            //ピクチャノードに所属する写真をすべて取得（重複チェックのため）
            List<PictureTable> tmp = dao.getGallery( mapPid, parentPid );
            PictureArrayList<PictureTable> allNodePictures = new PictureArrayList<>();
            allNodePictures.addAll( tmp );

            //ノードを挿入
            for( PictureTable picture: mPictures ){

                if( allNodePictures.hasPicture( picture.getPath() ) ){
                    //同じ写真データが既に格納されていれば、挿入対象外
                    continue;
                }

                dao.insert( picture );
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
