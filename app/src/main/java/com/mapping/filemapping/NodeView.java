package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

public class NodeView extends ChildNode implements Serializable {

    /*
     * コンストラクタ
     *   レイアウトから
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, AttributeSet attrs) {
        //super(context, new NodeTable(), null, R.layout.node);
        //super(context, null, R.layout.node_outside);
        super(context, null, R.layout.node);
    }

    /*
     * コンストラクタ
     *   new
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, NodeTable node) {
        //super(context, node, R.layout.node_outside);
        super(context, node, R.layout.node);
        //Log.i("NodeView", "3");

        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {
        //Log.i("NodeView", "init");
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
