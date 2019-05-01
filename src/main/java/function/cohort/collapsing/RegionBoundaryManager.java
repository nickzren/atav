package function.cohort.collapsing;

import function.annotation.base.GeneManager;
import function.cohort.collapsing.RegionBoundary.Region;
import function.variant.base.RegionManager;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RegionBoundaryManager {

    private static List<RegionBoundary> regionBoundaryList = new ArrayList<>();

    public static void init() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            return;
        }

        try {
            File f = new File(CollapsingCommand.regionBoundaryFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (!lineStr.isEmpty()) {
                    regionBoundaryList.add(new RegionBoundary(lineStr));
                }
            }

            br.close();
            fr.close();

            resetRegionList();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static List<RegionBoundary> getList() {
        return regionBoundaryList;
    }

    public static HashSet<String> getNameSet(String chr, int pos) {
        HashSet<String> nameSet = new HashSet<String>();

        for (RegionBoundary regionBoundary : regionBoundaryList) {
            for (Region region : regionBoundary.getList()) {
                if (region.isContained(chr, pos)) {
                    nameSet.add(regionBoundary.getName());
                    break;
                }
            }
        }

        return nameSet;
    }

    private static void resetRegionList() throws Exception {
        if (!RegionManager.isUsed()
                && !GeneManager.isUsed()) {
            RegionManager.clear();

            HashSet<String> regionSet = new HashSet<String>();

            for (RegionBoundary regionBoundary : regionBoundaryList) {
                for (Region region : regionBoundary.getList()) {
                    regionSet.add(region.toString());
                }
            }

            RegionManager.initRegionList(regionSet.toArray(new String[regionSet.size()]));
            RegionManager.sortRegionList();
        }
    }
}
