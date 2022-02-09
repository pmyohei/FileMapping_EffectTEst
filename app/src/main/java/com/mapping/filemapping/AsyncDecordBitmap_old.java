package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 非同期処理
 *   Bitmapの生成
 */
public class AsyncDecordBitmap_old {

    private final AppDatabase mDB;
    //private final PictureTable mPicture;
    private final String mFilePath;
    private final OnFinishListener mOnFinishListener;

    //コール元に返すデータ
    private Bitmap mBitmap;

    /*
     * コンストラクタ
     */
    public AsyncDecordBitmap_old(Context context, String path, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        //mPicture = picture;
        mFilePath = path;

        //返却データ
        mBitmap = null;
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
            operation();

            //後処理
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute();
                }
            });
        }

        /*
         * ファイルのBitmapを生成
         */
        private void operation() {
            //トリミング範囲で切り取り
            mBitmap = BitmapFactory.decodeFile( mFilePath );
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
        mOnFinishListener.onFinish( mBitmap);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 生成完了時、コールされる
         */
        void onFinish( Bitmap bitmap );
    }


}
