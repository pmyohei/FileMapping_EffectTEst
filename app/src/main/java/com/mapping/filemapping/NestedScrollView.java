package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class NestedScrollView extends ScrollView {

    public boolean mIsIntercept = false;

    public NestedScrollView(Context context) {
        super(context);
    }
    public NestedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NestedScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //子要素からTouchEventが抑止されないように、何も処理しない
        //※デフォルトでは、子要素がスクロールされたとき、本メソッドがコールされ、
        //　本ビュー自体のスクロールが停止させられてしまうため、
        //　何も処理しない処理にすることで、本ビューのスクロールも同時に可能にしている

        //
        if( mIsIntercept ){
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    /*
     * 子よりも先にタッチイベントを取得
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //TouchEventを子へ伝播させるため、自分でonTouchEventを処理
        onTouchEvent(ev);

        //常に子へ伝播する
        return false;
    }
}
