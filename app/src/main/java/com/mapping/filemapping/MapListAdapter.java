package com.mapping.filemapping;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MapViewHolder> {

    //マップリスト
    private ArrayList<MapTable> mData;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class MapViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout cl_mapItem;
        private TextView         tv_mapName;

        /*
         * コンストラクタ
         */
        public MapViewHolder(View itemView) {
            super(itemView);

            cl_mapItem = (ConstraintLayout)itemView.findViewById(R.id.cl_mapItem);
            tv_mapName = (TextView)itemView.findViewById(R.id.tv_mapName);

        }
    }

    /*
     * コンストラクタ
     */
    public MapListAdapter( ArrayList<MapTable> data ) {
        mData = data;
    }

    /*
     * ここの戻り値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {

        return 0;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public MapViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        //ビューを生成
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_map, viewGroup, false);

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

        //クリックリスナー
        viewHolder.cl_mapItem.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context context = view.getContext();

                //マップ画面へ遷移
                Intent intent = new Intent(context, MapActivity.class);
                intent.putExtra(ResourceManager.INTENT_ID_MAPLIST_TO_MAP, map.getPid());

                context.startActivity(intent);
            }
        });

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

}
