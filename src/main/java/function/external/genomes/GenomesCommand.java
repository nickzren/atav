package function.external.genomes;

import global.Data;

/**
 *
 * @author nick
 */
public class GenomesCommand {
    public static boolean isList1000Genomes = false;
    public static boolean isInclude1000Genomes = false;
    
    public static String genomesPop = "global";
    public static float genomesMaf = Data.NO_FILTER;
    
    public static boolean isGenomesMafValid(float value) {
        if (genomesMaf == Data.NO_FILTER) {
            return true;
        }

        if (value <= genomesMaf
                || value == Data.NA) {
            return true;
        }

        return false;
    }
}
