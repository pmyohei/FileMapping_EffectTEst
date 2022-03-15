package com.mapping.filemapping;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

/*
 * マップリストアダプタ
 */
public class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MapViewHolder> {

    /*
     * マップ表示リスナー
     */
    public interface openMapListener {
        void onOpenMap(MapTable map );
    }
    /*
     * マップ名編集リスナー
     */
    public interface editMapListener {
        void onEditMap(MapTable map, int index );
    }
    /*
     * マップ削除リスナー
     */
    public interface deleteMapListener {
        void onDeleteMap(MapTable map, int index );
    }

    //マップリスト
    private final ArrayList<MapTable> mData;
    //マップオープンリスナー
    private openMapListener mOpenMapListener;
    //編集リスナー
    private editMapListener mEditMapListener;
    //削除リスナー
    private deleteMapListener mDeleteMapListener;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class MapViewHolder extends RecyclerView.ViewHolder {

        private final TextView     tv_mapName;
        private final MaterialCardView mcv_map;
        private final ImageButton  ib_edit;
        private final ImageButton  ib_delete;

        /*
         * コンストラクタ
         */
        public MapViewHolder(View itemView) {
            super(itemView);

            mcv_map    = itemView.findViewById(R.id.mcv_map);
            tv_mapName = itemView.findViewById(R.id.tv_mapName);
            ib_edit    = itemView.findViewById(R.id.ib_edit);
            ib_delete  = itemView.findViewById(R.id.ib_delete);
        }

        /*
         * ビューの設定
         */
        public void setView( MapTable map, int index ){

            //お試し
/*            if( map.getFirstColor() != null ){
                mcv_map.setBackgroundColor( Color.parseColor(map.getFirstColor()) );
                tv_mapName.setTextColor( Color.parseColor(map.getSecondColor()) );
            }*/
            //--

            //マップ名
            tv_mapName.setText( map.getMapName() );

            //マップオープンリスナー
            mcv_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOpenMapListener.onOpenMap( map );
                }
            });

            //編集ボタンリスナー
            ib_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mEditMapListener.onEditMap( map, index );
                }
            });

            //削除ボタンリスナー
            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDeleteMapListener.onDeleteMap( map, index );
                }
            });

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
        View view = inflater.inflate(R.layout.item_map, viewGroup, false);

        return new MapViewHolder(view);
    }

    /*
     * ViewHolderの設定
     *   表示内容等の設定を行う
     */
    @Override
    public void onBindViewHolder(@NonNull MapViewHolder viewHolder, final int i) {

        //対象マップ情報
        MapTable map = mData.get(i);

        //ビューの設定
        viewHolder.setView( map, i );
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

    public void setOpenMapListener(openMapListener listener){
        mOpenMapListener = listener;
    }
    public void setEditMapListener(editMapListener listener){
        mEditMapListener = listener;
    }
    public void setDeleteMapListener(deleteMapListener listener){
        mDeleteMapListener = listener;
    }


}
