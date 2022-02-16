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
public class AsyncDecordBitmap {

    ExecutorService mExecutorService;

    public AsyncDecordBitmap(){
        //
        mExecutorService  = Executors.newFixedThreadPool(12);
    }

    /*
     * 実行
     */
    void execute( Runnable runnable  ) {
        //シングルスレッド（キューなし）で動作するexecutorを作成
        //ExecutorService executorService  = Executors.newFixedThreadPool(4);
        //非同期処理を送信
        //executorService.submit( runnable );

        mExecutorService.submit( runnable );
    }

}
