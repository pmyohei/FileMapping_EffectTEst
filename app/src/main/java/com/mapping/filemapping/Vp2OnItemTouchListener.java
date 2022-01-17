package com.mapping.filemapping;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/*
 * ViewPager2内で「RecyclerViewの横スクロール」を可能にする
 */
public class Vp2OnItemTouchListener implements RecyclerView.OnItemTouchListener {

    private final ViewPager2 mvp2;

    /*
     * コンストラクタ
     */
    public Vp2OnItemTouchListener(ViewPager2 vp2){
        mvp2 = vp2;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //タッチされた時、ViewPager2のスクロールを無効化
                mvp2.setUserInputEnabled(false);
                break;

            case MotionEvent.ACTION_UP:

                //タッチが離れた時、ViewPager2のスクロールを有効化
                mvp2.setUserInputEnabled(true);
                break;
        }

        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    }
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }
}
