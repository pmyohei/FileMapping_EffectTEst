package com.mapping.filemapping;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.List;

public class MapDesignAdapter extends RecyclerView.Adapter<MapDesignAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //マップ
    private View     mv_map;
    //FragmentManager
    private final FragmentManager mFragmentManager;

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
        private View     mv_map;
        //FragmentManager
        private final FragmentManager mFragmentManager;

        //マップデザイン
        private TextView tv_bgMapColorCode;
        private TextView tv_bgMapColorGraphic;

        //ノードデザイン
        private TextView tv_bgColorCode;
        private TextView tv_bgColorGraphic;
        private TextView tv_txColorCode;
        private TextView tv_txColorGraphic;
        private LinearLayout ll_nodeSize;

        //ラインデザイン
        private TextView tv_lineColorCode;
        private TextView tv_lineColorGraphic;
        private RadioGroup rg_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, View v_map, FragmentManager fragmentManager) {
            super(itemView);

            mv_map           = v_map;
            mFragmentManager = fragmentManager;

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
    public MapDesignAdapter(List<Integer> layoutIdList, View v_map, FragmentManager fragmentManager) {
        mData            = layoutIdList;
        mv_map           = v_map;
        mFragmentManager = fragmentManager;
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

        return new GuideViewHolder(view, position, mv_map, mFragmentManager);
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
