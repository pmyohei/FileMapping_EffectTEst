package com.mapping.filemapping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryPageAdapter extends RecyclerView.Adapter<GalleryPageAdapter.GuideViewHolder> {

    //レイアウトIDリスト
    private final List<Integer> mLayoutIds;
    //各ページのギャラリー
    private final List<PictureArrayList<PictureTable>> mGallerys;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder {

        private final GridView gv_gallery;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position) {
            super(itemView);

            //画像を表示
            gv_gallery = itemView.findViewById(R.id.gv_gallery);
        }

        /*
         * ページ設定
         */
        private void setPage( int position ) {
            //画像を表示
            gv_gallery.setAdapter( new GalleryAdapter( mGallerys.get(position) ) );
        }
    }

    /*
     * コンストラクタ
     */
    public GalleryPageAdapter(List<Integer> layoutIdList, List<PictureArrayList<PictureTable>> gallery) {
        mLayoutIds = layoutIdList;
        mGallerys = gallery;
    }

    /*
     * ここで返した値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //レイアウトIDを返す
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        //レイアウトを生成
        LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
        View view = inflater.inflate(mLayoutIds.get(position), viewGroup, false);

        return new GuideViewHolder(view, position);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder viewHolder, final int i) {

        //ページ設定
        viewHolder.setPage( i );

    }

    /*
     * 表示データ数の取得
     */
    @Override
    public int getItemCount() {
        //ページ数
        return mLayoutIds.size();
    }




}
