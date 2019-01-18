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

    private List<buffPointer> BuffPointerList = new ArrayList<>();		//构建缓冲区指针表
    private ByteBuffer MemBuff=ByteBuffer.allocateDirect(blocklength*bufflength);//分配blocklength*bufflength大小的缓冲区
    private boolean[] buffuse=new boolean[bufflength];//缓冲区可用状态表，true为可用
    private int blockmaxnum=0;

    public MemManage(){
        initbuffues();//初始化缓冲区状态表
        blockmaxnum=loadBlockMaxNum();//从磁盘加载最大块号
    }

    //退出时刷新数据到磁盘
    public void flush() {
        saveBlockMaxNum();//将缓冲区最大块号存入磁盘
        buffPointer sbu = new buffPointer();
        for (int i = 0; i < BuffPointerList.size(); i++) {
            sbu = BuffPointerList.get(i);
            if (sbu.flag) {
                save(sbu);
            }
        }//将缓冲区存入磁盘
        saveLog();
    }

    //删除元组
    public void deleteTuple(){}

    public void deleteTuple(int block,int offset){

    }

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
                    temp.deputyrule=new String[3];
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
                byte[] x=new byte[4];
                input.read(x,0,4);
                ret.maxid=bytes2Int(x,0,4);
                byte[] buff = new byte[12+attrstringlen*3];
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
            byte[] maxi=int2Bytes(tab.maxid,4);
            output.write(maxi,0,maxi.length);
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
                byte[] x=new byte[4];
                input.read(x,0,4);
                ret.maxTupleId=bytes2Int(x,0,4);
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
            byte[] max=int2Bytes(tab.maxTupleId,4);
            output.write(max,0,max.length);
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
        buffPointer s=null;
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
        buffPointer buffPointer=new buffPointer();
        if((buffPointer=findBlock(blockmaxnum))==null){
            if((buffPointer=load(blockmaxnum))==null){
                buffPointer=creatBlock();
            }
        }
        byte[] x=new byte[4];
        for(int i=0;i<4;i++){
            x[i]=MemBuff.get(buffPointer.buf_id*blocklength+i);
        }
        int spacestart=bytes2Int(x,0,4);
        if((blocklength-spacestart)>(4+attrstringlen*t.tupleHeader)){
            ret[0]=blockmaxnum;
            ret[1]=spacestart;
            buffPointer.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(buffPointer.buf_id*blocklength+spacestart+i,hea[i]);
            }
            byte[] str=null;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(buffPointer.buf_id*blocklength+spacestart+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(spacestart+4+t.tupleHeader*attrstringlen,4);
            for(int i=0;i<4;i++){
                MemBuff.put(buffPointer.buf_id*blocklength+i,sp[i]);
            }
            updateBufferPointerSequence(buffPointer);
            return ret;
        }else{
            buffPointer=creatBlock();
            ret[0]=blockmaxnum;
            ret[1]=4;
            buffPointer.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(buffPointer.buf_id*blocklength+4+i,hea[i]);
            }
            byte []str=null;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(buffPointer.buf_id*blocklength+4+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(4+4+t.tupleHeader*attrstringlen,4);
            for(int i=0;i<4;i++){
                MemBuff.put(buffPointer.buf_id*blocklength+i,sp[i]);
            }
            updateBufferPointerSequence(buffPointer);
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

    private void updateBufferPointerSequence(buffPointer p){
        buffPointer q=new buffPointer();
        q.blockNum=p.blockNum;
        q.buf_id=p.buf_id;
        q.flag=p.flag;
        BuffPointerList.remove(p);
        BuffPointerList.add(0,q);
    }

    //存块
    private boolean save(buffPointer blockpointer){
        File file=new File("/data/data/drz.oddb/Memory/"+blockpointer.blockNum);
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
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(file));
            byte[] buff=new byte[blocklength];
            offset=blockpointer.buf_id;
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

    //加载块
    private buffPointer load(int block){
        buffPointer Free=new buffPointer();
        if(BuffPointerList.size()==bufflength) {
            if(save(BuffPointerList.get(bufflength-1))){
                buffuse[BuffPointerList.get(bufflength-1).buf_id]=true;
                BuffPointerList.remove(bufflength-1);
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
                BuffPointerList.add(0,Free);
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

    private buffPointer creatBlock(){
        buffPointer newblockpointer=new buffPointer();
        if(BuffPointerList.size()==bufflength) {
            if(save(BuffPointerList.get(bufflength-1))){
                buffuse[BuffPointerList.get(bufflength-1).buf_id]=true;
                BuffPointerList.remove(bufflength-1);
            }
        }
        for(int i=0;i<bufflength;i++){
            if(buffuse[i]){
                newblockpointer.buf_id=i;
                buffuse[i]=false;
                break;
            }
        }
        blockmaxnum++;
        newblockpointer.blockNum=blockmaxnum;
        newblockpointer.flag=true;
        byte[] header=int2Bytes(4,4);
        for(int i=0;i<4;i++){
            MemBuff.put(newblockpointer.buf_id*blocklength+i,header[i]);
        }
        byte x=(byte)32;
        for(int i=4;i<blocklength;i++){
            MemBuff.put(newblockpointer.buf_id*blocklength+i,x);
        }
        BuffPointerList.add(0,newblockpointer);
        return newblockpointer;
    }

    //删除缓冲区块指针
    private boolean delete(int x){
        for(int i = 0; i< BuffPointerList.size(); i++){
            if(BuffPointerList.get(i).buf_id==x){
                BuffPointerList.remove(i);
                return true;
            }
        }
        return false;
    }

    private buffPointer findBlock(int x){
        buffPointer ret=null;
        for(int i = 0; i< BuffPointerList.size(); i++) {
            ret = BuffPointerList.get(i);
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