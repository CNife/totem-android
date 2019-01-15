package drz.oddb.Transaction;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.*;



import drz.oddb.parse.*;

public class TransAction {
    public String query(String s){

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
        parse p = new parse(byteArrayInputStream);
        try {
            String[] aa = p.Run();

            switch (Integer.parseInt(aa[0]))
            {
                case parse.OPT_CREATE_ORIGINCLASS:
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
}
