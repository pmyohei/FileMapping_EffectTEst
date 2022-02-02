package com.mapping.filemapping;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SeekbarView extends LinearLayout {

    //デザインレイアウト種別
    public static final int MAP = 0;
    public static final int NODE = 1;

    //サイズ変更対象
    public static final int SIZE_NODE = 0;
    public static final int SIZE_BORDER = 1;
    public static final int SIZE_LINE = 2;

    private int  mViewKind;       //ビュー種別（ノードorマップ(全ノード)）
    private int  mPart;           //サイズ変更個所
    private View mSetView;        //設定対象ビュー（ノードorマップ）

    /*
     * コンストラクタ
     */
    public SeekbarView(Context context) {
        this(context, null);
    }

    public SeekbarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekbarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    /*
     *
     */
    private void init( Context context ) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.size_seekbar, this, true);

    }

    /*
     * シークバー設定：ノードサイズ
     */
    public void setNodeSizeSeekbar(BaseNode node) {

        //ノードサイズの最大・最小値
        int maxSize;
        int minSize;
        if( node.getNode().getKind() == NodeTable.NODE_KIND_PICTURE ){
            maxSize = NodeTable.PICTURE_MAX_SIZE;
            minSize = NodeTable.PICTURE_MIN_SIZE;
        } else {
            maxSize = NodeTable.NODE_MAX_SIZE;
            minSize = NodeTable.NODE_MIN_SIZE;
        }

        //現在の位置
        float scaleSize = node.getScaleWidth();
        int progress    = (int)((scaleSize / maxSize) * 100f);

        Log.i("sb_nodeSize", "scaleSize=" + scaleSize + " progress=" + progress);

        //進捗バー最大値
        final int PROGRESS_MAX = 100;
        //増分
        int add = (maxSize - minSize) / PROGRESS_MAX;

        //ノードサイズリスナー
        SeekBar sb_size = findViewById(R.id.sb_size);
        sb_size.setMax( PROGRESS_MAX );
        sb_size.setProgress(progress);
        sb_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekbar){}
            @Override
            public void onStartTrackingTouch(SeekBar seekbar){}
            @Override
            public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

                //設定比率を計算
                float setSize = minSize + ( i * add);
                float setRatio = setSize / node.getWidth();

                node.setScale( setRatio );

                //Log.i("sb_nodeSize", "設定比率=" + setRatio + " 目標サイズ=" + setSize);
                //Log.i("sb_nodeSize", "結果サイズ=" + node.getScaleWidth());
            }
        });
    }

    /*
     * シークバー設定：ラインの太さ
     *   0 ~ 99 ： 0.1 ~ 10.0
     */
    public void setLineSizeSeekbar(BaseNode node ) {

        //進捗バー最大値
        final int PROGRESS_MAX = 99;
        //進捗バーの10分の1の値をラインの太さに設定する
        final int RATIO = 10;

        //シークバー
        SeekBar sb_size = findViewById(R.id.sb_size);

        //現在の値
        final int INIT_SIZE = 5;
        int progress = ( (node == null) ? INIT_SIZE : (int)((ChildNode)node).getLineSize() * RATIO );

        sb_size.setMax( PROGRESS_MAX );
        sb_size.setProgress( progress );
        sb_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekbar){}
            @Override
            public void onStartTrackingTouch(SeekBar seekbar){}
            @Override
            public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

                //設定するラインの幅
                float thick = (float)( i + 1 ) / RATIO;

                if( node == null ){
                    //全ノード

                    //マップ共通データ
                    MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
                    NodeArrayList<NodeTable> nodes = commonData.getNodes();
                    nodes.setAllNodeLineSize( thick );

                } else {
                    //ノード単体
                    ((ChildNode) node).setLineSize( thick );
                }

                //Log.i("sb_nodeSize", "設定比率=" + setRatio + " 目標サイズ=" + setSize);
                Log.i("seekbar", "ライン太さ=" + thick);
            }
        });
    }

    /*
     * シークバー設定：枠の太さ
     *   0 ~ 10 ： 0 ~ 10
     */
    public void setBorderSizeSeekbar(BaseNode node ) {

        //進捗バー最大値
        final int PROGRESS_MAX = 10;

        //シークバー
        SeekBar sb_size = findViewById(R.id.sb_size);

        //現在の値
        final int INIT_SIZE = 5;
        int progress = ( (node == null) ? INIT_SIZE : node.getBorderSize() );

        sb_size.setMax( PROGRESS_MAX );
        sb_size.setProgress( progress );
        sb_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekbar){}
            @Override
            public void onStartTrackingTouch(SeekBar seekbar){}
            @Override
            public void onProgressChanged(SeekBar seekbar, int i, boolean flag) {

                if( node == null ){
                    //全ノード

                    //マップ共通データ
                    MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
                    NodeArrayList<NodeTable> nodes = commonData.getNodes();
                    nodes.setAllNodeBorderSize( i );

                } else {
                    //ノード単体

                    //シークバーの値をそのまま設定
                    node.setBorderSize( i );
                }

                //Log.i("sb_nodeSize", "設定比率=" + setRatio + " 目標サイズ=" + setSize);
                Log.i("seekbar", "ノード枠太さ=" + i);
            }
        });
    }

    /*
     * マップ全体に色を設定
     */
    private void setMapColor( String code ){

        //マップ共通データ
        MapCommonData commonData = (MapCommonData)((Activity)getContext()).getApplication();
        NodeArrayList<NodeTable> nodes = commonData.getNodes();

/*        switch (mPart){

            case COLOR_MAP:
                //マップ色
                mSetView.setBackgroundColor( Color.parseColor(code) );
                break;

            case COLOR_BACKGROUNG:
                //ノード背景色
                nodes.setAllNodeBgColor( code );
                break;

            case COLOR_TEXT:
                //ノードテキストカラー
                nodes.setAllNodeTxColor( code );
                break;

            case COLOR_BORDER:
                //枠線カラー
                nodes.setAllNodeBorderColor( code );
                break;

            case COLOR_SHADOW:
                //影カラー
                nodes.setAllNodeShadowColor( code );
                break;

            case COLOR_LINE:
                //ラインカラー
                nodes.setAllNodeLineColor( code );
                break;
        }*/

    }

    /*
     * ノード単体に色を設定
     */
    private void setNodeColor( String code ){

        //ノードにキャスト
        BaseNode node = (BaseNode) mSetView;

/*        //色設定の対象毎に処理
        switch (mPart) {

            case COLOR_BACKGROUNG:
                //ノード背景色
                node.setNodeBackgroundColor(code);
                break;

            case COLOR_TEXT:
                //ノードテキストカラー
                node.setNodeTextColor(code);
                break;

            case COLOR_BORDER:
                //枠線カラー
                node.setBorderColor(code);
                break;

            case COLOR_SHADOW:
                //影カラー
                node.setShadowColor(code, node.getNode().getKind());
                break;

            case COLOR_LINE:
                //ラインカラー
                ((ChildNode) node).setLineColor(code);
                break;
        }*/

    }


}
