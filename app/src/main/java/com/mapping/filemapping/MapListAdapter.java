package com.mapping.filemapping;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MapListAdapter extends RecyclerView.Adapter<MapListAdapter.MapViewHolder> {

    //マップリスト
    private final ArrayList<MapTable> mData;

    //編集画面遷移ランチャー
    ActivityResultLauncher<Intent> mEditMapLauncher;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     * (固有のためインナークラスで定義)
     */
    class MapViewHolder extends RecyclerView.ViewHolder {

        private final TextView     tv_mapName;
        private final ImageButton  ib_map;
        private final ImageButton  ib_edit;
        private final ImageButton  ib_delete;

        /*
         * コンストラクタ
         */
        public MapViewHolder(View itemView) {
            super(itemView);

            tv_mapName = itemView.findViewById(R.id.tv_mapName);
            ib_map     = itemView.findViewById(R.id.ib_map);
            ib_edit    = itemView.findViewById(R.id.ib_edit);
            ib_delete  = itemView.findViewById(R.id.ib_delete);
        }

        /*
         * ビューの設定
         */
        public void setView( MapTable map ){

            //マップ名
            tv_mapName.setText( map.getMapName() );

            //マップオープンボタンリスナー
            ib_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Context context = view.getContext();

                    //マップ画面へ遷移
                    Intent intent = new Intent(context, MapActivity.class);
                    intent.putExtra(MapListActivity.KEY_MAP, map);

                    context.startActivity(intent);
                }
            });

            //編集ボタンリスナー
            ib_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //画面遷移
                    Intent intent = new Intent(view.getContext(), MapEntryActivity.class);
                    intent.putExtra(MapListActivity.KEY_ISCREATE, false );
                    intent.putExtra(MapListActivity.KEY_MAP, map );

                    Log.i("MapItemView", "map=" + map.getMapName());

                    mEditMapLauncher.launch( intent );
                }
            });

            //削除ボタンリスナー
            ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Context context = view.getContext();

                    //削除確認ダイアログを表示
                    new AlertDialog.Builder(context)
                            .setTitle("マップ削除確認")
                            .setMessage("マップを削除します。\nなお、マップ内の写真は端末上からは削除されません。")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //DBから自マップ削除
                                    AsyncDeleteMap db = new AsyncDeleteMap(context, map, new AsyncDeleteMap.OnFinishListener() {
                                        @Override
                                        public void onFinish() {
                                            //アダプタへ自身を削除させる
                                            Log.i("deleteMap", "mMap.getMapName()=" + map.getMapName());
                                            deleteMap(map.getPid() );
                                        }
                                    });
                                    //非同期処理開始
                                    db.execute();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });

        }
    }

    /*
     * コンストラクタ
     */
    public MapListAdapter( ArrayList<MapTable> data, ActivityResultLauncher<Intent> startCreateMapForResult ) {
        mData = data;

        mEditMapLauncher = startCreateMapForResult;
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
        viewHolder.setView( map );
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
        return mEditMapLauncher;
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
                break;
            }
            i++;
        }

        //リストから指定マップを削除
        mData.remove(i);

        //自身に削除通知を送る
        notifyItemRemoved(i);
    }

}
