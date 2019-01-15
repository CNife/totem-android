package drz.oddb.Memory;


import java.util.Dictionary;
import java.util.List;

public class Tuple {
    int tupleHeader;
    String[] tuple;

    public Tuple(String[] values) {
        tuple = values.clone();
        tupleHeader = values.length;
    }
}
