package com.mapping.filemapping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DesignPicturePageAdapter extends RecyclerView.Adapter<DesignPicturePageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //設定対象ノードビュー
    private final BaseNode        mv_node;
    //FragmentManager
    private final FragmentManager mFragmentManager;
    //ViewPager2
    private final ViewPager2 mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //設定対象ノードビュー
        private final BaseNode        mv_node;
        //ViewPager2
        private final ViewPager2      mvp2;

        /*--- サムネイル写真の変更 ---*/
        private ImageView iv_thumbnail;

        /*--- ノードデザイン ---*/
        private ImageView iv_circle;
        private ImageView iv_square;
        private SeekbarView  sbv_nodeSize;
        private ColorSelectionView csv_border;
        private SeekbarView  sbv_borderSize;
        private ColorSelectionView csv_shadow;

        /*--- ライン ---*/
        private ColorSelectionView csv_line;
        private SeekbarView  sbv_lineSize;


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node , ViewPager2 vp2) {
            super(itemView);

            mv_node = node;
            mvp2 = vp2;

            if (position == 0) {
                //サムネイルの変更
                iv_thumbnail    = itemView.findViewById(R.id.iv_thumbnail);

            } else if (position == 1) {
                //ノード形
                iv_circle    = itemView.findViewById(R.id.iv_circle);
                iv_square    = itemView.findViewById(R.id.iv_square);
                //ノードサイズ
                sbv_nodeSize       = itemView.findViewById(R.id.sbv_nodeSize);
                //枠線色
                csv_border = itemView.findViewById(R.id.csv_border);
                //枠線サイズ
                sbv_borderSize       = itemView.findViewById(R.id.sbv_borderSize);
                //影色
                csv_shadow = itemView.findViewById(R.id.csv_shadow);

            } else if (position == 2) {
                //色
                csv_line = itemView.findViewById(R.id.csv_line);
                //サイズ
                sbv_lineSize       = itemView.findViewById(R.id.sbv_lineSize);
            }
        }

        /*
         * 各種ページ設定
         */
        public void setPage(int position) {

            if (position == 0) {
                setPage0();

            } else if (position == 1) {
                setPage1();

            } else if (position == 2) {
                setPage2();
            }
        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {

            //ノード生成
            iv_thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //トリミング画面を開く
                    Context context = view.getContext();
                    Intent intent = new Intent(context, PictureTrimmingActivity.class);
                    intent.putExtra(MapActivity.INTENT_MAP_PID, mv_node.getNode().getPidMap());
                    intent.putExtra(MapActivity.INTENT_NODE_PID, mv_node.getNode().getPid());
                    intent.putExtra(MapActivity.INTENT_EDIT, true);

                    //開始
                    ((MapActivity)context).getTrimmingLauncher().launch(intent);
                }
            });

        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //ノード形
            iv_circle.setOnClickListener(new ClickShapeImage(NodeTable.CIRCLE) );
            iv_square.setOnClickListener( new ClickShapeImage(NodeTable.SQUARE) );

            //ノードサイズのシークバー
            sbv_nodeSize.setNodeSizeSeekbar( mv_node );

            //枠色
            csv_border.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_BORDER, mv_node );
            //枠サイズのシークバー
            sbv_borderSize.setBorderSizeSeekbar( mv_node );

            //影色
            csv_shadow.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_SHADOW, mv_node );
        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {
            //ラインカラー
            csv_line.setOnColorListener( ColorSelectionView.NODE, ColorSelectionView.COLOR_LINE, mv_node );
            //ラインサイズ
            sbv_lineSize.setLineSizeSeekbar( mv_node );
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //ノードに反映
            mv_node.setNodeName( charSequence.toString() );
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //ノードに反映
            mv_node.setNodeName( editable.toString() );
            mv_node.addOnNodeGlobalLayoutListener();
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
    public DesignPicturePageAdapter(List<Integer> layoutIdList, View v_node, FragmentManager fragmentManager, ViewPager2 vp) {
        mData            = layoutIdList;
        mv_node          =  (BaseNode)v_node;
        mFragmentManager = fragmentManager;
        mvp2             = vp;
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

        return new GuideViewHolder(view, position, mv_node, mvp2);
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
