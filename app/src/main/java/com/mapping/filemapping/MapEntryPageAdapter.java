package com.mapping.filemapping;

import static android.content.Context.WINDOW_SERVICE;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class MapEntryPageAdapter extends RecyclerView.Adapter<MapEntryPageAdapter.GuideViewHolder> {

    //フィールド変数
    private final List<Integer>   mData;
    //
    private final View mfl_sampleMap;
    //FragmentManager
    private final FragmentManager mFragmentManager;
    //ViewPager2
    private final ViewPager2 mvp2;

    /*
     * ViewHolder：リスト内の各アイテムのレイアウトを含む View のラッパー
     */
    class GuideViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        //設定対象ノードビュー
        private final View mfl_sampleMap;
        //FragmentManager
        private final FragmentManager mFragmentManager;
        //ViewPager2
        private final ViewPager2      mvp2;

        /*--- マップ名 ---*/
        private EditText et_mapName;

        /*--- ノード数 ---*/
        private NumberPicker np_nodeNum ;

        /*--- カラーパターン ---*/
        private RecyclerView rv_colorPattern2;
        private RecyclerView rv_colorPattern3;

        /*
         * コンストラクタ
         */
        public GuideViewHolder(View itemView, int position, View fl_sampleMap, FragmentManager fragmentManager, ViewPager2 vp2) {
            super(itemView);

            mfl_sampleMap = fl_sampleMap;
            mFragmentManager = fragmentManager;
            mvp2 = vp2;

            if (position == 0) {
                //マップ名
                et_mapName = itemView.findViewById(R.id.et_mapName);

            } else if (position == 1) {
                //ノード数
                np_nodeNum = itemView.findViewById(R.id.np_nodeNum);

            } else if (position == 2) {
                //カラーパターン2色
                rv_colorPattern2 = itemView.findViewById(R.id.rv_colorPattern2);

            } else if (position == 3) {
                //カラーパターン3色
                rv_colorPattern3 = itemView.findViewById(R.id.rv_colorPattern3);

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
            } else if (position == 3) {
                setPage3();
            }
        }

        /*
         * ページ設定（０）
         */
        public void setPage0() {
            //文字入力リスナーを設定
            et_mapName.addTextChangedListener(this);
        }

        /*
         * ページ設定（１）
         */
        public void setPage1() {

            //サンプルノード数範囲
            final int NODE_NUM_MIN = 1;
            final int NODE_NUM_MAX = 5;

            // 最大、最小を設定
            np_nodeNum.setValue(NODE_NUM_MIN);
            np_nodeNum.setMinValue(NODE_NUM_MIN);
            np_nodeNum.setMaxValue(NODE_NUM_MAX);

            //リスナー：値変更時
            np_nodeNum.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    //マップへの反映処理
                }
            });

            // 値を取得
            //int val = np_nodeNum.getValue();
        }

        /*
         * ページ設定（２）
         */
        public void setPage2() {

            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern2.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_2_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern2.setLayoutManager(new GridLayoutManager(context, 2));
            rv_colorPattern2.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap ) );

            //レイアウト確定待ち処理
            ViewTreeObserver observer = rv_colorPattern2.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            //レイアウト確定後は、不要なので本リスナー削除
                            rv_colorPattern2.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                            Context context = rv_colorPattern2.getContext();

                            //画面横サイズ
                            int screenWidth;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                //
                                WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
                                WindowMetrics windowMetrics = windowManager.getCurrentWindowMetrics();

                                screenWidth = windowMetrics.getBounds().width();
                                Log.d("screenWidth=>>>", screenWidth + "");

                            } else {
                                DisplayMetrics displayMetrics = new DisplayMetrics();
                                ((Activity)context).getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
                                screenWidth = displayMetrics.widthPixels;
                            }





                        }
                    }
            );
        }

        /*
         * ページ設定（３）
         */
        public void setPage3() {

            //カラーパターンのタイトルリストを取得
            Context context = rv_colorPattern3.getContext();
            String[] colorPattern = context.getResources().getStringArray(R.array.color_3_pattern_list);

            //レイアウトマネージャの生成・設定
            rv_colorPattern3.setLayoutManager(new GridLayoutManager(context, 2));
            rv_colorPattern3.setAdapter( new ColorPatternAdapter( colorPattern, mfl_sampleMap ) );
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
            //サンプルマップ側へ設定
            ((SampleMapView)mfl_sampleMap).setMapName( editable.toString() );
        }

    }

    /*
     * コンストラクタ
     */
    public MapEntryPageAdapter(List<Integer> layoutIdList, View fl_sampleMap, FragmentManager fragmentManager, ViewPager2 vp) {
        mData            = layoutIdList;
        mfl_sampleMap    = fl_sampleMap;
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

        return new GuideViewHolder(view, position, mfl_sampleMap, mFragmentManager, mvp2);
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
