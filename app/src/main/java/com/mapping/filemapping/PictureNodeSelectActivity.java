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

        //画面遷移元からの情報
        Intent intent = getIntent();
        //★マップIDは共通情報にする
        int mapPid          = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
        int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

        //画面遷移ランチャー（トリミング画面）
        ActivityResultLauncher<Intent> trimmingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    /*
                     * トリミング画面からの戻り処理
                     */
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("PicNodeActivity", "trimmingLauncher onActivityResult()");

                        if(result.getResultCode() == RESULT_OK) {

                            Intent intent = result.getData();
                            if(intent != null){
                                //resultコード設定
                                //※トリミング画面からの戻り値には、intentに入っているためそのまま渡す
                                setResult(RESULT_PICTURE_NODE, intent );
                            }

                            //画面終了
                            finish();
                        }
                    }
                }
        );

        //画面遷移ランチャー（写真ギャラリー）
        ActivityResultLauncher<Intent> pictureSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    /*
                     * 写真ギャラリー画面からの戻り処理
                     */
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("PicNodeActivity", "onActivityResult()");

                        if(result.getResultCode() == RESULT_OK) {

                            Intent intent = result.getData();
                            if(intent != null){
                                //コンテンツURIを取得
                                Uri contentUri = intent.getData();

                                //トリミング画面へ遷移
                                Intent nextIntent = new Intent(PictureNodeSelectActivity.this, PictureTrimmingActivity.class);
                                nextIntent.putExtra( "URI",  contentUri);
                                nextIntent.putExtra( MapActivity.INTENT_MAP_PID, mapPid);
                                nextIntent.putExtra( MapActivity.INTENT_NODE_PID, selectedNodePid);

                                trimmingLauncher.launch( nextIntent );

                            } else{
                                //画面終了
                                finish();
                            }

                        }
                    }
                }
        );


        //写真を一覧で表示
        Intent pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //Intent pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //pictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);     //画像の複数選択を可能にする
        pictureIntent.setType("image/*");

        pictureSelectLauncher.launch( pictureIntent );
        //startActivityForResult(pictureIntent, 111);
    }
}