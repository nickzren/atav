package function.coverage.base;

import global.Data;
import global.Index;

/**
 *
 * @author nick
 */
public class SiteCoverage {

    int[][][] caseCtrlSiteCovArray; // case or ctrl , accumulated site coverage

    public SiteCoverage(int length) {
        caseCtrlSiteCovArray = new int[2][1][length];
    }

    public void addValue(boolean isCase, int dpBinIndex, int posIndex) {
        if (dpBinIndex == Data.BYTE_NA) {
            return;
        }

        if (isCase) {
            caseCtrlSiteCovArray[Index.CASE][dpBinIndex][posIndex]++;
        } else {
            caseCtrlSiteCovArray[Index.CTRL][dpBinIndex][posIndex]++;
        }
    }

    public int getCaseSiteCov(byte dpBinIndex, int pos) {
        return caseCtrlSiteCovArray[Index.CASE][dpBinIndex][pos];
    }

    public int getCtrlSiteCov(byte dpBinIndex, int pos) {
        return caseCtrlSiteCovArray[Index.CTRL][dpBinIndex][pos];
    }

    public int getCaseSiteCov(int pos) {
        return caseCtrlSiteCovArray[Index.CASE][Index.DP_BIN_10][pos];
    }

    public int getCtrlSiteCov(int pos) {
        return caseCtrlSiteCovArray[Index.CTRL][Index.DP_BIN_10][pos];
    }
}
