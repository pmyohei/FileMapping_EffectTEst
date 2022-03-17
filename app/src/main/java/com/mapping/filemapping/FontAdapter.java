package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.FontItemViewHolder> {

    //言語
    public static int ALPHABET = 0;
    public static int JAPANESE = 1;

    //フォントリスト
    private final List<String> mFontFileNames;
    //設定対象ノードビュー
    private final BaseNode mv_node;
    private final View mView;
    private final int mLang;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class FontItemViewHolder extends RecyclerView.ViewHolder {

        //設定対象ノードビュー
        private final BaseNode mv_node;

        private final View mView;           //※共通データ取得用
        private final TextView tv_sampleFont;
        private final MaterialCardView mcv_fontItem;

        /*
         * コンストラクタ
         */
        public FontItemViewHolder(View itemView, BaseNode node, View view) {
            super(itemView);

            //対象ノード
            mv_node = node;
            mView = view;

            tv_sampleFont = itemView.findViewById(R.id.tv_sampleFont);
            mcv_fontItem = itemView.findViewById(R.id.mcv_fontItem);
        }

        /*
         * ビューの設定
         */
        public void setView(String fontFileName) {

            //指定フォントファイル名から、フォントを生成
            Context context = tv_sampleFont.getContext();
            int fontID = context.getResources().getIdentifier(fontFileName, "font", context.getPackageName());

            //運用誤りでファイル名文字列のファイルがない場合、何もしない
            if (fontID == 0) {
                Log.i("フォント保存対応", "ID変換エラー=" + fontFileName);
                disableFontItem();
                return;
            }

            //フォント変換エラー
            Typeface font = ResourcesCompat.getFont(context, fontID);
            if (font == null) {
                Log.i("フォント保存対応", "Typeface変換エラー=" + fontFileName);
                disableFontItem();
                return;
            }

            //フォントを設定
            tv_sampleFont.setTypeface(font);

            //フォント適用リスナー
            mcv_fontItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mv_node == null) {
                        //マップ内の全ノードへ適用
                        MapCommonData commonData = (MapCommonData) ((Activity) mView.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        nodes.setAllNodeFont(font, fontFileName);

                    } else {
                        //指定ノードへ適用
                        mv_node.setNodeFont(font, fontFileName);
                    }

                }
            });
        }

        /*
         * フォントアイコン無効化
         *   ※フォント設定エラーが発生することはないはずだが、念のため
         */
        public void disableFontItem(){
            //無効デザイン
            tv_sampleFont.setText("");
            tv_sampleFont.setBackgroundColor( tv_sampleFont.getContext().getResources().getColor( R.color.transparent_50_black ) );

            //フォント適用リスナー
            mcv_fontItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.toast_errorFont), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /*
     * コンストラクタ
     */
    public FontAdapter(List<String> data, BaseNode node, View view, int lang ) {
        mFontFileNames = data;
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
        int layout = ( (mLang == ALPHABET) ? R.layout.item_font_alpha : R.layout.item_font_jp );

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
        //Typeface font = mData.get(i);
        String fontFileName = mFontFileNames.get(i);

        //ビューの設定
        viewHolder.setView( fontFileName );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        return mFontFileNames.size();
    }

}
