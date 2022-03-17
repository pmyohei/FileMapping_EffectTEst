package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

/*
 *  ルートノード
 *    Serializable：intentによるデータの受け渡しを行うために実装
 */
public class RootNodeView extends BaseNode implements Serializable {

    /* フィールド */
    //シリアルID
    private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_VIEW;

    /*
     *  コンストラクタ
     * 　 レイアウトに埋め込んだビューの生成時は、本コンストラクタがコールされる
     */
    public RootNodeView(Context context, AttributeSet attrs) {
        //BaseNode
        //super(context, attrs, R.layout.node_outside);
        super(context, attrs, R.layout.node);
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
        CardView cv_node = findViewById(R.id.cv_node);
        //cv_node.setBackgroundColor( Color.parseColor(color) );
        cv_node.setCardBackgroundColor(Color.parseColor(color));

        mNode.setNodeColor( color );
    }

    /*
     * ノード名のテキスト色の設定
     *   para：例)#123456
     */
    @Override
    public void setNodeTextColor(String color) {
        ((TextView) findViewById(R.id.tv_node)).setTextColor( Color.parseColor(color) );

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
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeColor( Color.parseColor(color) );

        mNode.setBorderColor( color );
    }

    /*
     * ノードの形を円形にする
     */
    @Override
    public void setShapeCircle() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);
        //Log.i("Card", "width=" + cv_node.getWidth() + " height=" + cv_node.getHeight());

        //長い方の辺で縦横サイズを統一
        int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setMinimumHeight(max);
        cv_node.setMinimumWidth(max);

        cv_node.setRadius(max / 2.0f);
    }

    /*
     * ノードの形を四角（角丸）にする
     */
    @Override
    public void setShapeSquare() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        //長い方の辺で正方形を作る
        int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setMinimumHeight(max);
        cv_node.setMinimumWidth(max);

        cv_node.setRadius(max * ResourceManager.SQUARE_CORNER_RATIO);
    }

}
