package com.mapping.filemapping;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.android.colorpicker.ColorPickerView;

import java.util.List;

public class NodeDesignAdapter extends RecyclerView.Adapter<NodeDesignAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //設定対象ノードビュー
    private final BaseNode        mv_node;
    //FragmentManager
    private final FragmentManager mFragmentManager;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    static class GuideViewHolder extends RecyclerView.ViewHolder {

        //設定対象ノードビュー
        private final BaseNode        mv_node;
        //FragmentManager
        private final FragmentManager mFragmentManager;

        //ノード名
        private EditText et_nodeName;

        //ノードデザイン
        private TextView tv_bgColorCode;
        private TextView tv_bgColorGraphic;
        private TextView tv_txColorCode;
        private TextView tv_txColorGraphic;

        //ラインデザイン


        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, BaseNode node, FragmentManager fragmentManager) {
            super(itemView);

            mv_node = node;
            mFragmentManager = fragmentManager;

            if (position == 0) {
                et_nodeName = itemView.findViewById(R.id.et_nodeName);

            } else if (position == 1) {
                //背景色
                tv_bgColorCode    = itemView.findViewById(R.id.tv_bgColorCode);
                tv_bgColorGraphic = itemView.findViewById(R.id.tv_bgColorGraphic);
                //テキスト色
                tv_txColorCode    = itemView.findViewById(R.id.tv_txColorCode);
                tv_txColorGraphic = itemView.findViewById(R.id.tv_txColorGraphic);

            } else if (position == 2) {

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

            }

        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {

        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //背景色-カラーコード
            tv_bgColorCode.setOnClickListener(new ClickColorCode(true) );
            //背景色-カラーピッカー
            tv_bgColorGraphic.setOnClickListener( new ClickColorPicker(true) );

            //テキスト色-カラーコード
            tv_txColorCode.setOnClickListener(new ClickColorCode(false) );
            //テキスト色-カラーピッカー
            tv_txColorGraphic.setOnClickListener( new ClickColorPicker(false) );
        }

        /*
         *
         * カラーコード表示リスナー
         *
         */
        private class ClickColorCode implements View.OnClickListener {

            private boolean mIsBg;

            /*
             * コンストラクタ
             */
            public ClickColorCode( boolean isBg ){
                mIsBg = isBg;
            }

            @Override
            public void onClick(View view) {

                //ダイアログを生成
                ColorCodeDialog dialog = new ColorCodeDialog();

                //OKボタンリスナー
                dialog.setOnPositiveClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.i("Design", "カラーコード=" + ((EditText)view).getText());

                        //カラーコード文字列
                        String code = "#" + ((EditText)view).getText().toString();

                        if( mIsBg ){
                            //背景色
                            mv_node.setNodeBackgroundColor( code );
                        } else {
                            //テキストカラー
                            mv_node.setNodeTextColor( code );
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show(mFragmentManager, "ColorCode");
            }
        }

        /*
         *
         * カラーピッカー表示リスナー
         *
         */
        private class ClickColorPicker implements View.OnClickListener {

            private boolean mIsBg;

            /*
             * コンストラクタ
             */
            public ClickColorPicker( boolean isBg ){
                mIsBg = isBg;
            }

            @Override
            public void onClick(View view) {

                //ダイアログを生成
                ColorPickerDialog dialog = new ColorPickerDialog();

                //OKボタンリスナー
                dialog.setOnPositiveClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //カラーコード文字列
                        String code = "#" + Integer.toHexString( ((ColorPickerView)view).getColor() );

                        if( mIsBg ){
                            //背景色
                            mv_node.setNodeBackgroundColor( code );
                        } else {
                            //テキストカラー
                            mv_node.setNodeTextColor( code );
                        }

                        dialog.dismiss();
                    }
                });

                dialog.show(mFragmentManager, "ColorGraphic");
            }
        }
    }

    /*
     * コンストラクタ
     */
    public NodeDesignAdapter(List<Integer> layoutIdList, BaseNode v_node, FragmentManager fragmentManager) {
        mData            = layoutIdList;
        mv_node          = v_node;
        mFragmentManager = fragmentManager;
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

        return new GuideViewHolder(view, position, mv_node, mFragmentManager);
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
