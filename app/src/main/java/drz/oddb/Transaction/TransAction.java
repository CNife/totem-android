package drz.oddb.Transaction;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import drz.oddb.Log.*;
import drz.oddb.Memory.*;


import drz.oddb.parseStmt.CreateSelStmt;
import drz.oddb.parseStmt.CreateStmt;
import drz.oddb.parseStmt.DeleteStmt;
import drz.oddb.parseStmt.DropStmt;
import drz.oddb.parseStmt.InsertStmt;
import drz.oddb.parseStmt.ParseforSelect;
import drz.oddb.parseStmt.ParseforUpdate;
import drz.oddb.parseStmt.RawStmt;
import drz.oddb.parseStmt.SelectStmt;
import drz.oddb.parseStmt.UpdateStmt;
import drz.oddb.parseStmt.attrcontext;
import drz.oddb.show.PrintResult;
import drz.oddb.show.ShowTable;
import drz.oddb.Transaction.SystemTable.*;

import drz.oddb.parse.*;

public class TransAction {
    public TransAction(Context context) {
        this.context = context;
        RedoRest();
    }

    Context context;
    public MemManage mem = new MemManage();

    public ObjectTable topt = mem.loadObjectTable();
    public ClassTable classt = mem.loadClassTable();
    public AttributeTable attributet = mem.loadAttributeTable();
    public DeputyTable deputyt = mem.loadDeputyTable();
    public DeputyRuleTable deputyRulet = mem.loadDeputyRuleTable();
    public BiPointerTable biPointerT = mem.loadBiPointerTable();
    public SwitchingTable switchingT = mem.loadSwitchingTable();

    LogManage log = new LogManage(this);

    public void SaveAll( )
    {
        mem.saveObjectTable(topt);
        mem.saveClassTable(classt);
        mem.saveDeputyTable(deputyt);
        mem.saveBiPointerTable(biPointerT);
        mem.saveSwitchingTable(switchingT);
        mem.saveAttributeTable(attributet);
        mem.saveDeputyRuleTable(deputyRulet);
        mem.saveLog(log.LogT);
        while(!mem.flush());
        while(!mem.setLogCheck(log.LogT.logID));
        mem.setCheckPoint(log.LogT.logID);//成功退出,所以新的事务块一定全部执行
    }

    public void Test(){
        TupleList tpl = new TupleList();
        Tuple t1 = new Tuple();
        t1.tupleHeader = 5;
        t1.tuple = new Object[t1.tupleHeader];
        t1.tuple[0] = "a";
        t1.tuple[1] = 1;
        t1.tuple[2] = "b";
        t1.tuple[3] = 3;
        t1.tuple[4] = "e";
        Tuple t2 = new Tuple();
        t2.tupleHeader = 5;
        t2.tuple = new Object[t2.tupleHeader];
        t2.tuple[0] = "d";
        t2.tuple[1] = 2;
        t2.tuple[2] = "e";
        t2.tuple[3] = 2;
        t2.tuple[4] = "v";

        tpl.addTuple(t1);
        tpl.addTuple(t2);
        String[] attrname = {"attr2","attr1","attr3","attr5","attr4"};
        int[] attrid = {1,0,2,4,3};
        String[]attrtype = {"int","char","char","char","int"};

        PrintSelectResult(tpl,attrname,attrid,attrtype);

        int[] a = InsertTuple(t1);
        Tuple t3 = GetTuple(a[0],a[1]);
        int[] b = InsertTuple(t2);
        Tuple t4 = GetTuple(b[0],b[1]);
        System.out.println(t3);
    }

    private boolean RedoRest(){//redo
        LogTable redo;
        if((redo=log.GetReDo())!=null) {
            int redonum = redo.logTable.size();   //先把redo指令加前面
            for (int i = 0; i < redonum; i++) {
                String s = redo.logTable.get(i).str;

                log.WriteLog(s);
                query(s);
            }
        }else{
            return false;
        }
        return true;
    }

