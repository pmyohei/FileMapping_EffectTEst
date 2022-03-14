package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

/*
 *  ルートノード
 *    Serializable：intentによるデータの受け渡しを行うために実装
 */
public class BaseNode extends FrameLayout {

    /* フィールド */
    //シリアルID
    //private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_VIEW;

    //中心座標の初期値
    public static float INIT_CENTER_POS = -0.1f;

    //ノード情報
    public NodeTable mNode;
    //ダブルタップ検知用
    public GestureDetector mGestureDetector;
    //ノード中心座標
    public float mCenterPosX = INIT_CENTER_POS;
    public float mCenterPosY = INIT_CENTER_POS;
    //ツールアイコン表示
    //public boolean mIsOpenToolIcon;
    //ノード操作発生時の画面遷移ランチャー
    //public ActivityResultLauncher<Intent> mNodeOperationLauncher;
    //ノード生成／編集クリックリスナー
    //private MapActivity.NodeDesignClickListener mNodeDesignClickListener;
    //ダブルタップリスナー
    private View.OnClickListener mClickListener;
    //アイコンビュー（開いていない場合は、nullを設定する）
    private ToolIconsView mIconView;

    /*
     * コンストラクタ
     */
    public BaseNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * コンストラクタ
     * 　 レイアウトに埋め込んだビューの生成時用
     */
    public BaseNode(Context context, AttributeSet attrs, int layoutID) {
        super(context, attrs);

        Log.i("BaseNode", "1");

        init(layoutID);
    }

    /*
     * コンストラクタ
     * 　　new用
     */
    @SuppressLint("ClickableViewAccessibility")
    public BaseNode(Context context, NodeTable node, int layoutID) {
        super(context);

        //Log.i("BaseNode", "2");

        //ノード情報を保持
        mNode = node;
        //ノード操作ランチャーを保持
        //mNodeOperationLauncher = launcher;

        init(layoutID);
    }


    /*
     * 初期化処理
     */
    private void init(int layoutID) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, true);

        //Log.i("BaseNode", "init");

        //ツールアイコン未保持
        mIconView = null;

        //Log.i("init", "root getChildCount = " + getChildCount());

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if( mClickListener == null ){
                    //未設定なら、処理なし
                    return;
                }

                mClickListener.onClick( view );
            }
        });

        //※クリックを有効にしないとタッチ処理が検出されない
        //setClickable(true);
        //タッチリスナー
        setOnTouchListener(new RootNodeTouchListener());

        //ノード情報をビューに設定
        reflectViewNodeInfo();

        //ツールアイコン設定
        //setCommonToolIcon();
        //setParentToolIcon();


        //お試し
