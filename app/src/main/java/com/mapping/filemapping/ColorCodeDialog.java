package com.mapping.filemapping;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class ColorCodeDialog extends ColorDialog implements TextWatcher {

    /*
     * コンストラクタ
     */
    public ColorCodeDialog( String color ) {
        super(color);
    }

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

        //初期色の設定
        setInitColor();

        //入力されたRGBを色確認用ビューに反映させる
        EditText et_colorCode = dialog.findViewById(R.id.et_colorCode);
        et_colorCode.addTextChangedListener(this);

        //OKボタン
        dialog.findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //無効な文字列チェック
                EditText et_colorCode = getDialog().findViewById(R.id.et_colorCode);
                if( !et_colorCode.getText().toString().matches( "^[a-fA-F0-9]*" ) ){
                    Toast.makeText(et_colorCode.getContext(), getString(R.string.toast_errorRgb), Toast.LENGTH_SHORT).show();
                    return;
                }

                //色確定
                mPositiveClickListener.onClick( dialog.findViewById(R.id.v_checkColor) );
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
    /*
     * ６文字入力されたタイミングで、確認カラーに反映
     */
    @Override
    public void afterTextChanged(Editable editable) {
        //リスナーに渡すビューを、カラーコードのビューに入れ替え
        EditText et_colorCode = getDialog().findViewById(R.id.et_colorCode);
        String rgb = et_colorCode.getText().toString();

        //6文字未満なら、何もしない
        if( rgb.length() < 6 ){
            return;
        }

        //無効な文字列チェック
        //先頭から想定する文字が続いていない場合
        if( !rgb.matches( "^[a-fA-F0-9]*" ) ){
            return;
        }

        //例）「123456」→「#123456」を作成
        String code = "#" + et_colorCode.getText().toString();
        //確認色に反映
        getDialog().findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( code ) );
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
     * 初期色の設定
     */
    @Override
    public void setInitColor() {
        super.setInitColor();

        //「#」を取り除く
        String rgb;
        if( mInitColorStr.length() == 7 ){
            //「#123456」→「123456」
            rgb = mInitColorStr.replace("#", "");
        } else {
            //「#FF123456」→「123456」
            rgb = mInitColorStr.replaceAll("^#..", "");
        }

        Log.i("RGB", "設定文字列=" + mInitColorStr);
        Log.i("RGB", "設定文字列（除去）=" + rgb);

        //RGB文字列を設定
        EditText et_colorCode = getDialog().findViewById(R.id.et_colorCode);
        et_colorCode.setText( rgb );
    }
}
