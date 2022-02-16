package com.mapping.filemapping;

import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GalleryPageAdapter extends RecyclerView.Adapter<GalleryPageAdapter.ViewHolder> {

    //レイアウトIDリスト
    private final List<Integer> mLayoutIds;
    //各ページのギャラリー
    private final List<PictureArrayList<PictureTable>> mGallerys;
    //複数選択状態
    private boolean mIsMultipleSelection;
    //選択中の写真Indexリスト
    private List<Integer> mSelectedList;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        //ギャラリー表示用のGridView
        private final GridView gv_gallery;

        /*
         * コンストラクタ
         */
        public ViewHolder(View itemView) {
            super(itemView);

            //画像を表示
            gv_gallery = itemView.findViewById(R.id.gv_gallery);
        }

        /*
         * ページ設定
         */
        private void setPage(int position) {
            //画面向きを取得
            int orientation = gv_gallery.getContext().getResources().getConfiguration().orientation;

            //1行に表示する写真数を設定
            int pictureNumOnLine = ((orientation == Configuration.ORIENTATION_PORTRAIT) ? GalleryAdapter.PORTRAIT_NUM : GalleryAdapter.LANDSCAPE_NUM);
            gv_gallery.setNumColumns(pictureNumOnLine);

            Log.i("タブ写真", "setPage ページ=" + position);

            //ギャラリーアダプタの設定
            setGaleryAdapter( position );
        }


        /*
         * ギャラリーアダプタの設定
         */
        private void setGaleryAdapter( int pagePosition ) {

            Log.i("タブ写真", "setGaleryAdapter ページ=" + pagePosition);

            //アダプタを設定
            gv_gallery.setAdapter(new GalleryAdapter(gv_gallery.getContext(), mGallerys.get(pagePosition)));

            //アイテムクリックリスナー
            gv_gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    //複数選択モードでなければ
                    if( gv_gallery.getChoiceMode() == GridView.CHOICE_MODE_NONE ){
                        //ギャラリーの単体表示へ
                        transitionScreen(pagePosition, i);
                    }

                    /*--- 複数選択モードの場合、クリック時に自動でCheckable処理が入るため、本リスナー内では何もしない ---*/




/*                    //複数選択状態かどうか
                    if (mIsMultipleSelection) {
                        //選択状態の設定
                        //updateSelectedState(i);
                    } else {
                        //画面遷移
                        transitionScreen(pagePosition, i);
                    }*/
                }
            });

            //アイテムロングクリックリスナー
            gv_gallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    if( gv_gallery.getChoiceMode() == GridView.CHOICE_MODE_NONE ){
                        //複数選択モード出なければ、複数選択モードへ移行
                        //※この設定をすることで、クリックするとチェック状態が設定される
                        gv_gallery.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE);

                        //ロングクリックの場合、選択判定にならないため、手動で設定
                        gv_gallery.setItemChecked( i, true );

                        //ツールバーにメニューを表示
                        PictureGalleryActivity activity = (PictureGalleryActivity)gv_gallery.getContext();
                        activity.setMultipleOptionMenu(true);
                    }

                    return true;
                }
            });
        }

        /*
         * 画面遷移
         */
        public void transitionScreen(int pagePosition, int position) {

            PictureGalleryActivity activity = (PictureGalleryActivity)gv_gallery.getContext();

            //渡す情報を設定
            Intent intent = new Intent( activity.getApplication(), SinglePictureDisplayActivity.class );
            intent.putExtra( "pictures", mGallerys.get(pagePosition) );             //表示する写真リスト
            intent.putExtra( "position", position );                                //表示開始写真位置

            //ランチャーを使用して画面開始
            activity.getSinglePictureLauncher().launch(intent);
        }

        /*
         * 選択中状態の初期化
         */
        public void initSelectedState(int position) {

            boolean isSelected = false;

            //選択中リストにあるかチェック
            for( Integer index: mSelectedList ){
                if( position == index ){
                    //あれば選択中にする
                    isSelected = true;
                    break;
                }
            }

            //選択状態を設定
            gv_gallery.setItemChecked( position, isSelected);
        }

        /*
         * 選択中状態の更新
         */
        public void updateSelectedState(int position) {

            //選択中の場合
            if( gv_gallery.isItemChecked( position ) ){
                int i = 0;
                for( Integer queData: mSelectedList ){
                    if( queData == position ){
                        //選択中リストから削除
                        mSelectedList.remove( i );
                        //ビューの状態を更新
                        //gv_gallery.setItemChecked( position, false );

                        //※リストループ中で削除しているため、ここで処理を終了
                        //※（次のループにいくとおちる）
                        return;
                    }
                    i++;
                }

            } else {
                //選択中リストに追加
                mSelectedList.add( position );

                //ビューの状態を更新
                //gv_gallery.setItemChecked( position, true );
            }

/*            if( pictureInGalleryView.isChecked() ){
                int i = 0;
                for( Integer queData: mSelectedList ){
                    if( queData == position ){
                        //選択中リストから削除
                        mSelectedList.remove( i );
                        //ビューの状態を更新
                        pictureInGalleryView.toggle();

                        //※リストループ中で削除しているため、ここで処理を終了
                        //※（次のループにいくとおちる）
                        return;
                    }
                    i++;
                }

            } else {
                //選択中リストに追加
                mSelectedList.add( position );

                //ビューの状態を更新
                pictureInGalleryView.toggle();
            }*/
        }

    }

    /*
     * コンストラクタ
     */
    public GalleryPageAdapter(List<Integer> layoutIdList, List<PictureArrayList<PictureTable>> gallery) {
        mLayoutIds = layoutIdList;
        mGallerys = gallery;

        //複数選択状態
        mIsMultipleSelection = false;
        //選択中リスト初期化
        mSelectedList = new ArrayList<>();
    }

    /*
     * ここで返した値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //レイアウトIDを返す
        return position;
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        //レイアウトを生成
        LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
        View view = inflater.inflate(mLayoutIds.get(position), viewGroup, false);

        Log.i("タブ写真", "onCreateViewHolder ページ=" + position);

        return new ViewHolder(view);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {

        Log.i("タブ写真", "onBindViewHolder ページ=" + i);

        //ページ設定
        viewHolder.setPage( i );
    }

    /*
     * 表示データ数の取得
     */
    @Override
    public int getItemCount() {
        //ページ数
        return mLayoutIds.size();
    }

    /*
     * 複数選択状態の解除
     */
    public void cancellationMultipleSelection(GridView gv_gallery) {
        //mIsMultipleSelection = false;
        //mSelectedList.clear();

        //表示中GridViewの複数選択モードを解除
        gv_gallery.setChoiceMode( GridView.CHOICE_MODE_NONE );
    }


}
