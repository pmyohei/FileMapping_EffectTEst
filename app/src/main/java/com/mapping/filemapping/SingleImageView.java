package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.viewpager2.widget.ViewPager2;

/*
 * 写真単体表示用ビュー
 * 　　ズームや参照箇所のタッチ移動に対応
 */
public class SingleImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Matrix matrix = new Matrix();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private final float SCALE_MAX = 3.0f;
    private final float SCALE_MIN = 1.0f;
    private final float PINCH_SENSITIVITY = 5.0f;

    boolean test = false;

    public SingleImageView(Context context) {
        super(context);
    }

    public SingleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SingleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context) {
/*
        setImageResource(R.drawable.bmp_sample_cafe);
        setScaleType(ScaleType.MATRIX);

        float previousScale = getMatrixValue(Matrix.MSCALE_Y);
        Log.i("マトリクス", "previousScale=" + previousScale );
*/

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context,simpleOnGestureListener);
    }

    public void setScaleTypeA() {
        setScaleType(ScaleType.FIT_CENTER);
        //matrix = getImageMatrix();
        //setImageMatrix(matrix);
        Log.i("マトリクス", "FIT_CENTERの設定" );
    }

    public void setScaleType() {
        setScaleType(ScaleType.MATRIX);
        matrix = getImageMatrix();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if( !test ){
            setScaleType(ScaleType.MATRIX);
            //if (matrix == null) {
                matrix = getImageMatrix();

                float x = getMatrixValue(Matrix.MSCALE_X);
                float y = getMatrixValue(Matrix.MSCALE_Y);
                Log.i("マトリクス", "x=" + x + " y=" + y );
            //}
            test = true;
        }

        setImageMatrix(matrix);
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    //private ScaleGestureDetector.SimpleOnScaleGestureListener simpleOnScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {

        final float PRSET_SCALE = -1f;
        final float MAX_RATIO = 4f;

        float focusX;
        float focusY;
        float INIT_SCALE = PRSET_SCALE;
        float MAX_SCALE;

        //画像の拡大率
        float mScale;

        float mStartScale;

/*        public ScaleListener(){
            INIT_SCALE = getMatrixValue(Matrix.MSCALE_Y);
            MAX_SCALE  = getMatrixValue(Matrix.MSCALE_Y) * 2.5f;
        }*/

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor;
            //スケール操作前の比率（前回比率変更後の比率）
            float previousScale = getMatrixValue(Matrix.MSCALE_Y);

            if (detector.getScaleFactor() >= 1.0f) {
                scaleFactor = 1 + ((detector.getScaleFactor() - 1) / (previousScale * PINCH_SENSITIVITY));
            } else {
                scaleFactor = 1 - ((1 - detector.getScaleFactor()) / (previousScale * PINCH_SENSITIVITY));
            }

            //初期比率からの累計比率
            mScale = scaleFactor * previousScale;

            Log.i("マトリクス", "scale=" + mScale + " scaleFactor=" + scaleFactor + " previousScale=" + previousScale );

            //scaleFactor = mStartScale * detector.getScaleFactor();
            //Log.i("マトリクス", "mStartScale=" + mStartScale + " scaleFactor=" + scaleFactor);

            if ( mScale > MAX_SCALE ) {
                Log.i("マトリクス", "スケール範囲超過 scale=" + mScale + " MAX_SCALE=" + MAX_SCALE);
                return false;
            }

            if ( mScale < INIT_SCALE ) {
                Log.i("マトリクス", "スケール範囲小さい scale=" + mScale + " MAX_SCALE=" + MAX_SCALE);

                //元の比率になるようにする
                mScale = INIT_SCALE;
                scaleFactor = INIT_SCALE / previousScale;
            }

            //マトリクスを更新して、再描画
            matrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            invalidate();

            return super.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();

            //初期スケール未保持かどうか
            if( INIT_SCALE == PRSET_SCALE ){
                INIT_SCALE = getMatrixValue(Matrix.MSCALE_Y);
                MAX_SCALE  = getMatrixValue(Matrix.MSCALE_Y) * MAX_RATIO;
            }

            mStartScale = getMatrixValue(Matrix.MSCALE_Y);

            //ページスクロールを停止
            setPageScroll(false);

            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);

            //スケールが初期状態になっていれば、ページスクロールを可能にする
            if( mScale == INIT_SCALE ){
                Log.i("スクロール制御", "ピンチ操作終了→可能に設定");

                setPageScroll(true);
            }
        }
    };

    /*
     * ViewPager2 のページスクロール制御
     *   true ：スクロール可能
     *   false：スクロール停止
     */
    private void setPageScroll(boolean isScroll) {

        //ページスクロール制御
        View root = getRootView();
        ViewPager2 vp2_singlePicture = root.findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.setUserInputEnabled(isScroll);
    }

    private float getMatrixValue(int index) {
        if (matrix == null) {
            matrix = getImageMatrix();
        }

        float[] values = new float[9];
        matrix.getValues(values);

        float value = values[index];
        return value;
    }

    private final GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            //viewの縦横長
            float viewWidth = getWidth();
            float viewHeight = getHeight();
            //画像の縦横長
            float imageWidth = getImageWidth();
            float imageHeight = getImageHeight();
            //画像の左辺、右辺のx座標
            float leftSideX = getMatrixValue(Matrix.MTRANS_X);
            float rightSideX = leftSideX + imageWidth;
            //画像の上辺、底辺のy座標
            float topY = getMatrixValue(Matrix.MTRANS_Y);
            float bottomY = topY + imageHeight;

            //画像が収まっている場合は、動かす必要なし
            if( viewWidth >= imageWidth && viewHeight >= imageHeight){
                return false;
            }

            //指の動きに追随してほしいため符号を反転
            float x = -distanceX;
            float y = -distanceY;

            //端ではない
            boolean isEdge = false;

            int direction = 0;

            if(viewWidth > imageWidth){
                //横幅が画面内に収まっている場合、マトリクスを初期化
                x = 0;
                Log.i("スクロール", "x初期化");

            } else {
                if( leftSideX > 0 && x > 0 ){
                    //画像がビューよりも右側にある状態で、指の方向が右側
                    //画像の位置を端に設定
                    x = -leftSideX;

                    //端
                    isEdge = true;

                    direction = 1;

                    Log.i("スクロール", "xを反転 x=" + x + " leftSideX=" + leftSideX);

                } else if( rightSideX < viewWidth && x < 0 ) {
                    //画像がビューよりも右側にある状態で、指の方向が左側
                    //ビュー端（画面端）と画像右端との差
                    x = viewWidth - rightSideX;

                    //端
                    isEdge = true;

                    direction = 2;

                    Log.i("スクロール", "xを計算 x=" + x + " viewWidth=" + viewWidth + " rightSideX=" + rightSideX);

                } else if( (rightSideX == viewWidth) && (x > 0) ){
                    //画像が一番右にある状態で、右に移動させようとしたとき
                    //画像の移動はなし
                    x = -1f;
                    //端
                    isEdge = true;

                    direction = 2;

                } else if( (leftSideX == 0) && (x < 0) ) {
                    //画像が一番左にある状態で、左に移動させようとしたとき
                    //画像の移動はなし
                    x = 1;
                    //端
                    isEdge = true;

                    direction = 1;
                }
/*                } else {
                    Log.i("スクロール", "x処理なし x=" + x + " leftSideX=" + leftSideX + " rightSideX=" + rightSideX);
                    if( rightSideX == viewWidth  ){
                        Log.i("スクロール", "右限界 x=" + x + " rightSideX=" + rightSideX);
                        if( x < 0 ){
                            Log.i("スクロール", "右限界 xを１に");
                            x = 1;
                        }
                        x = -1f;
                        isEdge = true;
                        direction = 2;
                    }
                    if( leftSideX == 0 ){
                        Log.i("スクロール", "左限界 x=" + x + " leftSideX=" + leftSideX);
                        x = 1;
                        isEdge = true;
                        direction = 1;
                    }
                }*/
            }


