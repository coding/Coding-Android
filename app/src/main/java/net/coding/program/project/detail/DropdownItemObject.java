package net.coding.program.project.detail;

/**
 * Created by Neutra on 2015/4/24.
 */
public class DropdownItemObject {
    public DropdownItemObject(String text, int id, String value) {
        this.text = text;
        this.id = id;
        this.value = value;
    }

    public int id;
    public String text;
    public String value;
    private String suffix;

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