/*        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick( view );
            }
        });*/
    }


    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    public void reflectViewNodeInfo() {

        //Log.i("BaseNode", "reflectViewNodeInfo");

        //ノードデザインの更新
        setNodeDesign();
    }

    /*
     * ノードデザインの設定
     */
    public void setNodeDesign() {

        if (mNode == null) {
            //ノード情報未保持なら、何もしない
            return;
        }

        //ノードの基本属性のデザインを設定
        setAsBaseNodeInfo();
        //親ノード属性のデザインを設定
        setAsParentNodeInfo();

    }

    /*
     * 基本ノードとしての情報の設定
     */
    public void setAsBaseNodeInfo() {

        if (mNode == null) {
            return;
        }

        //枠色
        setBorderColor(mNode.getBorderColor());
        //枠サイズ
        setBorderSize(mNode.getBorderSize());
        //影色
        setShadowColor(mNode.getShadowColor(), mNode.getKind());
    }

    /*
     * 親ノードとしての情報の設定
     */
    public void setAsParentNodeInfo() {

        //ピクチャノードなら、何もしない
        if ((mNode == null) || (mNode.getKind() == NodeTable.NODE_KIND_PICTURE)) {
            return;
        }

        //ノード名
        setNodeName(mNode.getNodeName());
        //ノードテキストカラー
        setNodeTextColor( mNode.getTextColor() );
        //ノード背景色
        setNodeBackgroundColor(mNode.getNodeColor());
    }

    /*
     * ノード名の設定
     */
    public void setNodeName(String name) {
        ((TextView) findViewById(R.id.tv_node)).setText(name);
        mNode.setNodeName( name );

        //ノード名の変更時は、スケールを標準に戻す
        setScale( NodeTable.DEFAULT_SIZE_RATIO );
    }

    /*
     * ノード背景色の設定
     *   para：例)#123456
     */
    public void setNodeBackgroundColor(String color) {
        //背景色を設定
        //ColorDrawable colorDrawable = (ColorDrawable)findViewById(R.id.tv_node).getBackground();
        //colorDrawable.setColor( color );

        //Drawable drawable = findViewById(R.id.tv_node).getBackground();
        //drawable.setTint( color );
        CardView cv_node = findViewById(R.id.cv_node);
        //cv_node.setBackgroundColor( Color.parseColor(color) );
        cv_node.setCardBackgroundColor(Color.parseColor(color));

        mNode.setNodeColor( color );
    }

    /*
     * ノード背景色の取得
     */
    public String getNodeBackgroundColor() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        ColorStateList colorStateList = cv_node.getCardBackgroundColor();
        int colorInt = colorStateList.getDefaultColor();

        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード名のテキスト色の設定
     *   para：例)#123456
     */
    public void setNodeTextColor(String color) {
        ((TextView) findViewById(R.id.tv_node)).setTextColor( Color.parseColor(color) );

        mNode.setTextColor( color );
    }

    /*
     * ノード名のテキスト色の取得
     *   para：例)#123456
     */
    public String getNodeTextColor() {

        TextView tv_node = findViewById(R.id.tv_node);

        ColorStateList colorStateList = tv_node.getTextColors();
        int colorInt = colorStateList.getDefaultColor();

        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード名のフォント設定
     */
    public void setNodeFont(Typeface font) {

        TextView tv_node = findViewById(R.id.tv_node);
        tv_node.setTypeface( font );

        //ノードの形状を整える
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //ノードの形状を設定
                        //※フォントによってサイズが変わるため
                        setNodeShape( mNode.getNodeShape() );
                    }
                }
        );

        //Log.i("フォントサイズ", "size=" + ObjectSizeCalculator.sizeOf( font ) );
        //Log.i("フォントサイズ", "文字列=" + font.toString() );
        //Log.i("フォントサイズ", "size=" + font.toString().length() );

        //★保存はファイル名で行う
        //mNode
    }

    /*
     * ノード形の設定
     */
    public void setNodeShape( int shapeKind ) {

        if( shapeKind == NodeTable.CIRCLE ){
            setShapeCircle();
        } else {
            setShapeSquare();
        }

        mNode.setNodeShape( shapeKind );
    }

    /*
     * ノード枠線サイズの設定
     */
    public void setBorderSize( int thick ) {
        //枠サイズを設定
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeWidth( thick );

        mNode.setBorderSize( thick );
    }

    /*
     * ノード枠線サイズの取得
     */
    public int getBorderSize() {
        return ((MaterialCardView)findViewById( R.id.cv_node )).getStrokeWidth();
    }

    /*
     * ノード枠色の設定
     */
    public void setBorderColor( String color ) {
        //枠色を設定
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeColor( Color.parseColor(color) );

        mNode.setBorderColor( color );
    }

    /*
     * ノード枠色の取得
     */
    public String getBorderColor() {
        MaterialCardView cv_node = findViewById(R.id.cv_node);

        ColorStateList colorStateList = cv_node.getStrokeColorStateList();

        if( colorStateList == null ){
            //取得失敗なら、無効値用の色を返す
            return ResourceManager.NODE_INVALID_COLOR;
        }

        int colorInt = colorStateList.getDefaultColor();
        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード影色の設定
     */
    public void setShadowColor( String color, int nodeKind ) {

        //現在の影の有無
        boolean isShadow = mNode.isShadow();

        //影色を設定
        ((NodeOutsideView)findViewById( R.id.l_nodeBody )).setShadowColor( Color.parseColor(color), nodeKind, isShadow );

        mNode.setShadowColor( color );
    }

    /*
     * ノード影色の取得
     */
    public String getShadowColor() {
        return ((NodeOutsideView)findViewById( R.id.l_nodeBody )).getShadowColor();
    }

    /*
     * ノード影の有無の設定
     */
    public void setShadowOnOff(boolean isShadow ) {
        //影色を設定
        ((NodeOutsideView)findViewById( R.id.l_nodeBody )).setShadowOnOff( isShadow, mNode.getKind()  );

        mNode.setShadow( isShadow );
    }

    /*
     * ノード影の有無を切替
     */
    public void switchShadow() {
        //OnOff反転
        boolean isShadow = !mNode.isShadow();
        this.setShadowOnOff( isShadow );
    }

    /*
     * ノード影の有無を取得
     */
    public boolean isShadow() {
        //影の有無を取得
        return mNode.isShadow();
    }

    /*
     * ノードに設定中のノードサイズに設定
     */
    public void setSetScale() {
        float ratio = mNode.getSizeRatio();
        //findViewById( R.id.cl_node ).setScaleX( ratio );
        //findViewById( R.id.cl_node ).setScaleY( ratio );
        setScaleX( ratio );
        setScaleY( ratio );
    }

    /*
     * ノードサイズを設定
     */
    public void setScale( float ratio ) {
        setScaleX( ratio );
        setScaleY( ratio );

        mNode.setSizeRatio( ratio );
    }

    /*
     * 比率込みのノードサイズ（横幅）を取得
     *   ※ノードレイアウト全体のサイズ
     */
    public float getScaleWidth() {
        //現在の横幅 * 現在の比率
        return getWidth() * mNode.getSizeRatio();
    }

    /*
     * 比率込みのノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
    public float getScaleNodeBodyWidth() {
        //現在の横幅 * 現在の比率
        return findViewById(R.id.cv_node).getWidth() * mNode.getSizeRatio();
    }

    /*
     * ノード中心座標の設定
     */
    public void calcCenterPos() {
        //中心座標を計算し、設定
        this.mCenterPosX = getLeft() + (getWidth() / 2f);
        this.mCenterPosY = getTop() + (getHeight() / 2f);
    }

    /*
     * ノードの形を円形にする
     */
    private void setShapeCircle() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);
        //Log.i("Card", "width=" + cv_node.getWidth() + " height=" + cv_node.getHeight());

        //長い方の辺で縦横サイズを統一
        int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setMinimumHeight(max);
        cv_node.setMinimumWidth(max);

        cv_node.setRadius(max / 2.0f);
    }

    /*
     * ノードの形を四角（角丸）にする
     */
    private void setShapeSquare() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        //長い方の辺で正方形を作る
        int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setMinimumHeight(max);
        cv_node.setMinimumWidth(max);

        cv_node.setRadius(max * ResourceManager.SQUARE_CORNER_RATIO);
    }


