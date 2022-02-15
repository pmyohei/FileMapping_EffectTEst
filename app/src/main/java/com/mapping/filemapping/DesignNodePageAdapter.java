package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

import com.google.android.material.card.MaterialCardView;

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

        //設定対象ノードビュー
        private final BaseNode        mv_node;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        /*--- ノードテキスト ---*/
        private EditText et_nodeName;
        private ColorSelectionView csv_text;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;

        /*--- ノードデザイン ---*/
        private ColorSelectionView csv_background;
        private ImageView iv_circle;
        private ImageView iv_square;
        private SeekbarView  sbv_nodeSize;
        private ColorSelectionView csv_border;
        private SeekbarView  sbv_borderSize;
        private ColorSelectionView csv_shadow;

        /*--- ライン ---*/
        private ColorSelectionView csv_line;
        private SeekbarView  sbv_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mv_node = node;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            switch (position) {
                case 0:
                    //ノード名
                    et_nodeName = itemView.findViewById(R.id.et_nodeName);
                    break;

                case 1:
                    //フォント
                    rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                    rv_fontjapanese = itemView.findViewById(R.id.rv_fontJapanese);
                    break;

                case 2:
                    //ノードサイズ
                    sbv_nodeSize = itemView.findViewById(R.id.sbv_nodeSize);
                    //ラインサイズ
                    sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
                    //枠線サイズ
                    sbv_borderSize = itemView.findViewById(R.id.sbv_borderSize);
                    break;

                case 3:
                    //テキスト色
                    csv_text = itemView.findViewById(R.id.csv_text);
                    break;

                case 4:
                    //背景色
                    csv_background = itemView.findViewById(R.id.csv_background);
                    break;

                case 5:
                    //枠線色
                    csv_border = itemView.findViewById(R.id.csv_border);
                    break;

                case 6:
                    //ラインの色
                    csv_line = itemView.findViewById(R.id.csv_line);
                    break;

                case 7:
                    //影色
                    csv_shadow = itemView.findViewById(R.id.csv_shadow);
                    break;

                case 8:
                    //ノード形
                    iv_circle = itemView.findViewById(R.id.iv_circle);
                    iv_square = itemView.findViewById(R.id.iv_square);
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
            //文字入力リスナーを設定
            et_nodeName.addTextChangedListener(this);
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {
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
         * ページ設定（２）
         */
        public void setPage2() {
            //ノードサイズ
            sbv_nodeSize.setNodeSizeSeekbar( mv_node );
            //枠サイズ
            sbv_borderSize.setBorderSizeSeekbar( mv_node );

            //ルートノード以外はラインサイズも設定対象
            if( mv_node.getNode().getKind() != NodeTable.NODE_KIND_ROOT ){
                //ラインサイズ
                sbv_lineSize.setLineSizeSeekbar( mv_node );
            }
        }

        /*
         * ページ設定（３）
         */
        public void setPage3() {
            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_TEXT, mv_node );
        }

        /*
         * ページ設定（4）
         */
        public void setPage4() {
            //背景色
            csv_background.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BACKGROUNG, mv_node );
        }

        /*
         * ページ設定（5）
         */
        public void setPage5() {
            //枠色
            csv_border.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BORDER, mv_node );
        }

        /*
         * ページ設定（6）
         */
        public void setPage6() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_LINE, mv_node );
        }
        /*
         * ページ設定（7）
         */
        public void setPage7() {
            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_SHADOW, mv_node );

            //影のon/off

        }
        /*
         * ページ設定（8）
         */
        public void setPage8() {
            //ノード形
            iv_circle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );
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
