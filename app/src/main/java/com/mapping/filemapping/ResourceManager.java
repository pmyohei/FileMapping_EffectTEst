package com.mapping.filemapping;

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
    public static final String KEY_URI = "uri";



    //serialVersionUID
    public static final long SERIAL_VERSION_UID_NODE_TABLE = 1L;
    public static final long SERIAL_VERSION_UID_NODE_VIEW  = 2L;

    //ノード初期生成位置（親ノードからのオフセット）
    public static final int POS_NODE_INIT_OFFSET = 120;
}
