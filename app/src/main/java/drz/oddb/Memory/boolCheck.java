package drz.oddb.Memory;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


public class boolCheck {
    private static final String AND ="AND";
    private static final String OR ="OR";
    private static final String UNEQUAL = "!=";
    private static final String EQUAL = "=";
    private static final String GE = ">";
    private static final String GEQ = ">=";
    private static final String LE = "<";
    private static final String LEQ = "<=";


    private Map<String, String> params;

    public boolean isEnable(String enable, Map<String,String> params){

        this.params = params;

        Stack<String> operation = new Stack<String>();
        int tmpEnd = enable.length();
        for(int i = enable.length();i>1 ; i--){
            String twoChatter = enable.substring(i-2,i);

            String threeChatter = null;
            if(i>2)
                threeChatter = enable.substring(i-3,i);
            if (twoChatter.equals(OR)) {
                operation.add(enable.substring(i, tmpEnd).trim().replaceAll(" ", ""));
                operation.add(twoChatter);
                tmpEnd = i-2;
            } else if (threeChatter!=null && threeChatter.equals(AND)) {
                operation.add(enable.substring(i, tmpEnd).trim().replaceAll(" ", ""));
                operation.add(threeChatter);
                tmpEnd = i-3;
            }
        }
        operation.add(enable.substring(0,tmpEnd).replaceAll(" ", ""));
        System.out.println(operation);
        return judge(operation);
    }
    private boolean judge(Stack<String> operation) {
        if(operation.size()==1) {
            return isTrue(operation.pop());
        }else {
            boolean value1 = isTrue(operation.pop());
            String releation = operation.pop();
            boolean value2 = isTrue(operation.pop());
            if (releation.equals(AND)) {
                boolean operationResult =  value1 && value2;
                operation.add(String.valueOf(operationResult));
                return judge(operation);
            }
            else if(releation.equals(OR)) {
                boolean operationResult =  value1 || value2;
                operation.add(String.valueOf(operationResult));
                return judge(operation);
            }
            else {
                //LOG.error("the logical is wrong")
                return true;
            }
        }

    }


    private boolean isTrue(String condition) {
        if(condition.toLowerCase().equals("true")) {
            return true;
        }else if(condition.toLowerCase().equals("false")) {
            return false;
        }else {

            if (condition.contains(UNEQUAL)) {
                int index = condition.indexOf(UNEQUAL);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+2,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    System.out.println("wrong");
                    return false;
                }

                if(isStr2Num(value1)){
                    return Integer.parseInt(value1)!= Integer.parseInt(value2);
                }

                return !value1.equals(value2);
            }
            else if (condition.contains(GEQ)) {
                int index = condition.indexOf(GEQ);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+2,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    System.out.println("wrong");
                    return false;
                }

                if(isStr2Num(value1)){
                    return Integer.parseInt(value1)>= Integer.parseInt(value2);
                }

                return value1.toString().compareTo(value2.toString()) >= 0;
            }
            else if (condition.contains(GE)) {
                int index = condition.indexOf(GE);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+1,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    System.out.println("wrong");
                    return false;
                }
                if(isStr2Num(value1)){
                    return Integer.parseInt(value1)> Integer.parseInt(value2);
                }
                return value1.toString().compareTo(value2.toString()) > 0;
            }
            else if (condition.contains(LEQ)) {
                int index = condition.indexOf(LEQ);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+2,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    System.out.println("wrong");
                    return false;
                }
                if(isStr2Num(value1)){
                    return Integer.parseInt(value1)<= Integer.parseInt(value2);
                }
                return value1.toString().compareTo(value2.toString()) <= 0;
            }
            else if (condition.contains(LE)) {
                int index = condition.indexOf(LE);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+1,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    System.out.println("wrong");
                    return false;
                }
                if(isStr2Num(value1)){
                    return Integer.parseInt(value1)< Integer.parseInt(value2);
                }
                return value1.toString().compareTo(value2.toString()) < 0;
            }else if(condition.contains(EQUAL)) {
                int index = condition.indexOf(EQUAL);
                String stringOfValue1 = condition.substring(0, index);
                String stringOfValue2 = condition.substring(index+1,condition.length());

                String value1 = getValuFromString(stringOfValue1,params);
                String value2 = getValuFromString(stringOfValue2,params);
                System.out.println(stringOfValue1+":"+value1);
                System.out.println(stringOfValue2+":"+value2);
                if(value1==null || value2 ==null) {
                    //LOG.error();
                    return false;
                }
                if(isStr2Num(value1)){
                    return Integer.parseInt(value1) == Integer.parseInt(value2);
                }
                return value1.equals(value2);
            }
            else {
                System.out.println("wrong");
                return false;
            }
        }

    }
    private String getValuFromString(String configString, Map<String, String> params) {
        // TODO 这个函数主要是根据configString的知道，从其他参数里面获取想要的Value

        String result = configString;
        if(params.containsKey(configString)){
            result = params.get(configString);
        }
        return result;
    }


    public static boolean isStr2Num(String str) {

        try {

            Integer.parseInt(str);

            return true;

        } catch (NumberFormatException e) {

            return false;

        }

    }
    /*
    public static void main(String[] args) {

        Map<String ,String> params =new HashMap<>();
        params.put("name", "kangyucheng");
        params.put("id", "2014");
        params.put("age", "14");

        boolCheck getEnableService =new boolCheck();
        String string1 ="name != kangyucheng OR id >= 2014 AND age = 14";
        System.out.println(getEnableService.isEnable(string1,params));
    }
    */


}