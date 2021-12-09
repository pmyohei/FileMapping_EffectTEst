package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RootNodeView extends FrameLayout {

    /* フィールド-ノード間共通 */
    //操作ツール選択中ノード
    public static ViewGroup mv_toolSelectedNode = null;

    /* フィールド */
    //ノード情報
    public NodeTable mNode;

    //ダブルタップ検知用
    public GestureDetector mGestureDetector;

    //ルーツアイコン表示
    public boolean misOpenToolIcon;

    //データ
    public float mCenterPosX;        //ノード中心座標X
    public float mCenterPosY;        //ノード中心座標Y

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public RootNodeView(Context context) {
        super(context);

        Log.i("NodeView_new", "1");

        init();
    }

    public RootNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.i("NodeView_new", "2");

        init();
    }

    public RootNodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.i("NodeView_new", "3");

        init();
    }

    /*
     * 初期化処理
     */
    private void init() {

        Log.i("RootNodeView", "init");

        //ダブルタップリスナーを実装したGestureDetector
        //mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());

        //ツールアイコン非表示
        misOpenToolIcon = false;

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from( getContext() );
        inflater.inflate(R.layout.node, this, true);

        Log.i("init", "root getChildCount = " + getChildCount());

        //クリックリスナー
        //※空のクリック処理をオーバーライドしないと、タッチ処理が検出されないため、空処理を入れとく
        //※「implements View.OnClickListener」で空処理を入れるのはなぜか効果なし
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
            }
        });

        //タッチリスナー
        setOnTouchListener(new NodeTouchListener());
    }

    /*
     * ノード名の設定
     */
    public void setNodeName( String name ) {
        ((TextView)findViewById(R.id.tv_node)).setText(name);
    }


    /*
     * ノードタッチリスナー
     */
    private class NodeTouchListener implements View.OnTouchListener {

        /*
         * コンストラクタ
         */
        public NodeTouchListener() {

            //ダブルタップリスナーを実装したGestureDetector
            mGestureDetector = new GestureDetector(getContext(), new NodeTouchListener.DoubleTapListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ダブルタップ処理
            mGestureDetector.onTouchEvent(event);

            //イベント処理完了
            return false;
        }

        /*
         * ダブルタップリスナー
         */
        private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {

            /*
             * ダブルタップリスナー
             *   ツールアイコンの表示制御を行う。
             *   ※他ノードがオープン中であれば、クローズしてタップされたノードのツールアイコンを表示する
             */
            @Override
            public boolean onDoubleTap(MotionEvent event) {

                Log.i("tap", "onDoubleTap root");
                Log.i("tap", "onDoubleTap getChildCount1 = " + getChildCount());

                //本ビューのトップレイアウトを取得（※ここで取得しているのは本レイアウト自身）
                ViewGroup v_tappedNode = (ViewGroup)getChildAt(0);
                Log.i("tap", "onDoubleTap v_tappedNode = " + ((ViewGroup) v_tappedNode).getChildCount());

                if(misOpenToolIcon){
                    //ツールアイコン表示中の場合

                    //操作ツールクローズ
                    toolOpenControl( v_tappedNode, View.INVISIBLE );

                } else {
                    //ツールアイコン非表示の場合

                    //他のノードが表示中であれば、閉じる
                    if( mv_toolSelectedNode != null ){
                        //操作ツールクローズ
                        toolOpenControl( mv_toolSelectedNode, View.INVISIBLE );
                    }

                    //操作ツールオープン
                    toolOpenControl( v_tappedNode, View.VISIBLE );

                    //オープン中ノードとして保持
                    mv_toolSelectedNode = v_tappedNode;
                }

                //ツールアイコン状態変更
                misOpenToolIcon = !misOpenToolIcon;

                return super.onDoubleTap(event);
            }

            /*
             * 操作ツール表示制御
             */
            public void toolOpenControl(ViewGroup v_node, int value ) {

                //ツールアイコンを表示
                for (int i = 0; i < v_node.getChildCount(); i++) {
                    //子ビューを取得
                    View v = v_node.getChildAt(i);

                    //アイコンボタンの親レイアウトの場合
                    if (v instanceof LinearLayout) {
                        //表示・非表示
                        v.setVisibility(value);
                    }
                }
            }
        }
    }


    /*
     * ダブルタップリスナー
     */
/*    private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {

            Log.i("RootNodeView", "onDoubleTap");

            return super.onDoubleTap(event);
        }
    }*/

    /*-- getter／setter --*/
    public NodeTable getNode() {
        return mNode;
    }
    public void setNode(NodeTable mNode) {
        this.mNode = mNode;
    }

    public float getCenterPosX() {
        return mCenterPosX;
    }
    public void setCenterPosX(float centerPosX) {
        this.mCenterPosX = centerPosX;
    }

    public float getCenterPosY() {
        return mCenterPosY;
    }
    public void setCenterPosY(float centerPosY) {
        this.mCenterPosY = centerPosY;
    }

}
