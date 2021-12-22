package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NodeEntryActivity extends AppCompatActivity {

    /*-- 定数 --*/
    /* 画面遷移-レスポンスコード */
    public static final int RESULT_CREATED = 100;
    public static final int RESULT_EDITED  = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_information);

        //遷移元からの情報
        Intent intent = getIntent();
        boolean isCreate = intent.getBooleanExtra( MapActivity.INTENT_KIND_CREATE, false );

        //ポジティブボタン
        Button bt_positive = findViewById(R.id.bt_positive);

        //新規生成の場合
        if( isCreate ){
            //遷移元からの情報
            //★マップIDは共通情報にする
            int mapPid          = intent.getIntExtra(MapActivity.INTENT_MAP_PID, 0);
            int selectedNodePid = intent.getIntExtra(MapActivity.INTENT_NODE_PID, 0);

            //ノード生成
            bt_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ノード名空チェック
                    String nodeName = ((EditText)findViewById(R.id.et_nodeName)).getText().toString();
                    if( nodeName.isEmpty() ){
                        //空なら、メッセージ出力して終了
                        //★

                        return;
                    }

                    //マップ共通データ
                    MapCommonData mapCommonData = (MapCommonData)getApplication();
                    NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

                    //ノード名重複チェック
                    if( nodes.hasSameNodeNameAtParent(selectedNodePid, nodeName) ){
                        //既に同じノード名があるなら、メッセージ出力して終了
                        //★
                        Log.i("NodeInformationActivity", "新規生成　ノード名重複を検知");
                        return;
                    }

                    //ノード初期位置を親ノードの中心位置から一定の距離離した位置にする
                    NodeTable parentNode = nodes.getNode( selectedNodePid );
                    int posX = (int)parentNode.getCenterPosX() + ResourceManager.POS_NODE_INIT_OFFSET;
                    int posY = (int)parentNode.getCenterPosY();

                    //ノードを生成
                    //★
                    //※本ノード自体のpidはこの時点では確定していないため、DB処理完了後に設定
                    NodeTable newNode = new NodeTable();
                    newNode.setNodeName(nodeName);
                    newNode.setPidMap(mapPid);
                    newNode.setPidParentNode( selectedNodePid );
                    newNode.setKind(NodeTable.NODE_KIND_NODE);
                    newNode.setPos( posX, posY );
                    //newNode.setNodeColor();
                    //newNode.setTextColor();

                    //デザイン参考暫定----------
                    /*String nodeBackground = ((EditText)findViewById(R.id.et_nodeBackground)).getText().toString();
                    if( !nodeBackground.isEmpty() ){
                        int color =  Integer.parseInt(nodeBackground, 16);
                        newNode.setNodeColor( color );
                    }*/
                    //----------------------

                    //DB保存処理
                    AsyncCreateNode db = new AsyncCreateNode(view.getContext(), newNode, new AsyncCreateNode.OnCreateListener() {

                        @Override
                        public void onCreate(int pid) {
                            //データ挿入されたため、レコードに割り当てられたpidをノードに設定
                            newNode.setPid( pid );

                            //resultコード設定
                            intent.putExtra(ResourceManager.KEY_CREATED_NODE, newNode );
                            setResult(RESULT_CREATED, intent );

                            //元の画面へ戻る
                            finish();
                        }
                    });

                    //非同期処理開始
                    db.execute();
                }
            });

        } else {

            //編集対象ノード
            //NodeTable node = (NodeTable) intent.getSerializableExtra(MapActivity.INTENT_NODE);
            MapCommonData mapCommonData = (MapCommonData)getApplication();
            NodeTable node = mapCommonData.getEditNode();

            //親ノードのPid
            int parentPid = node.getPidParentNode();

            //ノード名
            ((EditText)findViewById(R.id.et_nodeName)).setText( node.getNodeName() );

            //★ノード情報を設定
            //

            //ノード生成
            bt_positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ノード名空チェック
                    String nodeName = ((EditText)findViewById(R.id.et_nodeName)).getText().toString();
                    if( nodeName.isEmpty() ){
                        //空なら、メッセージ出力して終了
                        //★

                        return;
                    }

                    //マップ共通データ
                    MapCommonData mapCommonData = (MapCommonData)getApplication();
                    NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();

                    //ノード名重複チェック
                    //※自分の名前はチェック対象外
                    if( nodes.hasSameNodeNameAtParent(parentPid, node.getPid(), nodeName) ){
                        //既に同じノード名があるなら、メッセージ出力して終了
                        //★
                        Log.i("NodeInformationActivity", "編集　ノード名重複を検知");
                        return;
                    }

                    //編集情報をノードに反映
                    //★編集情報のビュー側への適用をいつ行うか要検討
                    //★その他編集情報の反映が必要
                    node.setNodeName( nodeName );

                    //デザイン参考暫定----------
/*                    String nodeBackground = ((EditText)findViewById(R.id.et_nodeBackground)).getText().toString();
                    if( !nodeBackground.isEmpty() ){
                        int color =  Integer.parseInt(nodeBackground, 16);

                        node.setNodeColor( color );

                        Log.i("color", "nodeBackground=" + nodeBackground + "→" + color);
                    }*/
                    //----------------------

                    //DB保存処理
                    AsyncUpdateNode db = new AsyncUpdateNode(view.getContext(), node, new AsyncUpdateNode.OnFinishListener() {
                        @Override
                        public void onFinish() {

                            //共通データに編集済みノードを設定
                            mapCommonData.setEditNode( node );

                            //resultコード設定
                            //intent.putExtra( INTENT_UPDATED_NODE, node );
                            setResult(RESULT_EDITED, intent );

                            //元の画面へ戻る
                            finish();
                        }
                    });

                    //非同期処理開始
                    db.execute();
                }
            });
        }

        //キャンセル
        findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //resultコード設定
                setResult(RESULT_CANCELED);

                //元の画面へ戻る
                finish();
            }
        });

        //ノード背景色-カラーピッカー
        findViewById(R.id.tv_tmpColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //

            }
        });



    }








}