package com.mapping.filemapping;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PictureNodesBottomSheetDialog extends BottomSheetDialogFragment {

    //移動先のピクチャノード情報
    private ArrayList<ThumbnailGridAdapter.PictureNodeInfo> mPictureNodeInfos;

    public PictureNodesBottomSheetDialog( ArrayList<ThumbnailGridAdapter.PictureNodeInfo> info ){
        mPictureNodeInfos = info;
    }

    public static PictureNodesBottomSheetDialog newInstance( ArrayList<ThumbnailGridAdapter.PictureNodeInfo> info ) {
        return new PictureNodesBottomSheetDialog(info);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view = View.inflate(getContext(), R.layout.grid_picture_node, null);
        dialog.setContentView(view);

        //ピクチャノードのサムネイルを表形式で表示
        GridView gv_thumbnail = dialog.findViewById(R.id.gv_thumbnail);
        gv_thumbnail.setAdapter( new ThumbnailGridAdapter( getContext(), mPictureNodeInfos ) );

        //ダイアログを返す
        return dialog;
    }
}
