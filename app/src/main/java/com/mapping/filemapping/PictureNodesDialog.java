package com.mapping.filemapping;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/*
 * 移動先ピクチャノードを表示するダイアログ
 */
public class PictureNodesDialog extends DialogFragment {

    /*
     * 表示するピクチャノード情報
     *   Bundleに保存できるように、Parcelableを実装
     */
    public static class PictureNodeInfo implements Parcelable {

        private final int pictureNodePid;
        private final PictureTable thumbnail;
        private final String parentNodeName;

        public PictureNodeInfo(int pid, PictureTable thumbnail, String parentNodeName) {
            this.pictureNodePid = pid;
            this.thumbnail = thumbnail;
            this.parentNodeName = parentNodeName;
        }

        /*-- Parcelable --*/
        protected PictureNodeInfo(Parcel in) {
            pictureNodePid = in.readInt();
            thumbnail      = (PictureTable) in.readSerializable();
            parentNodeName = in.readString();
        }

        public static final Creator<PictureNodeInfo> CREATOR = new Creator<PictureNodeInfo>() {
            @Override
            public PictureNodeInfo createFromParcel(Parcel in) {
                return new PictureNodeInfo(in);
            }
            @Override
            public PictureNodeInfo[] newArray(int size) {
                return new PictureNodeInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }
        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(pictureNodePid);
            parcel.writeSerializable(thumbnail);
            parcel.writeString(parentNodeName);
        }
        /*-- Parcelable --*/

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

    //リスナー
    public interface NoticeDialogListener {
        //格納先ピクチャノードタッチリスナー
        void onThumbnailClick(PictureNodesDialog dialog, int i);
    }

    //Bundle保存キー
    private static final String KEY_PICTURE_NODE_INFO = "pictureNodeInfos";

    //リスナー
    NoticeDialogListener mListener;
    //移動先のピクチャノード情報
    private ArrayList<PictureNodeInfo> mPictureNodeInfos;

    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public PictureNodesDialog(){
        //do nothing
    }

    public static PictureNodesDialog newInstance(ArrayList<PictureNodeInfo> info) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_PICTURE_NODE_INFO, info);

        PictureNodesDialog dialog = new PictureNodesDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException( "must implement NoticeDialogListener" );
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        View view = View.inflate(getContext(), R.layout.grid_picture_node, null);
        dialog.setContentView(view);

        //Bundle情報からデータを取得
        mPictureNodeInfos = getArguments().getParcelableArrayList( KEY_PICTURE_NODE_INFO );

        //ピクチャノードのサムネイルを表形式で表示
        GridView gv_thumbnail = dialog.findViewById(R.id.gv_thumbnail);
        gv_thumbnail.setAdapter(new ThumbnailGridAdapter(getActivity(), mPictureNodeInfos));

        //サムネイルクリックリスナー
        gv_thumbnail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mListener.onThumbnailClick(
                        PictureNodesDialog.this,
                        mPictureNodeInfos.get(i).getPictureNodePid() );
            }
        });

        //ダイアログを返す
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        //ダイアログ取得
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        //サイズ設定
        setupDialogSize(dialog);
    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize(Dialog dialog) {

        //縦画面時の横割合
        final float PORTRAIT_RATIO = 0.5f;
        //横画面時の横割合
        final float LANDSCAPE_RATIO = 0.7f;

        //画面向きを取得
        int orientation = getResources().getConfiguration().orientation;
        float ratio = ( (orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_RATIO : LANDSCAPE_RATIO );

        //画面サイズの取得
        int screeenWidth = ResourceManager.getScreenWidth( getContext() );
        int screeenHeight = ResourceManager.getScreenHeight( getContext() );
        //レイアウトパラメータ
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width  = (int)screeenWidth;
        lp.height = (int)(screeenHeight * ratio);
        lp.gravity = Gravity.BOTTOM;

        //サイズ設定
        window.setAttributes(lp);
    }

}