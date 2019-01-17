package drz.oddb.Transaction.SystemTable;

public class SwitchingTableItem {
    public String attr = null;
    public String deputy = null;
    public String rule = null;

    public SwitchingTableItem(String attr, String deputy, String rule) {
        this.attr = attr;
        this.deputy = deputy;
        this.rule = rule;
    }

    public SwitchingTableItem(){}
}
