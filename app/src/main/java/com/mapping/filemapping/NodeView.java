package com.mapping.filemapping;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

public class NodeView extends ChildNode implements Serializable {

    /*
     * コンストラクタ
     *   レイアウトから
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, AttributeSet attrs) {
        super(context, null, R.layout.node);
    }

    /*
     * コンストラクタ
     *   new
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, NodeTable node) {
        super(context, node, R.layout.node);

        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {
        //Log.i("NodeView", "init");
    }

    /*
     * ノード背景色の設定
     *   para：例)#123456
     */
    @Override
    public void setNodeBackgroundColor(String color) {
        //背景色を設定
        //ColorDrawable colorDrawable = (ColorDrawable)findViewById(R.id.tv_node).getBackground();
        //colorDrawable.setColor( color );
        //Drawable drawable = findViewById(R.id.tv_node).getBackground();
        //drawable.setTint( color );
        //CardView cv_node = findViewById(R.id.cv_node);
        //cv_node.setBackgroundColor( Color.parseColor(color) );
        //cv_node.setCardBackgroundColor(Color.parseColor(color));

        //---------------------------------------
        // アニメーション付きで背景色を変更
        //---------------------------------------
        CardView cv_node = findViewById(R.id.cv_node);
        //変更前と変更後の色
        int srcColor = Color.parseColor( mNode.getNodeColor() );
        int dstColor = Color.parseColor( color );
        //設定メソッドは、「CardViewのsetCardBackgroundColor()」
        startTranceColorAnimation(getContext(), cv_node, "cardBackgroundColor", srcColor, dstColor);

/*        //アニメーション時間
        int duration = getResources().getInteger(R.integer.color_trance_animation_duration);

        ValueAnimator tranceAnimator = ObjectAnimator.ofArgb(cv_node, "cardBackgroundColor", srcColor, dstColor);
        tranceAnimator.setDuration( duration );

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(tranceAnimator);
        animatorSet.start();*/

        //---------------------------------------
        // テーブル更新
        //---------------------------------------
        mNode.setNodeColor( color );
    }

    /*
     * ノード名のテキスト色の設定
     *   para：例)#123456
     */
    @Override
    public void setNodeTextColor(String color) {
        //((TextView) findViewById(R.id.tv_node)).setTextColor( Color.parseColor(color) );

        //---------------------------------------
        // アニメーション付きでテキスト色を変更
        //---------------------------------------
        TextView tv_node = findViewById(R.id.tv_node);
        //変更前と変更後の色
        int srcColor = Color.parseColor( mNode.getTextColor() );
        int dstColor = Color.parseColor( color );
        //設定メソッドは、「TextViewのsetTextColor」
        startTranceColorAnimation(getContext(), tv_node, "textColor", srcColor, dstColor);

        mNode.setTextColor( color );
    }

    /*
     * ノード名のフォント設定
     */
    @Override
    public void setNodeFont(Typeface font, String fontFileName) {

        TextView tv_node = findViewById(R.id.tv_node);
        tv_node.setTypeface( font );

        //ノードの形状を整える
        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //ノードの形状を設定
                        //※フォントによってサイズが変わるため
                        setNodeShape( mNode.getNodeShape() );
                    }
                }
        );

        //ファイル名を保存
        mNode.setFontFileName( fontFileName );
    }

    /*
     * ノード枠線サイズの設定
     */
    @Override
    public void setBorderSize( int thick ) {
        //枠サイズを設定
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeWidth( thick );

        mNode.setBorderSize( thick );
    }

    /*
     * ノード枠色の設定
     */
    @Override
    public void setBorderColor( String color ) {
        //枠色を設定
        //((MaterialCardView)findViewById( R.id.cv_node )).setStrokeColor( Color.parseColor(color) );

        //---------------------------------------
        // アニメーション付きで背景色を変更
        //---------------------------------------
        CardView cv_node = findViewById(R.id.cv_node);
        //変更前と変更後の色
        int srcColor = Color.parseColor( mNode.getBorderColor() );
        int dstColor = Color.parseColor( color );
        //設定メソッドは、「MaterialCardViewのsetStrokeColor」
        startTranceColorAnimation(getContext(), cv_node, "strokeColor", srcColor, dstColor);


        mNode.setBorderColor( color );
    }


    /*
     * ノード形の設定
     */
    @Override
    public void setNodeShape( int shapeKind ) {

        //長い方の辺
        TextView tv_node = findViewById(R.id.tv_node);
        int max = Math.max( tv_node.getWidth(), tv_node.getHeight() );

        float radius = -1;
        if( shapeKind == NodeTable.CIRCLE ){
            radius = max / 2.0f;
        } else if ( shapeKind == NodeTable.SQUARE_ROUNDED ){
            radius = max * ResourceManager.SQUARE_CORNER_RATIO;
        }

        //ノード全体設定で指定された時のために、ノードに設定できない形状が指定された場合は何もしない
        if( radius == -1 ){
            return;
        }

        //長い方の辺で縦横サイズを統一
        MaterialCardView cv_node = findViewById(R.id.cv_node);
        cv_node.setMinimumHeight(max);
        cv_node.setMinimumWidth(max);
        cv_node.setRadius(radius);

        mNode.setNodeShape( shapeKind );
    }


}
