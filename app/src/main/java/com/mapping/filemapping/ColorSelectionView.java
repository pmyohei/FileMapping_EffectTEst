package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;

public class ColorSelectionView extends LinearLayout {

    //デザインレイアウト種別
    public static final int MAP = 0;
    public static final int NODE = 1;
    public static final int SHAPE_ONLY = 2;

    //カラー指定
    public static final int COLOR_BACKGROUNG = 0;
    public static final int COLOR_TEXT = 1;
    public static final int COLOR_BORDER = 2;
    public static final int COLOR_SHADOW = 3;
    public static final int COLOR_LINE = 4;
    public static final int COLOR_MAP = 5;


    private int  mViewKind;       //ビュー種別
    private int  mElement;        //カラー設定要素
    private View mSetView;        //設定対象ビュー

    /*
     * コンストラクタ
     */
    public ColorSelectionView(Context context) {
        this(context, null);
    }

    public ColorSelectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorSelectionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    /*
     *
     */
    private void init( Context context ) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.color_selection, this, true);
    }

    /*
     * 色設定対象ビュー
     */
    public void setOnColorListener(int kind, int element, View view ) {
        mViewKind = kind;
        mSetView = view;
        mElement = element;

        //リスナーの設定
        findViewById(R.id.tv_rgb).setOnClickListener(new ClickColor( ClickColor.RGB) );
        findViewById(R.id.tv_graphic).setOnClickListener( new ClickColor( ClickColor.PICKER) );
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

        /*
         * コンストラクタ
         */
        public ClickColor(int colorKind) {
            mInputKind = colorKind;
        }

        @Override
        public void onClick(View view) {

            //設定中の色を取得
            String settingColor = getCurrentColor();

            //ダイアログ
            ColorDialog dialog;
            if (mInputKind == RGB) {
                dialog = new ColorCodeDialog(settingColor);
            } else {
                dialog = new ColorPickerDialog(settingColor);
            }

            //OKボタンリスナー
            dialog.setOnPositiveClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Log.i("Design", "カラーコード=" + ((EditText)view).getText());

                    //カラーコード文字列
                    ColorDrawable colorDrawable = (ColorDrawable) view.getBackground();
                    int colorInt = colorDrawable.getColor();
                    String code = "#" + Integer.toHexString(colorInt);

                    //色を設定
                    setColor(code);

                    dialog.dismiss();
                }
            });

            dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "ColorCode");
        }

        /*
         * 設定中のカラーを取得
         */
        private String getCurrentColor() {

            return (mViewKind == MAP) ? getCurrentMapColor() : getCurrentNodeColor();
        }

        /*
         * 設定中のマップカラーを取得
         */
        private String getCurrentMapColor() {

            //マップ共通データ
            MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
            NodeArrayList<NodeTable> nodes = commonData.getNodes();

            //ルートノード
            BaseNode rootNode = nodes.getRootNode().getNodeView();

            //色設定の対象毎に処理
            switch (mElement){
                case COLOR_MAP:
                    //マップ色
                    ColorDrawable colorDrawable = (ColorDrawable) mSetView.getBackground();
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

        /*
         * 設定中のノードカラーを取得
         */
        private String getCurrentNodeColor() {

            //ノードにキャスト
            BaseNode node = (BaseNode) mSetView;

            //色設定の対象毎に処理
            switch (mElement) {

                case COLOR_BACKGROUNG:
                    //ノード背景色
                    return node.getNodeBackgroundColor();

                case COLOR_TEXT:
                    //ノードテキストカラー
                    return node.getNodeTextColor();

                case COLOR_BORDER:
                    //枠線カラー
                    return node.getBorderColor();

                case COLOR_SHADOW:
                    //影カラー
                    return node.getShadowColor();

                case COLOR_LINE:
                    //ラインカラー
                    return ((ChildNode) node).getLineColor();

                default:
                    //該当なし(フェールセーフ)
                    return ResourceManager.NODE_INVALID_COLOR;
            }
        }

        /*
         * 色を設定
         */
        private void setColor( String code ){

            if( mViewKind == MAP ){
                setMapColor( code );
            } else {
                setNodeColor( code );
            }
        }

        /*
         * マップ全体に色を設定
         */
        private void setMapColor( String code ){

            //マップ共通データ
            MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
            NodeArrayList<NodeTable> nodes = commonData.getNodes();

            switch (mElement){

                case COLOR_MAP:
                    //マップ色
                    mSetView.setBackgroundColor( Color.parseColor(code) );
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

        }

        /*
         * ノード単体に色を設定
         */
        private void setNodeColor( String code ){

            //ノードにキャスト
            BaseNode node = (BaseNode) mSetView;

            //色設定の対象毎に処理
            switch (mElement) {

                case COLOR_BACKGROUNG:
                    //ノード背景色
                    node.setNodeBackgroundColor(code);
                    break;

                case COLOR_TEXT:
                    //ノードテキストカラー
                    node.setNodeTextColor(code);
                    break;

                case COLOR_BORDER:
                    //枠線カラー
                    node.setBorderColor(code);
                    break;

                case COLOR_SHADOW:
                    //影カラー
                    node.setShadowColor(code);
                    break;

                case COLOR_LINE:
                    //ラインカラー
                    ((ChildNode) node).setLineColor(code);
                    break;
            }

        }
    }


}
