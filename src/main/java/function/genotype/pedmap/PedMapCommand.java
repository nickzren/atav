package function.genotype.pedmap;

import java.util.Iterator;
import static utils.CommandManager.getValidFloat;
import static utils.CommandManager.getValidInteger;
import static utils.CommandManager.getValidPath;
import utils.CommandOption;

/**
 *
 * @author nick
 */
public class PedMapCommand {

    public static boolean isPedMap = false;
    public static boolean isVariantIdOnly = false; //what's this where are we using it ? 
    public static boolean isEigenstrat = false;
    public static boolean isKinship = false;
    public static boolean isFlashPCA = false;
    public static String pedMapPath = ""; //what's this where are we using it ?
    public static String sampleCoverageSummaryPath = "";
    public static int kinshipSeed = 42;
    public static float kinshipRelatednessThreshold = 0.0884f;
    //for flashpca, plink outlier removal
    public static boolean isKeepOutliers = false;
    public static boolean isNoPlots = false;
    public static int numEvec = 10;
    public static int numNeighbor = 5;//nearest neighbor for outlier detection
    public static float z_thresh = -2.4f;//Z value per nearest neghbor

    //removed ppc and prop_diff filter
    //public static boolean isppc = false; //use ppc value in outlier detection
    //public static float ppc = 0.05f;//assumes snps in linkage equilibrium
    //public static boolean isProp_Diff = false;//use prop_diff value in outlier detection; requires ppc to be used too
    //public static float prop_diff = 0.99f;//Proportion of significantly different others samples
    public static void initOptions(Iterator<CommandOption> iterator) {
        CommandOption option;
        while (iterator.hasNext()) {
            option = (CommandOption) iterator.next();
            switch (option.getName()) {
                case "--eigenstrat":
                    isEigenstrat = true;
                    break;
                case "--kinship":
                    isKinship = true;
                    break;
                //for flashpca    
                case "--flashpca":
                    isFlashPCA = true;
                    break;
                case "--keep-outliers":
                    isKeepOutliers = true;
                    break;
                case "--num-eigvec":
                    numEvec = getValidInteger(option);
                    break;
                case "--num-nearest-neighbor":
                    numNeighbor = getValidInteger(option);
                    break;
                case "--no-plots":
                    isNoPlots = true;
                case "--z-score-thresh":
                    z_thresh = getValidFloat(option);
                    break;
                //flashpca args end   
                case "--sample-coverage-summary":
                    sampleCoverageSummaryPath = getValidPath(option);
                    break;
                case "--kinship-seed":
                    kinshipSeed = getValidInteger(option);
                    break;
                case "--kinship-relatedness-threshold":
                    kinshipRelatednessThreshold = getValidFloat(option);
                    break;
                default:
                    continue;
            }
            iterator.remove();
        }
    }
}
