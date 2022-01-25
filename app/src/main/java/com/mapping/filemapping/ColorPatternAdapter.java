package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorPatternAdapter extends RecyclerView.Adapter<ColorPatternAdapter.ViewHolder> {

    //private final List<String> mData;
    private final String[]     mData;
    private SampleMapView      mfl_sampleMap;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final SampleMapView     mfl_sampleMap;
        private final LinearLayout      ll_colorItem;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView, View view) {
            super(itemView);

            mfl_sampleMap = (SampleMapView)view;

            ll_colorItem   = itemView.findViewById(R.id.ll_colorItem);
        }

        /*
         * ビューの設定
         */
        public void setView( String colorPettern ){

            //カラーパターンタイトルのリソースID
            Context context = mfl_sampleMap.getContext();
            int colorPetternId = context.getResources().getIdentifier( colorPettern, "array", context.getPackageName() );

            //カラーパターン
            String[] colorsStr = context.getResources().getStringArray( colorPetternId );

            int count = 0;
            for( String color: colorsStr ){

                //カラーを設定するビューID
                String idStr = "v_color" + Integer.toString( count );
                int v_id = context.getResources().getIdentifier( idStr, "id", context.getPackageName() );

                //色を整数値に変換
                int colorValue = Color.parseColor( color );

                //カラーを設定
                View v = ll_colorItem.findViewById( v_id );
                v.setBackgroundColor( colorValue );

                count++;
            }

            //リスナー
            ll_colorItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //カラーパターンをサンプルマップに設定
                    mfl_sampleMap.setColorPattern( colorsStr );
                }
            });
        }
    }

    /*
     * コンストラクタ
     */
    public ColorPatternAdapter(String[] data, View sampleMap ) {
        mData = data;
        mfl_sampleMap = (SampleMapView) sampleMap;
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

        return new ViewHolder(view, mfl_sampleMap);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        //ビューの設定
        viewHolder.setView( mData[i] );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        return mData.length;
    }

}
