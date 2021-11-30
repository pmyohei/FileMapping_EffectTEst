package com.mapping.filemapping;

/*
 * マップ情報管理
 *   表示中マップの情報を管理する。
 *   表示するマップは1つだけであるため、シングルトンとする。
 */
public class MapInfoManager {

    //ピンチ操作後のビュー間の距離の比率
    private static float pinchDistanceRatioX = 1.0f;
    private static float pinchDistanceRatioY = 1.0f;

    /*
     * コンストラクタ
     */
    private MapInfoManager(){
    }

    /*
     * インスタンス取得
     *   para1：初期化指定
     */
    public static MapInfoManager getInstance( boolean init ) {

        if( init ){
            //初期化指定ありなら、比率を初期化
            pinchDistanceRatioX = 1.0f;
            pinchDistanceRatioY = 1.0f;
        }

        return MapInfoHolder.INSTANCE;
    }

    /*
     * Configクラスの唯一のインスタンスを保持する内部クラス
     */
    public static class MapInfoHolder {
        /* 唯一のインスタンス */
        private static final MapInfoManager INSTANCE = new MapInfoManager();
    }


    /*
     * ピンチ操作後のビュー間の距離の比率を設定
     */
    public void setPinchDistanceRatio( float x, float y ){
        pinchDistanceRatioX = x;
        pinchDistanceRatioY = y;
    }

    /*
     * ピンチ操作後のビュー間の距離の比率を取得
     */
    public float getPinchDistanceRatioX(){
        return pinchDistanceRatioX;
    }
    public float getPinchDistanceRatioY(){
        return pinchDistanceRatioY;
    }



}
