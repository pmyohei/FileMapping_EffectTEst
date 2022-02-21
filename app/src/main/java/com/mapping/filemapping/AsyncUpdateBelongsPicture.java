package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   指定ピクチャノード内に指定した写真があるかどうかを確認
 */
public class AsyncUpdateBelongsPicture {

    private final AppDatabase mDB;
    //移動先のピクチャノード
    private final int mPicutureNodePid;
    //移動対象の写真
    private PictureTable mPicture;
    //移動対象の写真（複数）
    private PictureArrayList<PictureTable> mPictures;
    //リスナー
    private final OnFinishListener mOnFinishListener;
    //単体か複数か
    private final boolean mIsSingle;

    //処理結果：更新写真にサムネイルがあったかどうか
    private boolean mIsThumbnail;


    /*
     * コンストラクタ（単体写真）
     */
    public AsyncUpdateBelongsPicture(Context context, int picutureNodePid, PictureTable picture, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mPicutureNodePid = picutureNodePid;
        mPicture = picture;

        mIsSingle = true;
    }

    /*
     * コンストラクタ（複数写真）
     */
    public AsyncUpdateBelongsPicture(Context context, int picutureNodePid, PictureArrayList<PictureTable> pictures, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mPicutureNodePid = picutureNodePid;
        mPictures = pictures;

        mIsSingle = false;
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
            operationDB();

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
        private void operationDB(){

            if( mIsSingle ){
                //単体写真の更新
                operationDBSingle();
            } else {
                //複数写真の更新
                operationDBMultiple();
            }
        }

        /*
         * 単体写真処理
         */
        private void operationDBSingle(){

            //サムネイルなし
            mIsThumbnail = false;

            //格納先ノードを更新
            mPicture.setPidParentNode( mPicutureNodePid );

            //対象写真がサムネイル写真の場合
            if( mPicture.isThumbnail() ){
                //サムネイル情報を無効化（移動先では別のサムネイルがあるため）
                mPicture.setDisableThumbnail();
                //フラグ更新
                mIsThumbnail = true;
            }

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();
            dao.update( mPicture );
        }

        /*
         * 複数写真処理
         */
        private void operationDBMultiple(){

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            //サムネイルなし
            mIsThumbnail = false;

            for( PictureTable picture: mPictures ){

                //移動先にも同じ写真がある場合、テーブルから削除
                if( dao.hasPictureInPictureNode( mPicutureNodePid, picture.getPath() ) != null){
                    //テーブルから削除
                    dao.delete( picture );

                    continue;
                }

                //格納先ノードを更新
                picture.setPidParentNode( mPicutureNodePid );

                //対象写真がサムネイル写真の場合
                if( picture.isThumbnail() ){
                    //サムネイル情報を無効化（移動先では別のサムネイルがあるため）
                    picture.setDisableThumbnail();
                    //フラグ更新
                    mIsThumbnail = true;
                }

                //更新
                dao.update( picture );
            }
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
        //処理完了
        mOnFinishListener.onFinish(mIsThumbnail);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 処理完了時、コールされる
         */
        void onFinish( boolean isThumbnail );
    }


}
