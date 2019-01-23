package drz.oddb.Log;

import drz.oddb.Memory.MemManage;
import drz.oddb.Transaction.SystemTable.BiPointerTable;
import drz.oddb.Transaction.SystemTable.ClassTable;
import drz.oddb.Transaction.SystemTable.DeputyTable;
import drz.oddb.Transaction.SystemTable.ObjectTable;
import drz.oddb.Transaction.SystemTable.SwitchingTable;

public class LogManage {

    final private int MAXSIZE=20;
    private int checkpoint=-1;
    private int logid=0;    //LogTable块id
    private MemManage mem = null;
    public LogTable LogT = new LogTable();   //存放执行层创建LogManage时写入的日志
    private ObjectTable topt;
    private ClassTable classt;
    private DeputyTable deputyt;
    private BiPointerTable biPointerT;
    private  SwitchingTable switchingT;
    //构造方法
    public LogManage(MemManage mem){
        this.mem = mem;
        this.topt = mem.loadObjectTable();
        this.classt = mem.loadClassTable();
        this.deputyt = mem.loadDeputyTable();
        this.biPointerT = mem.loadBiPointerTable();
        this.switchingT = mem.loadSwitchingTable();
        LogT.logID = GetCheck() + 1;       //为新块分配id->检查点+1
        logid = LogT.logID;
    }

    //若存够了20，需要调用该方法，初始化LogT为空
    private boolean init(){
        LogT = new LogTable();
        return true;
    }

    //分配事务ID,空方法
    private void AllocateTID(){
        // todo
    }

    //得到检查点号
    private int GetCheck(){
            int cpid = mem.loadCheck();
            return cpid;
    }

    //load日志块，找出需要redo的命令
    public LogTable GetReDo(){
            LogTable ret = null;
            checkpoint = GetCheck();    //得到检查点id
            ret = mem.loadLog(checkpoint+1);   //加载可能redo的日志

            if(ret != null){
                int lognum = ret.logTable.size();    //有几条语句需要redo
                for(int i=0;i<lognum;i++){
                    LogTableItem temp = ret.logTable.get(i);   //得到每一条语句
                    ret.logTable.add(temp);
                }
                ret.logID=checkpoint+1; //执行层写也可以，不可以写两次
                return ret;
            }
            return ret;
        }

    //写一条日志
    public boolean WriteLog(String s){

            int lognum = LogT.logTable.size();  //获得当前对象的logtable中有几条语句
            LogTableItem LogItem = new LogTableItem(s);     //把语句传入logItem

            if(lognum<MAXSIZE){  //List写得下
                LogT.logTable.add(LogItem);
            }else{

                mem.saveLog(LogT);  //把当前的存入
                mem.saveObjectTable(topt);
                mem.saveClassTable(classt);
                mem.saveDeputyTable(deputyt);
                mem.saveBiPointerTable(biPointerT);
                mem.saveSwitchingTable(switchingT);
                while(!mem.flush());
                while(!mem.setLogCheck(LogT.logID));
                mem.setCheckPoint(LogT.logID);

                init(); //新建一个日志块
                LogT.logTable.add(LogItem);
                LogT.logID=logid+1;
            }
            return true;
        }

    //删除日志文件
    public void DeleteLog(int lognum){
        //todo
    }

}
