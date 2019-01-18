package drz.oddb.Memory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import drz.oddb.Transaction.SystemTable.*;
import drz.oddb.Transaction.SystemTable.ObjectTable;

public class MemManage {
    final private int attrstringlen=8; //属性最大字符串长度为8Byte
    final private int bufflength=1000;//缓冲区大小为1000个块
    final private int blocklength=8*1024;//块大小为8KB

    private List<sbufesc> FreeList = new ArrayList<>();		//构建缓冲区指针
    private ByteBuffer MemBuff=ByteBuffer.allocateDirect(blocklength*bufflength);//分配blocklength*bufflength大小的缓冲区
    private boolean[] buffuse=new boolean[bufflength];//缓冲区可用状态表，true为可用
    private int blockmaxnum=0;
    private Random random;

    public MemManage(){
        initbuffues();//初始化缓冲区状态表
        blockmaxnum=loadBlockMaxNum();//从磁盘加载最大块号
        random=new Random();//创建随机数生成器
    }

    //退出时刷新数据到磁盘
    public void flush() {
        saveBlockMaxNum();//将缓冲区最大块号存入磁盘
        sbufesc sbu = new sbufesc();
        for (int i = 0; i < FreeList.size(); i++) {
            sbu = FreeList.get(i);
            if (sbu.flag) {
                save(sbu.blockNum);
            }
        }//将缓冲区存入磁盘
        saveLog();
    }

    //删除元组
    public void deleteTuple(){}

