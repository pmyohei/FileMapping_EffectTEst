package com.mapping.filemapping;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

/*
 * ギャラリーとして表示する写真用のビュー
 *   Checkableを実装し、選択状態を管理できるようにカスタマイズ
 */
public class PictureInGalleryView extends LinearLayout implements Checkable {

    //チェック（選択）状態
    private boolean mIsChecked;

    public PictureInGalleryView(Context context) {
        super(context);

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.item_gallery_picture, this, true);

        mIsChecked = false;
    }

    public PictureInGalleryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PictureInGalleryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PictureInGalleryView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    /*
     * ビューのチェック状態を変更
     */
    private void setCheckStateView(boolean b) {

        //選択状態の変更対象ビュー
        MaterialCardView mcv_picture = findViewById( R.id.mcv_picture );

        if( b ){
            //選択中に設定
            mcv_picture.setStrokeWidth( 20 );
        } else {
            //非選択中に設定
            mcv_picture.setStrokeWidth( 0 );
        }

    }


    /**** Checkable ****/
    @Override
    public void setChecked(boolean b) {
        //Log.i("複数選択対応", "コール確認 setChecked() b=" + b);
        mIsChecked = b;

        //ビューの状態を更新
        setCheckStateView( b );
    }

    @Override
    public boolean isChecked() {
        //Log.i("複数選択対応", "コール確認 isChecked()");
        return mIsChecked;
    }

    @Override
    public void toggle() {
        //状態反転
        setChecked( !mIsChecked );
        //Log.i("複数選択対応", "コール確認 toggle()");
    }
    /**** Checkable ****/
}
