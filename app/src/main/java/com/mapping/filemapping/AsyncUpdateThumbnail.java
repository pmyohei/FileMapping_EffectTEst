package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   ギャラリー写真を取得
 */
public class AsyncUpdateThumbnail {

    private final AppDatabase mDB;
    private final PictureTable mPicture;
    private final OnFinishListener mOnFinishListener;

    //コール元に返すデータ
    private PictureTable mOldThumbnail;
    private PictureTable mNewThumbnail;

    /*
     * コンストラクタ
     */
    public AsyncUpdateThumbnail(Context context, PictureTable picture, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mPicture = picture;

        //返却データ
        mOldThumbnail = null;
        mNewThumbnail = null;
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
            dbOperation();

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
        private void dbOperation() {

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            //ピクチャノードの所属ピクチャをすべて取得
            PictureArrayList<PictureTable> gallery = new PictureArrayList<>();
            gallery.addAll( dao.getGallery(mPicture.getPidMap(), mPicture.getPidParentNode()) );

            //現在設定中のサムネイル
            PictureTable currentThumbnail = gallery.getThumbnail(mPicture.getPidParentNode());

            //現在設定中のサムネイルを更新
            boolean isSamePicture = updateCurrentThumbnail(dao, currentThumbnail);
            if( isSamePicture ) {
                //同じ画像がサムネイルに選択された場合、テーブル処理終了
                return;
            }

            //変更対象が既にテーブルにあるかどうか
            PictureTable picture = gallery.getPicture( mPicture.getPidParentNode(), mPicture.getPath() );
            if( picture == null ){
                //なければ挿入
                long pid = dao.insert( mPicture );
                mPicture.setPid( (int)pid );

                mNewThumbnail = picture;

            } else {
                //あれば、サムネイル化して更新
                picture.setEnableThumbnail( mPicture.getTrimmingInfo() );
                dao.update( picture );

                mNewThumbnail = picture;
            }

        }

        /*
         * 現在設定中のサムネイル情報の更新
         *   戻り値；サムネイルに選択された画像が同じかどうか
         *          true ：同じ
         *          false：違う
         */
        private boolean updateCurrentThumbnail( PictureTableDao dao, PictureTable currentThumbnail ){

            //現在のサムネイルと同じ画像か
            boolean isSamePicture = false;

            //現在設定中のサムネイル
            if (currentThumbnail != null) {

                //変更対象の画像と同じ
                if (currentThumbnail.getPath().equals(mPicture.getPath())) {
                    //トリミング情報を更新
                    currentThumbnail.setTrimmingInfo(mPicture.getTrimmingInfo());

                    //サムネイルに選択された画像が同じ
                    isSamePicture = true;

                } else {
                    //非サムネイルに設定
                    currentThumbnail.setDisableThumbnail();
                }

                //現在サムネイルを更新
                dao.update( currentThumbnail );

                mOldThumbnail = currentThumbnail;
            }

            return isSamePicture;
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
        mOnFinishListener.onFinish( mOldThumbnail, mNewThumbnail);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 生成完了時、コールされる
         */
        void onFinish( PictureTable oldPicture, PictureTable newPicture );
    }


}
