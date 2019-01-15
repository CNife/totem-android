package drz.oddb.Transaction;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import drz.oddb.Memory.*;



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
            classt.classTable.add(new ClassTableItem(classname, classid, count, p[2 * i + 3], p[2 * i + 4]));
        }
    }

    private void CreateSelectDeputy(String[] p) {

    }


    private void Delete(String[] p) {
        String classname = p[1];
        int classid = 0;
        for (ClassTableItem item:classt.classTable){
            if (item.classname.equals(classname)){
                classid = item.classid;
                break;
            }
        }

        for (ClassTableItem item:classt.classTable){
            if (item.classid==classid){

                break;
            }
        }
        for (TopTableItem item:topt.topTable){
            if(item.classid == classid){
                Tuple tuple = GetTuple(item.dbid,item.offset);

            }
        }

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