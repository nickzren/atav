package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private ClinVarOutput clinVarOutput;
    private ClinVarPathoratio clinVarPathoratio;
    private HGMD hgmd;
    private String omimDiseaseName;
    private String acmg;
    private String adultOnset;
    private ClinGen clinGen;
    private String pgx;
    private int recessiveCarrier;

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + KnownVarManager.getTitle();
    }

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        String geneName = annotatedVar.getGeneName().toUpperCase();
        clinVarOutput = KnownVarManager.getClinVarOutput(annotatedVar);
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

        sb.append(clinVarOutput.toString());
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
