package function.cohort.vcf;

import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class VCFManager {
    public static void bgzipVCF(String path) {
        String cmd = ThirdPartyToolManager.BGZIP + " " + path;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }

    public static void tabixVCF(String path) {
        String cmd = ThirdPartyToolManager.TABIX + " -p vcf " + path;

        ThirdPartyToolManager.systemCall(new String[]{"/bin/sh", "-c", cmd});
    }
}
