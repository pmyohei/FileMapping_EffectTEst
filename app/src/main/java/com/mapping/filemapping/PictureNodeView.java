package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
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


        findViewById(R.id.iv_node).post(()-> {
            Log.i("サイズ確定", "post ノードサイズ=" + findViewById(R.id.iv_node).getWidth());
            //ノードに画像を設定
            setThumbnail();
        });

        //ノードに画像を設定
        //setThumbnail();
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
     *   ※本処理は、ノードレイアウトが確定してから（サイズが確定してから）コールすること
     */
    private void setBitmap(PictureTable thumbnail) {
        //画像ビュー
        ImageView iv_node = findViewById(R.id.iv_node);
        //path
        String path = ( (thumbnail == null) ? "": thumbnail.getPath() );

        //別のマップのサムネイルがキャッシュされている可能性があるため、キャッシュを削除する
        //※これをしないと、別のマップに同じサムネイルがあったとき、そのサムネイル情報で表示されてしまう
        Picasso.get().invalidate( new File(path) );

        //画像割り当て
        //※fit()ではなく、一定値を指定したresize()を使用
        // fit()だと、ノードサイズが小さく、解像度が下がるため。
        // ある程度の解像度を確保するためにresize()を使用する。
        // 仮にマップ画面が重くなる場合は、このリサイズ値を見直す
        Picasso.get()
                .load( new File(path) )
                .resize( ThumbnailTransformation.RESIZE, ThumbnailTransformation.RESIZE)
                .transform( new ThumbnailTransformation( thumbnail, iv_node.getWidth() ) )
                .error(R.drawable.baseline_no_thumbnail_24)
                .into( iv_node );
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
