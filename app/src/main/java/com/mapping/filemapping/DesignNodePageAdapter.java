package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DesignNodePageAdapter extends RecyclerView.Adapter<DesignNodePageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //設定対象ノードビュー
    private final BaseNode        mv_node;
    //FragmentManager
    private final FragmentManager mFragmentManager;
    //ViewPager2
    private final ViewPager2 mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //カラー指定
        private final int COLOR_BACKGROUNG = 0;
        private final int COLOR_TEXT = 1;
        private final int COLOR_BORDER = 2;
        private final int COLOR_SHADOW = 3;
        private final int COLOR_LINE = 4;

        //設定対象ノードビュー
        private final BaseNode        mv_node;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        /*--- ノードテキスト ---*/
        private EditText et_nodeName;
        //private TextView tv_txColorCode;
        //private TextView tv_txColorGraphic;
        private ColorSelectionView csv_text;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;

        /*--- ノードデザイン ---*/
        private TextView tv_bgColorCode;
        private TextView tv_bgColorGraphic;
        private ColorSelectionView csv_background;
        private ImageView iv_circle;
        private ImageView iv_square;
        private SeekBar  sb_nodeSize;
        private TextView tv_borderColorCode;
        private TextView tv_borderColorGraphic;
        private ColorSelectionView csv_border;
        private RadioGroup rg_borderSize;
        private TextView tv_shadowColorCode;
        private TextView tv_shadowColorGraphic;
        private ColorSelectionView csv_shadow;

        /*--- ライン ---*/
        private TextView tv_lineColorCode;
        private TextView tv_lineColorGraphic;
        private ColorSelectionView csv_line;
        private RadioGroup rg_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mv_node = node;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            if (position == 0) {
                //ノード名
                et_nodeName = itemView.findViewById(R.id.et_nodeName);
                //テキスト色
                //tv_txColorCode    = itemView.findViewById(R.id.tv_txColorCode);
                //tv_txColorGraphic = itemView.findViewById(R.id.tv_txColorGraphic);
                csv_text = itemView.findViewById(R.id.csv_text);

                //フォント
                rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                rv_fontjapanese   = itemView.findViewById(R.id.rv_fontJapanese);

            } else if (position == 1) {
                //背景色
                //tv_bgColorCode    = itemView.findViewById(R.id.tv_bgColorCode);
                //tv_bgColorGraphic = itemView.findViewById(R.id.tv_bgColorGraphic);
                csv_background = itemView.findViewById(R.id.csv_background);
                //ノード形
                iv_circle    = itemView.findViewById(R.id.iv_circle);
                iv_square    = itemView.findViewById(R.id.iv_square);
                //ノードサイズ
                sb_nodeSize       = itemView.findViewById(R.id.sb_nodeSize);
                //枠線色
                //tv_borderColorCode    = itemView.findViewById(R.id.tv_borderColorCode);
                //tv_borderColorGraphic = itemView.findViewById(R.id.tv_borderColorGraphic);
                csv_border = itemView.findViewById(R.id.csv_border);
                //枠線サイズ
                rg_borderSize = itemView.findViewById(R.id.rg_borderSize);
                //影色
                //tv_shadowColorCode    = itemView.findViewById(R.id.tv_shadowColorCode);
                //tv_shadowColorGraphic = itemView.findViewById(R.id.tv_shadowColorGraphic);
                csv_shadow = itemView.findViewById(R.id.csv_shadow);

            } else if (position == 2) {
                //色
                //tv_lineColorCode    = itemView.findViewById(R.id.tv_lineColorCode);
                //tv_lineColorGraphic = itemView.findViewById(R.id.tv_lineColorGraphic);
                csv_line = itemView.findViewById(R.id.csv_line);
                //サイズ
                rg_lineSize = itemView.findViewById(R.id.rg_lineSize);
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
            //文字入力リスナーを設定
            et_nodeName.addTextChangedListener(this);

            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_TEXT, mv_node );

            Context context = mv_node.getContext();

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
            rv_fontAlphabet.setAdapter( new FontAdapter( alphaFonts, mv_node, null, FontAdapter.ALPHABET ) );
            rv_fontjapanese.setAdapter( new FontAdapter( jpFonts, mv_node, null, FontAdapter.JAPANESE ) );

            //スクロールリスナー（ViewPager2のタブ切り替えを制御）
            rv_fontAlphabet.addOnItemTouchListener( new Vp2OnItemTouchListener( mvp2 ) );
            rv_fontjapanese.addOnItemTouchListener( new Vp2OnItemTouchListener( mvp2 ) );
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //背景色
            csv_background.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BACKGROUNG, mv_node );

            //ノード形
            iv_circle.setOnClickListener(new ClickShapeImage(NodeTable.CIRCLE) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );

            //現在の位置
            float scaleSize = mv_node.getScaleSize();
            int progress = (int)((scaleSize / NodeTable.NODE_MAX_SIZE) * 100f);

            Log.i("sb_nodeSize", "scaleSize=" + scaleSize + " progress=" + progress);

            //進捗バー最大値
            final int PROGRESS_MAX = 100;

            //増分
            int add = (NodeTable.NODE_MAX_SIZE - NodeTable.NODE_MIN_SIZE) / PROGRESS_MAX;

            //ノードサイズ
            sb_nodeSize.setMax( PROGRESS_MAX );
            sb_nodeSize.setProgress(progress);
            sb_nodeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekbar) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekbar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

                    //設定比率を計算
                    float setSize = NodeTable.NODE_MIN_SIZE + ( i * add);
                    float setRatio = setSize / mv_node.getWidth();

                    mv_node.setScale( setRatio );

                    Log.i("sb_nodeSize", "設定比率=" + setRatio + " 目標サイズ=" + setSize);
                    Log.i("sb_nodeSize", "結果サイズ=" + mv_node.getScaleSize());
                }
            });

            //枠色
            csv_border.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BORDER, mv_node );

            //枠サイズ
            //★UIをラジオボタンにするなら、ライン側と統一させる
            rg_borderSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    //選択されたindexを取得
                    RadioButton rb = radioGroup.findViewById( checkedId );
                    int idx = radioGroup.indexOfChild( rb );

                    //ラインサイズ設定
                    Log.i("NodeDesign", "枠サイズ設定値=" + (idx + 1));
                    mv_node.setBorderSize( idx + 1 );
                }
            });

            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_SHADOW, mv_node );


        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {

            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_LINE, mv_node );

            //ラインサイズ
            rg_lineSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    //選択されたindexを取得
                    RadioButton rb = radioGroup.findViewById( checkedId );
                    int idx = radioGroup.indexOfChild( rb );

                    //ラインサイズ設定
                    Log.i("NodeDesign", "ラインサイズ設定値=" + (idx + 1));
                    ((ChildNode)mv_node).setLineSize( idx + 1 );
                }
            });

        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //ノードに反映
            mv_node.setNodeName( charSequence.toString() );
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //ノードに反映
            mv_node.setNodeName( editable.toString() );
            mv_node.addOnNodeGlobalLayoutListener();
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
                //ノードに形状を設定
                mv_node.setNodeShape( mShapeKind );
            }
        }
    }

    /*
     * コンストラクタ
     */
    public DesignNodePageAdapter(List<Integer> layoutIdList, View v_node, FragmentManager fragmentManager, ViewPager2 vp) {
        mData            = layoutIdList;
        mv_node          =  (BaseNode)v_node;
        mFragmentManager = fragmentManager;
        mvp2             = vp;
    }

    /*
     * ここで返した値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //レイアウトIDを返す
        return position;
        //return mData.get(position);
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

        return new GuideViewHolder(view, position, mv_node, mFragmentManager, mvp2);
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
