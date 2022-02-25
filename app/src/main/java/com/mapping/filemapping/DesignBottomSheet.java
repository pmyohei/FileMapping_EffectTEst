package com.mapping.filemapping;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class DesignBottomSheet extends CoordinatorLayout {

    //デザインレイアウト種別
    public static final int MAP = 0;
    public static final int NODE = 1;
    public static final int PICTURE_NODE = 2;
    public static final int SHAPE_ONLY = 3;

    /*
     * コンストラクタ
     */
    public DesignBottomSheet(Context context) {
        this(context, null);
    }

    public DesignBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DesignBottomSheet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.design_bottom_sheet, this, true);
    }

    /*
     * 高さ設定
     *   画面の縦サイズに対して、指定された割合のサイズをBottomSheetの高さに設定
     */
    public void setBottomSheetHeight(Context context, float ratio) {

        //BottomSheet
        LinearLayout bs_design = findViewById(R.id.ll_bottomSheet);

        //高さを設定
        ViewGroup.LayoutParams layoutParams = bs_design.getLayoutParams();
        int windowHeight = getWindowHeight(context);
        if (layoutParams != null) {
            //画面の高さの半分
            layoutParams.height = (int) (windowHeight * ratio);
        }
        bs_design.setLayoutParams(layoutParams);
    }

    /*
     * 画面縦サイズの取得
     */
    private int getWindowHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /*
     * BottomSheetを開く
     */
    public void openBottomSheet(int kind, View view) {

        final float HALF = 0.5f;
        final float ONE_THIRD = 0.4f;

        float heightRatio;

        //レイアウト構築
        ViewPager2 vp;
        if (kind == NODE) {

            //ノード種別で切り分け
            if (((BaseNode) view).getNode().getKind() == NodeTable.NODE_KIND_PICTURE) {
                //ノードピクチャデザイン指定
                vp = setupPictureNodeDesignLayout(view);
            } else {
                //ノードデザイン指定
                vp = setupNodeDesignLayout(view);
            }

            heightRatio = HALF;

        } else if (kind == MAP) {
            //マップデザイン指定
            vp = setupMapDesignLayout(view);

            heightRatio = HALF;

        } else {
            //ノード形のみの指定
            vp = setupNodeSizeLayout(view);

            heightRatio = ONE_THIRD;
        }

        //高さ設定
        setBottomSheetHeight(getContext(), heightRatio);

        //オープン
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(findViewById(R.id.ll_bottomSheet));
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    /*
     * ノードデザイン用のレイアウトを設定
     */
    private ViewPager2 setupNodeDesignLayout(View v_node) {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add( R.layout.page_node_name);
        layoutIdList.add( R.layout.page_node_font);
        layoutIdList.add( R.layout.page_node_size);
        layoutIdList.add( R.layout.page_node_shape);
        layoutIdList.add( R.layout.page_node_text_color);
        layoutIdList.add( R.layout.page_node_color);
        layoutIdList.add( R.layout.page_node_border_color);
        layoutIdList.add( R.layout.page_node_shadow);
        //ルートノード以外は、ライン用ページを追加
        if (((BaseNode) v_node).getNode().getKind() != NodeTable.NODE_KIND_ROOT) {
            layoutIdList.add(R.layout.page_node_line_color);
        }

        //タブインジケータの文字列
        Resources resources = getResources();
        List<String> tabs = new ArrayList<>();
        tabs.add( resources.getString(R.string.tab_node_name));
        tabs.add( resources.getString(R.string.tab_font));
        tabs.add( resources.getString(R.string.tab_size));
        tabs.add( resources.getString(R.string.tab_shape));
        tabs.add( resources.getString(R.string.tab_text_color));
        tabs.add( resources.getString(R.string.tab_node_color));
        tabs.add( resources.getString(R.string.tab_border_color));
        tabs.add( resources.getString(R.string.tab_shadow));
        if (((BaseNode) v_node).getNode().getKind() != NodeTable.NODE_KIND_ROOT) {
            //ルートノード以外
            tabs.add(getResources().getString(R.string.tab_line_color));
        }

        //ViewPager2を生成
        ViewPager2 vp2 = findViewById(R.id.vp2_design);
        DesignNodePageAdapter adapter = new DesignNodePageAdapter(layoutIdList, v_node);
        vp2.setAdapter(adapter);

        //インジケータの設定
        TabLayout tabLayout = findViewById(R.id.tab_bottomDesign);
        new TabLayoutMediator(tabLayout, vp2,
                (tab, position) -> tab.setText(tabs.get(position))
        ).attach();

        return vp2;
    }


    /*
     * ピクチャノードデザイン用のレイアウトを設定
     */
    private ViewPager2 setupPictureNodeDesignLayout(View v_node) {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add( R.layout.page_set_thumbnail);
        layoutIdList.add( R.layout.page_node_size);
        layoutIdList.add( R.layout.page_node_shape);
        layoutIdList.add( R.layout.page_node_border_color);
        layoutIdList.add( R.layout.page_node_shadow);
        layoutIdList.add( R.layout.page_node_line_color);

        //タブインジケータの文字列
        Resources resources = getResources();
        List<String> tabs = new ArrayList<>();
        tabs.add( resources.getString(R.string.tab_thumnbnail));
        tabs.add( resources.getString(R.string.tab_size));
        tabs.add( resources.getString(R.string.tab_shape));
        tabs.add( resources.getString(R.string.tab_border_color));
        tabs.add( resources.getString(R.string.tab_shadow));
        tabs.add( resources.getString(R.string.tab_line_color));

        //ViewPager2を生成
        ViewPager2 vp2 = findViewById(R.id.vp2_design);
        DesignPicturePageAdapter adapter = new DesignPicturePageAdapter(layoutIdList, v_node, ((FragmentActivity) getContext()).getSupportFragmentManager(), vp2);
        vp2.setAdapter(adapter);

        //インジケータの設定
        TabLayout tabLayout = findViewById(R.id.tab_bottomDesign);
        new TabLayoutMediator(tabLayout, vp2,
                (tab, position) -> tab.setText(tabs.get(position))
        ).attach();

        return vp2;
    }

    /*
     * マップデザイン用のレイアウトを設定
     */
    private ViewPager2 setupMapDesignLayout(View v_map) {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add( R.layout.page_map_design);
        layoutIdList.add( R.layout.page_node_font);
        layoutIdList.add( R.layout.page_node_size);
        layoutIdList.add( R.layout.page_node_shape);
        layoutIdList.add( R.layout.page_node_text_color);
        layoutIdList.add( R.layout.page_node_color);
        layoutIdList.add( R.layout.page_node_border_color);
        layoutIdList.add( R.layout.page_node_shadow);
        layoutIdList.add( R.layout.page_node_line_color);

        //タブインジケータの文字列
        Resources resources = getResources();
        List<String> tabs = new ArrayList<>();
        tabs.add( resources.getString(R.string.tab_map_color));
        tabs.add( resources.getString(R.string.tab_font));
        tabs.add( resources.getString(R.string.tab_size));
        tabs.add( resources.getString(R.string.tab_shape));
        tabs.add( resources.getString(R.string.tab_text_color));
        tabs.add( resources.getString(R.string.tab_node_color));
        tabs.add( resources.getString(R.string.tab_border_color));
        tabs.add( resources.getString(R.string.tab_shadow));
        tabs.add( resources.getString(R.string.tab_line_color));

        //ページアダプタを設定
        ViewPager2 vp2 = findViewById(R.id.vp2_design);
        DesignMapPageAdapter adapter = new DesignMapPageAdapter(layoutIdList, v_map);
        vp2.setAdapter(adapter);

        //インジケータの設定
        TabLayout tabLayout = findViewById(R.id.tab_bottomDesign);
        new TabLayoutMediator(tabLayout, vp2,
                (tab, position) -> tab.setText(tabs.get(position))
        ).attach();

        return vp2;
    }

    /*
     * ノードサイズ限定のレイアウトを設定
     */
    private ViewPager2 setupNodeSizeLayout(View v_node) {
        //ノードデザイン設定レイアウト
        List<Integer> layoutIdList = new ArrayList<>();
        layoutIdList.add(R.layout.page_node_shape);

        ViewPager2 vp = findViewById(R.id.vp2_design);
        NodeShapeAdapter adapter = new NodeShapeAdapter(layoutIdList, v_node, ((FragmentActivity) getContext()).getSupportFragmentManager(), vp);
        vp.setAdapter(adapter);

        return vp;
    }

    /*
     * ボトムシートが閉じているかどうか
     */
    public boolean isCloseBottomSheet() {

        //Log.i("ボトムシートの状態チェック", "getState()=" + behavior.getState());

        //※「STATE_COLLAPSED」：完全に閉じている時の状態
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(findViewById(R.id.ll_bottomSheet));
        return ( behavior.getState() == STATE_COLLAPSED );
    }
}
