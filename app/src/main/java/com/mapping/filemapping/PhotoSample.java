package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PhotoSample extends View {

    private Paint paint;
    public Bitmap bitmap;

    public PhotoSample(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 画像、Exif情報をdraw
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }

}
