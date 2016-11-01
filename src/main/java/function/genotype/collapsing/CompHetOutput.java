package function.genotype.collapsing;

import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.Carrier;
import global.Data;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class CompHetOutput extends CollapsingOutput implements Comparable {

    public static String getTitle() {
        return "Family ID,"
                + "Sample Name,"
                + "Sample Type,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "Var Case Freq #1 & #2 (co-occurance),"
                + "Var Ctrl Freq #1 & #2 (co-occurance),"
                + initVarTitleStr("1") + ","
                + initVarTitleStr("2");
    }

    private static String initVarTitleStr(String var) {
        String varTitle = "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + GerpManager.getTitle()
                + TrapManager.getTitle()
                + "Is Minor Ref,"
                + "Genotype,"
                + "Samtools Raw Coverage,"
                + "Gatk Filtered Coverage,"
                + "Reads Alt,"
                + "Reads Ref,"
                + "Percent Alt Read,"
                + "Major Hom Case,"
                + "Het Case,"
                + "Minor Hom Case,"
                + "Minor Hom Case Freq,"
                + "Het Case Freq,"
                + "Major Hom Ctrl,"
                + "Het Ctrl,"
                + "Minor Hom Ctrl,"
                + "Minor Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "Missing Case,"
                + "QC Fail Case,"
                + "Missing Ctrl,"
                + "QC Fail Ctrl,"
                + "Case MAF,"
                + "Ctrl MAF,"
                + "LOO MAF,"
                + "Case HWE_P,"
                + "Ctrl HWE_P,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Codon Change,"
                + ExacManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle();

        String[] list = varTitle.split(",");

        varTitle = "";

        boolean isFirst = true;

        for (String s : list) {
            if (isFirst) {
                varTitle += s + " (#" + var + ")";
                isFirst = false;
            } else {
                varTitle += "," + s + " (#" + var + ")";
            }
        }

        return varTitle;
    }

    public CompHetOutput(CalledVariant c) {
        super(c);
    }

    @Override
    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        Carrier carrier = calledVar.getCarrier(sample.getId());

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
        sb.append(isMinorRef).append(",");
        sb.append(getGenoStr(calledVar.getGenotype(sample.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoverage(sample.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getAdAlt() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getADRef() : Data.NA)).append(",");
        sb.append(FormatManager.getPercAltRead(carrier != null ? carrier.getAdAlt() : Data.NA,
                carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(majorHomCount[Index.CASE]).append(",");
        sb.append(genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(minorHomCount[Index.CASE]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CASE])).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CTRL])).append(",");
        sb.append(genoCount[Index.MISSING][Index.CASE]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(looMAF)).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getEffect()).append(",");
        sb.append(calledVar.getHGVS_c()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());

        return sb.toString();
    }

    public boolean isHomOrRef(int geno) {
        return geno == Index.HOM || geno == Index.REF;
    }

    @Override
    public int compareTo(Object another) throws ClassCastException {
        CollapsingOutput that = (CollapsingOutput) another;
        return this.geneName.compareTo(that.geneName); //small -> large
    }
}
