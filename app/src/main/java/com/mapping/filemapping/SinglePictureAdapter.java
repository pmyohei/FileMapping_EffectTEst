package com.mapping.filemapping;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final LayoutInflater mInflater;

    //int Test;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final SingleMatrixImageView iv_singlePicture;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView) {
            super(itemView);
            iv_singlePicture = itemView.findViewById(R.id.iv_singlePicture);
        }

        /*
         * ビューの設定
         */
        public void setViewMatrix( PictureTable picture ){

            //Log.i("ページ更新チェック", "★更新発生 アダプタ側=" + Test );

            //画像設定前に、マトリクス関連の状態をリセット
            //※画像設定済みでピンチ操作が発生しているビューへの対策
            //　これをしないと画像の初期サイズが安定しない状態になる
            iv_singlePicture.resetMatrixData();

            //画像割り当て
            Picasso.get()
                    .load( new File( picture.getPath() ) )
                    .error(R.drawable.baseline_picture_read_error_24)
                    .into( iv_singlePicture );
        }
    }

    /*
     * コンストラクタ
     */
    public SinglePictureAdapter(Context context, ArrayList<PictureTable> data){
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
        View view = mInflater.inflate(R.layout.item_single_matrix_picture, viewGroup, false);

        return new ViewHolder(view);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        //Log.i("単体表示", "onBindViewHolder");

        //Test = i;

        //ビューの設定
        viewHolder.setViewMatrix( mData.get(i) );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
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

