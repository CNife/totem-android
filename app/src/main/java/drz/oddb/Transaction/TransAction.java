package drz.oddb.Transaction;

import java.io.ByteArrayInputStream;

import drz.oddb.Memory.*;


import drz.oddb.Transaction.SystemTable.*;

import drz.oddb.parse.*;

public class TransAction {

    TopTable topt = new TopTable();
    ClassTable classt = new ClassTable();
    DeputyTable deputyt = new DeputyTable();

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
                    Delete(aa);
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
        String classname = p[1];
        int count = Integer.parseInt(p[2]);
        classt.maxid++;
        int classid = classt.maxid;
        for (int i = 0; i < count; i++) {
            classt.classTable.add(new ClassTableItem(classname, classid, count,i,p[2 * i + 3], p[2 * i + 4]));
        }
    }

    private void CreateSelectDeputy(String[] p) {

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

        for (TopTableItem item:topt.topTable){
            if(item.classid == classid){
                Tuple tuple = GetTuple(item.dbid,item.offset);
                if(Condition(attrtype,tuple,attrid,p[4])){
                    DeleteTuple(item.blockid,item.offset);
                    topt.topTable.remove(item);
                }
            }
        }


    }
public void Print(TupleList tpl,String[] attrname,int[] attrid,String[] type){};

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


        for(TopTableItem item : topt.topTable){
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
        return null;
    }

    private int[] InsertTuple(Tuple tuple){
        return null;
    }

    private void DeleteTuple(int id, int offset){

    }



}