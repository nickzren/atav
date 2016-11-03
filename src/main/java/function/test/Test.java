package function.test;

import function.AnalysisBase;
import function.genotype.base.DPBinBlockManager;
import utils.ErrorManager;

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
        try {
//            CreateVDSDB.run();

//            OutputSubsetSample.run();
//            
//            LoadSubsetSample.run();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running test function";
    }

    public static void main(String[] args) {
        DPBinBlockManager.init();

        String str = "2Da18b1Ia4Cb22c1Wd2Ac2WbDLaWbN4aNb2X4aObSa1MbDHa4Fb2MZa";

        StringBuilder sb = new StringBuilder();

        int posIndex = 7112;

        int sum = 0;

        for (int pos = 0; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if (!DPBinBlockManager.getCoverageBin().containsKey(c)) {
                sb.append(c);
            } else {
                sum += Integer.parseInt(sb.toString(), 36);

                if (posIndex < sum) {
                    System.out.println(sb.toString());
                }

                sb.setLength(0);

            }
        }

        System.out.println(sum);
    }
}
