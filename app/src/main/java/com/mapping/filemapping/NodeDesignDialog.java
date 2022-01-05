package com.mapping.filemapping;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class NodeDesignDialog extends DialogFragment {

    /*
     * コンストラクタ
     */
    public NodeDesignDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ダイアログにレイアウトを設定
        return inflater.inflate(R.layout.node_design_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setFlags( 0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);

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
        if( dialog == null ){
            return;
        }

        //サイズ設定
        setupDialogSize(dialog);

        //
        List<Integer> list = new ArrayList<>();
        list.add(R.layout.input_node_name);
        list.add(R.layout.input_node_design);
        list.add(R.layout.input_node_line);

        NodeDesignAdapter adapter = new NodeDesignAdapter(list);
        ViewPager2 vp = dialog.findViewById(R.id.vp2_guide);
        vp.setAdapter(adapter);

        //インジケータの設定
        TabLayout tabLayout = dialog.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, vp,
                (tab, position) -> tab.setText("")
        ).attach();


    }

    /*
     * ダイアログサイズ設定
     */
    private void setupDialogSize( Dialog dialog ){

        Window window= dialog.getWindow();

        //画面メトリクスの取得
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        //レイアウトパラメータ
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height  = metrics.heightPixels / 2;
        lp.width   = metrics.widthPixels;
        lp.gravity = Gravity.BOTTOM;

        //サイズ設定
        window.setAttributes(lp);
        //window.setFlags( 0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }


}
