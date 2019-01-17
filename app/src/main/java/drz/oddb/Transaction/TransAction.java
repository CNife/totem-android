package drz.oddb.Transaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.ByteArrayInputStream;

import drz.oddb.Memory.*;


import drz.oddb.PrintResult;
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

        mem.exitFlush();
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
        String[]attrtype = {"char","int","char"};

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

                    break;
                case parse.OPT_DROP:

                    break;
                case parse.OPT_INSERT:
                    break;
                case parse.OPT_DELETE:

                    break;
                case parse.OPT_SELECT_DERECTSELECT:
                    break;
                case parse.OPT_SELECT_INDERECTSELECT:
                    break;
                default:
                    break;
            }
        } catch (ParseException e) {

            e.printStackTrace();
        }


        return s;

    }


    private void CreateOriginClass(String[] p) {
        String classname = p[2];
        int count = Integer.parseInt(p[1]);
        classt.maxid++;
        int classid = classt.maxid;
        for (int i = 0; i < count; i++) {
            classt.classTable.add(new ClassTableItem(classname, classid, count,i,p[2 * i + 3], p[2 * i + 4],"ori"));
        }
    }

    //CREATE SELECTDEPUTY aa SELECT  b1,b2,b3 FROM  bb WHERE t1="1" ;
    //2,3,aa,b1,b2,b3,bb,t1,=,"1"

    /*
    private void CreateSelectDeputy(String[] p) {
        int count = Integer.parseInt(p[1]);
        String classname = p[2];//代理类的名字
        String bedeputyname = p[count+3];//代理的类的名字
        classt.maxid++;
        int classid = classt.maxid;//代理类的id
        int bedeputyid = -1;//代理的类的id

        String attrname;
        String attrtype;
        for (int i = 0; i < count; i++) {
            int attrid;
            for (ClassTableItem item:classt.classTable) {
                attrname = item.attrname;
                attrtype = item.attrtype;
                if (item.classname == deputyname) {
                    bedeputyid = item.classid;
                    if(item.attrname.equals(p[i+2])){
                        classt.classTable.add(new ClassTableItem(classname, classid, count,i,p[2+i], attrtype,"de"));
                    }
                    break;
                }
            };
        }
        deputyt.deputyTable.add(new DeputyTableItem(bedeputyid,classid,todo));

        String[] select_s = new String[p.length-1];
        select_s[0] = parse.OPT_SELECT_DERECTSELECT+"";
        select_s[1] = p[1];
        for(int i = 0;i < p.length-2;i++)
        {
            select_s[2+i] = p[i+2];
        }

        TupleList tList = DirectSelect(select_s);
        for(int  i = 0;i < tList.tuplenum;i++)
        {
            int[] id = InsertTuple(tList.tuplelist.get(i));
            topt.objectTable.add(new ObjectTableItem("dz",1,classid,topt.maxTupleId++,id[0],id[1]));
        }
    }



       private void Drop(String[] p){
            String classname = p[1];
            int classid = -1;
           for (ClassTableItem item:classt.classTable) {
               if (item.classname == classname ){
                    classid = item.classid;
                    classt.classTable.remove(item);
               }
           }
           for (DeputyTableItem item:deputyt.deputyTable){
                if(item.deputyname == classname){
                    deputyt.deputyTable.remove(item);
                }
           }

           for (ObjectTableItem item:topt.objectTable){
               if(item.classid == classid){
                   DeleteTuple(item.blockid,item.offset);
                   topt.objectTable.remove(item);
               }
           }
       }
        //INSERT INTO aa VALUES (1,2,"3");
        //4,3,aa,1,2,"3"


       private void Insert(String[] p){
            int count = Integer.parseInt(p[1]);
            String classname = p[2];
            Object[] attrArr = new Object[count];

            int classid = -1;
           for (ClassTableItem item:classt.classTable) {
               if (item.classname == classname ){
                   classid = item.classid;
                   switch (item.attrtype){
                       case "int":
                           attrArr[item.attrid] = Integer.parseInt(p[item.attrid+3]);
                           break;
                       case "char":
                           attrArr[item.attrid] = p[item.attrid+3].replace("\"","");
                           break;
                   }
               }
           }

           int[] id = InsertTuple(new Tuple(attrArr));
            topt.objectTable.add(new ObjectTableItem("dz",1,classid,topt.maxTupleId++,id[0],id[1]));

       }

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

            for (ObjectTableItem item:topt.objectTable){
                if(item.classid == classid){
                    Tuple tuple = GetTuple(item.dbid,item.offset);
                    if(Condition(attrtype,tuple,attrid,p[4])){
                        DeleteTuple(item.blockid,item.offset);
                        topt.objectTable.remove(item);
                    }
                }
            }


        }
        */
    private boolean Condition(String attrtype,Tuple tuple,int attrid,String value1){
        String value = value1.replace("\"","");
        switch (attrtype){
            case "int":
                int value_int = Integer.parseInt(value);
                if(tuple.tuple[attrid].equals(value_int))
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


    private TupleList DirectSelect(String[] p){
        TupleList tpl = new TupleList();
        int attrnumber = Integer.parseInt(p[1]);
        String[] attrname = null;
        int[] attrid = null;
        String[] attrtype= null;
        String classname = p[2+attrnumber];
        int classid = 0;
        for(int i = 0;i < attrnumber;i++){
            for (ClassTableItem item:classt.classTable) {
                if (item.classname == classname && item.attrname.equals(p[2+i])) {
                    classid = item.classid;
                    attrid[i] = item.attrid;
                    attrtype[i] = item.attrtype;
                    attrname[i] = item.attrname;
                    break;
                }
            }
        }


        int sattrid = 0;
        String sattrtype = null;
        for (ClassTableItem item:classt.classTable) {
            if (item.classid == classid && item.attrname.equals(p[attrnumber+3])) {
                sattrid = item.attrid;
                sattrtype = item.attrtype;
                break;
            }
        }


        for(ObjectTableItem item : topt.objectTable){
            if(item.classid == classid){
                Tuple tuple = GetTuple(item.blockid,item.offset);
               if(Condition(sattrtype,tuple,sattrid,p[attrnumber+5])){
                   tpl.addTuple(tuple);
               }
            }
        }

        return tpl;

    }



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

    private void PrintSelectResult(TupleList tpl,String[] attrname,int[] attrid,String[] type) {
        Intent intent = new Intent(context, PrintResult.class);

        //todo
        Bundle bundle = new Bundle();
        bundle.putSerializable("tupleList",tpl);
        bundle.putStringArray("attrname", attrname);
        bundle.putIntArray("attrid",attrid);
        bundle.putStringArray("type", type);
        intent.putExtras(bundle);
        context.startActivity(intent);


    }
}