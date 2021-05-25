package function.cohort.vcf;

import global.Data;
import global.Index;
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

    public static String getGT(byte gt) {
        switch (gt) {
            case Index.HOM:
                return VCFOutput.HOM;
            case Index.HET:
                return VCFOutput.HET;
            case Index.REF:
                return VCFOutput.REF;
            default:
                return VCFOutput.NA;
        }
    }

    public static byte getGT(String gt) {
        switch (gt) {
            case VCFOutput.HOM:
                return Index.HOM;
            case VCFOutput.HET:
                return Index.HET;
            case VCFOutput.REF:
                return Index.REF;
            default:
                return Data.BYTE_NA;
        }
    }
}
