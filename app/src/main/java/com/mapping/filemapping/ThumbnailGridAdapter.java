package com.mapping.filemapping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

/*
 * サムネイルをグリッド表示するアダプタ
 */
public class ThumbnailGridAdapter extends BaseAdapter {

    //マップ上のピクチャノード情報
    private final ArrayList<PictureNodesDialog.PictureNodeInfo> mData;
    private final Context mContext;
    private final LayoutInflater mInflater;


    /*
     * コンストラクタ
     */
    public ThumbnailGridAdapter(Context context, ArrayList<PictureNodesDialog.PictureNodeInfo> data){
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
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
        }

        //ピクチャノード情報
        PictureNodesDialog.PictureNodeInfo pictureNodeInfo = mData.get(position);

        //ピクチャノードサイズ
        int viewSize = (int)mContext.getResources().getDimension(R.dimen.gallery_tab_size);

        ImageView iv_picture = convertView.findViewById( R.id.iv_picture );

        //サムネイルを割り当て
        //※画質を担保するため、resize()である程度画像の大きさを確保してからtransform()に渡す
        PictureTable thumbnail = pictureNodeInfo.getThumbnail();
        //path
        String path = ( (thumbnail == null) ? "": thumbnail.getPath() );
        Picasso.get()
                .load( new File( path ) )
                .resize( ThumbnailTransformation.RESIZE, ThumbnailTransformation.RESIZE )
                .transform( new ThumbnailTransformation( thumbnail, viewSize ) )
                .error(R.drawable.baseline_no_image)
                .into( iv_picture );

        //親ノード名を取得。なければ、なし用の文言を設定
        String parentNodeName = pictureNodeInfo.getParentNodeName();
        String setName = (parentNodeName.isEmpty()) ? mContext.getString(R.string.no_nodeName): parentNodeName ;

        TextView tv_parentNode = convertView.findViewById(R.id.tv_parentNode);
        tv_parentNode.setText( setName );

        //設定したビューを返す
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
}

