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
    private final NodeTable mPictureNode;
    private final PictureTable mPicture;
    private final OnFinishListener mOnFinishListener;

    //コール元に返すデータ
    private PictureTable mNewThumbnail;

    /*
     * コンストラクタ
     */
    public AsyncUpdateThumbnail(Context context, NodeTable pictureNode, PictureTable picture, OnFinishListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mOnFinishListener = listener;
        mPictureNode = pictureNode;
        mPicture = picture;

        //返却データ
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

            //NodeTableDao
            NodeTableDao nodeDao = mDB.daoNodeTable();
            //ピクチャノード更新
            nodeDao.updateNode( mPictureNode );

            //PictureTableDao
            PictureTableDao dao = mDB.daoPictureTable();

            //更新対象のピクチャノードに所属する写真をすべて取得
            PictureArrayList<PictureTable> galleryInPictureNode = new PictureArrayList<>();
            galleryInPictureNode.addAll( dao.getGallery(mPicture.getPidMap(), mPicture.getPidParentNode()) );

            //現在設定中のサムネイル
            PictureTable currentThumbnail = galleryInPictureNode.getThumbnail(mPicture.getPidParentNode());

            //現在設定中のサムネイル情報を更新
            boolean isSamePicture = updateCurrentThumbnail(dao, currentThumbnail);
            if( isSamePicture ) {
                //同じ画像がサムネイルに選択された場合、ここで処理終了
                mNewThumbnail = currentThumbnail;
                return;
            }

            //変更対象がピクチャノードに格納されているかどうか
            PictureTable picture = galleryInPictureNode.getPicture( mPicture.getPidParentNode(), mPicture.getPath() );
            if( picture == null ){
                //なければ新規挿入
                long pid = dao.insert( mPicture );
                mPicture.setPid( (int)pid );

                //新しいサムネイルとして保持
                mNewThumbnail = mPicture;

            } else {
                //あれば、サムネイル化して更新
                picture.setEnableThumbnail( mPicture.getTrimmingInfo(), mPicture.getSourceImageWidth(), mPicture.getSourceImageHeight()  );
                dao.update( picture );

                //新しいサムネイルとして保持
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

            //現在設定中のサムネイルがなくなっている場合、何もしない
            if( currentThumbnail == null ){
                return isSamePicture;
            }

            //変更対象の画像と同じ画像が選択された場合
            if (currentThumbnail.getPath().equals( mPicture.getPath()) ) {
                //トリミング情報だけ更新
                currentThumbnail.setTrimmingInfo(mPicture.getTrimmingInfo(), mPicture.getSourceImageWidth(), mPicture.getSourceImageHeight()  );

                //サムネイルに選択された画像が同じ
                isSamePicture = true;

            } else {
                //別の画像が選択された場合

                //現在設定中のサムネイルを非サムネイルに設定
                currentThumbnail.setDisableThumbnail();
            }

            //現在サムネイルを更新
            dao.update( currentThumbnail );

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
        mOnFinishListener.onFinish( mNewThumbnail);
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        /*
         * 生成完了時、コールされる
         */
        void onFinish( PictureTable newPicture );
    }


}
