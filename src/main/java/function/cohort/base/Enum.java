package function.cohort.base;

/**
 *
 * @author nick
 */
public class Enum {

    public enum FILTER {
        PASS(1), LIKELY(2), INTERMEDIATE(3), FAIL(4);
        private int value;

        private FILTER(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };
}
