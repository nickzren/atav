package function.external.knownvar;

import function.annotation.base.AnnotatedVariant;
import java.util.StringJoiner;
import utils.FormatManager;

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
        StringJoiner sj = new StringJoiner(",");

        sj.add(hgmdOutput.toString());
        sj.add(clinVarOutput.toString());
        sj.add(clinVarPathoratio.toString());
        sj.add(clinGen.toString());
        sj.add(omimDiseaseName);
        sj.add(FormatManager.getInteger(recessiveCarrier));
        sj.add(acmg);
        sj.add(dbDSMOutput.toString());

        return sj.toString();
    }
}
