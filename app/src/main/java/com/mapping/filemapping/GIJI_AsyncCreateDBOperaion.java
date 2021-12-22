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
        createMapA(mapDao, nodeDao);
        createMapB(mapDao, nodeDao);
        createMapC(mapDao, nodeDao);
        //createMapC(mapDao, nodeDao);

        return 0;
    }


    public void createMapA(MapTableDao mapDao, NodeTableDao nodeDao) {

        MapTable mapA = new MapTable();
        mapA.setMapName("MapA");

        MapPid = (int)mapDao.insert( mapA );

        //ノード生成
        NodeTable nodeR = new NodeTable();
        NodeTable nodeA = new NodeTable();
        NodeTable nodeB = new NodeTable();
        NodeTable nodeC = new NodeTable();

        nodeR.setNodeName("MapA Root");
        nodeA.setNodeName("MapA NodeA");
        nodeB.setNodeName("MapA NodeB");
        nodeC.setNodeName("MapA NodeC");

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
    }

    public void createMapB(MapTableDao mapDao, NodeTableDao nodeDao) {

        MapTable mapB = new MapTable();
        mapB.setMapName("MapB");

        MapPid = (int)mapDao.insert( mapB );

        //ノード生成
        NodeTable nodeR = new NodeTable();
        NodeTable nodeA = new NodeTable();
        NodeTable nodeB = new NodeTable();
        NodeTable nodeC = new NodeTable();
        NodeTable nodeD = new NodeTable();
        NodeTable nodeE = new NodeTable();

        nodeR.setNodeName("MapB Root");
        nodeA.setNodeName("MapB NodeA__________");
        nodeB.setNodeName("MapB\nNodeB");
        nodeC.setNodeName("M\na\np\nB\nN\no\nd\ne\nC");
        nodeD.setNodeName("MapB NodeD");
        //nodeE.setNodeName("nodeE");

        nodeR.setPidMap((int)MapPid);
        nodeA.setPidMap((int)MapPid);
        nodeB.setPidMap((int)MapPid);
        nodeC.setPidMap((int)MapPid);
        nodeD.setPidMap((int)MapPid);
        nodeE.setPidMap((int)MapPid);

        nodeR.setKind( NodeTable.NODE_KIND_ROOT );
        nodeA.setKind( NodeTable.NODE_KIND_NODE );
        nodeB.setKind( NodeTable.NODE_KIND_NODE );
        nodeC.setKind( NodeTable.NODE_KIND_NODE );
        nodeD.setKind( NodeTable.NODE_KIND_NODE );
        nodeE.setKind( NodeTable.NODE_KIND_PICTURE );

        nodeR.setPos( 4000, 4000 );
        nodeA.setPos( 4100, 4100 );
        nodeB.setPos( 3900, 4100 );
        nodeC.setPos( 4200, 4200 );
        nodeD.setPos( 4300, 4300 );
        nodeE.setPos( 4400, 4400 );

        //レコード追加
        int pidr = (int)nodeDao.insert( nodeR );
        int pida = (int)nodeDao.insert( nodeA );
        int pidb = (int)nodeDao.insert( nodeB );
        int pidc = (int)nodeDao.insert( nodeC );
        int pidd = (int)nodeDao.insert( nodeD );
        int pide = (int)nodeDao.insert( nodeE );

        nodeR.setPidParentNode( NodeTable.NO_PARENT );
        nodeA.setPidParentNode( pidr );
        nodeB.setPidParentNode( pidr );
        nodeC.setPidParentNode( pida );
        nodeD.setPidParentNode( pidc );
        nodeE.setPidParentNode( pidd );

        Log.i("GIJI", "pidr=" + pidr);
        Log.i("GIJI", "pida=" + pida);

        //!注意。updateNodeする場合、DBと同じ一意のキーを設定する必要がある
        nodeR.setPid(pidr);
        nodeA.setPid(pida);
        nodeB.setPid(pidb);
        nodeC.setPid(pidc);
        nodeD.setPid(pidd);
        nodeE.setPid(pide);

        //更新
        nodeDao.updateNode(nodeR);
        nodeDao.updateNode(nodeA);
        nodeDao.updateNode(nodeB);
        nodeDao.updateNode(nodeC);
        nodeDao.updateNode(nodeD);
        nodeDao.updateNode(nodeE);

        List<NodeTable> nodeList = nodeDao.getAll();

        Log.i("GIJI", "doInBackground size=" + nodeList.size());
    }

    public void createMapC(MapTableDao mapDao, NodeTableDao nodeDao) {

        MapTable mapA = new MapTable();
        mapA.setMapName("MapC");

        MapPid = (int)mapDao.insert( mapA );

        //ノード生成
        NodeTable nodeR = new NodeTable();
        NodeTable nodeA = new NodeTable();
        NodeTable nodeB = new NodeTable();
        NodeTable nodeC = new NodeTable();
        NodeTable nodeD = new NodeTable();

        nodeR.setNodeName("MapC Root");
        nodeA.setNodeName("MapC NodeA__________");
        nodeB.setNodeName("MapC\nNodeB");
        nodeC.setNodeName("M\na\np\nC\nN\no\nd\ne\nC");
        nodeD.setNodeName("MapC NodeD");

        nodeR.setPidMap((int)MapPid);
        nodeA.setPidMap((int)MapPid);
        nodeB.setPidMap((int)MapPid);
        nodeC.setPidMap((int)MapPid);
        nodeD.setPidMap((int)MapPid);

        nodeR.setKind( NodeTable.NODE_KIND_ROOT );
        nodeA.setKind( NodeTable.NODE_KIND_NODE );
        nodeB.setKind( NodeTable.NODE_KIND_NODE );
        nodeC.setKind( NodeTable.NODE_KIND_NODE );
        nodeD.setKind( NodeTable.NODE_KIND_NODE );

        nodeR.setPos( 7000, 7000 );
        nodeA.setPos( 7100, 7100 );
        nodeB.setPos( 6900, 7100 );
        nodeC.setPos( 7200, 7200 );
        nodeD.setPos( 7600, 7600 );

        //レコード追加
        int pidr = (int)nodeDao.insert( nodeR );
        int pida = (int)nodeDao.insert( nodeA );
        int pidb = (int)nodeDao.insert( nodeB );
        int pidc = (int)nodeDao.insert( nodeC );
        int pidd = (int)nodeDao.insert( nodeD );

        nodeR.setPidParentNode( NodeTable.NO_PARENT );
        nodeA.setPidParentNode( pidr );
        nodeB.setPidParentNode( pidr );
        nodeC.setPidParentNode( pida );
        nodeD.setPidParentNode( pidc );

        Log.i("GIJI", "pidr=" + pidr);
        Log.i("GIJI", "pida=" + pida);

        //!注意。updateNodeする場合、DBと同じ一意のキーを設定する必要がある
        nodeR.setPid(pidr);
        nodeA.setPid(pida);
        nodeB.setPid(pidb);
        nodeC.setPid(pidc);
        nodeD.setPid(pidd);

        //更新
        nodeDao.updateNode(nodeR);
        nodeDao.updateNode(nodeA);
        nodeDao.updateNode(nodeB);
        nodeDao.updateNode(nodeC);
        nodeDao.updateNode(nodeD);

        List<NodeTable> nodeList = nodeDao.getAll();

        Log.i("GIJI", "doInBackground size=" + nodeList.size());
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