    public SwitchingTable loadSwitchingTable(){
        SwitchingTable ret=new SwitchingTable();
        SwitchingTableItem temp=null;
        File switab=new File("/data/data/drz.oddb/transaction/switchingtable");
        if(!switab.exists()){
            return ret;
        }else{
            try {
                FileInputStream input = new FileInputStream(switab);
                byte buff[] = new byte[3*attrstringlen];
                while (input.read(buff, 0, 3*attrstringlen) != -1) {
                    temp = new SwitchingTableItem();
                    temp.attr = byte2str(buff, 0, attrstringlen);
                    temp.deputy = byte2str(buff, attrstringlen, attrstringlen);
                    temp.rule = byte2str(buff, attrstringlen*2, attrstringlen);
                    ret.switchingTable.add(temp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    public boolean saveSwitchingTable(SwitchingTable tab){
        File switab=new File("/data/data/drz.oddb/transaction/switchingtable");
        if(!switab.exists()){
            File path=switab.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                switab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(switab));
            for(int i=0;i<tab.switchingTable.size();i++){
                byte[] s1=str2Bytes(tab.switchingTable.get(i).attr);
                output.write(s1,0,s1.length);
                byte[] s2=str2Bytes(tab.switchingTable.get(i).deputy);
                output.write(s2,0,s2.length);
                byte[] s3=str2Bytes(tab.switchingTable.get(i).rule);
                output.write(s3,0,s3.length);
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

    public BiPointerTable loadBiPointerTable() {
        BiPointerTable ret=new BiPointerTable();
        BiPointerTableItem temp=null;
        File bitab=new File("/data/data/drz.oddb/transaction/bipointertable");
        if(!bitab.exists()){
            return ret;
        }else{
            try {
                FileInputStream input = new FileInputStream(bitab);
                byte buff[] = new byte[16];
                while (input.read(buff, 0, 16) != -1) {
                    temp = new BiPointerTableItem();
                    temp.classid = bytes2Int(buff, 0, 4);
                    temp.objectid = bytes2Int(buff, 4, 4);
                    temp.deputyid = bytes2Int(buff, 8, 4);
                    temp.deputyobjectid = bytes2Int(buff, 12, 4);
                    ret.biPointerTable.add(temp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    public boolean saveBiPointerTable(BiPointerTable tab){
        File bitab=new File("/data/data/drz.oddb/transaction/bipointertable");
        if(!bitab.exists()){
            File path=bitab.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                bitab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(bitab));
            for(int i=0;i<tab.biPointerTable.size();i++){
                byte[] i1=int2Bytes(tab.biPointerTable.get(i).classid,4);
                output.write(i1,0,i1.length);
                byte[] i2=int2Bytes(tab.biPointerTable.get(i).objectid,4);
                output.write(i2,0,i2.length);
                byte[] i3=int2Bytes(tab.biPointerTable.get(i).deputyid,4);
                output.write(i3,0,i3.length);
                byte[] i4=int2Bytes(tab.biPointerTable.get(i).deputyobjectid,4);
                output.write(i4,0,i4.length);
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

    public DeputyTable loadDeputyTable(){
        DeputyTable ret = new DeputyTable();
        DeputyTableItem temp=null;
        File deputytab=new File("/data/data/drz.oddb/transaction/deputytable");
        if(!deputytab.exists()){
            return ret;
        }else {
            try {
                FileInputStream input = new FileInputStream(deputytab);
                byte buff[] = new byte[8+3*attrstringlen];
                while (input.read(buff, 0, 8+attrstringlen*3) != -1) {
                    temp = new DeputyTableItem();
                    temp.originid = bytes2Int(buff, 0, 4);
                    temp.deputyid = bytes2Int(buff, 4, 4);
                    temp.deputyrule[0] = byte2str(buff, 8, attrstringlen);
                    temp.deputyrule[1] = byte2str(buff, 8+attrstringlen, attrstringlen);
                    temp.deputyrule[2] = byte2str(buff, 8+attrstringlen*2, attrstringlen);
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

    public boolean saveDeputyTable(DeputyTable tab){
        File deputytab=new File("/data/data/drz.oddb/transaction/deputytable");
        if(!deputytab.exists()){
            File path=deputytab.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                deputytab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(deputytab));
            for(int i=0;i<tab.deputyTable.size();i++){
                byte[] i1=int2Bytes(tab.deputyTable.get(i).originid,4);
                output.write(i1,0,i1.length);
                byte[] i2=int2Bytes(tab.deputyTable.get(i).deputyid,4);
                output.write(i2,0,i2.length);
                byte[] s1=str2Bytes(tab.deputyTable.get(i).deputyrule[0]);
                output.write(s1,0,s1.length);
                byte[] s2=str2Bytes(tab.deputyTable.get(i).deputyrule[1]);
                output.write(s2,0,s2.length);
                byte[] s3=str2Bytes(tab.deputyTable.get(i).deputyrule[2]);
                output.write(s3,0,s3.length);
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

    public ClassTable loadClassTable(){
        ClassTable ret = new ClassTable();
        ClassTableItem temp=null;
        File classtab=new File("/data/data/drz.oddb/transaction/classtable");
        if(!classtab.exists()){
            return ret;
        }else {
            try {
                FileInputStream input = new FileInputStream(classtab);
                byte buff[] = new byte[12+attrstringlen*3];
                while (input.read(buff, 0, 12+attrstringlen*3) != -1) {
                    temp = new ClassTableItem();
                    temp.classname = byte2str(buff, 0, attrstringlen);
                    temp.classid = bytes2Int(buff, attrstringlen, 4);
                    temp.attrnum = bytes2Int(buff, attrstringlen+4, 4);
                    temp.attrid = bytes2Int(buff, attrstringlen+8, 4);
                    temp.attrname = byte2str(buff, attrstringlen+12, attrstringlen);
                    temp.attrtype = byte2str(buff, attrstringlen*2+12, attrstringlen);
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

    public boolean saveClassTable(ClassTable tab){
        File classtab=new File("/data/data/drz.oddb/transaction/classtable");
        if(!classtab.exists()){
            File path=classtab.getParentFile();
            System.out.println(path.getAbsolutePath());
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                classtab.createNewFile();
                System.out.println("创建文件成功！");
            } catch (IOException e) {
                System.out.println("创建文件失败！");
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(classtab));
            System.out.println(tab.classTable.size());
            for(int i=0;i<tab.classTable.size();i++){
                byte[] s1=str2Bytes(tab.classTable.get(i).classname);
                output.write(s1,0,s1.length);
                byte[] i1=int2Bytes(tab.classTable.get(i).classid,4);
                output.write(i1,0,i1.length);
                byte[] i2=int2Bytes(tab.classTable.get(i).attrnum,4);
                output.write(i2,0,i2.length);
                byte[] i3=int2Bytes(tab.classTable.get(i).attrid,4);
                output.write(i3,0,i3.length);
                byte[] s2=str2Bytes(tab.classTable.get(i).attrname);
                output.write(s2,0,s2.length);
                byte[] s3=str2Bytes(tab.classTable.get(i).attrtype);
                output.write(s3,0,s3.length);
            }
            output.flush();
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("文件未找到！");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("文件未正常写入！");
            e.printStackTrace();
        }
        return false;
    }

    public ObjectTable loadObjectTable(){
        ObjectTable ret = new ObjectTable();
        ObjectTableItem temp=null;
        File objtab=new File("/data/data/drz.oddb/transaction/objecttable");
        if(!objtab.exists()){
            return ret;
        }else{
            try {
                FileInputStream input=new FileInputStream(objtab);
                byte buff[]=new byte[16];
                while(input.read(buff,0,16)!=-1){
                    temp=new ObjectTableItem();
                    temp.classid=bytes2Int(buff,0,4);
                    temp.tupleid=bytes2Int(buff,4,4);
                    temp.blockid=bytes2Int(buff,8,4);
                    temp.offset=bytes2Int(buff,12,4);
                    ret.objectTable.add(temp);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
    }

    public boolean saveObjectTable(ObjectTable tab){
        File objtab=new File("/data/data/drz.oddb/transaction/objecttable");
        if(!objtab.exists()){
            File path=objtab.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                objtab.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(objtab));
            for(int i = 0; i<tab.objectTable.size(); i++){
                byte[] i2=int2Bytes(tab.objectTable.get(i).classid,4);
                output.write(i2,0,i2.length);
                byte[] i3=int2Bytes(tab.objectTable.get(i).tupleid,4);
                output.write(i3,0,i3.length);
                byte[] i4=int2Bytes(tab.objectTable.get(i).blockid,4);
                output.write(i4,0,i2.length);
                byte[] i5=int2Bytes(tab.objectTable.get(i).offset,4);
                output.write(i5,0,i5.length);
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

    public Tuple readTuple(int blocknum,int offset){
        Tuple ret=new Tuple();
        sbufesc s=null;
        if((s=findBlock(blocknum))==null){
            s=load(blocknum);
        }
        byte[] header=new byte[4];
        for(int i=0;i<4;i++){
            header[i]=MemBuff.get(s.buf_id*blocklength+offset+i);
        }
        ret.tupleHeader=bytes2Int(header,0,4);
        ret.tuple=new java.lang.Object[ret.tupleHeader];
        byte[] temp=new byte[ret.tupleHeader*attrstringlen];
        for(int i=0;i<ret.tupleHeader*attrstringlen;i++){
            temp[i]=MemBuff.get(s.buf_id*blocklength+offset+4+i);
        }
        for(int i=0;i<ret.tupleHeader;i++){
            String str=byte2str(temp,i*attrstringlen,attrstringlen);
            ret.tuple[i]=str;
        }
        return ret;
    }

    public int[] writeTuple(Tuple t){
        int [] ret=new int[2];
        sbufesc sbu=new sbufesc();
        if((sbu=findBlock(blockmaxnum))==null){
            if((sbu=load(blockmaxnum))==null){
                sbu=creatBlock();
            }
        }
        byte[] x=new byte[4];
        for(int i=0;i<4;i++){
            x[i]=MemBuff.get(sbu.buf_id*blocklength+i);
        }
        int spacestart=bytes2Int(x,0,4);
        if((blocklength-spacestart)>(4+attrstringlen*t.tupleHeader)){
            ret[0]=blockmaxnum;
            ret[1]=spacestart;
            sbu.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(sbu.buf_id*blocklength+spacestart+i,hea[i]);
            }
            byte[] str=null;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(sbu.buf_id*blocklength+spacestart+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(spacestart+4+t.tupleHeader*attrstringlen,4);
            for(int i=0;i<4;i++){
                MemBuff.put(sbu.buf_id*blocklength+i,sp[i]);
            }
            return ret;
        }else{
            sbu=creatBlock();
            ret[0]=blockmaxnum;
            ret[1]=4;
            sbu.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(sbu.buf_id*blocklength+4+i,hea[i]);
            }
            byte []str=null;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(sbu.buf_id*blocklength+4+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(4+4+t.tupleHeader*attrstringlen,4);
            for(int i=0;i<4;i++){
                MemBuff.put(sbu.buf_id*blocklength+i,sp[i]);
            }
            return ret;
        }
    }

    private boolean saveLog(){
        //TODO
        return true;
    }

    private void loadLog(){
        //TODO
    }

    private boolean save(int block){
        File file=new File("/data/data/drz.oddb/Memory/"+block);
        if(!file.exists()){
            File path=file.getParentFile();
            if(!path.exists()){
                path.mkdirs();
                System.out.println("创建文件夹成功！");
            }
            try {
                file.createNewFile();
                System.out.println("创建文件成功！");
            } catch (IOException e) {
                System.out.println("创建文件失败！");
                e.printStackTrace();
            }
        }
        int offset=-1;
        sbufesc s=null;
        if((s=findBlock(block))==null){
            return false;//块不在缓冲区，返回false 存入失败
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(file));
            byte[] buff=new byte[blocklength];
            offset=s.buf_id;
            for(int i=0;i<blocklength;i++){
                buff[i]=MemBuff.get(offset*blocklength+i);
            }
            output.write(buff,0,blocklength);
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

    private sbufesc load(int block){
        sbufesc Free=new sbufesc();
        if(FreeList.size()==bufflength) {
            int k=random.nextInt(bufflength);
            save(k);
            buffuse[k]=true;
            if(delete(k)){
                System.out.println("删除成功！");
            }else{
                System.out.println("删除失败！");
            }
        }
        File file=new File("/data/data/drz.oddb/Memory/"+block);
        if(file.exists()){
            Free.blockNum=block;
            Free.flag=false;
            for(int i=0;i<bufflength;i++){
                if(buffuse[i]){
                    Free.buf_id=i;
                    buffuse[i]=false;
                    break;
                }
            }
            int offset=Free.buf_id*blocklength;
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] temp=new byte[blocklength];
                input.read(temp);
                for(int i=0;i<blocklength;i++){
                    MemBuff.put(offset+i,temp[i]);
                }
                //hashMap.put(block,Free);
                FreeList.add(Free);
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

    private void initbuffues(){
        for(int i=0;i<bufflength;i++){
            buffuse[i]=true;
        }
    }

    private sbufesc creatBlock(){
        sbufesc newblocksb=new sbufesc();
        if(FreeList.size()==bufflength) {
            int k=random.nextInt(bufflength);
            save(k);
            buffuse[k]=true;
            if(delete(k)){
                System.out.println("删除成功！");
            }else{
                System.out.println("删除失败！");
            }
        }
        for(int i=0;i<bufflength;i++){
            if(buffuse[i]){
                newblocksb.buf_id=i;
                buffuse[i]=false;
                break;
            }
        }
        blockmaxnum++;
        newblocksb.blockNum=blockmaxnum;
        newblocksb.flag=true;
        byte[] header=int2Bytes(4,4);
        for(int i=0;i<4;i++){
            MemBuff.put(newblocksb.buf_id*blocklength+i,header[i]);
        }
        byte x=(byte)32;
        for(int i=4;i<blocklength;i++){
            MemBuff.put(newblocksb.buf_id*blocklength+i,x);
        }
        FreeList.add(newblocksb);
        return newblocksb;
    }

    //删除缓冲区块指针
    private boolean delete(int x){
        for(int i=0;i<FreeList.size();i++){
            if(FreeList.get(i).buf_id==x){
                FreeList.remove(i);
                return true;
            }
        }
        return false;
    }

    private sbufesc findBlock(int x){
        sbufesc ret=null;
        for(int i=0;i<FreeList.size();i++) {
            ret = FreeList.get(i);
            if (ret.blockNum == x) {
                return ret;
            }
        }
        return null;
    }

    private int loadBlockMaxNum(){
        int ret=0;
        File file=new File("/data/data/drz.oddb/Memory/blocknum");
        if(file.exists()){
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] temp=new byte[4];
                input.read(temp,0,4);
                ret=bytes2Int(temp,0,4);
                return ret;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return 0;
        }else{
            return 0;
        }
    }

    private boolean saveBlockMaxNum(){
        File file=new File("/data/data/drz.oddb/Memory/blocknum");
        if(!file.exists()){
            File path=file.getParentFile();
            if(!path.exists()){
                path.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream output=new FileOutputStream(file);
            byte[] temp=int2Bytes(blockmaxnum,4);
            output.write(temp,0,4);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private byte[] str2Bytes(String s){
        byte[] ret=new byte[attrstringlen];
        byte[] temp=s.getBytes();
        if(temp.length>=attrstringlen){
            for(int i=0;i<attrstringlen;i++){
                ret[i]=temp[i];
            }
            return ret;
        }else{
            for(int i=0;i<temp.length;i++){
                ret[i]=temp[i];
            }
            for(int i=temp.length;i<attrstringlen;i++){
                ret[i]=(byte)32;
            }
            return ret;
        }
    }

    private String byte2str(byte[] b,int off,int len){
        String s="";
        int k=0;
        for(int i=off;i<off+len;i++){
            if(b[i]!=32){
                k++;
            }else{
                break;
            }
        }
        s=new String(b,off,k);
        return s;
    }

    private byte[] int2Bytes(int value, int len){
        byte[] b = new byte[len];
        for (int i = 0; i < len; i++) {
            b[len - i - 1] = (byte)(value >> 8 * i);
        }
        return b;
    }

    private int bytes2Int(byte[] b, int start, int len) {
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