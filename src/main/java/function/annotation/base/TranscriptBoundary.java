package function.annotation.base;

/**
 *
 * @author nick
 */
public class TranscriptBoundary {

    private int id;
    private String chr;
    private int[][] intervalAarry; // array of start, end intervals

    public TranscriptBoundary(String transcriptBoundaryStr) {
        // format: name chr (start..end,start..end, ... ,start..end) length
        String[] tmp = transcriptBoundaryStr.split("\\s+");

        id =  Integer.valueOf(tmp[0].replace("ENST", ""));
        chr = tmp[1];

        tmp[2] = tmp[2].replace("(", "").replace(")", "");

        String[] intervalStrArray = tmp[2].split(",");
        intervalAarry = new int[intervalStrArray.length][2];

        for (int i = 0; i < intervalStrArray.length; i++) {
            tmp = intervalStrArray[i].split("\\W");

            intervalAarry[i][0] = Integer.valueOf(tmp[0]); // start
            intervalAarry[i][1] = Integer.valueOf(tmp[2]); // end
        }
    }

    public int getId() {
        return id;
    }

    public String getChr() {
        return chr;
    }

    public int[][] getIntevalArray() {
        return intervalAarry;
    }

    public boolean isContained(int pos) {
        // if pos less than first start and last end then false
        if (pos < intervalAarry[0][0] || pos > intervalAarry[intervalAarry.length - 1][1]) {
            return false;
        }

        for (int i = 0; i < intervalAarry.length; i++) {
            if (pos >= intervalAarry[i][0]
                    && pos <= intervalAarry[i][1]) {
                return true;
            }
            
        }

        return false;
    }
}
