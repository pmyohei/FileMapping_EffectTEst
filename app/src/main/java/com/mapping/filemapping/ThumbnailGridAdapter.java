package com.mapping.filemapping;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/*
 * サムネイルをグリッド表示するアダプタ
 */
public class ThumbnailGridAdapter extends BaseAdapter {

    //写真数
    public final static int PORTRAIT_NUM = 2;
    public final static int LANDSCAPE_NUM = 6;

    //参照中の写真
    private PictureTable mShowPicture;
    //マップ上のピクチャノード情報
    private final ArrayList<PictureNodesBottomSheetDialog.PictureNodeInfo> mData;
    private final float mDp;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mPictureNumOnLine;            //1行で表示する写真の数


    /*
     * コンストラクタ
     */
    public ThumbnailGridAdapter(Context context, ArrayList<PictureNodesBottomSheetDialog.PictureNodeInfo> data, PictureTable showPicture){
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mShowPicture = showPicture;

        //画面密度
        mDp = context.getResources().getDisplayMetrics().density;

        //1行の写真表示数を設定
        setPictureNumOnLine();
    }

    /*
     * 表示写真の1辺の長さを設定
     */
    public void setPictureNumOnLine() {

        //画面向きを取得
        int orientation = mContext.getResources().getConfiguration().orientation;

        //向きに応じて、1行で表示する写真数を設定
        mPictureNumOnLine = ( (orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_NUM : LANDSCAPE_NUM );
    }

    @Override
    public int getCount() {
        //写真数
        return mData.size();
    }

    /*
     * セル一つ一つを描画する際にコールされる。
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //初めて表示されるなら、セルを割り当て。セルはレイアウトファイルを使用。
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_picture_node_info, null);

            //写真用ビューのサイズ
/*            int sideLength = (parent.getWidth() / mPictureNumOnLine) - (int)mDp;
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    sideLength,
                    sideLength);
            convertView.setLayoutParams(params);*/

            //float dp = mContext.getResources().getDisplayMetrics().density;
            //Log.i("ギャラリー", "position=" + position + " 前回設定サイズ=" + (parent.getWidth() / 2 - (int)dp) );
            //Log.i("ギャラリー", "position=" + position + " mPictureNumOnLine=" + mPictureNumOnLine);
        }

        //ピクチャノード情報
        PictureNodesBottomSheetDialog.PictureNodeInfo pictureNodeInfo = mData.get(position);

        //サムネイル情報
        ImageView iv_picture = convertView.findViewById( R.id.iv_picture );
        iv_picture.setImageBitmap( PictureNodeView.createThumbnail(mContext.getResources(), pictureNodeInfo.getThumbnail()) );
        
        //親ノード名
        TextView tv_parentNode = convertView.findViewById(R.id.tv_parentNode);
        tv_parentNode.setText( pictureNodeInfo.getParentNodeName() );

        //設定したビューを返す
        return convertView;
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public Object getItem(int position) {
        return null;
    }



}

