package com.mapping.filemapping;

import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

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
            //画面向きを取得
            int orientation = gv_gallery.getContext().getResources().getConfiguration().orientation;

            //1行に表示する写真数を設定
            int pictureNumOnLine = ( (orientation == Configuration.ORIENTATION_PORTRAIT) ? GalleryAdapter.PORTRAIT_NUM : GalleryAdapter.LANDSCAPE_NUM );
            gv_gallery.setNumColumns( pictureNumOnLine );

            //レイアウト確定後、アダプタの設定を行う
            //※ギャラリー用リサイクラービューの大きさで写真の大きさを決定しているため
            ViewTreeObserver observer = gv_gallery.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            //アダプタを設定
                            gv_gallery.setAdapter( new GalleryAdapter( gv_gallery.getContext(), mGallerys.get(position) ) );

                            //レイアウト確定後は、不要なので本リスナー削除
                            gv_gallery.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
            );
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
