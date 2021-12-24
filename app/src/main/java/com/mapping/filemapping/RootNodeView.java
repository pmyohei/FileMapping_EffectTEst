package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import androidx.activity.result.ActivityResultLauncher;

import java.io.Serializable;

/*
 *  ルートノード
 *    Serializable：intentによるデータの受け渡しを行うために実装
 */
public class RootNodeView extends BaseNode implements Serializable {

    /* フィールド */
    //シリアルID
    private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_VIEW;

/*
    //ノード情報
    public NodeTable mNode;
    //ダブルタップ検知用
    public GestureDetector mGestureDetector;
    //ツールアイコン表示
    public boolean misOpenToolIcon;
    //データ
    public float mCenterPosX;        //ノード中心座標X
    public float mCenterPosY;        //ノード中心座標Y
    //ノード操作発生時の画面遷移ランチャー
    ActivityResultLauncher<Intent> mNodeOperationLauncher;
*/

    /*
     * コンストラクタ
     *   NodeViewからのコール用
     */
/*    @SuppressLint("ClickableViewAccessibility")
    public RootNodeView(Context context, int layoutID) {
        super(context);

        Log.i("RootNodeView", "1");

        init(layoutID, true);
    }*/

    /*
     *  コンストラクタ
     * 　 レイアウトに埋め込んだビューの生成時は、本コンストラクタがコールされる
     */
    public RootNodeView(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.root_node);

        Log.i("RootNodeView", "2");

        //init(R.layout.root_node, true);

        //レイアウト生成
        //LayoutInflater inflater = LayoutInflater.from(getContext());
        //inflater.inflate(R.layout.root_node, this, true);

        //ツールアイコン設定
        //setToolIcon();
    }

    /*
     * 初期化処理
     */
/*    private void init( int layoutID, boolean attachToRoot ) {

        Log.i("RootNodeView", "init");

        //ツールアイコン非表示
        misOpenToolIcon = false;

        Log.i("init", "root getChildCount = " + getChildCount());

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, attachToRoot);

        //※クリックを有効にしないとタッチ処理が検出されない
        setClickable(true);
        //タッチリスナー
        setOnTouchListener(new RootNodeTouchListener());

        //ツールアイコン設定
        setToolIcon();
    }*/


    /*
     * ツールアイコン設定
     */
/*
    public void setToolIcon() {

        //クローズ
        ImageButton ib = findViewById(R.id.ib_close);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //クローズする
                operationToolIcon();
            }
        });

        //ノード生成
        ib = findViewById(R.id.ib_createNode);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, NodeEntryActivity.class);

                //タッチノードの情報を渡す
                intent.putExtra(MapActivity.INTENT_MAP_PID, mNode.getPidMap());
                intent.putExtra(MapActivity.INTENT_NODE_PID, mNode.getPid());
                intent.putExtra(MapActivity.INTENT_KIND_CREATE, true );

                //((Activity)context).startActivityForResult(intent, MapActivity.REQ_NODE_CREATE);
                mNodeOperationLauncher.launch( intent );

                //クローズする
                operationToolIcon();
            }
        });

        //ノード生成(ピクチャ)
        ib = findViewById(R.id.ib_createPictureNode);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, PictureNodeSelectActivity.class);
                intent.putExtra(MapActivity.INTENT_MAP_PID, mNode.getPidMap() );
                intent.putExtra(MapActivity.INTENT_NODE_PID,mNode.getPid() );

                //画面遷移
                mNodeOperationLauncher.launch( intent );

                //クローズする
                operationToolIcon();
            }
        });

        //ノード編集
        ib = findViewById(R.id.ib_edit);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, NodeEntryActivity.class);

                //mNode.setNodeView(null);

                //タッチノードの情報を渡す
                //intent.putExtra( MapActivity.INTENT_NODE, mNode );

                //タッチノードを共通データとして設定
                MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();
                mapCommonData.setEditNode( mNode );

                //画面遷移
                //((Activity)context).startActivityForResult(intent, MapActivity.REQ_NODE_EDIT);
                mNodeOperationLauncher.launch( intent );

                //クローズする
                operationToolIcon();
            }
        });

    }
*/


    /*
     * ノードテーブルの情報をノードビューに反映する
     */
/*    public void reflectViewNodeInfo() {

        //★設定を追加した際に反映

    }*/

    /*
     * ノード情報の設定
     */
