package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;
import java.util.Locale;

public class DesignNodePageAdapter extends RecyclerView.Adapter<DesignNodePageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //設定対象ノードビュー
    private final BaseNode        mv_node;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //設定対象ノードビュー
        private final BaseNode mv_node;

        /*--- ノードテキスト ---*/
        private EditText et_nodeName;
        private ColorSelectionView csv_text;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;
        private TextView tv_fontjapanese;
        /*--- ノードデザイン ---*/
        private ColorSelectionView csv_background;
        private ImageView iv_circle;
        private ImageView iv_squareRounded;
        private SeekbarView  sbv_nodeSize;
        private TextView tv_titel_nodeSize;
        private ColorSelectionView csv_border;
        private SeekbarView  sbv_borderSize;
        private ColorSelectionView csv_shadow;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private Switch sw_shadow;
        /*--- ライン ---*/
        private ColorSelectionView csv_line;
        private TextView tv_titel_lineSize;
        private SeekbarView  sbv_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node) {
            super(itemView);

            mv_node = node;

            switch (position) {
                case 0:
                    //ノード名
                    et_nodeName = itemView.findViewById(R.id.et_nodeName);
                    break;

                case 1:
                    //フォント
                    rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                    rv_fontjapanese = itemView.findViewById(R.id.rv_fontJapanese);
                    tv_fontjapanese = itemView.findViewById(R.id.tv_fontJapanese);
                    break;

                case 2:
                    //ノードサイズ
                    tv_titel_nodeSize = itemView.findViewById(R.id.tv_titel_nodeSize);
                    sbv_nodeSize = itemView.findViewById(R.id.sbv_nodeSize);
                    //ラインサイズ
                    tv_titel_lineSize = itemView.findViewById(R.id.tv_titel_lineSize);
                    sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
                    //枠線サイズ
                    sbv_borderSize = itemView.findViewById(R.id.sbv_borderSize);
                    break;

                case 3:
                    //ノード形
                    iv_circle = itemView.findViewById(R.id.iv_circle);
                    iv_squareRounded = itemView.findViewById(R.id.iv_squareRounded);
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
                    //影色
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
        private void setPage(int position) {

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
        private void setPage0() {
            //文字入力リスナーを設定
            et_nodeName.addTextChangedListener(this);
            //ノード名を設定
            et_nodeName.setText( mv_node.getNode().getNodeName() );
        }

        /*
         * ページ設定（１）
         */
        private void setPage1() {
            Context context = mv_node.getContext();

            //フォントアダプタ
            //レイアウトマネージャの生成・設定（横スクロール）
            LinearLayoutManager ll_manager = new LinearLayoutManager(context);
            ll_manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv_fontAlphabet.setLayoutManager(ll_manager);

            //フォントリソースリストを取得
            //List<Typeface> alphaFonts = ResourceManager.getAlphabetFonts( context );
            List<String> alphaFonts = ResourceManager.getAlphabetFonts();
            //RecyclerViewにアダプタを設定
            rv_fontAlphabet.setAdapter( new FontAdapter( alphaFonts, mv_node, null, FontAdapter.ALPHABET ) );
            //スクロールリスナー（ViewPager2のタブ切り替えを制御）
            ViewPager2 vp2_design = mv_node.getRootView().findViewById(R.id.vp2_design);
            rv_fontAlphabet.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2_design ) );

            //日本語設定の場合のみ、日本語フォントも設定
            Locale locale = Locale.getDefault();
            if(locale.equals(Locale.JAPAN)||locale.equals(Locale.JAPANESE)){
                LinearLayoutManager ll_manager2 = new LinearLayoutManager(context);
                ll_manager2.setOrientation(LinearLayoutManager.HORIZONTAL);
                rv_fontjapanese.setLayoutManager(ll_manager2);

                //フォントリソースリストを取得
                //List<Typeface> jpFonts = ResourceManager.getJapaneseFonts( context );
                List<String> jpFonts = ResourceManager.getJapaneseFonts();
                //RecyclerViewにアダプタを設定
                rv_fontjapanese.setAdapter( new FontAdapter( jpFonts, mv_node, null, FontAdapter.JAPANESE ) );
                //スクロールリスナー（ViewPager2のタブ切り替えを制御）
                rv_fontjapanese.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2_design ) );

            } else {
                //日本語以外なら、非表示
                tv_fontjapanese.setVisibility( View.GONE );
                rv_fontjapanese.setVisibility( View.GONE );
            }
        }

        /*
         * ページ設定（２）
         */
        private void setPage2() {

            //枠サイズ
            sbv_borderSize.setBorderSizeSeekbar( mv_node );

            //ノードサイズ
            //★リリース後、全対象にする
            if( mv_node.getNode().getKind() == NodeTable.NODE_KIND_PICTURE ){
                //ピクチャノードのみ設定
                sbv_nodeSize.setNodeSizeSeekbar( mv_node );
            } else {
                //ルートノード、ノード
                tv_titel_nodeSize.setVisibility( View.GONE );
                sbv_nodeSize.setVisibility( View.GONE );
            }

            //ラインサイズ
            if( mv_node.getNode().getKind() == NodeTable.NODE_KIND_ROOT ){
                //ルートノード
                tv_titel_lineSize.setVisibility( View.GONE );
                sbv_lineSize.setVisibility( View.GONE );
            } else {
                //ノード、ピクチャノード
                sbv_lineSize.setLineSizeSeekbar( mv_node );
            }

        }

        /*
         * ページ設定（３）
         */
        private void setPage3() {
            //ノード形
            iv_circle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE) );
            iv_squareRounded.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE_ROUNDED) );
        }

        /*
         * ページ設定（４）
         */
        private void setPage4() {
            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_TEXT, mv_node );
        }

        /*
         * ページ設定（５）
         */
        private void setPage5() {
            //背景色
            csv_background.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BACKGROUNG, mv_node );
        }

        /*
         * ページ設定（６）
         */
        private void setPage6() {
            //枠色
            csv_border.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BORDER, mv_node );
        }

        /*
         * ページ設定（７）
         */
        private void setPage7() {
            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_SHADOW, mv_node );

            //影のon/off
            sw_shadow.setChecked( mv_node.isShadow() );
            sw_shadow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //影の状態を反転
                    mv_node.switchShadow();
                }
            });
        }

        /*
         * ページ設定（８）
         */
        private void setPage8() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_LINE, mv_node );
        }

        /*--  TextChangedListener  --*/
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
            mv_node.addLayoutConfirmedListener();
        }
        /*--  --*/


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
    public DesignNodePageAdapter(List<Integer> layoutIdList, View v_node) {
        mData = layoutIdList;
        mv_node =  (BaseNode)v_node;
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

        return new GuideViewHolder(view, position, mv_node);
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
