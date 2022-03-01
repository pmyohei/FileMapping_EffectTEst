package com.mapping.filemapping;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

/*
 * ダイアログ
 * 　　処理中
 */
public class MyProgressDialog extends DialogFragment {

    public MyProgressDialog(){} //空のコンストラクタ（DialogFragmentのお約束）

    //インスタンス作成
    public static MyProgressDialog newInstance() {
        return new MyProgressDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.progress_dialog);

        //画面外タッチ時のクローズ不可
        dialog.setCanceledOnTouchOutside(false);

        /*-- キャンセル不可（setCancelable(false)）は、このタイミングで設定しても反映されないため注意 --*/

        //タイトル
        String title = getResources().getString(R.string.updating);
        dialog.setTitle(title);

        return dialog;
    }
}
