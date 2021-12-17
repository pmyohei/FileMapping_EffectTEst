package com.mapping.filemapping;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MapListAdapter_cutum_old extends RecyclerView.Adapter<MapListAdapter_cutum_old.MapViewHolder> {

    //マップリスト
    private ArrayList<MapTable> mData;

    private final Context mContext;

    ActivityResultLauncher<Intent> mStartCreateMapForResult;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class MapViewHolder extends RecyclerView.ViewHolder {

        private TextView     tv_mapName;

        /*
         * コンストラクタ
         */
        public MapViewHolder(View itemView) {
            super(itemView);

            tv_mapName = (TextView)itemView.findViewById(R.id.tv_mapName);
        }



    }

    /*
     * コンストラクタ
     */
    public MapListAdapter_cutum_old(Context context, ArrayList<MapTable> data, ActivityResultLauncher<Intent> startCreateMapForResult ) {
        mContext = context;
        mData = data;

        mStartCreateMapForResult = startCreateMapForResult;
    }

    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {

        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.map_item, viewGroup, false);
        //View view = new MapItemView( mContext, mData.get(viewType), this );

        return new MapViewHolder(view);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull MapViewHolder viewHolder, final int i) {

        MapTable map = mData.get(i);

        //マップ名
        viewHolder.tv_mapName.setText( map.getMapName() );
    }

    /*
     * データ数取得
     */
    @Override
    public int getItemCount() {
        //表示データ数を返す
        return mData.size();
        //return 0;
    }

    /*
     * マップ編集画面遷移ランチャーを取得
     */
    public ActivityResultLauncher<Intent> getEditLauncher(){
        return mStartCreateMapForResult;
    }

    /*
     * マップ削除
     *   リスト内からマップを削除し、自身に削除通知を送る
     */
    public void deleteMap( int mapPid ){

        int i = 0;
        for( MapTable map: mData ){
            Log.i("deleteMap", "map.id=" + map.getPid() + " target-mapPid=" + mapPid);

            if( map.getPid() == mapPid ){

                Log.i("deleteMap", "map=" + map.getMapName());

                //リストから指定マップを削除
                mData.remove(i);

                //自身に削除通知を送る
                notifyItemRemoved( i );
                break;
            }
            i++;
        }
    }

}
