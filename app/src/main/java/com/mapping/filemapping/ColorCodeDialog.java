package com.mapping.filemapping;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

public class ColorCodeDialog extends DialogFragment {

    private View.OnClickListener mPositiveClickListener;    //ボタンクリックリスナー

    /*
     * コンストラクタ
     */
    public ColorCodeDialog() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ダイアログにレイアウトを設定
        return inflater.inflate(R.layout.color_code_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //アニメーションを設定
        //dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;

        //ダイアログを返す
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        //ダイアログ取得
        Dialog dialog = getDialog();
        if (dialog == null) {
            return;
        }

        //OKボタン
        dialog.findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //リスナーに渡すビューを、カラーコードのビューに入れ替え
                EditText et_colorCode = dialog.findViewById(R.id.et_colorCode);

                //★入力チェックが必要

                mPositiveClickListener.onClick(et_colorCode);
            }
        });
    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize(Dialog dialog) {

        Window window = dialog.getWindow();

        //画面メトリクスの取得
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        //レイアウトパラメータ
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = metrics.heightPixels / 2;
        lp.width = metrics.widthPixels;
        lp.gravity = Gravity.BOTTOM;

        //サイズ設定
        window.setAttributes(lp);
        //window.setFlags( 0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /*
     * ダイアログサイズ設定
     */
    public void setOnPositiveClickListener(View.OnClickListener listener) {
        mPositiveClickListener = listener;
    }


}
