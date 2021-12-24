package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;

import java.io.Serializable;

/*
 *  ルートノード
 */
public interface InterfaceParentNode {

    /*
     * ツールアイコン設定
     *   ・子ノードの追加
     *   ・写真ノードの追加
     */
    void setParentToolIcon();

    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    void reflectNodeInformation();

    /*
     * ノード情報の設定
     */
    void setNodeInformation(NodeTable node);

    /*
     * ノード名の設定
     */
    void setNodeName(String name);

    /*
     * ノード背景色の設定
     */
    void setBackgroundColor(int color);

}
