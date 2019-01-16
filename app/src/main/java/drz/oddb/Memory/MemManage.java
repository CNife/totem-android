package drz.oddb.Memory;

import org.w3c.dom.Attr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<Integer,sbufesc> hashMap=new HashMap<>();//hash加速根据数据库id、表id和块号查找数据是否在缓冲区
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
        if(!deputytab.exists()){
            return ret;
        }else {
            try {
                FileInputStream input = new FileInputStream(deputytab);
                byte buff[] = new byte[16];
                while (input.read(buff, 0, 16) != -1) {
                    temp = new DeputyTableItem();
                    temp.classid = bytes2Int(buff, 0, 4);
                    temp.deputyid = bytes2Int(buff, 4, 4);
                    temp.deputyname = new String(buff, 8, 8);
                    ret.deputyTable.add(temp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    public static boolean saveDeputyTable(DeputyTable tab){
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
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(deputytab));
            for(int i=0;i<tab.deputyTable.size();i++){
                byte[] i1=int2Bytes(tab.deputyTable.get(i).classid,4);
                output.write(i1);
                byte[] i2=int2Bytes(tab.deputyTable.get(i).deputyid,4);
                output.write(i2);
                byte[] s1=tab.deputyTable.get(i).deputyname.getBytes();
                output.write(s1);
            }
            output.flush();
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static  ClassTable loadClassTable(){
        ClassTable ret = new ClassTable();
        ClassTableItem temp=null;
        File classtab=new File("/data/data/drz.doob/transaction/classtable");
        if(!classtab.exists()){
            return ret;
        }else {
            try {
                FileInputStream input = new FileInputStream(classtab);
                byte buff[] = new byte[36];
                while (input.read(buff, 0, 36) != -1) {
                    temp = new ClassTableItem();
                    temp.classname = new String(buff, 0, 8);
                    temp.classid = bytes2Int(buff, 8, 4);
                    temp.attrnum = bytes2Int(buff, 12, 4);
                    temp.attrid = bytes2Int(buff, 16, 4);
                    temp.attrname = new String(buff, 20, 8);
                    temp.attrtype = new String(buff, 28, 8);
                    ret.classTable.add(temp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    public static  boolean saveClassTable(ClassTable tab) {
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
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(classtab));
            for(int i=0;i<tab.classTable.size();i++){
                byte[] s1=tab.classTable.get(i).classname.getBytes();
                output.write(s1);
                byte[] i1=int2Bytes(tab.classTable.get(i).classid,4);
                output.write(i1);
                byte[] i2=int2Bytes(tab.classTable.get(i).attrnum,4);
                output.write(i2);
                byte[] i3=int2Bytes(tab.classTable.get(i).attrid,4);
                output.write(i3);
                byte[] s2=tab.classTable.get(i).attrname.getBytes();
                output.write(s2);
                byte[] s3=tab.classTable.get(i).attrtype.getBytes();
                output.write(s3);
            }
            output.flush();
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static  TopTable loadTopTable(){
        TopTable ret = new TopTable();
        TopTableItem temp=null;
        File toptab=new File("/data/data/drz.doob/transaction/toptable");
        if(!toptab.exists()){
            return ret;
        }else{
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
    }

    public static boolean saveTopTable(TopTable tab){
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
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(toptab));
            for(int i=0;i<tab.topTable.size();i++){
                byte[] s1=tab.topTable.get(i).dbname.getBytes();
                output.write(s1);
                byte[] i1=int2Bytes(tab.topTable.get(i).dbid,4);
                output.write(i1);
                byte[] i2=int2Bytes(tab.topTable.get(i).classid,4);
                output.write(i2);
                byte[] i3=int2Bytes(tab.topTable.get(i).tupleid,4);
                output.write(i3);
                byte[] i4=int2Bytes(tab.topTable.get(i).blockid,4);
                output.write(i2);
                byte[] i5=int2Bytes(tab.topTable.get(i).offset,4);
                output.write(i2);
            }
            output.flush();
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private sbufesc load(Integer block){
        sbufesc Free=new sbufesc();
        if(FreeList.size()==1000) {

        }
        Free.blockNum=block;
        Free.flag=false;
        for(int i=0;i<1000;i++){
            if(buffuse[i]){
                Free.buf_id=i;
                break;
            }
        }
        File file=new File("/data/data/drz.oddb/Memory/"+block);
        if(file.exists()){
            int offset=Free.buf_id*8*1024;
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] temp=new byte[8*1024];
                input.read(temp);
                for(int i=0;i<8*1024;i++){
                    MemBuff.put(offset+i,temp[i]);
                }
                hashMap.put(block,Free);
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

    private boolean save(Integer block){
        File file=new File("/data/data/drz.oddb/Memory/"+block);
        File path=file.getParentFile();
        if(!path.exists()){
            path.mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int offset;
        sbufesc s=null;
        if((s=hashMap.get(block))!=null){
            offset=s.blockNum.intValue();
        }else{
            return false;
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(file));
            byte[] buff=new byte[1024*8];

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

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