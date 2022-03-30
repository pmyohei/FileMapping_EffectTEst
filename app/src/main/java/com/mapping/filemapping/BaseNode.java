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
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

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

        //ノードクリックリスナー
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

        //ペイント生成
        mPaint = new Paint();

        if( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ){
            //※API28以下は、影の描画に必要な処理
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            //※透明の場合、影は描画されない
            mPaint.setColor(Color.WHITE);

        } else {
            mPaint.setColor(Color.TRANSPARENT);
        }

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
        setBorderColor( mNode.getBorderColor() );
        //枠サイズ
        setBorderSize( mNode.getBorderSize() );
        //影色
        setShadowColor( mNode.getShadowColor() );
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
        //フォント
        initNodeFont(mNode.getFontFileName() );
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
        //継承先で実装
    }

    /*
     * ノード背景色の取得
     */
    public String getNodeBackgroundColor() {
        return mNode.getNodeColor();
    }

    /*
     * ノード名のテキスト色の設定
     *   para：例)#123456
     */
    public void setNodeTextColor(String color) {
        //継承先で実装
    }

    /*
     * ノード名のテキスト色の取得
     *   para：例)#123456
     */
    public String getNodeTextColor() {
        return mNode.getTextColor();
    }


    /*
     * ノード名のフォント設定
     */
    public void initNodeFont(String fontFileName) {

        //何もしない
        if( (fontFileName == null) || (fontFileName.isEmpty()) ){
            return;
        }

        //指定フォントファイル名から、フォントを生成
        Context context = getContext();
        int fontID = context.getResources().getIdentifier( fontFileName, "font", context.getPackageName() );

        //運用誤りでファイル名文字列のファイルがない場合、何もしない
        if( fontID == 0 ){
            //Log.i("フォント保存対応", "初期設定 変換エラー=" + fontFileName);
            return;
        }

        //フォント生成エラーの場合、何もしない
        Typeface font = ResourcesCompat.getFont(context, fontID);
        if( font == null ){
            return;
        }

        //設定
        setNodeFont( font, fontFileName );
    }

    /*
     * ノード名のフォント設定
     */
    public void setNodeFont(Typeface font, String fontFileName) {
        //継承先で実装
;    }

    /*
     * ノード形の設定
     */
    public void setNodeShape( int shapeKind ) {
        //継承先で実装
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
    public void setShadowColor( String color ) {
        //現在の影の有無
        boolean isShadow = mNode.isShadow();
        //影色を設定
        setLayerShadowColor(Color.parseColor(color), isShadow );

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
        setLayerShadowOnOff( isShadow );
        mNode.setShadow( isShadow );
    }

    /*
     * 影色の設定
     */
    public void setLayerShadowColor(int colorHex, boolean isShadow) {

        //ペイント未生成なら生成
        if( mPaint == null ){
            initPaint();
        }

        //色更新
        mShadowColor = colorHex;

        //影設定ありなら、設定色で描画
        if( isShadow ){
            setLayerShadowOnOff( true );
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
    public void setLayerShadowOnOff(boolean isShadow ) {

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
        //現在の横幅
        if( mNode.getKind() == NodeTable.NODE_KIND_PICTURE ){
            //※paddingを考慮したサイズ（実際のサムネイルサイズ）を返す
            return findViewById(R.id.iv_node).getWidth() - getResources().getDimension(R.dimen.thumbnail_image_padding) * 2;
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
     * 比率込みのノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
    public float getScaleNodeWidth() {
        return getWidth() * mNode.getSizeRatio();
    }

    /*
     * ノードの形を円形にする
     */
/*
    public void setShapeCircle() {
        //継承先で実装
    }

    */
/*
     * ノードの形を四角（角丸）にする
     *//*

    public void setShapeSquare() {
        //継承先で実装
    }
*/


/*    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        Log.i("長さの確定確認", "onMeasure widthSize=" + widthSize);

        if( mNode != null ){
            Log.i("確定順調査", "onMeasure=" + mNode.getNodeName() + " height()" + getHeight());
        }
    }*/

    /*
     * レイアウト確定後リスナーの追加
     *　　ノードの形状、ノードの比率を設定
     *   ノードの形状は、ノード名が確定しないと適切な形にできないため、このタイミングで設定する
     */
    public void addLayoutConfirmedListener() {

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    //ノードの形状
                    setNodeShape( mNode.getNodeShape() );
                    //設定中のスケールサイズを設定
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
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //中心座標を計算し、設定
        this.mCenterPosX = left + (getWidth() / 2f);
        this.mCenterPosY = top + (getHeight() / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if( mPaint == null ){
            return;
        }

        if( mNode.getKind() == NodeTable.NODE_KIND_PICTURE ) {

            switch (mNode.getNodeShape()) {
                //四角形
                case NodeTable.SQUARE:
                case NodeTable.SQUARE_ROUNDED:
                    drawShadowSquare( canvas, NodeTable.NODE_KIND_PICTURE );
                    return;

                //ダイア
                case NodeTable.DIA:
                case NodeTable.DIA_SEMI:
                    drawShadowDia( canvas );
                    return;
            }

        } else {
            if( mNode.getNodeShape() == NodeTable.SQUARE_ROUNDED ){
                drawShadowSquare( canvas, NodeTable.NODE_KIND_NODE );
                return;
            }
        }

        //円形
        drawShadowCircle( canvas );
    }

    /*
     * 円の影を描画
     */
    public void drawShadowCircle( Canvas canvas ) {
        //ノード本体サイズ
        float radius = (int)(getNodeBodyWidth() / 2f);

        //--- API28以下のサイズ調整
        //--- 八角形の場合、角にペイントの色が見えてしまうため、少し縮小
        if( (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                && ( mNode.getNodeShape() == NodeTable.OCTAGON )
                && ( mNode.getKind() == NodeTable.NODE_KIND_PICTURE )){
            //縮小値は任意
            radius *= 0.955f;
        }

        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, radius, mPaint);
    }

    /*
     * 四角形の影を描画
     */
    public void drawShadowSquare( Canvas canvas, int nodeKind ) {

        int id;
        int padding = 0;
        if (nodeKind == NodeTable.NODE_KIND_PICTURE) {
            id = R.id.iv_node;
            //ピクチャノードの場合、paddingが設定されているため、その分を加算
            padding = (int)getResources().getDimension(R.dimen.thumbnail_image_padding);
        } else {
            id = R.id.cv_node;
        }

        //ノード本体（ピクチャノードなら、サムネイル画像の開始位置）
        int nodeBodyOrg = findViewById( id ).getLeft() - findViewById(R.id.ll_node).getLeft();
        nodeBodyOrg += padding;

        //ノード本体のサイズ
        float nodeBodyWidth = getNodeBodyWidth();

        //角の長さ
        float rx = 0f;
        float ry = 0f;
        if( mNode.getNodeShape() == NodeTable.SQUARE_ROUNDED ){
            //角丸指定なら、ノード側の長さとあわせる
            rx = nodeBodyWidth * ResourceManager.SQUARE_CORNER_RATIO;
            ry = rx;
        }

        //四角形を描画
        canvas.drawRoundRect( nodeBodyOrg, nodeBodyOrg,
                nodeBodyOrg + nodeBodyWidth,
                nodeBodyOrg + nodeBodyWidth,
                rx,
                ry,
                mPaint);
    }


    /*
     * ダイアの影を描画
     */
    public void drawShadowDia( Canvas canvas ) {

        ShapeableImageView iv_node = findViewById(R.id.iv_node);

        int padding = (int)getResources().getDimension(R.dimen.thumbnail_image_padding);
        //サムネイルサイズ（paddingなし）
        float thumbnailWidthNoPadding = getNodeBodyWidth();
        //サムネイルサイズ（paddingあり）
        float thumbnailWidth = iv_node.getWidth();

        //四角描画の前に、４５度傾ける
        canvas.save();
        canvas.rotate(45, getWidth() / 2f, getWidth() / 2f);

        //サムネイルの表示の始まり座標
        int nodeBodyOrg = (int)((float)iv_node.getLeft() + thumbnailWidth / 8f - findViewById(R.id.ll_node).getLeft());
        nodeBodyOrg += padding;

        canvas.drawRect( nodeBodyOrg, nodeBodyOrg,
                (nodeBodyOrg + thumbnailWidthNoPadding * 0.75f - padding),
                (nodeBodyOrg + thumbnailWidthNoPadding * 0.75f - padding),
                mPaint);
        canvas.restore();
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
