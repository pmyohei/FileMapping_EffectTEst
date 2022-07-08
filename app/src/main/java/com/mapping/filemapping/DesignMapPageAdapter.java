package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

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
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private MaterialCardView mcv_gradationClear;
        private ColorSelectionView csv_mapGradation;
        private MaterialCardView iv_TlBr;
        private MaterialCardView iv_TopBottom;
        private MaterialCardView iv_TrBl;
        private MaterialCardView iv_LeftRight;
        private MaterialCardView iv_RightLeft;
        private MaterialCardView iv_BlTr;
        private MaterialCardView iv_BottomTop;
        private MaterialCardView iv_BrTl;
        //エフェクト
        private TextView tv_star;
        private TextView tv_flower;
        private TextView tv_sakura;
        private TextView tv_spakcle;
        private TextView tv_dia;
        private TextView tv_dot;
        private TextView tv_circle;
        private TextView tv_heart;
        //ノードデザイン
        private ColorSelectionView csv_background;
        private ColorSelectionView csv_text;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;
        private TextView tv_fontjapanese;
        private ImageView iv_circle;
        private ImageView iv_circleLittle;
        private ImageView iv_squareRounded;
        private ImageView iv_square;
        private ImageView iv_octagon;
        private ImageView iv_octagonRounded;
        private ImageView iv_dia;
        private ImageView iv_diaSemi;
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
                    //マップ
                    csv_map = itemView.findViewById(R.id.csv_mapMonoColor);
                    csv_mapGradation = itemView.findViewById(R.id.csv_mapGradationColor);
                    mcv_gradationClear = itemView.findViewById(R.id.mcv_gradationClear);
                    iv_TlBr = itemView.findViewById(R.id.iv_TlBr);
                    iv_TopBottom = itemView.findViewById(R.id.iv_TopBottom);
                    iv_TrBl = itemView.findViewById(R.id.iv_TrBl);
                    iv_LeftRight = itemView.findViewById(R.id.iv_LeftRight);
                    iv_RightLeft = itemView.findViewById(R.id.iv_RightLeft);
                    iv_BlTr = itemView.findViewById(R.id.iv_BlTr);
                    iv_BottomTop = itemView.findViewById(R.id.iv_BottomTop);
                    iv_BrTl = itemView.findViewById(R.id.iv_BrTl);
                    break;

                case 1:
                    //tmp
                    tv_star = itemView.findViewById(R.id.tv_star);
                    tv_flower = itemView.findViewById(R.id.tv_flower);
                    tv_sakura = itemView.findViewById(R.id.tv_sakura);
                    tv_spakcle = itemView.findViewById(R.id.tv_spakcle);
                    tv_dia = itemView.findViewById(R.id.tv_dia);
                    tv_dot = itemView.findViewById(R.id.tv_dot);
                    tv_circle = itemView.findViewById(R.id.tv_circle);
                    tv_heart = itemView.findViewById(R.id.tv_heart);
                    break;

                case 2:
                    //フォント
                    rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                    rv_fontjapanese = itemView.findViewById(R.id.rv_fontJapanese);
                    tv_fontjapanese = itemView.findViewById(R.id.tv_fontJapanese);
                    break;

                case 3:
                    //ノードサイズ
                    tv_titel_nodeSize = itemView.findViewById(R.id.tv_titel_nodeSize);
                    sbv_nodeSize = itemView.findViewById(R.id.sbv_nodeSize);
                    //ラインサイズ
                    sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
                    //枠線サイズ
                    sbv_borderSize = itemView.findViewById(R.id.sbv_borderSize);
                    break;

                case 4:
                    //ノード形
                    iv_circle = itemView.findViewById(R.id.iv_TlBr);
                    iv_circleLittle = itemView.findViewById(R.id.iv_TopBottom);
                    iv_squareRounded = itemView.findViewById(R.id.iv_TrBl);
                    iv_square = itemView.findViewById(R.id.iv_BlTr);
                    iv_octagon = itemView.findViewById(R.id.iv_BottomTop);
                    iv_octagonRounded = itemView.findViewById(R.id.iv_BrTl);
                    iv_dia = itemView.findViewById(R.id.iv_LeftRight);
                    iv_diaSemi = itemView.findViewById(R.id.iv_RightLeft);
                    break;

                case 5:
                    //テキスト色
                    csv_text = itemView.findViewById(R.id.csv_text);
                    break;

                case 6:
                    //背景色
                    csv_background = itemView.findViewById(R.id.csv_background);
                    break;

                case 7:
                    //枠線色
                    csv_border = itemView.findViewById(R.id.csv_border);
                    break;

                case 8:
                    //影
                    sw_shadow = itemView.findViewById(R.id.sw_shadow);
                    csv_shadow = itemView.findViewById(R.id.csv_shadow);
                    break;

                case 9:
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
                    setMapColorPage();
                    break;
                case 1:
                    setMapEffectPage();
                    break;
                case 2:
                    setFontPage();
                    break;
                case 3:
                    setLinePage();
                    break;
                case 4:
                    setNodeShapePage();
                    break;
                case 5:
                    setTextPage();
                    break;
                case 6:
                    setBackgroundPage();
                    break;
                case 7:
                    setBorderPage();
                    break;
                case 8:
                    setShadowPage();
                    break;
                case 9:
                    setLineColorPage();
                    break;
            }
        }

        /*
         * ページ設定：マップ色
         */
        public void setMapColorPage() {
            //--------------------------------------
            // マップ色
            //--------------------------------------
            csv_map.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_MAP, mv_map );
            csv_mapGradation.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_MAP_GRADATION, mv_map );

            //--------------------------------------
            // グラデーションOnOff
            //--------------------------------------
            mcv_gradationClear.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         //※設定色は適当な値（本値は設定されない）
                         ((MapActivity)mv_map.getContext()).setMapColor( MapActivity.MAP_COLOR_PTN_GRADATION_OFF, "#000000", MapTable.GRNDIR_KEEPING );
                     }
                 }
            );

            //--------------------------------------
            // グラデーション方向
            //--------------------------------------
            iv_TlBr.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_TL_BR ));
            iv_TopBottom.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_TOP_BOTTOM ));
            iv_TrBl.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_TR_BL ));
            iv_LeftRight.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_LEFT_RIGHT ));
            iv_RightLeft.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_RIGHT_LEFT ));
            iv_BlTr.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_BL_TR ));
            iv_BottomTop.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_BOTTOM_TOP ));
            iv_BrTl.setOnClickListener( new ClickGradationDirectionImage( MapTable.GRNDIR_BR_TL ));
        }

        /*
         * ページ設定：マップエフェクト
         */
        public void setMapEffectPage() {
            //エフェクト追加先のマップビューを取得
            FrameLayout fl_map = mv_map.findViewById(R.id.fl_map);
            final EffectManager effectManager = new EffectManager( (ViewGroup)fl_map );

            tv_star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.STAR, Paint.Style.FILL, MapTable.SPIN);
                    effectManager.restartEffect();
                }
            });

            tv_flower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.FLOWER, Paint.Style.FILL, MapTable.SPIN);
                    effectManager.restartEffect();
                }
            });

            tv_sakura.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.SAKURA, Paint.Style.FILL, MapTable.SPIN);
                    effectManager.restartEffect();
                }
            });

            tv_spakcle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.SPARCLE_CENTRAL_CIRCLE, Paint.Style.FILL, MapTable.BLINK);
                    effectManager.setEffectVolume( 100 );
                    effectManager.restartEffect();
                }
            });

            tv_dia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.DIA, Paint.Style.FILL, MapTable.BLINK);
                    effectManager.restartEffect();
                }
            });

            tv_dot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.DOT, Paint.Style.FILL, MapTable.BLINK);
                    effectManager.restartEffect();
                }
            });

            tv_circle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.CIRCLE, Paint.Style.FILL, MapTable.BLINK);
                    effectManager.restartEffect();
                }
            });

            tv_heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    effectManager.setEffectAttr( MapTable.HEART_INFLATED, Paint.Style.STROKE, MapTable.STROKE_GRADATION_ROTATE);
                    effectManager.setEffectVolume( 20 );
                    effectManager.restartEffect();
                }
            });

        }

        /*
         * ページ設定：フォント
         */
        public void setFontPage() {

            Context context = mv_map.getContext();

            //フォントアダプタ
            //レイアウトマネージャの生成・設定（横スクロール）
            LinearLayoutManager ll_manager = new LinearLayoutManager(context);
            ll_manager.setOrientation(LinearLayoutManager.HORIZONTAL);
            rv_fontAlphabet.setLayoutManager(ll_manager);

            //フォントリソースリストを取得
            //List<Typeface> alphaFonts = ResourceManager.getAlphabetFonts( context );
            List<String> alphaFonts = ResourceManager.getAlphabetFonts();
            //RecyclerViewにアダプタを設定
            rv_fontAlphabet.setAdapter( new FontAdapter( alphaFonts, null, mv_map, FontAdapter.ALPHABET ) );
            //スクロールリスナー（ViewPager2のタブ切り替えを制御）
            ViewPager2 vp2_design = mv_map.getRootView().findViewById(R.id.vp2_design);
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
                rv_fontjapanese.setAdapter( new FontAdapter( jpFonts, null, mv_map, FontAdapter.JAPANESE ) );
                //スクロールリスナー（ViewPager2のタブ切り替えを制御）
                rv_fontjapanese.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2_design ) );

            } else {
                //日本語以外なら、非表示
                tv_fontjapanese.setVisibility( View.GONE );
                rv_fontjapanese.setVisibility( View.GONE );
            }
        }

        /*
         * ページ設定：ラインサイズ
         */
        public void setLinePage() {
            //ラインサイズ
            sbv_lineSize.setLineSizeSeekbar( null );
            //枠サイズのシークバー
            sbv_borderSize.setBorderSizeSeekbar( null );

            //ノードサイズは設定対象外のため、非表示
            tv_titel_nodeSize.setVisibility( View.GONE );
            sbv_nodeSize.setVisibility( View.GONE );
        }

        /*
         * ページ設定：ノードの形状
         */
        private void setNodeShapePage() {
            //ノード形
            iv_circle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE) );
            iv_circleLittle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE_LITTLE) );
            iv_squareRounded.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE_ROUNDED) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );
            iv_octagon.setOnClickListener( new ClickShapeImage(NodeTable.OCTAGON) );
            iv_octagonRounded.setOnClickListener( new ClickShapeImage(NodeTable.OCTAGON_ROUNDED) );
            iv_dia.setOnClickListener( new ClickShapeImage(NodeTable.DIA) );
            iv_diaSemi.setOnClickListener( new ClickShapeImage(NodeTable.DIA_SEMI) );
        }

        /*
         * ページ設定：テキスト色
         */
        private void setTextPage() {
            //テキスト色
            csv_text.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_TEXT, mv_map );
        }

        /*
         * ページ設定：ノード色
         */
        private void setBackgroundPage() {
            //背景色
            csv_background.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_NODE_BACKGROUNG, mv_map );
        }

        /*
         * ページ設定：ノード境界線
         */
        private void setBorderPage() {
            //枠色
            csv_border.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_BORDER, mv_map );
        }

        /*
         * ページ設定：ノードの影
         */
        private void setShadowPage() {
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
                    //状態反転
                    map.setShadow( b );
                }
            });
        }

        /*
         * ページ設定：ライン色
         */
        private void setLineColorPage() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.MAP, ColorSelectionView.COLOR_LINE, mv_map );
        }

        /*
         * ノード形状イメージリスナー
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


        /*
         * グラデーション方向クリックリスナー
         */
        private class ClickGradationDirectionImage implements View.OnClickListener {
            //グラデーション方向
            private final int mDirection;

            public ClickGradationDirectionImage(int direction ){
                mDirection = direction;
            }

            @Override
            public void onClick(View view) {
                //※設定色は適当な値（本色は設定されない）
                ((MapActivity)mv_map.getContext()).setMapColor( MapActivity.MAP_COLOR_PTN_GRADATION_DIR, "#000000", mDirection );
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
