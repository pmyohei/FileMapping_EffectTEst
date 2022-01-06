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
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.jaredrummler.android.colorpicker.ColorPickerView;

public class ColorPickerDialog extends DialogFragment {

    private View.OnClickListener mPositiveClickListener;    //ボタンクリックリスナー

    /*
     * コンストラクタ
     */
    public ColorPickerDialog() { }

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

        //カラーピッカー
        ColorPickerView cpv = dialog.findViewById(R.id.colorPicker);
        cpv.setOnColorChangedListener( new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int newColor) {
                Log.i("onColorChanged", "getColor=" + cpv.getColor());
                Log.i("onColorChanged", "getColor(Hex)=" + Integer.toHexString(cpv.getColor()) );

                //cpv.getColor()で取得できる値「ARGB」 例）ffb58d8d
                String code = "#" + Integer.toHexString( cpv.getColor() );

                //選択された色をチェック用のビューに反映
                dialog.findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( code ) );

                dialog.findViewById(R.id.v_checkColor).getBackground();
            }
        });

        //OKボタン
        dialog.findViewById(R.id.bt_positive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //リスナーに渡すビューを、カラーピッカーに入れ替え
                Log.i("onColorChanged", "OK getColor(Hex)=" + Integer.toHexString(cpv.getColor()) );
                mPositiveClickListener.onClick( cpv );
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
