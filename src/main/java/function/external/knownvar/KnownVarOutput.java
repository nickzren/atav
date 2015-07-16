package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import global.Data;
import java.sql.ResultSet;
import utils.DBManager;
import utils.ErrorManager;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    public static final String clinvarTable = "knownvar.clinvar_2015_06_22";
    public static final String hgmdTable = "knownvar.hgmd";
    public static final String omimTable = "knownvar.omim_2015_07_15";

    AnnotatedVariant annotatedVar;

    // clinvar
    private String clinvarClinicalSignificance;
    private String clinvarOtherIds;
    private String clinvarDiseaseName;
    int clinvarFlankingCount;

    // hgmd
    private String hgmdVariantClass;
    private String hgmdPmid;
    private String hgmdDiseaseName;
    int hgmdFlankingCount;

    // omim
    String omimDiseaseName;

    public static final String title
            = "Variant ID,"
            + "Clinvar Clinical Significance,"
            + "Clinvar Other Ids,"
            + "Clinvar Disease Name,"
            + "Clinvar Flanking Count,"
            + "HGMD Variant Class,"
            + "HGMD Pmid,"
            + "HGMD Disease Name,"
            + "HGMD Flanking Count,"
            + "OMIM Gene Name,"
            + "OMIM Disease Name";

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        this.annotatedVar = annotatedVar;

        initClinvar();

        initHGMD();

        initOMIM();
    }

    private void initClinvar() {
        try {
            String[] tmp = annotatedVar.variantIdStr.split("-"); // chr-pos-ref-alt

            String sql = "SELECT ClinicalSignificance,"
                    + "OtherIds,"
                    + "DiseaseName "
                    + "From " + clinvarTable + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos=" + tmp[1] + " "
                    + "AND ref='" + tmp[2] + "' "
                    + "AND alt='" + tmp[3] + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                clinvarClinicalSignificance = FormatManager.getString(rs.getString("ClinicalSignificance").replaceAll(";", " | "));
                clinvarOtherIds = FormatManager.getString(rs.getString("OtherIds")).replaceAll(",", " | ");
                clinvarDiseaseName = FormatManager.getString(rs.getString("DiseaseName")).replaceAll(",", "");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        clinvarFlankingCount = getFlankingCount(clinvarTable);
    }

    private void initHGMD() {
        try {
            String[] tmp = annotatedVar.variantIdStr.split("-"); // chr-pos-ref-alt

            String sql = "SELECT variantClass,"
                    + "pmid,"
                    + "DiseaseName "
                    + "From " + hgmdTable + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos=" + tmp[1] + " "
                    + "AND ref='" + tmp[2] + "' "
                    + "AND alt='" + tmp[3] + "'";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                hgmdVariantClass = FormatManager.getString(rs.getString("variantClass"));
                hgmdPmid = FormatManager.getString(rs.getString("pmid"));
                hgmdDiseaseName = FormatManager.getString(rs.getString("DiseaseName"));
            }

            while (rs.next()) // for variant that having multi annotations
            {
                hgmdVariantClass += " | " + FormatManager.getString(rs.getString("variantClass"));
                hgmdPmid += " | " + FormatManager.getString(rs.getString("pmid"));
                hgmdDiseaseName += " | " + FormatManager.getString(rs.getString("DiseaseName"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }

        hgmdFlankingCount = getFlankingCount(hgmdTable);
    }

    private int getFlankingCount(String table) {
        try {
            String[] tmp = annotatedVar.variantIdStr.split("-"); // chr-pos-ref-alt

            int pos = Integer.valueOf(tmp[1]);
            int width = KnownVarCommand.snvWidth;

            if (tmp[2].length() > 1
                    || tmp[3].length() > 1) {
                width = KnownVarCommand.indelWidth;
            }

            String sql = "SELECT count(*) as count "
                    + "From " + table + " "
                    + "WHERE chr='" + tmp[0] + "' "
                    + "AND pos BETWEEN " + (pos - width) + " AND " + (pos + width);

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt("count");
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
        
        return Data.NA;
    }

    private void initOMIM() {
        try {
            String sql = "SELECT diseaseName "
                    + "From " + omimTable + " "
                    + "WHERE geneName='" + annotatedVar.getGeneName() + "' ";

            ResultSet rs = DBManager.executeQuery(sql);

            if (rs.next()) {
                omimDiseaseName = FormatManager.getString(rs.getString("diseaseName"));
            }

            rs.close();
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(annotatedVar.variantIdStr).append(",");
        sb.append(FormatManager.getString(clinvarClinicalSignificance)).append(",");
        sb.append(FormatManager.getString(clinvarOtherIds)).append(",");
        sb.append(FormatManager.getString(clinvarDiseaseName)).append(",");
        sb.append(clinvarFlankingCount).append(",");
        sb.append(FormatManager.getString(hgmdVariantClass)).append(",");
        sb.append(FormatManager.getString(hgmdPmid)).append(",");
        sb.append(FormatManager.getString(hgmdDiseaseName)).append(",");
        sb.append(hgmdFlankingCount).append(",");
        sb.append("'").append(annotatedVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getString(omimDiseaseName));

        return sb.toString();
    }
}