/*    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        Log.i("長さの確定確認", "onMeasure widthSize=" + widthSize);
        Log.i("長さの確定確認", "onMeasure getWidth=" + getWidth());
    }*/

    /*
     * レイアウト確定後処理の設定
     */
    public void addOnNodeGlobalLayoutListener() {

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //Log.i("長さの確定確認", "addOnNodeGlobalLayoutListener");
                        //Log.i("長さの確定確認", "addOnNodeGlobalLayoutListener getWidth=" + getWidth());

                        //中心座標の計算
                        calcCenterPos();
                        //ノードの形状
                        setNodeShape( mNode.getNodeShape() );
                        //サイズを設定
                        setSetScale();

                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    /*
     * ノードクリックリスナーの設定
     */
    public void setOnNodeClickListener( View.OnClickListener listener ) {
        mClickListener = listener;
    }


    /*
     * ノードタッチリスナー
     */
    public class RootNodeTouchListener implements OnTouchListener, Serializable {

        //シリアルID
        private static final long serialVersionUID = 3L;

        /*
         * コンストラクタ
         */
        public RootNodeTouchListener() {

            //ダブルタップリスナーを実装したGestureDetector
            mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ダブルタップ処理
            return mGestureDetector.onTouchEvent(event);
        }

        /*
         * ダブルタップリスナー
         */
        private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener implements Serializable{

            /*
             * ダブルタップリスナー
             *   ツールアイコンの表示制御を行う。
             *   ※他ノードがオープン中であれば、クローズしてタップされたノードのツールアイコンを表示する
             */
            @Override
            public boolean onDoubleTap(MotionEvent event) {

                Log.i("tap", "onDoubleTap getChildCount1 = " + getChildCount());

                //ツールアイコン表示制御
                //operationToolIcon();

                //
                //mClickListener.onClick( mNode.getNodeView() );

                //return super.onDoubleTap(event);
                return true;
            }
        }
    }

    /*-- getter／setter --*/
    public NodeTable getNode() {
        return mNode;
    }
    public void setNode(NodeTable node) {
        //ノード情報を設定
        mNode = node;
        //ノードデザインの設定
        setNodeDesign();
    }

    public float getCenterPosX() {
        return mCenterPosX;
    }
    public float getCenterPosY() {
        return mCenterPosY;
    }
    public ToolIconsView getIconView() {
        return this.mIconView;
    }
    public void setIconView( ToolIconsView iconView ) {
        this.mIconView = iconView;
    }
    public boolean hasIconView() {
        return (this.mIconView != null);
    }
    public void closeIconView() {
        this.mIconView.closeMyself();
        this.mIconView = null;
    }
    public void clearIconView() {
        this.mIconView = null;
    }
}
