package com.mapping.filemapping;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Node;

import java.util.List;

/*
 * DB非同期処理
 *   create
 */
public class GIJI_AsyncCreateDBOperaion extends AsyncTask<Void, Void, Integer> {

    private final AppDatabase mDB;
    private final boolean mDelete;

    OnCreateListener mlistener;

    int MapPid;

    /*
     * コンストラクタ
     */
    public GIJI_AsyncCreateDBOperaion(Context context, boolean delete, OnCreateListener listener) {
        mDB = AppDatabaseManager.getInstance(context);
        mDelete = delete;

        mlistener = listener;
    }

    /*
     * コンストラクタ
     */
    public GIJI_AsyncCreateDBOperaion(Context context, boolean delete) {
        mDB = AppDatabaseManager.getInstance(context);
        mDelete = delete;
    }

    /*
     * メインスレッドとは別のスレッドで実行される。
     * 非同期で行いたい処理を記述
     */
    @Override
    protected Integer doInBackground(Void... params) {

        //MapDao
        MapTableDao mapDao = mDB.daoMapTable();
        //nodeDao
        NodeTableDao nodeDao = mDB.daoNodeTable();

        if( mDelete ){
            mapDao.deleteAll();
            nodeDao.deleteAll();

            return 0;
        }

        //マップ生成
        MapTable mapA = new MapTable();
        mapA.setMapName("TestMapA");

        MapPid = (int)mapDao.insert( mapA );

        //ノード生成
        NodeTable nodeR = new NodeTable();
        NodeTable nodeA = new NodeTable();
        NodeTable nodeB = new NodeTable();
        NodeTable nodeC = new NodeTable();

        nodeR.setNodeName("Root");
        nodeA.setNodeName("NodeA");
        nodeB.setNodeName("NodeB");
        nodeC.setNodeName("NodeC");

        nodeR.setPidMap((int)MapPid);
        nodeA.setPidMap((int)MapPid);
        nodeB.setPidMap((int)MapPid);
        nodeC.setPidMap((int)MapPid);

        nodeR.setKind( NodeTable.NODE_KIND_ROOT );
        nodeA.setKind( NodeTable.NODE_KIND_NODE );
        nodeB.setKind( NodeTable.NODE_KIND_NODE );
        nodeC.setKind( NodeTable.NODE_KIND_NODE );

        nodeR.setPos( 4000, 4000 );
        nodeA.setPos( 4100, 4100 );
        nodeB.setPos( 3900, 4100 );
        nodeC.setPos( 4200, 4200 );

        nodeR.setPidParentNode( -11 );
        nodeA.setPidParentNode( -11 );
        nodeB.setPidParentNode( -11 );
        nodeC.setPidParentNode( -11 );

        //レコード追加
        int pidr = (int)nodeDao.insert( nodeR );
        int pida = (int)nodeDao.insert( nodeA );
        int pidb = (int)nodeDao.insert( nodeB );
        int pidc = (int)nodeDao.insert( nodeC );

        nodeR.setPidParentNode( NodeTable.NO_PARENT );
        nodeA.setPidParentNode( pidr );
        nodeB.setPidParentNode( pidr );
        nodeC.setPidParentNode( pida );

        Log.i("GIJI", "pidr=" + pidr);
        Log.i("GIJI", "pida=" + pida);

        //!注意。updateNodeする場合、DBと同じ一意のキーを設定する必要がある
        nodeR.setPid(pidr);
        nodeA.setPid(pida);
        nodeB.setPid(pidb);
        nodeC.setPid(pidc);

        //更新
        nodeDao.updateNode(nodeR);
        nodeDao.updateNode(nodeA);
        nodeDao.updateNode(nodeB);
        nodeDao.updateNode(nodeC);

        List<NodeTable> nodeList = nodeDao.getAll();

        Log.i("GIJI", "doInBackground size=" + nodeList.size());

        return 0;
    }

    /*
     * doInBackground()後処理。メインスレッドで実行される。
     */
    @Override
    protected void onPostExecute(Integer code) {

        if( mlistener != null ){
            mlistener.onCreate( MapPid );
        }
    }

    public interface OnCreateListener {
        void onCreate( int mapPid );
    }


}
