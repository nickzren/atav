package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private HGMDOutput hgmdOutput;
    private ClinVarOutput clinVarOutput;
    private ClinVarPathoratio clinVarPathoratio;
    private ClinGen clinGen;
    private String omimDiseaseName;
    private int recessiveCarrier;
    private String acmg;
    private DBDSMOutput dbDSMOutput;

    public static String getTitle() {
        return "Variant ID,"
                + "Gene Name,"
                + KnownVarManager.getTitle();
    }

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        String geneName = annotatedVar.getGeneName().toUpperCase();
        hgmdOutput = KnownVarManager.getHGMDOutput(annotatedVar);
        clinVarOutput = KnownVarManager.getClinVarOutput(annotatedVar);
        clinVarPathoratio = KnownVarManager.getClinPathoratio(geneName);
        clinGen = KnownVarManager.getClinGen(geneName);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
        acmg = KnownVarManager.getACMG(geneName);
        dbDSMOutput = KnownVarManager.getDBDSMOutput(annotatedVar);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(hgmdOutput.toString());
        sb.append(clinVarOutput.toString());
        sb.append(clinVarPathoratio.toString());
        sb.append(clinGen.toString());
        sb.append(omimDiseaseName).append(",");
        sb.append(recessiveCarrier).append(",");
        sb.append(acmg).append(",");
        sb.append(dbDSMOutput.toString());

        return sb.toString();
    }
}
