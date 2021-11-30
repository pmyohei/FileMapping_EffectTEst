package com.mapping.filemapping;

import android.os.AsyncTask;

public class AsyncDBOperaion extends AsyncTask<Void, Void, Integer> {

    /*
     * コンストラクタ
     */
    public AsyncDBOperaion() {
    }


    /*
     * メインスレッドとは別のスレッドで実行される。
     * 非同期で行いたい処理を記述
     */
    @Override
    protected Integer doInBackground(Void... params) {

        return 0;
    }

    /*
     * doInBackground()後処理。メインスレッドで実行される。
     */
    @Override
    protected void onPostExecute(Integer code) {

    }

}
