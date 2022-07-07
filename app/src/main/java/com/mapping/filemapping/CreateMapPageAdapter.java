package com.mapping.filemapping;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CreateMapPageAdapter extends RecyclerView.Adapter<CreateMapPageAdapter.GuideViewHolder> {

    private final List<Integer> mData;
    private final SampleMapView mfl_sampleMap;
    //カラー生成ページ設定完了フラグ
    private boolean mIsColorGenerationFlg;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //設定対象ノードビュー
        private final SampleMapView mfl_sampleMap;
        /*--- マップ名 ---*/
        private EditText et_mapName;
        /*--- カラーパターン ---*/
        private RecyclerView rv_colorPattern2;
        private RecyclerView rv_colorPattern3;
        /*--- カラー自動生成 ---*/
        private LinearLayout ll_colorParent;
        private MaterialCardView mcv_color0;
        private MaterialCardView mcv_color1;
        private ImageView iv_check_state0;
        private ImageView iv_check_state1;
        private ImageView iv_generateColor;
        private RecyclerView rv_colorHistory;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, SampleMapView fl_sampleMap) {
            super(itemView);

            mfl_sampleMap = fl_sampleMap;
            mIsColorGenerationFlg = false;

            switch (position) {
                case 0:
                    //マップ名
                    et_mapName = itemView.findViewById(R.id.et_mapName);
                    break;

                case 1:
                    //カラーパターン2色
                    rv_colorPattern2 = itemView.findViewById(R.id.rv_colorPattern2);
                    break;

                case 2:
                    //カラーパターン3色
                    rv_colorPattern3 = itemView.findViewById(R.id.rv_colorPattern3);
                    break;

                case 3:
                    //カラー生成
                    ll_colorParent = itemView.findViewById(R.id.ll_colorParent);
                    mcv_color0 = itemView.findViewById(R.id.mcv_color0);
                    mcv_color1 = itemView.findViewById(R.id.mcv_color1);
                    iv_check_state0 = itemView.findViewById(R.id.iv_check_state0);
                    iv_check_state1 = itemView.findViewById(R.id.iv_check_state1);
                    iv_generateColor = itemView.findViewById(R.id.iv_generateColor);
                    rv_colorHistory = itemView.findViewById(R.id.rv_colorHistory);
                    break;
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            switch (position) {
                case 0:
                    setMapNamePage();
                    break;
                case 1:
                    setColor2Page();
                    break;
                case 2:
                    setColor3Page();
                    break;
                case 3:
                    setColorGeneratePage();
                    break;
            }
        }

        /*
         * ページ設定；マップ名入力
         */
        public void setMapNamePage() {
            //文字入力リスナーを設定
            et_mapName.addTextChangedListener(this);
        }

        /*
         * ページ設定；カラー選択2色
         */
        public void setColor2Page() {
            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern2.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_2_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern2.setLayoutManager( new LinearLayoutManager(context) );
            rv_colorPattern2.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap, ColorPatternAdapter.COLOR_2 ) );
        }

        /*
         * ページ設定；カラー選択3色
         */
        public void setColor3Page() {
            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern3.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_3_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern3.setLayoutManager(new LinearLayoutManager(context));
            rv_colorPattern3.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap, ColorPatternAdapter.COLOR_3 ) );
        }

        /*
         * ページ設定；カラー生成
         */
        public void setColorGeneratePage() {
            //--------------------------------
            // サンプルマップの色を選択中の色とする
            //--------------------------------
            String[] mapColors = mfl_sampleMap.getCurrentColors();
            mcv_color0.setCardBackgroundColor( Color.parseColor(mapColors[0]) );
            mcv_color1.setCardBackgroundColor( Color.parseColor(mapColors[1]) );

            //一度ページ設定されているなら、以降の処理は不要
            if( mIsColorGenerationFlg ){
                return;
            }
            mIsColorGenerationFlg = true;

            //--------------------------------
            // 色固定チェックリスナー
            //--------------------------------
            mcv_color0.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //チェック状態を反転
                    int check0_state = iv_check_state0.getVisibility();
                    if( check0_state == View.VISIBLE ){
                        iv_check_state0.setVisibility( View.INVISIBLE );
                    } else {
                        iv_check_state0.setVisibility( View.VISIBLE );
                    }

                    int check1_state = iv_check_state1.getVisibility();
                    if( check1_state == View.VISIBLE ){
                        iv_check_state1.setVisibility( View.INVISIBLE );
                    }
                }
            });

            mcv_color1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //チェック状態を反転
                    int check1_state = iv_check_state1.getVisibility();
                    if( check1_state == View.VISIBLE ){
                        iv_check_state1.setVisibility( View.INVISIBLE );
                    } else {
                        iv_check_state1.setVisibility( View.VISIBLE );
                    }

                    int check0_state = iv_check_state0.getVisibility();
                    if( check0_state == View.VISIBLE ){
                        iv_check_state0.setVisibility( View.INVISIBLE );
                    }
                }
            });

            //--------------------------------
            // 色履歴
            //--------------------------------
            Context context = rv_colorHistory.getContext();
            List<String[]> colorPattern = new ArrayList<>();

            ColorHistoryAdapter historyAdapter = new ColorHistoryAdapter( colorPattern, mfl_sampleMap, ll_colorParent, ColorHistoryAdapter.COLOR_2 );
            rv_colorHistory.setLayoutManager(new LinearLayoutManager(context));
            rv_colorHistory.setAdapter( historyAdapter );

            //--------------------------------
            // 色生成リスナー
            //--------------------------------
            iv_generateColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //色履歴に追加
                    addColorHistory( historyAdapter, colorPattern );
                    //カラー生成
                    createMatchingColor();
                    //サンプルマップへ色を反映
                    applyColorToSampleMap();
                }
            });
        }

        /*
         * マッチカラーの生成
         */
        private void createMatchingColor(){
            //ベースカラーの生成
            int baseColor = getBaseColor();

            //ベースカラーの適合色を生成
            float[] baseColorHSV = new float[3];
            Color.colorToHSV(baseColor, baseColorHSV);
            float[] matchingHSV = ColorGenerater.createMatchingHSV( baseColorHSV );
            int matchingColor = Color.HSVToColor( matchingHSV );

            //色設定
            applyGenerateColor( baseColor, matchingColor );
        }


        /*
         * ベースカラーの取得
         */
        private int getBaseColor(){
            if( iv_check_state0.getVisibility() == View.VISIBLE ){
                //チェック中の色を返す
                ColorStateList colorDrawable = mcv_color0.getCardBackgroundColor();
                return colorDrawable.getDefaultColor();
            }

            if( iv_check_state1.getVisibility() == View.VISIBLE ){
                //チェック中の色を返す
                ColorStateList colorDrawable = mcv_color1.getCardBackgroundColor();
                return colorDrawable.getDefaultColor();
            }

            //チェックなしの場合は、ランダムで生成
            float[] randomHSV = ColorGenerater.createRandomHSV();
            return Color.HSVToColor(randomHSV);
        }

        /*
         * 生成色をビューへ適用
         */
        private void applyGenerateColor( int baseColor, int matchingColor ){
            //1色目がチェック中なら、2色目の色のみ変更
            if( iv_check_state0.getVisibility() == View.VISIBLE ){
                mcv_color1.setCardBackgroundColor( matchingColor );
                return;
            }

            //2色目がチェック中なら、1色目の色のみ変更
            if( iv_check_state1.getVisibility() == View.VISIBLE ){
                mcv_color0.setCardBackgroundColor( matchingColor );
                return;
            }

            //チェックがなければ、どちらも色を適用
            mcv_color0.setCardBackgroundColor( baseColor );
            mcv_color1.setCardBackgroundColor( matchingColor );
        }

        /*
         * 生成色を色履歴へ追加
         */
        private void addColorHistory(ColorHistoryAdapter historyAdapter, List<String[]> colorPattern){
            //選択中の色を取得し、色履歴アダプタのリストへ追加
            String[] colorsStr = getSelecteColors();
            colorPattern.add( colorsStr );

            //色履歴アダプタへ追加を通知
            int addPos = colorPattern.size() - 1;
            historyAdapter.notifyItemInserted( addPos );
        }

        /*
         * 生成色をサンプルマップへ適用
         */
        private void applyColorToSampleMap(){
            //現在生成中の色リストを作成
            String[] colorsStr = getSelecteColors();
            //サンプルマップへ色を反映
            mfl_sampleMap.setColorPattern( colorsStr );
        }

        /*
         * 生成色をサンプルマップへ適用
         */
        private String[] getSelecteColors(){
            //現在生成中の色リストを作成
            String[] colorsStr = new String[2];
            ColorStateList colorDrawable0 = mcv_color0.getCardBackgroundColor();
            ColorStateList colorDrawable1 = mcv_color1.getCardBackgroundColor();
            int color0 = colorDrawable0.getDefaultColor();
            int color1 = colorDrawable1.getDefaultColor();
            String code0 = "#" + Integer.toHexString( color0 );
            String code1 = "#" + Integer.toHexString( color1 );
            colorsStr[0] = code0;
            colorsStr[1] = code1;

            return colorsStr;
        }


        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
            //サンプルマップ側へ設定
            mfl_sampleMap.setMapName( editable.toString() );
        }
    }

    /*
     * コンストラクタ
     */
    public CreateMapPageAdapter(List<Integer> layoutIdList, SampleMapView fl_sampleMap) {
        mData = layoutIdList;
        mfl_sampleMap = fl_sampleMap;
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
        View view = inflater.inflate(mData.get(position), viewGroup, false);

        return new GuideViewHolder(view, position, mfl_sampleMap);
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
        return mData.size();
    }
}
