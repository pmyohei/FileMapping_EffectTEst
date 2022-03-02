package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

/*
 * ノードの外側用のビュー
 *   主に影／発光の役割を果たす
 */
public class NodeOutsideView extends LinearLayout {
    private Paint mPaint;
    private int mShadowColor;

    float mRadius = 0.0f;

    /*
     *　レイアウトから生成時用
     */
    public NodeOutsideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.node_body, this, true);

        //OnDraw()をコールさせる設定
        setWillNotDraw(false);
    }

    /*
     * ペイント初期化
     */
    public void initPaint( int nodeKind ) {

        if( (nodeKind != NodeTable.NODE_KIND_PICTURE) && (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) ){
            //ピクチャノード以外で、API28以下なら、レイヤータイプを設定
            //※API28以下は、影の描画に必要な処理
            //※ピクチャノードでは、以下をコールすると写真が円形にならない
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        
        //ペイント生成
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
    }

    /*
     * 影の有無を設定
     */
    public void setShadowOnOff(boolean isShadow, int nodeKind ) {

        //ペイント未生成なら生成
        if( mPaint == null ){
            initPaint(nodeKind);
        }

        if( isShadow ){
            //影の最小・最大値
            final float MIN_RADIUS = 22f;
            final float MAX_RADIUS = 48f;

            float nodeRadius = findViewById(R.id.cv_node).getWidth() / 8f;

            //最大最小チェック
            //※最小限のサイズと最大サイズを設定
            if( nodeRadius < MIN_RADIUS ){
                nodeRadius = MIN_RADIUS;
            } else if( nodeRadius > MAX_RADIUS ){
                nodeRadius = MAX_RADIUS;
            }

            //影の設定
            mPaint.setShadowLayer(nodeRadius, 0, 0, mShadowColor);

            //Log.i("影範囲", "設定値=" + nodeRadius);
        } else {
            //影を削除
            mPaint.clearShadowLayer();
        }

        //再描画
        invalidate();
    }

    /*
     * 影の有無を切り替え
     */
/*
    public void switchShadow() {

        //状態を反転
        mIsShadow = !mIsShadow;

        if( mIsShadow ){
            //影の設定
            int width = findViewById(R.id.cv_node).getWidth();
            mPaint.setShadowLayer((width / 8f), 0, 0, mShadowColor);

        } else {
            //影を削除
            mPaint.clearShadowLayer();
        }

        //再描画
        invalidate();
    }
*/

    /*
     * ノード影の有無を取得
     */
/*    public boolean isShadow() {
        //影の有無を取得
        return mIsShadow;
    }*/

    /*
     * 影色の設定
     */
    public void setShadowColor(int colorHex, int nodeKind, boolean isShadow) {

        //ペイント未生成なら生成
        if( mPaint == null ){
            initPaint( nodeKind );
        }

        //色更新
        mShadowColor = colorHex;

        //影設定ありなら、設定色で描画
        if( isShadow ){
            setShadowOnOff( true, nodeKind );
        }
    }

    /*
     * 影色の取得
     */
    public String getShadowColor() {

        //★色は保持しておく
        //getShadowLayerColor()はAPIレベル対応が必要
        //return ( "#" + Integer.toHexString( (int)paint.getShadowLayerColorLong() ) );
        return ( "#" + Integer.toHexString( mShadowColor ) );
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if( mPaint == null ){
            return;
        }

        //ノードの横幅
        int width = findViewById(R.id.cv_node).getWidth();
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (width / 2f), mPaint);

        Log.i("サイズチェック", "onDraw レイアウト確定＝" + width);
    }

}

