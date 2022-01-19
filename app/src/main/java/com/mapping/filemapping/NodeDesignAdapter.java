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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.List;

public class NodeDesignAdapter extends RecyclerView.Adapter<NodeDesignAdapter.GuideViewHolder> {

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
        private final int NODE_BACKGROUNG_COLOR = 0;
        private final int NODE_TEXT_COLOR = 1;
        private final int LINE_COLOR = 2;

        //設定対象ノードビュー
        private final BaseNode        mv_node;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        /*--- ノードテキスト ---*/
        //ノード名
        private EditText et_nodeName;
        private RecyclerView rv_fontAlphabet;
        private RecyclerView rv_fontjapanese;

        /*--- ノード ---*/
        //ノードデザイン
        private TextView tv_bgColorCode;
        private TextView tv_bgColorGraphic;
        private TextView tv_txColorCode;
        private TextView tv_txColorGraphic;
        private SeekBar  sb_nodeSize;

        /*--- ライン ---*/
        //ラインデザイン
        private TextView tv_lineColorCode;
        private TextView tv_lineColorGraphic;
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
                et_nodeName = itemView.findViewById(R.id.et_nodeName);
                rv_fontAlphabet = itemView.findViewById(R.id.rv_fontAlphabet);
                rv_fontjapanese   = itemView.findViewById(R.id.rv_fontJapanese);

            } else if (position == 1) {
                //背景色
                tv_bgColorCode    = itemView.findViewById(R.id.tv_bgColorCode);
                tv_bgColorGraphic = itemView.findViewById(R.id.tv_bgColorGraphic);
                //テキスト色
                tv_txColorCode    = itemView.findViewById(R.id.tv_txColorCode);
                tv_txColorGraphic = itemView.findViewById(R.id.tv_txColorGraphic);
                //ノードサイズ
                sb_nodeSize       = itemView.findViewById(R.id.sb_nodeSize);

            } else if (position == 2) {
                //色
                tv_lineColorCode    = itemView.findViewById(R.id.tv_lineColorCode);
                tv_lineColorGraphic = itemView.findViewById(R.id.tv_lineColorGraphic);

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

            //背景色-カラーコード
            tv_bgColorCode.setOnClickListener(new ClickColorCode(NODE_BACKGROUNG_COLOR) );
            //背景色-カラーピッカー
            tv_bgColorGraphic.setOnClickListener( new ClickColorPicker(NODE_BACKGROUNG_COLOR) );

            //テキスト色-カラーコード
            tv_txColorCode.setOnClickListener(new ClickColorCode(NODE_TEXT_COLOR) );
            //テキスト色-カラーピッカー
            tv_txColorGraphic.setOnClickListener( new ClickColorPicker(NODE_TEXT_COLOR) );

            //ノードサイズ
            sb_nodeSize.setMax(100);
            sb_nodeSize.setProgress(50);
            sb_nodeSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekbar) {
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekbar) {
                }

                @Override
                public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

                    //ConstraintLayout cv = mv_node.findViewById( R.id.cl_node );
                    View cv = mv_node.findViewById( R.id.cv_node );

                    //Log.i("size", "getWidth=" + cv.getWidth());

                    float value;

                    //0.1～10.0　の範囲
                    //★暫定
                    if( i < 50 ){
                        value = (0.9f / 50) * i + 0.1f;
                    } else{
                        value = (9f / 50) * (i - 50) + 1;
                    }

                    //Log.i("size", "i=" + i + " value=" + value);

                    cv.setScaleX( value );
                    cv.setScaleY( value );
                }
            });


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

                        //カラーコード文字列
                        String code = "#" + ((EditText)view).getText().toString();

                        if( mColorKind == NODE_BACKGROUNG_COLOR ){
                            //ノード背景色
                            mv_node.setNodeBackgroundColor( code );
                        } else if ( mColorKind == NODE_TEXT_COLOR ){
                            //ノードテキストカラー
                            mv_node.setNodeTextColor( code );
                        } else {
                            //ラインカラー
                            ((ChildNode)mv_node).setLineColor( code );
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

                        //カラーコード文字列
                        String code = "#" + Integer.toHexString( ((ColorPickerView)view).getColor() );

                        if( mColorKind == NODE_BACKGROUNG_COLOR ){
                            //背景色
                            mv_node.setNodeBackgroundColor( code );
                        } else if ( mColorKind == NODE_TEXT_COLOR ){
                            //テキストカラー
                            mv_node.setNodeTextColor( code );
                        } else {
                            //ラインカラー
                            ((ChildNode)mv_node).setLineColor( code );
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
    public NodeDesignAdapter(List<Integer> layoutIdList, BaseNode v_node, FragmentManager fragmentManager, ViewPager2 vp) {
        mData            = layoutIdList;
        mv_node          = v_node;
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
