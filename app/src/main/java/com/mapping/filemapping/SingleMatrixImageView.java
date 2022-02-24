package com.mapping.filemapping;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Scroller;

import androidx.viewpager2.widget.ViewPager2;

/*
 * 写真単体表示用ビュー
 * 　　ズームや参照箇所のタッチ移動に対応
 */
public class SingleMatrixImageView extends androidx.appcompat.widget.AppCompatImageView {

    //ページ送り指定定数
    private final int PAGE_FEED_PRE  = 0x01;
    private final int PAGE_FEED_NEXT = 0x02;
    private final int PAGE_FEED_BOTH = PAGE_FEED_PRE | PAGE_FEED_NEXT;

    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    //フリング用Scroller
    private Scroller mFlingScroller;

    //マトリックス上の画像初期位置
    private final float PRE_INIT_VALUE = -1;
    private float mImageInitPosX;
    private float mImageInitPosY;

    //画像のScaleTypeをマトリクスにしたかどうか
    private boolean mIsSetScaleTypeToMatrix = false;

    //ピンチ操作中
    private boolean mIsPinching = false;
    //画像の初期比率
    private float mInitMatrixScale = PRE_INIT_VALUE;

    public SingleMatrixImageView(Context context) {
        super(context);
    }

    public SingleMatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SingleMatrixImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {

        //初期位置
        mImageInitPosX = PRE_INIT_VALUE;
        mImageInitPosY = PRE_INIT_VALUE;

        //フリング用スクロール生成
        mFlingScroller = new Scroller(context, new DecelerateInterpolator());

        //ジェスチャー操作
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, simpleOnGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //画像のScaleTypeをマトリクスに設定
        if (!mIsSetScaleTypeToMatrix) {
            //ビュー生成時点では、ScaleTypeがMATRIXではないため、ここでマトリクスに設定
            setScaleType(ScaleType.MATRIX);
            //※このタイミングで必要
            matrix = getImageMatrix();

            mInitMatrixScale = getMatrixValue(Matrix.MSCALE_Y);
            //mMaxImageScale = getMatrixValue(Matrix.MSCALE_Y) * MAX_RATIO;

            //画像初期位置
            mImageInitPosX = getMatrixValue(Matrix.MTRANS_X);
            mImageInitPosY = getMatrixValue(Matrix.MTRANS_Y);

            mIsSetScaleTypeToMatrix = true;
        }

        //マトリクスを設定
        setImageMatrix(matrix);

        //現在のスクロールを停止
        mFlingScroller.forceFinished(true);

        //ピンチ操作
        scaleGestureDetector.onTouchEvent(event);
        //タッチ移動操作
        gestureDetector.onTouchEvent(event);

        //ページ送りアイコンの表示判定
        checkShowPageFeedIcon();

        return true;
    }

    /*
     * ページ送りアイコンを表示する必要があるか判定
     */
    private void checkShowPageFeedIcon() {

        Log.i("ページ送り", "mInitMatrixScale=" + mInitMatrixScale);
        Log.i("ページ送り", "getMatrixValue(Matrix.MSCALE_Y)=" + getMatrixValue(Matrix.MSCALE_Y));

        //if( mInitMatrixScale == getMatrixValue(Matrix.MSCALE_Y) ){
        if( mInitMatrixScale >= getMatrixValue(Matrix.MSCALE_Y) ){
            //拡大していないなら、アイコン非表示にする
            hidePageFeedIcon( PAGE_FEED_BOTH );

            Log.i("ページ送り", "拡大していない状態 mInitMatrixScale=" + mInitMatrixScale);
            Log.i("ページ送り", "拡大していない状態 getMatrixValue(Matrix.MSCALE_Y)=" + getMatrixValue(Matrix.MSCALE_Y));
            return;
        }

        //自身のページ位置
        ViewPager2 vp2_singlePicture = getRootView().findViewById(R.id.vp2_singlePicture);
        int currentPage = vp2_singlePicture.getCurrentItem();

        Log.i("ページ送り", "rightSideX=" + (getMatrixValue(Matrix.MTRANS_X) + getImageWidth()));
        Log.i("ページ送り", "getWidth()=" + getWidth());

        //右端判定
        float rightSideX = getMatrixValue(Matrix.MTRANS_X) + getImageWidth();
        if( rightSideX == getWidth() ){
            //画像が画面右端にある場合

            Log.i("ページ送り", "currentPage=" + currentPage);
            Log.i("ページ送り", "pageNum=" + vp2_singlePicture.getAdapter().getItemCount());

            //自身が最後のページであれば何もしない
            int pageNum = vp2_singlePicture.getAdapter().getItemCount();
            if( currentPage == (pageNum - 1) ){
                return;
            }

            //右ページ送りするアイコンを表示
            getRootView().findViewById(R.id.iv_next).setVisibility(VISIBLE);

        } else {
            hidePageFeedIcon( PAGE_FEED_NEXT );
        }

        Log.i("ページ送り", "leftSideX=" + getMatrixValue(Matrix.MTRANS_X));

        //左端判定
        float leftSideX = getMatrixValue(Matrix.MTRANS_X);
        if( leftSideX == 0 ){

            //自身が先頭のページであれば何もしない
            if( currentPage == 0 ){
                return;
            }

            //左ページ送りするアイコンを表示
            getRootView().findViewById(R.id.iv_pre).setVisibility(VISIBLE);

        } else {
            hidePageFeedIcon( PAGE_FEED_PRE );
        }
    }

