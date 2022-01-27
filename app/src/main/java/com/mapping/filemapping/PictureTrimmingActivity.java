package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.card.MaterialCardView;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;

public class PictureTrimmingActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_PICTURE_NODE = 200;

    //写真ギャラリー用ランチャー
    private ActivityResultLauncher<Intent> mPictureSelectLauncher;
    //画面レイアウト設定完了フラグ
    private boolean mIsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_trimming);

        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("写真ノードの選択");
        setSupportActionBar(toolbar);
        //戻るボタン
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //レイアウト設定未完了
        mIsLayout = false;

        //画面遷移ランチャー（写真ギャラリー）
        mPictureSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {

                    /*
                     * 写真ギャラリー画面からの戻り処理
                     */
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        Log.i("PicNodeActivity", "onActivityResult()");

                        if (result.getResultCode() == RESULT_OK) {

                            Intent intent = result.getData();
                            if (intent != null) {
                                //コンテンツURIを取得
                                Uri contentUri = intent.getData();

                                //トリミング画面を設定
                                setCropLayout(contentUri);

                                return;
                            }
                        }

                        //画面終了
                        finish();
                    }
                }
        );

        //写真ギャラリーの表示
        openPictureGallery();
    }

    /*
     *　写真ギャラリーの表示
     */
    private void openPictureGallery() {
        //写真を一覧で表示
        Intent pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        //Intent pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        //pictureIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);     //画像の複数選択を可能にする
        pictureIntent.setType("image/*");

        //遷移
        mPictureSelectLauncher.launch(pictureIntent);
    }


    /*
     *　トリミング画像の設定
     */
    private void setTrimmingPicture( Bitmap bmp ) {

        //トリミング結果の画像
        final ImageView iv_cropped = findViewById(R.id.iv_cropped);

        //レイアウト確定待ち
        ViewTreeObserver observer = iv_cropped.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //形状（円）を設定
                        MaterialCardView mcv = findViewById(R.id.mcv);
                        mcv.setRadius( iv_cropped.getWidth() / 2f );

                        //レイアウト確定後は、不要なので本リスナー削除
                        iv_cropped.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );

        //トリミング対象の画像
        final CropImageView iv_cropTarget = findViewById(R.id.iv_cropTarget);
        iv_cropTarget.setImageBitmap(bmp);

        //レイアウト確定待ち
        observer = iv_cropTarget.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //初期位置のトリミング範囲を「トリミング結果の画像」に適用
                        iv_cropped.setImageBitmap(iv_cropTarget.getCroppedBitmap());

                        //レイアウト確定後は、不要なので本リスナー削除
                        iv_cropTarget.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    /*
     *　本画面のレイアウト設定
     */
    private void setCropLayout( Uri uri ) {

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

                //トリミング画面の設定
                setTrimmingPicture( bmp );

                if( mIsLayout ){
                    //レイアウト設定済みならここで終了
                    return;
                }

                //トリミング結果
                final ImageView iv_cropped = findViewById(R.id.iv_cropped);
                //トリミング対象
                final CropImageView iv_cropTarget = findViewById(R.id.iv_cropTarget);

                //トリミング範囲が変更されたとき、その時点のトリミング範囲を設定
                iv_cropTarget.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        //フレームに合わせてトリミング
                        iv_cropped.setImageBitmap(iv_cropTarget.getCroppedBitmap());
                        //イベント処理完了
                        return false;
                    }
                });

                //作成ボタン
                Button bt_create = findViewById(R.id.bt_create);
                bt_create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ノードを生成
                        createPictureNode( uri );
                    }
                });

                //レイアウト設定完了
                mIsLayout = true;
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
     *　写真ノードの生成
     */
    private void createPictureNode( Uri uri ) {

        //遷移元からの情報
        //★マップIDは共通情報にする
        Intent intent = getIntent();
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
/*
        NodeTable newNode = new NodeTable();
        newNode.setPidMap(mapPid);
        newNode.setPidParentNode( selectedNodePid );
        newNode.setKind(NodeTable.NODE_KIND_PICTURE);
        newNode.setPos( posX, posY );
        newNode.setUriIdentify( uriIdentify );
*/

        //ノードを生成
        NodeTable newNode = new NodeTable(
                "",
                mapPid,
                selectedNodePid,
                NodeTable.NODE_KIND_PICTURE,
                posX,
                posY
        );
        newNode.setUriIdentify( uriIdentify );


        //トリミング情報
        final CropImageView iv_cropTarget = findViewById(R.id.iv_cropTarget);
        RectF rectInfo = iv_cropTarget.getActualCropRect();

        //ピクチャ情報を生成
        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
        PictureTable picture = new PictureTable();
        picture.setPidMap( mapPid );
        picture.setPidParentNode( selectedNodePid );
        picture.setTrimmingInfo( rectInfo );
        picture.setUriIdentify( uriIdentify );

        //DB保存処理
        AsyncCreatePictureNode db = new AsyncCreatePictureNode(iv_cropTarget.getContext(), newNode, picture, new AsyncCreatePictureNode.OnFinishListener() {

            @Override
            public void onFinish(int nodePid, int picturePid) {
                //データ挿入されたため、レコードに割り当てられたpidをテーブルに設定
                newNode.setPid( nodePid );
                picture.setPid( picturePid );

                //resultコード設定
                Intent retIntent = getIntent();
                retIntent.putExtra(ResourceManager.KEY_CREATED_NODE, newNode );    //生成したピクチャノード
                retIntent.putExtra(ResourceManager.KEY_THUMBNAIL, picture );       //生成したサムネピクチャ
                setResult(RESULT_PICTURE_NODE, retIntent );

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

    /*
     * ツールバーオプションメニュー生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_trimming, menu);

        return true;
    }

    /*
     * ツールバー 戻るボタン押下処理
     */
    @Override
    public boolean onSupportNavigateUp() {
        //アクティビティ終了
        finish();

        return super.onSupportNavigateUp();
    }

    /*
     * ツールバーアクション選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_garally:
                //写真ギャラリーを表示
                openPictureGallery();
                return true;

            case R.id.action_shape:
                //ノードの形状設定
                DesignBottomSheet bs_design = findViewById(R.id.bs_design);
                bs_design.openBottomSheet(DesignBottomSheet.SHAPE_ONLY, findViewById(R.id.mcv));

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}