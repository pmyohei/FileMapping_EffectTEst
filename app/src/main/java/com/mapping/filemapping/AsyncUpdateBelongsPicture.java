package com.mapping.filemapping;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * DB非同期処理
 *   画像が所属するピクチャノードの変更
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
    //処理結果（複数のみ）：移動した写真（移動先に同じ写真のなかった写真）
    private PictureArrayList<PictureTable> mMovedPictures;

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

            //移動なし写真リスト
            mMovedPictures = new PictureArrayList<>();
            //サムネイルなし
            mIsThumbnail = false;

            for( PictureTable picture: mPictures ){

                //移動先にも同じ写真がある場合、何もしない（格納先の変更対象外）
                if( dao.hasPictureInPictureNode( mPicutureNodePid, picture.getPath() ) != null){
                    //Log.i("複数選択改修", "同じ写真ありの判定");
                    continue;
                }

                //格納先ピクチャノードを変更
                picture.setPidParentNode( mPicutureNodePid );

                //対象写真がサムネイル写真の場合
                //★現在、サムネイルかどうかの判定は先で使用していない
/*                if( picture.isThumbnail() ){
                    //サムネイル情報を無効化（移動先では別のサムネイルがあるため）
                    picture.setDisableThumbnail();
                    //フラグ更新
                    mIsThumbnail = true;
                }*/

                //更新
                dao.update( picture );
                //移動写真リストに追加
                mMovedPictures.add( picture );
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
        if( mIsSingle ){
            mOnFinishListener.onFinish(mIsThumbnail);
        }else{
            mOnFinishListener.onFinish(mIsThumbnail, mMovedPictures);
        }
    }

    /*
     * データ作成完了リスナー
     */
    public interface OnFinishListener {
        //単体写真
        void onFinish( boolean isThumbnail );
        //複数写真
        void onFinish( boolean isThumbnail, PictureArrayList<PictureTable> movedPictures );
    }


}