/*    public void setNodeInformation(NodeTable node) {
        //ノード名
        setNodeName(node.getNodeName());
        //ノード背景色
        //★仮
        //setBackgroundColor(getResources().getColor( R.color.cafe_2 ));
        Log.i("setNodeInformation", "getNodeColor()=" + node.getNodeColor());
        setBackgroundColor( Color.parseColor(node.getNodeColor()) );

    }*/

    /*
     * ノード名の設定
     */
/*
    public void setNodeName(String name) {
        ((TextView) findViewById(R.id.tv_node)).setText(name);
    }
*/

    /*
     * ノード背景色の設定
     */
/*
    public void setBackgroundColor(int color) {
        //背景色を設定
        //ColorDrawable colorDrawable = (ColorDrawable)findViewById(R.id.tv_node).getBackground();
        //colorDrawable.setColor( color );

        Drawable aa = findViewById(R.id.tv_node).getBackground();
        aa.setTint( color );
    }
*/

    /*
     * ノード中心座標の設定
     */
/*    public void calcCenterPos() {
        //中心座標を計算し、設定
        this.mCenterPosX = getLeft() + (getWidth() / 2f);
        this.mCenterPosY = getTop()  + (getHeight() / 2f);
    }*/

    /*
     * ツールアイコン表示制御
     */
/*
    public void operationToolIcon() {

        //共通データ
        MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();

        //表示制御値
        int visible;

        //オープン状態チェック
        if( misOpenToolIcon ){

            //クローズする
            visible = View.GONE;

            //クローズするためnull設定
            mapCommonData.setToolOpeningNode(null);
            //mv_toolOpenNode = null;

        } else{

            //オープンする
            visible = View.VISIBLE;

            //オープン中のノードがあれば閉じる
            //if( mv_toolOpenNode != null ){
            if( mapCommonData.isToolOpening() ){
                //mv_toolOpenNode.toolDisplayControl();
                mapCommonData.closeToolOpeningNode();
            }

            //自ノードをオープン中ノードとして保持
            //mv_toolOpenNode = this;
            mapCommonData.setToolOpeningNode(this);
        }

        //本ビューのレイアウトを取得（※ここで取得しているのは、ノード用レイアウトのルートレイアウト）
        ViewGroup vg_NodeLayout = (ViewGroup)getChildAt(0);

        Log.i("toolOpenControl", "before=" + getWidth() + " " + getHeight());

        //ツールアイコンを表示
        for (int i = 0; i < vg_NodeLayout.getChildCount(); i++) {
            //子ビューを取得
            View v = vg_NodeLayout.getChildAt(i);

            //アイコンボタンの親レイアウトの場合
            if (v instanceof ImageButton) {

                Log.i("toolOpenControl", "value=" + visible);

                //表示・非表示
                v.setVisibility(visible);
                //v.setTranslationZ(200);
                //v.bringToFront();
            }
        }

        //ツールアイコンのオープン状態変更
        misOpenToolIcon = !misOpenToolIcon;

        Log.i("toolOpenControl", "after =" + getWidth() + " " + getHeight() + " misOpenToolIcon=" + misOpenToolIcon);

        //ルートノード
        if( mNode.getKind() == NodeTable.NODE_KIND_ROOT ){
            //レイアウト位置調整は不要のため、ここで終了
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
*/



    /*
     * ノードタッチリスナー
     */
/*
    public class RootNodeTouchListener implements View.OnTouchListener, Serializable {

        //シリアルID
        private static final long serialVersionUID = 3L;

        */
/*
         * コンストラクタ
         *//*

        public RootNodeTouchListener() {

            //ダブルタップリスナーを実装したGestureDetector
            mGestureDetector = new GestureDetector(getContext(), new DoubleTapListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {

            //ダブルタップ処理
            return mGestureDetector.onTouchEvent(event);
        }

        */
/*
         * ダブルタップリスナー
         *//*

        private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener implements Serializable{

            */
/*
             * ダブルタップリスナー
             *   ツールアイコンの表示制御を行う。
             *   ※他ノードがオープン中であれば、クローズしてタップされたノードのツールアイコンを表示する
             *//*

            @Override
            public boolean onDoubleTap(MotionEvent event) {

                Log.i("tap", "onDoubleTap getChildCount1 = " + getChildCount());

                //ツールアイコン表示制御
                operationToolIcon();

                //return super.onDoubleTap(event);
                return true;
            }
        }
    }
*/

    /*-- getter／setter --*/

}
