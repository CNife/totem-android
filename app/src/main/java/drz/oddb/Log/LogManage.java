package drz.oddb.Log;

import drz.oddb.Memory.MemManage;

public class LogManage {

    final private int MAXSIZE=20;
    private int checkpoint=0;
    MemManage mem = null;
    LogTable LogT = null;   //存放执行层创建LogManage时写入的日志(20个一组)

    //构造方法
    public LogManage(MemManage mem){
        this.mem = mem;
    }

    //若存够了20，需要调用该方法，初始化LogT为空
    private boolean init(){
        LogT = null;
        return true;
    }

    //分配事务ID,空方法
    private void AllocateTID(){
            //TODO
        }

    //得到检查点号
    private int GetCheck(){
            int cpid = mem.loadCheck();
            return cpid;
    }

    //load日志块，找出需要redo的命令
    public LogTable ReDo(){
            LogTable ret = null;
            checkpoint = GetCheck();    //得到检查点id
            ret = mem.loadLog(checkpoint+1);   //加载可能redo的日志

            if(ret != null){
                int lognum = ret.logTable.size();    //有几条语句需要redo
                for(int i=0;i<lognum;i++){
                    LogTableItem temp = ret.logTable.get(i);   //得到每一条语句
                    ret.logTable.add(temp);
                }
                ret.check=0;
                ret.logID=checkpoint+1;
                return ret;
            }
            return ret;
        }

    //写一条日志
    public boolean WriteLog(String s){

            int lognum = LogT.logTable.size();  //获得当前对象的logtable中有几条语句
            LogTableItem LogItem = new LogTableItem(s);     //把语句传入logItem

            if(lognum<20){  //List写得下
                LogT.logTable.add(LogItem);
            }else{
                mem.saveLog(LogT);  //把当前的存入
                init();
                LogT.logTable.add(LogItem);
                LogT.logID++;
                LogT.check=0;
            }
            return true;
        }

    //删除日志文件
    public void clearLog(int lognum){
        //todo
    }

}
