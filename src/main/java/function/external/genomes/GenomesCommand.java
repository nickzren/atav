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
    public static float maxGenomesMaf = Data.NO_FILTER;
    
    public static boolean isMaxGenomesMafValid(float value) {
        if (maxGenomesMaf == Data.NO_FILTER) {
            return true;
        }

        return value <= maxGenomesMaf
                || value == Data.NA;
    }
}
