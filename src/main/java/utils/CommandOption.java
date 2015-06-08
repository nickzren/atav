package utils;

/**
 *
 * @author nick
 */
public class CommandOption {

    private String name;
    private String value;

    public CommandOption(String n, String s) {
        name = n;
        value = s;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
