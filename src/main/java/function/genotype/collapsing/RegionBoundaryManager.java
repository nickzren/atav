package function.genotype.collapsing;

import function.genotype.collapsing.RegionBoundary.Region;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author nick
 */
public class RegionBoundaryManager {

    private static List<RegionBoundary> regionBoundaryList = new ArrayList<RegionBoundary>();

    public static void init() throws Exception {
        if (CollapsingCommand.regionBoundaryFile.isEmpty()) {
            return;
        }

        File f = new File(CollapsingCommand.regionBoundaryFile);
        FileInputStream fstream = new FileInputStream(f);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String lineStr = "";
        while ((lineStr = br.readLine()) != null) {
            if (!lineStr.isEmpty()) {
                regionBoundaryList.add(new RegionBoundary(lineStr));
            }
        }

        br.close();
        in.close();
        fstream.close();
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
}
