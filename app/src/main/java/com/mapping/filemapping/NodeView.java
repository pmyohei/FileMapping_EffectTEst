package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import java.io.Serializable;

public class NodeView extends ChildNode implements Serializable {


    /*
     * コンストラクタ
     *   レイアウトから
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, AttributeSet attrs) {
       // super(context, new NodeTable(), null, R.layout.node);
        super(context, null, R.layout.node_outside);
    }

    /*
     * コンストラクタ
     *   new
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, NodeTable node) {
        super(context, node, R.layout.node_outside);

        Log.i("NodeView", "3");

        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {
        Log.i("NodeView", "init");
    }

}
