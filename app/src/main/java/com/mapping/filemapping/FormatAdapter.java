package com.mapping.filemapping;

import static android.view.View.GONE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class FormatAdapter extends RecyclerView.Adapter<FormatAdapter.ViewHolder> {

    private final List<String> mData;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        //private final SampleMapView mfl_sampleMap;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView, View view) {
            super(itemView);
            //iv_picture = itemView.findViewById( R.id.iv_picture );
        }

        /*
         * ビューの設定
         */
        public void setView(){
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
    public FormatAdapter( List<String> data ) {
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
        View view = inflater.inflate(R.layout.item_color_pattern, viewGroup, false);

        return new ViewHolder(view, null);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        //ビューの設定
        //viewHolder.setView( mData.get(i) );
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
