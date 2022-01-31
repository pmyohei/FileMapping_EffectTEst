package com.mapping.filemapping;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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
        public static final String ICON_CREATE_NODE = "createNode";
        public static final String ICON_CREATE_PICTURE_NODE = "createPictureNode";
        public static final String ICON_EDIT = "edit";
        public static final String ICON_DISPLAY_ALL_PICTURE = "displayAllPicture";
        public static final String ICON_DELETE = "delete";
        public static final String ICON_CHANGE_PARENT = "changeParent";
        public static final String ICON_NEW_MAP = "newMap";
        public static final String ICON_CLOSE = "close";
        
        private final String iconKind;
        private final ImageButton ibIcon;

        public TooliconData(String kind, ImageButton ib ){
            iconKind = kind;
            ibIcon = ib;
        }

        public String getIconKind() {
            return iconKind;
        }
        public ImageButton getIbIcon() {
            return ibIcon;
        }
    }

    //次のアイコンの加算角度
    private final int ADD_ANGLE = 30;

    private BaseNode v_baseNode;
    private FrameLayout mfl_map;


    /*
     * コンストラクタ(New)
     */
    public ToolIconsView(Context context, BaseNode v_node) {
        super(context);
        v_baseNode = v_node;

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
     *
     */
    public void init() {

        //レイアウト生成
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.node_tool_icon, this, true);

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

/*
                        //Padding設定
                        //※アイコンの影が見切れるため、このタイミングで設定
                        int padding = (int)getResources().getDimension( R.dimen.node_icon_parent_padding);
                        ConstraintLayout cl_toolIcon = findViewById( R.id.cl_toolIcon );
                        Log.i("アイコン", "padding 初期値=" + cl_toolIcon.getPaddingLeft() + " 設定値=" + padding);
                        cl_toolIcon.setPadding( padding, padding, padding, padding );
*/

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
        //アニメーション開始オフセット
        final long ANIM_OFFSET = 25;

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
            ib.setVisibility(VISIBLE);

            //表示アニメーション
            Animation animation = AnimationUtils.loadAnimation(ib.getContext(), R.anim.show_tool_icon);
            animation.setStartOffset( ANIM_OFFSET * count );
            ib.startAnimation(animation);

            //リスナーの設定
            setIconListener(toolIconData);

            Log.i("アイコン", "角度=" + START_ANGLE * count);

            count++;
        }


        //Padding設定
        //※アイコンの影が見切れるため、このタイミングで設定
        int padding = (int)getResources().getDimension( R.dimen.node_icon_parent_padding);
        ConstraintLayout cl_toolIcon = findViewById( R.id.cl_toolIcon );
        Log.i("アイコン", "padding 初期値=" + cl_toolIcon.getPaddingLeft() + " 設定値=" + padding);
        cl_toolIcon.setPadding( padding, padding, padding, padding );


        //生成位置
        //※ツールアイコン表示後に行う
        setupShowPosition();
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
            iconViews.add( new TooliconData( TooliconData.ICON_CREATE_NODE, findViewById( R.id.ib_createNode ) ) );
            iconViews.add( new TooliconData( TooliconData.ICON_CREATE_PICTURE_NODE, findViewById( R.id.ib_createPictureNode ) ) );
            iconViews.add( new TooliconData( TooliconData.ICON_EDIT, findViewById( R.id.ib_edit ) ) );
            iconViews.add( new TooliconData( TooliconData.ICON_DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture ) ) );
            iconViews.add( new TooliconData( TooliconData.ICON_CLOSE, findViewById( R.id.ib_close ) ) );

        } else if (kind == NodeTable.NODE_KIND_NODE) {
            iconViews.add( new TooliconData( TooliconData.ICON_CREATE_NODE, findViewById( R.id.ib_createNode )) );
            iconViews.add( new TooliconData( TooliconData.ICON_CREATE_PICTURE_NODE, findViewById( R.id.ib_createPictureNode )) );
            iconViews.add( new TooliconData( TooliconData.ICON_EDIT, findViewById( R.id.ib_edit )) );
            iconViews.add( new TooliconData( TooliconData.ICON_DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture )) );
            iconViews.add( new TooliconData( TooliconData.ICON_DELETE, findViewById( R.id.ib_delete )) );
            iconViews.add( new TooliconData( TooliconData.ICON_CHANGE_PARENT, findViewById( R.id.ib_changeParent )) );
            //iconViews.add( new TooliconData( TooliconData.ICON_NEW_MAP, findViewById( R.id.ib_newMap )) );
            iconViews.add( new TooliconData( TooliconData.ICON_CLOSE, findViewById( R.id.ib_close )) );

        } else {
            iconViews.add( new TooliconData( TooliconData.ICON_EDIT, findViewById( R.id.ib_edit )) );
            iconViews.add( new TooliconData( TooliconData.ICON_DISPLAY_ALL_PICTURE, findViewById( R.id.ib_displayAllPicture )) );
            iconViews.add( new TooliconData( TooliconData.ICON_DELETE, findViewById( R.id.ib_delete )) );
            iconViews.add( new TooliconData( TooliconData.ICON_CHANGE_PARENT, findViewById( R.id.ib_changeParent )) );
            iconViews.add( new TooliconData( TooliconData.ICON_CLOSE, findViewById( R.id.ib_close )) );
        }

        return iconViews;
    }


    /*
     * アイコンのリスナー設定
     */
    public void setIconListener(TooliconData toolIconData ) {

        //アイコンビュー
        ImageButton ib = toolIconData.getIbIcon();
        //アイコン種別
        String iconKind = toolIconData.getIconKind();

        switch (iconKind) {
            case TooliconData.ICON_CREATE_NODE:

                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_CREATE_PICTURE_NODE:


                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_EDIT:


                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_DISPLAY_ALL_PICTURE:


                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_DELETE:


                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_CHANGE_PARENT:


                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_NEW_MAP:
                //※現状なし
                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });

                break;
            case TooliconData.ICON_CLOSE:

                ib.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Log.i("アイコン", "クリックされました");
                    }
                });
                break;
        }







    }











    /*
     *
     */
    public void tm(  ) {


    }
}

