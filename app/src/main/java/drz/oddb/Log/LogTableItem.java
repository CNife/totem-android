package drz.oddb.Log;

public class LogTableItem {
    public int TID = 1;     //事务号
    public int LogNum = 0;    //块号
    public LogTableItem(int TID,int logNum){
        this.TID = TID;
        this.LogNum = logNum;
    }
    public LogTableItem(){
        this.TID = 1;
        this.LogNum = 0;
    }

}
