package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/*
 * ノードの外側用のビュー
 *   主に影／発光の役割を果たす
 */
public class NodeOutsideView extends LinearLayout {
    private final Paint mPaint;
    private int mShadowColor;
    private boolean mIsShadow;

    float mRadius = 0.0f;

    /*
     *　レイアウトから生成時用
     */
    public NodeOutsideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mPaint = new Paint();
        //paint.setColor( getResources().getColor( R.color.fill ) );
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);

        mIsShadow = false;
    }

    /*
     * 影の有無を設定
     */
    public void setShadow( boolean isShadow ) {

        if( isShadow ){
            //影の設定
            int width = findViewById(R.id.cv_node).getWidth();
            mPaint.setShadowLayer((width / 8f), 0, 0, mShadowColor);

        } else {
            //影を削除
            mPaint.clearShadowLayer();
        }

        //影の有無の設定を更新
        mIsShadow = isShadow;

        //再描画
        invalidate();
    }

    /*
     * 影の有無を切り替え
     */
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

    /*
     * ノード影の有無を取得
     */
    public boolean isShadow() {
        //影の有無を取得
        return mIsShadow;
    }

    /*
     * 影色の設定
     */
    public void setShadowColor(int colorHex) {
        //色更新
        mShadowColor = colorHex;
        mIsShadow    = true;

        //ノードの横幅
        int width = findViewById(R.id.cv_node).getWidth();
        //影の設定
        mPaint.setShadowLayer((width / 8f), 0, 0, mShadowColor);

        Log.i("影", "mShadowColor＝" + mShadowColor);

        //再描画
        invalidate();
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

        //ノードの横幅
        int width = findViewById(R.id.cv_node).getWidth();

        Log.i("サイズチェック", "onDraw レイアウト確定＝" + width);

        //mPaint.setShadowLayer((width / 6f), 0, 0, Color.RED);
        //paint.setColor(getResources().getColor(R.color.mark_5));
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (width / 2f), mPaint);
    }

}

