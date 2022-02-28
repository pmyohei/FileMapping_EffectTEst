package com.mapping.filemapping;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;

import com.jaredrummler.android.colorpicker.ColorPickerView;

public class ColorPickerDialog extends ColorDialog {

    /*
     * コンストラクタ
     */
    public ColorPickerDialog( String color ) {
        super(color);
    }

    public ColorPickerDialog() {
        super("test");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ダイアログにレイアウトを設定
        return inflater.inflate(R.layout.color_picker_dialog, container, false);
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

        //サイズ設定
        setupDialogSize();

        //初期色の設定
        setInitColor();

        //カラーピッカー
        ColorPickerView cpv = dialog.findViewById(R.id.colorPicker);
        cpv.setOnColorChangedListener( new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                Log.i("onColorChanged", "getColor=" + cpv.getColor());
                Log.i("onColorChanged", "getColor(Hex)=" + Integer.toHexString(cpv.getColor()) );

                //「例）#123456」を作成
                //cpv.getColor()で取得できる値「ARGB」 例）ffb58d8d
                String code = "#" + Integer.toHexString( cpv.getColor() );

                //選択された色をチェック用のビューに反映
                dialog.findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( code ) );
            }
        });


        //OKボタン
        dialog.findViewById(R.id.bt_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //色確定
                mPositiveClickListener.onClick( dialog.findViewById(R.id.v_checkColor) );
            }
        });
    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize() {

        Window window = getDialog().getWindow();

        //画面メトリクスの取得
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        //レイアウトパラメータ
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = (int)(metrics.heightPixels * 0.8f);
        lp.width  = (int)(metrics.widthPixels * 0.8f);

        //サイズ設定
        window.setAttributes(lp);
    }

    /*
     * 初期色の設定
     */
    @Override
    public void setInitColor() {
        super.setInitColor();

        //カラーピッカーに初期値を設定
        ColorPickerView cpv = getDialog().findViewById(R.id.colorPicker);
        cpv.setColor( Color.parseColor( mInitColorStr ) );
    }
}
