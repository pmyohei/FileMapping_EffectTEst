package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class DesignPicturePageAdapter extends RecyclerView.Adapter<DesignPicturePageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //設定対象ノードビュー
    private final BaseNode mv_node;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder {

        //設定対象ノードビュー
        private final BaseNode mv_node;

        /*--- サムネイル写真の変更 ---*/
        private MaterialCardView mcv_thumbnail;
        private TextView tv_contents;

        /*--- ノードデザイン ---*/
        private ImageView iv_circle;
        private ImageView iv_circleLittle;
        private ImageView iv_squareRounded;
        private ImageView iv_square;
        private ImageView iv_octagon;
        private ImageView iv_octagonRounded;
        private ImageView iv_dia;
        private ImageView iv_diaSemi;
        private SeekbarView  sbv_nodeSize;
        private ColorSelectionView csv_border;
        private SeekbarView  sbv_borderSize;
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        private Switch sw_shadow;
        private ColorSelectionView csv_shadow;

        /*--- ライン ---*/
        private ColorSelectionView csv_line;
        private SeekbarView  sbv_lineSize;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node ) {
            super(itemView);

            mv_node = node;

            switch (position) {
                case 0:
                    //サムネイルの変更
                    mcv_thumbnail = itemView.findViewById(R.id.mcv_thumbnail);
                    tv_contents = itemView.findViewById(R.id.tv_contents);

                    break;

                case 1:
                    //ノードサイズ
                    sbv_nodeSize = itemView.findViewById(R.id.sbv_nodeSize);
                    //ラインサイズ
                    sbv_lineSize = itemView.findViewById(R.id.sbv_lineSize);
                    //枠線サイズ
                    sbv_borderSize = itemView.findViewById(R.id.sbv_borderSize);
                    break;

                case 2:
                    //ノード形
                    iv_circle = itemView.findViewById(R.id.iv_circle);
                    iv_circleLittle = itemView.findViewById(R.id.iv_circleLittle);
                    iv_squareRounded = itemView.findViewById(R.id.iv_squareRounded);
                    iv_square = itemView.findViewById(R.id.iv_square);
                    iv_octagon = itemView.findViewById(R.id.iv_octagon);
                    iv_octagonRounded = itemView.findViewById(R.id.iv_octagonRounded);
                    iv_dia = itemView.findViewById(R.id.iv_dia);
                    iv_diaSemi = itemView.findViewById(R.id.iv_diaSemi);
                    break;

                case 3:
                    //枠線色
                    csv_border = itemView.findViewById(R.id.csv_border);
                    break;

                case 4:
                    //影色
                    sw_shadow = itemView.findViewById(R.id.sw_shadow);
                    csv_shadow = itemView.findViewById(R.id.csv_shadow);
                    break;

                case 5:
                    //ラインの色
                    csv_line = itemView.findViewById(R.id.csv_line);
                    break;
            }
        }

        /*
         * 各種ページ設定
         */
        private void setPage(int position) {

            switch (position){
                case 0:
                    setPage0();
                    break;
                case 1:
                    setPage1();
                    break;
                case 2:
                    setPage2();
                    break;
                case 3:
                    setPage3();
                    break;
                case 4:
                    setPage4();
                    break;
                case 5:
                    setPage5();
                    break;
            }
        }

        /*
         * ページ設定（０）
         */
        private void setPage0() {

            //ノード生成
            mcv_thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //トリミング画面を開く
                    Context context = view.getContext();
                    Intent intent = new Intent(context, TrimmingActivity.class);
                    intent.putExtra(MapActivity.INTENT_MAP_PID, mv_node.getNode().getPidMap());
                    intent.putExtra(MapActivity.INTENT_NODE_PID, mv_node.getNode().getPid());
                    intent.putExtra(MapActivity.INTENT_EDIT, true);

                    //開始
                    //※上記のcontextではキャスト不可でエラー
                    ((MapActivity)tv_contents.getContext()).getTrimmingLauncher().launch(intent);
                }
            });
        }

        /*
         * ページ設定（１）
         */
        private void setPage1() {
            //ノードサイズのシークバー
            sbv_nodeSize.setNodeSizeSeekbar( mv_node );
            //枠サイズのシークバー
            sbv_borderSize.setBorderSizeSeekbar( mv_node );
            //ラインサイズ
            sbv_lineSize.setLineSizeSeekbar( mv_node );
        }

        /*
         * ページ設定（２）
         */
        private void setPage2() {
            //ノード形
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
         * ページ設定（３）
         */
        private void setPage3() {
            //枠色
            csv_border.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BORDER, mv_node );
        }

        /*
         * ページ設定（４）
         */
        private void setPage4() {
            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_SHADOW, mv_node );

            //影のon/off
            sw_shadow.setChecked( mv_node.isShadow() );
            sw_shadow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    //影の状態を反転
                    mv_node.switchShadow();
                }
            });
        }

        /*
         * ページ設定（５）
         */
        private void setPage5() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_LINE, mv_node );
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
                //ノードに形状を設定
                mv_node.setNodeShape( mShapeKind );
            }
        }
    }

    /*
     * コンストラクタ
     */
    public DesignPicturePageAdapter(List<Integer> layoutIdList, View v_node ) {
        mData = layoutIdList;
        mv_node = (BaseNode)v_node;
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

        return new GuideViewHolder(view, position, mv_node);
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
