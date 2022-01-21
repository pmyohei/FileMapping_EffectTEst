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


    public void setColorID(int colorID) {
        mPaint.setColor(getResources().getColor(colorID));
        invalidate();
    }

    public int getColorHex() {
        return mPaint.getColor();
    }

    public void setColorHex(int colorHex) {
        mPaint.setColor(colorHex);
        invalidate();
    }

    /*
     * 影色の設定
     */
    public void setShadowColor(int colorHex) {

        //影色の設定
        int width = findViewById(R.id.cv_node).getWidth();
        mPaint.setShadowLayer((width / 6f), 0, 0, colorHex);

        //再描画
        invalidate();
    }

    /*
     * 影色の取得
     */
    public String getShadowColor() {

        //★色は保持しておく
        //getShadowLayerColor()はAPIレベル対応が必要のため
        //return ( "#" + Integer.toHexString( (int)paint.getShadowLayerColorLong() ) );
        return "#aaaaaa";
    }


    @Override
    protected void onDraw(Canvas canvas) {

        int width = findViewById(R.id.cv_node).getWidth();

        Log.i("サイズチェック", "onDraw レイアウト確定＝" + width);

        //paint.setShadowLayer( (width / 4f), width / 4, getHeight() / 4, Color.RED );
        mPaint.setShadowLayer((width / 5f), 0, 0, Color.GRAY);

        //paint.setColor(getResources().getColor(R.color.mark_5));
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (width / 2f), mPaint);
    }

}

