package com.mapping.filemapping;

import static android.media.ExifInterface.ORIENTATION_NORMAL;
import static android.media.ExifInterface.ORIENTATION_ROTATE_90;

import static com.isseiaoki.simplecropview.CropImageView.RotateDegrees.ROTATE_90D;
import static com.mapping.filemapping.ResourceManager.SQUARE_CORNER_RATIO;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ExifInterface;
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
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.isseiaoki.simplecropview.CropImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Objects;

public class TrimmingActivity extends AppCompatActivity {

    //画像選択エラー種別
    private final int ERROR_KIND_PATH = 1;              //選択フォルダエラー
    private final int ERROR_KIND_SAME_THUMNBNAIL = 2;   //同じサムネイルあり
    private final int ERROR_KIND_REMOVED = 3;           //選択中画像が端末から削除されたs

    //写真ギャラリー用ランチャー
    private ActivityResultLauncher<Intent> mPictureSelectLauncher;
    //画面レイアウト設定完了フラグ
    private boolean mIsLayout;
    //画像回転有無
    private boolean mIsRotate;
    //選択画像のURI／Path
    private Uri mUri;
    private String mPath;
    //サムネイルリスト
    private PictureArrayList<PictureTable> mThumbnails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_trimming);

        //ツールバー設定
        setToolBar();

        //画像未回転
        mIsRotate = false;
        //レイアウト設定未完了
        mIsLayout = false;

        Context context = this;

        //画面遷移ランチャー（写真ギャラリー）
        mPictureSelectLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        if (result.getResultCode() == RESULT_OK) {
                            Intent intent = result.getData();
                            if (intent != null) {
                                //コンテンツURIを取得
                                Uri uri = intent.getData();
                                String path = ResourceManager.getPathFromUri(context, uri);

                                //パス生成エラーの場合、
                                if (path == null) {
                                    //端末の画像フォルダから選択する旨を表示
                                    //※画像フォルダからアクセスしていないとみなす
                                    confirmOpenStorage( ERROR_KIND_PATH );
                                    return;
                                }

                                //同じ画像がサムネイルとして既にあるなら、もう一度ギャラリーを表示
                                boolean hasThumnbnail = hasThumbnail(path);
                                if (hasThumnbnail) {
                                    //同じ画像が既に設定されている旨を表示
                                    confirmOpenStorage(ERROR_KIND_SAME_THUMNBNAIL);
                                    return;
                                }

                                //コンテンツURIを保持
                                mUri = uri;
                                mPath = path;

                                //本画面レイアウト設定
                                setCropLayout();
                                return;
                            }
                        }

                        //画面終了
                        finish();
                    }
                }
        );

        //マップid
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        int mapPid = mapCommonData.getMapPid();

        //サムネイル取得
        AsyncReadThumbnail db = new AsyncReadThumbnail(this, mapPid, new AsyncReadThumbnail.OnFinishListener() {
            @Override
            public void onFinish(PictureArrayList<PictureTable> thumbnails) {
                //サムネイルリストを保持
                mThumbnails = thumbnails;

                //端末内の写真ギャラリーを表示
                openPictureGallery();
            }
        });
        db.execute();

        //作成ボタン
        final Button bt_create = findViewById(R.id.bt_create);
        bt_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //画像未選択なら、ギャラリーを表示
                if( mUri == null ){
                    openPictureGallery();
                    return;
                }

                //遷移元からの情報
                Intent intent = getIntent();
                boolean isEdit = intent.getBooleanExtra(MapActivity.INTENT_EDIT, false);

                if (isEdit) {
                    //編集なら、サムネイルを変更
                    changeThumbnail();
                } else {
                    //ノードを生成
                    createPictureNode();
                }
            }
        });

    }

    /*
     * ツールバーの設定
     */
    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar_trimming);
        toolbar.setTitle("");           //タイトルは非表示（フォントが適用されていないため）
        setSupportActionBar(toolbar);
        //戻るボタン
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //システムバー
        getWindow().setStatusBarColor(Color.BLACK);
    }

    /*
     *　写真ギャラリーの表示
     */
    private void openPictureGallery() {
        //写真を一覧で表示
        Intent pictureIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pictureIntent.addCategory(Intent.CATEGORY_OPENABLE);
        pictureIntent.setType("image/*");

        //画面の向きを現在の向きで固定化
        //※これをしないと、「外部ストレージにアクセス→画面向き変更→写真選択」でエラーになる
        //※現状の仕様では、本画面は縦固定のため、横のルートにいくことはない。
        //※外部ストレージの画面向きを固定する設定ではない。あくまでトリミング画面用。
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //外部ストレージにアクセス
        mPictureSelectLauncher.launch(pictureIntent);
    }

    /*
     *　アラートダイアログを表示
     */
    private void confirmOpenStorage( int king ) {

        int titleID;
        int messageID;

        switch ( king ){
            case ERROR_KIND_PATH:
                titleID = R.string.alert_trimming_disableDirectory_title;
                messageID = R.string.alert_trimming_disableDirectory_message;
                break;

            case ERROR_KIND_SAME_THUMNBNAIL:
                titleID = R.string.alert_trimming_sameThumbnail_title;
                messageID = R.string.alert_trimming_sameThumbnail_message;
                break;

            case ERROR_KIND_REMOVED:
                titleID = R.string.alert_trimming_removed_title;
                messageID = R.string.alert_trimming_removed_message;
                break;

            //ありえないルート
            default:
                titleID = R.string.alert_trimming_disableDirectory_title;
                messageID = R.string.alert_trimming_disableDirectory_message;
                break;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle( getString(titleID) )
            .setMessage( getString(messageID) )
            .setPositiveButton(getString(R.string.alert_trimming_anotherPicture_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //端末ギャラリーを開く
                    openPictureGallery();
                }
            })
            //.setCancelable(false)   //キャンセル不可（ボタン押下しない限り閉じない）
            .show();

        //メッセージ文は、Styleのフォントが適用されないため個別に設定
        ((TextView)dialog.findViewById(android.R.id.message)).setTypeface( Typeface.SERIF );
    }

    /*
     *　サムネイルが既にあるかどうか
     *    ただし、設定中の画像であれば、なしとする（トリミング範囲の変更であるため）
     */
    private boolean hasThumbnail( String path ) {

        //遷移元からの情報
        Intent intent = getIntent();
        boolean isEdit = intent.getBooleanExtra(MapActivity.INTENT_EDIT, false);

        //マップ上に同じ画像があるかどうか
        boolean has = mThumbnails.hasPicture(path);

        //新規作成なら、有無の結果をそのまま返す
        if (!isEdit) {
            return has;
        }
        //サムネイル編集の場合、なければなしを返す
        if (!has) {
            return false;
        }

        //ある場合でも、変更対象のノードに現在割りあたっている画像なら選択可能（なし扱い）とする
        int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);
        if (mThumbnails.getPicture(selectedNodePid, path) != null) {
            return false;
        }

        return true;
    }


    /*
     *　トリミング結果の写真を割り当て
     */
    private void setCroppedPicture() {

        //トリミング結果を反映
        ImageView iv_toThumbnail = findViewById(R.id.iv_toThumbnail);
        CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);
        iv_toThumbnail.setImageBitmap(iv_cropSource.getCroppedBitmap());

        /*-- Picassoは使わない --*/
        /*-- ・Bitmapがキャッシュに格納されてしまうため、同じ画像だとTrancelate()がコールされない --*/
        /*-- ・キャッシュを無効化しても、反映が遅い --*/
    }

    /*
     *　本画面のレイアウト設定
     */
    private void setCropLayout() {

        //Bitmapを取得
        Bitmap bmp = getBitmapFromUri(mUri);
        if (bmp == null) {
            //取得エラー
            Log.i("URI", "Bitmap生成エラー");
            return;
        }
        //トリミング画像の設定
        setTrimmingPicture();
        //レイアウト初回設定
        firstSetLayout();
    }

    /*
     *　トリミング画像の設定
     */
    private void setTrimmingPicture() {

        //トリミング結果の画像
        final ImageView iv_toThumbnail = findViewById(R.id.iv_toThumbnail);

        //レイアウト確定待ち
        ViewTreeObserver observer = iv_toThumbnail.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    //形状（円）を設定
                    MaterialCardView mcv = findViewById(R.id.mcv_cropped);
                    mcv.setRadius(iv_toThumbnail.getWidth() / 2f);

                    //レイアウト確定後は、不要なので本リスナー削除
                    iv_toThumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        );

        //画面の向きを取得
        int orientation = ORIENTATION_NORMAL;
        try {
            ExifInterface exifInterface = new ExifInterface(mPath);
            orientation = Integer.parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION));

        } catch (Exception e) {
            Log.e(e.toString(), "ExifInterface Error");
        }

        //トリミング対象の画像
        //※画像の割り当て完了後、画像の向きに合わせた回転を行う
        final CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);
        Picasso.get()
                .load(mUri)
                .error(R.drawable.ic_no_image)
                .into(iv_cropSource, new PicassoCallback(orientation));
    }

    /*
     *　レイアウト初回設定
     */
    @SuppressLint("ClickableViewAccessibility")
    private void firstSetLayout() {

        if (mIsLayout) {
            //レイアウト設定済みならここで終了
            return;
        }

        //トリミング範囲の反映処理
        //※画像が回転しているかどうかで、反映タイミングを分ける
        //　回転させた画像に対して随時反映は重過ぎるため
        final CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);
        iv_cropSource.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (mIsRotate) {
                    //画像が回転されていれば、タッチが離れたタイミングでトリミング結果を反映させる
                    //※タッチ中常に反映するのは、重すぎるため
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        //トリミング結果を反映
                        setCroppedPicture();
                    }
                } else {
                    //画像未回転なら、随時反映

                    //トリミング結果を反映
                    setCroppedPicture();
                }

                return false;
            }
        });

        //レイアウト設定完了
        mIsLayout = true;
    }

    /*
     *　写真ノードの生成
     */
    private void createPictureNode() {

        //遷移元からの情報
        Intent intent = getIntent();
        int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

        //マップ共通データ
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        MapTable map = mapCommonData.getMap();
        //色パターン
        String[] colors = map.getDefaultColors();

        NodeTable parentNode = mapCommonData.getNodeInMap(selectedNodePid);
        //初期生成位置オフセット
        int initRelativePos = (int)getResources().getDimension(R.dimen.init_relative_pos);
        //スケールを考慮したノード半径
        int radius = (int)(parentNode.getNodeView().getScaleWidth() / 2f);
        //ノード初期位置を親ノードから一定の距離離した位置にする
        int posX = (int) parentNode.getPosX() + radius + initRelativePos;
        int posY = (int) parentNode.getPosY();

        //マップPID
        int mapPid = map.getPid();

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
        newNode.setColorPattern(colors);
        //形状の設定
        newNode.setNodeShape(getNodeShape());
        //影の有無を設定
        newNode.setShadow(map.isShadow());

        //トリミング情報
        final CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);
        RectF rectInfo = iv_cropSource.getActualCropRect();

        Bitmap bmp = getBitmapFromUri(mUri);
        //Log.i("トリミング問題", "生成　bmp.getWidth=" + bmp.getWidth());
        //Log.i("トリミング問題", "生成　bmp.getHeight=" + bmp.getHeight());
        //Log.i("トリミング問題", "生成　rectInfo.left=" + rectInfo.left);
        //Log.i("トリミング問題", "生成　rectInfo.top=" + rectInfo.top);
        //Log.i("トリミング問題", "生成　rectInfo.width()=" + rectInfo.width());
        //Log.i("トリミング問題", "生成　rectInfo.height()=" + rectInfo.height());

        //画像が回転している場合は、縦横のサイズは反対になる
        int width = ((mIsRotate) ? bmp.getHeight() : bmp.getWidth());
        int height = ((mIsRotate) ? bmp.getWidth() : bmp.getHeight());

        //ピクチャ情報を生成
        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
        PictureTable picture = new PictureTable(
                mapPid,
                PictureTable.UNKNOWN,   //ピクチャノードのpidも未確定
                mPath);
        //トリミング情報を設定
        picture.setTrimmingInfo(rectInfo, width, height);

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
     *　設定中の形状の取得
     */
    private int getNodeShape() {
        //トリミング結果の画像
        float imageRadius = findViewById(R.id.iv_toThumbnail).getWidth();
        float cardCornerRadius = ((MaterialCardView) findViewById(R.id.mcv_cropped)).getRadius();

        //四角形の場合よりも確実に大きい値
        float largerSquare = imageRadius * (SQUARE_CORNER_RATIO * 2f);

        //「四角形の場合に設定される値より確実に大きい値」よりも角の値が小さい場合、四角と判断する
        //※形の変更が発生しない場合、厳密に角サイズがimageに対して半分の値になっていない場合があるため
        return ( (largerSquare > cardCornerRadius) ? NodeTable.SQUARE : NodeTable.CIRCLE );
    }

    /*
     *　コンテンツURIからビットマップを取得
     */
    private Bitmap getBitmapFromUri(Uri uri) {

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
    private void changeThumbnail() {

        //遷移元からの情報
        Intent intent = getIntent();
        int mapPid = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
        int pictureNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

        //トリミング情報
        final CropImageView iv_cropTarget = findViewById(R.id.iv_cropSource);
        RectF rectInfo = iv_cropTarget.getActualCropRect();

        //画像が回転している場合は、縦横のサイズは反対になる
        Bitmap bmp = getBitmapFromUri(mUri);
        int width = ((mIsRotate) ? bmp.getHeight() : bmp.getWidth());
        int height = ((mIsRotate) ? bmp.getWidth() : bmp.getHeight());

        //ピクチャ情報を生成
        //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
        PictureTable picture = new PictureTable(
                mapPid,
                pictureNodePid,
                mPath);
        //トリミング情報を設定
        picture.setTrimmingInfo(rectInfo, width, height);

        //DB保存処理
        AsyncUpdateThumbnail db = new AsyncUpdateThumbnail(this, picture, new AsyncUpdateThumbnail.OnFinishListener() {
            @Override
            public void onFinish( PictureTable newPicture) {
                //変わったノードとサムネ情報を返す
                Intent retIntent = getIntent();
                retIntent.putExtra(ResourceManager.KEY_NEW_THUMBNAIL, newPicture);
                setResult(MapActivity.RESULT_UPDATE_TUHMBNAIL, retIntent);

                //元の画面へ戻る
                finish();
            }
        });

        //非同期処理開始
        db.execute();
    }


    /*
     * onRestart()
     */
    @Override
    protected void onRestart(){
        super.onRestart();

        //画像未選択の状態なら、なにもしない
        if( mUri == null ){
            return;
        }

        //端末の画像の保存状態が変更された可能性があるため、画像を更新
        String path = ResourceManager.getPathFromUri(this, mUri);
        if( path == null ){
            //パスが生成できないなら、端末から削除されたとみなす

            //設定中の画像を無効化
            ImageView iv_toThumbnail = findViewById(R.id.iv_toThumbnail);
            CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);
            iv_toThumbnail.setImageResource( R.drawable.ic_no_image);
            iv_cropSource.setImageBitmap( null );

            //メッセージを表示
            confirmOpenStorage( ERROR_KIND_REMOVED );

            //URI初期化
            mUri = null;
        }
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
                l_bottomSheet.openBottomSheet(DesignBottomSheet.SHAPE_ONLY, findViewById(R.id.mcv_cropped));

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*
     * 画像割り当て完了コールバック
     */
    private class PicassoCallback implements Callback{

        int mOrientation;

        public PicassoCallback( int orientation ){
            mOrientation = orientation;
        }

        @Override
        public void onSuccess() {

            if( mOrientation == ORIENTATION_ROTATE_90 ){
                //回転あり
                mIsRotate = true;
                //回転時の処理
                setRotateImage();
            } else {
                //回転なし
                mIsRotate = false;
                //トリミング範囲を反映
                setCroppedPicture();
            }
        }

        @Override
        public void onError(Exception e) {
        }

        /*
         * 画像回転処理
         */
        private void setRotateImage(){
            //画像を回転
            final CropImageView iv_cropSource = findViewById(R.id.iv_cropSource);

            //回転時のアニメーション時間をなしにする
            //※アニメーション時間をなしにしないと、トリミング結果の反映の方が先に処理されるため
            int ROTATE_ANIMATION = 0;
            iv_cropSource.setAnimationDuration(ROTATE_ANIMATION);
            iv_cropSource.rotateImage( ROTATE_90D );

            //トリミング範囲を反映
            setCroppedPicture();

            //トリミング範囲が反映されるタイミングが変わることをダイアログで表示
            AlertDialog dialog = new AlertDialog.Builder(iv_cropSource.getContext())
                    .setTitle( getString(R.string.alert_trimming_ui_title) )
                    .setMessage( getString(R.string.alert_trimming_ui_message) )
                    .setPositiveButton( getString(android.R.string.ok), null)
                    .show();

            //メッセージ文は、Styleのフォントが適用されないため個別に設定
            ((TextView)dialog.findViewById(android.R.id.message)).setTypeface( Typeface.SERIF );
        }

    }

}