package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private ClinVarOutput clinVarOutput;
    private ClinVarPathoratio clinVarPathoratio;
    private HGMDOutput hgmdOutput;
    private String omimDiseaseName;
    private String acmg;
    private ClinGen clinGen;
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
        hgmdOutput = KnownVarManager.getHGMDOutput(annotatedVar);
        omimDiseaseName = KnownVarManager.getOMIM(geneName);
        acmg = KnownVarManager.getACMG(geneName);
        clinGen = KnownVarManager.getClinGen(geneName);
        recessiveCarrier = KnownVarManager.getRecessiveCarrier(geneName);
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

        return sb.toString();
    }
}
