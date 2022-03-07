package com.mapping.filemapping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/*
 * 移動先ピクチャノードを表示するbottomsheetダイアログ
 */
public class PictureNodesBottomSheetDialog extends BottomSheetDialogFragment {

    /*
     * 表示するピクチャノード情報
     */
    public static class PictureNodeInfo {

        private final int pictureNodePid;
        private final PictureTable thumbnail;
        private final String parentNodeName;

        public PictureNodeInfo(int pid, PictureTable thumbnail, String parentNodeName) {
            this.pictureNodePid = pid;
            this.thumbnail = thumbnail;
            this.parentNodeName = parentNodeName;
        }

        public int getPictureNodePid() {
            return pictureNodePid;
        }

        public PictureTable getThumbnail() {
            return thumbnail;
        }

        public String getParentNodeName() {
            return parentNodeName;
        }
    }

    /*------- 変数 -------*/
    //表示元のアクティビティ
    private Activity mActivity;
    //移動先のピクチャノード情報
    private ArrayList<PictureNodeInfo> mPictureNodeInfos;

    //-- 単体写真
    //参照中の写真（単体）
    private PictureTable mShowPicture;

    //-- 複数選択
    //移動元のピクチャノードのpid
    private int mTabPictureNodePid;
    //選択された写真リスト
    private PictureArrayList<PictureTable> mSelectedPictures;

    //単体・複数判別用
    private boolean mIsSingle;


    /*
     * 単体写真用
     */
    public PictureNodesBottomSheetDialog( Activity activity, ArrayList<PictureNodeInfo> info, PictureTable showPicture) {
        mActivity = activity;
        mPictureNodeInfos = info;
        mShowPicture = showPicture;

        mIsSingle = true;
    }

    /*
     * 複数選択写真用
     */
    public PictureNodesBottomSheetDialog( Activity activity, ArrayList<PictureNodeInfo> info, int tabPictureNodePid, PictureArrayList<PictureTable> selectedPictures) {
        mActivity = activity;
        mPictureNodeInfos = info;
        mTabPictureNodePid = tabPictureNodePid;
        mSelectedPictures = selectedPictures;

        mIsSingle = false;
    }

    /*
     * インスタンス生成（写真単体選択時）
     */
/*    public static PictureNodesBottomSheetDialog newInstance(ArrayList<PictureNodeInfo> info, PictureTable showPicture) {
        //return new PictureNodesBottomSheetDialog(info, showPicture);
    }*/

    /*
     * インスタンス生成（写真複数選択時）
     */
/*    public static PictureNodesBottomSheetDialog newInstance(ArrayList<PictureNodeInfo> info, int tabPictureNodePid, PictureArrayList<PictureTable> selectedPictures) {
        //return new PictureNodesBottomSheetDialog(info, tabPictureNodePid, selectedPictures);
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view = View.inflate(getContext(), R.layout.grid_picture_node, null);
        dialog.setContentView(view);

        //ピクチャノードのサムネイルを表形式で表示
        GridView gv_thumbnail = dialog.findViewById(R.id.gv_thumbnail);
        gv_thumbnail.setAdapter(new ThumbnailGridAdapter(getActivity(), mPictureNodeInfos, mShowPicture));

        //サムネイルクリックリスナー
        gv_thumbnail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //移動先判定
                checkHasNode(i);
            }
        });

        //ダイアログを返す
        return dialog;
    }

    /*
     * 移動先判定
     */
    private void checkHasNode(int i) {

        if (mIsSingle) {
            //単体写真が移動先にあるかどうか
            checkHasNodeSinglePicture(mPictureNodeInfos.get(i).getPictureNodePid());
        } else {
            //移動元のノードと同じノードが選択されたかどうか
            checkHasNodeMultiplePicture(mPictureNodeInfos.get(i).getPictureNodePid());
        }
    }

    /*
     * 格納先ノード判定（単体写真）
     *   para1：移動先として選択されたピクチャノードpid
     */
    private void checkHasNodeSinglePicture(int toPicutureNodePid) {

        //確認する写真パス
        String path = mShowPicture.getPath();
        //ピクチャテーブルを参照し、写真があるかどうかチェック
        AsyncHasPicture db = new AsyncHasPicture(getActivity(), toPicutureNodePid, path, new AsyncHasPicture.OnFinishListener() {
            @Override
            public void onFinish(boolean hasPicture) {

                if (hasPicture) {
                    //既に写真があれば、トースト表示して終了
                    Toast.makeText(getActivity(), getString(R.string.toast_samePicture), Toast.LENGTH_SHORT).show();
                    return;
                }

                //なければ移動確認のダイアログを表示
                confirmDialog(toPicutureNodePid);
            }
        });

        db.execute();
    }

    /*
     * 格納先ノード判定（複数写真）
     *   para1：移動先として選択されたピクチャノードpid
     *   para2：移動元のピクチャノードpid
     */
    private void checkHasNodeMultiplePicture(int toPicutureNodePid) {

        if (toPicutureNodePid == mTabPictureNodePid) {
            //移動先に選択されたノードが、移動元ノードと同じであれば、トーストを表示して終了
            Toast.makeText(getActivity(), getString(R.string.toast_samePicture), Toast.LENGTH_SHORT).show();
            return;
        }

        //なければ移動確認のダイアログを表示
        confirmDialog(toPicutureNodePid);
    }

    /*
     * 移動確認用ダイアログ
     */
    private void confirmDialog(int toPicutureNodePid) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle( getActivity().getString(R.string.alert_movePicture_title) )
                .setMessage( getActivity().getString(R.string.alert_movePicture_message) )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if( mIsSingle ){
                            moveSinglePicture(toPicutureNodePid);
                        } else {
                            moveMultiplePicture(toPicutureNodePid);
                        }

                        //閉じる
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView)dialog.findViewById(android.R.id.message)).setTypeface( Typeface.SERIF );
    }

    /*
     * ノード移動処理（単体写真）
     */
    private void moveSinglePicture( int toPicutureNodePid ) {

        //ピクチャテーブルの所属を更新
        AsyncUpdateBelongsPicture db = new AsyncUpdateBelongsPicture(getActivity(), toPicutureNodePid, mShowPicture,
                new AsyncUpdateBelongsPicture.OnFinishListener() {
                    @Override
                    public void onFinish( boolean isThumbnail ) {

                        //移動した写真を単体表示中のアダプタから削除
                        ((SinglePictureDisplayActivity) mActivity).updatePictureAdapter();
                    }
                });

        //非同期処理開始
        db.execute();
    }

    /*
     * ノード移動処理（複数写真）
     */
    private void moveMultiplePicture( int toPicutureNodePid ) {

        //ピクチャテーブルの所属を更新
        AsyncUpdateBelongsPicture db = new AsyncUpdateBelongsPicture(getActivity(), toPicutureNodePid, mSelectedPictures,
                new AsyncUpdateBelongsPicture.OnFinishListener() {
                    @Override
                    public void onFinish(boolean isThumbnail) {

                        //移動した写真を表示中のタブから削除
                        ((PictureGalleryActivity) mActivity).updateGallery( mSelectedPictures, toPicutureNodePid, isThumbnail );
                    }
                });
        //非同期処理開始
        db.execute();
    }
}