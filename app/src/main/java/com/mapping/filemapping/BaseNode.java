package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
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
import androidx.cardview.widget.CardView;

import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

/*
 *  ルートノード
 *    Serializable：intentによるデータの受け渡しを行うために実装
 */
public class BaseNode extends FrameLayout {

    /* フィールド */
    //シリアルID
    //private static final long serialVersionUID = ResourceManager.SERIAL_VERSION_UID_NODE_VIEW;

    //中心座標の初期値
    public static float INIT_CENTER_POS = -0.1f;

    //ノード情報
    public NodeTable mNode;
    //ダブルタップ検知用
    public GestureDetector mGestureDetector;
    //ツールアイコン表示
    public boolean mIsOpenToolIcon;
    //ノード中心座標
    public float mCenterPosX = INIT_CENTER_POS;
    public float mCenterPosY = INIT_CENTER_POS;
    //ノード操作発生時の画面遷移ランチャー
    public ActivityResultLauncher<Intent> mNodeOperationLauncher;
    //ノード生成／編集クリックリスナー
    private MapActivity.NodeDesignClickListener mNodeDesignClickListener;
    //ダブルタップリスナー
    private View.OnClickListener mClickListener;
    //アイコンビュー（開いていない場合は、nullを設定する）
    private ToolIconsView mIconView;

    /*
     * コンストラクタ
     */
    public BaseNode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

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
    private void init(int layoutID) {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(layoutID, this, true);

        Log.i("BaseNode", "init");

        //ツールアイコン非表示
        mIsOpenToolIcon = false;
        //ツールアイコン未保持
        mIconView = null;

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


        //お試し
/*        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickListener.onClick( view );
            }
        });*/
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

        //自分自身
        BaseNode bn_self = this;

        //ノード編集
        findViewById(R.id.ib_edit).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                //mNodeDesignClickListener.setTouchNode( bn_self );
                mNodeDesignClickListener.onClickIcon(bn_self, view, false);

/*
                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, NodeEntryActivity.class);

                //mNode.setNodeView(null);

                //タッチノードの情報を渡す
                //intent.putExtra( MapActivity.INTENT_NODE, mNode );

                //タッチノードを共通データとして設定
                MapCommonData mapCommonData = (MapCommonData) ((Activity) getContext()).getApplication();
                mapCommonData.setEditNode(mNode);

                //画面遷移
                //((Activity)context).startActivityForResult(intent, MapActivity.REQ_NODE_EDIT);
                mNodeOperationLauncher.launch(intent);
*/

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
        if ((mNode == null) || (mNode.getKind() == NodeTable.NODE_KIND_PICTURE)) {
            return;
        }

        //自分自身
        BaseNode bn_self = this;

        //ノード生成
        findViewById(R.id.ib_createNode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                mNodeDesignClickListener.onClickIcon(bn_self, view, true);

/*
                //ノード情報画面へ遷移
                Context context = getContext();
                Intent intent = new Intent(context, NodeEntryActivity.class);

                //タッチノードの情報を渡す
                intent.putExtra(MapActivity.INTENT_MAP_PID, mNode.getPidMap());
                intent.putExtra(MapActivity.INTENT_NODE_PID, mNode.getPid());
                intent.putExtra(MapActivity.INTENT_KIND_CREATE, true);

                //((Activity)context).startActivityForResult(intent, MapActivity.REQ_NODE_CREATE);
                //mNodeOperationLauncher.launch( intent );

                //ダイアログを生成
                DialogFragment dialog = new NodeDesignDialog();
                //dialog.setArguments(bundle);
                dialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "New Node");
*/

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
                Intent intent = new Intent(context, PictureTrimmingActivity.class);
                intent.putExtra(MapActivity.INTENT_MAP_PID, mNode.getPidMap());
                intent.putExtra(MapActivity.INTENT_NODE_PID, mNode.getPid());

                //画面遷移
                mNodeOperationLauncher.launch(intent);

                //クローズする
                operationToolIcon();
            }
        });

    }

    /*
     * ツールアイコン-ノード生成／編集クリックリスナー
     */
    public void setOnNodeDesignClickListener(MapActivity.NodeDesignClickListener listener) {
        mNodeDesignClickListener = listener;
    }

    /*
     * ノードデザインの設定
     */
