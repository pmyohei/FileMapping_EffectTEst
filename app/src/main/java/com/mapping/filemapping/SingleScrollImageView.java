package com.mapping.filemapping;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager2.widget.ViewPager2;

/*
 * 写真単体表示用ビュー
 * 　　ズームや参照箇所のタッチ移動に対応
 */
public class SingleScrollImageView extends androidx.appcompat.widget.AppCompatImageView {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private boolean mPinchImmediatelyAfter = true;
    private ViewGroup mParentView;
    private float mInitImageHeight;
    private float mInitImageWidth;


    private boolean mIsMinScale;

    static int AAA = 0;
    static boolean TEST = true;

    private float mPreTouchPosX;


    public SingleScrollImageView(Context context) {
        this(context, null);
    }
    public SingleScrollImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SingleScrollImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {

        //画像の初期高さ
        mInitImageHeight = -1;

        //ピンチ率最小
        mIsMinScale = true;

        //
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

/*    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure( widthMeasureSpec, heightMeasureSpec );

        ImageView iv_singleScrollPicture = findViewById(R.id.iv_singleScrollPicture);
        if( iv_singleScrollPicture.getLayoutParams() == null ){
            return;
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.height = mParentView.getHeight();
        layoutParams.width = mParentView.getWidth();

        iv_singleScrollPicture.setLayoutParams(layoutParams);
    }*/


    /*
     * スクロール制御
     *   true ：スクロールを停止
     *   false：スクロールを再開
     */
    private void disableScroll(boolean disable) {

        Log.i("ピンチ操作", "スクロール操作(true=無効化)=" + disable);

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }

        NestedScrollView nsv_vertical = mParentView.findViewById(R.id.nsv_vertical);
        nsv_vertical.mIsIntercept = disable;
        nsv_vertical.requestDisallowInterceptTouchEvent(disable);

        ((HorizontalScrollView)mParentView.findViewById(R.id.hsv_horizontal)).requestDisallowInterceptTouchEvent(disable);
    }

    /*
     * 写真が画面端まで達しているかどうか
     *   para1：タッチ移動量(X軸)
     */
    private boolean isEdgeOnScreen( float distanceX ) {

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }

        HorizontalScrollView hsv_horizontal = mParentView.findViewById(R.id.hsv_horizontal);
        boolean canRightScroll = hsv_horizontal.canScrollHorizontally(1);
        boolean canLeftScroll  = hsv_horizontal.canScrollHorizontally(-1);

        Log.i("スクロール可能かチェック", "1 ：" + canRightScroll);
        Log.i("スクロール可能かチェック", "-1：" + canLeftScroll);

        if( distanceX > 0 ){
            //タッチが右に移動
            if( !canRightScroll ){
                //右端まできていれば（右にスクロールできなければ）
                return true;
            }

        } else if( distanceX < 0 ){
            //タッチが左に移動
            if( !canRightScroll ){
                //左端まできていれば（左にスクロールできなければ）
                return true;
            }
        }

/*        //ピンチ操作直後はチェックしない
        if( mPinchImmediatelyAfter ){

            Log.i("スクロール可能かチェック 直後", "1 ：" + canRightScroll);
            Log.i("スクロール可能かチェック 直後", "-1：" + canLeftScroll);

            if( canLeftScroll ){
                //左端が端ではなくなる時まで、ピンチ操作直後扱いとする
                Log.i("ページ操作", "左端が端ではなくなった");
                mPinchImmediatelyAfter = false;
            }

            //端ではない判定とする
            return false;
        }

        if( !canRightScroll || !canLeftScroll ){

            Log.i("ページ操作", "どちらかが端まで到達");
            Log.i("ページ操作", "canRightScroll：" + canRightScroll);
            Log.i("ページ操作", "canLeftScroll ：" + canLeftScroll);

            //左右どちらかへスクロールできない（端まで到達）している場合
            return true;
        }*/

