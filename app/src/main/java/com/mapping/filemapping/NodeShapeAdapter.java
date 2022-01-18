package com.mapping.filemapping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class NodeShapeAdapter extends RecyclerView.Adapter<NodeShapeAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>     mData;
    //マップ
    private BaseNode                mv_node;
    //FragmentManager
    private final FragmentManager   mFragmentManager;
    //ViewPager2
    private final ViewPager2        mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class GuideViewHolder extends RecyclerView.ViewHolder {

        //カラー指定
        private final int CIRCLE = 0;
        private final int SQUARE = 1;

        //マップ
        private final View mv_node;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        //ノードの形状
        private ImageView iv_circle;
        private ImageView iv_square;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, View v_map, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mv_node = v_map;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            if (position == 0) {
                //サイズ
                iv_circle = itemView.findViewById(R.id.iv_circle);
                iv_square = itemView.findViewById(R.id.iv_square);
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            if (position == 0) {
                setPage0();
            }

        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {

            //円形
            iv_circle.setOnClickListener(new ClickShapeImage(CIRCLE) );
            //四角形
            iv_square.setOnClickListener( new ClickShapeImage(SQUARE) );
        }

        /*
         *
         * ノード形状イメージリスナー
         *
         */
        private class ClickShapeImage implements View.OnClickListener {

            private final int mShapeKind;

            /*
             * コンストラクタ
             */
            public ClickShapeImage(int kind ){
                mShapeKind = kind;
            }

            @Override
            public void onClick(View view) {

                //ノードに対して、形状を適用

            }
        }
    }

    /*
     * コンストラクタ
     */
    public NodeShapeAdapter(List<Integer> layoutIdList, BaseNode v_node, FragmentManager fragmentManager, ViewPager2 vp2) {
        mData            = layoutIdList;
        mv_node           = v_node;
        mFragmentManager = fragmentManager;
        mvp2             = vp2;
    }

    /*
     * ここで返した値が、onCreateViewHolder()の第２引数になる
     */
    @Override
    public int getItemViewType(int position) {
        //レイアウトIDを返す
        return position;
        //return mData.get(position);
    }

    /*
     *　ViewHolderの生成
     */
    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        //レイアウトを生成
        LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
        View view = inflater.inflate(mData.get(position), viewGroup, false);

        return new GuideViewHolder(view, position, mv_node, mFragmentManager, mvp2);
    }

    /*
     * ViewHolderの設定
     */
    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder viewHolder, final int i) {

        //ページ設定
        viewHolder.setPage( i );

    }

    /*
     * 表示データ数の取得
     */
    @Override
    public int getItemCount() {
        //ページ数
        return mData.size();
    }


}