    /*
     * ページ送り用アイコンを非表示にする
     */
    private void hidePageFeedIcon( int target ) {

        //右送り
        if( (target & PAGE_FEED_NEXT) == PAGE_FEED_NEXT ){
            //表示されていれば非表示
            ImageView iv_next = getRootView().findViewById(R.id.iv_next);
            if( iv_next.getVisibility() == VISIBLE ){
                iv_next.setVisibility(GONE);
            }
        }

        //左送り
        if( (target & PAGE_FEED_PRE) == PAGE_FEED_PRE ) {
            //表示されていれば非表示
            ImageView iv_pre = getRootView().findViewById(R.id.iv_pre);
            if( iv_pre.getVisibility() == VISIBLE ){
                iv_pre.setVisibility(GONE);
            }
        }
    }


    /*
     * ViewPager2 のページスクロール制御
     *   true ：スクロール可能
     *   false：スクロール停止
     */
    private void setPageScroll(boolean isScroll) {

        //test用--------
        //if( isScroll ){
        //    isScroll = false;
        //}
        //---------------

        //ページスクロール制御
        View root = getRootView();
        ViewPager2 vp2_singlePicture = root.findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.setUserInputEnabled(isScroll);
    }

    /*
     * マトリクスの指定値を取得
     */
    private float getMatrixValue(int index) {
        if (matrix == null) {
            matrix = getImageMatrix();
        }

        float[] values = new float[9];
        matrix.getValues(values);

        return values[index];
    }

