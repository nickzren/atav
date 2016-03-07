package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    ClinVar clinVar;
    ClinVarPathoratio clinVarPathoratio;
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
        String geneName = annotatedVar.getGeneName().toUpperCase();
        clinVar = KnownVarManager.getClinVar(annotatedVar);
        clinVarPathoratio = KnownVarManager.getClinPathoratio(geneName);
        hgmd = KnownVarManager.getHGMD(annotatedVar);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
        acmg = KnownVarManager.getACMG(geneName);
        adultOnset = KnownVarManager.getAdultOnset(geneName);
        clinGen = KnownVarManager.getClinGen(geneName);
        pgx = KnownVarManager.getPGx(geneName);
        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(clinVar.toString());
        sb.append(clinVarPathoratio.toString());
        sb.append(hgmd.toString());
        sb.append(omimDiseaseName).append(",");
        sb.append(acmg).append(",");
        sb.append(adultOnset).append(",");
        sb.append(clinGen.toString());
        sb.append(pgx).append(",");
        sb.append(recessiveCarrier).append(",");

        return sb.toString();
    }
}
