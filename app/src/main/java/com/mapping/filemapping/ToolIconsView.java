package com.mapping.filemapping;

import static com.mapping.filemapping.MapActivity.MOVE_UPPER;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

/*
 * ノードのツールアイコン全体のビュー
 */
public class ToolIconsView extends ConstraintLayout {

    private class TooliconData {

        //アイコン種別
        public static final int CREATE_NODE = 0;
        public static final int CREATE_PICTURE_NODE = 1;
        public static final int EDIT = 2;
        public static final int DISPLAY_ALL_PICTURE = 3;
        public static final int DELETE = 4;
        public static final int CHANGE_PARENT = 5;
        public static final int NEW_MAP = 6;
        public static final int ADD_PICTURE = 7;
        public static final int CLOSE = 8;

        private final int iconKind;
        private final ImageButton ibIcon;

        public TooliconData(int kind, ImageButton ib ){
            iconKind = kind;
            ibIcon = ib;
        }

        public int getIconKind() {
            return iconKind;
        }
        public ImageButton getIbIcon() {
            return ibIcon;
        }
    }

    //次のアイコンの加算角度
    private final int ADD_ANGLE = 30;
    //対象ノード
    private BaseNode v_baseNode;
    //マップ画面
    private MapActivity mMapActivity;

    /*
     * コンストラクタ(New)
     */
    public ToolIconsView(Context context, BaseNode v_node, MapActivity mapActivity) {
        super(context);
        v_baseNode = v_node;
        mMapActivity = mapActivity;

        //初期化
        init();
    }

    /*
     *　レイアウトから生成時
     *    ※呼ばれない作り
     */
    public ToolIconsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * 初期化処理
     */
    private void init() {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.node_tool_icon, this, true);

        //既に開いているノードを閉じる
        closeShowingIcon();

        //ノードに自分を持たせる
        v_baseNode.setIconView(this);

