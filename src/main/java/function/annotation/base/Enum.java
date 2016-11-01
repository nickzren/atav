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
        
        String str = "2Ga2b16aDb4Na2XbEIaLbIWaBb7a1Db1Sc3Db1Wc17Cd4Rc12b8c8b2Jc51d2c3dCcAd2c62d2c6dAc4MdWc1Eb4cHbHc31bAa35b27cD3d64e3Ed2e8dTEe1PMd2eId3e9dRIe";

        StringBuilder sb = new StringBuilder();
        
        int sum = 0;
        
        for (int pos = 0; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if (!DPBinBlockManager.getCoverageBin().containsKey(c)) {
                sb.append(c);
            }else{
                sum += Integer.parseInt(sb.toString(), 36);
                sb.setLength(0);
            }
        }
        
        System.out.println(sum);

    }
}
