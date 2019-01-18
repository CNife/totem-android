package drz.oddb.Transaction;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import drz.oddb.Memory.*;


import drz.oddb.PrintResult;
import drz.oddb.ShowClass;
import drz.oddb.Transaction.SystemTable.*;

import drz.oddb.parse.*;

public class TransAction {
    public TransAction(Context context) {
        this.context = context;
    }

    Context context;
    MemManage mem = new MemManage();
    ObjectTable topt = mem.loadObjectTable();
    ClassTable classt = mem.loadClassTable();
    DeputyTable deputyt = mem.loadDeputyTable();
    BiPointerTable biPointerT = mem.loadBiPointerTable();
    SwitchingTable switchingT = mem.loadSwitchingTable();
    public void SaveAll( )
    {
        mem.saveObjectTable(topt);
        mem.saveClassTable(classt);
        mem.saveDeputyTable(deputyt);
        mem.saveBiPointerTable(biPointerT);
        mem.saveSwitchingTable(switchingT);

        mem.flush();
    }


    public void Test(){
        TupleList tpl = new TupleList();
        Tuple t1 = new Tuple();
        t1.tupleHeader = 3;
        t1.tuple = new Object[t1.tupleHeader];
        t1.tuple[0] = "a";
        t1.tuple[1] = 1;
        t1.tuple[2] = "b";
        Tuple t2 = new Tuple();
        t2.tupleHeader = 3;
        t2.tuple = new Object[t2.tupleHeader];
        t2.tuple[0] = "d";
        t2.tuple[1] = 2;
        t2.tuple[2] = "e";
        tpl.addTuple(t1);
        tpl.addTuple(t2);
        String[] attrname = {"attr2","attr1","attr3"};
        int[] attrid = {1,0,2};
        String[]attrtype = {"int","char","char"};

        PrintSelectResult(tpl,attrname,attrid,attrtype);

        //int[] a = InsertTuple(t1);
        //Tuple t3 = GetTuple(a[0],a[1]);
        //System.out.println(t3);
    }

    public String query(String s) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
        parse p = new parse(byteArrayInputStream);
        try {
            String[] aa = p.Run();

            switch (Integer.parseInt(aa[0])) {
                case parse.OPT_CREATE_ORIGINCLASS:
                    CreateOriginClass(aa);
                    break;
                case parse.OPT_CREATE_SELECTDEPUTY:
                    CreateSelectDeputy(aa);
                    break;
                case parse.OPT_DROP:
                    Drop(aa);
                    break;
                case parse.OPT_INSERT:
                    Insert(aa);
                    break;
                case parse.OPT_DELETE:
                    Delete(aa);
                    break;
                case parse.OPT_SELECT_DERECTSELECT:
                    DirectSelect(aa);
                    break;
                case parse.OPT_SELECT_INDERECTSELECT:
                    InDirectSelect(aa);
                    break;
                default:
                    break;
            }
        } catch (ParseException e) {

            e.printStackTrace();
        }


