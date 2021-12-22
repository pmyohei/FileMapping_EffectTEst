package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.FileDescriptor;
import java.io.IOException;

public class PictureTrimmingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_trimming);

        //コンテンツURIを取得
        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra("URI");

        //ファイルを指す
        ParcelFileDescriptor pfDescriptor = null;
        try{

            //ContentResolver:コンテンツモデルへのアクセスを提供
            ContentResolver contentResolver = getContentResolver();

            //URI下のデータにアクセスする
            pfDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if(pfDescriptor != null){

                //実際のFileDescriptorを取得
                FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                pfDescriptor.close();

                //トリミング用のビューにセット
                final CropImageView cropImageView = (CropImageView)findViewById(R.id.cropImageView);
                cropImageView.setImageBitmap(bmp);
                //トリミング結果画像
                final ImageView croppedImageView = (ImageView)findViewById(R.id.croppedImageView);

                //ボタン押下
                Button cropButton = (Button)findViewById(R.id.crop_button);
                cropButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //フレームに合わせてトリミング
                        croppedImageView.setImageBitmap(cropImageView.getCroppedBitmap());

                        //トリミング情報取得
                        RectF rext = cropImageView.getActualCropRect();

                        Log.i("TrimmingActivity", "rext.top=" + rext.top + " rext.bottom=" + rext.bottom + " rext.right=" + rext.right + " rext.left=" + rext.left);
                    }
                });

                Button bt_positive = (Button)findViewById(R.id.bt_positive);
                bt_positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //遷移元からの情報
                        int mapPid          = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
                        int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

                        //マップ共通データ
                        MapCommonData mapCommonData = (MapCommonData)getApplication();

                        //ノード初期位置を親ノードの中心位置から一定の距離離した位置にする
                        NodeTable parentNode = mapCommonData.getNodeInMap( selectedNodePid );
                        int posX = (int)parentNode.getCenterPosX() + ResourceManager.POS_NODE_INIT_OFFSET;
                        int posY = (int)parentNode.getCenterPosY();

                        //URI情報
                        String[] uriSplit = uri.toString().split( ResourceManager.URI_SPLIT );
                        String uriIdentify = uriSplit[1];

                        //ノードを生成
                        //★
                        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
                        NodeTable newNode = new NodeTable();
                        newNode.setPidMap(mapPid);
                        newNode.setPidParentNode( selectedNodePid );
                        newNode.setKind(NodeTable.NODE_KIND_PICTURE);
                        newNode.setPos( posX, posY );
                        newNode.setUriIdentify( uriIdentify );

                        //トリミング情報
                        RectF rectInfo = cropImageView.getActualCropRect();

                        //ピクチャ情報を生成
                        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
                        PictureTable picture = new PictureTable();
                        picture.setPidParentNode( selectedNodePid );
                        picture.setTrimmingInfo( rectInfo );
                        picture.setUriIdentify( uriIdentify );

                        //DB保存処理
                        AsyncCreatePictureNode db = new AsyncCreatePictureNode(v.getContext(), newNode, picture, new AsyncCreatePictureNode.OnFinishListener() {

                            @Override
                            public void onFinish(int nodePid, int picturePid) {
                                //データ挿入されたため、レコードに割り当てられたpidをテーブルに設定
                                newNode.setPid( nodePid );
                                picture.setPid( picturePid );

                                //resultコード設定
                                Intent retIntent = getIntent();
                                retIntent.putExtra(ResourceManager.KEY_CREATED_NODE, newNode );    //生成したピクチャノード
                                //retIntent.putExtra(ResourceManager.KEY_URI, uriSplit[1] );         //Uri識別子
                                setResult(RESULT_OK, retIntent );

                                //元の画面へ戻る
                                finish();
                            }
                        });

                        //非同期処理開始
                        db.execute();

                        //Log.i("TrimmingActivity", "rectInfo.top=" + rectInfo.top + " rectInfo.bottom=" + rectInfo.bottom + " rectInfo.right=" + rectInfo.right + " rectInfo.left=" + rectInfo.left);
                        //Log.i("TrimmingActivity", "uri=" + uri);
                        //Log.i("TrimmingActivity", "uri identify=" + bb[1]);

                    }
                });

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
}