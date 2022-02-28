package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.Serializable;

public class PictureNodeView extends ChildNode implements Serializable  /*implements View.OnTouchListener*/ {

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public PictureNodeView(Context context, NodeTable node) {
        super(context, node, R.layout.picture_node);

        Log.i("PictureNodeView", "3");

        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {

        Log.i("PictureNodeView", "init");

        //ノードの中身の表示を変更
        findViewById(R.id.tv_node).setVisibility(GONE);
        findViewById(R.id.iv_node).setVisibility(VISIBLE);

        //ノードに画像を設定
        setThumbnail();
    }

    /*
     * ノードに画像を設定
     */
    private void setThumbnail() {
        //本ノードのサムネイルを取得
        MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
        PictureTable thumbnail = mapCommonData.getThumbnails().getThumbnail(mNode.getPid());

        setBitmap(thumbnail);
    }

    /*
     * ノード画像の更新
     */
    public void updateThumbnail(PictureTable thumbnail) {
        //ノードに画像を設定
        setBitmap(thumbnail);
    }

    /*
     * ノードに画像を設定
     */
    private void setBitmap(PictureTable thumbnail) {
        //画像ビュー
        ImageView iv_node = findViewById(R.id.iv_node);

        //path
        String path = ( (thumbnail == null) ? "": thumbnail.getPath() );

        Log.i("Picassoでトリミング", "setBitmap path=" + path);
        if( path.equals( "/storage/emulated/0/DCIM/Camera/PXL_20211120_080210517.jpg" ) ){
            Log.i("Picassoでトリミング", "最後の画像だけやめる");
            //return;
        }

        //画像割り当て
        Picasso.get()
                .load( new File( path ) )
                .transform( new ThumbnailTransformation(  thumbnail ) )
                .error(R.drawable.baseline_no_thumbnail_24)
                .into( iv_node );

        //サムネイルのBitmapを生成
        //Bitmap bitmap = createThumbnail( getResources(), thumbnail );
        //サムネイルを設定
        //iv_node.setImageBitmap(bitmap);
    }

    /*
     * サムネイルとなるBitmapを生成
     */
    public static Bitmap createThumbnail(Resources resources, PictureTable thumbnail)  {

        if (thumbnail == null) {
            Log.i("URI", "createThumbnail() 指定サムネイル=null");
            return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
        }

        String path = thumbnail.getPath();
        if (path == null) {
            //フェールセーフ
            Log.i("URI", "createThumbnail() pathなし");
            return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
        }

        //トリミング範囲で切り取り
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return Bitmap.createBitmap(bitmap, thumbnail.getTrgLeft(), thumbnail.getTrgTop(), thumbnail.getTrgWidth(), thumbnail.getTrgHeight());
    }

    /*
     * サムネイルとなるBitmapを生成
     */
    public static Bitmap createThumbnail_old(Resources resources, PictureTable thumbnail)  {

        if (thumbnail == null) {
            Log.i("URI", "createThumbnail() 指定サムネイル=null");
            return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
        }

        String path = thumbnail.getPath();
        if (path == null) {
            //フェールセーフ
            Log.i("URI", "createThumbnail() pathなし");
            return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
        }

        //トリミング範囲で切り取り
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        return Bitmap.createBitmap(bitmap, thumbnail.getTrgLeft(), thumbnail.getTrgTop(), thumbnail.getTrgWidth(), thumbnail.getTrgHeight());
    }


    /*
     * サムネイル情報切り取り
     */
/*
    public static class ThumbnailTransformation_old implements Transformation {

        //サムネイル情報
        PictureTable mThumbnail;

        public ThumbnailTransformation_old(PictureTable thumbnail ){
            mThumbnail = thumbnail;
        }

        @Override
        public Bitmap transform(Bitmap source) {

*/
/*            if (mThumbnail == null) {
                Log.i("URI", "createThumbnail() 指定サムネイル=null");
                return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
            }

            String path = mThumbnail.getPath();
            if (path == null) {
                //フェールセーフ
                Log.i("URI", "createThumbnail() pathなし");
                return BitmapFactory.decodeResource( resources, R.drawable.baseline_no_thumbnail_24);
            }*//*


            //トリミング範囲で切り取り
            Bitmap result = Bitmap.createBitmap(source, mThumbnail.getTrgLeft(), mThumbnail.getTrgTop(), mThumbnail.getTrgWidth(), mThumbnail.getTrgHeight());

            //Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
            if (result != source) {
                source.recycle();
            }

            return result;
        }

        @Override
        public String key() { return "square()"; }
    }
*/

}
