package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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

public class GalleryAdapter extends BaseAdapter {

    //写真数
    public final static int PORTRAIT_NUM = 2;
    public final static int LANDSCAPE_NUM = 6;

    private final PictureArrayList<PictureTable> mData;
    private final float mDp;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mPictureNumOnLine;              //1行で表示する写真の数
    private boolean mIsMultipleSelection;       //複数選択状態
    private List<Integer> mSelectedList;        //選択中の写真Index


    /*
     * ViewHolder
     */
    private class ViewHolder {

        //セル位置
        //private int position;
        private final ImageView mIv_picture;
        //private final MaterialCardView mMcv_picture;

        /*
         * コンストラクタ
         */
        public ViewHolder(View view){
            //mMcv_picture = view.findViewById( R.id.mcv_picture );
            mIv_picture = view.findViewById( R.id.iv_picture );
        }

        /*
         * ビューの設定
         */
        @SuppressLint("ClickableViewAccessibility")
        public void setView( PictureInGalleryView pictureInGalleryView, int position ) {

            //Picassoを利用して画像を設定
            Picasso.get()
                    .load( new File( mData.get(position).getPath() ) )
                    .error(R.drawable.baseline_picture_read_error_24)
                    .into( mIv_picture );

            //選択中状態の設定
            initSelectedState( pictureInGalleryView, position );

            //クリックリスナー
            mIv_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //複数選択状態かどうか
                    if( mIsMultipleSelection ){
                        //選択状態の設定
                        updateSelectedState(pictureInGalleryView, position);
                    } else {
                        //画面遷移
                        transitionScreen(position);
                    }
                }
            });

            //ロングクリックリスナー
            mIv_picture.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    if( !mIsMultipleSelection ){
                        //複数選択状態になければ、複数選択状態へ移行
                        mIsMultipleSelection = true;

                        //選択中にする
                        updateSelectedState(pictureInGalleryView, position);

                        //ツールバーにメニューを表示
                        ((PictureGalleryActivity)mContext).setMultipleOptionMenu(true);
                    }

                    return true;
                }
            });
        }

        /*
         * 画面遷移
         */
        public void transitionScreen(int position) {

            //渡す情報を設定
            Intent intent = new Intent( ((PictureGalleryActivity)mContext).getApplication(), SinglePictureDisplayActivity.class );
            intent.putExtra( "pictures", mData );       //表示する写真リスト
            intent.putExtra( "position", position );    //表示開始写真位置

            //ランチャーを使用して画面開始
            ((PictureGalleryActivity)mContext).getSinglePictureLauncher().launch(intent);
        }

        /*
         * 選択中状態の初期化
         */
        public void initSelectedState(PictureInGalleryView pictureInGalleryView, int position) {

            boolean isSelected = false;

            //選択中リストにあるかチェック
            for( Integer index: mSelectedList ){
                if( position == index ){
                    isSelected = true;
                    break;
                }
            }

            //非選択中に設定
            pictureInGalleryView.setChecked(isSelected);
        }

        /*
         * 選択中状態の更新
         */
        public void updateSelectedState(PictureInGalleryView pictureInGalleryView, int position) {

            if( pictureInGalleryView.isChecked() ){
                int i = 0;
                for( Integer queData: mSelectedList ){
                    if( queData == position ){
                        //選択中リストから削除
                        mSelectedList.remove( i );
                        //ビューの状態を更新
                        pictureInGalleryView.toggle();

                        //※リストループ中で削除しているため、ここで処理を終了
                        //※（次のループにいくとおちる）
                        return;
                    }
                    i++;
                }

            } else {
                //選択中リストに追加
                mSelectedList.add( position );

                //ビューの状態を更新
                pictureInGalleryView.toggle();
            }

            //ビューの状態を更新
            //pictureInGalleryView.toggle();

/*            for( Integer index: mSelectedList ){
                //既に選択済みであれば
                if( position == index ){
                    mSelectedList.remove(index);
                    pictureInGalleryView.setCheckStateView(false);

                    return;
                }
            }

            //なければ追加
            mSelectedList.add(position);
            pictureInGalleryView.setCheckStateView(true);*/
        }


        /*
         * 選択中状態の設定
         */
/*        public void setSelectedView( boolean isSelected ) {

            if( isSelected ){
                //選択中に設定
                mMcv_picture.setStrokeWidth( 20 );
            } else {
                //非選択中に設定
                mMcv_picture.setStrokeWidth( 0 );
            }
        }*/

    }

    /*
     * コンストラクタ
     */
    public GalleryAdapter(Context context, PictureArrayList<PictureTable> data){
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);

        //複数選択状態
        mIsMultipleSelection = false;
        //選択中リスト初期化
        mSelectedList = new ArrayList<>();
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

        Log.i("複数選択対応", "getView position=" + position);

        ViewHolder holder;

        if (convertView == null) {
            //ビュー未生成の場合、新たに生成
            //convertView = mInflater.inflate(R.layout.item_gallery_picture, null);
            convertView = new PictureInGalleryView(mContext);

            //写真用ビューのサイズ
            int sideLength = (parent.getWidth() / mPictureNumOnLine) - (int)mDp*2;
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


    /*
     * 選択状態の解除
     */
    public void clearSelectedState() {
        mIsMultipleSelection = false;
        mSelectedList.clear();
    }

}

