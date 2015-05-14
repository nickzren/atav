package atav.analysis.coverage.base;

import atav.analysis.base.Region;
import atav.global.SqlQuery;
import atav.manager.utils.DBManager;
import java.util.Iterator;

/**
 *
 * @author qwang
 */
public class Transcript {

    String Name;
    String NameType;
    InputList ExonList = null;
    Region Translatedregion = null;

    public Transcript(String name) {
        Name = parse(name);
        if (name.startsWith("ENST")) {
            NameType = "stable_id";
        } else if (name.startsWith("CCDS")) {
            NameType = "ccds_id";
        } else {
            NameType = "unknown";
        }
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return NameType;
    }

    public boolean isValid() {
        return Name.length() > 0 && (NameType.equals("stable_id") || NameType.equals("ccds_id"));
    }

    public void populateExonList() {
        ExonList = DBUtils.getExonList(getExonString());
    }

    public InputList getExonList() {
        if (ExonList == null) {
            populateExonList();
        }
        return ExonList;
    }

    public Region getTranslatedRegion() {
        if (Translatedregion == null) {
            Translatedregion = DBUtils.getTranslatedRegion(getUTRString());
        }
        return Translatedregion;
    }

    public boolean isCCDS() throws Exception {
        if (NameType.equals("ccds_id")) {
            return true;
        }
        if (NameType.equals("unknown")) {
            return false;
        }
        String str = "SELECT t.transcript_id "
                + "FROM _DB_HSC_.transcript t, _DB_HSC_.object_xref ox,_DB_HSC_.xref x "
                + "WHERE transcript_id = ensembl_id "
                + "AND x.xref_id = ox.xref_id "
                + "AND ensembl_object_type = 'Transcript' "
                + "AND external_db_id = 3800 "
                + "AND t.stable_id ='_TRANSCRIPT_' ";
        str = str.replaceAll("_TRANSCRIPT_", Name);
        str = str.replaceAll("_DB_HSC_", DBManager.DB_HOMO_SAPIENS_CORE_NAME);
        return DBUtils.isEmpty(str);
    }

    public void filterByUTR() {
        Region translated = getTranslatedRegion();
        if (translated != null) {
            InputList FilteredExons = new InputList();
            for (Iterator it = ExonList.iterator(); it.hasNext();) {
                Exon exon = (Exon) it.next();
                CoveredRegion region = exon.getCoveredRegion().intersect(translated);
                if (region != null) {
                    exon.setRegion(region);
                    FilteredExons.add(exon);
                }
            }
            ExonList = FilteredExons;
        }
    }

    public String getUTRString() {
        if (isValid()) {
            String str;
            if (NameType.equals("stable_id")) {
                str = SqlQuery.TRANSCRIPT_UTR_STABLEID;
            } else {
                str = SqlQuery.TRANSCRIPT_UTR_CCDSID;
            }
            str = str.replaceAll("_TRANSCRIPT_", Name);
            str = str.replaceAll("_DB_HSC_", DBManager.DB_HOMO_SAPIENS_CORE_NAME);
            return str;
        } else {
            return "";
        }
    }

    public String getExonString() {
        if (isValid()) {
            String str;
            if (NameType.equals("stable_id")) {
                str = SqlQuery.TRANSCRIPT_EXON_STABLEID;
            } else {
                str = SqlQuery.TRANSCRIPT_EXON_CCDSID;
            }
            str = str.replaceAll("_TRANSCRIPT_", Name);
            str = str.replaceAll("_DB_HSC_", DBManager.DB_HOMO_SAPIENS_CORE_NAME);
            return str;
        } else {
            return "";
        }
    }

    private String parse(String r) {
        return r.trim().toUpperCase();
    }

    public int getLength() {
        int CumResult = 0;
        for (Iterator it = ExonList.iterator(); it.hasNext();) {
            Exon exon = (Exon) it.next();
            CumResult = CumResult + exon.getCoveredRegion().getLength();
        }
        return CumResult;
    }

    @Override
    public String toString() {
        return Name;
    }
}