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
    public static float maxGenomesAF = Data.NO_FILTER;
    
    public static boolean isMaxGenomesAFValid(float value) {
        if (maxGenomesAF == Data.NO_FILTER) {
            return true;
        }

        return value <= maxGenomesAF
                || value == Data.FLOAT_NA;
    }
}
