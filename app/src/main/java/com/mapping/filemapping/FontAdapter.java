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

import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.FontItemViewHolder> {

    //言語
    public static int ALPHABET = 0;
    public static int JAPANESE = 1;

    //フォントリスト
    private final List<Typeface> mData;
    //設定対象ノードビュー
    private final BaseNode       mv_node;
    private View                 mView;
    private int                  mLang;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class FontItemViewHolder extends RecyclerView.ViewHolder {

        //設定対象ノードビュー
        private final BaseNode          mv_node;

        private final View              mView;           //※共通データ取得用
        private final TextView          tv_sampleFont;
        private final LinearLayout      ll_fontItem;

        /*
         * コンストラクタ
         */
        public FontItemViewHolder(View itemView, BaseNode node, View view) {
            super(itemView);

            //対象ノード
            mv_node       = node;
            mView         = view;

            tv_sampleFont = itemView.findViewById(R.id.tv_sampleFont);
            ll_fontItem   = itemView.findViewById(R.id.ll_fontItem);
        }

        /*
         * ビューの設定
         */
        public void setView( Typeface font ){

            //フォントを設定
            tv_sampleFont.setTypeface( font );

            //フォント適用リスナー
            ll_fontItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if( mv_node == null ){
                        //マップ内の全ノードへ適用
                        MapCommonData commonData = (MapCommonData)((Activity)mView.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        nodes.setAllNodeTxFont( font );

                    } else {
                        //指定ノードへ適用
                        mv_node.setNodeFont( font );
                    }

                }
            });
        }
    }

    /*
     * コンストラクタ
     */
    public FontAdapter(List<Typeface> data, BaseNode node, View view, int lang ) {
        mData = data;
        mv_node = node;
        mView = view;
        mLang = lang;
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
    public FontItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //レイアウトファイル
        int layout = ( (mLang == ALPHABET) ? R.layout.item_font: R.layout.item_font_jp );

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layout, viewGroup, false);

        return new FontItemViewHolder(view, mv_node, mView);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull FontItemViewHolder viewHolder, final int i) {

        //対象マップ情報
        Typeface font = mData.get(i);

        //ビューの設定
        viewHolder.setView( font );
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
