package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   指定ピクチャノード内に指定した写真があるかどうかを確認
 */
public class AsyncHasPicture {

    private final AppDatabase mDB;
    private final int mPicutureNodePid;
    private final String mPath;
    private final OnFinishListener mOnFinishListener;

    //判定結果
    private boolean mHasPicture;

    /*
     * コンストラクタ
     */
    public AsyncHasPicture(Context context, int picutureNodePid, String path, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mPicutureNodePid = picutureNodePid;
        mPath = path;
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
         * DBからデータを取得
         */
        private void operationDB(){

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            //ピクチャノード内に指定されたパスがあるかをチェック
            PictureTable picture = dao.hasPictureInPictureNode( mPicutureNodePid, mPath );
            //保持結果
            mHasPicture = ( picture != null );
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
        //生成完了
        mOnFinishListener.onFinish( mHasPicture );
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 生成完了時、コールされる
         */
        void onFinish( boolean hasPicture);
    }


}
