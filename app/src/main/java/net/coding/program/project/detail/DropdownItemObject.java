package net.coding.program.project.detail;

/**
 * Created by Neutra on 2015/4/24.
 */
public class DropdownItemObject {
    public DropdownItemObject(String text) {
        this(text, 0);
    }

    public DropdownItemObject(String text, int id) {
        this.text = text;
        this.id = id;
    }

    public int id;
    public String text;
    public String suffix;
}
