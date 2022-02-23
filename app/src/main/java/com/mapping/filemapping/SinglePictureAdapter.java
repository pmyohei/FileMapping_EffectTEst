package com.mapping.filemapping;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/*
 * 写真単体をリスト表示するアダプタ
 */
public class SinglePictureAdapter extends RecyclerView.Adapter<SinglePictureAdapter.ViewHolder> {

    private final ArrayList<PictureTable> mData;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mParentHeight;
    private int mParentWidth;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        //private final SingleImageView iv_singlePicture;
        //private final SingleScrollImageView iv_singleScrollPicture;
        private final ImageView iv_singleScrollPicture;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView) {
            super(itemView);
            //iv_singlePicture = itemView.findViewById(R.id.iv_singlePicture);
            iv_singleScrollPicture = itemView.findViewById(R.id.iv_singleScrollPicture);
        }

        /*
         * ビューの設定
         */
        public void setView( PictureTable picture ){

            Log.i("単体表示", "getPath=" + picture.getPath() );

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

            layoutParams.height = mParentHeight;
            layoutParams.width = mParentWidth;

            iv_singleScrollPicture.setLayoutParams(layoutParams);

            Picasso.get()
                    .load( new File( picture.getPath() ) )
                    //.fit().centerInside()
                    .error(R.drawable.baseline_picture_read_error_24)
                    //.into( iv_singlePicture );
                    .into( iv_singleScrollPicture );

            //★ページ遷移後の更新はこれだけやりたい
            //iv_singlePicture.setScaleTypeA();
        }
    }

    /*
     * コンストラクタ
     */
    public SinglePictureAdapter(Context context, ArrayList<PictureTable> data){
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
    }

    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        Log.i("単体表示", "onCreateViewHolder");

        //ビューを生成
        View view = mInflater.inflate(R.layout.item_single_picture, viewGroup, false);
        //View view = new SingleScrollImageView( viewGroup.getContext(), viewGroup );

        Log.i("写真スクロール", "親=" + viewGroup.getClass());
        Log.i("写真スクロール", "親 height=" + viewGroup.getHeight());
        Log.i("写真スクロール", "親 width=" + viewGroup.getWidth());

        mParentHeight = viewGroup.getHeight();
        mParentWidth = viewGroup.getWidth();

        return new ViewHolder(view);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        Log.i("単体表示", "onBindViewHolder");

        //ビューの設定
        viewHolder.setView( mData.get(i) );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        Log.i("単体表示", "mData.size()=" + mData.size());
        return mData.size();
    }

    /*
     * アイテム削除
     */
    public void removeItem( int index ) {
        //リストから削除
        mData.remove( index );
        //自身に削除通知
        this.notifyItemRemoved( index );
    }

}

