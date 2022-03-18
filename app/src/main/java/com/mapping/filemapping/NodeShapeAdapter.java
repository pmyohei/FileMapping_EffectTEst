package com.mapping.filemapping;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.List;

/*
 * ノードの形状のみ
 */
public class NodeShapeAdapter extends RecyclerView.Adapter<NodeShapeAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer> mData;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class GuideViewHolder extends RecyclerView.ViewHolder {

        //ノードの形状
        private ImageView iv_circle;
        private ImageView iv_circleLittle;
        private ImageView iv_squareRounded;
        private ImageView iv_square;
        private ImageView iv_octagon;
        private ImageView iv_octagonRounded;
        private ImageView iv_dia;
        private ImageView iv_diaSemi;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position ) {
            super(itemView);

            if (position == 0) {
                //サイズ
                iv_circle = itemView.findViewById(R.id.iv_circle);
                iv_circleLittle = itemView.findViewById(R.id.iv_circleLittle);
                iv_squareRounded = itemView.findViewById(R.id.iv_squareRounded);
                iv_square = itemView.findViewById(R.id.iv_square);
                iv_octagon = itemView.findViewById(R.id.iv_octagon);
                iv_octagonRounded = itemView.findViewById(R.id.iv_octagonRounded);
                iv_dia = itemView.findViewById(R.id.iv_dia);
                iv_diaSemi = itemView.findViewById(R.id.iv_diaSemi);
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
            //形状
            iv_circle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE) );
            iv_circleLittle.setOnClickListener( new ClickShapeImage(NodeTable.CIRCLE_LITTLE) );
            iv_squareRounded.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE_ROUNDED) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );
            iv_octagon.setOnClickListener( new ClickShapeImage(NodeTable.OCTAGON) );
            iv_octagonRounded.setOnClickListener( new ClickShapeImage(NodeTable.OCTAGON_ROUNDED) );
            iv_dia.setOnClickListener( new ClickShapeImage(NodeTable.DIA) );
            iv_diaSemi.setOnClickListener( new ClickShapeImage(NodeTable.DIA_SEMI) );
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
                //形状を保持させる
                ((TrimmingActivity)view.getContext()).setThumbnailShape( mShapeKind );
            }
        }
    }

    /*
     * コンストラクタ
     */
    public NodeShapeAdapter(List<Integer> layoutIdList) {
        mData = layoutIdList;
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
    public GuideViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {

        //レイアウトを生成
        LayoutInflater inflater = LayoutInflater.from( viewGroup.getContext() );
        View view = inflater.inflate(mData.get(position), viewGroup, false);

        return new GuideViewHolder(view, position);
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
