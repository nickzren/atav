package function.cohort.collapsing;

import function.variant.base.RegionManager;
import function.variant.base.VariantManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import utils.ErrorManager;

/**
 *
 * @author nick
 */
public class RegionBoundaryManager {

    private static HashMap<String, List<RegionBoundary>> regionBoundaryMap = new HashMap<>();

    public static void init() {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            return;
        }

        try {
            File f = new File(CollapsingCommand.regionBoundaryFile);
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            ArrayList<String> regionList = new ArrayList<>();

            String lineStr = "";
            while ((lineStr = br.readLine()) != null) {
                if (!lineStr.isEmpty()) {
                    RegionBoundary regionBoundary = new RegionBoundary(lineStr);

                    // if used --region
                    if (!RegionManager.isChrContained(regionBoundary.getChr())) {
                        continue;
                    }

                    regionBoundaryMap.putIfAbsent(regionBoundary.getChr(), new ArrayList<>());
                    regionBoundaryMap.get(regionBoundary.getChr()).add(regionBoundary);

                    for (int i = 0; i < regionBoundary.getIntevalArray().length; i++) {
                        regionList.add(regionBoundary.getRegionStrByIndex(i));
                    }
                }
            }

            if (!VariantManager.isUsed()) {
                RegionManager.initRegionList(regionList.toArray(new String[regionList.size()]));
            }

            br.close();
            fr.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    public static List<RegionBoundary> getList(String chr) {
        return regionBoundaryMap.get(chr);
    }

    public static List<String> getNameList(String chr, int pos) {
        List<String> nameList = new ArrayList<>();

        for (RegionBoundary regionBoundary : regionBoundaryMap.get(chr)) {
            if (regionBoundary.isContained(pos)) {
                nameList.add(regionBoundary.getName());
            }
        }

        return nameList;
    }
}
