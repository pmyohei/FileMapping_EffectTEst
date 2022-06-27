package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/*
 * ギャラリー（ノード配下の写真リスト表示）用アダプタ
 */
public class GalleryGridAdapter extends BaseAdapter {

    //写真数
    public final static int PORTRAIT_NUM = 2;
    public final static int LANDSCAPE_NUM = 6;

    private final PictureArrayList<PictureTable> mData;
    private final float mDp;
    private final Context mContext;
    private final GridView mGV_gallery;
    private int mPictureNumOnLine;              //1行で表示する写真の数


    /*
     * ViewHolder
     */
    private class ViewHolder {

        //画像view
        private final PictureInGalleryView mPictureInGalleryView;
        private final ImageView mIv_picture;

        /*
         * コンストラクタ
         */
        public ViewHolder(View view){
            mPictureInGalleryView = (PictureInGalleryView)view;
            mIv_picture = view.findViewById( R.id.iv_picture );
        }

        /*
         * ビューの設定
         */
        @SuppressLint("ClickableViewAccessibility")
        public void setView( int position ) {

            //複数選択モードでなければ、チェック状態なしにする
            //※複数選択を解除しても、viewのデザインが選択状態になっているケースの対応
            if( mGV_gallery.getChoiceMode() == GridView.CHOICE_MODE_NONE ){
                mPictureInGalleryView.setChecked(false);
            }

            if( ResourceManager.READ_URI ){
                Picasso.get()
                        .load( mData.get(position).getPath() )
                        .fit().centerCrop()                     //※画像の表示範囲の指定はxmlではなくここでやること（表示がかなり重くなるため）
                        .error(R.drawable.baseline_no_image)    //エラー画像の設定は、Picassoでは行わない、描画が遅れるため（※この.error設定は念のため）
                        .into( mIv_picture );
            } else {
                //リリース版はこちら

                //ファイルがないとき、Picassoでは描画が遅れるため、ここでエラー設定を行う
                String path = mData.get(position).getPath();
                File file = new File(path);
                if( !file.isFile() ){
                    mIv_picture.setImageResource( R.drawable.baseline_no_image);
                    return;
                }

                //Picassoを利用して画像を設定
                Picasso.get()
                        .load( file )
                        .fit().centerCrop()                     //※画像の表示範囲の指定はxmlではなくここでやること（表示がかなり重くなるため）
                        .error(R.drawable.baseline_no_image)    //エラー画像の設定は、Picassoでは行わない、描画が遅れるため（※この.error設定は念のため）
                        .into( mIv_picture );
            }
        }
    }

    /*
     * コンストラクタ
     */
    public GalleryGridAdapter(Context context, GridView gv_gallery, PictureArrayList<PictureTable> data){
        mContext = context;
        mGV_gallery = gv_gallery;
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
            //※ナビゲーションバーがあっても横画面時に表示エリアのサイズが適切になるように、アクティビティのルートレイアウトのサイズを取得
            int sideLength = (parent.getRootView().findViewById(R.id.ll_gallery).getWidth() / mPictureNumOnLine) - (int)mDp*2;
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    sideLength,
                    sideLength);
            convertView.setLayoutParams(params);

            //ViewHolderを生成し、タグ設定
            holder = new ViewHolder( convertView );
            convertView.setTag(holder);

        } else {
            //一度表示されているなら、そのまま活用
            holder = (ViewHolder)convertView.getTag();
        }

        //写真ビュー設定
        holder.setView( position );

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

