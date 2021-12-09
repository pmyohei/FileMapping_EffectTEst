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
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

public class RootNodeView extends FrameLayout {

    /* フィールド-ノード間共通 */
    //ツールアイコン選択中ノード
    public static RootNodeView mv_toolOpenNode = null;

    /* フィールド */
    //ノード情報
    public NodeTable mNode;

    //自身ノード
    public RootNodeView mSelfView;

    //ダブルタップ検知用
    public GestureDetector mGestureDetector;

    //ルーツアイコン表示
    public boolean misOpenToolIcon;

    //データ
    public float mCenterPosX;        //ノード中心座標X
    public float mCenterPosY;        //ノード中心座標Y

    /*
     * コンストラクタ
     *   NodeViewからのコール用
     */
    @SuppressLint("ClickableViewAccessibility")
    public RootNodeView(Context context, int layoutID) {
        super(context);

        Log.i("RootNodeView", "1");

        init(layoutID);
    }

    /*
     *  コンストラクタ
     * 　 レイアウトに埋め込んだビューの生成時は、本コンストラクタがコールされる
     */
    public RootNodeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.i("RootNodeView", "2");

        init(R.layout.node);
    }

/*    public RootNodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.i("RootNodeView", "3");

        init();
    }*/

    /*
     * 初期化処理
     */
    private void init( int layoutID ) {

        Log.i("RootNodeView", "init");

        //自身のビュー
        mSelfView = this;

        //ツールアイコン非表示
        misOpenToolIcon = false;

        Log.i("init", "root getChildCount = " + getChildCount());

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, true);

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
        setOnTouchListener(new RootNodeTouchListener());

        //ツールアイコン設定
        setToolIcon();
    }


    /*
     * ツールアイコン設定
     */
    public void setToolIcon() {

        //クローズ
        ImageButton ib_close = findViewById(R.id.ib_close);
        ib_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //クローズする
                toolDisplayControl();
            }
        });


    }



    /*
     * ノード名の設定
     */
    public void setNodeName(String name) {
        ((TextView) findViewById(R.id.tv_node)).setText(name);
    }


    /*
     * ツールアイコン表示制御
     */
    public void toolDisplayControl() {

        //表示制御値
        int visible;

        //オープン状態チェック
        if( misOpenToolIcon ){

            //クローズする
            visible = View.GONE;

            //クローズするためnull設定
            mv_toolOpenNode = null;

        } else{

            //オープンする
            visible = View.VISIBLE;

            //オープン中のノードがあれば閉じる
            if( mv_toolOpenNode != null ){
                mv_toolOpenNode.toolDisplayControl();
            }

            //自ノードをオープン中ノードとして保持
            mv_toolOpenNode = mSelfView;
        }

        //本ビューのレイアウトを取得（※ここで取得しているのは、ノード用レイアウトのルートレイアウト）
        ViewGroup vg_NodeLayout = (ViewGroup)getChildAt(0);

        Log.i("toolOpenControl", mNode.getNodeName() + " before=" + getWidth() + " " + getHeight());

        //ツールアイコンを表示
        for (int i = 0; i < vg_NodeLayout.getChildCount(); i++) {
            //子ビューを取得
            View v = vg_NodeLayout.getChildAt(i);

            //アイコンボタンの親レイアウトの場合
            if (v instanceof ImageButton) {

                Log.i("toolOpenControl", "value=" + visible);

                //表示・非表示
                v.setVisibility(visible);
            }
        }

        //ツールアイコンのオープン状態変更
        misOpenToolIcon = !misOpenToolIcon;

        Log.i("toolOpenControl", "after =" + getWidth() + " " + getHeight() + " misOpenToolIcon=" + misOpenToolIcon);

        //ルートノード
        if( mNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //レイアウト位置調整は不要のため、ここで終了77
            return;
        }

        //サイズが変わるため、中心位置が移動しないよう新しいサイズで位置調整
        ViewTreeObserver observer = vg_NodeLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        vg_NodeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //新しいサイズ
                        int newWidth  = getWidth();
                        int newHeight = getHeight();

                        //マージン計算
                        int left = (int)mCenterPosX - (newWidth / 2);
                        int top  = (int)mCenterPosY - (newHeight / 2);

                        //位置反映
                        layout( left, top, left + newWidth, top + newHeight );

                        //現在の表示上位置をマージンに反映
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)getLayoutParams();
                        mlp.setMargins(getLeft(), getTop(), 0, 0);

                        Log.i("toolOpenControl", mNode.getNodeName() + " global=" + getWidth() + " " + getHeight());
                    }
                }
        );
    }



    /*
     * ノードタッチリスナー
     */
    public class RootNodeTouchListener implements View.OnTouchListener {

        /*
         * コンストラクタ
         */
        public RootNodeTouchListener() {

            //ダブルタップリスナーを実装したGestureDetector
            mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ダブルタップ処理
            boolean test = mGestureDetector.onTouchEvent(event);

            //イベント処理完了
            //return false;
            return test;
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

                Log.i("tap", "onDoubleTap getChildCount1 = " + getChildCount());

                //ツールアイコン表示制御
                toolDisplayControl();

                //return super.onDoubleTap(event);
                return true;
            }
        }
    }

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
