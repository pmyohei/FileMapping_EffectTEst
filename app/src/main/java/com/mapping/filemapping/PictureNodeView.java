package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.Serializable;

public class PictureNodeView extends ChildNode implements Serializable  /*implements View.OnTouchListener*/ {

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public PictureNodeView(Context context, NodeTable node, PictureTable thumbnail, ActivityResultLauncher<Intent> nodeOperationLauncher) {
        super(context, node, nodeOperationLauncher, R.layout.picture_node);

        Log.i("PictureNodeView", "3");

        initNode(thumbnail);
    }

    /*
     * 初期化処理
     */
    private void initNode(PictureTable thumbnail) {

        Log.i("PictureNodeView", "init");

       //ノードに画像を設定
        setNodeBitmap(thumbnail);

        //ツールアイコン設定
        setNodeToolIcon();
    }

    /*
     * ノードに画像を設定
     */
    public void setNodeBitmap(PictureTable thumbnail) {

        //仮ガード-----
        if( mNode.getUriIdentify() == null ){
            return;
        }
        //-----

        //ContentResolver:コンテンツモデルへのアクセスを提供
        ContentResolver contentResolver = getContext().getContentResolver();

        //URI下のデータにアクセスする
        ParcelFileDescriptor pfDescriptor = null;
        try {
            //URI作成
            Uri uri = Uri.parse( ResourceManager.URI_PATH + mNode.getUriIdentify() );

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
                ((ImageView)findViewById(R.id.iv_node)).setImageBitmap( bmp );
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

    /*
     * ツールアイコン設定
     */
    public void setNodeToolIcon() {

    }
}
