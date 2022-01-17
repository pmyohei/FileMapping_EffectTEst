package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.List;

public class MapDesignAdapter extends RecyclerView.Adapter<MapDesignAdapter.GuideViewHolder> {

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
        private final int NODE_BACKGROUNG_COLOR = 0;
        private final int NODE_TEXT_COLOR = 1;
        private final int LINE_COLOR = 2;
        private final int MAP_COLOR = 3;

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
        private LinearLayout ll_nodeSize;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;

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
                //ノードサイズ
                ll_nodeSize       = itemView.findViewById(R.id.ll_nodeSize);
                //フォント
                rv_fontAlphabet   = itemView.findViewById(R.id.rv_fontAlphabet);
                rv_fontjapanese   = itemView.findViewById(R.id.rv_fontJapanese);

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
            tv_bgMapColorCode.setOnClickListener(new ClickColorCode(MAP_COLOR) );
            //背景色-カラーピッカー
            tv_bgMapColorGraphic.setOnClickListener( new ClickColorPicker(MAP_COLOR) );
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //背景色-カラーコード
            tv_bgColorCode.setOnClickListener(new ClickColorCode(NODE_BACKGROUNG_COLOR) );
            //背景色-カラーピッカー
            tv_bgColorGraphic.setOnClickListener( new ClickColorPicker(NODE_BACKGROUNG_COLOR) );

            //テキスト色-カラーコード
            tv_txColorCode.setOnClickListener(new ClickColorCode(NODE_TEXT_COLOR) );
            //テキスト色-カラーピッカー
            tv_txColorGraphic.setOnClickListener( new ClickColorPicker(NODE_TEXT_COLOR) );

            //ノードサイズは設定対象外のため、非表示
            ll_nodeSize.setVisibility( View.GONE );


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
        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {

            //ラインカラー-カラーコード
            tv_lineColorCode.setOnClickListener(new ClickColorCode(LINE_COLOR) );
            //ラインカラー-カラーピッカー
            tv_lineColorGraphic.setOnClickListener( new ClickColorPicker(LINE_COLOR) );

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
         *
         * カラーコード表示リスナー
         *
         */
        private class ClickColorCode implements View.OnClickListener {

            private final int mColorKind;

            /*
             * コンストラクタ
             */
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

                        if( mColorKind == MAP_COLOR ){
                            //マップ色
                            mv_map.setBackgroundColor( Color.parseColor(code) );
                        } else if( mColorKind == NODE_BACKGROUNG_COLOR ){
                            //ノード背景色
                            nodes.setAllNodeBgColor( code );
                        } else if ( mColorKind == NODE_TEXT_COLOR ){
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

        /*
         *
         * カラーピッカー表示リスナー
         *
         */
        private class ClickColorPicker implements View.OnClickListener {

            private final int mColorKind;

            /*
             * コンストラクタ
             */
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

                        if( mColorKind == MAP_COLOR ){
                            //マップ色
                            mv_map.setBackgroundColor( Color.parseColor(code) );
                        } else if( mColorKind == NODE_BACKGROUNG_COLOR ){
                            //ノード背景色
                            nodes.setAllNodeBgColor( code );
                        } else if ( mColorKind == NODE_TEXT_COLOR ){
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
    }

    /*
     * コンストラクタ
     */
    public MapDesignAdapter(List<Integer> layoutIdList, View v_map, FragmentManager fragmentManager, ViewPager2 vp2) {
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
