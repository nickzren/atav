package function.nondb.ppi;

import java.util.Iterator;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class PPICommand {

    public static boolean isPPI = false;
    public static String ppiExclude = "#N/A";
    public static String ppiFile = "";
    public static String ppiGenotypeFile = "";
    public static int ppiPermutaitons = 100;

    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;

        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            if (option.getName().equals("--exclude")) {
                ppiExclude = option.getValue();
            } else if (option.getName().equals("--ppi-file")) {
                ppiFile = getValidPath(option);
            } else if (option.getName().equals("--geno")) {
                ppiGenotypeFile = getValidPath(option);
            } else if (option.getName().equals("--perm")) {
                ppiPermutaitons = getValidInteger(option);
            } else {
                continue;
            }
            iterator.remove();
        }
    }
}
