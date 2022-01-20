package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DesignMapPageAdapter extends RecyclerView.Adapter<DesignMapPageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //マップ
    private View     mv_map;
    //FragmentManager
    private final FragmentManager mFragmentManager;
    //ViewPager2
    private final ViewPager2 mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class GuideViewHolder extends RecyclerView.ViewHolder {

        //カラー指定
        private final int COLOR_BACKGROUNG = 0;
        private final int COLOR_TEXT = 1;
        private final int COLOR_BORDER = 2;
        private final int COLOR_SHADOW = 3;
        private final int COLOR_LINE = 4;
        private final int COLOR_MAP = 5;

        //マップ
        private final View            mv_map;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        //マップデザイン
        private TextView tv_bgMapColorCode;
        private TextView tv_bgMapColorGraphic;

        //ノードデザイン
        private TextView tv_bgColorCode;
        private TextView tv_bgColorGraphic;
        private TextView tv_txColorCode;
        private TextView tv_txColorGraphic;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;
        private ImageView iv_circle;
        private ImageView iv_square;
        private TextView tv_borderColorCode;
        private TextView tv_borderColorGraphic;
        private RadioGroup rg_borderSize;
        private TextView tv_shadowColorCode;
        private TextView tv_shadowColorGraphic;

        //ラインデザイン
        private TextView tv_lineColorCode;
        private TextView tv_lineColorGraphic;
        private RadioGroup rg_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, View v_map, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mv_map           = v_map;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            if (position == 0) {
                //マップ色
                tv_bgMapColorCode    = itemView.findViewById(R.id.tv_bgMapColorCode);
                tv_bgMapColorGraphic = itemView.findViewById(R.id.tv_bgMapColorGraphic);

            } else if (position == 1) {
                //背景色
                tv_bgColorCode    = itemView.findViewById(R.id.tv_bgColorCode);
                tv_bgColorGraphic = itemView.findViewById(R.id.tv_bgColorGraphic);
                //テキスト色
                tv_txColorCode    = itemView.findViewById(R.id.tv_txColorCode);
                tv_txColorGraphic = itemView.findViewById(R.id.tv_txColorGraphic);
                //フォント
                rv_fontAlphabet   = itemView.findViewById(R.id.rv_fontAlphabet);
                rv_fontjapanese   = itemView.findViewById(R.id.rv_fontJapanese);
                //ノード形
                iv_circle    = itemView.findViewById(R.id.iv_circle);
                iv_square    = itemView.findViewById(R.id.iv_square);
                //枠線色
                tv_borderColorCode    = itemView.findViewById(R.id.tv_borderColorCode);
                tv_borderColorGraphic = itemView.findViewById(R.id.tv_borderColorGraphic);
                //枠線サイズ
                rg_borderSize = itemView.findViewById(R.id.rg_borderSize);
                //影色
                tv_shadowColorCode    = itemView.findViewById(R.id.tv_shadowColorCode);
                tv_shadowColorGraphic = itemView.findViewById(R.id.tv_shadowColorGraphic);

            } else if (position == 2) {
                //色
                tv_lineColorCode    = itemView.findViewById(R.id.tv_lineColorCode);
                tv_lineColorGraphic = itemView.findViewById(R.id.tv_lineColorGraphic);
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

            //背景色-カラーコード
            tv_bgMapColorCode.setOnClickListener(new ClickColor( ClickColor.RGB, COLOR_MAP) );
            //背景色-カラーピッカー
            tv_bgMapColorGraphic.setOnClickListener( new ClickColor( ClickColor.PICKER, COLOR_MAP) );
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //背景色
            tv_bgColorCode.setOnClickListener(new ClickColor( ClickColor.RGB, COLOR_BACKGROUNG) );
            tv_bgColorGraphic.setOnClickListener( new ClickColor( ClickColor.PICKER, COLOR_BACKGROUNG) );

            //テキスト色
            tv_txColorCode.setOnClickListener(new ClickColor( ClickColor.RGB, COLOR_TEXT) );
            tv_txColorGraphic.setOnClickListener( new ClickColor( ClickColor.PICKER, COLOR_TEXT) );

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
            tv_borderColorCode.setOnClickListener(new ClickColor( ClickColor.RGB, COLOR_BORDER) );
            tv_borderColorGraphic.setOnClickListener( new ClickColor( ClickColor.PICKER, COLOR_BORDER) );

            //枠サイズ
            //★UIをラジオボタンにするなら、ライン側と統一させる
            rg_borderSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
            });

            //影色
            tv_shadowColorCode.setOnClickListener(new ClickColor( ClickColor.RGB, COLOR_SHADOW) );
            tv_shadowColorGraphic.setOnClickListener( new ClickColor( ClickColor.PICKER, COLOR_SHADOW) );

        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {

            //ラインカラー-カラーコード
            tv_lineColorCode.setOnClickListener(new ClickColor(ClickColor.RGB, COLOR_LINE) );
            //ラインカラー-カラーピッカー
            tv_lineColorGraphic.setOnClickListener( new ClickColor(ClickColor.PICKER, COLOR_LINE) );

            //ラインサイズ
            rg_lineSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
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
            });

        }

        /*
         * カラー入力ダイアログ表示リスナー
         */
        private class ClickColor implements View.OnClickListener {

            //カラー入力方法
            public static final int RGB = 0;
            public static final int PICKER = 1;

            //カラー入力方法
            private final int mInputKind;
            //設定対象
            private final int mSetTarget;

            /*
             * コンストラクタ
             */
            public ClickColor( int colorKind, int setTarget ){
                mInputKind = colorKind;
                mSetTarget = setTarget;
            }

            @Override
            public void onClick(View view) {

                //設定中の色を取得
                String currentColor = getCurrentColor();

                //ダイアログ
                ColorDialog dialog;
                if( mInputKind == RGB ){
                    dialog = new ColorCodeDialog( currentColor );
                } else {
                    dialog = new ColorPickerDialog( currentColor );
                }

                //OKボタンリスナー
                dialog.setOnPositiveClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //Log.i("Design", "カラーコード=" + ((EditText)view).getText());

                        //カラーコード文字列
                        ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
                        int colorInt = colorDrawable.getColor();
                        String code = "#" + Integer.toHexString( colorInt );

                        //マップ共通データ
                        MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        switch (mSetTarget){

                            case COLOR_MAP:
                                //マップ色
                                mv_map.setBackgroundColor( Color.parseColor(code) );
                                break;

                            case COLOR_BACKGROUNG:
                                //ノード背景色
                                nodes.setAllNodeBgColor( code );
                                break;

                            case COLOR_TEXT:
                                //ノードテキストカラー
                                nodes.setAllNodeTxColor( code );
                                break;

                            case COLOR_BORDER:
                                //枠線カラー
                                nodes.setAllNodeBorderColor( code );
                                break;

                            case COLOR_SHADOW:
                                //影カラー
                                nodes.setAllNodeShadowColor( code );
                                break;

                            case COLOR_LINE:
                                //ラインカラー
                                nodes.setAllNodeLineColor( code );
                                break;
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show(mFragmentManager, "ColorCode");
            }

            /*
             * 設定中のカラーを取得
             */
            private String getCurrentColor(){

                //マップ共通データ
                MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                NodeArrayList<NodeTable> nodes = commonData.getNodes();

                //ルートノード
                BaseNode rootNode = nodes.getRootNode().getNodeView();

                //色設定の対象毎に処理
                switch (mSetTarget){
                    case COLOR_MAP:
                        //マップ色
                        ColorDrawable colorDrawable = (ColorDrawable)mv_map.getBackground();
                        return "#" + Integer.toHexString( colorDrawable.getColor() );

                    case COLOR_BACKGROUNG:
                        //ノード背景色
                        return rootNode.getNodeBackgroundColor();

                    case COLOR_TEXT:
                        //ノードテキストカラー
                        return rootNode.getNodeTextColor();

                    case COLOR_BORDER:
                        //枠線カラー
                        return rootNode.getBorderColor();

                    case COLOR_SHADOW:
                        //影カラー
                        return rootNode.getShadowColor();

                    case COLOR_LINE:
                        //ラインカラー

                        //先頭ノード（ルートを除く）
                        ChildNode topChildNode = (ChildNode)nodes.getTopChildNode().getNodeView();
                        if(topChildNode == null){
                            return ResourceManager.NODE_INVALID_COLOR;
                        }

                        return topChildNode.getLineColor();

                    default:
                        //該当なし(フェールセーフ)
                        return ResourceManager.NODE_INVALID_COLOR;
                }
            }

        }

        /*
         *
         * カラーコード表示リスナー
         *
         */
/*
        private class ClickColorCode implements View.OnClickListener {

            private final int mColorKind;

            */
/*
             * コンストラクタ
             *//*

            public ClickColorCode( int kind ){
                mColorKind = kind;
            }

            @Override
            public void onClick(View view) {

                //ダイアログを生成
                ColorCodeDialog dialog = new ColorCodeDialog();

                //OKボタンリスナー
                dialog.setOnPositiveClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.i("Design", "カラーコード=" + ((EditText)view).getText());

                        //マップ共通データ
                        MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        //カラーコード文字列
                        String code = "#" + ((EditText)view).getText().toString();

                        if( mColorKind == COLOR_MAP){
                            //マップ色
                            mv_map.setBackgroundColor( Color.parseColor(code) );
                        } else if( mColorKind == COLOR_BACKGROUNG){
                            //ノード背景色
                            nodes.setAllNodeBgColor( code );
                        } else if ( mColorKind == COLOR_TEXT){
                            //ノードテキストカラー
                            nodes.setAllNodeTxColor( code );
                        } else {
                            //ラインカラー
                            nodes.setAllNodeLineColor( code );
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show(mFragmentManager, "ColorCode");
            }
        }
*/

        /*
         *
         * カラーピッカー表示リスナー
         *
         */
/*
        private class ClickColorPicker implements View.OnClickListener {

            private final int mColorKind;

            */
/*
             * コンストラクタ
             *//*

            public ClickColorPicker( int kind ){
                mColorKind = kind;
            }

            @Override
            public void onClick(View view) {

                //ダイアログを生成
                ColorPickerDialog dialog = new ColorPickerDialog();

                //OKボタンリスナー
                dialog.setOnPositiveClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //マップ共通データ
                        MapCommonData commonData = (MapCommonData)((Activity)mv_map.getContext()).getApplication();
                        NodeArrayList<NodeTable> nodes = commonData.getNodes();

                        //カラーコード文字列
                        String code = "#" + Integer.toHexString( ((ColorPickerView)view).getColor() );

                        if( mColorKind == COLOR_MAP){
                            //マップ色
                            mv_map.setBackgroundColor( Color.parseColor(code) );
                        } else if( mColorKind == COLOR_BACKGROUNG){
                            //ノード背景色
                            nodes.setAllNodeBgColor( code );
                        } else if ( mColorKind == COLOR_TEXT){
                            //ノードテキストカラー
                            nodes.setAllNodeTxColor( code );
                        } else {
                            //ラインカラー
                            nodes.setAllNodeLineColor( code );
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show(mFragmentManager, "ColorGraphic");
            }
        }
*/

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

        return new GuideViewHolder(view, position, mv_map, mFragmentManager, mvp2);
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
