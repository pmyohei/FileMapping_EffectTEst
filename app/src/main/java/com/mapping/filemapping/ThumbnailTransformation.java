package com.mapping.filemapping;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.squareup.picasso.Transformation;

/*
 * サムネイル情報切り取り
 */
public class ThumbnailTransformation implements Transformation {

    //サムネイル情報
    PictureTable mThumbnail;

    public ThumbnailTransformation(PictureTable thumbnail ){
        mThumbnail = thumbnail;
    }

    @Override
    public Bitmap transform(Bitmap source) {

        int trgLeft = mThumbnail.getTrgLeft();
        int trgWidth = mThumbnail.getTrgWidth();

        //画像範囲のガード処理（横サイズ）
        int right = trgLeft + trgWidth;
        if( right > source.getWidth() ){
            //トリミング範囲に誤りがある場合は、元の画像の大きさで作成
            //※発生しない想定だが、想定外のことが起こった時用
            trgLeft = 0;
            trgWidth = source.getWidth();

            Log.i("Picassoでトリミング", "トリミングエラー：横サイズ");
        }


        int trgTop = mThumbnail.getTrgTop();
        int trgHeight = mThumbnail.getTrgHeight();

        //画像範囲のガード処理（縦サイズ）
        int bottom = trgTop + trgHeight;
        if( bottom > source.getHeight() ){
            //トリミング範囲に誤りがある場合は、元の画像の大きさで作成
            //※発生しない想定だが、想定外のことが起こった時用
            trgTop = 0;
            trgHeight = source.getHeight();

            Log.i("Picassoでトリミング", "トリミングエラー：横サイズ");
        }

        //トリミング範囲で切り取り
        Bitmap result = Bitmap.createBitmap(source, trgLeft, trgTop, trgWidth, trgHeight);
        if (result != source) {
            source.recycle();
        }

        return result;
    }

    @Override
    public String key() { return "square()"; }
}
