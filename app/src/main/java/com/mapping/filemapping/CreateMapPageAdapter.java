package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CreateMapPageAdapter extends RecyclerView.Adapter<CreateMapPageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //
    private final View mfl_sampleMap;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //設定対象ノードビュー
        private final View mfl_sampleMap;
        /*--- マップ名 ---*/
        private EditText et_mapName;

        /*--- カラーパターン ---*/
        private RecyclerView rv_colorPattern2;
        private RecyclerView rv_colorPattern3;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, View fl_sampleMap) {
            super(itemView);

            mfl_sampleMap = fl_sampleMap;

            switch (position) {
                case 0:
                    //マップ名
                    et_mapName = itemView.findViewById(R.id.et_mapName);
                    break;

                case 1:
                    //ノード数
                    //np_nodeNum = itemView.findViewById(R.id.np_nodeNum);
                    //カラーパターン2色
                    rv_colorPattern2 = itemView.findViewById(R.id.rv_colorPattern2);
                    break;

                case 2:
                    //カラーパターン3色
                    rv_colorPattern3 = itemView.findViewById(R.id.rv_colorPattern3);
                    break;

                case 3:
                    break;
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            switch (position) {
                case 0:
                    setPage0();
                    break;
                case 1:
                    setPage1();
                    break;
                case 2:
                    setPage2();
                    break;
                case 3:
                    break;
            }
        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {
            //文字入力リスナーを設定
            et_mapName.addTextChangedListener(this);
        }

        /*
         * ページ設定（２）
         */
        public void setPage1() {

            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern2.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_2_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern2.setLayoutManager( new LinearLayoutManager(context) );
            rv_colorPattern2.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap, ColorPatternAdapter.COLOR_2 ) );
        }

        /*
         * ページ設定（３）
         */
        public void setPage2() {

            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern3.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_3_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern3.setLayoutManager(new LinearLayoutManager(context));
            rv_colorPattern3.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap, ColorPatternAdapter.COLOR_3 ) );
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
            ((SampleMapView)mfl_sampleMap).setMapName( editable.toString() );
        }

    }

    /*
     * コンストラクタ
     */
    public CreateMapPageAdapter(List<Integer> layoutIdList, View fl_sampleMap) {
        mData            = layoutIdList;
        mfl_sampleMap    = fl_sampleMap;
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
