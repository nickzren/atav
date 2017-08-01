package function.external.bis;

/**
 *
 * @author nick
 */
public class Region {

    private int start;
    private int end;

    public Region(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean isIncluded(int pos) {
        if (pos >= start && pos <= end) {
            return true;
        }

        return false;
    }
}
