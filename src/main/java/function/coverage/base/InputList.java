/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package function.coverage.base;

import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author qwang
 */
public class InputList extends Vector {

    /**
     * Constructs an empty InputList
     */
    public InputList() {
        //initial capacity of 100, increased by 100 if necessary
        super(100, 100);
    }

    /**
     * Parse and add a region to the InputList if it is in a valid region
     * format.
     *
     * @param r the input string representing a region
     * @return true if the region is valid and false otherwise
     * @throws NumberFormatException if the second and third items are not
     * numbers
     */
    public boolean addRegion(String r) throws NumberFormatException {
        String[] items = parse(r);
        if (items.length > 0) {
            if (items[0].startsWith("CCDS")) { //it is a CCDS_ID, so let it go
                return false;
            }
        }
        if (items.length == 3) {
            int start = Integer.parseInt(items[1]);
            int end = Integer.parseInt(items[2]);
            add(new CoveredRegion(items[0], start, end));
            return true;
        } else {
            return false;
        }
    }

    public void addRegion(String chr, int start, int end) {
        add(new CoveredRegion(chr, start, end));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        if (!this.isEmpty()) {
            for (Iterator it = this.iterator(); it.hasNext();) {
                Object obj = it.next();
                str.append(obj.toString()).append("\n");
            }
        } else {
            str.append("Empty List");
        }
        return str.toString();
    }

    private String[] parse(String r) {
        return r.trim().toUpperCase().replace("CHR", "").replaceAll("\\W+", ".").split("\\W");
    }
}