/*    public void setShadowPaint() {

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mShadowPaint = new Paint();
        mShadowPaint.setColor(Color.WHITE);
        mShadowPaint.setAntiAlias(true);
    }*/

    /*
     * ノードテーブルの情報をノードビューに反映する
     */
    public void reflectViewNodeInfo() {

        Log.i("BaseNode", "reflectViewNodeInfo");

        //ノードデザインの更新
        setNodeDesign();
    }

    /*
     * ノードデザインの設定
     */
    public void setNodeDesign() {

        if (mNode == null) {
            //ノード情報未保持なら、何もしない
            return;
        }

        //ノードの基本属性のデザインを設定
        setAsBaseNodeInfo();

        //親ノード属性のデザインを設定
        setAsParentNodeInfo();

    }

    /*
     * 基本ノードとしての情報の設定
     */
    public void setAsBaseNodeInfo() {

        if (mNode == null) {
            return;
        }

        //枠色
        setBorderColor(mNode.getBorderColor());
        //枠サイズ
        setBorderSize(mNode.getBorderSize());
        //影色
        setShadowColor(mNode.getShadowColor(), mNode.getKind());
    }

    /*
     * 親ノードとしての情報の設定
     */
    public void setAsParentNodeInfo() {

        //ピクチャノードなら、何もしない
        if ((mNode == null) || (mNode.getKind() == NodeTable.NODE_KIND_PICTURE)) {
            return;
        }

        //ノード名
        setNodeName(mNode.getNodeName());
        //ノードテキストカラー
        setNodeTextColor( mNode.getTextColor() );
        //ノード背景色
        setNodeBackgroundColor(mNode.getNodeColor());
    }

    /*
     * ノード名の設定
     */
    public void setNodeName(String name) {
        ((TextView) findViewById(R.id.tv_node)).setText(name);

        //レイアウト確定後処理
        //★
        //・ノードを円形にする
        //・中心位置を再設定
        //・ラインを再描画
    }

    /*
     * ノード背景色の設定
     *   para：例)#123456
     */
    public void setNodeBackgroundColor(String color) {
        //背景色を設定
        //ColorDrawable colorDrawable = (ColorDrawable)findViewById(R.id.tv_node).getBackground();
        //colorDrawable.setColor( color );

        //Drawable drawable = findViewById(R.id.tv_node).getBackground();
        //drawable.setTint( color );
        CardView cv_node = findViewById(R.id.cv_node);
        //cv_node.setBackgroundColor( Color.parseColor(color) );
        cv_node.setCardBackgroundColor(Color.parseColor(color));
    }

    /*
     * ノード背景色の取得
     */
    public String getNodeBackgroundColor() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        ColorStateList colorStateList = cv_node.getCardBackgroundColor();
        int colorInt = colorStateList.getDefaultColor();

        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード名のテキスト色の設定
     *   para：例)#123456
     */
    public void setNodeTextColor(String color) {
        ((TextView) findViewById(R.id.tv_node)).setTextColor( Color.parseColor(color) );
    }

    /*
     * ノード名のテキスト色の取得
     *   para：例)#123456
     */
    public String getNodeTextColor() {

        TextView tv_node = findViewById(R.id.tv_node);

        ColorStateList colorStateList = tv_node.getTextColors();
        int colorInt = colorStateList.getDefaultColor();

        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード名のフォント設定
     */
    public void setNodeFont(Typeface font) {
        TextView tv_node = findViewById(R.id.tv_node);
        tv_node.setTypeface( font );
    }

    /*
     * ノード形の設定
     */
    public void setNodeShape( int shapeKind ) {

        if( shapeKind == NodeTable.CIRCLE ){
            setShapeCircle();
        } else {
            setShapeSquare();
        }

    }

    /*
     * ノード枠線サイズの設定
     */
    public void setBorderSize( int thick ) {
        //枠サイズを設定
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeWidth( thick );
    }

    /*
     * ノード枠線サイズの取得
     */
    public int getBorderSize() {
        return ((MaterialCardView)findViewById( R.id.cv_node )).getStrokeWidth();
    }

    /*
     * ノード枠色の設定
     */
    public void setBorderColor( String color ) {
        //枠色を設定
        ((MaterialCardView)findViewById( R.id.cv_node )).setStrokeColor( Color.parseColor(color) );
    }

    /*
     * ノード枠色の取得
     */
    public String getBorderColor() {
        MaterialCardView cv_node = findViewById(R.id.cv_node);

        ColorStateList colorStateList = cv_node.getStrokeColorStateList();

        if( colorStateList == null ){
            //取得失敗なら、無効値用の色を返す
            return ResourceManager.NODE_INVALID_COLOR;
        }

        int colorInt = colorStateList.getDefaultColor();
        return ( "#" + Integer.toHexString( colorInt ) );
    }

    /*
     * ノード影色の設定
     */
    public void setShadowColor( String color, int nodeKind ) {
        //影色を設定
        ((NodeOutsideView)findViewById( R.id.l_nodeBody )).setShadowColor( Color.parseColor(color), nodeKind );
    }

    /*
     * ノード影色の取得
     */
    public String getShadowColor() {
        return ((NodeOutsideView)findViewById( R.id.l_nodeBody )).getShadowColor();
    }

    /*
     * ノード影の有無の設定
     */
    public void setShadowOnOff(boolean isShadow, int nodeKind  ) {
        //影色を設定
        ((NodeOutsideView)findViewById( R.id.l_nodeBody )).setShadowOnOff( isShadow, nodeKind  );
    }

    /*
     * ノード影の有無を切替
     */
    public void switchShadow() {
        //影色を設定
        ((NodeOutsideView)findViewById( R.id.l_nodeBody )).switchShadow();
    }

    /*
     * ノード影の有無を取得
     */
    public boolean isShadow() {
        //影の有無を取得
        return ((NodeOutsideView)findViewById( R.id.l_nodeBody )).isShadow();
    }

    /*
     * ノードサイズを設定
     */
    public void setScale() {
        //★アイコン対応で替えるかも
        float ratio = mNode.getSizeRatio();
        findViewById( R.id.cl_node ).setScaleX( ratio );
        findViewById( R.id.cl_node ).setScaleY( ratio );
    }

    /*
     * ノードサイズを設定
     */
    public void setScale( float ratio ) {
        //★アイコン対応で替えるかも
        setScaleX( ratio );
        setScaleY( ratio );

        mNode.setSizeRatio( ratio );
    }

    /*
     * 比率込みのノードサイズ（横幅）を取得
     *   ※ノードレイアウト全体のサイズ
     */
    public float getScaleWidth() {
        //現在の横幅 * 現在の比率
        return getWidth() * mNode.getSizeRatio();
    }


    /*
     * 比率込みのノード本体サイズ（横幅）を取得
     *   ※ノード本体のサイズ
     */
    public float getScaleNodeBodyWidth() {
        //現在の横幅 * 現在の比率
        return findViewById(R.id.cv_node).getWidth() * mNode.getSizeRatio();
    }

    /*
     * ノード中心座標の設定
     */
    public void calcCenterPos() {
        //中心座標を計算し、設定
        this.mCenterPosX = getLeft() + (getWidth() / 2f);
        this.mCenterPosY = getTop() + (getHeight() / 2f);
    }

    /*
     * ノード本体の左マージンを取得
     *  ※本メソッドはツールアイコンオープン時にノード本体のマージンを取得したい場合に
     *    使用される想定
     */
    public int getNodeLeft() {
        return (int)(mCenterPosX - (findViewById(R.id.cv_node).getWidth() / 2f));
    }
    /*
     * ノード本体の上マージンを取得
     *  ※本メソッドはツールアイコンオープン時にノード本体のマージンを取得したい場合に
     *    使用される想定
     */
    public int getNodeTop() {
        return (int)(mCenterPosY - (findViewById(R.id.cv_node).getHeight() / 2f));
    }

    /*
     * ノードの形を円形にする
     */
    private void setShapeCircle() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        //CardView cv_node = findViewById(R.id.cv_node);
        Log.i("Card", "width=" + cv_node.getWidth() + " height=" + cv_node.getHeight());

        int max;
        int width  = cv_node.getWidth();
        int height = cv_node.getHeight();
        if (width > height) {
            cv_node.setMinimumHeight(width);
            max = width;
        } else {
            cv_node.setMinimumWidth(height);
            max = height;
        }

        //int max = Math.max( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setRadius(max / 2.0f);
    }

    /*
     * ノードの形を四角（角丸）にする
     */
    private void setShapeSquare() {

        MaterialCardView cv_node = findViewById(R.id.cv_node);

        //短い辺から角丸値を計算
        int min = Math.min( cv_node.getWidth(), cv_node.getHeight() );
        cv_node.setRadius(min * ResourceManager.SQUARE_CORNER_RATIO);
    }

    /*
     * レイアウト確定後処理の設定
     */
    public void addOnNodeGlobalLayoutListener() {

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Log.i("OnGlobalLayoutListener", "Base側通過チェック");

                        //中心座標の計算
                        calcCenterPos();

                        //ノードの形状
                        setNodeShape( mNode.getNodeShape() );

                        //サイズを設定
                        setScale();

                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );
    }

    /*
     *
     */
    public void setOnNodeClickListener( View.OnClickListener listener ) {
        mClickListener = listener;
    }


