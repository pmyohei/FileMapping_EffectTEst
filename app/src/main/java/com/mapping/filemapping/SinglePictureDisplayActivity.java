package com.mapping.filemapping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class SinglePictureDisplayActivity extends AppCompatActivity {

    private ArrayList<ThumbnailGridAdapter.PictureNodeInfo> mPictureNodeInfo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture_display);

        //共通データから、選択されたギャラリーのリストを取得
        Intent intent = getIntent();
        ArrayList<PictureTable> galley = (ArrayList) intent.getSerializableExtra("test");



        //マップ上のピクチャノードpid
        MapCommonData mapCommonData = (MapCommonData) getApplication();
        NodeArrayList<NodeTable> nodes = mapCommonData.getNodes();
        NodeArrayList<NodeTable> pictureNodePids = nodes.getAllPictureNodes();

        //マップ上のピクチャノード情報（写真移動先の表示用）
        mPictureNodeInfo = new ArrayList<>();

        //サムネイルを取得
        int mapPid = nodes.get(0).getPidMap();
        AsyncReadThumbnail db = new AsyncReadThumbnail(this, mapPid, new AsyncReadThumbnail.OnFinishListener() {
            @Override
            public void onFinish( PictureArrayList<PictureTable> thumbnails ) {

                //全ピクチャノード数
                for( NodeTable node: pictureNodePids  ){
                    //各ピクチャノード情報
                    int pid = node.getPid();

                    NodeTable parentNode = nodes.getNode( node.getPidParentNode() );
                    String parentNodeName = parentNode.getNodeName();

                    PictureTable thumbnail = thumbnails.getThumbnail( pid );

                    //ピクチャノード情報を生成
                    mPictureNodeInfo.add( new ThumbnailGridAdapter.PictureNodeInfo(
                            pid,
                            thumbnail,
                            parentNodeName
                    ) );
                }
            }
        });

        //非同期処理開始
        db.execute();

        Log.i("単体表示", "galley.size()=" + galley.size());

        //各写真の表示設定
        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
        vp2_singlePicture.setAdapter(new SinglePictureAdapter(this, galley));
        //vp2_singlePicture.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false ));

        //写真削除リスナー
        findViewById(R.id.iv_trash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //確認ダイアログを表示
                //confirmDeletePicture(vp2_singlePicture.getCurrentItem());
            }
        });

        //写真移動リスナー
        findViewById(R.id.iv_changeNode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //移動先候補を表示
                showMoveDestination();
            }
        });
    }

    /*
     * 削除確認
     */
    private void confirmDeletePicture(PictureTable aa) {

        //削除確認ダイアログを表示
        new AlertDialog.Builder(this)
                .setTitle("写真の削除")
                .setMessage("ノードから写真を削除します。\n※端末上から写真は削除されません。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //アダプタから削除
                        ViewPager2 vp2_singlePicture = findViewById(R.id.vp2_singlePicture);
                        SinglePictureAdapter adapter = (SinglePictureAdapter) vp2_singlePicture.getAdapter();
                        if (adapter != null) {
                            //フェールセーフ
                            int index = vp2_singlePicture.getCurrentItem();
                            adapter.removeItem(index);
                        }

/*                        //削除対象ノード
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
                        */
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }

    /*
     * 所属するピクチャノードの変更
     */
    private void showMoveDestination(){
        //マップ上のピクチャノードを移動先候補として表示
        PictureNodesBottomSheetDialog bottomSheetDialog = PictureNodesBottomSheetDialog.newInstance( mPictureNodeInfo );
        bottomSheetDialog.show(getSupportFragmentManager(), "");


    }

}