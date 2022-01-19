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

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class DesignDialog extends DialogFragment {

    //タグ
    public static final String TAG_NODE = "node";
    public static final String TAG_MAP = "map";
    public static final String TAG_ONLY_SIZE = "size";

    //設定対象ノードビュー
    private View     mv_map;
    private BaseNode mv_node;


    /*
     * コンストラクタ（ノードデザイン）
     */
    public DesignDialog(View v_map) {
        mv_map = v_map;
    }

    /*
     * コンストラクタ（ノードデザイン）
     */
    public DesignDialog(BaseNode v_node) {
        mv_node = v_node;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //ダイアログにレイアウトを設定
        return inflater.inflate(R.layout.design_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //ダイアログ取得
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //背景を透明にする(デフォルトテーマに付いている影などを消す) ※これをしないと、画面横サイズまで拡張されない
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        //ダイアログ外タッチ時、ダイアログを閉じないようにする
        setCancelable(false);

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
        setupDialogSize(dialog);

        ViewPager2 vp;
        if( getTag().equals( TAG_NODE ) ){
            //ノードデザイン指定
            vp = setupNodeDesignLayout();
        } else if( getTag().equals( TAG_MAP ) ) {
            //マップデザイン指定
            vp = setupMapDesignLayout();
        } else {
            //ノードサイズのみの指定
            vp = setupNodeSizeLayout();
        }

        //インジケータの設定
        TabLayout tabLayout = dialog.findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, vp,
                (tab, position) -> tab.setText("")
        ).attach();

        //キャンセル
        dialog.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //★ノードを元の状態に戻す


                dismiss();
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
        lp.height = (int) (metrics.heightPixels * ResourceManager.NODE_DESIGN_DIALOG_RATIO);
        lp.width = metrics.widthPixels;
        lp.gravity = Gravity.BOTTOM;

        //サイズ設定
        window.setAttributes(lp);
        //window.setFlags( 0 , WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    /*
     * ノードデザイン用のレイアウトを設定
     */
    private ViewPager2 setupNodeDesignLayout() {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.page_node_text);
        layoutIdList.add(R.layout.page_node_design);
        layoutIdList.add(R.layout.page_node_line_design);

        ViewPager2 vp2 = getDialog().findViewById(R.id.vp2_design);

        NodeDesignAdapter adapter = new NodeDesignAdapter(layoutIdList, mv_node, ((FragmentActivity) getContext()).getSupportFragmentManager(), vp2);
        vp2.setAdapter(adapter);

        return vp2;
    }

    /*
     * マップデザイン用のレイアウトを設定
     */
    private ViewPager2 setupMapDesignLayout() {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.page_map_design);
        layoutIdList.add(R.layout.page_all_node_design);
        layoutIdList.add(R.layout.page_node_line_design);

        ViewPager2 vp = getDialog().findViewById(R.id.vp2_design);
        MapDesignAdapter adapter = new MapDesignAdapter(layoutIdList, mv_map, ((FragmentActivity) getContext()).getSupportFragmentManager(), vp);
        vp.setAdapter(adapter);

        return vp;
    }

    /*
     * ノードサイズ限定のレイアウトを設定
     */
    private ViewPager2 setupNodeSizeLayout() {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.page_node_shape);

        ViewPager2 vp = getDialog().findViewById(R.id.vp2_design);
        NodeShapeAdapter adapter = new NodeShapeAdapter(layoutIdList, mv_map, ((FragmentActivity) getContext()).getSupportFragmentManager(), vp);
        vp.setAdapter(adapter);

        return vp;
    }
}