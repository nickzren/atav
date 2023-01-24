package function.annotation.geneanno;

import function.annotation.base.GeneManager;
import function.external.gevir.GeVIRManager;
import function.external.gnomad.GnomADGene;
import function.external.gnomad.GnomADManager;
import function.external.knownvar.KnownVarManager;
import function.external.knownvar.KnownVarOutput;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.synrvis.SynRvisManager;
import global.Data;
import java.util.Collections;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class GeneAnnoOutput {

    private String geneName;
    private KnownVarOutput knownVarOutput;
    private GnomADGene gnomADGene;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Gene Name");
        sj.add("UpToDate Gene Name");
        sj.add("All Gene Symbols");
        sj.add(KnownVarManager.getGeneHeader());
        sj.add(GnomADManager.getGeneMetricsHeader());
        sj.add(RvisManager.getHeader());
        sj.add(GeVIRManager.getHeader());
        sj.add(SynRvisManager.getHeader());
        sj.add(MgiManager.getHeader());
        
        return sj.toString();
    }

    public GeneAnnoOutput(String geneName) {
        this.geneName = geneName;

        knownVarOutput = new KnownVarOutput();
        knownVarOutput.init(geneName);

        gnomADGene = GnomADManager.getGnomADGene(geneName);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(geneName);
        sj.add(GeneManager.getUpToDateGene(geneName));
        sj.add(GeneManager.getAllGeneSymbol(Collections.singleton(geneName)));
        sj.merge(knownVarOutput.getStringJoiner4Gene());
        sj.merge(getGeneMetrics());
        sj.add(RvisManager.getLine(geneName));
        sj.add(GeVIRManager.getLine(geneName));
        sj.add(SynRvisManager.getLine(geneName));
        sj.add(MgiManager.getLine(geneName));
        

        return sj;
    }

    public StringJoiner getGeneMetrics() {
        if (gnomADGene == null) {
            StringJoiner sj = new StringJoiner(",");
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            sj.add(Data.STRING_NA);
            return sj;
        } else {
            return gnomADGene.getGeneMetricsSJ();
        }
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
