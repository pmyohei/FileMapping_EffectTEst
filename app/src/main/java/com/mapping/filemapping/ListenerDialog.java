package com.mapping.filemapping;

import android.app.Dialog;
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

public class ListenerDialog extends DialogFragment {

    public View.OnClickListener mPositiveClickListener;    //OKボタンクリックリスナー

    /*
     * コンストラクタ
     */
    public ListenerDialog() {
    }

    @Override
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
    }

    /*
     * OKボタンリスナー設定
     */
    public void setOnPositiveClickListener(View.OnClickListener listener) {
        mPositiveClickListener = listener;
    }
}
