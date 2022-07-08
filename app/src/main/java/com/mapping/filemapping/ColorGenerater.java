package com.mapping.filemapping;

import android.graphics.Color;
import android.util.Log;

import java.util.Random;

/*
 * カラー生成
 */
public class ColorGenerater {

    //適合色角度リスト
    static float[] matchAngles = {45, 90, 105, 120, 135, 180};

    public ColorGenerater(){}

    /*
     * ランダムなHSVを取得
     */
    static public float[] createRandomHSV() {
        //範囲上限・最小値
        final int RANGE_H = 3601;   //0.0 - 360.0
        final int RANGE_S = 61;     //0.00 - 0.60
        final int RANGE_V = 31;     //0.70 - 1.00  ※最小値加算した場合
        final float MIN_V = 0.7f;

        //色相・彩度・明度
        float[] hsv = new float[3];

        Random random = new Random();
        hsv[0] = random.nextInt(RANGE_H) / 10f;
        hsv[1] = random.nextInt(RANGE_S) / 100f;
        hsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //Log.i("createRandomHSV", "色相=" + hsv[0] + " 彩度=" + hsv[1] + " 明度=" + hsv[2]);

        return hsv;
    }

    /*
     * ベース色に適合する色をランダムで取得
     */
    static public float[] createMatchingHSV( float[] baseHSV ) {
        //色相・彩度・明度
        float[] pairHsv = new float[3];

        //------------------------
        // 彩度・明度
        //------------------------
        //範囲上限・最小値
        final int RANGE_S = 61;     //0.00 - 0.60
        final int RANGE_V = 31;     //0.70 - 1.00  ※最小値加算した場合
        final float MIN_V = 0.7f;
        //彩度／明度はランダム
        Random random = new Random();
        pairHsv[1] = (random.nextInt(RANGE_S) / 100f);
        pairHsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //------------------------
        // 色相
        //------------------------
        //回転方向
        int dir = random.nextInt(2);
        //適合色の角度
        int angleNum = matchAngles.length;
        int anglePos = random.nextInt(angleNum);
        final float ANGLE = matchAngles[anglePos];

        //指定カラーの指定角度分回転させた色を取得
        pairHsv[0] = getAnglePositionColor( baseHSV[0], ANGLE, dir );

        //Log.i("補色", "色相=" + pairHsv[0] + " 彩度=" + pairHsv[1] + " 明度=" + pairHsv[2]);
        return pairHsv;
    }

    /*
     * 指定されたHSV値に対する補色を取得
     */
    private int getComplementaryColor(float[] hsv) {
        //範囲上限
        final int RANGE_S = 61;     //0.00 - 0.50
        final int RANGE_V = 21;     //0.80 - 1.00  ※最小値加算した場合
        //最小値
        final float MIN_V = 0.8f;
        //色相・彩度・明度
        float[] pairHsv = new float[3];

        //補色の色相を取得
        final float COMPLEMENTARY = 180f;
        if (hsv[0] + COMPLEMENTARY > 360f) {
            pairHsv[0] = hsv[0] - COMPLEMENTARY;
        } else {
            pairHsv[0] = hsv[0] + COMPLEMENTARY;
        }

        //彩度／明度はランダム
        Random random = new Random();
        pairHsv[1] = (random.nextInt(RANGE_S) / 100f);
        pairHsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //Log.i("補色", "色相=" + pairHsv[0] + " 彩度=" + pairHsv[1] + " 明度=" + pairHsv[2]);

        return Color.HSVToColor(pairHsv);
    }

    /*
     * 指定されたHSV値に対する15度隣の色を取得
     */
    private int getAnalogyColor(float[] hsv ){
        //範囲上限
        final int RANGE_S = 51;     //0.00 - 0.50
        final int RANGE_V = 21;     //0.80 - 1.00  ※最小値加算した場合
        //最小値
        final float MIN_V = 0.8f;
        //色相・彩度・明度
        float[] pairHsv = new float[3];

        //どっち側の色相取るか
        Random random = new Random();
        int dir = random.nextInt(2);
        final float ANGLE = 15f;
        pairHsv[0] = getAnglePositionColor( hsv[0], ANGLE, dir );

        //彩度／明度はランダム
        pairHsv[1] = random.nextInt(RANGE_S) / 100f;
        pairHsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //Log.i("15度の隣", "色相=" + pairHsv[0] + " 彩度=" + pairHsv[1] + " 明度=" + pairHsv[2]);

        return Color.HSVToColor(pairHsv);
    }

    /*
     * 指定されたHSV値に対する4等分した時の隣の色を取得
     */
    private int getIntermediateColor(float[] hsv ){
        //範囲上限
        final int RANGE_S = 51;     //0.00 - 0.50
        final int RANGE_V = 21;     //0.80 - 1.00  ※最小値加算した場合
        //最小値
        final float MIN_V = 0.8f;
        //色相・彩度・明度
        float[] pairHsv = new float[3];

        //どっち側の色相取るか
        Random random = new Random();
        int dir = random.nextInt(2);
        final float ANGLE = 45f;
        pairHsv[0] = getAnglePositionColor( hsv[0], ANGLE, dir );

        //彩度／明度はランダム
        pairHsv[1] = (random.nextInt(RANGE_S) / 100f);
        pairHsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //Log.i("4分割の隣", "色相=" + pairHsv[0] + " 彩度=" + pairHsv[1] + " 明度=" + pairHsv[2]);

        return Color.HSVToColor(pairHsv);
    }

    /*
     * 指定されたHSV値に対する3等分した時の隣の色を取得
     */
    private int getOpornentColor(float[] hsv ){
        //範囲上限
        final int RANGE_S = 51;     //0.00 - 0.50
        final int RANGE_V = 21;     //0.80 - 1.00  ※最小値加算した場合
        //最小値
        final float MIN_V = 0.8f;
        //色相・彩度・明度
        float[] pairHsv = new float[3];

        //どちら側の色相取るか
        Random random = new Random();
        int dir = random.nextInt(2);
        final float ANGLE = 120f;
        pairHsv[0] = getAnglePositionColor( hsv[0], ANGLE, dir );

        //彩度／明度はランダム
        pairHsv[1] = random.nextInt(RANGE_S) / 100f;
        pairHsv[2] = (random.nextInt(RANGE_V) / 100f) + MIN_V;

        //Log.i("3分割の隣", "色相=" + pairHsv[0] + " 彩度=" + pairHsv[1] + " 明度=" + pairHsv[2]);

        return Color.HSVToColor(pairHsv);
    }

    /*
     * 基準の色に対して、指定角度回転された位置にある色を取得
     *   para1:基準色
     *   para2:取得する角度
     *   para3:回転方向
     */
    private static float getAnglePositionColor(float baseColor, float angle, int direction){

        float positionColor;
        if( direction == 0 ){
            positionColor = ( (baseColor + angle) > 360f ? baseColor - angle : baseColor + angle );
        } else {
            positionColor = ( (baseColor - angle) < 0f ? baseColor + angle : baseColor - angle );
        }

        return positionColor;
    }


}