        return false;
    }

    /*
     * 写真が画面端まで達しているかどうか
     */
    private boolean isEdgeOnScreen() {

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }

        HorizontalScrollView hsv_horizontal = mParentView.findViewById(R.id.hsv_horizontal);
        boolean canRightScroll = hsv_horizontal.canScrollHorizontally(1);
        boolean canLeftScroll  = hsv_horizontal.canScrollHorizontally(-1);

        Log.i("スクロール可能かチェック", "1 ：" + canRightScroll);
        Log.i("スクロール可能かチェック", "-1：" + canLeftScroll);

        //ピンチ操作直後はチェックしない
        if( mPinchImmediatelyAfter ){

            Log.i("スクロール可能かチェック 直後", "1 ：" + canRightScroll);
            Log.i("スクロール可能かチェック 直後", "-1：" + canLeftScroll);

            if( canLeftScroll ){
                //左端が端ではなくなる時まで、ピンチ操作直後扱いとする
                Log.i("ページ操作", "左端が端ではなくなった");
                mPinchImmediatelyAfter = false;
            }

            //端ではない判定とする
            return false;
        }

        if( !canRightScroll || !canLeftScroll ){

            Log.i("ページ操作", "どちらかが端まで到達");
            Log.i("ページ操作", "canRightScroll：" + canRightScroll);
            Log.i("ページ操作", "canLeftScroll ：" + canLeftScroll);

            //左右どちらかへスクロールできない（端まで到達）している場合
            return true;
        }

        return false;
    }

    /*
     * スクロール位置移動
     */
    private void scrollByPinch( float height, float width, float scaleFactor ) {

        ImageView iv_singleScrollPicture = findViewById(R.id.iv_singleScrollPicture);
        int org_height = iv_singleScrollPicture.getHeight();
        int org_width  = iv_singleScrollPicture.getWidth();

        //必要な移動量：ｘ
        int scrollx = (int)((width * scaleFactor) - org_height);
        int scrolly = (int)((height * scaleFactor) - org_width);

        //
        HorizontalScrollView hsv_horizontal = mParentView.findViewById(R.id.hsv_horizontal);
        hsv_horizontal.scrollBy( scrollx / 2, 0 );
        //hsv_horizontal.scrollBy( 10, 0 );

        //
        NestedScrollView nsv_vertical = mParentView.findViewById(R.id.nsv_vertical);
        nsv_vertical.scrollBy( 0, scrolly * -1 / 2 ) ;
        //nsv_vertical.scrollBy( 0, -10 ) ;

        //iv_singleScrollPicture.scrollBy( scrollx / 2, scrolly * -1 / 2 ) ;

        Log.i("ピンチ操作差分", "移動 x=" + (scrollx / 2) + " y=" + (scrolly * -1 / 2) );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //親から子（自分）へのタッチ処理止めを無効化
        //getParent().requestDisallowInterceptTouchEvent(true);

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }
        ViewPager2 vp2_singlePicture = mParentView.findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.requestDisallowInterceptTouchEvent(true);

        //タッチ開始
        if( event.getAction() == MotionEvent.ACTION_DOWN ){

            //前回のタッチ位置を保持
            mPreTouchPosX = event.getX();

            //拡大されているなら、ページ送り無効化
            if( !mIsMinScale ){
                Log.i("ページ操作", "拡大中のため、ページ送り無効にする");
                enablePageScroll(false);
            }
        }

        Log.i("コール順確認", "写真");

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }

        //ピンチ操作
        scaleGestureDetector.onTouchEvent(event);

        //ピンチ操作完了後は、少しだけずらす
        HorizontalScrollView hsv_horizontal = mParentView.findViewById(R.id.hsv_horizontal);

        if( true ){
            return true;
        }

        //タッチ移動中
        if( event.getAction() == MotionEvent.ACTION_MOVE ){

            //移動量を計算
            float distanceX = event.getX() - mPreTouchPosX;

            Log.i("ページ操作", "タッチ移動量=" + distanceX);
            Log.i("ページ操作", "event.getX()=" + event.getX());
            Log.i("ページ操作", "mPreTouchPosX=" + mPreTouchPosX);

            //ページ送り有効化チェック
            if( isEdgeOnScreen( distanceX ) ){
                Log.i("ページ操作", "端まで到達");
                enablePageScroll( true );
            }

            //前回のタッチ位置を保持
            mPreTouchPosX = event.getX();

        }


        Log.i("タッチ中確認", "処理されてる？");

        return true;
    }



    /*
     * 写真のピンチ操作リスナー
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        private final int HEIGHT_BIT = 0x01;
        private final int WIDTH_BIT  = 0x02;
        private final int ALL_BIT  = 0x03;


        private int scale_init;

        private LinearLayout.LayoutParams mLayoutParams = null;

        //ピンチ操作開始時点のマップスケールを保持
        private float mHeight;
        private float mWidth;

        float mRatio;


/*        public ScaleListener(){
            INIT_SCALE = getMatrixValue(Matrix.MSCALE_Y);
            MAX_SCALE  = getMatrixValue(Matrix.MSCALE_Y) * 2.5f;
        }*/

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            //スクロールを無効化
            disableScroll(true);

            if( mLayoutParams == null ){
                mLayoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
            }

            ImageView iv_singleScrollPicture = findViewById(R.id.iv_singleScrollPicture);
            mHeight = iv_singleScrollPicture.getHeight();
            mWidth  = iv_singleScrollPicture.getWidth();

            //初期サイズ未保持なら、保持する
            if( mInitImageHeight < 0 ){
                mInitImageHeight = mHeight;
                mInitImageWidth  = mWidth;
            }

            scale_init = 0x00;

            mIsMinScale = false;

            //
            enablePageScroll( false );


            mRatio = iv_singleScrollPicture.getScaleX();

            Log.i("ピンチ操作差分", "開始：左マージン=" + iv_singleScrollPicture.getLeft());
            Log.i("ピンチ操作差分", "開始：高さ=" + iv_singleScrollPicture.getHeight());

            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            //ピンチ倍率
            float scaleFactor = detector.getScaleFactor();

            //ピンチ比率をそのまま適用すると大きいため、小数点部分を狭める
            final float REDUCTION_RATE_SCALEUP   = 0.2f;    //拡大時の比率縮小割合
            final float REDUCTION_RATE_SCALEDOWN = 0.4f;    //縮小時の比率縮小割合

            //小数点部分
            float point;
            if( scaleFactor > 1.0f ){
                //拡大の場合
                point = scaleFactor - 1;
                scaleFactor = 1 + (point * REDUCTION_RATE_SCALEUP );
            } else {
                //縮小の場合
                point = 1 - scaleFactor;
                scaleFactor = 1 - (point * REDUCTION_RATE_SCALEDOWN );
            }

            //設定サイズ
            int setHeight = (int)(mHeight * scaleFactor);
            int setWidth  = (int)(mWidth * scaleFactor);

            //大きいときのみ更新。初期サイズを下回る場合は、初期サイズを設定（初期サイズより小さいサイズにはしない）
            mLayoutParams.height = ( (mInitImageHeight < setHeight) ? setHeight: (int)mInitImageHeight );
            mLayoutParams.width  = ( (mInitImageWidth < setWidth)   ? setWidth : (int)mInitImageWidth );

            //最小
            mIsMinScale = false;
            if( (mLayoutParams.height <= mInitImageHeight) && (mLayoutParams.width <= mInitImageWidth) ){
                
                Log.i("ページ操作", "mInitImageHeight=" + mInitImageHeight);
                Log.i("ページ操作", "mLayoutParams.height=" + mLayoutParams.height);
                Log.i("ページ操作", "mInitImageWidth=" + mInitImageWidth);
                Log.i("ページ操作", "mLayoutParams.width=" + mLayoutParams.width);
                
                mIsMinScale = true;
            }


            ImageView iv_singleScrollPicture = findViewById(R.id.iv_singleScrollPicture);
            iv_singleScrollPicture.setLayoutParams( mLayoutParams );

            //scrollByPinch(mHeight, mWidth, scaleFactor);

            //iv_singleScrollPicture.setScaleX( mRatio * scaleFactor );
            //iv_singleScrollPicture.setScaleY( mRatio * scaleFactor );

            Log.i("ピンチ操作", "scaleFactor=" + scaleFactor);
            Log.i("ピンチ操作A", "mInitImageHeight=" + mInitImageHeight);
            Log.i("ピンチ操作A", "setHeight=" + setHeight);
            Log.i("ピンチ操作A", "mInitImageWidth=" + mInitImageWidth);
            Log.i("ピンチ操作A", "setWidth=" + setWidth);
            Log.i("ピンチ操作", "mHeight=" + mHeight + " → " + mLayoutParams.height);

            return super.onScale(detector);
        }


        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            ImageView iv_singleScrollPicture = findViewById(R.id.iv_singleScrollPicture);
            Log.i("ピンチ操作差分", "終了：左マージン=" + iv_singleScrollPicture.getLeft());
            Log.i("ピンチ操作差分", "終了：高さ=" + iv_singleScrollPicture.getHeight());

            //スクロールを有効化
            disableScroll(false);

            if( mIsMinScale ){
                Log.i("ページ操作", "ピンチ最小状態");
                enablePageScroll( true );
            }

            //ピンチ操作完了後は、少しだけずらす
            //HorizontalScrollView hsv_horizontal = mParentView.findViewById(R.id.hsv_horizontal);
            //hsv_horizontal.scrollBy( 10, 0 );

            //ピンチ操作直後
            mPinchImmediatelyAfter = true;
        }
    };

    /*
     * ViewPager2 のページスクロール制御
     *   true ：スクロール可能
     *   false：スクロール停止
     */
    private void pageRequestDisallowInterceptTouchEvent(boolean isScroll) {

        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }

        ViewPager2 vp2_singlePicture = mParentView.findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.requestDisallowInterceptTouchEvent(isScroll);
    }

    /*
     * ViewPager2 のページスクロール制御
     *   true ：スクロール可能
     *   false：スクロール停止
     */
    private void enablePageScroll(boolean isScroll) {
        //ページスクロール制御
        if( mParentView == null ){
            mParentView = (ViewGroup) getRootView();
        }
        ViewPager2 vp2_singlePicture = mParentView.findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.setUserInputEnabled(isScroll);

        Log.i("ページ操作", "trueでページ送りOK=" + isScroll);
    }

}
