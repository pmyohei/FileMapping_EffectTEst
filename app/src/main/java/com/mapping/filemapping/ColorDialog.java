package com.mapping.filemapping;

import android.app.Dialog;
import android.graphics.Color;
import android.view.View;

import androidx.fragment.app.DialogFragment;

public class ColorDialog extends ListenerDialog {

    //初期カラー
    public String mInitColorStr;

    /*
     * コンストラクタ
     */
    public ColorDialog(String color) {
        mInitColorStr = color;
    }

/*    @Override
    public void onStart() {
        super.onStart();

        //ダイアログ取得
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        setColor();
    }*/

    /*
     * 色情報の設定
     */
    public void setInitColor() {
        //確認用ビューに初期色を設定
        getDialog().findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( mInitColorStr ) );
    }
}
