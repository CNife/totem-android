package drz.oddb.Log;

import java.util.ArrayList;
import java.util.List;

public class LogTable {
    public List<LogTableItem> logTable=new ArrayList<>();
    public int maxLogNum;

    public void clear(){
        logTable.clear();
        maxLogNum = 0;
    }

}
