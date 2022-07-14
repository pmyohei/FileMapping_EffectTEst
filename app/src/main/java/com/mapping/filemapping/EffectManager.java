package com.mapping.filemapping;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import java.util.Random;

public class EffectManager {

    //エフェクトビュー追加先レイアウト
    final private ViewGroup mAddDistView;
    //エフェクトの形状
    private int mEffectShape;
    //エフェクトアニメーション
    private int mEffectAnimation;
    //エフェクトのPaintStyle
    private Paint.Style mPaintStyle;
    //エフェクト量
    private int mEffectVolume;

    /*
     * コンストラクタ
     */
    public EffectManager(ViewGroup parentView) {
        mAddDistView = parentView;
    }

    /*
     * コンストラクタ
     *   para1：エフェクトビュー追加先のレイアウト
     *   para2：エフェクトの形状
     *   para3：エフェクトのアニメーション
     */
    public EffectManager(ViewGroup parentView, int shape, Paint.Style paintStyle, int animation) {
        mAddDistView = parentView;

        //エフェクト情報の設定
        setEffectAttr(shape, paintStyle, animation);
    }

    /*
     * エフェクト開始
     */
    public void startEffect() {
        //エフェクトを生成
        createEffects();
    }

    /*
     * エフェクト停止
     */
    public void stopEffect() {
        //エフェクトを削除
        removeEffects();
    }

    /*
     * エフェクト再開始
     *   表示中のエフェクトを削除し、現在設定中のエフェクト設定値で再開始
     */
    public void restartEffect() {

        //------------------------
        // 描画中のエフェクトを削除
        //------------------------
        removeEffects();

        //------------------------
        // 新しいエフェクトを生成
        //------------------------
        createEffects();
    }

    /*
     * エフェクト情報の設定
     *   para1：エフェクト形状
     *   para2：エフェクトアニメーション
     */
    public void setEffectAttr(int shape, Paint.Style paintStyle, int animation) {
        mEffectShape = shape;
        mPaintStyle = paintStyle;
        mEffectAnimation = animation;

        //初期値
        mEffectVolume = 20;
    }

    /*
     * エフェクト情報の設定
     *   para1：エフェクト形状
     *   para2：エフェクトアニメーション
     */
    public void setEffectVolume(int volume) {
        mEffectVolume = volume;
    }

    /*
     * エフェクト生成
     */
    public void createEffects() {

        //レイアウト確定待ち
        //mAddDistView.post(() -> {
            //中央座標を取得
            int centerX = mAddDistView.getWidth() / 2;
            int centerY = mAddDistView.getHeight() / 2;

            int rangeX = (int)(mAddDistView.getWidth() * 0.4f);
            int rangeY = (int)(mAddDistView.getHeight() * 0.4f);
            int absRangeX = rangeX / 2;
            int absRangeY = rangeY / 2;

            Log.i("エフェクト生成位置", "centerX=" + centerX + " centerY=" + centerY);

            //---------------------------------
            //エフェクトビューの生成
            //---------------------------------
            for (int i = 0; i < mEffectVolume; i++) {

                //---------------------------------
                // エフェクトビュー生成・レイアウトへ追加
                //---------------------------------
                EffectView effectView = new EffectView(mAddDistView.getContext(), mEffectShape, mPaintStyle);
                mAddDistView.addView(effectView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                //---------------------------------
                // 生成座標の設定
                //---------------------------------
                //座標offsetをランダムに設定
                Random random = new Random();
                int offsetX = random.nextInt( rangeX ) - absRangeX;
                int offsetY = random.nextInt( rangeY ) - absRangeY;

                //位置設定
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) effectView.getLayoutParams();
                mlp.setMargins(centerX + offsetX, centerY + offsetY, mlp.rightMargin, mlp.bottomMargin);

                //Log.i("エフェクト生成位置", "位置x=" + (centerX + offsetX) + " 位置y=" + (centerY + offsetY));

                //---------------------------------
                //　アニメーションの適用
                //---------------------------------
                applyEffectAnimation( effectView );
            }
        //});
    }

    /*
     * エフェクト削除
     */
    private void removeEffects() {
        //子ビュー数
        int childNum = mAddDistView.getChildCount();

        //------------------------------------
        // 最後尾から削除チェック
        //------------------------------------
        for( int i = childNum - 1; i >= 0; i--){
            //エフェクトビューならレイアウトから除外
            View view = mAddDistView.getChildAt(i);
            if( view instanceof EffectView ){
                mAddDistView.removeView( view );
            }
        }
    }

    /*
     * エフェクト有効／無効
     */
    private void visibilityEffects(int visible) {

        //--------------------------------
        // 全てのエフェクト可視状態を変更する
        //--------------------------------

    }

