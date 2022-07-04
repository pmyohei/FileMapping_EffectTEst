package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class EffectView extends View {

    /*
     * グラデーション座標
     */
    class GradationCoordinate {

        //--------------------------------
        // 座標最大値・進行度
        //--------------------------------
        int width;
        int height;

        //--------------------------------
        // グラデーション移動方向変更の閾値
        //--------------------------------
        final float STEP_CHANGE_INTERVAL = 0.25f;
        final float SHIFT_RIGHT = STEP_CHANGE_INTERVAL;
        final float SHIFT_DOWN = SHIFT_RIGHT + STEP_CHANGE_INTERVAL;
        final float SHIFT_LEFT = SHIFT_DOWN + STEP_CHANGE_INTERVAL;
        final float SHIFT_UP = SHIFT_LEFT + STEP_CHANGE_INTERVAL;

        //--------------------------------
        // グラデーション開始終了座標
        //--------------------------------
        float startX;
        float startY;
        float endX;
        float endY;

        /*
         * コンストラクタ
         */
        public GradationCoordinate( int width, int height){
            this.width = width;
            this.height = height;
        }

        /*
         * 進行度に応じた座標設定
         */
        public void setGradationCoordinate(float process){

            if (process <= SHIFT_RIGHT) {
                //-- 開始座標 --//
                //移動方向：右
                startX = width * (process / STEP_CHANGE_INTERVAL);
                startY = 0;
                //-- 終了座標 --//
                //移動方向：左
                endX = width * (1 - (process / STEP_CHANGE_INTERVAL));
                endY = height;

            } else if (process <= SHIFT_DOWN) {
                //-- 開始座標 --//
                //移動方向：下
                startX = width;
                startY = height * ((process - SHIFT_RIGHT) / STEP_CHANGE_INTERVAL);

                //-- 終了座標 --//
                //移動方向：上
                endX = 0;
                endY = height * (1 - ((process - SHIFT_RIGHT) / STEP_CHANGE_INTERVAL));

            } else if (process <= SHIFT_LEFT) {
                //-- 開始座標 --//
                //移動方向：左
                startX = width * (1 - (process - SHIFT_DOWN) / STEP_CHANGE_INTERVAL);
                startY = height;
                //-- 終了座標 --//
                //移動方向：右
                endX = width * ((process - SHIFT_DOWN) / STEP_CHANGE_INTERVAL);
                endY = 0;

            } else {
                //-- 開始座標 --//
                //移動方向：上
                startX = 0;
                startY = height * (1 - ((process - SHIFT_LEFT) / STEP_CHANGE_INTERVAL));
                //-- 終了座標 --//
                //移動方向：下
                endX = width;
                endY = height * ((process - SHIFT_LEFT) / STEP_CHANGE_INTERVAL);
            }
        }
    }


    //エフェクト形状
    private int mEffectShape;
    //エフェクトのPaintStyle
    private Paint.Style mPaintStyle;
    //ビュー関連
    private int mSize = 0;
    private int mColor = 0;
    private int mShadowColor;// = getResources().getColor(R.color.yellow);
    private ArrayList<Path> mPath = new ArrayList<>();
    private ArrayList<Paint> mPaint = new ArrayList<>();
    //グラデーション座標
    GradationCoordinate mGradationCoordinate = null;

    /*
     *　レイアウトから生成時用
     */
    /*
    public EffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }*/
    /*
     *　コードから生成時用
     */
    public EffectView(Context context, int shape, Paint.Style paintStyle) {
        super(context);
        init(shape, paintStyle);
    }

    /*
     * 初期設定
     */
    private void init(int shape, Paint.Style paintStyle) {
        //エフェクト形状の設定
        setEffectInfo(shape, paintStyle);

        //ランダム設定
        setRandomSize();
        setRandomColor();
    }


    /*
     * サイズのランダム設定
     */
    private void setRandomSize() {
        //サイズ範囲
        final int MAX_RANGE = getEffectRangeSize() + 1;
        final int MIN_SIZE = getEffectMinSize();

        //ランダムな値をサイズに適用
        Random random = new Random();
        int offset = random.nextInt(MAX_RANGE);
        mSize = MIN_SIZE + offset;
    }

    /*
     * 色のランダム設定
     */
    private void setRandomColor() {

        int color;
        int shadowColor;

        Random random = new Random();
        int i = random.nextInt(6);
        switch (i) {
            case 0:
                color = getResources().getColor(R.color.effect_right_yellow);
                shadowColor = getResources().getColor(R.color.effect_yellow);
                break;
            case 1:
                color = getResources().getColor(R.color.effect_right_red);
                shadowColor = getResources().getColor(R.color.effect_red);
                break;
            case 2:
                color = getResources().getColor(R.color.effect_right_blue);
                shadowColor = getResources().getColor(R.color.effect_blue);
                break;
            case 3:
                color = getResources().getColor(R.color.effect_right_green);
                shadowColor = getResources().getColor(R.color.effect_green);
                break;
            case 4:
                color = getResources().getColor(R.color.effect_right_pink);
                shadowColor = getResources().getColor(R.color.effect_pink);
                break;
            case 5:
                color = getResources().getColor(R.color.effect_right_purple);
                shadowColor = getResources().getColor(R.color.effect_purple);
                break;
            default:
                color = getResources().getColor(R.color.effect_right_yellow);
                shadowColor = getResources().getColor(R.color.effect_yellow);
                break;
        }

        //色情報の保持
        mColor = color;
        mShadowColor = shadowColor;
    }

    /*
     * Paint色設定
     */
    public void setPaintColor(Paint paint) {

        int colorType = 0;

        switch (colorType) {
            case 0:
                //単色
                paint.setColor(mColor);
                break;

            case 1:
                //グラデーション：LinearGradient：直線
                Shader gradient = new LinearGradient(
                        //グラデーション開始座標
                        0, 0,
                        //グラデーション終了座標
                        0, mSize,
                        mColor, Color.WHITE, Shader.TileMode.MIRROR);
                paint.setShader(gradient);

                break;

            case 2:
                //グラデーション：RadialGradient：放射状
                float halfSize = mSize / 2f;

                gradient = new RadialGradient(
                        //中心座標
                        halfSize, halfSize,
                        //半径
                        halfSize,
                        //Color.WHITE, getResources().getColor(R.color.pinkTest),
                        Color.WHITE, getResources().getColor(R.color.transparent_50_white),
                        //Color.WHITE, Color.WHITE,
                        //Color.WHITE, mColor,
                        Shader.TileMode.CLAMP);
                paint.setShader(gradient);

                break;
        }
    }

    /*
     * エフェクトの形状設定
     */
    public void setEffectInfo(int effectShape, Paint.Style paintStyle) {
        //エフェクト情報
        mEffectShape = effectShape;
        mPaintStyle = paintStyle;
        //描画情報クリア
        mPath.clear();
        mPaint.clear();
    }

    /*
     * 最小サイズに加算するランダム最大値の取得
     */
    private int getEffectRangeSize() {

        //--------------------------------
        // エフェクト形状に応じたサイズ加算範囲
        //--------------------------------
        switch (mEffectShape) {
            case MapTable.HEART_NORMAL:
            case MapTable.HEART_THIN:
            case MapTable.HEART_INFLATED:
            case MapTable.STAR:
            case MapTable.SPARKLE_SHORT:
            case MapTable.SPARKLE_SHIN:
            case MapTable.SPARKLE_LONG:
            case MapTable.SPARKLE_RANDOM:
            case MapTable.SPARCLE_CENTRAL_CIRCLE:
            case MapTable.FLOWER:
            case MapTable.SAKURA:
            case MapTable.CIRCLE:
                return 100;

            case MapTable.DOT:
            case MapTable.TRIANGLE:
            case MapTable.DIA:
                return 10;

            default:
                return 20;
        }
    }

    /*
     * 最小サイズの取得
     */
    private int getEffectMinSize() {

        //--------------------------------
        // エフェクト形状に応じた最小サイズ
        //--------------------------------
        switch (mEffectShape) {
            case MapTable.HEART_NORMAL:
            case MapTable.HEART_THIN:
            case MapTable.HEART_INFLATED:
            case MapTable.STAR:
            case MapTable.SPARKLE_SHORT:
            case MapTable.SPARKLE_SHIN:
            case MapTable.SPARKLE_LONG:
            case MapTable.SPARKLE_RANDOM:
            case MapTable.SPARCLE_CENTRAL_CIRCLE:
            case MapTable.FLOWER:
            case MapTable.SAKURA:
            case MapTable.CIRCLE:
                return 100;

            case MapTable.DOT:
            case MapTable.TRIANGLE:
            case MapTable.DIA:
                return 10;

            default:
                return 60;
        }
    }

    /*
     * エフェクトのPathを生成
     */
    private void createEffectShapePath(Canvas canvas) {

        //Pathが設定済みなら何もしない
        if (mPath.size() > 0) {
            return;
        }

        //--------------------------------
        // エフェクト形状に応じたPath設定
        //--------------------------------
        switch (mEffectShape) {
            case MapTable.HEART_NORMAL:
                mPath = createHeartNormal();
                break;

            case MapTable.HEART_THIN:
                mPath = createHeartThin();
                break;

            case MapTable.HEART_INFLATED:
                mPath = createHeartInflated();
                break;

            case MapTable.TRIANGLE:
                mPath = createTriangle();
                break;

            case MapTable.DIA:
                mPath = createDia();
                break;

            case MapTable.STAR:
                mPath = createStar();
                break;

            case MapTable.SPARKLE_SHORT:
                mPath = createSparkleShort();
                break;

            case MapTable.SPARKLE_SHIN:
                mPath = createSparkleShin();
                break;

            case MapTable.SPARKLE_LONG:
                mPath = createSparkleLong();
                break;

            case MapTable.SPARKLE_RANDOM:
                mPath = createSparkleRandom();
                break;

            case MapTable.SPARCLE_CENTRAL_CIRCLE:
                mPath = createCenterCircleSparkle(canvas);
                break;

            case MapTable.FLOWER:
                mPath = createFlower();
                break;

            case MapTable.SAKURA:
                mPath = createSakura();
                break;

            case MapTable.CIRCLE:
            case MapTable.DOT:
                mPath = createCircle();
                break;

            default:
                mPath = createSakura();
                break;
        }
    }

    /*
     * エフェクトのPaintを生成
     */
    private void createEffectShapePaint() {

        //Paint設定済みなら何もしない
        if (mPaint.size() > 0) {
            return;
        }

        //--------------------------------
        // エフェクト形状に応じたPath設定
        //--------------------------------
        switch (mEffectShape) {
            case MapTable.HEART_NORMAL:
            case MapTable.HEART_THIN:
            case MapTable.HEART_INFLATED:
            case MapTable.TRIANGLE:
            case MapTable.DIA:
            case MapTable.STAR:
            case MapTable.SPARKLE_SHORT:
            case MapTable.SPARKLE_SHIN:
            case MapTable.SPARKLE_LONG:
            case MapTable.SPARKLE_RANDOM:
            case MapTable.FLOWER:
            case MapTable.SAKURA:
            case MapTable.CIRCLE:
                //Paint単体生成
                mPaint = createCommonPaint();
                break;

            case MapTable.SPARCLE_CENTRAL_CIRCLE:
                //十字スパークル用Paintの生成
                mPaint = createCrossSparklePaint();
                break;

            default:
                //Paint単体生成
                mPaint = createCommonPaint();
                break;
        }
    }


    /*
     * Path生成
     *  ハート（ノーマル）
     */
    private ArrayList<Path> createHeartNormal() {
        //ビューサイズの半分の値
        final float halfSize = mSize / 2f;

        Path path = new Path();

        path.moveTo(halfSize, mSize);
        path.lineTo(0, halfSize);
        path.cubicTo(0, 0, halfSize, 0, halfSize, halfSize);
        path.cubicTo(halfSize, 0, mSize, 0, mSize, halfSize);
        path.lineTo(halfSize, mSize);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  ハート（細め）
     */
    private ArrayList<Path> createHeartThin() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;
        float quaterSize = mSize / 4f;

        Path path = new Path();

        path.moveTo(halfSize, mSize);
        path.cubicTo(0, halfSize, quaterSize, 0, halfSize, halfSize);
        path.cubicTo(halfSize + quaterSize, 0, mSize, halfSize, halfSize, mSize);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  ハート（膨らんだ形状）
     */
    private ArrayList<Path> createHeartInflated() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;
        //制御点
        float farOriCtrlPos = halfSize * 0.66f;             //制御点（原点から遠くの位置）
        float nearOriCtrlPos = halfSize * 0.33f;            //制御点（原点から近くの位置）
        float farCntCtrlPos = halfSize + farOriCtrlPos;     //制御点（中間地点から遠くの位置）
        float nearCntCtrlPos = halfSize + nearOriCtrlPos;   //制御点（中間地点から近くの位置）

        Path path = new Path();

        path.moveTo(halfSize, mSize);
        path.cubicTo(farOriCtrlPos, halfSize, nearOriCtrlPos, mSize, 0, halfSize);
        path.cubicTo(0, 0, halfSize, 0, halfSize, halfSize);
        path.cubicTo(halfSize, 0, mSize, 0, mSize, halfSize);
        path.cubicTo(farCntCtrlPos, mSize, nearCntCtrlPos, halfSize, halfSize, mSize);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  三角形
     */
    private ArrayList<Path> createTriangle() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        Path path = new Path();

        path.moveTo(halfSize, 0);
        path.lineTo(0, mSize);
        path.lineTo(mSize, mSize);
        path.lineTo(halfSize, 0);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  ダイヤ
     */
    private ArrayList<Path> createDia() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        Path path = new Path();

        path.moveTo(halfSize, 0);
        path.lineTo(0, halfSize);
        path.lineTo(halfSize, mSize);
        path.lineTo(mSize, halfSize);
        path.lineTo(halfSize, 0);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }


    /*
     * Path生成
     *  星
     */
    private ArrayList<Path> createStar() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        //描画情報
        int points = 5;                    //頂点数（普通の星のため、5つ設定）
        int startAngle = -90;              //90度の位置から頂点を描画
        float viewRadius = halfSize / 2f;  //基本半径
        float outerRadius = halfSize / 2f; //基本半径から頂点までの長さ
        float innerRadius = 0;             //基本半径から内側までの長さ

        //指定頂点を辿るPathを生成
        Path path = getSparklePath(points, startAngle, (viewRadius + outerRadius), (viewRadius - innerRadius));

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }


    /*
     * Path生成
     *  スパークル：短め
     */
    private ArrayList<Path> createSparkleShort() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        int points = 4;                                     //外側の頂点数（普通の星の場合は5つ）
        int startAngle = -90;                               //90度の位置から頂点を描画
        float viewRadius = halfSize / 4f;                  //基本半径
        float outerRadius = halfSize - (halfSize / 4f);   //基本半径に加算
        float innerRadius = 0;                              //基本半径から減算

        //指定頂点を辿るPathを生成
        Path path = getSparklePath(points, startAngle, (viewRadius + outerRadius), (viewRadius - innerRadius));

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  スパークル：細め
     */
    private ArrayList<Path> createSparkleShin() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        int startAngle = -90;                           //90度の位置から頂点を描画
        float viewRadius = halfSize / 8f;               //基本半径
        float outerRadius = halfSize - (halfSize / 8f); //基本半径に加算
        float innerRadius = 0;                          //基本半径から減算

        //頂点の描画
        Path path = getCrossShinSparkle(startAngle, (viewRadius + outerRadius), (viewRadius - innerRadius));

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  スパークル：不規則
     */
    private ArrayList<Path> createSparkleRandom() {

        int points = 9;                                     //外側の頂点数（普通の星の場合は5つ）
        int startAngle = -90;                               //90度の位置から頂点を描画
        float viewRadius = mSize / 8f;                      //基本半径
        float innerRadius = viewRadius * 0.6f;              //基本半径から減算

        //頂点の描画
        Path path = setVertexRandomLen(points, startAngle, viewRadius, (viewRadius - innerRadius));

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  スパークル：長め
     */
    private ArrayList<Path> createSparkleLong() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        Path path = new Path();

        final int CENTER_CIRCLE_RADIUS = 4;
        final int POS_DIFF = CENTER_CIRCLE_RADIUS / 2;

        //中央円
        path.addCircle(halfSize, halfSize, CENTER_CIRCLE_RADIUS, Path.Direction.CW);
        //上
        path.moveTo(halfSize - POS_DIFF, halfSize);
        path.lineTo(halfSize, 0);
        path.lineTo(halfSize + POS_DIFF, halfSize);
        //下
        path.lineTo(halfSize, mSize);
        path.lineTo(halfSize - POS_DIFF, halfSize);
        path.close();
        //左
        path.moveTo(halfSize, halfSize + POS_DIFF);
        path.lineTo(0, halfSize);
        path.lineTo(halfSize, halfSize - POS_DIFF);
        //右
        path.lineTo(mSize, halfSize);
        path.lineTo(halfSize, halfSize + POS_DIFF);
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *  スパークル：中央に円。十字／斜め十字。
     */
    private ArrayList<Path> createCenterCircleSparkle(Canvas canvas) {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        //--------------------------------------------
        // オブジェクトのPathを生成
        //--------------------------------------------
        //中心円の生成
        Path circlePath = new Path();
        circlePath.addCircle(halfSize, halfSize, halfSize / 3, Path.Direction.CW);
        //十字（細め）
        Path sparcleCrossPath = getCrossShinSparkle(-90, halfSize, halfSize * 0.06f);
        //斜め十字（細め）
        Path sparcleDiagonalPath = getCrossShinSparkle(-45, halfSize / 2, halfSize * 0.06f);


        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(circlePath);
        pathes.add(sparcleCrossPath);
        pathes.add(sparcleDiagonalPath);

        return pathes;
    }

    /*
     * Path生成
     *   花びら
     */
    private ArrayList<Path> createFlower() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        //各軸の位置（割合）
        float x_25 = mSize * 0.25f;
        float x_75 = mSize * 0.75f;
        float y_25 = mSize * 0.25f;
        float y_75 = mSize * 0.75f;

        Path path = new Path();
        path.moveTo(halfSize, halfSize);
        //上の花びら
        path.quadTo(x_25, y_25, halfSize, 0);       //左
        path.quadTo(x_75, y_25, halfSize, halfSize);    //右
        //左の花びら
        path.quadTo(x_25, y_25, 0, halfSize);       //上
        path.quadTo(x_25, y_75, halfSize, halfSize);    //下
        //下の花びら
        path.quadTo(x_25, y_75, halfSize, mSize);      //左
        path.quadTo(x_75, y_75, halfSize, halfSize);    //右
        //右の花びら
        path.quadTo(x_75, y_75, mSize, halfSize);      //下
        path.quadTo(x_75, y_25, halfSize, halfSize);    //上

        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *   桜の花びら
     */
    private ArrayList<Path> createSakura() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        //角度差分：基準の角度に対して、各頂点の角度差分
        final int LEFT_CTRL_ANGLE = -40;
        final int RIGHT_CTRL_ANGLE = 40;
        final int CENTER_ANGLE = 0;
        final int LEFT_TIP_ANGLE = -10;
        final int RIGHT_TIP_ANGLE = 10;

        //花びらの幅
        float petalWidth = mSize / 3f;
        //花びらの幅の半分
        float halfPetalWidth = petalWidth / 2f;

        //各点の高さ情報
        final float petalHeight = halfSize;
        final float innerHeight = halfSize - halfPetalWidth;
        final float ctrlHeight = halfSize / 2f;

        //描画中心座標
        final float cx = halfSize;
        final float cy = halfSize;

        //ビューの中心へ移動
        Path path = new Path();
        path.moveTo(cx, cy);

        //円の半径
        float halfPI = (float) Math.PI / 180f;

        //描画開始情報
        int startAngle = -90;     //90度の位置から頂点を描画
        int points = 5;           //花びらの数

        //各頂点間の角度
        float oneAngle = 360f / points;
        for (int i = 0; i < points; i++) {
            //描画する花びらの角度
            float angle = startAngle + (oneAngle * i);

            //--------------------------------------------
            // 各頂点・制御点のラジアン
            //--------------------------------------------
            //制御点（左・右）
            float leftCtrlRadians = (float) ((angle + LEFT_CTRL_ANGLE) * halfPI);
            float rightCtrlRadians = (float) ((angle + RIGHT_CTRL_ANGLE) * halfPI);
            //中央の切れ込み
            float centerRadians = (float) ((angle + CENTER_ANGLE) * halfPI);
            //花びら先端（左・右）
            float leftTipRadians = (float) ((angle + LEFT_TIP_ANGLE) * halfPI);
            float rightTipRadians = (float) ((angle + RIGHT_TIP_ANGLE) * halfPI);

            //--------------------------------------------
            // 各点の高さに対する、指定角度を反映した値を算出
            //--------------------------------------------
            //制御点（左）
            float tranceLeftCtrlX = (float) (ctrlHeight * Math.cos(leftCtrlRadians));
            float tranceLeftCtrlY = (float) (ctrlHeight * Math.sin(leftCtrlRadians));
            //制御点（右）
            float tranceRightCtrlX = (float) (ctrlHeight * Math.cos(rightCtrlRadians));
            float tranceRightCtrlY = (float) (ctrlHeight * Math.sin(rightCtrlRadians));
            //中央の切れ込み点
            float tranceInnerX = (float) (innerHeight * Math.cos(centerRadians));
            float tranceInnerY = (float) (innerHeight * Math.sin(centerRadians));
            //花びら選択（左）
            float tranceLeftTipX = (float) (petalHeight * Math.cos(leftTipRadians));
            float tranceLeftTipY = (float) (petalHeight * Math.sin(leftTipRadians));
            //花びら選択（右）
            float tranceRightTipX = (float) (petalHeight * Math.cos(rightTipRadians));
            float tranceRightTipY = (float) (petalHeight * Math.sin(rightTipRadians));

            //--------------------------------------------
            // 中心点を基準にした座標を計算
            //--------------------------------------------
            //制御点（左）の座標
            float leftCtrlPosX = cx + tranceLeftCtrlX;
            float leftCtrlPosY = cy + tranceLeftCtrlY;
            //制御点（右）の座標
            float rightCtrlPosX = cx + tranceRightCtrlX;
            float rightCtrlPosY = cy + tranceRightCtrlY;
            //中央の切れ込み点の座標
            float innerPosX = cx + tranceInnerX;
            float innerPosY = cy + tranceInnerY;
            //花びら選択（左）の座標
            float leftTipPosX = cx + tranceLeftTipX;
            float leftTipPosY = cy + tranceLeftTipY;
            //花びら選択（右）の座標
            float rightTipPosX = cx + tranceRightTipX;
            float rightTipPosY = cy + tranceRightTipY;

            //--------------------------------------------
            // 花びらを描画
            //--------------------------------------------
            //花びらの左側：下の先端　→　先端（左）
            path.quadTo(leftCtrlPosX, leftCtrlPosY, leftTipPosX, leftTipPosY);
            //内側の切れ込み（左）：先端（左）　→　中央の切れ込み
            path.lineTo(innerPosX, innerPosY);
            //内側の切れ込み（右）：中央の切れ込み　→　先端（右）
            path.lineTo(rightTipPosX, rightTipPosY);
            //花びらの右側：先端（右）　→　下の先端
            path.quadTo(rightCtrlPosX, rightCtrlPosY, cx, cy);
        }
        path.close();

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }

    /*
     * Path生成
     *   円
     */
    private ArrayList<Path> createCircle() {
        //ビューサイズの半分の値
        float halfSize = mSize / 2f;

        //円形
        Path path = new Path();
        path.addCircle( halfSize, halfSize, halfSize, Path.Direction.CW );

        //Pathリストに設定
        ArrayList<Path> pathes = new ArrayList<>();
        pathes.add(path);

        return pathes;
    }


    /*
     * スパークルのPath取得
     *   para1：頂点の数
     *   para2：頂点描画開始角度（右水平方向0度・時計回り）
     *   para3：外側の頂点までの長さ
     *   para4：内側の頂点までの長さ
     */
    private Path getSparklePath(int points, float startAngle, float outerDist, float innerDist) {

        //ビューサイズの半分の値
        final float halfSize = mSize / 2f;
        //中心座標
        final float centerX = halfSize;
        final float centerY = halfSize;
        //頂点の角度間隔
        final float oneAngle = 360f / points;
        //円の半径
        final float halfPI = (float) Math.PI / 180f;

        //パス
        Path path = new Path();

        //頂点の数だけ処理
        for (int i = 0; i < points; i++) {
            //--------------------
            // 角度情報
            //--------------------
            float angle = startAngle + (oneAngle * i);
            float radians = (float) (angle * halfPI);
            float halfRadians = (float) ((angle + (oneAngle / 2f)) * halfPI);

            //--------------------
            // 外側の頂点
            //--------------------
            //「頂点までの距離」に角度を反映
            float offsetX = (float) (outerDist * Math.cos(radians));
            float offsetY = (float) (outerDist * Math.sin(radians));
            //中心点を基準に頂点の座標を算出
            float x = centerX + offsetX;
            float y = centerY + offsetY;

            //--------------------
            // Pathに外側頂点を反映
            //--------------------
            if (i == 0) {
                //移動
                path.moveTo(x, y);
            } else {
                //ライン描画：内側の頂点　→　外側の頂点
                path.lineTo(x, y);
            }

            //--------------------
            // 内側の頂点
            //--------------------
            //「頂点までの距離」に角度を反映
            offsetX = (float) (innerDist * Math.cos(halfRadians));
            offsetY = (float) (innerDist * Math.sin(halfRadians));
            //中心点を基準に頂点の座標を算出
            x = centerX + offsetX;
            y = centerY + offsetY;

            //ライン描画：外側の頂点　→　内側の頂点
            path.lineTo(x, y);
        }

        return path;
    }

    /*
     * スパークルのPath取得
     * 　十字で細さの指定が可能なスパークルPathを生成する。
     *   para1：頂点描画開始角度（右水平方向0度・時計回り）
     *   para2：外側の頂点までの長さ
     *   para3：内側の頂点までの長さ ※本値が小さい程、細いスパークルとなる
     */
    private Path getCrossShinSparkle(float startAngle, float outerDist, float innerDist) {
        //ビューサイズの半分の値
        final float halfSize = mSize / 2f;
        //頂点数
        final int VERTEX_NUM = 4;
        //頂点数
        final int INNER_VERTEX_ANGLE_OFFSET = 45;
        //中心座標
        final float centerX = halfSize;
        final float centerY = halfSize;
        //頂点間の角度間隔
        final float oneAngle = 360f / VERTEX_NUM;
        //円の半径
        final float halfPI = (float) Math.PI / 180f;

        //パス
        Path path = new Path();

        //頂点の数だけ処理
        for (int i = 0; i < VERTEX_NUM; i++) {
            //--------------------
            // 角度情報
            //--------------------
            float angle = startAngle + (oneAngle * i);
            float radians = angle * halfPI;

            //--------------------
            // 外側の頂点
            //--------------------
            //外側の頂点の座標を算出
            float offsetX = (float) (outerDist * Math.cos(radians));
            float offsetY = (float) (outerDist * Math.sin(radians));
            float x = centerX + offsetX;
            float y = centerY + offsetY;

            //--------------------
            // Pathに外側頂点を反映
            //--------------------
            if (i == 0) {
                //初めは移動
                path.moveTo(x, y);
            } else {
                //ライン描画
                path.lineTo(x, y);
            }

            //--------------------
            // 内側の頂点
            //--------------------
            //内側の頂点は、外側の頂点から45度ずらした位置とする
            float angleIn = angle + INNER_VERTEX_ANGLE_OFFSET;
            float radiansIn = angleIn * halfPI;
            //内側頂点の座標を算出
            offsetX = (float) (innerDist * Math.cos(radiansIn));
            offsetY = (float) (innerDist * Math.sin(radiansIn));
            x = centerX + offsetX;
            y = centerY + offsetY;

            //ライン描画
            path.lineTo(x, y);
        }

        return path;
    }

    /*
     * スパークルのPath取得
     *   スパークルの長さはランダムとする
     *   para1：頂点の数
     *   para2：頂点描画開始角度（右水平方向0度・時計回り）
     *   para3：外側の頂点までの長さ
     *   para4：内側の頂点までの長さ
     */
    private Path setVertexRandomLen(int points, float startAngle, float viewRadius, float innerDist) {
        //ビューサイズの半分の値
        final float halfSize = mSize / 2f;
        //中心座標
        final float centerX = halfSize;
        final float centerY = halfSize;
        //頂点の角度間隔
        final float oneAngle = 360f / points;
        //円の半径
        final float halfPI = (float) Math.PI / 180f;

        //光の長さの範囲
        int range = (int) halfSize * 100;
        Random random = new Random();

        //Path
        Path path = new Path();

        //--------------------
        // 頂点分Path情報を設定
        //--------------------
        for (int i = 0; i < points; i++) {

            //--------------------
            // 角度情報
            //--------------------
            float angle = startAngle + (oneAngle * i);
            float radians = angle * halfPI;
            float halfRadians = (angle + (oneAngle / 2f)) * halfPI;

            //光の長さをランダムに生成
            float outerRadius = random.nextInt(range + 1) / 100f;

            //--------------------
            // 外側の頂点
            //--------------------
            float offsetX = (float) ((viewRadius + outerRadius) * Math.cos(radians));
            float offsetY = (float) ((viewRadius + outerRadius) * Math.sin(radians));
            float x = centerX + offsetX;
            float y = centerY + offsetY;

            //--------------------
            // Pathに外側頂点を反映
            //--------------------
            if (i == 0) {
                //初めは移動
                path.moveTo(x, y);
            } else {
                //ライン描画
                path.lineTo(x, y);
            }

            //--------------------
            // 内側の頂点
            //--------------------
            offsetX = (float) (innerDist * Math.cos(halfRadians));
            offsetY = (float) (innerDist * Math.sin(halfRadians));
            x = centerX + offsetX;
            y = centerY + offsetY;

            //ライン描画
            path.lineTo(x, y);
        }
        path.close();

        return path;
    }

    /*
     * Paint生成
     *   共通のPaint情報設定。設定するPaintは1つのみ。
     */
    private ArrayList<Paint> createCommonPaint() {

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShadowLayer(mSize / 8f, 0, 0, mShadowColor);

        //Paintの色情報設定
        setPaintColor(paint);
        paint.setAlpha(0xFF);   //※色設定後に行う必要がある

        //----------------------------------------
        // Paint.Styleの適用
        //----------------------------------------
        if( mPaintStyle == Paint.Style.FILL ){
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4);
        }

        ArrayList<Paint> paints = new ArrayList<>();
        paints.add(paint);
        return paints;
    }


    /*
     * Paint生成
     *  　十字スパークルPaint生成
     */
    private ArrayList<Paint> createCrossSparklePaint() {

        //ビューサイズの半分の値
        final float halfSize = mSize / 2f;

        //--------------------------------------------
        // グラデーションの生成
        //--------------------------------------------
        //中心円のグラデーション
        Shader circleGradient = new RadialGradient(
                halfSize, halfSize,         //中心座標
                halfSize / 3,         //半径
                getResources().getColor(R.color.transparent_50_white), Color.TRANSPARENT,
                Shader.TileMode.CLAMP);

        //十字のグラデーション
        Shader sparkleGradient = new RadialGradient(
                halfSize, halfSize,         //中心座標
                halfSize,                   //半径
                Color.WHITE, Color.TRANSPARENT,
                Shader.TileMode.CLAMP);

        //--------------------------------------------
        // Paint情報生成
        //--------------------------------------------
        //中心円
        Paint CirclePaint = new Paint();
        CirclePaint.setAntiAlias(true);
        CirclePaint.setAlpha(0xFF);
        CirclePaint.setStyle(Paint.Style.FILL);
        CirclePaint.setShadowLayer(mSize / 8f, 0, 0, Color.WHITE);
        CirclePaint.setShader(circleGradient);

        //十字／斜め十字
        Paint sparklePaint = new Paint();
        sparklePaint.setAntiAlias(true);
        sparklePaint.setAlpha(0xFF);
        sparklePaint.setStyle(Paint.Style.FILL);
        sparklePaint.setShadowLayer(mSize / 8f, 0, 0, Color.WHITE);
        sparklePaint.setShader(sparkleGradient);

        //--------------------------------------------
        // Paintリストに追加
        //--------------------------------------------
        ArrayList<Paint> paints = new ArrayList<>();
        paints.add(CirclePaint);         //中心円
        paints.add(sparklePaint);        //十字
        paints.add(sparklePaint);        //斜め十字

        return paints;
    }

    /*
     * エフェクトオブジェクトの描画
     */
    private void drawEffectPath(Canvas canvas) {

        int pathNum = mPath.size();

        //--------------------
        // Pathなし
        //--------------------
        //フェールセーフとして何もしない
        if (pathNum == 0) {
            return;
        }

        //--------------------
        // Pathが1つのみ
        //--------------------
        if (pathNum == 1) {
            canvas.drawPath(mPath.get(0), mPaint.get(0));
            return;
        }

        //--------------------
        // Pathが複数あり
        //--------------------
        //Pathに対応するPaintがなければ何もしない（フェールセーフ）
        if (pathNum != mPaint.size()) {
            return;
        }

        //Pathの数だけ保存
        for (int i = 0; i < pathNum; i++) {
            //描画
            Path path = mPath.get(i);
            Paint paint = mPaint.get(i);
            canvas.drawPath(path, paint);
            //Path毎に保存
            canvas.save();
        }
        //保存したPathを全て復元
        canvas.restore();

    }


    /*
     * エフェクトアニメーションプロパティ
     * 　回転　android:propertyName="spinEffect"
     */
    public void setSpinEffect(float rotate) {
        //角度に反映
        setRotation(rotate);
    }


    /*
     * エフェクトアニメーション
     * 　枠線グラデーションを回っているように見せるアニメーション
     *   para1：アニメーション進行度（0.0～1.0）
     *
     *   ！特記事項！
     * 　　　本メソッドはAnimationクラスの「applyTransformation」からコールされる
     */
    public void setStrokeGradationEffect(float process) {

        //----------------------------------------
        // 進行度に応じたグラデーション座標を計算
        //----------------------------------------
        if( mGradationCoordinate == null ){
            //未生成なら生成
            mGradationCoordinate = new GradationCoordinate(mSize, mSize);
        }
        mGradationCoordinate.setGradationCoordinate( process );

        //----------------------------------------
        // グラデーション生成
        //----------------------------------------
        Shader gradient = new LinearGradient(
                mGradationCoordinate.startX, mGradationCoordinate.startY,             //グラデーション開始座標
                mGradationCoordinate.endX, mGradationCoordinate.endY,                 //グラデーション終了座標
                Color.RED,
                Color.WHITE,
                Shader.TileMode.MIRROR);

        //----------------------------------------
        // グラデーションをPaintに適用
        //----------------------------------------
        if( mPaint.size() == 0 ){
            //Paintが生成されていないタイミングであれば、何もしない
            return;
        }
        Paint paint = mPaint.get(0);
        paint.setShader(gradient);
    }


    /*
     * onDraw
     *   エフェクトの描画
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //エフェクト形状の設定
        createEffectShapePath( canvas );
        //エフェクト描画Paintの設定
        createEffectShapePaint();
        //エフェクトPathの描画
        drawEffectPath( canvas );
    }

    /*
     * onMeasure
     *   本ビューへのサイズ反映を目的にオーバーライド
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //設定サイズを保持していれば、反映する
        if (mSize > 0) {
            setMeasuredDimension(mSize, mSize);
        }
    }


}