        //ツールアイコン設定
        setupIcon();
    }

    /*
     * 本レイアウトの生成位置
     */
    public void setupShowPosition() {

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        //レイアウト確定後は、不要なので本リスナー削除
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        //ノードの中心位置
                        int nodex = v_baseNode.getLeft() + (v_baseNode.getWidth() / 2);
                        int nodey = v_baseNode.getTop() + (v_baseNode.getHeight() / 2);

                        //本レイアウト位置
                        int left = nodex - (getWidth() / 2);
                        int top = nodey - (getHeight() / 2);

                        //見かけ上の移動
                        //※これをせず、mlp.setMargins()のみの処理をすると、画面上から見えなくなってしまうため注意
                        layout(left, top, left + getWidth(), top + getHeight());

                        //レイアウト位置変更
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getLayoutParams();
                        mlp.setMargins(left, top, mlp.rightMargin, mlp.bottomMargin);

                        //アイコンを表示
                        showIcon();
                    }
                }
        );

    }


    /*
     * ツールアイコン設定
     */
    public void setupIcon() {

        //アイコンビュー情報を取得
        List<TooliconData> iconViews = getIconResourceId();

        //半径延長比率
        final float RADIUS_EXTENSION_RATIO = 1.0f;
        //半径
        int radius = (int) ((v_baseNode.getWidth()) * RADIUS_EXTENSION_RATIO);
        //中心のビュー
        int v_center_id = findViewById(R.id.v_center).getId();

        //アイコン設置初期角度
        final int START_ANGLE = 30;

        int count = 0;
        for (TooliconData toolIconData : iconViews) {

            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);

            Log.i("アイコン", "半径 初期値=" + layoutParams.circleRadius + " 設定値=" + radius);

            //角度、半径を設定
            layoutParams.circleConstraint = v_center_id;
            layoutParams.circleAngle  = START_ANGLE * (count + 1);
            layoutParams.circleRadius = radius;

            //アイコンビュー
            ImageButton ib = toolIconData.getIbIcon();
            ib.setLayoutParams(layoutParams);

            //※最終的な可視化はレイアウト確定後に行う
            //※この時点では、サイズが欲しいため領域確保のみ
            ib.setVisibility(INVISIBLE);

            //リスナーの設定
            setIconListener(toolIconData);

            Log.i("アイコン", "角度=" + START_ANGLE * count);

            count++;
        }

        //生成位置
        //※ツールアイコン表示後に行う
        setupShowPosition();
    }

    /*
     * アイコンを表示
     */
    public void showIcon() {

        //アイコンビュー情報を取得
        List<TooliconData> iconViews = getIconResourceId();

        //アニメーション開始オフセット
        final long ANIM_OFFSET = 20;

        int count = 0;
        for (TooliconData toolIconData : iconViews) {

            //アイコンビュー
            ImageButton ib = toolIconData.getIbIcon();
            ib.setVisibility(VISIBLE);

            //表示アニメーション
            Animation animation = AnimationUtils.loadAnimation(ib.getContext(), R.anim.show_tool_icon);
            animation.setStartOffset( ANIM_OFFSET * count );
            ib.startAnimation(animation);

            count++;
        }
    }

    /*
     * ツールアイコンをリストを取得
     */
    public List<TooliconData> getIconResourceId() {

        //ツールアイコンリスト
        List<TooliconData> iconViews = new ArrayList<>();
        //ノード種別
        int kind = v_baseNode.getNode().getKind();

        if (kind == NodeTable.NODE_KIND_ROOT) {
            iconViews.add( new TooliconData( TooliconData.CREATE_NODE, findViewById( R.id.ib_createNode ) ) );
            iconViews.add( new TooliconData( TooliconData.CREATE_PICTURE_NODE, findViewById( R.id.ib_createPictureNode ) ) );
            iconViews.add( new TooliconData( TooliconData.EDIT, findViewById( R.id.ib_edit ) ) );
            iconViews.add( new TooliconData( TooliconData.DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture ) ) );
            iconViews.add( new TooliconData( TooliconData.CLOSE, findViewById( R.id.ib_close ) ) );

        } else if (kind == NodeTable.NODE_KIND_NODE) {
            iconViews.add( new TooliconData( TooliconData.CREATE_NODE, findViewById( R.id.ib_createNode )) );
            iconViews.add( new TooliconData( TooliconData.CREATE_PICTURE_NODE, findViewById( R.id.ib_createPictureNode )) );
            iconViews.add( new TooliconData( TooliconData.EDIT, findViewById( R.id.ib_edit )) );
            iconViews.add( new TooliconData( TooliconData.DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture )) );
            iconViews.add( new TooliconData( TooliconData.DELETE, findViewById( R.id.ib_delete )) );
            iconViews.add( new TooliconData( TooliconData.CHANGE_PARENT, findViewById( R.id.ib_changeParent )) );
            //iconViews.add( new TooliconData( TooliconData.ICON_NEW_MAP, findViewById( R.id.ib_newMap )) );
            iconViews.add( new TooliconData( TooliconData.CLOSE, findViewById( R.id.ib_close )) );

        } else {
            iconViews.add( new TooliconData( TooliconData.EDIT, findViewById( R.id.ib_edit )) );
            iconViews.add( new TooliconData( TooliconData.ADD_PICTURE, findViewById( R.id.ib_addPhoto )) );
            iconViews.add( new TooliconData( TooliconData.DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture )) );
            iconViews.add( new TooliconData( TooliconData.DELETE, findViewById( R.id.ib_delete )) );
            iconViews.add( new TooliconData( TooliconData.CHANGE_PARENT, findViewById( R.id.ib_changeParent )) );
            iconViews.add( new TooliconData( TooliconData.CLOSE, findViewById( R.id.ib_close )) );
        }

        return iconViews;
    }


    /*
     * アイコンのリスナー設定
     */
    public void setIconListener(TooliconData toolIconData ) {

        //アイコンビュー
        ImageButton ib = toolIconData.getIbIcon();
        //クリックリスナー
        OnClickListener listener = null;

        //アイコン毎にリスナーを生成
        int iconKind = toolIconData.getIconKind();
        switch (iconKind) {
            case TooliconData.CREATE_NODE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //アイコンが開かれたノードを親ノードとする
                        NodeTable parentNode = v_baseNode.getNode();

                        //初期生成位置
                        int posX = (int)parentNode.getCenterPosX() + ResourceManager.POS_NODE_INIT_OFFSET;
                        int posY = (int)parentNode.getCenterPosY();

                        //ノードを生成
                        NodeTable newNode = new NodeTable(
                                "",
                                parentNode.getPidMap(),
                                parentNode.getPid(),
                                NodeTable.NODE_KIND_NODE,
                                posX,
                                posY
                        );

                        //カラーパターン設定
                        String[] colors = mMapActivity.getMapDefaultColors();
                        newNode.setColorPattern( colors );

                        //ノードをマップに追加
                        BaseNode v_node = mMapActivity.drawNode( mMapActivity.findViewById(R.id.fl_map), newNode );

                        //DB保存処理
                        AsyncCreateNode db = new AsyncCreateNode(mMapActivity, newNode, new AsyncCreateNode.OnFinishListener() {
                            @Override
                            public void onFinish(int nodePid) {
                                //データ挿入されたため、レコードに割り当てられたpidをテーブルに設定
                                newNode.setPid(nodePid);

                                //マップ内ノードリストに追加
                                MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
                                mapCommonData.addNodes(newNode);

                                //更新対象ビューに追加
                                mapCommonData.enqueUpdateNodeWithUnique(newNode);
                            }
                        });

                        db.execute();

                        //BottomSheetを開く（画面移動あり）
                        mMapActivity.openDesignBottomSheet(DesignBottomSheet.NODE, v_node, posX, posY, MOVE_UPPER);

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.CREATE_PICTURE_NODE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();

                        //ノード
                        NodeTable node = v_baseNode.getNode();

                        //ピクチャトリミング画面へ遷移
                        Context context = getContext();
                        Intent intent = new Intent(context, PictureTrimmingActivity.class);
                        intent.putExtra(MapActivity.INTENT_MAP_PID, node.getPidMap());
                        intent.putExtra(MapActivity.INTENT_NODE_PID, node.getPid());
                        intent.putExtra(MapActivity.INTENT_COLORS, mMapActivity.getMapDefaultColors());

                        //開始
                        mMapActivity.getTrimmingLauncher().launch(intent);
                    }
                };

                break;

            case TooliconData.EDIT:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //更新対象ビューに追加
                        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
                        mapCommonData.enqueUpdateNodeWithUnique(v_baseNode.getNode());

                        //ノード本体のマージンを取得
                        float marginLeft = v_baseNode.getLeft();
                        float marginTop  = v_baseNode.getTop();

                        //BottomSheetを開く（画面移動あり）
                        mMapActivity.openDesignBottomSheet(DesignBottomSheet.NODE, v_baseNode, marginLeft, marginTop, MOVE_UPPER);

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.DISPLAY_ALL_PICTURE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        Intent intent = new Intent( mMapActivity, PictureGalleryActivity.class );
                        intent.putExtra(MapActivity.INTENT_NODE_PID, v_baseNode.getNode().getPid());

                        mMapActivity.startActivity(intent);

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.DELETE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //本ノード配下のノード（本ノード含む）を全て取得する
                        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
                        mapCommonData.setDeleteNodes(v_baseNode.getNode().getPid());

                        //削除確認ダイアログを表示
                        new AlertDialog.Builder(getContext())
                                .setTitle("ノード削除確認")
                                .setMessage("配下のノードも全て削除されます。\nなお、端末上から写真は削除されません。")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //削除対象ノード
                                        NodeArrayList<NodeTable> nodes = mapCommonData.getDeleteNodes();

                                        //DBからノード削除
                                        AsyncDeleteNode db = new AsyncDeleteNode(getContext(), nodes, new AsyncDeleteNode.OnFinishListener() {
                                            @Override
                                            public void onFinish() {

                                                //自身と配下ノードをレイアウトから削除
                                                ((ChildNode)v_baseNode).removeLayoutUnderSelf();

                                                //共通データに削除完了処理を行わせる
                                                mapCommonData.finishDeleteNode();
                                            }
                                        });

                                        //非同期処理開始
                                        db.execute();
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.CHANGE_PARENT:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //マップアクティビティを親ノード変更モードにする
                        mMapActivity.enableChangeParentMode( v_baseNode.getNode().getPid() );

                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.NEW_MAP:
                //※現状なし
                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };

                break;

            case TooliconData.ADD_PICTURE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("アイコン", "クリックされました ADD_PICTURE");

                        //写真を一覧で表示
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");

                        //開始
                        mMapActivity.getGalleryLauncher().launch(intent);

                        /*-- 自分のクローズ処理は行わない仕様とする --*/
                        /*-- ギャラリーから戻ってきたとき、マップ画面側でどのピクチャノードがタッチされたのかを判別するため --*/
                    }
                };
                break;

            case TooliconData.CLOSE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                        //ノードに持たせていた自分をクローズ
                        v_baseNode.closeIconView();
                    }
                };
                break;
        }

        //リスナーを設定
        ib.setOnClickListener( listener );
    }

    /*
     * オープン中のアイコンのクローズ
     */
    public void closeShowingIcon() {

        //ノードリスト
        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        BaseNode haveNode = nodes.getShowingIconNode();
        if( haveNode == null ){
            //どのノードも開いてなければ、何もしない
            return;
        }

        //アイコンを閉じさせる
        haveNode.closeIconView();
    }

    /*
     * 自分自身をレイアウトから削除する
     */
    public void closeMyself() {
        //※removeView()がすぐに反映されないため、GONEさせとく
        setVisibility(GONE);

        ViewGroup parent = (ViewGroup)getRootView();
        parent.removeView( this );

        Log.i("ツールアイコン", "クローズチェック");
    }


}

