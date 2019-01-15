package drz.oddb.Memory;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class StoreFun {
    HashMap<bufferTag,sbufesc> hashMap; //hash加速根据数据库id、表id和块号查找数据
    List<sbufesc> freeList;		//构建缓冲区freeList

    sbufesc load(bufferTag tag ){
        sbufesc free = freeList.get(0);     //返回值
        bufferTag btag = free.tag;

        if(btag != null){
            save(free.tag);     //换出该块
        }

        int dbid = tag.dbOid;
        int tid = tag.tableOid;
        int bnum = tag.blockNum;

        String filename = find(dbid,tid);
        File file = new File(filename);
        if(file.exists() == false){         //找文件，不存在就创建
            try{
                file.createNewFile();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        //TODO,打开

        int offest = 8*1024*bnum;

        //TODO,读入缓冲区

        free.flag = false;
        free.tag.dbOid = dbid;
        free.tag.tableOid = tid;
        free.tag.blockNum = bnum;

        freeList.add(free);
        freeList.remove(0);
        hashMap.put(btag,free);

        return free;
    }

    String find(int dbid, int tid) {
        String s = null;
        //TODO
        return s;
    }

    boolean save(bufferTag tag ){
        boolean result =  true;
        sbufesc sbu = hashMap.get(tag);
        if(sbu.flag == true){
            int dbid = tag.dbOid;
            int tid = tag.tableOid;
            find(dbid,tid);

            //TODO,打开文件

            int bnum = tag.blockNum;
            int offset = 8*1024*bnum;

            //TODO，将块写入

            sbu.flag = false;
        }
        return result;      //先设为永远返回true
    }

    HeapTupleData readTuple(ItemPointerData data){
        HeapTupleData result =  new HeapTupleData();    //返回值
        HeapTupleHeader htuphead = new HeapTupleHeader();   //返回值表头信息

        bufferTag btag = data.tag;
        sbufesc sbu = hashMap.get(btag);

        if(sbu == null){
            sbu = load(btag);
        }

        //TODO，复制信息 PageHeaderData -> HeapTupleHeader
        //TODO,计算元祖位置，复制到flag & value

        htuphead.attrName = null;   //TODO
        htuphead.attrNum = 0;    //TODO
        htuphead.attrType = null; //TODO

        result.t_self = data;   //记录块号和偏移
        result.t_data = htuphead;
        result.flag = null;  //TODO，记录对应属性是否为空
        result.value = null; //TODO，属性值

        return result;
    }

    sbufesc newTuple(HeapTupleData data){
        sbufesc free =  freeList.get(0);
        if(free.tag != null){
            save(free.tag);
        }
        PageHeaderData phd = new PageHeaderData();
        phd.startFreespace = 200;
        phd.endFreespace = 8*1024;
        phd.tupleNum = 0;
        phd.flag = null; //TODO

        return free;
    }

    ItemPointerData writeTuple(HeapTupleData data){
        ItemPointerData ipd = data.t_self;
        bufferTag btag = data.t_self.tag;

        sbufesc sbu = load(btag);
        if(btag.blockNum == -1){        //块号不存在
            sbufesc newsbu =  newTuple(data);
            //TODO 1
        }
        else{   //块号存在
            //TODO，扫描Pageheaderdata寻找空位
            if(true){
                //根据Linp找到位置，写入元组
            }
            else {
                //if(endFreespace - startFreespace > tuple_size+4+100){
                //TODO 1
                //}
                //else{
                //TODO
                //}
            }
        }

        //TODO,HeapTupleData的flag复制到header中的flag，更新header的记录长度
        //TODO,复制HeapTupleData的value

        sbu.flag = false;
        return ipd;
    }

}
