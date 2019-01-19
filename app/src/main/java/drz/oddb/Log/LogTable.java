package drz.oddb.Log;

import java.util.ArrayList;
import java.util.List;

public class LogTable {
    public List<LogTableItem> logTable=new ArrayList<>();
    public int maxLogBlockNum;

    public void clear(){
        logTable.clear();
        maxLogBlockNum = 0;
    }

}
