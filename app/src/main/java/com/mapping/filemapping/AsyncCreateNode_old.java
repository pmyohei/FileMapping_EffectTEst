package com.mapping.filemapping;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ノードcreate用
 */
public class AsyncCreateNode_old {

    private final Context               mContext;
    private final AppDatabase           mDB;
    private final NodeTable             mNode;
    private       int                   mPid;
    private final OnFinishListener      mOnCreateListener;

    DialogFragment mProgressDialog;

    /*
     * コンストラクタ
     */
    public AsyncCreateNode_old(Context context, NodeTable newNode, OnFinishListener listener) {
        mContext          = context;
        mDB               = AppDatabaseManager.getInstance(context);
        mOnCreateListener = listener;
        mNode             = newNode;
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
         * DB処理
         */
        private void insertDB(){
            //NodeDao
            NodeTableDao nodeDao = mDB.daoNodeTable();

            //ノードを挿入し、レコードに割り当てられたpidを取得
            mPid = (int)nodeDao.insert( mNode );
        }
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {

        //画面の向きを現在の向きで固定化
        //※処理中ダイアログ表示中に向きが変わると落ちるため
        Configuration config = mContext.getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //処理中ダイアログを開く
        mProgressDialog = MyProgressDialog.newInstance();
        mProgressDialog.show( ((FragmentActivity)mContext).getSupportFragmentManager(), "TEST" );
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

        //処理待ち用のダイアログをクローズ
        if (mProgressDialog != null && mProgressDialog.getShowsDialog()) {
            mProgressDialog.dismiss();
        }

        //画面向き固定化解除
        ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        //読み取り完了
        mOnCreateListener.onFinish( mPid );
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
