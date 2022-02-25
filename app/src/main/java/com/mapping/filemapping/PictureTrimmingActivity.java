package com.mapping.filemapping;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.card.MaterialCardView;
import com.isseiaoki.simplecropview.CropImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class PictureTrimmingActivity extends AppCompatActivity {

    /*-- 定数 --*/


    //写真ギャラリー用ランチャー
    private ActivityResultLauncher<Intent> mPictureSelectLauncher;
    //画面レイアウト設定完了フラグ
    private boolean mIsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_trimming);

        //ツールバー設定
        Toolbar toolbar = findViewById(R.id.toolbar_trimming);
        toolbar.setTitle("写真ノードの選択");
        setSupportActionBar(toolbar);
        //戻るボタン
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Admobロード
        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        //権限付与
        //※query()を発行するとき、権限エラーになるためここでも指定が必要
        final int REQUEST_EXTERNAL_STORAGE = 1;
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

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

                                //本画面レイアウト設定
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

        //試ししたが、落ちる
        //pictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //pictureIntent.addCategory(Intent.ACTION_OPEN_DOCUMENT_TREE );

        //遷移
        mPictureSelectLauncher.launch(pictureIntent);
    }


    /*
     *　トリミング画像の設定
     */
    private void setTrimmingPicture(Bitmap bmp) {

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
                        mcv.setRadius(iv_cropped.getWidth() / 2f);

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
    private void setCropLayout(Uri contentUri) {

        //Bitmapを取得
        Bitmap bmp = getBitmapFromUri(contentUri);
        if( bmp == null ){
            //取得エラー
            Log.i("URI", "Bitmap生成エラー");
            return;
        }

        //トリミング画像の設定
        setTrimmingPicture(bmp);

        if (mIsLayout) {
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

                //遷移元からの情報
                Intent intent = getIntent();
                boolean isEdit = intent.getBooleanExtra(MapActivity.INTENT_EDIT, false);

                if( isEdit ){
                    //編集なら、サムネイルを変更
                    changeThumbnail( contentUri );
                } else{
                    //ノードを生成
                    createPictureNode(contentUri);
                }

            }
        });

        //レイアウト設定完了
        mIsLayout = true;
    }

    /*
     *　写真ノードの生成
     */
    private void createPictureNode(Uri uri) {

        //遷移元からの情報
        //★マップIDは共通情報にする
        Intent intent = getIntent();
        int mapPid = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
        int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);
        String[] colors = (String[]) intent.getSerializableExtra( MapActivity.INTENT_COLORS );

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) getApplication();

        //ノード初期位置を親ノードの中心位置から一定の距離離した位置にする
        NodeTable parentNode = mapCommonData.getNodeInMap(selectedNodePid);
        int posX = (int) parentNode.getCenterPosX() + ResourceManager.POS_NODE_INIT_OFFSET;
        int posY = (int) parentNode.getCenterPosY();

        Log.i("URI", "URI=" + uri.toString());

        //絶対パスを取得
        String path = ResourceManager.getPathFromUri(this, uri);
        if (path == null ) {
            //絶対パスの取得に失敗した場合
            Log.i("URI", "絶対パス取得エラー");
            //★
            Toast.makeText(this, "失敗しました", Toast.LENGTH_SHORT).show();
            return;
        }

        //ピクチャノードを生成
        NodeTable newNode = new NodeTable(
                "",
                mapPid,
                selectedNodePid,
                NodeTable.NODE_KIND_PICTURE,
                posX,
                posY
        );
        //カラーパターン設定
        newNode.setColorPattern( colors );

        //トリミング情報
        final CropImageView iv_cropTarget = findViewById(R.id.iv_cropTarget);
        RectF rectInfo = iv_cropTarget.getActualCropRect();

        //ピクチャ情報を生成
        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
        PictureTable picture = new PictureTable(
                mapPid,
                PictureTable.UNKNOWN,   //ピクチャノードのpidも未確定
                path,
                true,
                rectInfo);

        //DB保存処理
        AsyncCreatePictureNode db = new AsyncCreatePictureNode(this, newNode, picture, new AsyncCreatePictureNode.OnFinishListener() {

            @Override
            public void onFinish(int nodePid, PictureTable picture) {
                //データ挿入されたため、レコードに割り当てられたpidをテーブルに設定
                newNode.setPid(nodePid);

                //resultコード設定
                Intent retIntent = getIntent();
                retIntent.putExtra(ResourceManager.KEY_CREATED_NODE, newNode);    //生成したピクチャノード
                retIntent.putExtra(ResourceManager.KEY_THUMBNAIL, picture);       //生成したサムネピクチャ
                setResult(MapActivity.RESULT_PICTURE_NODE, retIntent);

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
     *　コンテンツURIからビットマップを取得
     */
    private Bitmap getBitmapFromUri( Uri uri ) {

        //ファイルを指す
        ParcelFileDescriptor pfDescriptor = null;
        try {
            //ContentResolver:コンテンツモデルへのアクセスを提供
            ContentResolver contentResolver = getContentResolver();

            //URI下のデータにアクセスする
            pfDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (pfDescriptor != null) {

                //実際のFileDescriptorを取得
                FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                pfDescriptor.close();

                //ビットマップを返す
                return bmp;
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (pfDescriptor != null) {
                    pfDescriptor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /*
     *　サムネイルの変更
     */
    private void changeThumbnail(Uri uri) {

        //遷移元からの情報
        Intent intent = getIntent();
        int mapPid = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
        int pictureNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

        //絶対パスを取得
        String path = ResourceManager.getPathFromUri(this, uri);
        if (path == null ) {
            //絶対パスの取得に失敗した場合
            Log.i("URI", "絶対パス取得エラー");
            //★
            Toast.makeText(this, "失敗しました", Toast.LENGTH_SHORT).show();
            finish();
        }

        //トリミング情報
        final CropImageView iv_cropTarget = findViewById(R.id.iv_cropTarget);
        RectF rectInfo = iv_cropTarget.getActualCropRect();

        //ピクチャ情報を生成
        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
        PictureTable picture = new PictureTable(
                mapPid,
                pictureNodePid,
                path,
                true,
                rectInfo);

        //DB保存処理
        AsyncUpdateThumbnail db = new AsyncUpdateThumbnail(this, picture, new AsyncUpdateThumbnail.OnFinishListener() {

            @Override
            public void onFinish(PictureTable oldPicture, PictureTable newPicture) {

                //変わったノードとサムネ情報を返す
                Intent retIntent = getIntent();
                retIntent.putExtra(ResourceManager.KEY_NEW_THUMBNAIL, newPicture);
                retIntent.putExtra(ResourceManager.KEY_OLD_THUMBNAIL, oldPicture);
                setResult(MapActivity.RESULT_UPDATE_TUHMBNAIL, retIntent);

                //元の画面へ戻る
                finish();
            }
        });

        //非同期処理開始
        db.execute();
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
                DesignBottomSheet l_bottomSheet = findViewById(R.id.dbs_shape);
                l_bottomSheet.openBottomSheet(DesignBottomSheet.SHAPE_ONLY, findViewById(R.id.mcv));

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

}