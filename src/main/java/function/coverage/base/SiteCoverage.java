package function.coverage.base;

import global.Index;

/**
 *
 * @author nick
 */
public class SiteCoverage {

    int[][] caseCtrlSiteCovArray; // case or ctrl , accumulated site coverage

    public SiteCoverage(int length) {
        caseCtrlSiteCovArray = new int[2][length];
    }

    public void addValue(boolean isCase, int posIndex) {
        if (isCase) {
            caseCtrlSiteCovArray[Index.CASE][posIndex]++;
        } else {
            caseCtrlSiteCovArray[Index.CTRL][posIndex]++;
        }
    }
    
    public int getCaseSiteCov(int pos){
        return caseCtrlSiteCovArray[Index.CASE][pos];
    }
    
    public int getCtrlSiteCov(int pos){
        return caseCtrlSiteCovArray[Index.CTRL][pos];
    }
}