    /*
     * 写真のピンチ操作リスナー：拡大／縮小
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //許容最大比率
        final float MAX_RATIO = 8f;

        //画像の初期比率
        //float INIT_SCALE = PRSET_SCALE;
        //最大拡大比率（画像）
        float mMaxImageScale = PRE_INIT_VALUE;

        //焦点
        float focusX;
        float focusY;
        //前回、画像に設定した比率
        float mPreScaleFactor;
        //画像の拡大率
        float mImageScale;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            //ピンチ焦点
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();

            //ピンチ操作ON
            mIsPinching = true;

            Log.i("不具合", "onScaleBegin()");

            //初期スケール未保持かどうか
            //if (mInitMatrixScale == PRE_INIT_VALUE) {
            if (mMaxImageScale == PRE_INIT_VALUE) {
                //mInitMatrixScale = getMatrixValue(Matrix.MSCALE_Y);
                mMaxImageScale = getMatrixValue(Matrix.MSCALE_Y) * MAX_RATIO;

                //画像初期位置
                //mImageInitPosX = getMatrixValue(Matrix.MTRANS_X);
                //mImageInitPosY = getMatrixValue(Matrix.MTRANS_Y);
            }

            //ピンチ開始比率初期化
            mPreScaleFactor = 1.0f;

            //ページスクロールを停止
            setPageScroll(false);

            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            Log.i("不具合", "onScale()");

            float factor = detector.getScaleFactor();
            float currentImageScale = getMatrixValue(Matrix.MSCALE_Y);

            //小数点部分を少ない値にする
            //※detectorから取得できるピンチ比率をそのまま設定するのは、画像の拡大縮小には大きすぎるため
            float scaleFactor;
            float point;
            if (factor > 1.0f) {
                //拡大の場合
                point = factor - 1;
                scaleFactor = 1 + (point * 0.2f);
            } else {
                //縮小の場合
                point = 1 - factor;
                scaleFactor = 1 - (point * 0.3f);
            }
            
            //前回の比率を取り除く
            //※これをしないと、加速度的にズームされる
            scaleFactor /= mPreScaleFactor;

            //前回設定値として保持
            mPreScaleFactor = scaleFactor;

            //マトリクスに対する初期比率からの累計比率
            mImageScale = scaleFactor * currentImageScale;

            //想定比率以上は拡大させない
            if (mImageScale > mMaxImageScale) {
                return false;
            }

            //想定比率以下は縮小させない
            if (mImageScale < mInitMatrixScale) {
                //元の比率に収める
                mImageScale = mInitMatrixScale;
                scaleFactor = mInitMatrixScale / currentImageScale;
            }

            //マトリクスを更新して、再描画
            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            invalidate();

            return super.onScale(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            //ピンチ操作OFF
            mIsPinching = false;

            //スケールが初期状態になっていれば、ページスクロールを可能にする
            if (mImageScale == mInitMatrixScale) {
                Log.i("スクロール制御", "ピンチ操作終了→可能に設定");
                //画像の位置を元の状態にする
                setImageFitScreen();
                //ページ制御
                setPageScroll(true);
            }

            //ページ送りアイコンの表示判定
            //checkShowPageFeedIcon();
        }

        /*
         * 画像を初期位置に収める
         */
        private void setImageFitScreen() {
            //現在位置
            final float currentX = getMatrixValue(Matrix.MTRANS_X);
            final float currentY = getMatrixValue(Matrix.MTRANS_Y);

            //初期位置との差分
            float offsetX = mImageInitPosX - currentX;
            float offsetY = mImageInitPosY - currentY;

            //Log.i("位置リセット", "offsetX=" + offsetX);
            //Log.i("位置リセット", "offsetY=" + offsetY);

            //Matrixを操作
            matrix.postTranslate(offsetX, offsetY);
            invalidate();
        }
    };



    /*
     * 写真のスクロールリスナー
     */
    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {

            //指の動きに合わせて画像の描画範囲を変更
            translateImageMatrix(distanceX, distanceY);

            return super.onScroll(event1, event2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.i("画像フリング", "開始");

            //加速度の減算調整
            //※そのままの指の加速度だと、値が大きすぎるため
            final float ACCELERATION_SUBTRACTION = 2.5f;
            //スクローラーの移動可能範囲（とりあえず大きな値を設定しておく）
            final int RANGE = 2000;

            // アニメーションを開始
            mFlingScroller.fling(
                    0,                                        //scroll の開始位置 (X)
                    0,                                        //scroll の開始位置 (Y)
                    (int) (velocityX / ACCELERATION_SUBTRACTION),   //初速
                    (int) (velocityY / ACCELERATION_SUBTRACTION),   //初速
                    -RANGE,
                    RANGE,
                    -RANGE,
                    RANGE
            );

            //フリング操作時、加速度をスクロールに反映
            final int MOVE_DURATION = 500;
            ValueAnimator scrollAnimator = ValueAnimator.ofFloat(0, 1).setDuration(MOVE_DURATION);
            scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                //前回位置
                int preX = 0;
                int preY = 0;

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {

                    if (!mFlingScroller.isFinished()) {
                        mFlingScroller.computeScrollOffset();

                        //前回からの移動量を計算
                        float moveX = mFlingScroller.getCurrX() - preX;
                        float moveY = mFlingScroller.getCurrY() - preY;

                        //画像のメトリクスを移動させる
                        translateImageMatrix(-moveX, -moveY);

                        //Log.i("画像フリング", "getCurrX=" + mFlingScroller.getCurrX());
                        //Log.i("画像フリング", "getCurrY=" + mFlingScroller.getCurrY());

                        //前回位置として保持
                        preX = mFlingScroller.getCurrX();
                        preY = mFlingScroller.getCurrY();

                    } else {
                        scrollAnimator.cancel();
                    }
                }
            });
            scrollAnimator.start();

            return false;
        }

        /*
         * 画像のメトリクス移動
         */
        private void translateImageMatrix(float distanceX, float distanceY) {
            //viewの縦横長
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            //画像の縦横長
            float imageWidth = getImageWidth();
            float imageHeight = getImageHeight();

            //ピンチ操作中は何もしない
            if (mIsPinching) {
                return;
            }

            //画像が収まっている場合は、動かす必要なし
            if ((viewWidth >= imageWidth) && (viewHeight >= imageHeight)) {
                return;
            }

            //指の動きに追随してほしいため符号を反転
            float x = -distanceX;       //x；左から右にスワイプ：＋ 右から左にスワイプ：ー
            float y = -distanceY;       //y：上から下にスワイプ：＋ 下から上にスワイプ：ー

            //画像移動量
            x = getMatrixTransX(x, imageWidth, viewWidth);
            y = getMatrixTransY(y, imageHeight, viewHeight);

            //Matrixを操作
            matrix.postTranslate(x, y);
            invalidate();

            //ページ送りアイコンの表示判定
            //checkShowPageFeedIcon();
        }

        /*
         * 画像の移動量の取得：x軸
         */
        private float getMatrixTransX(float x, float imageWidth, float viewWidth) {

            //画像の左辺、右辺のx座標
            final float leftSideX = getMatrixValue(Matrix.MTRANS_X);
            final float rightSideX = leftSideX + imageWidth;

            //横幅が画面内に収まっている場合、マトリクスを初期化
            if (viewWidth > imageWidth) {
                Log.i("スクロール", "x初期化");
                return 0f;
            }

            //画像が画面よりも右側にある状態で、指の方向が右側
            if (leftSideX > 0 && x > 0) {
                //画像の位置を端に設定

                Log.i("端の挙動変問題", "x 画像の位置を端に設定");
                Log.i("スクロール", "xを反転 x=" + x + " leftSideX=" + leftSideX);

                //画面端に収める
                return (leftSideX * -1);
            }

            //画像がビューよりも右側にある状態で、指の方向が左側
            if (rightSideX < viewWidth && x < 0) {
                //ビュー端（画面端）と画像右端との差

                Log.i("端の挙動変問題", "x ビュー端（画面端）と画像右端との差");
                Log.i("スクロール", "xを計算 x=" + x + " viewWidth=" + viewWidth + " rightSideX=" + rightSideX);

                //画面端に収める
                return (viewWidth - rightSideX);
            }

            //画面右端に画像右端がきている場合
            if ((rightSideX == viewWidth)) {
                if (x > 0) {
                    Log.i("端の挙動変問題", "x 移動なし：右端を参照中に左へ");

                    return x;

                } else {
                    Log.i("端の挙動変問題", "x 移動なし：右端を参照中にさらに左へ");

                    //移動なし
                    //return -1f;
                    return 0;
                }
            }

            //画面左端に画像左端がきている場合
            if ((leftSideX == 0)) {

                if (x > 0) {
                    //画像が一番左にある状態で、左に移動させようとしたとき

                    Log.i("端の挙動変問題", "x 移動なし：左にいる想定でさらに左を見る行為");

                    //移動なし
                    //return 1f;
                    return 0;
                } else {
                    Log.i("端の挙動変問題", "x 移動なし：左端参照中に右を参照");

                    return x;
                }
            }

            Log.i("端の挙動変問題", "x その他");

            //いずれにも該当しない
            return x;
        }

        /*
         * 画像の移動量の取得：y軸
         */
        private float getMatrixTransY(float y, float imageHeight, float viewHeight ){

            //画像の上辺、底辺のy座標
            //※レイアウトの原点は左上のため、底辺の方が座標上の値は大きくなる
            final float topY = getMatrixValue(Matrix.MTRANS_Y);
            final float bottomY = topY + imageHeight;

            //横幅が画面内に収まっている場合、マトリクスを初期化
            if (viewHeight > imageHeight) {
                return  0;
            }

            //上がった分を埋める
            if (topY > 0 && y > 0) {
                Log.i("端の挙動変問題", "y 上がった分を埋める");
                return (topY * -1f);
            }

            //下がった分を埋める
            if ((bottomY < viewHeight) && (y < 0)) {

                Log.i("端の挙動変問題", "y 下がった分を埋める");

                return (viewHeight - bottomY);
            }

            //画像の底辺が画面底辺と一致している
            if (bottomY == viewHeight) {

                if (y > 0) {
                    Log.i("端の挙動変問題", "y 移動なし：一番上");

                    return y;

                } else {
                    Log.i("端の挙動変問題", "y 移動なし：一番上からさらに上を見る行為");

                    //さらに画像下を見ようとする場合は、移動なしにする
                    //return -1f;
                    return 0;
                }
            }

            //画像の上辺が画面上辺と一致している
            if (topY == 0) {

                //一番下まできていれば、移動なし
                if (y > 0) {
                    Log.i("端の挙動変問題", "y 移動なし：一番下");

                    //移動なし
                    //return  1;
                    return 0;
                } else {
                    Log.i("端の挙動変問題", "y 移動なし：一番下からさらに下を見る行為");

                    return y;
                }
            }

            //いずれにも該当しない
            return y;
        }
    };

    /*
     * 画像の横幅（拡大率を考慮）
     */
    private float getImageWidth(){
        return (getDrawable().getIntrinsicWidth()) * getMatrixValue(Matrix.MSCALE_X);
    }

    /*
     * 画像の高さ（拡大率を考慮）
     */
    private float getImageHeight(){
        return (getDrawable().getIntrinsicHeight()) * getMatrixValue(Matrix.MSCALE_Y);
    }

}
