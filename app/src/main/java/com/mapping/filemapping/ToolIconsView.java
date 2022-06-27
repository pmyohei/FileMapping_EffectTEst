package com.mapping.filemapping;

import static com.mapping.filemapping.MapActivity.MOVE_UPPER;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

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
        public static final int HELP = 8;
        public static final int CLOSE = 9;

        private final int iconKind;
        private final ImageButton ibIcon;

        public TooliconData(int kind, ImageButton ib) {
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

    //許可リクエストコード
    public static final int REQUEST_EXTERNAL_STORAGE_FOR_PICTURE_NODE = 1;
    public static final int REQUEST_EXTERNAL_STORAGE_FOR_GALLERY = 2;
    public static final int REQUEST_EXTERNAL_STORAGE_FOR_ADD_PICTURE = 3;
    public static final int REQUEST_EXTERNAL_EDIT_PICTURE_NODE = 4;

    //対象ノード
    private BaseNode mBaseNode;
    //マップ画面
    private MapActivity mMapActivity;

    /*
     * コンストラクタ(New)
     */
    public ToolIconsView(Context context, BaseNode v_node, MapActivity mapActivity) {
        super(context);
        mBaseNode = v_node;
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
        mBaseNode.setIconView(this);

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
                        int nodex = mBaseNode.getLeft() + (mBaseNode.getWidth() / 2);
                        int nodey = mBaseNode.getTop() + (mBaseNode.getHeight() / 2);

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

        //中心のビュー
        int v_center_id = findViewById(R.id.v_center).getId();
        //半径
        final int ADD_NODE_RADIUS = (int)getResources().getDimension(R.dimen.node_icon_add_distance);     //ノード半径に対する延長サイズ
        final int MINIMUN_RADIUS  = (int)getResources().getDimension(R.dimen.node_icon_minimun_distance);     //アイコン半径の最低ライン
        int radius = (int) ((mBaseNode.getScaleNodeBodyWidth() / 2f) + ADD_NODE_RADIUS);
        if (radius < MINIMUN_RADIUS) {
            //ノードとアイコンの距離が最低ラインよりも小さければ、最低サイズで設定
            radius = MINIMUN_RADIUS;
        }

        //Log.i("アイコン半径", "getScaleNodeBodyWidth" + v_baseNode.getScaleNodeBodyWidth());
        //Log.i("アイコン半径", "radius" + radius);
        //Log.i("アイコン半径 dimen", "node_icon_node_distance=" + getResources().getDimension(R.dimen.node_icon_add_distance));
        //Log.i("アイコン半径 dimen", "node_icon_minimun=" + getResources().getDimension(R.dimen.node_icon_minimun_distance));

        //アイコン設置初期角度
        final int START_ANGLE = 30;

        int count = 0;
        for (TooliconData toolIconData : iconViews) {

            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);

            //Log.i("アイコン", "半径 初期値=" + layoutParams.circleRadius + " 設定値=" + radius);

            //角度、半径を設定
            layoutParams.circleConstraint = v_center_id;
            layoutParams.circleAngle = START_ANGLE * (count + 1);
            layoutParams.circleRadius = radius;

            //アイコンビュー
            ImageButton ib = toolIconData.getIbIcon();
            ib.setLayoutParams(layoutParams);

            //※最終的な可視化はレイアウト確定後に行う
            //※この時点では、サイズが欲しいため領域確保のみ
            ib.setVisibility(INVISIBLE);

            //リスナーの設定
            setIconListener(toolIconData);

            //Log.i("アイコン", "角度=" + START_ANGLE * count);

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
            animation.setStartOffset(ANIM_OFFSET * count);
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
        int kind = mBaseNode.getNode().getKind();

        if (kind == NodeTable.NODE_KIND_ROOT) {
            iconViews.add(new TooliconData(TooliconData.CREATE_NODE, findViewById(R.id.ib_createNode)));
            iconViews.add(new TooliconData(TooliconData.CREATE_PICTURE_NODE, findViewById(R.id.ib_createPictureNode)));
            iconViews.add(new TooliconData(TooliconData.EDIT, findViewById(R.id.ib_edit)));
            iconViews.add(new TooliconData(TooliconData.DISPLAY_ALL_PICTURE, findViewById(R.id.ib_displayAllPicture)));
            iconViews.add(new TooliconData(TooliconData.HELP, findViewById(R.id.ib_help)));
            iconViews.add(new TooliconData(TooliconData.CLOSE, findViewById(R.id.ib_close)));

        } else if (kind == NodeTable.NODE_KIND_NODE) {
            iconViews.add(new TooliconData(TooliconData.CREATE_NODE, findViewById(R.id.ib_createNode)));
            iconViews.add(new TooliconData(TooliconData.CREATE_PICTURE_NODE, findViewById(R.id.ib_createPictureNode)));
            iconViews.add(new TooliconData(TooliconData.EDIT, findViewById(R.id.ib_edit)));
            iconViews.add(new TooliconData(TooliconData.DISPLAY_ALL_PICTURE, findViewById(R.id.ib_displayAllPicture)));
            iconViews.add(new TooliconData(TooliconData.DELETE, findViewById(R.id.ib_delete)));
            iconViews.add(new TooliconData(TooliconData.CHANGE_PARENT, findViewById(R.id.ib_changeParent)));
            //iconViews.add( new TooliconData( TooliconData.ICON_NEW_MAP, findViewById( R.id.ib_newMap )) );
            iconViews.add(new TooliconData(TooliconData.HELP, findViewById(R.id.ib_help)));
            iconViews.add(new TooliconData(TooliconData.CLOSE, findViewById(R.id.ib_close)));

        } else {
            iconViews.add(new TooliconData(TooliconData.EDIT, findViewById(R.id.ib_edit)));
            iconViews.add(new TooliconData(TooliconData.ADD_PICTURE, findViewById(R.id.ib_addPhoto)));
            iconViews.add(new TooliconData(TooliconData.DISPLAY_ALL_PICTURE, findViewById(R.id.ib_displayAllPicture)));
            iconViews.add(new TooliconData(TooliconData.DELETE, findViewById(R.id.ib_delete)));
            iconViews.add(new TooliconData(TooliconData.CHANGE_PARENT, findViewById(R.id.ib_changeParent)));
            iconViews.add(new TooliconData(TooliconData.HELP, findViewById(R.id.ib_help)));
            iconViews.add(new TooliconData(TooliconData.CLOSE, findViewById(R.id.ib_close)));
        }

        return iconViews;
    }


    /*
     * アイコンのリスナー設定
     */
    public void setIconListener(TooliconData toolIconData) {

        //アイコンビュー
        ImageButton ib = toolIconData.getIbIcon();
        //クリックリスナー
        OnClickListener listener = null;

        //アイコン毎にリスナーを生成
        int iconKind = toolIconData.getIconKind();
        switch (iconKind) {

            //--------------------------------------------------------
            // ノード新規生成
            //--------------------------------------------------------
            case TooliconData.CREATE_NODE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //アイコンが開かれたノードを親ノードとする
                        NodeTable parentNode = mBaseNode.getNode();

                        //階層上限に到達していれば、メッセージを出して終了
                        if( isReachUpperLimitHierarchy( parentNode ) ){
                            Toast.makeText(mMapActivity, getResources().getString(R.string.toast_reachUpperLimitHierarchy), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //ノード数上限に到達していれば、メッセージを出して終了
                        if (isReachUpperLimitNodeNum( parentNode.getPid(), NodeTable.NODE_KIND_NODE )) {
                            Toast.makeText(mMapActivity, getResources().getString(R.string.toast_reachUpperLimitNode), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //初期生成位置オフセット
                        int initRelativePos = (int)getResources().getDimension(R.dimen.init_relative_pos);
                        //スケールを考慮したノード半径
                        int radius = (int)(parentNode.getNodeView().getScaleWidth() / 2f);
                        //初期生成位置
                        int posX = (int) parentNode.getPosX() + radius + initRelativePos;
                        int posY = (int) parentNode.getPosY();

                        //ノードを生成
                        NodeTable newNode = new NodeTable(
                                "",
                                parentNode.getPidMap(),
                                parentNode.getPid(),
                                NodeTable.NODE_KIND_NODE,
                                posX,
                                posY
                        );

                        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
                        MapTable map = mapCommonData.getMap();

                        //カラーパターン設定
                        String[] colors = map.getDefaultColors();
                        newNode.setColorPattern(colors);
                        //影の有無を設定
                        newNode.setShadow(map.isShadow());

                        //ノードをマップに追加
                        BaseNode v_node = mMapActivity.drawNode(newNode);

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

                        //フォーカスさせる座標
                        //※新ノードが生成される絶対位置
                        int moveToX = (int) parentNode.getNodeView().getLeft() + radius + initRelativePos;
                        int moveToY = (int) parentNode.getNodeView().getTop();

                        //BottomSheetを開く（画面移動あり）
                        mMapActivity.openDesignBottomSheet(DesignBottomSheet.NODE, v_node, moveToX, moveToY);

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // ピクチャノード新規生成
            //--------------------------------------------------------
            case TooliconData.CREATE_PICTURE_NODE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        NodeTable node = mBaseNode.getNode();

                        //ノード数上限に到達していれば、メッセージを出して終了
                        if (isReachUpperLimitNodeNum( node.getPid(), NodeTable.NODE_KIND_PICTURE )) {
                            Toast.makeText(mMapActivity, getResources().getString(R.string.toast_reachUpperLimitPictureNode), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //権限の確認
                        //※API29のみ、WRITEを要求しないとimageにアクセスできない
                        String permissionStr = ( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                                ? Manifest.permission.WRITE_EXTERNAL_STORAGE
                                : Manifest.permission.READ_EXTERNAL_STORAGE);
                        int permission = ContextCompat.checkSelfPermission(mMapActivity, permissionStr);
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            //操作対象のノードを設定
                            mMapActivity.setToolIconNode(mBaseNode.getNode());
                            //権限付与
                            permissionsStorage(REQUEST_EXTERNAL_STORAGE_FOR_PICTURE_NODE);
                        } else {
                            //既に権限があれば、画面遷移
                            mMapActivity.transitionTrimming(node);
                        }

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // ノード編集
            //--------------------------------------------------------
            case TooliconData.EDIT:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        if( mBaseNode.getNode().getKind() == NodeTable.NODE_KIND_PICTURE ){
                            //ピクチャノードなら権限確認
                            //※API29のみ、WRITEを要求しないとimageにアクセスできない
                            String permissionStr = ( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                                    ? Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    : Manifest.permission.READ_EXTERNAL_STORAGE);
                            int permission = ContextCompat.checkSelfPermission(mMapActivity, permissionStr);
                            if (permission != PackageManager.PERMISSION_GRANTED) {

                                mMapActivity.setToolIconNode(mBaseNode.getNode());
                                //権限付与
                                permissionsStorage(REQUEST_EXTERNAL_EDIT_PICTURE_NODE);
                            } else {
                                //権限があれば、編集
                                mMapActivity.openEdit(mBaseNode);
                            }

                        } else {
                            //ピクチャノード以外は、編集処理へ
                            mMapActivity.openEdit(mBaseNode);
                        }

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // ギャラリー表示
            //--------------------------------------------------------
            case TooliconData.DISPLAY_ALL_PICTURE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //権限の確認
                        //※API29のみ、WRITEを要求しないとimageにアクセスできない
                        String permissionStr = ( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                                ? Manifest.permission.WRITE_EXTERNAL_STORAGE
                                : Manifest.permission.READ_EXTERNAL_STORAGE);
                        int permission = ContextCompat.checkSelfPermission(mMapActivity, permissionStr);
                        if (permission != PackageManager.PERMISSION_GRANTED) {

                            mMapActivity.setToolIconNode(mBaseNode.getNode());
                            //権限付与
                            permissionsStorage(REQUEST_EXTERNAL_STORAGE_FOR_GALLERY);
                        } else {
                            //権限があれば、ギャラリー画面へ
                            mMapActivity.transitionGallery(mBaseNode.getNode());
                        }

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // ノード削除
            //--------------------------------------------------------
            case TooliconData.DELETE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //本ノード配下のノード（本ノード含む）を全て取得する
                        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
                        mapCommonData.setDeleteNodes(mBaseNode.getNode().getPid());

                        Context context = getContext();

                        //削除確認ダイアログを表示
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setTitle(context.getString(R.string.alert_deleteNode_title))
                                .setMessage(context.getString(R.string.alert_deleteNode_message))
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
                                                ((ChildNode) mBaseNode).removeLayoutUnderSelf();

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

                        //メッセージ文は、Styleのフォントが適用されないため個別に設定
                        ((TextView) dialog.findViewById(android.R.id.message)).setTypeface(Typeface.SERIF);

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // 親ノードの変更
            //--------------------------------------------------------
            case TooliconData.CHANGE_PARENT:
                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //マップアクティビティを親ノード変更モードにする
                        mMapActivity.enableChangeParentMode(mBaseNode.getNode().getPid());

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // 新マップの作成
            //--------------------------------------------------------
            case TooliconData.NEW_MAP:
                //※現状なし
                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };

                break;

            //--------------------------------------------------------
            // 写真をノードに追加
            //--------------------------------------------------------
            case TooliconData.ADD_PICTURE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //権限の確認
                        //※API29のみ、WRITEを要求しないとimageにアクセスできない
                        String permissionStr = ( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                                ? Manifest.permission.WRITE_EXTERNAL_STORAGE
                                : Manifest.permission.READ_EXTERNAL_STORAGE);
                        int permission = ContextCompat.checkSelfPermission(mMapActivity, permissionStr);
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            //権限付与
                            permissionsStorage(REQUEST_EXTERNAL_STORAGE_FOR_ADD_PICTURE);
                        } else {
                            mMapActivity.transitionMediaStorage();
                        }

                        /*-- 自分のクローズ処理は行わない仕様とする --*/
                        /*-- 外部ストレージから戻ってきたとき、マップ画面側でどのピクチャノードがタッチされたのかを判別するため --*/
                    }
                };
                break;

            //--------------------------------------------------------
            // ヘルプ
            //--------------------------------------------------------
            case TooliconData.HELP:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");

                        //ヘルプダイアログの表示
                        showHelpIconDialog();

                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };
                break;

            //--------------------------------------------------------
            // アイコンクローズ
            //--------------------------------------------------------
            case TooliconData.CLOSE:

                listener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                        //ノードに持たせていた自分をクローズ
                        mBaseNode.closeIconView();
                    }
                };
                break;
        }

        //リスナーを設定
        ib.setOnClickListener(listener);
    }

    /*
     * ノード数上限到達チェック
     */
    private boolean isReachUpperLimitNodeNum(int nodePid, int nodeKind ) {

        //ノードリスト
        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //上限チェック
        return nodes.isUpperLimitNum( nodePid, nodeKind);
    }

    /*
     * 階層上限到達チェック
     */
    private boolean isReachUpperLimitHierarchy(NodeTable node) {
        //ノードリスト
        MapCommonData mapCommonData = (MapCommonData) mMapActivity.getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

        //上限チェック
        return nodes.isUpperLimitHierarchy( node );
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

    /*
     * ヘルプダイアログを表示する
     */
    private void showHelpIconDialog() {
        DialogFragment helpDialog = HelpDialog.newInstance(HelpDialog.HELP_KIND_ICON, mBaseNode.getNode().getKind());
        helpDialog.show( mMapActivity.getSupportFragmentManager(), "");
    }

    /*
     * 権限付与
     */
    private void permissionsStorage( int requestCode ) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //API23未満なら、許可ダイアログは不要
            return;
        }

        //許可ダイアログは必須
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };
        //API29のみ、WRITEを要求しないとimageにアクセスできないため、API29ならパーミッション上書き
        if( Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ){
            PERMISSIONS_STORAGE[0] = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        }
        //権限要求
        ActivityCompat.requestPermissions(
                mMapActivity,
                PERMISSIONS_STORAGE,
                requestCode
        );
    }
}