/*    @Override
    protected void onDraw(Canvas canvas) {

        int nodeWidth = findViewById(R.id.cv_node).getWidth();

        Log.i("サイズチェック", "BaseView レイアウト確定＝" + nodeWidth);

        //paint.setShadowLayer( (nodeWidth / 4f), nodeWidth / 4, getHeight() / 4, Color.RED );
        mShadowPaint.setShadowLayer((nodeWidth / 5f), 0, 0, Color.RED);

        //paint.setColor(getResources().getColor(R.color.mark_5));
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, (nodeWidth / 2f), mShadowPaint);
    }*/

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

            Log.i("TranslationZ", "設定前→getTranslationZ=" + findViewById(R.id.cl_node).getTranslationZ());
            //findViewById(R.id.cl_node).setTranslationZ(1f);
            Log.i("TranslationZ", "設定後→getTranslationZ=" + findViewById(R.id.cl_node).getTranslationZ());
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

        //findViewById(R.id.cl_node).setTranslationZ(1f);
        //findViewById(R.id.cl_node).setElevation(100);

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
                //operationToolIcon();

                //
                mClickListener.onClick( mNode.getNodeView() );

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
        mNode = node;

        //ノード情報の設定
        setNodeDesign();

        //ツールアイコンの設定
        setParentToolIcon();
    }

    public float getCenterPosX() {
        return mCenterPosX;
    }
    public float getCenterPosY() {
        return mCenterPosY;
    }

    public void setNodeOperationLauncher( ActivityResultLauncher<Intent> launcher ) {
        this.mNodeOperationLauncher = launcher;
    }

    public ToolIconsView getIconView() {
        return this.mIconView;
    }
    public void setIconView( ToolIconsView iconView ) {
        this.mIconView = iconView;
    }
    public boolean hasIconView() {
        return (this.mIconView != null);
    }
    public void closeIconView() {
        this.mIconView.closeMyself();
        this.mIconView = null;
    }
    public void clearIconView() {
        this.mIconView = null;
    }
}
