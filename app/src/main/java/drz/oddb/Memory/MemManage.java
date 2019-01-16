package drz.oddb.Memory;

import org.w3c.dom.Attr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import drz.oddb.Transaction.SystemTable.*;

public class MemManage {
    final private int attrstringlen=8; //属性最大字符串长度
    final private int bufflength=1000;//缓冲区大小为1000*8K

    //系统表变量
    /*private List<TopTable> topTab=new ArrayList<>();
    private List<ClassTable> classTab=new ArrayList<>();
    private List<DbTable> dbTab=new ArrayList<>();
    private List<AttrTable> attrTab=new ArrayList<>();
    private List<DeputyTable> deputyTab=new ArrayList<>();*/

    private HashMap<bufferTag,sbufesc> hashMap;//hash加速根据数据库id、表id和块号查找数据是否在缓冲区
    private List<sbufesc> FreeList = new ArrayList<>();		//构建缓冲区freeList
    private ByteBuffer MemBuff=ByteBuffer.allocateDirect(8*1024*bufflength);//buff
    private boolean[] buffuse=new boolean[1000];

    public MemManage(){
        /*loadTopTable();
        loadClassTable();
        loadDbTable();
        loadAttrTable();
        loadDeputyTable();*/
        initbuffuesd();
    }

    private void initbuffuesd(){
        for(int i=0;i<1000;i++){
            buffuse[i]=true;
        }
    }


    public static  DeputyTable loadDeputyTable(){
        DeputyTable ret = new DeputyTable();
        DeputyTableItem temp=null;
        File deputytab=new File("/data/data/drz.doob/transaction/deputytable");
        File path=deputytab.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                deputytab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input=new FileInputStream(deputytab);
            byte buff[]=new byte[16];
            while(input.read(buff,0,16)!=-1){
                temp=new DeputyTableItem();
                temp.classid=bytes2Int(buff,0,4);
                temp.deputyid=bytes2Int(buff,4,4);
                temp.deputyname=new String(buff,8,8);
                ret.deputyTable.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static  ClassTable loadClassTable(){
        ClassTable ret = new ClassTable();
        ClassTableItem temp=null;
        File classtab=new File("/data/data/drz.doob/transaction/classtable");
        File path=classtab.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                classtab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input=new FileInputStream(classtab);
            byte buff[]=new byte[36];
            while(input.read(buff,0,36)!=-1){
                temp=new ClassTableItem();
                temp.classname=new String(buff,0,8);
                temp.classid=bytes2Int(buff,8,4);
                temp.attrnum=bytes2Int(buff,12,4);
                temp.attrid=bytes2Int(buff,16,4);
                temp.attrname=new String(buff,20,8);
                temp.attrtype=new String(buff,28,8);
                ret.classTable.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static  TopTable loadTopTable(){
        TopTable ret = new TopTable();
        TopTableItem temp=null;
        File toptab=new File("/data/data/drz.doob/transaction/toptable");
        File path=toptab.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                toptab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input=new FileInputStream(toptab);
            byte buff[]=new byte[28];
            while(input.read(buff,0,28)!=-1){
                temp=new TopTableItem();
                temp.dbname=new String(buff,0,8);
                temp.dbid=bytes2Int(buff,8,4);
                temp.classid=bytes2Int(buff,12,4);
                temp.tupleid=bytes2Int(buff,16,4);
                temp.blockid=bytes2Int(buff,20,4);
                temp.offset=bytes2Int(buff,24,4);
                ret.topTable.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private sbufesc load(bufferTag tag){
        sbufesc Free=new sbufesc();
        if(FreeList.size()==1000) {

        }
        Free.tag=tag;
        Free.flag=false;
        for(int i=0;i<1000;i++){
            if(buffuse[i]){
                Free.buf_id=i;
                break;
            }
        }
        File file=new File("/data/data/drz.oddb/Memory/"+tag.dbOid+"/"+tag.blockNum);
        if(file.exists()){
            int offset=Free.buf_id*8*1024;
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] temp=new byte[8*1024];
                input.read(temp);
                for(int i=0;i<8*1024;i++){
                    MemBuff.put(offset+i,temp[i]);
                }
                return Free;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    private boolean save(bufferTag tag){

        return false;
    }
/*
    private void loadDeputyTable(){
        File deputytable=new File("/data/data/drz.doob/systemtable/deputytable");
        File path=deputytable.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                deputytable.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input = new FileInputStream(deputytable);
            byte buff[]=new byte[16];
            while(input.read(buff,0,16)!=-1){
                DeputyTable temp=new DeputyTable();
                temp.classid=bytes2Int(buff,0,4);
                temp.deputyid=bytes2Int(buff,4,4);
                temp.deputynum=new String(buff,8,attrstringlen);
                deputyTab.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAttrTable(){
        File attrtable=new File("/data/data/drz.doob/systemtable/attrtable");
        File path=attrtable.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                attrtable.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input = new FileInputStream(attrtable);
            byte buff[]=new byte[24];
            while(input.read(buff,0,24)!=-1){
                AttrTable temp=new AttrTable();
                temp.classid=bytes2Int(buff,0,4);
                temp.tableid=bytes2Int(buff,4,4);
                temp.attrname=new String(buff,8,attrstringlen);
                temp.type=new String(buff,16,attrstringlen);
                attrTab.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDbTable(){
        File dbtable=new File("/data/data/drz.doob/systemtable/dbtable");
        File path=dbtable.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                dbtable.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input = new FileInputStream(dbtable);
            byte buff[]=new byte[12];
            while(input.read(buff,0,12)!=-1){
                DbTable temp=new DbTable();
                temp.dbid=bytes2Int(buff,0,4);
                temp.dbname=new String(buff,4,attrstringlen);
                dbTab.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadClassTable(){
        File classtable=new File("/data/data/drz.doob/systemtable/classtable");
        File path=classtable.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                classtable.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input = new FileInputStream(classtable);
            byte buff[]=new byte[28];
            while(input.read(buff,0,28)!=-1){
                ClassTable temp=new ClassTable();
                temp.classid=bytes2Int(buff,0,4);
                temp.classname=new String(buff,4,attrstringlen);
                temp.tableid=bytes2Int(buff,12,4);
                temp.tablename=new String(buff,16,attrstringlen);
                temp.attrnum=bytes2Int(buff,24,4);
                classTab.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTopTable(){
        File toptable=new File("/data/data/drz.doob/systemtable/toptable");
        File path=toptable.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                toptable.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream input = new FileInputStream(toptable);
            byte buff[]=new byte[16];
            while(input.read(buff,0,16)!=-1){
                TopTable temp=new TopTable();
                temp.dbid=bytes2Int(buff,0,4);
                temp.tableid=bytes2Int(buff,4,4);
                temp.tupleid=bytes2Int(buff,8,4);
                temp.blockid=bytes2Int(buff,12,4);
                topTab.add(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
    private static byte[] int2Bytes(int value, int len){
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte)(value >> 8 * i);
        }
        return b;
    }

    private static int bytes2Int(byte[] b, int start, int len) {
        int sum = 0;
        int end = start + len;
        for (int i = start; i < end; i++) {
            int n = b[i]& 0xff;
            n <<= (--len) * 8;
            sum += n;
        }
        return sum;
    }
}