package com.mapping.filemapping;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.viewpager2.widget.ViewPager2;

/*
 * 実験用
 * 　　単体写真の横スクロール用のビュー
 *     スクロールされた時、移動の方向と左右のスクロール可否をチェックし、ページ送りを制御する
 *     ※ページ送りの制御はできるが、タッチムーブ中にページ送りができなかったため、廃案
 */
public class NestedHorizontalScrollView_old extends HorizontalScrollView {

    public NestedHorizontalScrollView_old(Context context) {
        super(context);
    }
    public NestedHorizontalScrollView_old(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NestedHorizontalScrollView_old(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected int computeHorizontalScrollRange() {
        Log.i("ズーム問題", "super.computeHorizontalScrollRange()=" + super.computeHorizontalScrollRange() );
        return 4000;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (x < 0) x = 0;
        else {
            int maxWidth = computeHorizontalScrollRange() - getWidth();
            if (maxWidth < 0) x = 0;
            else if (x > maxWidth) x = maxWidth;
        }

        Log.i("ズーム問題","scrollTo()");
        super.scrollTo(x, y);
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged( l, t, oldl, oldt);

        Log.i("ページ操作", "スクロール量=" + (l - oldl));

        //スクロール量
        int distanceX = l - oldl;

        //親のViewPager
        ViewPager2 vp2_singlePicture = getRootView().findViewById(R.id.vp2_singlePicture);

        if( distanceX > 0 ){
            //タッチが右に移動
            if( canScrollHorizontally(1) ){
                //右端まできていれば（右にスクロールできなければ）
                //vp2_singlePicture.setUserInputEnabled(true);
            }

        } else if( distanceX < 0 ){
            //タッチが左に移動
            if( canScrollHorizontally(-1) ){
                //左端まできていれば（左にスクロールできなければ）
                //vp2_singlePicture.setUserInputEnabled(true);
            }
        }

    }

}
