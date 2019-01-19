package drz.oddb.Log;

import drz.oddb.Memory.MemManage;

/********************/
/*实现方式：由于事实上只有一个事务，所以事务ID永远为1；事务块无限大(正常退出可以删除)，只有1个，结构见LogBlockDefine
每一条日志记录的格式见Log*/
/********************/
public class LogManage {
    final private int MAXSIZE=20;
    private int LogmaxNo=0;

        //构造方法
        /*public LogManage(){
            InitLogStr();
            mem.loadLog();
        }

        //初始化LogStr
        private boolean InitLogStr(){
            for(int i=0;i<MAXSIZE;i++){
                LogStr[i] = " ";
            }
            LogStrNum=0;
            return true;
        }

        //分配事务ID
        private int AllocateTID(){
            int ret = 1;    //默认只有一个事务
            return 1;
        }

        //load日志块，找出需要redo的命令
        public String[] ReDo(){
            String[] redoList = new String[20];
            //todo
            return redoList;
        }

        //重做redo()的返回值，在执行层实现

        //写一条日志
        public boolean WriteLog(String s,int commitflag){
            if(commitflag == 0){    //这条语句之后还要继续写
                if(LogStrNum<20){   //没有存满20个
                    LogStr[LogStrNum] = s;
                    LogStrNum++;
                }
                else{       //存满20个，需要把日志写回磁盘
                    mem.saveLog(LogStr);
                    //TODO when写入success
                    Success();
                    InitLogStr();
                    LogStr[0] = s;  //重新开始写LogStr
                }
            }else {     //写完这条语句就要提交事务(点击退出)
                if (LogStrNum < 20) {   //没有存满20个
                    LogStr[LogStrNum] = s;
                    mem.saveLog(LogStr);
                    //TODO when写入success
                    Success();
                    //initLogStr();
                } else {
                    mem.saveLog(LogStr);
                    InitLogStr();
                    LogStr[0] = s;  //重新开始写LogStr
                    mem.saveLog(LogStr);
                    //TODO when写入success
                    Success();
                }
            }
            return true;
        }

        //设置成功flag(标记该flag前的所有内容都已写回磁盘)
        public boolean Success(){
            mem.writeFlag();
            return true;
        }

        //同步数据缓冲区和磁盘数据
        public boolean Sync(){
            boolean ret = false;
            //todo
            return ret;
        }

        //若正常退出，删除日志文件，也可以手动删除
        public void clearLog(){
            mem.clearLogBlock();
        }
*/
}
