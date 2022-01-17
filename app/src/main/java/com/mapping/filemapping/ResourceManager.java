package com.mapping.filemapping;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

/*
 * 共通リソース管理
 */
public class ResourceManager {

    //URIパス
    public static final String URI_PATH = "content://com.android.providers.media.documents/document/image%";

    //URI識別子分割用の文字列
    //URI 例)content://com.android.providers.media.documents/document/image%3A27
    public static final String URI_SPLIT = "image%";

    /* 画面遷移-キー */
    public static final String KEY_MAPID = "MapID";
    public static final String KEY_CREATED_NODE = "CreatedNode";
    public static final String KEY_UPDATED_NODE = "UpdatedNode";
    public static final String KEY_THUMBNAIL = "thumbnail";

    //serialVersionUID
    public static final long SERIAL_VERSION_UID_NODE_TABLE = 1L;
    public static final long SERIAL_VERSION_UID_NODE_VIEW  = 2L;

    //ノード初期生成位置（親ノードからのオフセット）
    public static final int POS_NODE_INIT_OFFSET = 120;

    //ノード生成ダイアログの表示領域の割合
    public static final float NODE_CREATE_DIALOG_RATIO = 0.5f;

    /*
     * 本アプリケーション内のフォントリスト
     */
    public static List<Typeface> getAlphabetFonts(Context context){

        List<Typeface> fonts = new ArrayList<>();
        fonts.add( ResourcesCompat.getFont(context, R.font.luxurious_roman_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.roboto_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.the_nautigal_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.dongle_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.oswald_variable_font_wght) );
        fonts.add( ResourcesCompat.getFont(context, R.font.mochiy_pop_p_one_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.moon_dance_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.josefin_sans_variable_font_wght) );

        return fonts;
    }

    /*
     * 本アプリケーション内のフォントリスト
     */
    public static List<Typeface> getJapaneseFonts(Context context){

        List<Typeface> fonts = new ArrayList<>();
        fonts.add( ResourcesCompat.getFont(context, R.font.ipaexm) );
        fonts.add( ResourcesCompat.getFont(context, R.font.ipaexg) );
        fonts.add( ResourcesCompat.getFont(context, R.font.hannari_mincho_regular) );
        fonts.add( ResourcesCompat.getFont(context, R.font.senobi_gothic_medium) );
        fonts.add( ResourcesCompat.getFont(context, R.font.jk_maru_gothic_m) );
        fonts.add( ResourcesCompat.getFont(context, R.font.pixel_mplus10_regular) );

        return fonts;
    }
}
