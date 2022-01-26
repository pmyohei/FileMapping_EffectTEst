package com.mapping.filemapping;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ColorHistoryAdapter extends RecyclerView.Adapter<ColorHistoryAdapter.ColorHistoryViewHolder> {

    //フォントリスト
    private final ArrayList<String> mData;
    //設定対象ノードビュー
    private final BaseNode       mv_node;
    private View                 mView;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ColorHistoryViewHolder extends RecyclerView.ViewHolder {

        //設定対象ノードビュー
        private final BaseNode          mv_node;

        private final View              mView;           //※共通データ取得用
        private final LinearLayout      ll_fontItem;

        /*
         * コンストラクタ
         */
        public ColorHistoryViewHolder(View itemView, BaseNode node, View view) {
            super(itemView);

            //対象ノード
            mv_node       = node;
            mView         = view;

            //tv_sampleFont = itemView.findViewById(R.id.tv_sampleFont);
            ll_fontItem   = itemView.findViewById(R.id.ll_fontItem);
        }

        /*
         * ビューの設定
         */
        public void setView( String color ){

            //フォントを設定
            //tv_sampleFont.setTypeface( color );

            //フォント適用リスナー
            ll_fontItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if( mv_node == null ){
                        //マップ内の全ノードへ適用
                        MapCommonData commonData = (MapCommonData)((Activity)mView.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        //nodes.setAllNodeFont( "color" );

                    } else {
                        //指定ノードへ適用
                        //mv_node.setNodeFont( "color" );
                    }

                }
            });
        }
    }

    /*
     * コンストラクタ
     */
    public ColorHistoryAdapter(ArrayList<String> colors, BaseNode node, View view ) {
        mData = colors;
        mv_node = node;
        mView = view;
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
    public ColorHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_color_history, viewGroup, false);

        return new ColorHistoryViewHolder(view, mv_node, mView);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull ColorHistoryViewHolder viewHolder, final int i) {

        //対象マップ情報
        String color = mData.get(i);

        //ビューの設定
        viewHolder.setView( color );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        return mData.size();
    }

}
