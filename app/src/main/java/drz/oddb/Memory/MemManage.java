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

import drz.oddb.Log.LogTable;
import drz.oddb.Log.LogTableItem;
import drz.oddb.Transaction.SystemTable.*;
import drz.oddb.Transaction.SystemTable.ObjectTable;

public class MemManage {
    final private int attrstringlen=8; //属性最大字符串长度为8Byte
    final private int bufflength=1000;//缓冲区大小为1000个块
    final private int blocklength=8*1024;//块大小为8KB

    private List<buffPointer> BuffPointerList = new ArrayList<>();		//构建缓冲区指针表
    private ByteBuffer MemBuff=ByteBuffer.allocateDirect(blocklength*bufflength);//分配blocklength*bufflength大小的缓冲区
    private boolean[] buffuse=new boolean[bufflength];//缓冲区可用状态表，true为可用
    private int blockmaxnum=-1;//最大的块号
    private int[] blockspace=new int[10];//块空闲空间信息


    public MemManage(){
        initbuffues();//初始化缓冲区状态表
        loadBlockSpace();//从磁盘加载块信息
    }

    //刷新数据到磁盘
    public void flush() {
        saveBlockSpace();//将块信息存入磁盘
        buffPointer sbu;
        for (int i = 0; i < BuffPointerList.size(); i++) {
            sbu = BuffPointerList.get(i);
            if (sbu.flag) {
                save(sbu);
            }
        }//将缓冲区存入磁盘
    }

    //删除元组
    public void deleteTuple(){}

    public void deleteTuple(int block,int offset){

    }

