package function.variant.base;

import function.external.bis.BisManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsManager;
import function.external.exac.ExacManager;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.mtr.MTRManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Output implements Cloneable {

    public static String getVariantDataTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "Rs Number,";
    }

    public static String getAnnotationDataTitle() {
        return "Transcript Stable Id,"
                + "Has CCDS Transcript,"
                + "Effect,"
                + "HGVS_c,"
                + "HGVS_p,"
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Gene Name,"
                + "All Effect Gene Transcript HGVS_p Polyphen_Humdiv Polyphen_Humvar,";
    }

    public static String getExternalDataTitle() {
        return EvsManager.getTitle()
                + ExacManager.getTitle()
                + ExacManager.getGeneVariantCountTitle()
                + GnomADManager.getExomeTitle()
                + GnomADManager.getGenomeTitle()
                + KnownVarManager.getTitle()
                + KaviarManager.getTitle()
                + GenomesManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + BisManager.getTitle()
                + GerpManager.getTitle()
                + TrapManager.getTitle()
                + MgiManager.getTitle()
                + DenovoDBManager.getTitle()
                + DiscovEHRManager.getTitle()
                + MTRManager.getTitle();
    }

    public static String getGenoStatDataTitle() {
        return "Hom Case,"
                + "Het Case,"
                + "Hom Ref Case,"
                + "Hom Case Freq,"
                + "Het Case Freq,"
                + "Hom Ctrl,"
                + "Het Ctrl,"
                + "Hom Ref Ctrl,"
                + "Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "QC Fail Case,"
                + "QC Fail Ctrl,"
                + "Covered Case,"
                + "Covered Ctrl,"
                + "Covered Sample Binomial P (two sided),"
                + "Case AF,"
                + "Ctrl AF,"
                + "Case HWE_P,"
                + "Ctrl HWE_P,";
    }

    public static String getCarrierDataTitle() {
        return "Sample Name,"
                + "Sample Type,"
                + "Sample Phenotype,"
                + "GT,"
                + "DP,"
                + "DP Bin,"
                + "AD REF,"
                + "AD ALT,"
                + "Percent Alt Read,"
                + "Percent Alt Read Binomial P,"
                + "GQ,"
                + "VQSLOD,"
                + "SOR,"
                + "FS,"
                + "MQ,"
                + "QD,"
                + "Qual,"
                + "Read Pos Rank Sum,"
                + "MQ Rank Sum,"
                + "FILTER,";
    }

    protected CalledVariant calledVar;

    public Output(CalledVariant c) {
        calledVar = c;
    }

    public CalledVariant getCalledVariant() {
        return calledVar;
    }

    public String getGenoStr(byte geno) {
        switch (geno) {
            case Index.HOM:
                return "hom";
            case Index.HET:
                return "het";
            case Index.REF:
                return "hom ref";
            case Data.BYTE_NA:
                return Data.STRING_NA;
            default:
                return Data.STRING_NA;
        }
    }

    public boolean isQualifiedGeno(byte geno) {
        return geno == Index.HOM || geno == Index.HET;
    }

    public void getGenoStatData(StringBuilder sb) {
        sb.append(calledVar.genoCount[Index.HOM][Index.CASE]
                + calledVar.genoCount[Index.HOM_MALE][Index.CASE]).append(",");
        sb.append(calledVar.genoCount[Index.HET][Index.CASE]).append(",");
        sb.append(calledVar.genoCount[Index.REF][Index.CASE]
                + calledVar.genoCount[Index.REF_MALE][Index.CASE]).append(",");
        sb.append(FormatManager.getFloat(calledVar.homFreq[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(calledVar.hetFreq[Index.CASE])).append(",");
        sb.append(calledVar.genoCount[Index.HOM][Index.CTRL]
                + calledVar.genoCount[Index.HOM_MALE][Index.CTRL]).append(",");
        sb.append(calledVar.genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(calledVar.genoCount[Index.REF][Index.CTRL]
                + calledVar.genoCount[Index.REF_MALE][Index.CTRL]).append(",");
        sb.append(FormatManager.getFloat(calledVar.homFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getFloat(calledVar.hetFreq[Index.CTRL])).append(",");
        sb.append(calledVar.getQcFailSample(Index.CASE)).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(calledVar.getCoveredSample(Index.CASE)).append(",");
        sb.append(calledVar.getCoveredSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCoveredSampleBinomialP())).append(",");
        sb.append(FormatManager.getFloat(calledVar.af[Index.CASE])).append(",");
        sb.append(FormatManager.getFloat(calledVar.af[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(calledVar.hweP[Index.CASE])).append(",");
        sb.append(FormatManager.getDouble(calledVar.hweP[Index.CTRL])).append(",");
    }

    public void getCarrierData(StringBuilder sb, Carrier carrier, Sample sample) {
        sb.append(sample.getName()).append(",");
        sb.append(sample.getType()).append(",");
        sb.append(sample.getPhenotype()).append(",");
        sb.append(getGenoStr(calledVar.getGT(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(calledVar.getDPBin(sample.getIndex()))).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADRef() : Data.SHORT_NA)).append(",");
        sb.append(FormatManager.getShort(carrier != null ? carrier.getADAlt() : Data.SHORT_NA)).append(",");
        sb.append(carrier != null ? carrier.getPercAltRead() : Data.STRING_NA).append(",");
        sb.append(carrier != null ? FormatManager.getDouble(carrier.getPercentAltReadBinomialP()) : Data.STRING_NA).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getVQSLOD() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getSOR() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getMQ() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getByte(carrier != null ? carrier.getQD() : Data.BYTE_NA)).append(",");
        sb.append(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA)).append(",");
        sb.append(carrier != null ? carrier.getFILTER() : Data.STRING_NA).append(",");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Output output = (Output) super.clone();

        output.calledVar.genoCount = FormatManager.deepCopyArray(calledVar.genoCount);
        output.calledVar.homFreq = FormatManager.deepCopyArray(calledVar.homFreq);
        output.calledVar.hetFreq = FormatManager.deepCopyArray(calledVar.hetFreq);
        output.calledVar.af = FormatManager.deepCopyArray(calledVar.af);
        output.calledVar.hweP = FormatManager.deepCopyArray(calledVar.hweP);

        return output;
    }
}
