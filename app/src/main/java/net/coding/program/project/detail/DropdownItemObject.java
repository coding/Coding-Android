package net.coding.program.project.detail;

/**
 * Created by Neutra on 2015/4/24.
 */
public class DropdownItemObject {
    public DropdownItemObject(String text, int id, String filter) {
        this.text = text;
        this.id = id;
        this.filter = filter;
    }

    public int id;
    public String text;
    public String suffix;
    public String filter;
}
