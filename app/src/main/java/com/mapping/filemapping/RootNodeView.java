package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
     * 比率込みのノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
/*    @Override
    public float getScaleNodeBodyWidth() {
        //現在の横幅 * 現在の比率
        return findViewById(R.id.cv_node).getWidth() * mNode.getSizeRatio();
    }*/

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