        return s;

    }

    //CREATE CLASS dZ123 (nB1 int,nB2 char) ;
    //1,2,dZ123,nB1,int,nB2,char
    private void CreateOriginClass(String[] p) {
        String classname = p[2];
        int count = Integer.parseInt(p[1]);
        classt.maxid++;
        int classid = classt.maxid;
        for (int i = 0; i < count; i++) {
            classt.classTable.add(new ClassTableItem(classname, classid, count,i,p[2 * i + 3], p[2 * i + 4],"ori"));
        }
    }

    //INSERT INTO aa VALUES (1,2,"3");
    //4,3,aa,1,2,"3"
    //0 1 2  3 4  5
    private int Insert(String[] p){


        int count = Integer.parseInt(p[1]);
        for(int o =0;o<count+3;o++){
            p[o] = p[o].replace("\"","");
        }

        String classname = p[2];
        Object[] tuple_ = new Object[count];

        int classid = 0;

        for(ClassTableItem item:classt.classTable)
        {
            if(item.classname.equals(classname)){
                classid = item.classid;
            }
        }

        for(int j = 0;j<count;j++){
            tuple_[j] = p[j+3];
        }

        Tuple tuple = new Tuple(tuple_);
        tuple.tupleHeader=count;

        int[] a = InsertTuple(tuple);
        topt.maxTupleId++;
        int tupleid = topt.maxTupleId;
        topt.objectTable.add(new ObjectTableItem(classid,tupleid,a[0],a[1]));

        //向代理类加元组

        for(DeputyTableItem item:deputyt.deputyTable){
            if(classid == item.originid){
                //判断代理规则

                String attrtype=null;
                int attrid=0;
                for(ClassTableItem item1:classt.classTable){
                    if(item1.classid == classid&&item1.attrname == item.deputyrule[0]) {
                        attrtype = item1.attrtype;
                        attrid = item1.attrid;
                        break;
                    }
                }

                if(Condition(attrtype,tuple,attrid,item.deputyrule[3])){
                    String[] ss= p.clone();
                    String s1 = null;

                    for(ClassTableItem item2:classt.classTable){
                        if(item2.classid == item.deputyid) {
                            s1 = item2.classname;
                            break;
                        }
                    }
                    //是否要插switch的值
                    String[] attrname1 = new String[count];
                    int[] attrid1 = new int[count];
                    for(ClassTableItem item3 : classt.classTable){
                        if(item3.classid == classid){
                            int k = 0;
                            for(; k<count;k++){
                                attrname1[k] = item3.attrname;
                                attrid1[k] = item3.attrid;

                            }
                            if (k ==count-1)
                                    break;
                        }
                    }
                    for (int l = 0;l<count;l++) {
                        for (SwitchingTableItem item4 : switchingT.switchingTable) {
                            if (item4.attr.equals(attrname1[l])){
                                int sw = Integer.parseInt(p[3+attrid1[l]]);
                                ss[3+attrid1[l]] = new Integer(sw+item4.rule).toString();
                            }
                        }
                    }

                    ss[2] = s1;
                    int deojid=Insert(ss);
                    //插入Bi
                    biPointerT.biPointerTable.add(new BiPointerTableItem(classid,tupleid,item.deputyid,deojid));



                }
            }
        }
        return tupleid;



    }

    private boolean Condition(String attrtype,Tuple tuple,int attrid,String value1){
        String value = value1.replace("\"","");
        switch (attrtype){
            case "int":
                int value_int = Integer.parseInt(value);
                if(Integer.parseInt((String)tuple.tuple[attrid])==value_int)
                    return true;
                break;
            case "char":
                String value_string = value;
                if(tuple.tuple[attrid].equals(value_string))
                    return true;
                break;

        }
        return false;
    }
    //DELETE FROM bb WHERE t4="5SS";
    //5,bb,t4,=,"5SS"
    private void Delete(String[] p) {
        String classname = p[1];
        String attrname = p[2];
        int classid = 0;
        int attrid=0;
        String attrtype=null;
        for (ClassTableItem item:classt.classTable) {
            if (item.classname == classname && item.attrname.equals(attrname)) {
                classid = item.classid;
                attrid = item.attrid;
                attrtype = item.attrtype;
                break;
            }
        }
        //寻找需要删除的
        for (ObjectTableItem item:topt.objectTable){
            if(item.classid == classid){
                Tuple tuple = GetTuple(item.blockid,item.offset);
                if(Condition(attrtype,tuple,attrid,p[4])){
                    //需要删除的元组
                    DeletebyID(item.tupleid);
                }
            }
        }


    }

    private void DeletebyID(int id){


        for (ObjectTableItem item:topt.objectTable){
            if(item.tupleid == id){
                //需要删除的tuple


                //删除代理类的元组
                int deobid = 0;
                for(BiPointerTableItem item1:biPointerT.biPointerTable){
                    if(item.tupleid == item1.deputyid){
                        biPointerT.biPointerTable.remove(item1);
                    }
                    if(item.tupleid == item1.objectid){
                        deobid = item1.deputyobjectid;
                        DeletebyID(deobid);
                        biPointerT.biPointerTable.remove(item1);
                    }
                }

                //删除自身
                DeleteTuple(item.blockid,item.offset);
                topt.objectTable.remove(item);





                }
            }
    }

    //DROP CLASS asd;
    //3,asd
    private void Drop(String[] p){
        String classname = p[1];
        int classid = 0;
        //找到classid顺便 清除类表和switch表
        for (ClassTableItem item:classt.classTable) {
            if (item.classname == classname ){
                classid = item.classid;
                for(SwitchingTableItem item2:switchingT.switchingTable) {
                    if (item2.attr.equals( item.attrname)||item2.deputy .equals( item.attrname)){
                        switchingT.switchingTable.remove(item2);
                    }
                }
                classt.classTable.remove(item);
            }
        }
        //清元组表同时清了bi
        for(ObjectTableItem item1:topt.objectTable){
            if(item1.classid == classid){
                DeletebyID(item1.tupleid);

            }
        }
        //清deputy
        for(DeputyTableItem item3:deputyt.deputyTable){
            if(item3.deputyid == classid){
                deputyt.deputyTable.remove(item3);
            }
            if(item3.originid == classid){
                //删除代理类
                String[]s = p.clone();
                for(ClassTableItem item4: classt.classTable){
                    if(item4.classid == item3.deputyid){
                        s[1] = item4.classname;
                        Drop(s);
                    }
                }
                deputyt.deputyTable.remove(item3);
            }
        }

    }


    //SELECT  b1+2 AS c1,b2 AS c2,b3 AS c3 FROM  bb WHERE t1="1";
    //6,3,b1,1,2,c1,b2,0,0,c2,b3,0,0,c3,bb,t1,=,"1"
    //0 1 2  3 4 5  6  7 8 9  10 111213 14 15 16 17
    private TupleList DirectSelect(String[] p){
        TupleList tpl = new TupleList();
        int attrnumber = Integer.parseInt(p[1]);
        String[] attrname = null;
        int[] attrid = null;
        String[] attrtype= null;
        String classname = p[2+4*attrnumber];
        int classid = 0;
        for(int i = 0;i < attrnumber;i++){
            for (ClassTableItem item:classt.classTable) {
                if (item.classname == classname && item.attrname.equals(p[2+4*i])) {
                    classid = item.classid;
                    attrid[i] = item.attrid;
                    attrtype[i] = item.attrtype;
                    attrname[i] = p[5+4*i];
                    //重命名

                    break;
                }
            }
        }


        int sattrid = 0;
        String sattrtype = null;
        for (ClassTableItem item:classt.classTable) {
            if (item.classid == classid && item.attrname.equals(p[3+4*attrnumber])) {
                sattrid = item.attrid;
                sattrtype = item.attrtype;
                break;
            }
        }


        for(ObjectTableItem item : topt.objectTable){
            if(item.classid == classid){
                Tuple tuple = GetTuple(item.blockid,item.offset);
                if(Condition(sattrtype,tuple,sattrid,p[4*attrnumber+5])){
                    //Switch

                    for(int j = 0;j<attrnumber;j++){
                        if(Integer.parseInt(p[3+4*j])==1){
                            int value = Integer.parseInt(p[4+4*j]);
                            int orivalue = Integer.parseInt((String)tuple.tuple[attrid[j]]);
                            Object ob = value+orivalue;
                            tuple.tuple[attrid[j]] = ob;
                        }

                    }



                    tpl.addTuple(tuple);
                }
            }
        }
        PrintSelectResult(tpl,attrname,attrid,attrtype);
        return tpl;

    }


    //CREATE SELECTDEPUTY aa SELECT  b1+2 AS c1,b2 AS c2,b3 AS c3 FROM  bb WHERE t1="1" ;
    //2,3,aa,b1,1,2,c1,b2,0,0,c2,b3,0,0,c3,bb,t1,=,"1"
    //0 1 2  3  4 5 6  7  8 9 10 11 121314 15 16 17 18
    private void CreateSelectDeputy(String[] p) {
        int count = Integer.parseInt(p[1]);
        String classname = p[2];//代理类的名字
        String bedeputyname = p[4*count+3];//代理的类的名字
        classt.maxid++;
        int classid = classt.maxid;//代理类的id
        int bedeputyid = -1;//代理的类的id
        String[] attrname=new String[count];
        String[] bedeputyattrname=new String[count];
        int[] bedeputyattrid = new int[count];
        String[] attrtype=new String[count];
        int[] attrid=new int[count];
        for(int j = 0;j<count;j++){
            attrname[j] = p[4*j+6];
            attrid[j] = j;
            bedeputyattrname[j] = p[4*j+3];
        }

        String attrtype1;
        for (int i = 0; i < count; i++) {

            for (ClassTableItem item:classt.classTable) {
                if (item.classname.equals(bedeputyname)&&item.attrname.equals(p[3+4*i])) {
                    bedeputyid = item.classid;
                    bedeputyattrid[i] = item.attrid;

                        classt.classTable.add(new ClassTableItem(classname, classid, count,attrid[i],attrname[i], item.attrtype,"de"));
                        //swi
                        if(Integer.parseInt(p[4+4*i]) == 1){
                            switchingT.switchingTable.add(new SwitchingTableItem(item.attrname,attrname[i],p[5+4*i]));

                        }
                    break;
                }
            };
        }



        String[] con =new String[3];
        con[0] = p[4+4*count];
        con[1] = p[5+4*count];
        con[2] = p[6+4*count];
        deputyt.deputyTable.add(new DeputyTableItem(bedeputyid,classid,con));


        TupleList tpl= new TupleList();

        int conid = 0;
        String contype  = null;
        for(ClassTableItem item3:classt.classTable){
            if(item3.attrname.equals(con[0])){
                conid = item3.attrid;
                contype = item3.attrtype;
                break;
            }
        }
        List<ObjectTableItem> obj = new ArrayList<>();
        for(ObjectTableItem item2:topt.objectTable){
            if(item2.classid ==bedeputyid){
                Tuple tuple = GetTuple(item2.blockid,item2.offset);
                if(Condition(contype,tuple,conid,con[2])){
                    //插入
                    //swi
                    Tuple ituple = new Tuple();
                    ituple.tupleHeader = count;
                    ituple.tuple = new Object[count];

                    for(int o =0;o<count;o++){
                        if(Integer.parseInt(p[4+4*o]) == 1){
                            int value = Integer.parseInt(p[5+4*o]);
                            int orivalue =Integer.parseInt((String)tuple.tuple[bedeputyattrid[o]]);
                            Object ob = value+orivalue;
                            ituple.tuple[o] = ob;
                        }
                        if(Integer.parseInt(p[4+4*o]) == 0){
                            ituple.tuple[o] = tuple.tuple[bedeputyattrid[o]];
                        }
                    }

                    topt.maxTupleId++;
                    int tupid = topt.maxTupleId;

                    int [] aa = InsertTuple(ituple);
                    //topt.objectTable.add(new ObjectTableItem(classid,tupid,aa[0],aa[1]));
                    obj.add(new ObjectTableItem(classid,tupid,aa[0],aa[1]));

                    //bi
                    biPointerT.biPointerTable.add(new BiPointerTableItem(bedeputyid,item2.tupleid,classid,tupid));

                }
            }
        }
        for(ObjectTableItem item6:obj) {
            topt.objectTable.add(item6);
        }
    }

    //SELECT popSinger -> singer.nation  FROM popSinger WHERE singerName = "JayZhou";
    //7,2,popSinger,singer,nation,popSinger,singerName,=,"JayZhou"
    //0 1 2         3      4      5         6          7  8
    private TupleList InDirectSelect(String[] p){
        TupleList tpl= new TupleList();
        String classname = p[3];
        String attrname = p[4];
        String crossname = p[2];
        String[] attrtype = new String[1];
        String[] con =new String[3];
        con[0] = p[6];
        con[1] = p[7];
        con[2] = p[8];

        int classid = 0;
        int crossid = 0;
        String crossattrtype = null;
        int crossattrid = 0;
        for(ClassTableItem item : classt.classTable){
            if(item.classname.equals(classname)){
                classid = item.classid;
                if(attrname.equals(item.attrname))
                    attrtype[0]=item.attrtype;
            }
            if(item.classname.equals(crossname)){
                crossid = item.classid;
                if(item.attrname.equals(con[0])) {
                    crossattrtype = item.attrtype;
                    crossattrid = item.attrid;
                }
            }
        }

        for(ObjectTableItem item1:topt.objectTable){
            if(item1.classid == crossid){
                Tuple tuple = GetTuple(item1.blockid,item1.offset);
                if(Condition(crossattrtype,tuple,crossattrid,con[2])){
                    for(BiPointerTableItem item3: biPointerT.biPointerTable){
                        if(item1.tupleid == item3.objectid){
                            for(ObjectTableItem item2: topt.objectTable){
                                if(item2.tupleid == item3.deputyobjectid){
                                    Tuple ituple = GetTuple(item2.blockid,item2.offset);
                                    tpl.addTuple(ituple);
                                }
                            }
                        }
                    }

                }
            }

        }
        String[] name = new String[1];
        name[0] = attrname;
        int[] id = new int[1];
        id[0] = 0;
        PrintSelectResult(tpl,name,id,attrtype);
        return tpl;




    }

    /*





        //INSERT INTO aa VALUES (1,2,"3");
        //4,3,aa,1,2,"3"




        */







    private Tuple GetTuple(int id, int offset) {

        return mem.readTuple(id,offset);
    }

    private int[] InsertTuple(Tuple tuple){
        return mem.writeTuple(tuple);
    }

    private void DeleteTuple(int id, int offset){
        mem.deleteTuple();
        return;
    }
    private void PrintClass(ObjectTable topt,SwitchingTable switchingT,DeputyTable deputyt,BiPointerTable biPointerT,ClassTable classTable){
        Intent intent = new Intent(context, ShowClass.class);

        Bundle bundle = new Bundle();
        bundle.putSerializable("ObjectTable", (Serializable) topt);
        bundle.putSerializable("SwitchingTable", (Serializable) switchingT);
        bundle.putSerializable("eputyTable", (Serializable) deputyt);
        bundle.putSerializable("BiPointerTable", (Serializable) biPointerT);
        bundle.putSerializable("ClassTable", (Serializable) classTable);

        intent.putExtras(bundle);
        context.startActivity(intent);


    }
    private void PrintSelectResult(TupleList tpl,String[] attrname,int[] attrid,String[] type) {
        Intent intent = new Intent(context, PrintResult.class);


        Bundle bundle = new Bundle();
        bundle.putSerializable("tupleList",tpl);
        bundle.putStringArray("attrname", attrname);
        bundle.putIntArray("attrid",attrid);
        bundle.putStringArray("type", type);
        intent.putExtras(bundle);
        context.startActivity(intent);


    }
}