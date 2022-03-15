package com.mapping.filemapping;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

import androidx.fragment.app.DialogFragment;

/*
 * カラーピッカー／カラーコードダイアログの親クラス
 */
public class ColorDialog extends DialogFragment {

    //リスナー
    public interface NoticeDialogListener  {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener listener;

    //OKボタンクリックリスナー
    public View.OnClickListener mPositiveClickListener;
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

        //Cancelボタン
        dialog.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //終了
                dismiss();
            }
        });
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException("must implement NoticeDialogListener");
        }
    }

    /*
     * OKボタンリスナー設定
     */
/*    public void setOnPositiveClickListener(View.OnClickListener listener) {
        mPositiveClickListener = listener;
    }*/

    /*
     * 色情報の設定
     */
    public void setInitColor() {
        //確認用ビューに初期色を設定
        getDialog().findViewById(R.id.v_checkColor).setBackgroundColor( Color.parseColor( mInitColorStr ) );
    }
}
