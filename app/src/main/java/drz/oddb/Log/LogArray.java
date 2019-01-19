package drz.oddb.Log;
import java.io.Serializable;

public class LogArray implements Serializable {
    public String[] logstr = new String[20];
    public int lognum = 0;

    public boolean initArray(){
        for(int i=0;i<20;i++){
            logstr[i] = " ";
        }
        lognum=0;
        return true;
    }

    public void AddLog(Log Log){
        logstr[lognum] = Log.s;
        lognum++;
    }
}
