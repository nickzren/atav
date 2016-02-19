package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    String variantIdStr;
    String geneName;
    Clinvar clinvar;
    HGMD hgmd;
    String omimDiseaseName; 
    String acmg;
    String adultOnset;
    ClinGen clinGen;
    String pgx;
    int recessiveCarrier;

    public static final String title
            = "Variant ID,"
            + "Gene Name,"
            + KnownVarManager.getTitle();

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        variantIdStr = annotatedVar.variantIdStr;
        geneName = annotatedVar.getGeneName();
        clinvar = KnownVarManager.getClinvar(annotatedVar);
        hgmd = KnownVarManager.getHGMD(annotatedVar);
        
        String name = geneName.toUpperCase();
        omimDiseaseName = KnownVarManager.getOMIM(name);
        acmg = KnownVarManager.getACMG(name);
        adultOnset = KnownVarManager.getAdultOnset(name);
        clinGen = KnownVarManager.getClinGen(name);
        pgx = KnownVarManager.getPGx(name);
        recessiveCarrier = KnownVarManager.getRecessiveCarrier(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinvar.toString()).append(",");
        sb.append(hgmd.toString()).append(",");
        sb.append(omimDiseaseName).append(",");
        sb.append(acmg).append(",");
        sb.append(adultOnset).append(",");
        sb.append(clinGen.toString()).append(",");
        sb.append(pgx).append(",");
        sb.append(recessiveCarrier).append(",");

        return sb.toString();
    }
}
