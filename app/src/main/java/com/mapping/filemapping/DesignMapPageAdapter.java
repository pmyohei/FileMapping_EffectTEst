package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DesignMapPageAdapter extends RecyclerView.Adapter<DesignMapPageAdapter.PageViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //マップ
    private View                  mv_map;
    //FragmentManager
    private final FragmentManager mFragmentManager;
    //ViewPager2
    private final ViewPager2 mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class PageViewHolder extends RecyclerView.ViewHolder {

        //マップ
        private final View            mv_map;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

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
        private SeekbarView  sbv_borderSize;
        private ColorSelectionView csv_shadow;

        //ラインデザイン
        private ColorSelectionView csv_line;
        private SeekbarView  sbv_lineSize;


        /*
         * コンストラクタ
         */
        public PageViewHolder(View itemView, int position, View v_map, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mv_map           = v_map;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            if (position == 0) {
                //マップ色
                csv_map = itemView.findViewById(R.id.csv_map);

            } else if (position == 1) {
                //背景色
                csv_background = itemView.findViewById(R.id.csv_background);
                //テキスト色
                csv_text = itemView.findViewById(R.id.csv_text);
                //フォント
                rv_fontAlphabet   = itemView.findViewById(R.id.rv_fontAlphabet);
                rv_fontjapanese   = itemView.findViewById(R.id.rv_fontJapanese);
                //ノード形
                iv_circle    = itemView.findViewById(R.id.iv_circle);
                iv_square    = itemView.findViewById(R.id.iv_square);
                //枠線色
                csv_border = itemView.findViewById(R.id.csv_border);
                //枠線サイズ
                //rg_borderSize = itemView.findViewById(R.id.rg_borderSize);
                sbv_borderSize       = itemView.findViewById(R.id.sbv_borderSize);
                //影色
                csv_shadow = itemView.findViewById(R.id.csv_shadow);

            } else if (position == 2) {
                //色
                csv_line = itemView.findViewById(R.id.csv_line);
                //サイズ
                //rg_lineSize = itemView.findViewById(R.id.rg_lineSize);
                sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            if (position == 0) {
                setPage0();

            } else if (position == 1) {
                setPage1();

            } else if (position == 2) {
                setPage2();
            }

        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {

            //背景色-カラーコード
            csv_map.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_MAP, mv_map );
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //背景色
            csv_background.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_BACKGROUNG, mv_map );

            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_TEXT, mv_map );

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
            rv_fontAlphabet.addOnItemTouchListener( new Vp2OnItemTouchListener( mvp2 ) );
            rv_fontjapanese.addOnItemTouchListener( new Vp2OnItemTouchListener( mvp2 ) );

            //ノード形
            iv_circle.setOnClickListener(new ClickShapeImage(NodeTable.CIRCLE) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );

            //枠色
            csv_border.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_BORDER, mv_map );

            //枠サイズのシークバー
            sbv_borderSize.setBorderSizeSeekbar( null );

            //★UIをラジオボタンにするなら、ライン側と統一させる
/*            rg_borderSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    //選択されたindexを取得
                    RadioButton rb = radioGroup.findViewById( checkedId );
                    int idx = radioGroup.indexOfChild( rb );

                    //マップ共通データ
                    MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                    NodeArrayList<NodeTable> nodes = commonData.getNodes();

                    nodes.setAllNodeBorderSize( idx + 1 );
                }
            });*/

            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_SHADOW, mv_map );

        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {

            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_LINE, mv_map );

            //ラインサイズ
            sbv_lineSize.setLineSizeSeekbar( null );

/*            rg_lineSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    //選択されたindexを取得
                    RadioButton rb = radioGroup.findViewById( checkedId );
                    int idx = radioGroup.indexOfChild( rb );

                    //マップ共通データ
                    MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                    NodeArrayList<NodeTable> nodes = commonData.getNodes();

                    nodes.setAllNodeLineSize( idx + 1 );
                }
            });*/
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
    public DesignMapPageAdapter(List<Integer> layoutIdList, View v_map, FragmentManager fragmentManager, ViewPager2 vp2) {
        mData            = layoutIdList;
        mv_map           = v_map;
        mFragmentManager = fragmentManager;
        mvp2             = vp2;
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

        return new PageViewHolder(view, position, mv_map, mFragmentManager, mvp2);
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
