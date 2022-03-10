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
    private final OnFinishListener mOnFinishListener;
    //削除対象の写真（単体）
    private PictureTable mPicture;
    //削除対象の写真（複数）
    private PictureArrayList<PictureTable> mPictures;
    //単体か複数か
    private final boolean mIsSingle;

    //処理結果：更新写真にサムネイルがあったかどうか
    private boolean mIsThumbnail;
    //削除された写真を保持していたピクチャノードのpid
    private int mSrcPictureNodePid;

    /*
     * コンストラクタ（単体）
     */
    public AsyncDeletePicture(Context context, PictureTable picture, OnFinishListener listener) {
        mDB               = AppDatabaseManager.getInstance(context);
        mPicture          = picture;
        mOnFinishListener = listener;

        mIsSingle = true;
    }

    /*
     * コンストラクタ（複数）
     */
    public AsyncDeletePicture(Context context, PictureArrayList<PictureTable> pictures, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mPictures = pictures;
        mOnFinishListener = listener;

        mIsSingle = false;
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

            if( mIsSingle ){
                //単体写真の更新
                operationDBSingle();
            } else {
                //複数写真の更新
                operationDBMultiple();
            }
        }

        /*
         * 単体写真処理
         */
        private void operationDBSingle(){

            //サムネイルかどうか
            mIsThumbnail = mPicture.isThumbnail();
            //ピクチャノード
            mSrcPictureNodePid = mPicture.getPidParentNode();

            //テーブルから削除
            PictureTableDao dao = mDB.daoPictureTable();
            dao.delete(mPicture);
        }

        /*
         * 複数写真処理
         */
        private void operationDBMultiple(){

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            mIsThumbnail = false;

            //テーブルから削除
            for( PictureTable picture: mPictures ){

                //サムネイルならフラグを更新
                if( picture.isThumbnail() ){
                    mIsThumbnail = true;
                    //ピクチャノード
                    mSrcPictureNodePid = picture.getPidParentNode();
                }
                dao.delete(picture);
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
        //完了
        mOnFinishListener.onFinish(mIsThumbnail, mSrcPictureNodePid);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 完了時、コールされる
         */
        void onFinish(boolean isThumbnail, int srcPictureNodePid);
    }


}
