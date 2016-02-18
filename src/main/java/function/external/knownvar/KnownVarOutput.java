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
    PGx pgx;
    RecessiveCarrier recessiveCarrier;

    public static final String title
            = "Variant ID,"
            + "Gene Name,"
            + KnownVarManager.getTitle();

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        variantIdStr = annotatedVar.variantIdStr;
        geneName = annotatedVar.getGeneName();
        clinvar = KnownVarManager.getClinvar(annotatedVar);
        hgmd = KnownVarManager.getHGMD(annotatedVar);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
        acmg = KnownVarManager.getACMG(geneName);
        adultOnset = KnownVarManager.getAdultOnset(geneName);
        clinGen = new ClinGen(geneName);
        pgx = new PGx(geneName);
        recessiveCarrier = new RecessiveCarrier(geneName);
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
        sb.append(pgx.toString()).append(",");
        sb.append(recessiveCarrier.toString()).append(",");

        return sb.toString();
    }
}