/*
            if( isEdge ){
                if( direction == 1 ){
                    if( x > 0 ){
                        //ページ制御
                        Log.i("スクロール", "右に行く場合は、スクロール不可にする");
                        setPageScroll( false );
                    }

                } else if( direction == 2 ){
                    if( x < 0 ){
                        //ページ制御
                        Log.i("スクロール", "左に行く場合は、スクロール不可にする");
                        setPageScroll( false );
                    }
                }
            }
*/

            //ページ制御
            setPageScroll( isEdge );

            if(viewHeight > imageHeight){
                y = 0;
            } else {
                if(topY > 0 && y > 0 ){
                    //上がった分を埋める
                    y = -topY;

                } else if( (bottomY < viewHeight) && (y < 0) ){
                    //下がった分を埋める
                    y = viewHeight - bottomY;

                } else {
                    if( bottomY == viewHeight ){
                        //一番上まできていれば、移動なし
                        y = -1;
                    } else if( topY == 0 ) {
                        //一番下まできていれば、移動なし
                        y = 1;
                    }
                }
            }

            Log.i("スクロール", "値チェック x=" + x + " leftSideX=" + leftSideX);
            Log.i("スクロール", "---------------------");

            //Matrixを操作
            matrix.postTranslate(x, y);
            //再描画
            invalidate();

            return super.onScroll(event1, event2, distanceX, distanceY);
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
