package function.cohort.vcf;

import java.util.Iterator;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class VCFCommand {

    public static boolean isList = false;
    public static boolean isOutputCaseOnly = false;
    
    public static void initOptions(Iterator<CommandOption> iterator)
            throws Exception {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--output-case-only":
                    isOutputCaseOnly = true;
                    break;
                default:
                    continue;
            }

            iterator.remove();
        }
    }
    
    public static boolean isOutputCaseOnly(boolean isCaseSample) {
        if (VCFCommand.isOutputCaseOnly) {
            return isCaseSample;
        } else {
            return true;
        }
    }
}
