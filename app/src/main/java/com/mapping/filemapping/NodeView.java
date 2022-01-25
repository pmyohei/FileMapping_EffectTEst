package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;

import java.io.Serializable;

public class NodeView extends ChildNode implements Serializable {


    /*
     * コンストラクタ
     *   レイアウトから
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, AttributeSet attrs) {
       // super(context, new NodeTable(), null, R.layout.node);
        super(context, null, null, R.layout.node);
    }

    /*
     * コンストラクタ
     *   new
     */
    @SuppressLint("ClickableViewAccessibility")
    public NodeView(Context context, NodeTable node, ActivityResultLauncher<Intent> nodeOperationLauncher) {
        super(context, node, nodeOperationLauncher, R.layout.node);

        Log.i("NodeView", "3");

        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {

        Log.i("NodeView", "init");

        //ツールアイコン設定
        setNodeToolIcon();
    }

    /*
     * ツールアイコン設定
     */
    public void setNodeToolIcon() {

        //ノードの別マップ化

    }

}
