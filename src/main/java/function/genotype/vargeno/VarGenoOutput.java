package function.genotype.vargeno;

import function.genotype.base.CalledVariant;
import function.variant.base.Output;
import function.genotype.base.Sample;
import global.Index;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.annotation.base.TranscriptManager;
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
import utils.MathManager;

/**
 *
 * @author nick
 */
public class VarGenoOutput extends Output {

    private double percentAltReadBinomialP = Data.NA;

    public static String getTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "Rs Number,"
                // annotation data
                + "Transcript Stable Id,"
                + "Is CCDS Transcript,"
                + "Effect,"
                + "HGVS_c,"
                + "HGVS_p,"
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "All Effect Gene Transcript HGVS_p,"
                // external data
                + EvsManager.getTitle()
                + ExacManager.getTitle()
                + KnownVarManager.getTitle()
                + KaviarManager.getTitle()
                + GenomesManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GerpManager.getTitle()
                + TrapManager.getTitle()
                + MgiManager.getTitle()
                // genotype data
                + "Sample Name,"
                + "Sample Type,"
                + "GT,"
                + "DP,"
                + "DP Bin,"
                + "AD_REF,"
                + "AD_ALT,"
                + "Percent Alt Read,"
                + "Percent Alt Read Binomial P,"
                + "GQ,"
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,"
                + "Is Minor Ref,"
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
                + "Case Maf,"
                + "Ctrl Maf,"
                + "Case HWE_P,"
                + "Ctrl HWE_P,";
    }

    public VarGenoOutput(CalledVariant c) {
        super(c);
    }

    public String getString(Sample sample) {
        StringBuilder sb = new StringBuilder();

        Carrier carrier = calledVar.getCarrier(sample.getId());
        int readsAlt = carrier != null ? carrier.getAdAlt() : Data.NA;
        int readsRef = carrier != null ? carrier.getADRef() : Data.NA;

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        // annotation data
        sb.append(calledVar.getStableId()).append(",");
        sb.append(TranscriptManager.isCCDSTranscript((calledVar.getStableId()))).append(",");
        sb.append(calledVar.getEffect()).append(",");
        sb.append(calledVar.getHGVS_c()).append(",");
        sb.append(calledVar.getHGVS_p()).append(",");
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getAllGeneTranscript()).append(",");
        // external data
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.getGerpScore());
        sb.append(calledVar.getTrapScore());
        sb.append(calledVar.getMgi());
        // genotype data
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(calledVar.getDPBin(sample.getIndex()))).append(",");
        sb.append(FormatManager.getInteger(readsRef)).append(",");
        sb.append(FormatManager.getInteger(readsAlt)).append(",");
        sb.append(FormatManager.getPercAltRead(readsAlt, carrier != null ? carrier.getDP() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(MathManager.getBinomial(readsAlt + readsRef, readsAlt, 0.5))).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getGQ() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getFS() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getMQ() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getQD() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getQual() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getReadPosRankSum() : Data.NA)).append(",");
        sb.append(FormatManager.getDouble(carrier != null ? carrier.getMQRankSum() : Data.NA)).append(",");        
        sb.append(carrier != null ? carrier.getFILTER() : "NA").append(",");
        sb.append(isMinorRef).append(",");
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
        sb.append(FormatManager.getDouble(hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");

        return sb.toString();
    }
}
