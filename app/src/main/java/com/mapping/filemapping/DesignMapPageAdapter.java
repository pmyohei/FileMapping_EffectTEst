package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DesignMapPageAdapter extends RecyclerView.Adapter<DesignMapPageAdapter.PageViewHolder> {

    //フィールド変数
    private final List<Integer> mData;
    //マップ
    private final View mv_map;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class PageViewHolder extends RecyclerView.ViewHolder {

        //マップ
        private final View mv_map;
        //マップデザイン
        private ColorSelectionView csv_map;
        //ノードデザイン
        private ColorSelectionView csv_background;
        private ColorSelectionView csv_text;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;
        private ImageView iv_circle;
        private ImageView iv_square;
        private ColorSelectionView csv_border;
        private SeekbarView sbv_borderSize;
        private SeekbarView sbv_nodeSize;
        private TextView tv_titel_nodeSize;
        private ColorSelectionView csv_shadow;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private Switch sw_shadow;
        //ラインデザイン
        private ColorSelectionView csv_line;
        private SeekbarView  sbv_lineSize;


        /*
         * コンストラクタ
         */
        public PageViewHolder(View itemView, int position, View v_map) {
            super(itemView);
            mv_map = v_map;

            switch (position) {
                case 0:
                    //ノード名
                    csv_map = itemView.findViewById(R.id.csv_map);
                    break;

                case 1:
                    //フォント
                    rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                    rv_fontjapanese = itemView.findViewById(R.id.rv_fontJapanese);
                    break;

                case 2:
                    //ノードサイズ
                    tv_titel_nodeSize = itemView.findViewById(R.id.tv_titel_nodeSize);
                    sbv_nodeSize = itemView.findViewById(R.id.sbv_nodeSize);
                    //ラインサイズ
                    sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
                    //枠線サイズ
                    sbv_borderSize = itemView.findViewById(R.id.sbv_borderSize);
                    break;

                case 3:
                    //ノード形
                    iv_circle = itemView.findViewById(R.id.iv_circle);
                    iv_square = itemView.findViewById(R.id.iv_square);
                    break;

                case 4:
                    //テキスト色
                    csv_text = itemView.findViewById(R.id.csv_text);
                    break;

                case 5:
                    //背景色
                    csv_background = itemView.findViewById(R.id.csv_background);
                    break;

                case 6:
                    //枠線色
                    csv_border = itemView.findViewById(R.id.csv_border);
                    break;

                case 7:
                    //影
                    sw_shadow = itemView.findViewById(R.id.sw_shadow);
                    csv_shadow = itemView.findViewById(R.id.csv_shadow);
                    break;

                case 8:
                    //ラインの色
                    csv_line = itemView.findViewById(R.id.csv_line);
                    break;
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            switch (position){
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
                    setPage3();
                    break;
                case 4:
                    setPage4();
                    break;
                case 5:
                    setPage5();
                    break;
                case 6:
                    setPage6();
                    break;
                case 7:
                    setPage7();
                    break;
                case 8:
                    setPage8();
                    break;
            }
        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {
            //マップ色
            csv_map.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_MAP, mv_map );
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            Context context = mv_map.getContext();

            //フォントアダプタ
            //レイアウトマネージャの生成・設定（横スクロール）
            LinearLayoutManager ll_manager = new LinearLayoutManager(context);
            ll_manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            LinearLayoutManager ll_manager2 = new LinearLayoutManager(context);
            ll_manager2.setOrientation(LinearLayoutManager.HORIZONTAL);

            rv_fontAlphabet.setLayoutManager(ll_manager);
            rv_fontjapanese.setLayoutManager(ll_manager2);

            //フォントリソースリストを取得
            List<Typeface> alphaFonts = ResourceManager.getAlphabetFonts( context );
            List<Typeface> jpFonts = ResourceManager.getJapaneseFonts( context );

            //RecyclerViewにアダプタを設定
            rv_fontAlphabet.setAdapter( new FontAdapter( alphaFonts, null, mv_map, FontAdapter.ALPHABET ) );
            rv_fontjapanese.setAdapter( new FontAdapter( jpFonts, null, mv_map, FontAdapter.JAPANESE ) );

            //スクロールリスナー（ViewPager2のタブ切り替えを制御）
            ViewPager2 vp2_design = mv_map.getRootView().findViewById(R.id.vp2_design);
            rv_fontAlphabet.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2_design ) );
            rv_fontjapanese.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2_design ) );
        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {
            //ラインサイズ
            sbv_lineSize.setLineSizeSeekbar( null );
            //枠サイズのシークバー
            sbv_borderSize.setBorderSizeSeekbar( null );

            //ノードサイズは設定対象外のため、非表示
            tv_titel_nodeSize.setVisibility( View.GONE );
            sbv_nodeSize.setVisibility( View.GONE );
        }

        /*
         * ページ設定（３）
         */
        private void setPage3() {
            //ノード形
            iv_circle.setOnClickListener(new ClickShapeImage(NodeTable.CIRCLE) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );
        }

        /*
         * ページ設定（４）
         */
        private void setPage4() {
            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_TEXT, mv_map );
        }

        /*
         * ページ設定（５）
         */
        private void setPage5() {
            //背景色
            csv_background.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_BACKGROUNG, mv_map );
        }

        /*
         * ページ設定（６）
         */
        private void setPage6() {
            //枠色
            csv_border.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_BORDER, mv_map );
        }

        /*
         * ページ設定（７）
         */
        private void setPage7() {
            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_SHADOW, mv_map );

            //マップ共通データ
            MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
            MapTable map = commonData.getMap();

            //影のon/off
            sw_shadow.setChecked( map.isShadow() );
            sw_shadow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //全ノードの影の状態を反転
                    commonData.getNodes().setAllNodeShadow( b );
                }
            });
        }

        /*
         * ページ設定（８）
         */
        private void setPage8() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_LINE, mv_map );
        }

        /*
         *
         * ノード形状イメージリスナー
         *
         */
        private class ClickShapeImage implements View.OnClickListener {

            private final int mShapeKind;

            /*
             * コンストラクタ
             */
            public ClickShapeImage(int kind ){
                mShapeKind = kind;
            }

            @Override
            public void onClick(View view) {

                //マップ共通データ
                MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                NodeArrayList<NodeTable> nodes = commonData.getNodes();

                //全ノードに形状を設定
                nodes.setAllNodeShape( mShapeKind );
            }
        }
    }

    /*
     * コンストラクタ
     */
    public DesignMapPageAdapter(List<Integer> layoutIdList, View v_map) {
        mData = layoutIdList;
        mv_map = v_map;
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
    public PageViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        //レイアウトを生成
        LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
        View view = inflater.inflate(mData.get(position), viewGroup, false);

        return new PageViewHolder(view, position, mv_map);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull PageViewHolder viewHolder, final int i) {
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
