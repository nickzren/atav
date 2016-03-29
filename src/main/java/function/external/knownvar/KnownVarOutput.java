package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import function.external.genomes.GenomesManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;

/**
 *
 * @author nick
 */
public class KnownVarOutput {

    private ClinVar clinVar;
    private ClinVarPathoratio clinVarPathoratio;
    private HGMD hgmd;
    private String omimDiseaseName;
    private String acmg;
    private String adultOnset;
    private ClinGen clinGen;
    private String pgx;
    private int recessiveCarrier;

    private boolean isValid = true;

    public static final String title
            = "Variant ID,"
            + "Gene Name,"
            + KnownVarManager.getTitle()
            + RvisManager.getTitle()
            + SubRvisManager.getTitle()
            + GenomesManager.getTitle();

    public KnownVarOutput(AnnotatedVariant annotatedVar) {
        checkValid(annotatedVar);

        if (isValid) {
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
    }

    private void checkValid(AnnotatedVariant annotatedVar) {
        if (KnownVarCommand.isKnownVarOnly) {
            isValid = KnownVarManager.isClinVar(annotatedVar)
                    || KnownVarManager.isHGMD(annotatedVar);
        }
    }

    public boolean isValid() {
        return isValid;
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
