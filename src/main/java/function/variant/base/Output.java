package function.variant.base;

import function.external.ccr.CCRCommand;
import function.external.ccr.CCRManager;
import function.external.denovo.DenovoDBCommand;
import function.external.limbr.LIMBRManager;
import function.external.denovo.DenovoDBManager;
import function.external.discovehr.DiscovEHRCommand;
import function.external.discovehr.DiscovEHRManager;
import function.external.evs.EvsCommand;
import function.external.evs.EvsManager;
import function.external.exac.ExACCommand;
import function.external.exac.ExACManager;
import function.external.gnomad.GnomADManager;
import function.external.gerp.GerpCommand;
import function.external.gerp.GerpManager;
import function.external.gnomad.GnomADCommand;
import function.external.knownvar.KnownVarCommand;
import function.external.knownvar.KnownVarManager;
import function.external.limbr.LIMBRCommand;
import function.external.mgi.MgiCommand;
import function.external.mgi.MgiManager;
import function.external.mtr.MTRCommand;
import function.external.mtr.MTRManager;
import function.external.primateai.PrimateAICommand;
import function.external.primateai.PrimateAIManager;
import function.external.revel.RevelCommand;
import function.external.revel.RevelManager;
import function.external.rvis.RvisCommand;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisCommand;
import function.external.subrvis.SubRvisManager;
import function.external.trap.TrapCommand;
import function.external.trap.TrapManager;
import function.cohort.base.CalledVariant;
import function.cohort.base.Carrier;
import function.cohort.base.CohortLevelFilterCommand;
import function.cohort.base.GenotypeLevelFilterCommand;
import function.cohort.base.Sample;
import function.external.chm.CHMCommand;
import function.external.chm.CHMManager;
import function.external.genomeasia.GenomeAsiaCommand;
import function.external.genomeasia.GenomeAsiaManager;
import function.external.gevir.GeVIRCommand;
import function.external.gevir.GeVIRManager;
import function.external.gme.GMECommand;
import function.external.gme.GMEManager;
import function.external.igmaf.IGMAFCommand;
import function.external.igmaf.IGMAFManager;
import function.external.iranome.IranomeCommand;
import function.external.iranome.IranomeManager;
import function.external.mpc.MPCCommand;
import function.external.mpc.MPCManager;
import function.external.pext.PextCommand;
import function.external.pext.PextManager;
import function.external.synrvis.SynRvisCommand;
import function.external.synrvis.SynRvisManager;
import function.external.topmed.TopMedCommand;
import function.external.topmed.TopMedManager;
import global.Data;
import global.Index;
import java.util.StringJoiner;
import utils.FormatManager;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class Output {

    public static StringJoiner getVariantDataHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Variant ID");
        sj.add("Variant Type");
        sj.add("Ref Allele");
        sj.add("Alt Allele");
        sj.add("Rs Number");

        return sj;
    }

    public static StringJoiner getAnnotationDataHeader() {
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
        sj.add("UpToDate Gene Name");
        sj.add("All Gene Symbols");
        sj.add("Consequence annotations: Effect|Gene|Transcript|HGVS_c|HGVS_p|Polyphen_Humdiv|Polyphen_Humvar");

        return sj;
    }

    public static StringJoiner getExternalDataHeader() {
        StringJoiner sj = new StringJoiner(",");

        if (EvsCommand.isInclude) {
            sj.add(EvsManager.getHeader());
        }

        if (ExACCommand.isInclude) {
            sj.add(ExACManager.getHeader());
        }

        if (ExACCommand.isIncludeCount) {
            sj.add(ExACManager.getGeneVariantCountHeader());
        }

        if (GnomADCommand.isIncludeExome) {
            sj.add(GnomADManager.getExomeHeader());
        }

        if (GnomADCommand.isIncludeGenome) {
            sj.add(GnomADManager.getGenomeHeader());
        }

        if (GnomADCommand.isIncludeGeneMetrics) {
            sj.add(GnomADManager.getGeneMetricsHeader());
        }

        if (KnownVarCommand.isInclude) {
            sj.add(KnownVarManager.getHeader());
        }

        if (RvisCommand.isInclude) {
            sj.add(RvisManager.getHeader());
        }

        if (SubRvisCommand.isInclude) {
            sj.add(SubRvisManager.getHeader());
        }

        if (GeVIRCommand.isInclude) {
            sj.add(GeVIRManager.getHeader());
        }

        if (SynRvisCommand.isInclude) {
            sj.add(SynRvisManager.getHeader());
        }

        if (LIMBRCommand.isInclude) {
            sj.add(LIMBRManager.getHeader());
        }

        if (CCRCommand.isInclude) {
            sj.add(CCRManager.getHeader());
        }

        if (GerpCommand.isInclude) {
            sj.add(GerpManager.getHeader());
        }

        if (TrapCommand.isInclude) {
            sj.add(TrapManager.getHeader());
        }

        if (MgiCommand.isInclude) {
            sj.add(MgiManager.getHeader());
        }

        if (DenovoDBCommand.isInclude) {
            sj.add(DenovoDBManager.getHeader());
        }

        if (DiscovEHRCommand.isInclude) {
            sj.add(DiscovEHRManager.getHeader());
        }

        if (MTRCommand.isInclude) {
            sj.add(MTRManager.getHeader());
        }

        if (RevelCommand.isInclude) {
            sj.add(RevelManager.getHeader());
        }

        if (PrimateAICommand.isInclude) {
            sj.add(PrimateAIManager.getHeader());
        }

        if (VariantLevelFilterCommand.isIncludeLOFTEE) {
            sj.add("LOFTEE-HC in CCDS");
        }

        if (MPCCommand.isInclude) {
            sj.add(MPCManager.getHeader());
        }

        if (PextCommand.isInclude) {
            sj.add(PextManager.getHeader());
        }

        if (CHMCommand.isFlag) {
            sj.add(CHMManager.getHeader());
        }

        if (GMECommand.isInclude) {
            sj.add(GMEManager.getHeader());
        }

        if (TopMedCommand.isInclude) {
            sj.add(TopMedManager.getHeader());
        }

        if (GenomeAsiaCommand.isInclude) {
            sj.add(GenomeAsiaManager.getHeader());
        }

        if (IranomeCommand.isInclude) {
            sj.add(IranomeManager.getHeader());
        }
        
        if (IGMAFCommand.isInclude) {
            sj.add(IGMAFManager.getHeader());
        }

        return sj;
    }

    // quick hack here, eventually will get rid of min covered sample binomial p
    public static StringJoiner getCohortLevelHeader() {
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
        if (CohortLevelFilterCommand.minCoveredSampleBinomialP != Data.NO_FILTER) {
            sj.add("Covered Sample Binomial P (two sided)");
        }
        sj.add("Case AF");
        sj.add("Ctrl AF");
        sj.add("Case HWE_P");
        sj.add("Ctrl HWE_P");

        return sj;
    }

    public static StringJoiner getCarrierDataHeader() {
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
    // The value will be dynamically updated per sample
    private float looAF = 0;

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
        // --hom-only
        if (GenotypeLevelFilterCommand.isHomOnly) {
            return geno == Index.HOM;
        }

        // --het-only
        if (GenotypeLevelFilterCommand.isHetOnly) {
            return geno == Index.HET;
        }

        // --include-hom-ref
        if (GenotypeLevelFilterCommand.isIncludeHomRef && geno == Index.REF) {
            return true;
        }

        // default: hom alt or het is valid 
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
        if (CohortLevelFilterCommand.minCoveredSampleBinomialP != Data.NO_FILTER) {
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

    public void calculateLooAF(Sample sample) {
        if (sample.getId() != Data.INTEGER_NA) {
            byte geno = calledVar.getGT(sample.getIndex());

            // delete current sample geno as 'leave one out' concept
            calledVar.deleteSampleGeno(geno, sample);

            // calculateLooAF
            int alleleCount = 2 * calledVar.genoCount[Index.HOM][Index.CASE]
                    + calledVar.genoCount[Index.HET][Index.CASE]
                    + 2 * calledVar.genoCount[Index.HOM][Index.CTRL]
                    + calledVar.genoCount[Index.HET][Index.CTRL];
            int totalCount = alleleCount
                    + calledVar.genoCount[Index.HET][Index.CASE]
                    + 2 * calledVar.genoCount[Index.REF][Index.CASE]
                    + calledVar.genoCount[Index.HET][Index.CTRL]
                    + 2 * calledVar.genoCount[Index.REF][Index.CTRL];

            looAF = MathManager.devide(alleleCount, totalCount);

            // add deleted sample geno back
            calledVar.addSampleGeno(geno, sample);
        }
    }

    public boolean isMaxLooAFValid() {
        return CohortLevelFilterCommand.isLooAFValid(looAF);
    }

    public double getLooAf() {
        return looAF;
    }
}
