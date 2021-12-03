package com.mapping.filemapping;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;

/*
 * DB非同期処理
 *   read
 */
public class AsyncReadDBOperaion_old extends AsyncTask<Void, Void, Integer> {

    private final AppDatabase mDB;
    private final OnReadListener mOnReadListener;

    /*
     * コンストラクタ
     */
    public AsyncReadDBOperaion_old(Context context, OnReadListener listener) {
        mDB             = AppDatabaseManager.getInstance(context);
        mOnReadListener = listener;
    }

    /*
     * メインスレッドとは別のスレッドで実行される。
     * 非同期で行いたい処理を記述
     */
    @Override
    protected Integer doInBackground(Void... params) {

        //MapDao
        MapTableDao mapDao = mDB.daoMapTable();

        //マップテーブルからデータを取得






        return 0;
    }

    /*
     * doInBackground()後処理。メインスレッドで実行される。
     */
    @Override
    protected void onPostExecute(Integer code) {
        //読み取り完了
        mOnReadListener.onRead();
    }

    /*
     * データ読み取り完了リスナー
     */
    public interface OnReadListener {

        void onRead();
    }




}
