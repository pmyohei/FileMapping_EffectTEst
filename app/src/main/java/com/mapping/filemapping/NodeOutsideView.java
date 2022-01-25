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
    }

    /*
     * 影色の設定
     */
    public void setShadowColor(int colorHex) {
        //色更新
        mShadowColor = colorHex;

        //ノードの横幅
        int width = findViewById(R.id.cv_node).getWidth();
        //影の設定
        mPaint.setShadowLayer((width / 6f), 0, 0, mShadowColor);

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

