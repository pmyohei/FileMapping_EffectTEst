package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 非同期処理
 *   Bitmapの生成
 */
public class RunnableDecordBitmap implements Runnable {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private  RunnableDecordBitmap.OnFinishListener mOnFinishListener;

    //コール元に返すデータ
    private Bitmap mBitmap;
    private final String mFilePath;
    private  ImageView mImageView;

    /*
     * コンストラクタ
     */
    public RunnableDecordBitmap( String path, RunnableDecordBitmap.OnFinishListener listener){
        mFilePath = path;
        mOnFinishListener = listener;
    }

    /*
     * コンストラクタ
     */
    public RunnableDecordBitmap( String path, ImageView view){
        mFilePath = path;
        mImageView = view;
    }

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
        mImageView.setImageBitmap( mBitmap );

/*        //表示されない
        Picasso.get()
           //.load( new File( mFilePath ) )
           .load( R.drawable.ic_no_image_gallery )
           //.load( "file://" + mFilePath )
           .error(R.drawable.ic_no_image_gallery)
           .into( mImageView );*/
    }

    /*
     * バックグランド処理終了後の処理
     */
    void onPostExecute() {
        //生成完了
        //mImageView.setImageBitmap( mBitmap );
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
