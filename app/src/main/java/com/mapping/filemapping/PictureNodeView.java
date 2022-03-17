package com.mapping.filemapping;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;

public class PictureNodeView extends ChildNode implements Serializable {

    //サムネイル
    private PictureTable mThumbnail;

    /*
     * コンストラクタ
     */
    @SuppressLint("ClickableViewAccessibility")
    public PictureNodeView(Context context, NodeTable node, PictureTable thumbnail) {
        //super(context, node, R.layout.node_outside);
        super(context, node, R.layout.picture_node);

        //サムネイル写真
        mThumbnail = thumbnail;
        //初期化
        initNode();
    }

    /*
     * 初期化処理
     */
    private void initNode() {

        //ノードに画像を設定
        findViewById(R.id.iv_node).post(()-> {
            //Log.i("サイズ確定", "post ノードサイズ=" + findViewById(R.id.iv_node).getWidth());
            setThumbnail();
        });
    }

    /*
     * ノードに画像を設定
     */
    private void setThumbnail() {
        //ビットマップ設定
        setBitmap(mThumbnail);
    }

    /*
     * ノード画像の更新
     */
    public void updateThumbnail(PictureTable thumbnail) {
        //ノードに画像を設定
        mThumbnail = thumbnail;
        setBitmap(thumbnail);
    }

    /*
     * サムネイル画像の状態チェック
     *   サムネイル画像が端末からなくなっていれば、無効画像を設定する
     */
    public void checkStateThumbnail() {
        File file = new File( mThumbnail.getPath() );
        if( !file.isFile() ){
            //画像が端末から削除されていれば、再設定
            setBitmap(mThumbnail);
        }
    }

    /*
     * ノードに画像を設定
     *   ※本処理は、ノードレイアウトが確定してから（サイズが確定してから）コールすること
     */
    private void setBitmap(PictureTable thumbnail) {
        //画像ビュー
        ImageView iv_node = findViewById(R.id.iv_node);
        //path
        String path = ( (thumbnail == null) ? "": thumbnail.getPath() );

        //別のマップのサムネイルがキャッシュされている可能性があるため、キャッシュを削除する
        //※これをしないと、別のマップに同じサムネイルがあったとき、そのサムネイル情報で表示されてしまう
        Picasso.get().invalidate( new File(path) );

        //画像割り当て
        //※fit()ではなく、一定値を指定したresize()を使用
        // fit()だと、ノードサイズが小さく、解像度が下がるため。
        // ある程度の解像度を確保するためにresize()を使用する。
        // 仮にマップ画面が重くなる場合は、このリサイズ値を見直す
        Picasso.get()
                .load( new File(path) )
                .resize( ThumbnailTransformation.RESIZE, ThumbnailTransformation.RESIZE)
                .transform( new ThumbnailTransformation( thumbnail, iv_node.getWidth() ) )
                .error(R.drawable.ic_no_image)
                .into( iv_node );
    }

    /*
     * ノード枠線サイズの設定
     */
    @Override
    public void setBorderSize( int thick ) {
        //枠サイズを設定
        ((ShapeableImageView)findViewById( R.id.iv_node )).setStrokeWidth( thick );

        mNode.setBorderSize( thick );
    }

    /*
     * ノード枠色の設定
     */
    @Override
    public void setBorderColor( String color ) {

        ColorStateList colorState = new ColorStateList(
            new int[][] {
                    new int[]{ android.R.attr.state_checked},
                    new int[]{ -android.R.attr.state_checked},
            },
            new int[] {
                    Color.parseColor( color ),
                    Color.parseColor( color ),
            }
        );

        //枠色を設定
        ((ShapeableImageView)findViewById( R.id.iv_node )).setStrokeColor( colorState );

        mNode.setBorderColor( color );
    }

    /*
     * ノードの形を円形にする
     */
    @Override
    public void setShapeCircle() {

        ShapeableImageView iv_node = findViewById(R.id.iv_node);
        //円形
        ShapeAppearanceModel.Builder builder
                = ShapeAppearanceModel.builder( iv_node.getContext(), R.style.circleImageView, 0 );
        //適用
        iv_node.setShapeAppearanceModel( builder.build() );
    }

    /*
     * ノードの形を四角（角丸）にする
     */
    @Override
    public void setShapeSquare() {

        ShapeableImageView iv_node = findViewById(R.id.iv_node);
        //角丸
        ShapeAppearanceModel.Builder builder
                = ShapeAppearanceModel.builder( iv_node.getContext(), R.style.roundedCornersImageView, 0 );
        //適用
        iv_node.setShapeAppearanceModel( builder.build() );
    }

}
