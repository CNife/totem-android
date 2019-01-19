package drz.oddb.Log;

import java.io.Serializable;

public class Log implements Serializable {
        int loglength; //str.getlength()得到字符个数
        String s;       //存入的字符串

        public Log(String s) {
                loglength = s.length();
                s = s;
        }

        public Log(){}

}
