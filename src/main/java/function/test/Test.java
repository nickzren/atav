package function.test;

import function.AnalysisBase;
import function.genotype.base.DPBinBlockManager;

/**
 * code here for any testing or small ETL task purpose
 *
 * @author nick
 */
public class Test extends AnalysisBase {

    @Override
    public void initOutput() {
    }

    @Override
    public void closeOutput() {
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processDatabaseData() {

    }

    @Override
    public String toString() {
        return "Start running test function";
    }

    public static void main(String[] args) {
        DPBinBlockManager.init();

        String str = "D8a9b3c9d1a3d1bOd1aHd9e1d2e2d2e1d3Fe1dHNeMd1eDd4c1d3b9a1d1bUd1cYdMc35b2O1a1b1aCbIcLd1cGd1cFd3e3d1Ne1d5eHd1e5d2e11d88ePd1e9a1e1Aa1dSa5b1a5b1a4bPa1b1a2b1a1b1aGb1a9b2aDb1aDb1aDb3c13bYa13b13RaRb1aEb8c1b4c1a1cId1a2DdCe2d1Ke1dAeGdFe3d4Me1d4eTd6cBbQc3d2cFd1b2d1b32d3c1b7cWbOHaEbFc1aOc1a5c75d1e1d2Ae1d2e46d1aOdQcCb6c1Mb8Da2NbIc7d";

        StringBuilder sb = new StringBuilder();

        StringBuilder sbTmp = new StringBuilder();

        int posIndex = 1343;

        int sum = 0;

        for (int pos = 0; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            sbTmp.append(c);

            if (!DPBinBlockManager.getCoverageBin().containsKey(c)) {
                sb.append(c);
            } else {
                sum += Integer.parseInt(sb.toString(), 36);

                System.out.println(sb.toString() + " " + Integer.parseInt(sb.toString(), 36));

                if (posIndex <= sum) {
                    System.out.println(sbTmp.toString());
                    break;
                }

                sb.setLength(0);

            }
        }

        System.out.println(sum);
    }
}
