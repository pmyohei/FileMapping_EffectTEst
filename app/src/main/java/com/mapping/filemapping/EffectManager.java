package com.mapping.filemapping;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.graphics.Paint;
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
     * エフェクト更新
     */
    public void updateEffect() {

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
    }

    /*
     * エフェクト生成
     */
    private void createEffects() {

        //レイアウト確定待ち
        mAddDistView.post(() -> {
            //中央座標を取得
            int centerX = mAddDistView.getWidth() / 2;
            int centerY = mAddDistView.getHeight() / 2;

            //---------------------------------
            //エフェクトビューの生成
            //---------------------------------
            for (int i = 0; i < 40; i++) {

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
                int offsetX = random.nextInt(centerX / 4 + 1);
                int offsetY = random.nextInt(centerY / 4 + 1);
                //座標をばらけさせる（半分の割合を逆の座標へ）
                if ((offsetX % 2) == 0) {
                    offsetX *= -1;
                }
                if ((offsetY % 2) == 0) {
                    offsetY *= -1;
                }

                //位置設定
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) effectView.getLayoutParams();
                mlp.setMargins(centerX + offsetX, centerY + offsetY, mlp.rightMargin, mlp.bottomMargin);

                //---------------------------------
                //　アニメーションの適用
                //---------------------------------
                applyEffectAnimation( effectView );
            }
        });
    }

    /*
     * エフェクト削除
     */
    private void removeEffects() {


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
        tmpSet.setStartDelay( delay );
        tmpSet.start();
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
        tmpSet.setStartDelay( delay );
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
        animation.setStartOffset(delay);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animationTarget.startAnimation(animation);
    }

}
