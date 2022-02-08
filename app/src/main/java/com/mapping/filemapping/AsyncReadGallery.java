package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ギャラリー写真を取得
 */
public class AsyncReadGallery {

    private final AppDatabase mDB;
    private final int mMapPid;
    private final List<Integer> mPictureNodePids;
    private final OnFinishListener mOnFinishListener;
    private List<PictureArrayList<PictureTable>> mGalleries;

    /*
     * コンストラクタ
     */
    public AsyncReadGallery(Context context, int mapPid, List<Integer> pictureNodePids, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mMapPid = mapPid;
        mPictureNodePids = pictureNodePids;
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
         * DBからデータを取得
         */
        private void insertDB(){

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            //log
/*            List< PictureTable > tmpLog = new ArrayList<>();
            for( PictureTable aa: dao.getAll() ){
                Log.i("ギャラリー確認 すべてのピクチャ", "絶対パス=" + aa.getPath());
            }*/
            //log

            //各ギャラリーのリスト
            mGalleries = new ArrayList<>();
            //「すべて」タグ用の全写真
            PictureArrayList<PictureTable> allPictures = new PictureArrayList<>();

            //ピクチャノード分ループ
            for( Integer picturePid: mPictureNodePids ){
                Log.i("ギャラリー確認", "DBから取得=" + picturePid);

                //ピクチャノードに格納された写真
                PictureArrayList<PictureTable> galleryInPictureNode = new PictureArrayList<>();
                galleryInPictureNode.addAll( dao.getGallery( mMapPid, picturePid ) );

                //全写真リストに追加
                allPictures.addAll( galleryInPictureNode );

                //ギャラリーリストに写真リストを格納
                mGalleries.add( galleryInPictureNode );
            }

            //ギャラリーリストの先頭に、「すべて」の写真リストを格納
            mGalleries.add(0, allPictures);

/*            Log.i("ギャラリー確認", "DBから取得完了");
            for( PictureTable aa: galleryInPictureNode ){
                Log.i("ギャラリー確認", "DBから取得 パス=" + aa.getPath());
            }*/

            //本リストに追加
            //mGalleries = new PictureArrayList<>();
            //mGalleries.addAll( galleryInPictureNode );
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
        mOnFinishListener.onFinish( mGalleries );
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 生成完了時、コールされる
         */
        void onFinish( List<PictureArrayList<PictureTable>> galleries);
    }


}
