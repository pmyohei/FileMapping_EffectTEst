package com.mapping.filemapping;

import android.content.Context;
import android.os.AsyncTask;

/*
 * DB非同期処理
 *   create
 */
public class AsyncCreateDBOperaion extends AsyncTask<Void, Void, Integer> {

    private final AppDatabase mDB;

    /*
     * コンストラクタ
     */
    public AsyncCreateDBOperaion(Context context) {
        mDB = AppDatabaseManager.getInstance(context);
    }


    /*
     * メインスレッドとは別のスレッドで実行される。
     * 非同期で行いたい処理を記述
     */
    @Override
    protected Integer doInBackground(Void... params) {

        //生成





        return 0;
    }

    /*
     * doInBackground()後処理。メインスレッドで実行される。
     */
    @Override
    protected void onPostExecute(Integer code) {

    }

}
