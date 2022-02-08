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

public class ColorPatternAdapter extends RecyclerView.Adapter<ColorPatternAdapter.ViewHolder> {

    //カラーパターン
    public static final int COLOR_2 = 2;
    public static final int COLOR_3 = 3;

    //private final List<String> mData;
    private final String[] mData;
    private final SampleMapView mfl_sampleMap;
    private final int mPattern;

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
    public ColorPatternAdapter(String[] data, View sampleMap, int pattern ) {
        mData = data;
        mfl_sampleMap = (SampleMapView) sampleMap;
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
