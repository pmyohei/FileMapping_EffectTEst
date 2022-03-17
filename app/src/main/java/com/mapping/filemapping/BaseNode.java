package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

/*
 *  ベースノード
 *    ノードの基本クラス。どのクラスにも実装される。
 */
public class BaseNode extends FrameLayout {

    //中心座標の初期値（ありえない値）
    public static float INIT_CENTER_POS = -0.1f;

    //ノード情報
    public NodeTable mNode;
    //ノード中心座標
    public float mCenterPosX = INIT_CENTER_POS;
    public float mCenterPosY = INIT_CENTER_POS;
    //ダブルタップリスナー
    private View.OnClickListener mClickListener;
    //アイコンビュー（開いていない場合は、nullを設定する）
    private ToolIconsView mIconView;

    //影用ペイント
    private Paint mPaint;
    //影色
    private int mShadowColor;
    //影サイズの最小・最大値
    private float MIN_RADIUS;
    private float MAX_RADIUS;

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

        //初期化処理
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

        init(layoutID);
    }


    /*
     * 初期化処理
     */
    private void init(int layoutID) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, true);

        //影サイズの最小最大（px→dp）
        MIN_RADIUS = getResources().getDimension(R.dimen.shadow_radius_min);
        MAX_RADIUS = getResources().getDimension(R.dimen.shadow_radius_max);

        //Log.i("影半径", "MIN_RADIUS=" + MIN_RADIUS);
        //Log.i("影半径", "MAX_RADIUS=" + MAX_RADIUS);

        //OnDraw()をコールさせる設定
        setWillNotDraw(false);

        //ツールアイコン未保持
        mIconView = null;

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

        //ノード情報をビューに設定
        reflectViewNodeInfo();
    }

    /*
     * ペイント初期化
     */
    public void initPaint() {

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ){
            //※API28以下は、影の描画に必要な処理
            setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);
        }

        //ペイント生成
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
    }

    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    public void reflectViewNodeInfo() {
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
        //継承先で実装
    }

    /*
     * ノード枠線サイズの取得
     */
    public int getBorderSize() {
        return mNode.getBorderSize();
    }

    /*
     * ノード枠色の設定
     */
    public void setBorderColor( String color ) {
        //継承先で実装
    }

    /*
     * ノード枠色の取得
     */
    public String getBorderColor() {
        return mNode.getBorderColor();
    }

    /*
     * ノード影色の設定
     */
    public void setShadowColor( String color, int nodeKind ) {

        //現在の影の有無
        boolean isShadow = mNode.isShadow();
        //影色を設定
        setLayerShadowColor(Color.parseColor(color), nodeKind, isShadow );
        mNode.setShadowColor( color );
    }

    /*
     * ノード影色の取得
     */
    public String getShadowColor() {
        return ( "#" + Integer.toHexString( mShadowColor ) );
    }

    /*
     * ノード影のon/offの設定
     */
    public void setShadowOnOff(boolean isShadow ) {
        //影色を設定
        setLayerShadowOnOff( isShadow, mNode.getKind() );
        mNode.setShadow( isShadow );
    }

    /*
     * 影色の設定
     */
    public void setLayerShadowColor(int colorHex, int nodeKind, boolean isShadow) {

        //ペイント未生成なら生成
        if( mPaint == null ){
            initPaint();
        }

        //色更新
        mShadowColor = colorHex;

        //影設定ありなら、設定色で描画
        if( isShadow ){
            setLayerShadowOnOff( true, nodeKind );
        }
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
     * 影の有無を設定
     */
    public void setLayerShadowOnOff(boolean isShadow, int nodeKind ) {

        //ペイント未生成なら生成
        if( mPaint == null ){
            initPaint();
        }

        if( isShadow ){
            float nodeRadius = getNodeBodyWidth() / 8f;

            //最大最小チェック
            //※最小限のサイズと最大サイズを設定
            if( nodeRadius < MIN_RADIUS ){
                nodeRadius = MIN_RADIUS;
            } else if( nodeRadius > MAX_RADIUS ){
                nodeRadius = MAX_RADIUS;
            }

            //Log.i("影半径", "nodeRadius=" + nodeRadius);

            //影の設定
            mPaint.setShadowLayer(nodeRadius, 0, 0, mShadowColor);

        } else {
            //影を削除
            mPaint.clearShadowLayer();
        }

        //再描画
        invalidate();
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
     * ノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
    public float getNodeBodyWidth() {
        //現在の横幅 * 現在の比率
        if( mNode.getKind() == NodeTable.NODE_KIND_PICTURE ){
            return findViewById(R.id.iv_node).getWidth();
        } else {
            return findViewById(R.id.cv_node).getWidth();
        }
    }

    /*
     * 比率込みのノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
    public float getScaleNodeBodyWidth() {

        //現在の横幅 * 現在の比率
        if( mNode.getKind() == NodeTable.NODE_KIND_PICTURE ){
            return findViewById(R.id.iv_node).getWidth() * mNode.getSizeRatio();
        } else {
            return findViewById(R.id.cv_node).getWidth() * mNode.getSizeRatio();
        }
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
    public void setShapeCircle() {
        //継承先で実装
    }

    /*
     * ノードの形を四角（角丸）にする
     */
    public void setShapeSquare() {
        //継承先で実装
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

    @Override
    protected void onDraw(Canvas canvas) {

        if( mPaint == null ){
            return;
        }

        //ノードの横幅
        int radius = (int)(getNodeBodyWidth() / 2f);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, mPaint);
    }

    /*------ getter／setter ------*/

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
}