    public SwitchingTable loadSwitchingTable(){
        SwitchingTable ret=new SwitchingTable();
        SwitchingTableItem temp;
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
                input.close();
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/transaction/成功！");
            }
            try {
                if(switab.createNewFile())System.out.println("创建文件/data/data/drz.oddb/transaction/switchingtable成功！");
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
        BiPointerTableItem temp;
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
                input.close();
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/transaction/成功！");
            }
            try {
                if(bitab.createNewFile())System.out.println("创建文件/data/data/drz.oddb/transaction/bipointertable成功！");
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
        DeputyTableItem temp;
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
                input.close();
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/transaction/成功！");
            }
            try {
                if(deputytab.createNewFile())System.out.println("创建文件/data/data/drz.oddb/transaction/deputytable成功！");
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
        ClassTableItem temp;
        File classtab=new File("/data/data/drz.oddb/transaction/classtable");
        if(!classtab.exists()){
            return ret;
        }else {
            try {
                FileInputStream input = new FileInputStream(classtab);
                byte[] x=new byte[4];
                if(input.read(x,0,4)!=-1){
                    ret.maxid=bytes2Int(x,0,4);
                }
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/transaction/成功！");
            }
            try {
                if(classtab.createNewFile())System.out.println("创建路径/data/data/drz.oddb/transaction/classtable成功！");
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
        ObjectTableItem temp;
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
                input.close();
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/transaction/成功！");
            }
            try {
                if(objtab.createNewFile())System.out.println("创建路径/data/data/drz.oddb/transaction/objecttable成功！");
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
        byte[] sta=new byte[4];
        for(int i=0;i<4;i++){
            MemBuff.get(s.buf_id*blocklength+offset+i);
        }
        int start=bytes2Int(sta,0,4);
        byte[] header=new byte[4];
        for(int i=0;i<4;i++){
            header[i]=MemBuff.get(s.buf_id*blocklength+start+i);
        }
        ret.tupleHeader=bytes2Int(header,0,4);
        ret.tuple=new java.lang.Object[ret.tupleHeader];
        byte[] temp=new byte[ret.tupleHeader*attrstringlen];
        for(int i=0;i<ret.tupleHeader*attrstringlen;i++){
            temp[i]=MemBuff.get(s.buf_id*blocklength+start+4+i);
        }
        for(int i=0;i<ret.tupleHeader;i++){
            String str=byte2str(temp,i*attrstringlen,attrstringlen);
            ret.tuple[i]=str;
        }
        return ret;
    }

    public int[] writeTuple(Tuple t){
        int [] ret=new int[2];
        int tuplelength=4+attrstringlen*t.tupleHeader;
        buffPointer p=null;
        int k=-1;
        if(blockspace!=null){
            for(int i=0;i<=blockmaxnum;i++){
                if(blockspace[i]>=8+tuplelength){
                    k=i;
                    break;
                }
            }
        }
        if(k!=-1){
            blockspace[k]=blockspace[k]-tuplelength-4;
            p=findBlock(k);
            byte[] x=new byte[4];
            for(int i=0;i<4;i++){
                x[i]=MemBuff.get(p.buf_id*blocklength+i);
            }
            int spacestart=bytes2Int(x,0,4);
            for(int i=0;i<4;i++){
                x[i]=MemBuff.get(p.buf_id*blocklength+i);
            }
            int spaceend=bytes2Int(x,0,4);
            ret[0]=k;
            ret[1]=spacestart;
            spacestart=spacestart+4;
            spaceend=spaceend-tuplelength;
            p.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+spaceend+i,hea[i]);
            }
            byte[] str;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(p.buf_id*blocklength+spaceend+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(spacestart,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+i,sp[i]);
            }
            sp=int2Bytes(spaceend,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+4+i,sp[i]);
                MemBuff.put(p.buf_id*blocklength+spacestart-4+i,sp[i]);
            }
            updateBufferPointerSequence(p);
            return ret;
        }else{
            p=creatBlock();
            blockspace[blockmaxnum]=blockspace[blockmaxnum]-tuplelength-4;
            ret[0]=blockmaxnum;
            ret[1]=8;
            int spacetart=12;
            int spaceend=blocklength-tuplelength;
            p.flag=true;
            byte[] hea=int2Bytes(t.tupleHeader,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+spaceend+i,hea[i]);
            }
            byte[] str;
            for(int i=0;i<t.tupleHeader;i++){
                str=str2Bytes(t.tuple[i].toString());
                for(int j=0;j<attrstringlen;j++){
                    MemBuff.put(p.buf_id*blocklength+spaceend+4+i*attrstringlen+j,str[j]);
                }
            }
            byte[] sp=int2Bytes(spacetart,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+i,sp[i]);
            }
            sp=int2Bytes(spaceend,4);
            for(int i=0;i<4;i++){
                MemBuff.put(p.buf_id*blocklength+4+i,sp[i]);
                MemBuff.put(p.buf_id*blocklength+spacetart-4+i,sp[i]);
            }
            updateBufferPointerSequence(p);
            return ret;
        }
    }

    public boolean saveLog(LogTable log){
        File file=new File("/data/data/drz.oddb/Log/"+log.logID);
        if(!file.exists()){
            File path=file.getParentFile();
            if(!path.exists()){
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/Log/成功！");
            }
            try {
                if(file.createNewFile())System.out.println("创建成功！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(file));
            byte[] header=int2Bytes(log.check,4);
            output.write(header,0,4);
            for(int i=0;i<log.logTable.size();i++){
                byte[] in1=int2Bytes(log.logTable.get(i).length,4);
                output.write(in1,0,4);
                byte[] logstr=log.logTable.get(i).str.getBytes();
                output.write(logstr,0,log.logTable.get(i).str.length());
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

    public  LogTable loadLog(int logid){
        LogTable log=new LogTable();
        LogTableItem temp;
        File file=new File("/data/data/drz.oddb/Log/"+logid);
        if(!file.exists()){
            return null;
        }else{
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] x=new byte[4];
                input.read(x,0,4);
                log.check=bytes2Int(x,0,4);
                while((input.read(x,0,4)!=-1)){
                    temp=new LogTableItem();
                    temp.length=bytes2Int(x,0,4);
                    byte[] y=new byte[temp.length];
                    input.read(y,0,temp.length);
                    temp.str=new String(y,0,temp.length);
                    log.logTable.add(temp);
                }
                input.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return log;
        }
    }

    public boolean check(int lognum){
        File file=new File("/data/data/drz.oddb/Log/checklogid");
        if(!file.exists()){
            File path=file.getParentFile();
            if(!path.exists()){
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/Log/成功！");
            }
            try {
                if(file.createNewFile())System.out.println("创建成功！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream output=new FileOutputStream(file);
            byte[] check=int2Bytes(lognum,4);
            output.write(check,0,4);
            output.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  false;
    }

    public int loadCheck(){
        int ret=0;
        File file=new File("/data/data/drz.oddb/Log/checklogid");
        if(!file.exists()){
            return ret;
        }else{
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] x=new byte[4];
                input.read(x,0,4);
                ret=bytes2Int(x,0,4);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ret;
        }
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
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/Memory/成功！");
                System.out.println("创建文件夹成功！");
            }
            try {
                if(file.createNewFile()) {
                    System.out.println("创建文件成功！");
                }
            } catch (IOException e) {
                System.out.println("创建文件失败！");
                e.printStackTrace();
            }
        }
        int offset;
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
                input.close();
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

    //初始化缓冲区使用位图
    private void initbuffues(){
        for(int i=0;i<bufflength;i++){
            buffuse[i]=true;
        }
    }

    //创建块
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
        if(blockspace!=null){
            int[] s=new int[blockmaxnum+1];
            for(int i=0;i<blockmaxnum;i++){
                s[i]=blockspace[i];
            }
            s[blockmaxnum]=blocklength-8;
            blockspace=s;
        }else{
            blockspace=new int[1];
            blockspace[0]=blocklength-8;
        }
        newblockpointer.blockNum=blockmaxnum;
        newblockpointer.flag=true;
        byte[] header=new byte[8];
        byte[] start=int2Bytes(8,4);
        byte[] end=int2Bytes(blocklength,4);
        for(int i=0;i<8;i++){
            if(i<4){
                header[i]=start[i];
            }else{
                header[i]=end[i-4];
            }
        }
        for(int i=0;i<8;i++){
            MemBuff.put(newblockpointer.buf_id*blocklength+i,header[i]);
        }
        byte x=(byte)32;
        for(int i=4;i<blocklength;i++){
            MemBuff.put(newblockpointer.buf_id*blocklength+i,x);
        }
        BuffPointerList.add(0,newblockpointer);
        return newblockpointer;
    }

    //寻找块
    private buffPointer findBlock(int x){
        buffPointer ret;
        for(int i = 0; i< BuffPointerList.size(); i++) {
            ret = BuffPointerList.get(i);
            if (ret.blockNum == x) {
                return ret;
            }
        }
        return null;
    }

    //从磁盘加载块空间信息
    private void loadBlockSpace(){
        File file=new File("/data/data/drz.oddb/Memory/blockspace");
        if(file.exists()){
            try {
                FileInputStream input=new FileInputStream(file);
                byte[] temp=new byte[4];
                input.read(temp,0,4);
                blockmaxnum=bytes2Int(temp,0,4);
                blockspace=new int[blockmaxnum+1];
                for(int i=0;i<=blockmaxnum;i++){
                    input.read(temp,0,4);
                    blockspace[i]=bytes2Int(temp,0,4);
                }
                input.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            blockspace=null;
        }
    }

    //存入块空间信息到磁盘
    private boolean saveBlockSpace(){
        File file=new File("/data/data/drz.oddb/Memory/blockspace");
        if(!file.exists()){
            File path=file.getParentFile();
            if(!path.exists()){
                if(path.mkdirs())System.out.println("创建路径/data/data/drz.oddb/Memory/成功！");
            }
            try {
                if(file.createNewFile())System.out.println("创建成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedOutputStream output=new BufferedOutputStream(new FileOutputStream(file));
            byte[] temp=int2Bytes(blockmaxnum,4);
            output.write(temp,0,4);
            for(int i=0;i<=blockmaxnum;i++){
                temp=int2Bytes(blockspace[i],4);
                output.write(temp,0,4);
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
        String s;
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