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
public class BaseNode extends FrameLayout {

    /* フィールド */
    //シリアルID
    //private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_VIEW;

    //ノード情報
    public NodeTable mNode;
    //ダブルタップ検知用
    public GestureDetector mGestureDetector;
    //ツールアイコン表示
    public boolean mIsOpenToolIcon;
    //データ
    public float mCenterPosX;        //ノード中心座標X
    public float mCenterPosY;        //ノード中心座標Y
    //ノード操作発生時の画面遷移ランチャー
    public ActivityResultLauncher<Intent> mNodeOperationLauncher;


    /*
     * コンストラクタ
     * 　 レイアウトに埋め込んだビューの生成時用
     */
    public BaseNode(Context context, AttributeSet attrs, int layoutID) {
        super(context, attrs);

        Log.i("BaseNode", "1");

        init(layoutID);
    }

    /*
     * コンストラクタ
     * 　　new用
     */
    @SuppressLint("ClickableViewAccessibility")
    public BaseNode(Context context, NodeTable node, ActivityResultLauncher<Intent> launcher, int layoutID) {
        super(context);

        Log.i("BaseNode", "2");

        //ノード情報を保持
        mNode = node;
        //ノード操作ランチャーを保持
        mNodeOperationLauncher = launcher;

        //レイアウト生成
        //LayoutInflater inflater = LayoutInflater.from(getContext());
        //inflater.inflate(layoutID, this, true);

        init(layoutID);
    }


    /*
     * 初期化処理
     */
    private void init( int layoutID ) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, true);

        Log.i("BaseNode", "init");

        //ツールアイコン非表示
        mIsOpenToolIcon = false;

        Log.i("init", "root getChildCount = " + getChildCount());

        //※クリックを有効にしないとタッチ処理が検出されない
        setClickable(true);
        //タッチリスナー
        setOnTouchListener(new RootNodeTouchListener());

        //ノード情報をビューに設定
        reflectViewNodeInfo();

        //ツールアイコン設定
        setCommonToolIcon();
        setParentToolIcon();
    }


    /*
     * ツールアイコン設定
     *   ・クローズ
     *   ・配下の写真の一覧表示
     *   ・ノード編集
     */
    public void setCommonToolIcon() {

        //クローズ
        findViewById(R.id.ib_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //クローズする
                operationToolIcon();
            }
        });

        //ノード編集
        findViewById(R.id.ib_edit).setOnClickListener(new OnClickListener() {
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

    /*
     * ツールアイコン設定
     *   ・子ノードの追加
     *   ・写真ノードの追加
     */
    public void setParentToolIcon() {

        //ピクチャノードなら、何もしない
        if( (mNode == null) || (mNode.getKind() == NodeTable.NODE_KIND_PICTURE) ){
            return;
        }

        //ノード生成
        findViewById(R.id.ib_createNode).setOnClickListener(new OnClickListener() {
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
        findViewById(R.id.ib_createPictureNode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, PictureNodeSelectActivity.class);
                intent.putExtra( MapActivity.INTENT_MAP_PID, mNode.getPidMap() );
                intent.putExtra( MapActivity.INTENT_NODE_PID,mNode.getPid() );

                //画面遷移
                mNodeOperationLauncher.launch( intent );

                //クローズする
                operationToolIcon();
            }
        });

    }



    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    public void reflectViewNodeInfo() {

        Log.i("BaseNode", "reflectViewNodeInfo");

        //ノードデザインの更新
        setNodeDesign( mNode );
        setParentNodeInfo( mNode );
    }

    /*
     * ノードデザインの設定
     */
    public void setNodeDesign(NodeTable node) {

        if( node == null ){
            //ノード情報未保持なら、何もしない
            return;
        }

        //ノードデザイン
        //ノードの形
        //ノードの大きさ

    }

    /*
     * 親ノード情報の設定
     */
    public void setParentNodeInfo(NodeTable node) {

        //ピクチャノードなら、何もしない
        if( (node == null) || (node.getKind() == NodeTable.NODE_KIND_PICTURE) ){
            return;
        }

        //ノード名
        setNodeName(node.getNodeName());
        //ノード背景色
        //★仮
        //setBackgroundColor(getResources().getColor( R.color.cafe_2 ));
        Log.i("setNodeInformation", "getNodeColor()=" + node.getNodeColor());
        setBackgroundColor( Color.parseColor(node.getNodeColor()) );
    }

    /*
     * ノード名の設定
     */
    public void setNodeName(String name) {
        ((TextView) findViewById(R.id.tv_node)).setText(name);
    }

    /*
     * ノード背景色の設定
     */
    public void setBackgroundColor(int color) {
        //背景色を設定
        //ColorDrawable colorDrawable = (ColorDrawable)findViewById(R.id.tv_node).getBackground();
        //colorDrawable.setColor( color );

        Drawable drawable = findViewById(R.id.tv_node).getBackground();
        drawable.setTint( color );
    }

    /*
     * ノード中心座標の設定
     */
    public void calcCenterPos() {
        //中心座標を計算し、設定
        this.mCenterPosX = getLeft() + (getWidth() / 2f);
        this.mCenterPosY = getTop()  + (getHeight() / 2f);
    }

    /*
     * ツールアイコン表示制御
     */
    public void operationToolIcon() {

        //共通データ
        MapCommonData mapCommonData = (MapCommonData)((Activity)getContext()).getApplication();

        //表示制御値
        int visible;

        //オープン状態チェック
        if(mIsOpenToolIcon){

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
        mIsOpenToolIcon = !mIsOpenToolIcon;

        Log.i("toolOpenControl", "after =" + getWidth() + " " + getHeight() + " misOpenToolIcon=" + mIsOpenToolIcon);

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
                        MarginLayoutParams mlp = (MarginLayoutParams)getLayoutParams();
                        mlp.setMargins(getLeft(), getTop(), 0, 0);

                        Log.i("toolOpenControl", mNode.getNodeName() + " global=" + getWidth() + " " + getHeight());
                    }
                }
        );
    }



    /*
     * ノードタッチリスナー
     */
    public class RootNodeTouchListener implements OnTouchListener, Serializable {

        //シリアルID
        private static final long serialVersionUID = 3L;

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
            return mGestureDetector.onTouchEvent(event);
        }

        /*
         * ダブルタップリスナー
         */
        private class DoubleTapListener extends GestureDetector.SimpleOnGestureListener implements Serializable{

            /*
             * ダブルタップリスナー
             *   ツールアイコンの表示制御を行う。
             *   ※他ノードがオープン中であれば、クローズしてタップされたノードのツールアイコンを表示する
             */
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

    /*-- getter／setter --*/
    public NodeTable getNode() {
        return mNode;
    }
    public void setNode(NodeTable node) {
        this.mNode = node;

        //ノード情報の設定
        setNodeDesign(node);
        setParentNodeInfo(node);

        //ツールアイコンの設定
        setParentToolIcon();
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

    public void setNodeOperationLauncher( ActivityResultLauncher<Intent> launcher ) {
        this.mNodeOperationLauncher = launcher;
    }
}
