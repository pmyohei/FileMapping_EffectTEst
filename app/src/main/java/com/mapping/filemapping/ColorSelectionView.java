package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;
import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.ArrayList;

public class ColorSelectionView extends LinearLayout {

    //デザインレイアウト種別
    public static final int MAP = 0;
    public static final int NODE = 1;

    //カラー指定
    public static final int COLOR_BACKGROUNG = 0;
    public static final int COLOR_TEXT = 1;
    public static final int COLOR_BORDER = 2;
    public static final int COLOR_SHADOW = 3;
    public static final int COLOR_LINE = 4;
    public static final int COLOR_MAP = 5;

    private int  mViewKind;       //ビュー種別（ノードorマップ(全ノード)）
    private int  mPart;           //カラー設定個所
    private View mSetView;        //設定対象ビュー（ノードorマップ）

    private ColorHistoryAdapter mColorHistoryAdapter;

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

        //色履歴リスト
        MapCommonData mapCommonData = (MapCommonData) ((Activity)context).getApplication();
        ArrayList<String> colors =  mapCommonData.getColorHistory();

        mColorHistoryAdapter = new ColorHistoryAdapter( colors );
        
        //色履歴を設定
        RecyclerView rv_history = findViewById(R.id.rv_history);
        rv_history.setAdapter( mColorHistoryAdapter );
        rv_history.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false ));

        //色履歴更新リスナー
        findViewById(R.id.iv_updateHistory).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //色履歴アダプタを更新
                mColorHistoryAdapter.notifyDataSetChanged();
            }
        });
    }

    /*
     * 色設定対象ビュー
     */
    public void setOnColorListener(int kind, int part, View view ) {
        mViewKind = kind;
        mPart     = part;
        mSetView  = view;

        //リスナーの設定
        findViewById(R.id.tv_rgb).setOnClickListener(new ClickColor( ClickColor.RGB) );
        findViewById(R.id.tv_graphic).setOnClickListener( new ClickColor( ClickColor.PICKER) );

        //横スクロールを本リサイクラービューに優先させる
        ViewPager2 vp2 = mSetView.getRootView().findViewById( R.id.vp2_design );
        RecyclerView rv_history = findViewById(R.id.rv_history);
        rv_history.addOnItemTouchListener( new Vp2OnItemTouchListener( vp2 ) );
    }

    /*
     * 色を設定
     */
    private void setColor( String code ){

        if( mViewKind == MAP ){
            setMapAllNodeColor( code );
        } else {
            setNodeColor( code );
        }
    }

    /*
     * マップ全体に色を設定
     */
    private void setMapAllNodeColor(String code ){

        //マップ共通データ
        MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = commonData.getNodes();

        switch (mPart){

            case COLOR_MAP:
                //マップ色
                ((MapActivity)mSetView.getContext()).setMapColor( code );
                //mSetView.setBackgroundColor( Color.parseColor(code) );
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
        switch (mPart) {

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

    /*
     * カラー入力アイコンリスナー
     */
    private class ClickColor implements View.OnClickListener, TextWatcher {

        //カラー入力方法
        public static final int RGB = 0;
        public static final int PICKER = 1;

        //カラー入力方法
        private final int mInputKind;

        //表示中ダイアログ
        private AlertDialog mDialog;

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
            //ColorDialog dialog;
            if (mInputKind == RGB) {
                showColorCodeDialog(settingColor);
            } else {
                showColorPickerDialog(settingColor);
            }
        }

        /*
         * カラーコード入力ダイアログ
         */
        private void showColorCodeDialog(String settingColor) {
            View layout = LayoutInflater.from(getContext()).inflate(R.layout.color_code_dialog, null);

            mDialog = new AlertDialog.Builder(getContext())
                    .setView(layout)
                    .create();

            //確認用ビューに初期色を設定
            layout.findViewById(R.id.v_checkColor).setBackgroundColor(Color.parseColor(settingColor));

            //「#」を取り除く
            String rgb;
            if (settingColor.length() == 7) {
                //「#123456」→「123456」
                rgb = settingColor.replace("#", "");
            } else {
                //「#FF123456」→「123456」
                rgb = settingColor.replaceAll("^#..", "");
            }

            //RGB文字列を設定
            EditText et_colorCode = layout.findViewById(R.id.et_colorCode);
            et_colorCode.setText(rgb);
            //入力リスナー設定
            et_colorCode.addTextChangedListener(this);

            //OKボタン
            layout.findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //OKボタンリスナー処理
                    positiveCode();
                }
            });

            //キャンセルボタン
            layout.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                }
            });

            mDialog.show();
        }

        /*
         * カラーピッカー入力ダイアログ
         */
        private void showColorPickerDialog(String settingColor) {
            View layout = LayoutInflater.from(getContext()).inflate(R.layout.color_picker_dialog, null);

            mDialog = new AlertDialog.Builder(getContext())
                    .setView(layout)
                    .create();

            //確認用ビューに初期色を設定
            layout.findViewById(R.id.v_checkColor).setBackgroundColor(Color.parseColor(settingColor));

            //カラーピッカーに初期値を設定
            ColorPickerView cpv = layout.findViewById(R.id.colorPicker);
            cpv.setColor(Color.parseColor(settingColor));

            //カラーピッカー
            cpv.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
                @Override
                public void onColorChanged(int newColor) {
                    //Log.i("onColorChanged", "getColor=" + cpv.getColor());
                    //Log.i("onColorChanged", "getColor(Hex)=" + Integer.toHexString(cpv.getColor()) );

                    //「例）#123456」を作成
                    //cpv.getColor()で取得できる値「ARGB」 例）ffb58d8d
                    String code = "#" + Integer.toHexString(cpv.getColor());

                    //選択された色をチェック用のビューに反映
                    layout.findViewById(R.id.v_checkColor).setBackgroundColor(Color.parseColor(code));
                }
            });

            //OKボタン
            layout.findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //OKボタンリスナー処理
                    positiveCommon();
                }
            });

            //キャンセルボタン
            layout.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDialog.dismiss();
                }
            });

            mDialog.show();
        }

        /*
         * ダイアログOKボタンリスナー設定
         */
        private void positiveCode() {

            //無効な文字列チェック
            EditText et_colorCode = mDialog.findViewById(R.id.et_colorCode);
            if (!et_colorCode.getText().toString().matches("^[a-fA-F0-9]*")) {
                Toast.makeText(et_colorCode.getContext(), getResources().getString(R.string.toast_errorRgb), Toast.LENGTH_SHORT).show();
                return;
            }

            //共通設定
            positiveCommon();
        }

        /*
         * ダイアログOKボタンリスナー設定
         */
        private void positiveCommon() {
            //確認ビュー
            View v_checkColor = mDialog.findViewById(R.id.v_checkColor);

            //カラーコード文字列
            ColorDrawable colorDrawable = (ColorDrawable) v_checkColor.getBackground();
            int colorInt = colorDrawable.getColor();
            String code = "#" + Integer.toHexString(colorInt);

            //色を設定
            setColor(code);

            //色履歴の追加
            MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
            int idx = commonData.addColorHistory( code );

            if( idx >= 0 ){
                //アダプタに追加を通知
                mColorHistoryAdapter.notifyItemInserted(idx);
            }

            //ダイアログ閉じる
            mDialog.dismiss();
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
            switch (mPart){
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
            switch (mPart) {

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

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        /*
         * ６文字入力されたタイミングで、確認カラーに反映
         */
        @Override
        public void afterTextChanged(Editable editable) {
            //リスナーに渡すビューを、カラーコードのビューに入れ替え
            EditText et_colorCode = mDialog.findViewById(R.id.et_colorCode);
            String rgb = et_colorCode.getText().toString();

            //6文字未満なら、何もしない
            if( rgb.length() < 6 ){
                return;
            }

            //無効な文字列チェック
            //先頭から想定する文字が続いていない場合
            if( !rgb.matches( "^[a-fA-F0-9]*" ) ){
                return;
            }

            //例）「123456」→「#123456」を作成
            String code = "#" + et_colorCode.getText().toString();
            //確認色に反映
            mDialog.findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( code ) );
        }

    }

    /*
     * 色履歴アダプタ
     */
    public class ColorHistoryAdapter extends RecyclerView.Adapter<ColorHistoryAdapter.ColorHistoryViewHolder> {

        //色履歴
        private final ArrayList<String> mData;

        /*
         * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
         * (固有のためインナークラスで定義)
         */
        class ColorHistoryViewHolder extends RecyclerView.ViewHolder {

            private final MaterialCardView v_historyColorItem;

            /*
             * コンストラクタ
             */
            public ColorHistoryViewHolder(View itemView) {
                super(itemView);

                v_historyColorItem = itemView.findViewById(R.id.v_historyColorItem);
            }

            /*
             * ビューの設定
             */
            public void setView( String color ){

                //色を設定
                //v_historyColorItem.setBackgroundColor( Color.parseColor( color ) );

                ColorStateList colorState = new ColorStateList(
                        new int[][] {
                                new int[]{ android.R.attr.state_checked},
                                new int[]{ -android.R.attr.state_checked},
                        },
                        new int[] {
                                Color.parseColor( color ),
                                Color.parseColor( color ),
                        }
                );
                v_historyColorItem.setCardForegroundColor( colorState );

                //リスナー
                v_historyColorItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //色を設定
                        setColor( color );
                    }
                });
            }
        }

        /*
         * コンストラクタ
         */
        public ColorHistoryAdapter(ArrayList<String> colors ) {
            mData = colors;
        }

        /*
         * ここの戻り値が、onCreateViewHolder()の第２引数になる
         */
        @Override
        public int getItemViewType(int position) {

            return position;
        }

        /*
         *　ViewHolderの生成
         */
        @NonNull
        @Override
        public ColorHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

            //ビューを生成
            LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            View view = inflater.inflate(R.layout.item_color_history, viewGroup, false);

            return new ColorHistoryViewHolder(view);
        }

        /*
         * ViewHolderの設定
         *   表示内容等の設定を行う
         */
        @Override
        public void onBindViewHolder(@NonNull ColorHistoryViewHolder viewHolder, final int i) {

            //対象マップ情報
            String color = mData.get(i);

            //ビューの設定
            viewHolder.setView( color );
        }

        /*
         * データ数取得
         */
        @Override
        public int getItemCount() {
            //表示データ数を返す
            return mData.size();
        }

    }

}
