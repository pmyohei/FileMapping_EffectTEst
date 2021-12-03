package com.mapping.filemapping;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
 * 非同期処理
 */
public class AsyncRunnable implements Runnable {

    Handler handler = new Handler(Looper.getMainLooper());

    /*
     * バックグラウンド処理
     */
    @Override
    public void run() {


        //後処理
        handler.post(new Runnable() {
            @Override
            public void run() {
                //onPostExecute();
            }
        });
    }

    /*
     *
     */
    private void read(){

    }
}
