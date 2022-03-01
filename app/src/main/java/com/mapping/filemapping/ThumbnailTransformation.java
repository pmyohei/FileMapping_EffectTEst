package com.mapping.filemapping;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.util.Log;

import com.squareup.picasso.Transformation;

/*
 * サムネイル情報切り取り
 */
public class ThumbnailTransformation implements Transformation {

    //画像読み込み時のリサイズ値
    public static final int RESIZE = 800;

    //サムネイル情報
    PictureTable mThumbnail;
    //画像を割り当てるビューのサイズ
    int mViewSize;

    public ThumbnailTransformation(PictureTable thumbnail, int size ){
        mThumbnail = thumbnail;
        mViewSize = size;
    }

    @Override
    public Bitmap transform(Bitmap source)
    {
        //ビューサイズ未確定の状態でコールされた場合、トリミングなし
        if( mViewSize == 0 ){
            Log.i("ThumbnailTransformation", "viewサイズ未確定");
            return source;
        }

        //現時点の画像サイズ（fit()やresize()適用後の画像サイズ）
        final float sourceWidth = source.getWidth();
        final float sourceHeight = source.getHeight();

        RectF rectInfo = mThumbnail.getTrimmingInfo();
        int trgLeft   = (int)(sourceWidth * ( rectInfo.left / mThumbnail.getSourceImageWidth() ));
        int trgWidth  = (int)(sourceWidth * ( rectInfo.width() / mThumbnail.getSourceImageWidth() ));
        int trgTop    = (int)(sourceHeight * ( rectInfo.top / mThumbnail.getSourceImageHeight() ));
        int trgHeight = (int)(sourceHeight * ( rectInfo.height() / mThumbnail.getSourceImageHeight() ));

        //画像範囲のガード処理（横サイズ）
        int right = trgLeft + trgWidth;
        if( right > sourceWidth ){
            //トリミング範囲に誤りがある場合は、元の画像の大きさで作成
            //※端末で画像名が変更になった場合など
            trgLeft = 0;
            trgWidth = source.getWidth();

            Log.i("トリミング問題", "トリミングエラー：横サイズ");
        }

        //画像範囲のガード処理（縦サイズ）
        int bottom = trgTop + trgHeight;
        if( bottom > sourceHeight ){
            //トリミング範囲に誤りがある場合は、元の画像の大きさで作成
            //※端末で画像名が変更になった場合など
            trgTop = 0;
            trgHeight = source.getHeight();

            Log.i("トリミング問題", "トリミングエラー：横サイズ");
        }

        //トリミング範囲で切り取り
        Bitmap bmpTrimming = Bitmap.createBitmap(source, trgLeft, trgTop, trgWidth, trgHeight);
        //割り当て先のviewサイズに設定
        Bitmap result = Bitmap.createScaledBitmap(bmpTrimming, mViewSize, mViewSize, true);

        if (result != source) {
            source.recycle();
        }
        return result;
    }

    @Override
    public String key() { return "square()"; }
}
