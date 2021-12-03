package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;

public class LineView extends View {

    //描画開始座標
    private float mStartPosX = 0;
    private float mStartPosY = 0;

    //描画終端座標
    private float mEndPosX = 0;
    private float mEndPosY = 0;

    /*
     * コンストラクタ
     */
    public LineView(Context context) {
        super(context);
    }

    /*
     * コンストラクタ
     */
    public LineView(Context context, float startPosX, float startPosY, float endPosX, float endPosY) {
        super(context);

        //描画開始・終了座標
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
     * 開始・終端位置の移動
     */
    public void moveStartEndPos(float startPosX, float startPosY, float endPosX, float endPosY) {

        //開始位置を更新
        mStartPosX = startPosX;
        mStartPosY = startPosY;

        //終端位置を更新
        mEndPosX = endPosX;
        mEndPosY = endPosY;

        //再描画
        invalidate();
    }

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

        //ペイント情報
        @SuppressLint("DrawAllocation") Paint paint = new Paint();
        paint.setStrokeWidth(2f);
        paint.setColor(Color.LTGRAY);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);

        //Path生成
        @SuppressLint("DrawAllocation") Path path = new Path();

        //スタート地点を移動
        path.moveTo(mStartPosX, mStartPosY);

        //制御点X, 制御点Y, 終点X, 終点Y
        path.quadTo(mStartPosX, (mStartPosY + mEndPosY) / 2, mEndPosX, mEndPosY);

        Log.i("onDraw", "mParentPosX=" + mStartPosX + " mParentPosY=" + mStartPosY);

        //描画
        canvas.drawPath(path, paint);
    }

}
