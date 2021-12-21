package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Locale;

public class PictureNodeSelectActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_PICTURE_NODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_node_select);

        //画面遷移ランチャーを生成
        ActivityResultLauncher<Intent> PictureSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    /*
                     * 画面遷移先からの戻り処理
                     */
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("PicNodeActivity", "onActivityResult()");

                        if(result.getResultCode() == RESULT_OK) {

                            Intent intent = result.getData();
                            if(intent != null){

                                //ファイルを指す
                                ParcelFileDescriptor pfDescriptor = null;

                                try{
                                    //コンテンツURIを取得
                                    Uri contentUri = intent.getData();

                                    // Uriを表示
                                    //TextView tv_uri = findViewById(R.id.tv_uri);
                                    //tv_uri.setText(String.format(Locale.US, "Uri:　%s",contentUri.toString()));
//
                                    //Log.d("contentUri", "URI=" + tv_uri.getText());

                                    //ContentResolver:コンテンツモデルへのアクセスを提供
                                    ContentResolver contentResolver = getContentResolver();

                                    //URI下のデータにアクセスする
                                    pfDescriptor = contentResolver.openFileDescriptor(contentUri, "r");
                                    if(pfDescriptor != null){

                                        //実際のFileDescriptorを取得
                                        FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                                        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                                        pfDescriptor.close();

                                        //トリミング画面へ遷移する
                                        //★一旦後で


                                        //URIを元の画面へ
                                        Intent retrunIntent = getIntent();
                                        retrunIntent.putExtra( "URI",  contentUri);
                                        setResult(RESULT_PICTURE_NODE, intent );

                                        //ImageView imageView = findViewById(R.id.imageView);
                                        //imageView.setImageBitmap(bmp);
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

                            //画面終了
                            finish();
                        }
                    }
                }
        );


        //写真を一覧で表示
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);     //画像の複数選択を可能にする
        intent.setType("image/*");

        PictureSelectLauncher.launch( intent );
        //startActivityForResult(intent, 111);
    }
}