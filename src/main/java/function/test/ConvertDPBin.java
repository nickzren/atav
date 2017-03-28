package function.test;

import function.genotype.base.DPBinBlockManager;
import function.variant.base.RegionManager;
import global.Data;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import utils.DBManager;

/**
 *
 * @author nick
 */
public class ConvertDPBin {

    static final String TAB = "\t";

    public static void run() throws Exception {
        BufferedWriter bw;

        int count = 0;

        for (String chr : RegionManager.ALL_CHR) {
            bw = new BufferedWriter(new FileWriter("/nfs/seqscratch_ssd/zr2180/WalDB/DP_bins_chr" + chr));

            String sql = "SELECT * FROM DP_bins_chr" + chr;

            ResultSet rset = DBManager.executeReadOnlyQuery(sql);

            while (rset.next()) {
                int sampleId = rset.getInt("sample_id");
                int blockId = rset.getInt("block_id") * 10; // 1k block , first block id value
                String dpBinStr = rset.getString("DP_string");
                String blockDPBinStr; // 1k block string

                int dpBinStartPos = Data.INTEGER_NA;
                int blockStartPos = 0;
                int endPos = 0;
                int exceedLength = 0;
                char exceedBin = 'z';

                StringBuilder dpBinSB = new StringBuilder();

                for (int pos = 0; pos < dpBinStr.length(); pos++) {
                    char bin = dpBinStr.charAt(pos);

                    if (!DPBinBlockManager.getCoverageBin().containsKey(bin)) {
                        dpBinSB.append(bin);

                        if (dpBinStartPos == Data.INTEGER_NA) {
                            dpBinStartPos = pos;
                        }
                    } else {
                        int interval = Integer.parseInt(dpBinSB.toString(), 36);
                        endPos += interval; // add cov bin interval
                        dpBinSB.setLength(0); // clear StringBuilder

                        if (endPos < blockId + 999 - exceedLength) {
                            continue;
                        } else if (endPos == blockId + 999 - exceedLength) {
                            blockDPBinStr = dpBinStr.substring(blockStartPos, pos);

                            if (exceedLength > 0) {
                                blockDPBinStr = Integer.toString(exceedLength, Character.MAX_RADIX)
                                        + exceedBin
                                        + blockDPBinStr;

                                exceedLength = 0;
                            }
                        } else {
                            if(dpBinStartPos > 0){
                                dpBinStartPos -= 1;
                            }
                            
                            blockDPBinStr = dpBinStr.substring(blockStartPos, dpBinStartPos);
                            dpBinStartPos = Data.INTEGER_NA;

                            if (exceedLength > 0) {
                                blockDPBinStr = Integer.toString(exceedLength, Character.MAX_RADIX)
                                        + exceedBin
                                        + blockDPBinStr;
                            }

                            exceedLength = endPos - blockId - 1000;
                            exceedBin = bin;

                            interval = interval - exceedLength;

                            blockDPBinStr += Integer.toString(interval, Character.MAX_RADIX) + bin;
                        }

                        bw.write(sampleId + TAB + blockId + TAB + blockDPBinStr);
                        bw.newLine();

                        blockStartPos = pos + 1;
                        blockId += 1000;
                    }
                }

//                if (count++ = 1) {
                bw.flush();
                bw.close();
                System.exit(0);
//                }
            }
        }
    }
}
