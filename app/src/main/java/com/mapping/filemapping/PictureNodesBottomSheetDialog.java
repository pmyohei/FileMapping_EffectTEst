package com.mapping.filemapping;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

/*
 * 移動先ピクチャノードを表示するbottomsheetダイアログ
 */
public class PictureNodesBottomSheetDialog extends BottomSheetDialogFragment {

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
        void onThumbnailClick(BottomSheetDialogFragment dialog, int i);
    }

    //Bundle保存キー
    private static final String KEY_PICTURE_NODE_INFO = "pictureNodeInfos";

    //リスナー
    NoticeDialogListener mListener;
    //移動先のピクチャノード情報
    private ArrayList<PictureNodeInfo> mPictureNodeInfos;

    //空のコンストラクタ
    //※必須（画面回転等の画面再生成時にコールされる）
    public PictureNodesBottomSheetDialog(){
        //do nothing
    }

    public static PictureNodesBottomSheetDialog newInstance(ArrayList<PictureNodeInfo> info) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_PICTURE_NODE_INFO, info);

        PictureNodesBottomSheetDialog dialog = new PictureNodesBottomSheetDialog();
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

        View view = View.inflate(getContext(), R.layout.grid_picture_node, null);
        dialog.setContentView(view);


/*        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                // Right here!
                final BottomSheetBehavior behaviour = BottomSheetBehavior.from(bottomSheet);
                behaviour.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            dialog.dismiss();
                        }

                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            behaviour.setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    }
                });
            }
        });*/

        //Bundle情報の保持
        mPictureNodeInfos = getArguments().getParcelableArrayList( KEY_PICTURE_NODE_INFO );

        //ピクチャノードのサムネイルを表形式で表示
        GridView gv_thumbnail = dialog.findViewById(R.id.gv_thumbnail);
        gv_thumbnail.setAdapter(new ThumbnailGridAdapter(getActivity(), mPictureNodeInfos));

        //サムネイルクリックリスナー
        gv_thumbnail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mListener.onThumbnailClick(
                        PictureNodesBottomSheetDialog.this,
                        mPictureNodeInfos.get(i).getPictureNodePid() );
            }
        });

        //ダイアログを返す
        return dialog;
    }

}