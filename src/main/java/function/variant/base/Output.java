package function.variant.base;

import function.external.denovo.DenovoDBCommand;
import function.external.limbr.LIMBRManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExacCommand;
import function.external.exac.ExacManager;
import function.external.genomes.GenomesCommand;
import function.external.gnomad.GnomADManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gnomad.GnomADCommand;
import function.external.kaviar.KaviarCommand;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.limbr.LIMBRCommand;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mtr.MTRCommand;
import function.external.mtr.MTRManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Carrier;
import function.genotype.base.GenotypeLevelFilterCommand;
import static function.genotype.base.GenotypeLevelFilterCommand.isIncludeHomRef;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;

/**
 *
 * @author nick
 */
public class Output {

    public static StringJoiner getVariantDataTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.add("Variant ID");
        sj.add("Variant Type");
        sj.add("Ref Allele");
        sj.add("Alt Allele");
        sj.add("Rs Number");
        
        return sj;
    }

    public static StringJoiner getAnnotationDataTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.add("Transcript Stable Id");
        sj.add("Has CCDS Transcript");
        sj.add("Effect");
        sj.add("HGVS_c");
        sj.add("HGVS_p");
        sj.add("Polyphen Humdiv Score");
        sj.add("Polyphen Humdiv Prediction");
        sj.add("Polyphen Humdiv Score (CCDS)");
        sj.add("Polyphen Humdiv Prediction (CCDS)");
        sj.add("Polyphen Humvar Score");
        sj.add("Polyphen Humvar Prediction");
        sj.add("Polyphen Humvar Score (CCDS)");
        sj.add("Polyphen Humvar Prediction (CCDS)");
        sj.add("Gene Name");
        sj.add("All Effect Gene Transcript HGVS_p Polyphen_Humdiv Polyphen_Humvar");
        
        return sj;
    }

    public static StringJoiner getExternalDataTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        if (EvsCommand.isIncludeEvs) {
            sj.add(EvsManager.getTitle());
        }

        if (ExacCommand.isIncludeExac) {
            sj.add(ExacManager.getTitle());
        }

        if (ExacCommand.isIncludeExacGeneVariantCount) {
            sj.add(ExacManager.getGeneVariantCountTitle());
        }

        if (GnomADCommand.isIncludeGnomADExome) {
            sj.add(GnomADManager.getExomeTitle());
        }

        if (GnomADCommand.isIncludeGnomADGenome) {
            sj.add(GnomADManager.getGenomeTitle());
        }

        if (KnownVarCommand.isIncludeKnownVar) {
            sj.add(KnownVarManager.getTitle());
        }

        if (KaviarCommand.isIncludeKaviar) {
            sj.add(KaviarManager.getTitle());
        }

        if (GenomesCommand.isInclude1000Genomes) {
            sj.add(GenomesManager.getTitle());
        }

        if (RvisCommand.isIncludeRvis) {
            sj.add(RvisManager.getTitle());
        }

        if (SubRvisCommand.isIncludeSubRvis) {
            sj.add(SubRvisManager.getTitle());
        }

        if (LIMBRCommand.isIncludeLIMBR) {
            sj.add(LIMBRManager.getTitle());
        }

        if (GerpCommand.isIncludeGerp) {
            sj.add(GerpManager.getTitle());
        }

        if (TrapCommand.isIncludeTrap) {
            sj.add(TrapManager.getTitle());
        }

        if (MgiCommand.isIncludeMgi) {
            sj.add(MgiManager.getTitle());
        }

        if (DenovoDBCommand.isIncludeDenovoDB) {
            sj.add(DenovoDBManager.getTitle());
        }

        if (DiscovEHRCommand.isIncludeDiscovEHR) {
            sj.add(DiscovEHRManager.getTitle());
        }

        if (MTRCommand.isIncludeMTR) {
            sj.add(MTRManager.getTitle());
        }
        
        if (RevelCommand.isIncludeRevel) {
            sj.add(RevelManager.getTitle());
        }
        
        return sj;
    }

    // quick hack here, eventually will get rid of min covered sample binomial p
    public static StringJoiner getGenoStatDataTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.add("Hom Case");
        sj.add("Het Case");
        sj.add("Hom Ref Case");
        sj.add("Hom Case Freq");
        sj.add("Het Case Freq");
        sj.add("Hom Ctrl");
        sj.add("Het Ctrl");
        sj.add("Hom Ref Ctrl");
        sj.add("Hom Ctrl Freq");
        sj.add("Het Ctrl Freq");
        sj.add("QC Fail Case");
        sj.add("QC Fail Ctrl");
        sj.add("Covered Case");
        sj.add("Covered Ctrl");
        sj.add("Covered Case Percentage");
        sj.add("Covered Ctrl Percentage");
        if (GenotypeLevelFilterCommand.minCoveredSampleBinomialP != Data.NO_FILTER) {
            sj.add("Covered Sample Binomial P (two sided)");
        }
        sj.add("Case AF");
        sj.add("Ctrl AF");
        sj.add("Case HWE_P");
        sj.add("Ctrl HWE_P");
        
        return sj;
    }

    public static StringJoiner getCarrierDataTitle() {
        StringJoiner sj = new StringJoiner(",");
        
        sj.add("Sample Name");
        sj.add("Sample Type");
        sj.add("Sample Phenotype");
        sj.add("GT");
        sj.add("DP");
        sj.add("DP Bin");
        sj.add("AD REF");
        sj.add("AD ALT");
        sj.add("Percent Alt Read");
        sj.add("Percent Alt Read Binomial P");
        sj.add("GQ");
        sj.add("VQSLOD");
        sj.add("SOR");
        sj.add("FS");
        sj.add("MQ");
        sj.add("QD");
        sj.add("Qual");
        sj.add("Read Pos Rank Sum");
        sj.add("MQ Rank Sum");
        sj.add("FILTER");
        
        return sj;
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
        if (isIncludeHomRef && geno == Index.REF) {
            return true;
        }

        return geno == Index.HOM || geno == Index.HET;
    }

    public void getGenoStatData(StringJoiner sj) {
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.HOM][Index.CASE]));
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.HET][Index.CASE]));
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.REF][Index.CASE]));
        sj.add(FormatManager.getFloat(calledVar.homFreq[Index.CASE]));
        sj.add(FormatManager.getFloat(calledVar.hetFreq[Index.CASE]));
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.HOM][Index.CTRL]));
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.HET][Index.CTRL]));
        sj.add(FormatManager.getInteger(calledVar.genoCount[Index.REF][Index.CTRL]));
        sj.add(FormatManager.getFloat(calledVar.homFreq[Index.CTRL]));
        sj.add(FormatManager.getFloat(calledVar.hetFreq[Index.CTRL]));
        sj.add(FormatManager.getInteger(calledVar.getQcFailSample(Index.CASE)));
        sj.add(FormatManager.getInteger(calledVar.getQcFailSample(Index.CTRL)));
        sj.add(FormatManager.getInteger(calledVar.getCoveredSample(Index.CASE)));
        sj.add(FormatManager.getInteger(calledVar.getCoveredSample(Index.CTRL)));
        sj.add(FormatManager.getFloat(calledVar.getCoveredSamplePercentage(Index.CASE)));
        sj.add(FormatManager.getFloat(calledVar.getCoveredSamplePercentage(Index.CTRL)));
        if (GenotypeLevelFilterCommand.minCoveredSampleBinomialP != Data.NO_FILTER) {
            sj.add(FormatManager.getDouble(calledVar.getCoveredSampleBinomialP()));
        }
        sj.add(FormatManager.getFloat(calledVar.af[Index.CASE]));
        sj.add(FormatManager.getFloat(calledVar.af[Index.CTRL]));
        sj.add(FormatManager.getDouble(calledVar.hweP[Index.CASE]));
        sj.add(FormatManager.getDouble(calledVar.hweP[Index.CTRL]));
    }

    public void getCarrierData(StringJoiner sj, Carrier carrier, Sample sample) {
        sj.add(sample.getName());
        sj.add(sample.getType());
        sj.add(sample.getPhenotype());
        sj.add(getGenoStr(calledVar.getGT(sample.getIndex())));
        sj.add(FormatManager.getShort(carrier != null ? carrier.getDP() : Data.SHORT_NA));
        sj.add(FormatManager.getShort(calledVar.getDPBin(sample.getIndex())));
        sj.add(FormatManager.getShort(carrier != null ? carrier.getADRef() : Data.SHORT_NA));
        sj.add(FormatManager.getShort(carrier != null ? carrier.getADAlt() : Data.SHORT_NA));
        sj.add(carrier != null ? carrier.getPercAltRead() : Data.STRING_NA);
        sj.add(carrier != null ? FormatManager.getDouble(carrier.getPercentAltReadBinomialP()) : Data.STRING_NA);
        sj.add(FormatManager.getByte(carrier != null ? carrier.getGQ() : Data.BYTE_NA));
        sj.add(FormatManager.getFloat(carrier != null ? carrier.getVQSLOD() : Data.FLOAT_NA));
        sj.add(FormatManager.getFloat(carrier != null ? carrier.getSOR() : Data.FLOAT_NA));
        sj.add(FormatManager.getFloat(carrier != null ? carrier.getFS() : Data.FLOAT_NA));
        sj.add(FormatManager.getByte(carrier != null ? carrier.getMQ() : Data.BYTE_NA));
        sj.add(FormatManager.getByte(carrier != null ? carrier.getQD() : Data.BYTE_NA));
        sj.add(FormatManager.getInteger(carrier != null ? carrier.getQual() : Data.INTEGER_NA));
        sj.add(FormatManager.getFloat(carrier != null ? carrier.getReadPosRankSum() : Data.FLOAT_NA));
        sj.add(FormatManager.getFloat(carrier != null ? carrier.getMQRankSum() : Data.FLOAT_NA));
        sj.add(carrier != null ? carrier.getFILTER() : Data.STRING_NA);
    }
}
