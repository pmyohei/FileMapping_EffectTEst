package com.mapping.filemapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/*
 * サムネイルをグリッド表示するアダプタ
 */
public class ThumbnailGridAdapter extends BaseAdapter {

    //写真数
    public final static int PORTRAIT_NUM = 2;
    public final static int LANDSCAPE_NUM = 6;

    //参照中の写真
    private PictureTable mShowPicture;
    //マップ上のピクチャノード情報
    private final ArrayList<ThumbnailGridAdapter.PictureNodeInfo> mData;
    private final float mDp;
    private final Context mContext;
    private final LayoutInflater mInflater;
    private int mPictureNumOnLine;            //1行で表示する写真の数


    /*
     * コンストラクタ
     */
    public ThumbnailGridAdapter(Context context, ArrayList<PictureNodeInfo> data, PictureTable showPicture){
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mShowPicture = showPicture;

        //画面密度
        mDp = context.getResources().getDisplayMetrics().density;

        //1行の写真表示数を設定
        setPictureNumOnLine();
    }

    /*
     * 表示写真の1辺の長さを設定
     */
    public void setPictureNumOnLine() {

        //画面向きを取得
        int orientation = mContext.getResources().getConfiguration().orientation;

        //向きに応じて、1行で表示する写真数を設定
        mPictureNumOnLine = ( (orientation == Configuration.ORIENTATION_PORTRAIT) ? PORTRAIT_NUM : LANDSCAPE_NUM );
    }

    @Override
    public int getCount() {
        //写真数
        return mData.size();
    }

    /*
     * セル一つ一つを描画する際にコールされる。
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //初めて表示されるなら、セルを割り当て。セルはレイアウトファイルを使用。
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_picture_node_info, null);

            //写真用ビューのサイズ
/*            int sideLength = (parent.getWidth() / mPictureNumOnLine) - (int)mDp;
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                    sideLength,
                    sideLength);
            convertView.setLayoutParams(params);*/

            //float dp = mContext.getResources().getDisplayMetrics().density;
            //Log.i("ギャラリー", "position=" + position + " 前回設定サイズ=" + (parent.getWidth() / 2 - (int)dp) );
            //Log.i("ギャラリー", "position=" + position + " mPictureNumOnLine=" + mPictureNumOnLine);
        }

        //ピクチャノード情報
        PictureNodeInfo pictureNodeInfo = mData.get(position);

        //path情報
        //サムネイルがなければ、読み込みエラーにさせる
        String path = ( (pictureNodeInfo.thumbnail == null) ? "" : pictureNodeInfo.thumbnail.getPath() );

        //サムネイルを設定
        ImageView iv_picture = convertView.findViewById( R.id.iv_picture );
        Picasso.get()
                .load( new File( path ) )
                .error(R.drawable.baseline_priority_high_black_24)
                .into( iv_picture );

        //親ノード名
        TextView tv_parentNode = convertView.findViewById(R.id.tv_parentNode);
        tv_parentNode.setText( pictureNodeInfo.parentNodeName );

        //クリックリスナー
        LinearLayout ll_pictureNodeInfo = convertView.findViewById( R.id.ll_pictureNodeInfo );
        ll_pictureNodeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //格納ノード移動処理
                    changeNode( pictureNodeInfo.pictureNodePid );
                }
            }
        );

        //設定したビューを返す
        return convertView;
    }


    /*
     * 格納先ノード移動処理
     */
    public void changeNode( int picutureNodePid ) {

        //確認する写真パス
        String path = mShowPicture.getPath();
        //ピクチャテーブルを参照し、写真があるかどうかチェック
        AsyncHasPicture db = new AsyncHasPicture(mContext, picutureNodePid, path, new AsyncHasPicture.OnFinishListener() {
            @Override
            public void onFinish( boolean hasPicture ) {

                if( hasPicture ){
                    //既に写真があれば、トースト表示して終了
                    Toast.makeText(mContext, "既にあり", Toast.LENGTH_SHORT).show();
                    return;
                }

                //なければ移動確認のダイアログを表示
                confirmDialog( picutureNodePid );
            }
        });

        db.execute();
    }

    /*
     * 移動確認用ダイアログ
     */
    public void confirmDialog( int picutureNodePid ) {

        new AlertDialog.Builder(mContext)
                .setTitle("格納先の確認")
                .setMessage("選択されたノードに写真を移動しますか？")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Log.i("格納先更新チェック", "前 親pid=" + mShowPicture.getPidParentNode());

                        //ピクチャテーブルの所属を更新
                        AsyncUpdateBelongsPicture db = new AsyncUpdateBelongsPicture(mContext, picutureNodePid, mShowPicture, new AsyncUpdateBelongsPicture.OnFinishListener() {
                            @Override
                            public void onFinish() {

                                Log.i("格納先更新チェック", "後 親pid=" + mShowPicture.getPidParentNode());

                                //移動した写真を単体表示中のアダプタから削除
                                ((SinglePictureDisplayActivity)mContext).updatePictureAdapter();
                            }
                        });
                        //非同期処理開始
                        db.execute();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    /*
     *
     */
    public void tmp() {



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
     * 表示するピクチャノード情報
     */
    public static class PictureNodeInfo {

        private final int pictureNodePid;
        private final PictureTable thumbnail;
        private final String parentNodeName;

        public PictureNodeInfo( int pid, PictureTable thumbnail, String parentNodeName ){
            this.pictureNodePid = pid;
            this.thumbnail = thumbnail;
            this.parentNodeName = parentNodeName;
        }
    }
}

