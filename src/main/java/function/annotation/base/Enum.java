package function.annotation.base;

import function.genotype.base.DPBinBlockManager;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class Enum {

    public enum Impact {
        HIGH(1), MODERATE(2), LOW(3), MODIFIER(4);
        private int value;

        private Impact(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };

    private static StringJoiner inputImpactSJ = new StringJoiner(",");

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
