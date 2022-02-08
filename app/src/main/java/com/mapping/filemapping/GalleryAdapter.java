package com.mapping.filemapping;

import static android.content.Context.VIBRATOR_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryAdapter extends BaseAdapter {

    private final PictureArrayList<PictureTable> mData;


    /*
     * 日付レイアウトクラス
     */
    private class ViewHolder {

        //セル位置
        private int position;
        private ImageView iv_picture;

        /*
         * ビューの設定
         */
        @SuppressLint("ClickableViewAccessibility")
        public void setView( View view, PictureTable picture ) {

            iv_picture = view.findViewById( R.id.iv_picture );

            //トリミング範囲で切り取り
            Bitmap bitmap = BitmapFactory.decodeFile( picture.getPath() );
            //画像設定
            iv_picture.setImageBitmap( bitmap );

            //レイアウト全体にタッチリスナーを設定
            //ll_cell.setClickable(true);                             //！これがないとダブルタップが検知されない
            //ll_cell.setOnTouchListener(new DateTouchListener( this ));
        }


    }

    /*
     * コンストラクタ
     */
    public GalleryAdapter(PictureArrayList<PictureTable> data){
        mData = data;
    }

    @Override
    public int getCount() {
        //その月の日数
        return mData.size();
    }

    /*
     * セル一つ一つを描画する際にコールされる。
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //findViewById()で取得した参照を保持するためのクラス
        ViewHolder holder;

        Context context = parent.getContext();

        //初めて表示されるなら、セルを割り当て。セルはレイアウトファイルを使用。
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gallery_picture, null);
            //convertView = new DateCellView(mContext);

            //ビューを生成。レイアウト内のビューを保持。
            holder = new ViewHolder();

            //タグ設定
            convertView.setTag(holder);

        } else {
            //一度表示されているなら、そのまま活用
            holder = (ViewHolder)convertView.getTag();
        }

        //ビューの設定
        holder.setView( convertView, mData.get(position) );

        //セルのサイズを指定
        //画面解像度の比率を取得
        float dp = context.getResources().getDisplayMetrics().density;
        //セルの幅と高さ
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                parent.getWidth() / 2 - (int)dp,
                parent.getWidth() / 2 - (int)dp);
        convertView.setLayoutParams(params);

        //設定したビューを返す(このビューが日付セルとして表示される)
        return convertView;
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public Object getItem(int position) {
        return null;
    }



/*
    横になったとき
    //自身へ変更通知
    this.notifyDataSetChanged();*/
}

