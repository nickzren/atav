package function.annotation.base;

import function.genotype.base.DPBinBlockManager;
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
}
