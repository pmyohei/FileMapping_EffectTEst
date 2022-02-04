package com.mapping.filemapping;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final PictureArrayList<PictureTable> mData;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iv_picture;

        /*
         * コンストラクタ
         */
        public ViewHolder( View itemView ) {
            super(itemView);

            iv_picture = itemView.findViewById( R.id.iv_picture );
        }

        /*
         * ビューの設定
         */
        public void setView( PictureTable picture ){

            //絶対パス
            String path = picture.getPath();

            Log.i("ギャラリー確認", "アダプタ内 setView=" + path);

            //トリミング範囲で切り取り
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            //画像設定
            iv_picture.setImageBitmap( bitmap );

            /*
            //リスナー
            ll_colorItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });*/
        }
    }

    /*
     * コンストラクタ
     */
    public GalleryAdapter(PictureArrayList<PictureTable> data ) {
        mData = data;
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

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_gallery_picture, viewGroup, false);

        return new ViewHolder(view);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        //ビューの設定
        viewHolder.setView( mData.get(i) );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //データ数
        return mData.size();
    }

}
