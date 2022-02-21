package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

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

/*        //サムネイルなし
        if (thumbnail == null) {
            //なし用のアイコンを設定
            iv_node.setBackgroundResource(R.drawable.baseline_no_thumbnail_24);
            return;
        }*/

        //サムネイルのBitmapを生成
        Bitmap bitmap = createThumbnail( getResources(), thumbnail );



        /*        if( bitmap == null ){
            //ビットマップ化に失敗した場合、なし用のアイコンを設定
            iv_node.setBackgroundResource(R.drawable.baseline_no_thumbnail_24);
            return;
        }*/

        //サムネイルを設定
        iv_node.setImageBitmap(bitmap);

/*        if( thumbnail != null ){
            //サムネイルのBitmapを生成
            Bitmap bitmap = createThumbnail( thumbnail );
            if( bitmap != null ){
                //Bitmapが生成できれば、その画像を設定
                iv_node.setImageBitmap(bitmap);
                return;
            }
        }*/

        //サムネイルなしか、ビットマップ化に失敗した場合、なし用のアイコンを設定
        //iv_node.setBackgroundResource( R.drawable.bmp_sample_cafe );

/*        if( thumbnail == null ){
            //サムネイル情報がなければ、なし用アイコンを設定
            iv_node.setBackgroundResource( R.drawable.bmp_sample_cafe );

        } else {
            //サムネイルのBitmapを生成
            Bitmap bitmap = createThumbnail( thumbnail );
            //設定
            iv_node.setImageBitmap(bitmap);
        }*/


/*
        String path = thumbnail.getPath();
        if (path == null) {
            //フェールセーフ
            Log.i("URI", "setBitmap() pathなし");
            return;
        }

        //トリミング範囲で切り取り
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        bitmap = Bitmap.createBitmap(bitmap, thumbnail.getTrgLeft(), thumbnail.getTrgTop(), thumbnail.getTrgWidth(), thumbnail.getTrgHeight());
*/
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
     * ノードに画像を設定
     */
/*
    public void setNodeBitmap(PictureTable thumbnail) {

        //仮ガード-----
        String path = mNode.getPath();
        if( path == null ){
            return;
        }
        //-----

        //ノードの中身の表示を変更
        findViewById(R.id.tv_node).setVisibility( GONE );
        findViewById(R.id.iv_node).setVisibility( VISIBLE );

        //ContentResolver:コンテンツモデルへのアクセスを提供
        ContentResolver contentResolver = getContext().getContentResolver();

        //URI下のデータにアクセスする
        ParcelFileDescriptor pfDescriptor = null;
        try {

            //Fike → InputStream → Bitmap
            File file = new File(path);
            Uri uri = Uri.fromFile(file);

            //URI作成
            //Uri uri = Uri.parse( ResourceManager.URI_PATH + mNode.getPath() );

            Log.i("setNodeBitmap", "uri=" + uri);

            //Descriptor取得
            pfDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if(pfDescriptor != null) {

                //実際のFileDescriptorを取得
                FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                pfDescriptor.close();

                Log.i("setNodeBitmap", "bmp.getHeight()=" + bmp.getHeight() + " bmp.width()=" + bmp.getWidth());

                //トリミング情報を反映
                //RectF rectF = thumbnail.getTrimmingInfo();
                //Log.i("setNodeBitmap", "rectF.getHeight()=" + rectF.height() + " rectF.width()=" + rectF.width());
                //bmp = Bitmap.createBitmap( bmp, (int)rectF.left, (int)rectF.top, (int)rectF.width(), (int)rectF.height() );
                bmp = Bitmap.createBitmap( bmp, thumbnail.getTrgLeft(), thumbnail.getTrgTop(), thumbnail.getTrgWidth(), thumbnail.getTrgHeight() );

                //画像を設定
                ImageView iv_node = findViewById(R.id.iv_node);
                iv_node.setImageBitmap( bmp );
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(pfDescriptor != null){
                    pfDescriptor.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
*/

}