    /*
     * エフェクトアニメーションのエフェクトビューへの適用
     */
    private void applyEffectAnimation(View animationTarget) {

        //--------------------------------
        // 指定アニメーションに応じて適用
        //--------------------------------
        switch (mEffectAnimation) {
            //------------------------
            // 明滅
            //------------------------
            case MapTable.BLINK:
                applyBlinkEffectAnimation(animationTarget);
                break;

            //------------------------
            // 場所を変えながら明滅
            //------------------------
            case MapTable.BLINK_MOVE:
                applyBlinkMoveEffectAnimation(animationTarget);
                break;

            //------------------------
            // 回転
            //------------------------
            case MapTable.SPIN:
                applySpinEffectAnimation(animationTarget);
                break;

            //------------------------
            // ゆっくりな移動
            //------------------------
            case MapTable.SLOW_MOVE:

                break;

            //------------------------
            // ゆっくりと浮き上がる
            //------------------------
            case MapTable.SLOW_FLOAT:

                break;

            //------------------------
            // 枠線のグラデーションの回転
            //------------------------
            case MapTable.STROKE_GRADATION_ROTATE:
                applyStrokeGradationRotateEffectAnimation(animationTarget);
                break;




            //------------------------
            // 処理なし
            //------------------------
            default:
                break;
        }
    }

    /*
     * エフェクトアニメーション
     * 　　明滅
     */
    private void applyBlinkEffectAnimation( View animationTarget ) {

        //------------------------
        // 乱数値
        //------------------------
        Random random = new Random();
        int duration = random.nextInt( 2501 ) + 1000;
        int delay    = random.nextInt( 1001 );

        //------------------------
        // アニメーションの適用
        //------------------------
        AnimatorSet tmpSet = (AnimatorSet) AnimatorInflater.loadAnimator(animationTarget.getContext(), R.animator.effect_blink);
        tmpSet.setTarget(animationTarget);
        tmpSet.setDuration( duration );
        //tmpSet.setStartDelay( delay );
        tmpSet.start();
    }

    /*
     * エフェクトアニメーション
     * 　　明滅：移動あり
     */
    private void applyBlinkMoveEffectAnimation( View animationTarget ) {

        //------------------------
        // 乱数値
        //------------------------
        Random random = new Random();
        int duration = random.nextInt( 2501 ) + 1000;

        //------------------------
        // アニメーションの適用
        //------------------------
        Animator anim = AnimatorInflater.loadAnimator(animationTarget.getContext(), R.animator.effect_blink);
        anim.setTarget( animationTarget );
        anim.setDuration( duration );
/*        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                Log.i("アニメーション開始", "コールチェック");
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                Log.i("アニメーション開始", "コールチェック　エンド");
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
                Log.i("アニメーション開始", "コールチェック　リピート");
            }
        });*/

        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationRepeat(Animator animation) {
                Log.i("アニメーション開始", "コールチェック　リピートA");
            }});


        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(animationTarget, "alpha", 1f, 0f);
        fadeAnim.setDuration( 1000 );
        fadeAnim.setRepeatMode( ValueAnimator.REVERSE );
        fadeAnim.setRepeatCount( ValueAnimator.INFINITE );
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationRepeat(Animator animation) {
                Log.i("アニメーション開始", "コールチェック　リピートAAA");
            }});
        fadeAnim.start();

        //AnimatorSet tmpSet = (AnimatorSet) AnimatorInflater.loadAnimator(animationTarget.getContext(), R.animator.effect_blink);
        AnimatorSet tmpSet = new AnimatorSet();
        //tmpSet.setTarget(animationTarget);
        //tmpSet.setDuration( duration );

        //tmpSet.play(anim);
        //tmpSet.start();
    }


    /*
     * エフェクトアニメーション
     * 　　回転
     */
    private void applySpinEffectAnimation( View animationTarget ) {

        //------------------------
        // 乱数値
        //------------------------
        Random random = new Random();
        int duration = random.nextInt( 8001 ) + 8000;
        int delay    = random.nextInt( 1001 );

        //------------------------
        // アニメーションの適用
        //------------------------
        AnimatorSet tmpSet = (AnimatorSet) AnimatorInflater.loadAnimator(animationTarget.getContext(), R.animator.effect_spin);
        tmpSet.setTarget(animationTarget);
        tmpSet.setDuration( duration );
        //tmpSet.setStartDelay( delay );
        tmpSet.start();
    }


    /*
     * エフェクトアニメーション
     * 　　枠線のグラデーション回転
     */
    private void applyStrokeGradationRotateEffectAnimation( View animationTarget ) {

        //------------------------
        // 乱数値
        //------------------------
        Random random = new Random();
        int duration = random.nextInt( 8001 ) + 4000;
        int delay    = random.nextInt( 1001 );

        //------------------------
        // アニメーションの適用
        //------------------------
        EffectAnimation animation = new EffectAnimation((EffectView)animationTarget);
        animation.setDuration(duration);
        //animation.setStartOffset(delay);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);

        animationTarget.startAnimation(animation);
    }

}
