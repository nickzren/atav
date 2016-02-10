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
    OMIM omim;
    ACMG acmg;
    AdultOnset adultOnset;
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
        omim = new OMIM(geneName);
        acmg = new ACMG(geneName);
        adultOnset = new AdultOnset(geneName);
        clinGen = new ClinGen(geneName);
        pgx = new PGx(geneName);
        recessiveCarrier = new RecessiveCarrier(geneName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinvar.toString()).append(",");
        sb.append(hgmd.toString()).append(",");
        sb.append(omim.toString()).append(",");
        sb.append(acmg.toString()).append(",");
        sb.append(adultOnset.toString()).append(",");
        sb.append(clinGen.toString()).append(",");
        sb.append(pgx.toString()).append(",");
        sb.append(recessiveCarrier.toString()).append(",");

        return sb.toString();
    }
}
