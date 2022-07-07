package com.mapping.filemapping;

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

public class ColorHistoryAdapter extends RecyclerView.Adapter<ColorHistoryAdapter.ViewHolder> {

    //カラーパターン
    public static final int COLOR_2 = 2;

    private final List<String[]> mData;
    private final SampleMapView mfl_sampleMap;
    private final ViewGroup mll_colorParent;
    private final int mPattern;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final SampleMapView mfl_sampleMap;
        private final ViewGroup mll_colorParent;
        private final LinearLayout ll_colorItem;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView, View view, ViewGroup colorParent) {
            super(itemView);

            mfl_sampleMap = (SampleMapView)view;
            mll_colorParent = colorParent;
            ll_colorItem = itemView.findViewById(R.id.ll_colorItem);
        }

        /*
         * ビューの設定
         */
        public void setView( String[] colorPettern ){

            Context context = mfl_sampleMap.getContext();

            int count = 0;
            for( String color: colorPettern ){
                //カラーを設定するビューID
                String idStr = "v_color" + Integer.toString( count );
                int v_id = context.getResources().getIdentifier( idStr, "id", context.getPackageName() );

                //色を整数値に変換
                int colorValue = Color.parseColor( color );

                //カラーを設定
                MaterialCardView v = ll_colorItem.findViewById( v_id );
                ColorStateList colorState = new ColorStateList(
                        new int[][] {
                                new int[]{ android.R.attr.state_checked},
                                new int[]{ -android.R.attr.state_checked},
                        },
                        new int[] {
                                colorValue,
                                colorValue,
                        }
                );
                v.setCardForegroundColor( colorState );

                count++;
            }

            //色履歴クリックリスナー
            ll_colorItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //履歴をサンプルマップに反映
                    mfl_sampleMap.setColorPattern( colorPettern );
                    //履歴を選択中色に反映
                    MaterialCardView mcv_color0 = mll_colorParent.findViewById(R.id.mcv_color0);
                    MaterialCardView mcv_color1 = mll_colorParent.findViewById(R.id.mcv_color1);
                    mcv_color0.setCardBackgroundColor( Color.parseColor(colorPettern[0]) );
                    mcv_color1.setCardBackgroundColor( Color.parseColor(colorPettern[1]) );
                }
            });
        }
    }

    /*
     * コンストラクタ
     */
    public ColorHistoryAdapter(List<String[]> data, View sampleMap, ViewGroup colorParent, int pattern ) {
        mData = data;
        mfl_sampleMap = (SampleMapView) sampleMap;
        mll_colorParent = colorParent;
        mPattern = pattern;
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

        int layout = ( (mPattern == COLOR_2) ? R.layout.item_color_pattern2: R.layout.item_color_pattern3 );

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layout, viewGroup, false);

        return new ViewHolder(view, mfl_sampleMap, mll_colorParent);
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
        //表示データ数を返す
        return mData.size();
    }

}
