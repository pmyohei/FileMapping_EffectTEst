package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * ギャラリー（ノード配下の写真リスト表示）用アダプタ
 */
public class GalleryAdapter extends BaseAdapter {

    //写真数
    public final static int PORTRAIT_NUM = 2;
    public final static int LANDSCAPE_NUM = 6;

    private final PictureArrayList<PictureTable> mData;
    private final float mDp;
    private final Context mContext;
    private int mPictureNumOnLine;              //1行で表示する写真の数


    /*
     * ViewHolder
     */
    private class ViewHolder {

        //セル位置
        private final ImageView mIv_picture;

        /*
         * コンストラクタ
         */
        public ViewHolder(View view){
            mIv_picture = view.findViewById( R.id.iv_picture );
        }

        /*
         * ビューの設定
         */
        @SuppressLint("ClickableViewAccessibility")
        public void setView( PictureInGalleryView pictureInGalleryView, int position ) {

            Log.i("Picasso", "パス=" + mData.get(position).getPath());

            //Picassoを利用して画像を設定
            Picasso.get()
                    .load( new File( mData.get(position).getPath() ) )
                    .fit().centerCrop()                                    //※画像の表示範囲の指定はxmlではなくここでやること（表示がかなり重くなるため）
                    .error(R.drawable.baseline_picture_read_error_24)
                    .into( mIv_picture );

        }

    }

    /*
     * コンストラクタ
     */
    public GalleryAdapter(Context context, PictureArrayList<PictureTable> data){
        mContext = context;
        mData = data;

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

        //Log.i("複数選択対応", "getView position=" + position + "parent.getWidth()=" + parent.getWidth());
        //Log.i("複数選択対応", "parent.getRootView().getWidth()=" + parent.getRootView().getWidth());

        ViewHolder holder;

        if (convertView == null) {
            //ビュー未生成の場合、新たに生成
            //convertView = mInflater.inflate(R.layout.item_gallery_picture, null);
            convertView = new PictureInGalleryView(mContext);

            //写真用ビューのサイズ
            //※parent(=GridView)は、レイアウトが確定していない状態にあるため、その親のレイアウトのサイズを取得
            //※（横幅は同じため、問題なし）
            int sideLength = (parent.getRootView().getWidth() / mPictureNumOnLine) - (int)mDp*2;
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    sideLength,
                    sideLength);
            convertView.setLayoutParams(params);

            //ViewHolderを生成し、タグ設定
            holder = new ViewHolder( convertView );
            convertView.setTag(holder);

            //float dp = mContext.getResources().getDisplayMetrics().density;
            //Log.i("ギャラリー", "position=" + position + " 前回設定サイズ=" + (parent.getWidth() / 2 - (int)dp) );
            //Log.i("ギャラリー", "position=" + position + " mPictureNumOnLine=" + mPictureNumOnLine);

        } else {
            //一度表示されているなら、そのまま活用
            holder = (ViewHolder)convertView.getTag();
        }

        //写真ビュー設定
        holder.setView( (PictureInGalleryView)convertView, position );

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

