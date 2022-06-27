package com.mapping.filemapping;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class EffectAnimation extends Animation {

    //アニメーション適用対象エフェクトビュー
    final private EffectView mEffectView;


    /*
     * コンストラクタ
     */
    public EffectAnimation(EffectView effectView) {
        this.mEffectView = effectView;
    }

    /*
     * 本メソッド
     *   アニメーションデータを設定する
     */
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        //グラデーションを設定
        mEffectView.setStrokeGradationEffect( interpolatedTime );
        mEffectView.invalidate();
    }

}
