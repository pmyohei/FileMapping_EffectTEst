package com.mapping.filemapping;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理-共通
 *   処理中ダイアログ
 */
public class AsyncShowProgress {

    private final Context mContext;
    private DialogFragment mProgressDialog;

    /*
     * コンストラクタ
     */
    public AsyncShowProgress(Context context) {
        mContext = context;
    }

    /*
     * バックグラウンド前処理
     */
    void onPreExecute() {

        Log.i("非同期処理 処理中", "ダイアログ表示");

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
        mProgressDialog.setCancelable(false);   //キャンセル不可
        mProgressDialog.show( ((FragmentActivity)mContext).getSupportFragmentManager(), "TEST" );
    }

    /*
     * 実行
     */
/*    void execute() {
        //バックグランド前処理
        onPreExecute();

        //シングルスレッド（キューなし）で動作するexecutorを作成
        ExecutorService executorService  = Executors.newSingleThreadExecutor();

        //非同期処理を送信
        executorService.submit(new AsyncRunnable());
    }*/

    /*
     * バックグランド処理終了後の処理
     */
    void onPostExecute() {
        //処理待ち用のダイアログをクローズ
        if (mProgressDialog != null && mProgressDialog.getShowsDialog()) {
            mProgressDialog.dismiss();
        }

        Log.i("非同期処理 処理中", "ダイアログクローズ");

        //画面向き固定化解除
        ((FragmentActivity)mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
