package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

public class LineView extends View {

    private float xZahyou = 0;
    private float yZahyou = 0;

    private float mStartPosX = 0;
    private float mStartPosY = 0;
    private float mEndPosX = 0;
    private float mEndPosY = 0;

    public LineView(Context context) {
        super(context);
    }


    public LineView(Context context, float startPosX, float startPosY, float endPosX, float endPosY) {
        super(context);

        mStartPosX = startPosX;
        mStartPosY = startPosY;
        mEndPosX   = endPosX;
        mEndPosY   = endPosY;

        //ノードに対して背面になるようにする（デフォルト値は0のため、0未満の値を指定）
        setTranslationZ(-1);
    }

/*    @Override
    public boolean onTouchEvent(MotionEvent event) {

        xZahyou = event.getX();
        yZahyou = event.getY();

        Log.i("onTouchEvent", "xZahyou=" + xZahyou + " yZahyou=" + yZahyou);

        this.invalidate();

        return true;
    }*/


    /*
     * 終端位置の移動
     */
    public void moveEndPos(float endPosX, float endPosY) {

        //終端位置を更新
        mEndPosX = endPosX;
        mEndPosY = endPosY;

        //再描画
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
/*
        // 半径10の円を描画する
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.RED);

        canvas.drawCircle(xZahyou, yZahyou, 10, p);
*/

        Paint paint = new Paint();
        paint.setStrokeWidth(2f);
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        Path path = new Path();
        // スタート地点を移動
        //path.moveTo(250, 250);
        path.moveTo(mStartPosX, mStartPosY);
        // 制御点1 X, 制御点1 Y, 制御点2 X, 制御点2Y, 終点X, 終点Y
        //path.cubicTo(450, 0, 500, 50, 500, 250);
        path.quadTo(mStartPosX, (mStartPosY + mEndPosY) / 2, mEndPosX, mEndPosY);
        //path.quadTo(100f, 800f, xZahyou, yZahyou);

        Log.i("onDraw", "mParentPosX=" + mStartPosX + " mParentPosY=" + mStartPosY);

        canvas.drawPath(path, paint);
    }

}