    public void query(String s) {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
        parse p = new parse(byteArrayInputStream);
        RawStmt rs;
        try {
            if(s.trim().startsWith("SELECT")){
                rs = ParseforSelect.ParseSelect(s);
            }
            else if(s.trim().startsWith("UPDATE")){
                rs = ParseforUpdate.ParseUpdate(s);
            }
            else {
                rs = p.Run();
            }
            System.out.println(rs.toString());
            //System.out.println("好奇怪啊");
            if(rs.NodeTag=="CREATEORIGIN"){
                CreateStmt cs =(CreateStmt)rs;
                createOriginClass(cs);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("创建成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="CREATEDEPUTY"){
                CreateSelStmt css = (CreateSelStmt)rs;
                CreateSelectDeputy(css);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("创建成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="DROP"){
                DropStmt dp = (DropStmt)rs;
                Drop(dp);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("删除成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="INSERT"){
                InsertStmt is = (InsertStmt)rs;
                insert(is);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("插入成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="DELETE"){
                DeleteStmt ds = (DeleteStmt)rs;
                Delete(ds);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("删除成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="UPDATE"){
                UpdateStmt us = (UpdateStmt)rs;
                Update(us);
                new AlertDialog.Builder(context).setTitle("提示").setMessage("更新成功").setPositiveButton("确定",null).show();
            }
            else if(rs.NodeTag=="SELECT"){
               // System.out.println("有点奇怪");
                //System.out.println("select匹配成功");
                SelectStmt ss =(SelectStmt)rs;
                Select2(ss);
            }
            else{
                new AlertDialog.Builder(context).setTitle("提示").setMessage("特殊情况解析失败").setPositiveButton("确定",null).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //CREATE CLASS dZ123 (nB1 int,nB2 char) ;
    //1,2,dZ123,nB1,int,nB2,char
    public void createOriginClass(CreateStmt cs){
        String classname = cs.classname;
        classt.maxid++;
        int classid = classt.maxid;
        classt.classTable.add(new ClassTableItem(classname,classid,cs.cols.size(),0));

        for(int i=0;i<cs.cols.size();i++) {
            attributet.maxid++;
            attributet.attributeTable.add(new AttributeTableItem(classid,i,cs.cols.get(i).attrname,cs.cols.get(i).attrtype,0));
        }
    }


    //INSERT INTO aa VALUES (1,2,"3");
    //4,3,aa,1,2,"3"
    //0 1 2  3 4  5
    public int insert(InsertStmt is){
        int count = is.attrnames.size();
        String classname=is.classname;
        int classid =getClassid(classname);
        List<AttributeTableItem> ls= getClassAtt(classid);
        int size_num = ls.size();
        Object[] tuple_ = new Object[size_num];
        //将属性
        for(int i=0;i<is.attrnames.size();i++) {
            int pos= getpos(ls,is.attrnames.get(i));
            tuple_[pos] = is.attrvalues.get(i);
        }
        Tuple tuple = new Tuple(tuple_);
        tuple.tupleHeader=size_num;
        int[] a = InsertTuple(tuple);
        topt.maxTupleId++;
        int tupleid = topt.maxTupleId;
        topt.objectTable.add(new ObjectTableItem(classid,tupleid,a[0],a[1]));

        //向代理类加元组

        for(DeputyTableItem deputyTableItem1 :deputyt.deputyTable){
            if(classid == deputyTableItem1.originid){
                //判断代理规则


                List<AttributeTableItem> attlist = getClassAtt(classid);
                String deputyrule = null;
                //获取代理规则
                for(DeputyRuleTableItem deputyRuleTableItem1 : deputyRulet.deputyRuleTable){
                    if(deputyRuleTableItem1.ruleid == deputyTableItem1.ruleid){
                        deputyrule = deputyRuleTableItem1.deputyrule;
                        break;
                    }
                }

                HashMap<String, Integer> attMap= getRuleAtt(deputyrule,classid);

                HashMap<String, String> params = new HashMap<>();
                for(String str : attMap.keySet()){
                    params.put(str,tuple.tuple[attMap.get(str)].toString());
                }

                boolCheck boolCheck =new boolCheck();
                if(boolCheck.isEnable(deputyrule,params)){
                    InsertStmt ss= new InsertStmt();
                    int deputyclassid = 0;

                    //查找代理类类名
                    for(ClassTableItem classTableItem2:classt.classTable){
                        if(classTableItem2.classid == deputyTableItem1.deputyid) {
                            ss.classname = classTableItem2.classname;
                            deputyclassid = classTableItem2.classid;
                            break;
                        }
                    }

                    //替换属性对应值
                    for(AttributeTableItem attributeTable1 : attributet.attributeTable){
                        //if(attributeTable1.classid == deputyclassid && attributeTable1.isdeputy != 0){
                        if(attributeTable1.classid == deputyclassid ){

                            String swirule = null;
                            for(SwitchingTableItem switchingTableItem1 : switchingT.switchingTable ){
                                if(switchingTableItem1.deputyId == attributeTable1.isdeputy)
                                    swirule = switchingTableItem1.rule;
                            }

                            if(swirule == null){
                                if(attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("int"))
                                    ss.attrvalues.add("0");
                                else if(attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("char")) {
                                    ss.attrvalues.add(" ");
                                }
                                ss.attrnames.add(attributeTable1.attrname);
                                continue;
                            }

                            HashMap<String,Integer> swiattMap = getRuleAtt(swirule,classid);
                            int k =0;
                            for(int i = 0; i < is.attrnames.size(); i++){
                                if(swirule.contains(is.attrnames.get(i))){
                                    swirule=swirule.replaceAll(is.attrnames.get(i),is.attrvalues.get(i));
                                    k++;
                                }
                            }

                            if( k == swiattMap.size() ){
                                if(swirule.contains("(")||swirule.contains("{")||swirule.contains("[")){
                                    int re = calculate(swirule);
                                    ss.attrvalues.add(String.valueOf(re));
                                } else {
                                    ss.attrvalues.add(swirule);
                                }
                                ss.attrnames.add(attributeTable1.attrname);

                            }


                        }
                    }

                    if(ss.attrnames.size()>0){
                        int deojid = insert(ss);
                        //插入Bi
                        biPointerT.biPointerTable.add(new BiPointerTableItem(classid,tupleid,deputyTableItem1.deputyid,deojid));
                    }



                }
            }
        }
        return tupleid;
    }

    public int getAttrPos(int classid,String attrname ){
        for( int i=0;i<attributet.attributeTable.size();i++)
            if(attributet.attributeTable.get(i).classid==classid && attributet.attributeTable.get(i).attrname.equals(attrname))
                return attributet.attributeTable.get(i).attrid;
        System.out.println("未找到属性位置");

        return -2;
    }

    public int getBiObjectid(int originid,int deputyid,int objectid){
        for(BiPointerTableItem bi: biPointerT.biPointerTable ){
            if(bi.classid==originid && bi.deputyid ==deputyid && bi.objectid==objectid)
                return bi.deputyobjectid;
        }
        //System.out.println("未找到deputyobjectid");
        return -1;
    }

    public int getBiDeputyObjectid(int originid,int deputyid,int deputyobid){
        for(BiPointerTableItem bi: biPointerT.biPointerTable ){
            if(bi.classid==originid && bi.deputyid ==deputyid && bi.deputyobjectid==deputyobid)
                return bi.objectid;
        }
        return -1;
    }

    public String getAttrType(int classid,String attrname ){
        for( int i=0;i<attributet.attributeTable.size();i++)
            if(attributet.attributeTable.get(i).classid==classid && attributet.attributeTable.get(i).attrname.equals(attrname))
                return attributet.attributeTable.get(i).attrtype;
       // System.out.println("未找到属性类型");

        return "no";
    }

    public String getCrossType(attrcontext attrc, int objectid){
        int deputyobjectid=-1;
        int deputyclassid=-1;
        int originclassid=-1;
        for(int i=0;i<attrc.crossclass.size()-1;i++){
            originclassid = getClassid(attrc.crossclass.get(i));
            deputyclassid= getClassid(attrc.crossclass.get(i+1));
            deputyobjectid = getBiObjectid(originclassid,deputyclassid,objectid);
        }

        return getAttrType(deputyclassid,attrc.attrname);
    }



    public Object getCrossValue(attrcontext attrc,int objectid){
        int deputyobjectid=-1;
        int deputyclassid=-1;
        int originclassid=-1;
        List<Integer> biids;
        for(int i=0;i<attrc.crossclass.size()-1;i++){
            originclassid = getClassid(attrc.crossclass.get(i));
            deputyclassid= getClassid(attrc.crossclass.get(i+1));
            deputyobjectid = getBiObjectid(originclassid,deputyclassid,objectid);
        }

        int attrid =getAttrPos(deputyclassid,attrc.attrname);
        Tuple tuple =null;
        for(ObjectTableItem ob: topt.objectTable){
            if(ob.tupleid==deputyobjectid && ob.classid==deputyclassid)
                tuple = GetTuple(ob.blockid,ob.offset);
        }
        Object result =tuple.tuple[attrid];
        return result;
    }

    public int[] getobjectattr(int object_id){
        int[] two =new int[2];
        for (ObjectTableItem objectTableItem1 : topt.objectTable) {
            if(objectTableItem1.tupleid==object_id){
                two[0]=objectTableItem1.blockid;
                two[1]=objectTableItem1.offset;
            }
        }
        return two;
    }

    public List<Integer> getbpids(int originid,int deputyid,List<Integer> uplist){
        List<Integer> ls = new ArrayList();
        for (Integer i : uplist) {
                int tmp =getBiObjectid(originid,deputyid,i);
                if(tmp!=-1)
                    ls.add(tmp);
            }

        for(Integer i:uplist){
            int tmp=getBiDeputyObjectid(deputyid,originid,i);
                    if(tmp!=-1)
                        ls.add(tmp);
        }
        return ls;
    }




    public List<Tuplesp> gettuples(List<Integer> ls){
        List<Tuplesp> res= new ArrayList<Tuplesp>();
        for(ObjectTableItem objectTableItem1 : topt.objectTable)
            if(ls.contains(objectTableItem1.tupleid)){
                Tuplesp tp= new Tuplesp(GetTuple(objectTableItem1.blockid, objectTableItem1.offset),objectTableItem1.tupleid) ;
            }
        return res;
    }
    public int getattrnum(int classid){
        for( ClassTableItem a : classt.classTable)
            if(a.classid==classid)
                return a.attrnum;
            return -1;
    }

/*
    public List<Tuplesp> switchattr(List<Tuplesp> lst,int originid,int deputyid){
        for(Tuplesp a : lst ){
            Tuple tmp = new Tuple();
            int i=getattrnum(deputyid);
            Object[] tuple_ = new Object[i];
            int[] two = new int[2];
            two=getobjectattr(a.obejct_id);
            tmp=GetTuple(two[0],two[1]);
            for(AttributeTableItem attributeTable1 : attributet.attributeTable){
                //if(attributeTable1.classid == deputyclassid && attributeTable1.isdeputy != 0){
                if(attributeTable1.classid == deputyid ){

                    String swirule = null;
                    for(SwitchingTableItem switchingTableItem1 : switchingT.switchingTable ){
                        if(switchingTableItem1.deputyId == attributeTable1.isdeputy)
                            swirule = switchingTableItem1.rule;
                    }

                    if(swirule == null){
                        if(attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("int"))
                            //ss.attrvalues.add("0");
                            tuple_[attributeTable1.attrid]=tmp.tuple[attributeTable1.attrid];
                        else if(attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("char")) {
                            //ss.attrvalues.add(" ");
                            tuple_[attributeTable1.attrid]=tmp.tuple[attributeTable1.attrid];
                        }
                        //ss.attrnames.add(attributeTable1.attrname);
                        continue;
                    }

                    HashMap<String,Integer> swiattMap = getRuleAtt(swirule,originid);
                    int k =0;
                    for(int i = 0; i < is.attrnames.size(); i++){
                        if(swirule.contains(is.attrnames.get(i))){
                            swirule=swirule.replaceAll(is.attrnames.get(i),is.attrvalues.get(i));
                            k++;
                        }
                    }

                    if( k == swiattMap.size() ){
                        if(swirule.contains("(")||swirule.contains("{")||swirule.contains("[")){
                            int re = calculate(swirule);
                            ss.attrvalues.add(String.valueOf(re));
                        } else {
                            ss.attrvalues.add(swirule);
                        }
                        //ss.attrnames.add(attributeTable1.attrname);

                    }


                }
            }
        }

    }


 */

    public List<Tuplesp> getDeputyTuple(int originid,int deputyid){
        //System.out.println("Deputy!");
        List<Tuplesp> res= new ArrayList<Tuplesp>();
        for(BiPointerTableItem a : biPointerT.biPointerTable){
            if(a.classid==originid&&a.deputyid==deputyid){
                int[] two = new int[2];
                two = getobjectattr(a.objectid);
                Tuple origintuple = GetTuple(two[0],two[1]);
                Object[] tuple_ = new Object[getattrnum(deputyid)];
                two = getobjectattr(a.deputyobjectid);
                Tuple deputytuple=GetTuple(two[0],two[1]);
                for(AttributeTableItem attributeTable1 : attributet.attributeTable) {
                    //if(attributeTable1.classid == deputyclassid && attributeTable1.isdeputy != 0){
                    if (attributeTable1.classid == deputyid) {

                        String swirule = null;
                        for (SwitchingTableItem switchingTableItem1 : switchingT.switchingTable) {
                            if (switchingTableItem1.deputyId == attributeTable1.isdeputy)
                                swirule = switchingTableItem1.rule;
                        }

                        if (swirule == null) {
                            if (attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("int"))
                                //ss.attrvalues.add("0");
                                tuple_[attributeTable1.attrid] = deputytuple.tuple[attributeTable1.attrid];
                            else if (attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("char")) {
                                //ss.attrvalues.add(" ");
                                tuple_[attributeTable1.attrid] = deputytuple.tuple[attributeTable1.attrid];
                            }
                            //ss.attrnames.add(attributeTable1.attrname);
                            continue;
                        }

                        HashMap<String, Integer> swiattMap = getRuleAtt(swirule, originid);
                        //System.out.println("\n通过Map.keySet遍历key和value：");
                        for(String key:swiattMap.keySet())
                        {
                            if(swirule.contains(key))
                                swirule=swirule.replaceAll(key,origintuple.tuple[swiattMap.get(key)].toString());
                        }
                        if (swirule.contains("(") || swirule.contains("{") || swirule.contains("[")) {
                                int re = calculate(swirule);
                                tuple_[attributeTable1.attrid]=(String.valueOf(re));
                        } else {
                                //ss.attrvalues.add(swirule);
                                tuple_[attributeTable1.attrid]=(swirule);
                        }
                            //ss.attrnames.add(attributeTable1.attrname);
                    }
                }
                Tuple switchtuple = new Tuple(tuple_);
                Tuplesp sp = new Tuplesp(switchtuple,a.deputyobjectid);
                res.add(sp);
            }
        }
        return res;
    }


    public TupleList Select(SelectStmt ss){
        TupleList tpl = new TupleList();
        int classid=getClassid(ss.classname);
        //if(ss.whereclause!=null)

        boolean flag= false;
        for( attrcontext at : ss.attrs ){
            if(at.isCross==true)
                flag=true;
        }
        System.out.println("intercept1");
        if(flag==true){

            ArrayList<String> crossclass =ss.attrs.get(0).crossclass;
            String[] attrname = new String[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            int[] attrid=new int[ss.attrs.size()];
            int[] attrorder=new int[ss.attrs.size()];
            int lastclassid = getClassid(crossclass.get(crossclass.size()-1));
            for(int i=0;i<ss.attrs.size();i++){
                attrorder[i]=getAttrPos(lastclassid,ss.attrs.get(i).attrname);
                attrid[i]=i;
                attrname[i]=ss.attrs.get(i).attrname;
                attrtype[i]=getAttrType(lastclassid,ss.attrs.get(i).attrname);
            }
            List<Integer> firstlist = new ArrayList<>();
            if(ss.whereclause!=null){
                List<AttributeTableItem> ls=getExistAttr(ss.whereclause, classid);
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < ls.size(); i++) {
                        params.put(ls.get(i).attrname, tuple.tuple[ls.get(i).attrid].toString());
                    }
                    boolCheck boolcheck = new boolCheck();
                    //找到符合select条件的行
                    if (boolcheck.isEnable(ss.whereclause, params)) {
                       firstlist.add(objectTableItem1.tupleid);
                    }
                }
            }
            }
            else {
                for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                    if (objectTableItem1.classid == classid) {
                        firstlist.add(objectTableItem1.tupleid);
                    }
                }
            }
            List<Tuplesp> lst = new ArrayList<Tuplesp>();
            boolean realflag=false;
            if(getclasstype(classid)==0) {
                lst = gettuples(firstlist);
                realflag=true;
            }
            List<Integer> diverselist=firstlist;
            for(int i=0;i<crossclass.size()-1;i++){
                int originid =getClassid(crossclass.get(i));
                int deputyid=getClassid(crossclass.get(i+1));
                diverselist=getbpids(originid,deputyid,diverselist);
                if(realflag==false){
                    if(getclasstype(deputyid)==0){
                        realflag=true;
                        lst=gettuples(diverselist);
                    }
                }
                else{

                }
            }
            for(int i=0;i<diverselist.size();i++) {
                int[] two=getobjectattr(diverselist.get(i));
                Tuple tupletmp = GetTuple(two[0], two[1]);
                Object[] tuple_=new Object[ss.attrs.size()];
                for(int j=0;j<ss.attrs.size();j++){
                    tuple_[j]=tupletmp.tuple[attrorder[j]];
                    String tmp = (String) tuple_[i];
                    tmp = tmp.replaceAll("\"", "");
                    tuple_[i] = tmp;
                }
                Tuple resulttuple = new Tuple(tuple_);
                tpl.addTuple(resulttuple);
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }




        if(ss.whereclause!=null) {
            List<AttributeTableItem> ls = getExistAttr(ss.whereclause, classid);
            //List<AttributeTableItem> attls = new ArrayList();
            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];

            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }
            System.out.println("intercept");
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < ls.size(); i++) {
                        params.put(ls.get(i).attrname, tuple.tuple[ls.get(i).attrid].toString());
                    }
                    boolCheck boolcheck = new boolCheck();
                    //找到符合select条件的行
                    if (boolcheck.isEnable(ss.whereclause, params)) {
                        Object[] tuple_ = new Object[posmark.length];
                        for (int i = 0; i < posmark.length; i++) {
                            if (posmark[i] >= 0) {
                                tuple_[i] = tuple.tuple[posmark[i]];
                                //去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;
                            } else {
                                tuple_[i] = getCrossValue(ss.attrs.get(i), objectTableItem1.tupleid);
                                attrtype[i] = getCrossType(ss.attrs.get(i), objectTableItem1.tupleid);
//去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;

                            }
                        }
                        Tuple resulttuple = new Tuple(tuple_);
                        tpl.addTuple(resulttuple);
                    }
                }
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }
        else{

            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];

            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }

            System.out.println("intercept");
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    //找到符合select条件的行

                        Object[] tuple_ = new Object[posmark.length];
                        for (int i = 0; i < posmark.length; i++) {
                            if (posmark[i] >= 0) {
                                tuple_[i] = tuple.tuple[posmark[i]];
                                //去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;
                            } else {
                                tuple_[i] = getCrossValue(ss.attrs.get(i), objectTableItem1.tupleid);
                                attrtype[i] = getCrossType(ss.attrs.get(i), objectTableItem1.tupleid);
//去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;

                            }
                        }
                        Tuple resulttuple = new Tuple(tuple_);
                        tpl.addTuple(resulttuple);
                    }

            }

            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;

        }

    }

    public ClassTableItem getclassitem(String classname){
        for(int i=0;i<classt.classTable.size();i++)
            if(classname.equals(classt.classTable.get(i).classname))
                return classt.classTable.get(i);
        System.out.println("不存在该类名");
        return null;

    }

    public List<Tuplesp> getOriginTuple(int classid,String whereclause){
        List<Tuplesp> res = new ArrayList();
        List<AttributeTableItem> ls=getExistAttr(whereclause, classid);
        if(whereclause==null) {
            int attrnum =getattrnum(classid);
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);

                    Object[] tuple_ = new Object[attrnum];
                    for (int i = 0; i < attrnum; i++) {
                        tuple_[i] = tuple.tuple[i];
                        //去掉引号
                        String tmp = (String) tuple_[i];
                        tmp = tmp.replaceAll("\"", "");
                        tuple_[i] = tmp;
                    }
                    Tuple resulttuple = new Tuple(tuple_);
                    Tuplesp sp = new Tuplesp(resulttuple,objectTableItem1.tupleid);
                    res.add(sp);

                }
            }
            return res;
        }
        else{
            int attrnum =getattrnum(classid);
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < ls.size(); i++) {
                        params.put(ls.get(i).attrname, tuple.tuple[ls.get(i).attrid].toString());
                    }
                    boolCheck boolcheck = new boolCheck();
                    if(boolcheck.isEnable(whereclause,params)) {
                          Object[] tuple_ = new Object[attrnum];
                          for (int i = 0; i < attrnum; i++) {
                            tuple_[i] = tuple.tuple[i];
                            //去掉引号
                            String tmp = (String) tuple_[i];
                            tmp = tmp.replaceAll("\"", "");
                            tuple_[i] = tmp;
                        }
                        Tuple resulttuple = new Tuple(tuple_);
                        Tuplesp sp = new Tuplesp(resulttuple,objectTableItem1.tupleid);
                        res.add(sp);
                    }
                }
            }
            return res;
        }
    }

    public List<Tuplesp> getCrossTuples(int originid,int deputyid,List<Tuplesp> ls){
        int attrnum = getattrnum(deputyid);
        List<Tuplesp> res = new ArrayList();
        for(Tuplesp a : ls){
            int newid =getBiObjectid(originid,deputyid,a.object_id);
            int[] two = new int[2];
            two=getobjectattr(newid);
            Object[] tuple_ = new Object[attrnum];
            Tuple deputytuple = GetTuple(two[0],two[1]);

            for(AttributeTableItem attributeTable1 : attributet.attributeTable) {
                //if(attributeTable1.classid == deputyclassid && attributeTable1.isdeputy != 0){
                if (attributeTable1.classid == deputyid) {

                    String swirule = null;
                    for (SwitchingTableItem switchingTableItem1 : switchingT.switchingTable) {
                        if (switchingTableItem1.deputyId == attributeTable1.isdeputy)
                            swirule = switchingTableItem1.rule;
                    }

                    if (swirule == null) {
                        if (attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("int"))
                            //ss.attrvalues.add("0");
                            tuple_[attributeTable1.attrid] = deputytuple.tuple[attributeTable1.attrid];
                        else if (attributeTable1.isdeputy == 0 && attributeTable1.attrtype.equals("char")) {
                            //ss.attrvalues.add(" ");
                            tuple_[attributeTable1.attrid] = deputytuple.tuple[attributeTable1.attrid];
                        }
                        //ss.attrnames.add(attributeTable1.attrname);
                        continue;
                    }

                    HashMap<String, Integer> swiattMap = getRuleAtt(swirule, originid);
                    //System.out.println("\n通过Map.keySet遍历key和value：");
                    for(String key:swiattMap.keySet())
                    {
                        if(swirule.contains(key))
                            swirule=swirule.replaceAll(key,a.tuple.tuple[swiattMap.get(key)].toString());
                    }
                    if (swirule.contains("(") || swirule.contains("{") || swirule.contains("[")) {
                        int re = calculate(swirule);
                        tuple_[attributeTable1.attrid]=(String.valueOf(re));
                    } else {
                        //ss.attrvalues.add(swirule);
                        tuple_[attributeTable1.attrid]=(swirule);
                    }
                    //ss.attrnames.add(attributeTable1.attrname);
                }
            }
            Tuple switchtuple = new Tuple(tuple_);
            Tuplesp sp = new Tuplesp(switchtuple,newid);
            res.add(sp);
        }

        return res;
    }

    public List<Tuplesp> getReverseTuples(int originid,int deputyid,List<Tuplesp> ls){

        List<Tuplesp> res = new ArrayList();
       for(Tuplesp a : ls){
           int newid=getBiDeputyObjectid(originid,deputyid,a.object_id);
           if(newid!=-1){
               int[] two = new int[2];
               two=getobjectattr(newid);
               Tuple origintuple = GetTuple(two[0],two[1]);
               Tuplesp sp = new Tuplesp(origintuple,newid);
               res.add(sp);
           }
       }
       return res;

    }

    public TupleList Select2(SelectStmt ss){
        TupleList tpl = new TupleList();
        int classid=getClassid(ss.classname);
        int orginclassid;
        //if(ss.whereclause!=null)

        boolean flag= false;
        for( attrcontext at : ss.attrs ){
            if(at.isCross==true)
                flag=true;
        }
        System.out.println("intercept1");
        if(flag==true){
            ArrayList<String> crossclass =ss.attrs.get(0).crossclass;
            String[] attrname = new String[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            int[] attrid=new int[ss.attrs.size()];
            int[] attrorder=new int[ss.attrs.size()];
            int lastclassid = getClassid(crossclass.get(crossclass.size()-1));
            for(int i=0;i<ss.attrs.size();i++){
                attrorder[i]=getAttrPos(lastclassid,ss.attrs.get(i).attrname);
                attrid[i]=i;
                attrname[i]=ss.attrs.get(i).attrname;
                attrtype[i]=getAttrType(lastclassid,ss.attrs.get(i).attrname);
            }
            List<Integer> firstlist = new ArrayList<>();
            ClassTableItem classinfo = getclassitem(ss.classname);
            List<Tuplesp> origintuples;
            boolean originflag=true;
            if(classinfo.classtype==0){
                orginclassid=classid;
                origintuples =getOriginTuple(classid,ss.whereclause);
            }
            else{
                origintuples = new ArrayList();
                DeputyTableItem dp= getDeputy(classid);
                List<Tuplesp> tmpsp=getDeputyTuple(dp.originid,classid);
                orginclassid=dp.originid;

                List<AttributeTableItem> ls=getExistAttr(ss.whereclause, classid);
                for(Tuplesp a : tmpsp){
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < ls.size(); i++) {
                        params.put(ls.get(i).attrname, a.tuple.tuple[ls.get(i).attrid].toString());
                    }
                    boolCheck boolcheck = new boolCheck();
                    if(boolcheck.isEnable(ss.whereclause,params)){
                        origintuples.add(a);
                    }

                }
                origintuples=getReverseTuples(dp.originid,classid,origintuples);

            }
            int startindex=0;
            for(int i=0;i<crossclass.size();i++) {
                String tmpname =getclassname(orginclassid);
                if (crossclass.get(i).equals(tmpname)) {
                    startindex = i;
                    break;
                }
            }
            List<Tuplesp> crosstuples=origintuples;
            for(int i=startindex;i<crossclass.size()-1;i++){
                int originid =getClassid(crossclass.get(i));
                int deputyid=getClassid(crossclass.get(i+1));
                crosstuples= getCrossTuples(originid,deputyid,crosstuples);
            }
            System.out.println("cross!");
            for(int i=0;i<crosstuples.size();i++) {
                Tuple tupletmp = crosstuples.get(i).tuple;
                Object[] tuple_=new Object[ss.attrs.size()];
                for(int j=0;j<ss.attrs.size();j++){
                    tuple_[j]=tupletmp.tuple[attrorder[j]];
                    String tmp = (String) tuple_[i];
                    tmp = tmp.replaceAll("\"", "");
                    tuple_[i] = tmp;
                }
                Tuple resulttuple = new Tuple(tuple_);
                tpl.addTuple(resulttuple);
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }

        ClassTableItem classinfo =getclassitem(ss.classname);

        if(classinfo.classtype==0 && ss.whereclause==null){
            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];
            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }
            System.out.println("real!");
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    //找到符合select条件的行

                    Object[] tuple_ = new Object[posmark.length];
                    for (int i = 0; i < posmark.length; i++) {

                        tuple_[i] = tuple.tuple[posmark[i]];
                        //去掉引号
                        String tmp = (String) tuple_[i];
                        tmp = tmp.replaceAll("\"", "");
                        tuple_[i] = tmp;

                    }
                    Tuple resulttuple = new Tuple(tuple_);
                    tpl.addTuple(resulttuple);
                }

            }

            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }
        else if(classinfo.classtype==0 && ss.whereclause!=null){
            List<AttributeTableItem> ls = getExistAttr(ss.whereclause, classid);
            //List<AttributeTableItem> attls = new ArrayList();
            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];

            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }
            System.out.println("real where!");
            for (ObjectTableItem objectTableItem1 : topt.objectTable) {
                if (objectTableItem1.classid == classid) {
                    Tuple tuple = GetTuple(objectTableItem1.blockid, objectTableItem1.offset);
                    HashMap<String, String> params = new HashMap<>();
                    for (int i = 0; i < ls.size(); i++) {
                        params.put(ls.get(i).attrname, tuple.tuple[ls.get(i).attrid].toString());
                    }
                    boolCheck boolcheck = new boolCheck();
                    //找到符合select条件的行
                    if (boolcheck.isEnable(ss.whereclause, params)) {
                        Object[] tuple_ = new Object[posmark.length];
                        for (int i = 0; i < posmark.length; i++) {
                            if (posmark[i] >= 0) {
                                tuple_[i] = tuple.tuple[posmark[i]];
                                //去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;
                            } else {
                                tuple_[i] = getCrossValue(ss.attrs.get(i), objectTableItem1.tupleid);
                                attrtype[i] = getCrossType(ss.attrs.get(i), objectTableItem1.tupleid);
//去掉引号
                                String tmp = (String) tuple_[i];
                                tmp = tmp.replaceAll("\"", "");
                                tuple_[i] = tmp;

                            }
                        }
                        Tuple resulttuple = new Tuple(tuple_);
                        tpl.addTuple(resulttuple);
                    }
                }
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }
        else if(classinfo.classtype!=0&&ss.whereclause==null){
            List<Tuple> res = new ArrayList();
            DeputyTableItem dp= getDeputy(classid);
            List<Tuplesp> lst =getDeputyTuple(dp.originid,dp.deputyid);
            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];
            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }
            System.out.println("deputy!");
            for(Tuplesp a : lst){
                Object[] tuple_ = new Object[posmark.length];
                for (int i = 0; i < posmark.length; i++) {
                    tuple_[i] = a.tuple.tuple[posmark[i]];
                    String tmp = (String) tuple_[i];
                    tmp = tmp.replaceAll("\"", "");
                    tuple_[i] = tmp;
                   }
                Tuple resulttuple = new Tuple(tuple_);
                tpl.addTuple(resulttuple);
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }
        else {
            List<AttributeTableItem> ls = getExistAttr(ss.whereclause, classid);
            List<Tuple> res = new ArrayList();
            DeputyTableItem dp= getDeputy(classid);
            List<Tuplesp> lst =getDeputyTuple(dp.originid,dp.deputyid);
            //List<AttributeTableItem> attls = new ArrayList();
            int[] posmark = new int[ss.attrs.size()];
            String[] attrname = new String[ss.attrs.size()];

            int[] attrid = new int[ss.attrs.size()];
            String[] attrtype = new String[ss.attrs.size()];
            for (int i = 0; i < ss.attrs.size(); i++) {
                attrname[i] = ss.attrs.get(i).attrname;
                attrid[i] = i;
                attrtype[i] = getAttrType(classid, ss.attrs.get(i).attrname);
                if (ss.attrs.get(i).isCross == false)
                    posmark[i] = getAttrPos(classid, ss.attrs.get(i).attrname);
                else
                    posmark[i] = -1;
            }
            System.out.println("deputy!");
            for(Tuplesp a : lst){
                HashMap<String, String> params = new HashMap<>();
                for (int i = 0; i < ls.size(); i++) {
                    params.put(ls.get(i).attrname, a.tuple.tuple[ls.get(i).attrid].toString());
                }
                boolCheck boolcheck = new boolCheck();
                //找到符合select条件的行
                if (boolcheck.isEnable(ss.whereclause, params)) {
                    Object[] tuple_ = new Object[posmark.length];
                    for (int i = 0; i < posmark.length; i++) {
                        tuple_[i] = a.tuple.tuple[posmark[i]];
                        //去掉引号
                        String tmp = (String) tuple_[i];
                        tmp = tmp.replaceAll("\"", "");
                        tuple_[i] = tmp;
                    }
                    Tuple resulttuple = new Tuple(tuple_);
                    tpl.addTuple(resulttuple);
                }
            }
            PrintSelectResult(tpl,attrname,attrid,attrtype);
            return tpl;
        }

    }

    private int getclasstype(int classid){
        for( ClassTableItem a:classt.classTable){
            if(a.classid==classid)
                return a.classtype;
        }
        return 2;
    }

    private DeputyTableItem getDeputy(int id){
        for(DeputyTableItem a : deputyt.deputyTable){
            if(a.deputyid==id)
                return a;
        }
        return null;
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
    public void Delete(DeleteStmt ds){
        String classname=ds.classname;
        int classid=getClassid(classname);
        List<AttributeTableItem> ls = getExistAttr(ds.whereclause,classid);

        OandB ob2 = new OandB();
        for(ObjectTableItem objectTableItem1 : topt.objectTable) {
            if(objectTableItem1.classid == classid){
                Tuple tuple = GetTuple(objectTableItem1.blockid,objectTableItem1.offset);
                HashMap<String, String> params = new HashMap<>();
                for( int i=0;i<ls.size();i++){
                    params.put(ls.get(i).attrname,tuple.tuple[ls.get(i).attrid].toString());
                }
                boolCheck boolcheck = new boolCheck();
                if(boolcheck.isEnable(ds.whereclause,params)){
                    OandB ob = new OandB(DeletebyID(objectTableItem1.tupleid));
                    for(ObjectTableItem obj:ob.o){
                        ob2.o.add(obj);
                    }
                    for(BiPointerTableItem bip:ob.b){
                        ob2.b.add(bip);
                    }
                }
            }
        }
        for(ObjectTableItem obj:ob2.o){
            topt.objectTable.remove(obj);
        }
        for(BiPointerTableItem bip:ob2.b) {
            biPointerT.biPointerTable.remove(bip);
        }
    }


    private OandB DeletebyID(int id){

        List<ObjectTableItem> todelete1 = new ArrayList<>();
        List<ObjectTableItem> todelete3 = new ArrayList<>();
        List<BiPointerTableItem>todelete2 = new ArrayList<>();
        OandB ob = new OandB(todelete1,todelete3,todelete2);
        for (Iterator it1 = topt.objectTable.iterator(); it1.hasNext();){
            ObjectTableItem item  = (ObjectTableItem)it1.next();
            if(item.tupleid == id){
                //需要删除的tuple


                //删除代理类的元组
                int deobid = 0;

                for(Iterator it = biPointerT.biPointerTable.iterator(); it.hasNext();){
                    BiPointerTableItem item1 =(BiPointerTableItem) it.next();
                    if(item.tupleid == item1.deputyobjectid){
                        //it.remove();
                      if(!todelete2.contains(item1))
                            todelete2.add(item1);
                    }
                    if(item.tupleid == item1.objectid){
                        deobid = item1.deputyobjectid;
                        OandB ob2=new OandB(DeletebyID(deobid));

                        for(ObjectTableItem obj:ob2.o){
                            if(!todelete1.contains(obj))
                                todelete1.add(obj);
                        }
                        for(BiPointerTableItem bip:ob2.b){
                            if(!todelete2.contains(bip))
                                todelete2.add(bip);
                        }

                        //biPointerT.biPointerTable.remove(item1);

                    }
                }


                //删除自身
                DeleteTuple(item.blockid,item.offset);
                if(!todelete2.contains(item))
                    todelete1.add(item);

            }
        }

        return ob;
    }

    //DROP CLASS asd;
    //3,asd

    private void Drop(DropStmt dp){
        List<DeputyTableItem> dti;
        List<DeputyRuleTableItem> dtir = new ArrayList<>();
        dti = Drop1(dp);
        for(DeputyTableItem item:dti){
            for(DeputyRuleTableItem deputyRuleTableItem:deputyRulet.deputyRuleTable){
                if(deputyRuleTableItem.ruleid==item.ruleid)
                    dtir.add(deputyRuleTableItem);
            }
            deputyt.deputyTable.remove(item);
        }
        for(DeputyRuleTableItem deputyRuleTableItem1:dtir){
            deputyRulet.deputyRuleTable.remove(deputyRuleTableItem1);
        }

    }

    private List<DeputyTableItem> Drop1(DropStmt dp){
        String classname = dp.classname;
        int classid = 0;
        //找到classid顺便 清除类表和switch表
        for (Iterator it1 = classt.classTable.iterator(); it1.hasNext();) {
            ClassTableItem item =(ClassTableItem) it1.next();
            if (item.classname.equals(classname) ){
                classid = item.classid;
                for(Iterator it2 = attributet.attributeTable.iterator(); it2.hasNext();){
                    AttributeTableItem attributeTableItem = (AttributeTableItem) it2.next();
                    if(attributeTableItem.classid == classid){
                        for(Iterator it = switchingT.switchingTable.iterator(); it.hasNext();) {
                            SwitchingTableItem item2 =(SwitchingTableItem) it.next();
                            if (item2.deputyId == attributeTableItem.isdeputy){
                                it.remove();
                            }
                        }
                        it2.remove();
                    }
                }
                it1.remove();
            }
        }
        //清元组表同时清了bi
        OandB ob2 = new OandB();
        for(ObjectTableItem item1:topt.objectTable){
            if(item1.classid == classid){
                OandB ob = DeletebyID(item1.tupleid);
                for(ObjectTableItem obj:ob.o){
                    ob2.o.add(obj);
                }
                for(BiPointerTableItem bip:ob.b){
                    ob2.b.add(bip);
                }
            }
        }
        for(ObjectTableItem obj:ob2.o){
            topt.objectTable.remove(obj);
        }
        for(BiPointerTableItem bip:ob2.b) {
            biPointerT.biPointerTable.remove(bip);
        }

        //清deputy
        List<DeputyTableItem> dti = new ArrayList<>();
        for(DeputyTableItem item3:deputyt.deputyTable){
            if(item3.deputyid == classid){
                if(!dti.contains(item3))
                    dti.add(item3);
            }
            if(item3.originid == classid){
                //删除代理类
                DropStmt dp2 = dp;

                for(ClassTableItem item5: classt.classTable) {
                    if (item5.classid == item3.deputyid) {
                        dp2.classname = item5.classname;
                        List<DeputyTableItem> dti2 = Drop1(dp2);
                        for(DeputyTableItem item8:dti2){
                            if(!dti.contains(item8))
                                dti.add(item8);
                        }
                    }
                }
                if(!dti.contains(item3))
                    dti.add(item3);
            }
        }
        return dti;

    }


    //SELECT  b1 AS c1,b2 AS c2,b3 AS c3 FROM  bb WHERE t1="1";
    //6,3,b1,  c1,b2,c2,b3,c3,bb,t1,=,"1"
    //0 1 2    3  4  5  6  7  8  9 10 11


    //CREATE SELECTDEPUTY nandb SELECT name AS n1,(age+5) AS birth,salary AS s1 FROM company WHERE age=20;
    //2,3,nandb,name,n1,(age+5),birth,salary,s1,company,age,=,20
    //0 1 2     3    4   5      6       7     8     9   10  11 12
    private void CreateSelectDeputy(CreateSelStmt css) {

        String classname = css.classname;
        classt.maxid++;

        int classid = classt.maxid;
        int originclassid = getClassid(css.originname);

        int attrNum = css.relattrs.size()+css.deputyattrs.size();

        classt.classTable.add(new ClassTableItem(classname,classid,attrNum,1));
        deputyRulet.deputyRuleTable.add(new DeputyRuleTableItem(deputyRulet.maxid,css.whereclause));
        deputyt.deputyTable.add(new DeputyTableItem(originclassid,classid,deputyRulet.maxid++));

        int attrid = 0;
        for(int i=0;i<css.relattrs.size();i++) {
            attributet.attributeTable.add(new AttributeTableItem(classid,attrid,css.relattrs.get(i).attrname,css.relattrs.get(i).attrtype,0));
            attrid ++;
        }

        for(int i=0;i<css.deputyattrs.size();i++) {
            attributet.attributeTable.add(new AttributeTableItem(classid,attrid,css.deputyattrs.get(i).deputyname,"char",switchingT.maxid));
            attrid ++;
            switchingT.switchingTable.add(new SwitchingTableItem(switchingT.maxid++,css.deputyattrs.get(i).switchrule));
        }

        HashMap<String, Integer> attMap= getRuleAtt(css.whereclause,originclassid);
        List<AttributeTableItem> attlist = getClassAtt(originclassid);
        List<ObjectTableItem> obj = new ArrayList<>();

        for(ObjectTableItem objectTableItem1 : topt.objectTable){
            if(objectTableItem1.classid == originclassid){
                Tuple tuple = GetTuple(objectTableItem1.blockid,objectTableItem1.offset);
                HashMap<String, String> params = new HashMap<>();
                for(String str : attMap.keySet()){
                    params.put(str,tuple.tuple[attMap.get(str)].toString());
                }
                boolCheck boolCheck =new boolCheck();
                if(boolCheck.isEnable(css.whereclause,params)){
                    Tuple ituple = new Tuple();
                    ituple.tupleHeader = attrNum;
                    ituple.tuple = new Object[attrNum];

                    int tid = 0;
                    for(int i=0;i<css.relattrs.size();i++) {
                        if(css.relattrs.get(i).attrtype.equals("int"))
                            ituple.tuple[tid] = 0;
                        else
                            ituple.tuple[tid] = " ";
                        tid++;
                    }

                    for(int i=0;i<css.deputyattrs.size();i++) {
                        String exp = css.deputyattrs.get(i).switchrule;
                        for(AttributeTableItem attributeTableItem1 : attlist){
                            if(exp.contains(attributeTableItem1.attrname)){
                                exp = exp.replaceAll(attributeTableItem1.attrname,(String)tuple.tuple[attributeTableItem1.attrid]);
                            }
                        }
                        if(exp.contains("(")||exp.contains("{")||exp.contains("[")){
                            int re = calculate(exp);
                            ituple.tuple[tid] = re;
                        } else {
                            ituple.tuple[tid] = exp;
                        }
                        tid++;
                    }

                    topt.maxTupleId++;
                    int tupid = topt.maxTupleId;

                    int [] result = InsertTuple(ituple);
                    obj.add(new ObjectTableItem(classid,tupid,result[0],result[1]));

                    biPointerT.biPointerTable.add(new BiPointerTableItem(originclassid,objectTableItem1.tupleid,classid,topt.maxTupleId));

                }

            }
        }

        topt.objectTable.addAll(obj);
    }

    //SELECT popSinger -> singer.nation  FROM popSinger WHERE singerName = "JayZhou";
    //7,2,popSinger,singer,nation,popSinger,singerName,=,"JayZhou"
    //0 1 2         3      4      5         6          7  8


    //UPDATE Song SET type = ‘jazz’WHERE songId = 100;
    //OPT_CREATE_UPDATE，Song，type，“jazz”，songId，=，100
    //0                  1     2      3        4      5  6
    private void Update(UpdateStmt us){
        String classname = us.classname;

        int classid = 0;

        for(ClassTableItem item :classt.classTable){
            if (item.classname.equals(classname)){
                classid = item.classid;
                break;
            }
        }
        HashMap<String, Integer> attMap= getRuleAtt(us.whereclause,classid);

        OandB ob2 = new OandB();
        Iterator it1 = topt.objectTable.iterator();
        while(it1.hasNext()){
            ObjectTableItem item3 = (ObjectTableItem)it1.next();
            if(item3.classid == classid){
                Tuple tuple = GetTuple(item3.blockid,item3.offset);
                HashMap<String, String> params = new HashMap<>();
                for(String str : attMap.keySet()){
                    params.put(str,tuple.tuple[attMap.get(str)].toString());
                }
                boolCheck boolCheck =new boolCheck();
                if(boolCheck.isEnable(us.whereclause,params)){
                    UpdatebyID(ob2, classid, item3.tupleid,us);

                }
            }
        }
        for(ObjectTableItem obj:ob2.o){
            topt.objectTable.remove(obj);
        }
        for(ObjectTableItem obj:ob2.o2){
            topt.objectTable.add(obj);
        }
        for(BiPointerTableItem bip:ob2.b){
            biPointerT.biPointerTable.remove(bip);
        }
    }

    private void UpdatebyID(OandB ob2, int classid, int tupleid, UpdateStmt us){


        for (Iterator it1 = topt.objectTable.iterator(); it1.hasNext();){
            ObjectTableItem item = (ObjectTableItem)it1.next();
            if(item.tupleid ==tupleid && item.classid == classid){
                Tuple tuple = GetTuple(item.blockid,item.offset);
                Tuple tupleOld = GetTuple(item.blockid,item.offset);

                //更新元组值

                for(AttributeTableItem attributeTableItem1 : attributet.attributeTable){
                    for(int i = 0; i < us.attrs.size(); i++){
                        if( us.attrs.get(i).equals(attributeTableItem1.attrname)&&attributeTableItem1.classid==classid)
                            tuple.tuple[attributeTableItem1.attrid] =  us.values.get(i);
                    }
                }
                UpateTuple(tuple,item.blockid,item.offset);
//                Tuple tuple1 = GetTuple(item.blockid,item.offset);
//                UpateTuple(tuple1,item.blockid,item.offset);


                for(DeputyTableItem item2:deputyt.deputyTable) {

                    if(classid == item2.originid ){

                        String deputyrule = null;

                        for(DeputyRuleTableItem deputyRuleTableItem1 : deputyRulet.deputyRuleTable){
                            if(deputyRuleTableItem1.ruleid == item2.ruleid)
                                deputyrule=deputyRuleTableItem1.deputyrule;
                        }

                        HashMap<String, Integer> attMap= getRuleAtt(deputyrule,classid);

                        HashMap<String, String> params = new HashMap<>();
                        HashMap<String, String> paramsOld = new HashMap<>();
                        for(String str : attMap.keySet()){
                            params.put(str,tuple.tuple[attMap.get(str)].toString());
                        }
                        for(String str : attMap.keySet()){
                            paramsOld.put(str,tupleOld.tuple[attMap.get(str)].toString());
                        }
                        boolCheck boolCheck =new boolCheck();
                        if(boolCheck.isEnable(deputyrule,params) && boolCheck.isEnable(deputyrule,paramsOld)){
                            UpdateStmt us2 = new UpdateStmt();
                            for(BiPointerTableItem item1: biPointerT.biPointerTable) {
                                if (item1.objectid == tupleid && item1.classid == classid) {
                                    for(AttributeTableItem item4:attributet.attributeTable){
                                        if(item4.isdeputy != 0){
                                            String swirule = null;
                                            for(SwitchingTableItem switchingTableItem1 : switchingT.switchingTable ){
                                                if(switchingTableItem1.deputyId == item4.isdeputy)
                                                    swirule = switchingTableItem1.rule;
                                            }

                                            HashMap<String,Integer> swiattMap = getRuleAtt(swirule,classid);

                                            for(String name: swiattMap.keySet()){
                                                System.out.println("不对劲"+tuple.tuple[swiattMap.get(name)].toString());
                                                swirule=swirule.replaceAll(name,tuple.tuple[swiattMap.get(name)].toString());
                                            }

                                            if(swirule.contains("(")||swirule.contains("{")||swirule.contains("[")){
                                                int re = calculate(swirule);
                                                us2.values.add(String.valueOf(re));
                                            } else {
                                                us2.values.add(swirule);
                                            }

                                            us2.attrs.add(item4.attrname);
                                        }else{
                                            if(item4.attrtype.equals("int"))
                                                us2.values.add("0");
                                            else if(item4.attrtype.equals("char")) {
                                                us2.values.add(" ");
                                            }
                                            us2.attrs.add(item4.attrname);
                                        }
                                    }
                                    UpdatebyID(ob2,item1.deputyid,item1.deputyobjectid, us2);
                                }
                            }
                        } else if(!boolCheck.isEnable(deputyrule,params) && boolCheck.isEnable(deputyrule,paramsOld)){

                            for(BiPointerTableItem item1: biPointerT.biPointerTable) {
                                if (item1.objectid == tupleid) {
                                    OandB ob =new OandB(DeletebyID(item1.deputyobjectid));
                                    for(ObjectTableItem obje:ob.o){
                                        ob2.o.add(obje);
                                    }
                                    for(BiPointerTableItem bip:ob.b){
                                        ob2.b.add(bip);
                                    }
                                }
                            }

                        }else if(boolCheck.isEnable(deputyrule,params) && !boolCheck.isEnable(deputyrule,paramsOld)){
                            topt.maxTupleId++;
                            int Dtupid = topt.maxTupleId;


                            for(ClassTableItem item4:classt.classTable){
                                if(item4.classid==item2.deputyid){
                                    Tuple t = new Tuple();
                                    t.tupleHeader = item4.attrnum;
                                    t.tuple = new Object[t.tupleHeader];

                                    for(AttributeTableItem attributeTableItem1:attributet.attributeTable){
                                        if(attributeTableItem1.classid==item4.classid&&attributeTableItem1.isdeputy != 0){
                                            String swirule = null;
                                            for(SwitchingTableItem switchingTableItem1 : switchingT.switchingTable ){
                                                if(switchingTableItem1.deputyId == attributeTableItem1.isdeputy)
                                                    swirule = switchingTableItem1.rule;
                                            }

                                            HashMap<String,Integer> swiattMap = getRuleAtt(swirule,classid);

                                            for(String name: swiattMap.keySet()){
                                                swirule=swirule.replaceAll(name,tuple.tuple[swiattMap.get(name)].toString());
                                            }
                                            if(swirule.contains("(")||swirule.contains("{")||swirule.contains("[")){
                                                int re = calculate(swirule);
                                                t.tuple[attributeTableItem1.attrid] = re;
                                            } else {
                                                t.tuple[attributeTableItem1.attrid] = swirule;
                                            }
                                        }else if(attributeTableItem1.classid==item4.classid){
                                            if(attributeTableItem1.attrtype.equals("int"))
                                                t.tuple[attributeTableItem1.attrid] = "0";
                                            else if(attributeTableItem1.attrtype.equals("char")) {
                                                t.tuple[attributeTableItem1.attrid] = " ";
                                            }
                                        }
                                    }
                                    int [] result = InsertTuple(t);
                                    ob2.o2.add(new ObjectTableItem(item2.deputyid,Dtupid,result[0],result[1]));

                                    //bi
                                    biPointerT.biPointerTable.add(new BiPointerTableItem(classid,tupleid,item2.deputyid,Dtupid));
                                }
                            }

                        }
                    }
                }

            }
        }


    }




        //INSERT INTO aa VALUES (1,2,"3");
        //4,3,aa,1,2,"3"







    private class OandB{
        public List<ObjectTableItem> o= new ArrayList<>();
        public List<ObjectTableItem> o2= new ArrayList<>();
        public List<BiPointerTableItem> b= new ArrayList<>();
        public OandB(){}
        public OandB(OandB oandB){
            this.o = oandB.o;
            this.o2 = oandB.o2;
            this.b = oandB.b;
        }

        public OandB(List<ObjectTableItem> o,List<ObjectTableItem> o2, List<BiPointerTableItem> b) {
            this.o = o;
            this.o2 = o2;
            this.b = b;
        }
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

    private void UpateTuple(Tuple tuple,int blockid,int offset){
        mem.UpateTuple(tuple,blockid,offset);
    }

    private void PrintTab(ObjectTable topt,SwitchingTable switchingT,DeputyTable deputyt,BiPointerTable biPointerT,ClassTable classTable, DeputyRuleTable deputyrulet, AttributeTable attributeTable) {
        Intent intent = new Intent(context, ShowTable.class);

        Bundle bundle0 = new Bundle();
        bundle0.putSerializable("ObjectTable",topt);
        bundle0.putSerializable("SwitchingTable",switchingT);
        bundle0.putSerializable("DeputyTable",deputyt);
        bundle0.putSerializable("BiPointerTable",biPointerT);
        bundle0.putSerializable("ClassTable",classTable);
        bundle0.putSerializable("DeputyRuleTable",deputyrulet);
        bundle0.putSerializable("AttributeTable",attributeTable);
        intent.putExtras(bundle0);
        context.startActivity(intent);


    }

    private void PrintSelectResult(TupleList tpl, String[] attrname, int[] attrid, String[] type) {
        Intent intent = new Intent(context, PrintResult.class);


        Bundle bundle = new Bundle();
        bundle.putSerializable("tupleList", tpl);
        bundle.putStringArray("attrname", attrname);
        bundle.putIntArray("attrid", attrid);
        bundle.putStringArray("type", type);
        intent.putExtras(bundle);
        context.startActivity(intent);


    }
    public void PrintTab(){
        PrintTab(topt,switchingT,deputyt,biPointerT,classt,deputyRulet,attributet);
    }




    public int calculate(String strExpression)
    {
        String s = simplify(strExpression);
        System.out.println("s : "+s);
        String numStr = "";//记录数字
        Stack<Character> opeStack = new Stack<>();//符号站
        int l = s.length();//字符串长度 l
        List<String> list = new ArrayList<>();

        for(int i=0;i<l;i++)
        {
            char ch = s.charAt(i);

            if(isAllOpe(ch))
            {
                if(numStr!="")
                {
                    list.add(numStr);
                    numStr="";
                }


                if(ch=='(')
                {
                    opeStack.push(ch);
                }
                else if(isOpe(ch))
                {
                    char top = opeStack.peek();
                    if(isGreater(ch, top))
                    // ch优先级大于top 压栈
                    {
                        opeStack.push(ch);
                    }
                    else
                    //否则,将栈内元素出栈,直到遇见 '(' 然后将ch压栈
                    {
                        while(true)
                        //必须先判断一下 后出栈 否则会有空栈异常
                        {
                            char t=opeStack.peek();
                            if(t=='(')
                                break;
                            if(isGreater(ch, t))
                                break;

                            list.add(Character.toString(t));
                            t=opeStack.pop();
                        }
                        opeStack.push(ch);

                    }

                }
                else if(ch==')')
                {
                    char t = opeStack.pop();
                    while(t!='('&&!opeStack.isEmpty())
                    {
                        list.add(Character.toString(t));
                        t = opeStack.pop();
                    }
                }

            }
            else//处理数字
            {
                numStr+=ch;
            }
        }

        //计算后缀表达式
        System.out.println(list.toString());
        Stack<Integer> num = new Stack<>();
        int size = list.size();
        for(int i=0;i<size;i++)
        {
            String t =list.get(i);
            if(isNumeric(t))
            {//将t转换成int 方便计算
                num.push(Integer.parseInt(t));
            }
            else
            {
                //如果t为运算符则 只有一位
                char c = t.charAt(0);
                int b = num.pop();
                //如果有 算式是类似于 -8-8 这样的需要判断一下栈是否为空
                int a = num.pop();
                switch(c)
                {
                    case '+':
                        num.push(a+b);
                        break;
                    case '-':
                        num.push(a-b);
                        break;
                    case '*':
                        num.push(a*b);
                        break;
                    case '/':
                        num.push(a/b);
                        break;
                    default:
                        break;
                }
            }
        }
        //System.out.println(num.pop());
        return num.pop();
    }


    /**化简表达式
     * 将表达式中的 {}[]替换为()
     * 负数的处理
     * 为了方便将中缀转换为后缀在字符串前后分别加上(,) eg:"1+1" 变为"(1+1)"
     * @param str 输入的字符串
     * @return s 返回简化完的表达式
     */
    public static String simplify(String str)
    {
        //负数的处理
        // 处理负数，这里在-前面的位置加入一个0，如-4变为0-4，
        // 细节：注意-开头的地方前面一定不能是数字或者反括号，如9-0,(3-4)-5，这里地方是不能加0的
        // 它的后面可以是数字或者正括号，如-9=>0-9, -(3*3)=>0-(3*3)
        String s = str.replaceAll("(?<![0-9)}\\]])(?=-[0-9({\\[])", "0");
        //将表达式中的 {}[]替换为()
        s = s.replace('[', '(');
        s = s.replace('{', '(');
        s = s.replace(']', ')');
        s = s.replace('}', ')');
        //为了方便将中缀转换为后缀在字符串前后分别加上(,)
        s="("+s+")";

        return s ;
    }

    /**判断字符c是否为合理的运算符
     *
     * @param c
     * @return
     */
    public static boolean isOpe(char c)
    {
        if(c=='+'||c=='-'||c=='*'||c=='/')
            return true;
        else
            return false;
    }

    public static boolean isAllOpe(char c)
    {
        if(c=='+'||c=='-'||c=='*'||c=='/')
            return true;

        else if(c=='('||c==')')
            return true;
        else
            return false;
    }

    /**
     * 比较字符等级a是否大于b
     * @param a
     * @param b
     * @return 大于返回true 小于等于返回false
     */
    public static boolean isGreater(char a,char b)
    {
        int a1 = getLevel(a);
        int b1 = getLevel(b);
        if(a1>b1)
            return true;
        else
            return false;
    }

    /**
     * 得到一个字符的优先级
     * @param a
     * @return
     */
    public static int getLevel(char a)
    {

        if(a=='+')
            return 0;
        else if(a=='-')
            return 1;
        else if(a=='*')
            return 3;
        else if(a=='/')
            return 4;
        else
            return -1;

    }

    //判断是不是数字
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

    //获取一个类的所有属性列表
    public List<AttributeTableItem> getClassAtt(int classid){
        List<AttributeTableItem> ls = new ArrayList();
        for(int i=0;i<attributet.attributeTable.size();i++)
            if(attributet.attributeTable.get(i).classid==classid)
                ls.add(attributet.attributeTable.get(i));
        return ls;
    }

    //获取对应规则和源类中，所有的属性名和属性位置
    public HashMap<String,Integer> getRuleAtt(String rule, int classid){
        List<AttributeTableItem> ls = getClassAtt(classid);
        HashMap<String,Integer> hs = new HashMap();
        for(int i=0;i<ls.size();i++)
            if(rule.contains(ls.get(i).attrname))
                hs.put(ls.get(i).attrname,ls.get(i).attrid);
        return hs;
    }

    //根据类名获取classid
    public int getClassid(String classname){
        for(int i=0;i<classt.classTable.size();i++)
            if(classname.equals(classt.classTable.get(i).classname))
                return classt.classTable.get(i).classid;
        System.out.println("不存在该类名");
        return -1;
    }

    public String getType(String classname, String attrName){
        int classid = getClassid(classname);
        for(int i=0;i<attributet.attributeTable.size();i++)
            if(classid == attributet.attributeTable.get(i).classid && attrName.equals(attributet.attributeTable.get(i).attrname))
                return attributet.attributeTable.get(i).attrtype;
        return null;
    }

    //获取属性所在的位置，用于把值放在insert tuple中的正确位置
    public int getpos(List<AttributeTableItem> ls, String attrname){
        for(int i=0;i<ls.size();i++)
            if(ls.get(i).attrname.equals(attrname))
                return ls.get(i).attrid;
        System.out.println("不存在相关属性位置");
        return -1;
    }

    //获取字符串中出现过的属性信息
    public List<AttributeTableItem> getExistAttr(String exp,int classid){
        List<AttributeTableItem> ls = getClassAtt(classid);
        List<AttributeTableItem> result = new ArrayList();
        for(int i=0;i<ls.size();i++)
            if(exp.contains(ls.get(i).attrname))
                result.add(ls.get(i));
        return result;
    }

    public String getclassname(int id){
        for(ClassTableItem a : classt.classTable)
            if(a.classid==id)
                return a.classname;
            return "";
    }
}

