package function.annotation.geneanno;

import function.annotation.base.GeneManager;
import function.external.knownvar.KnownVarManager;
import function.external.knownvar.KnownVarOutput;
import java.util.Collections;
import java.util.StringJoiner;

/**
 *
 * @author nick
 */
public class GeneAnnoOutput {

    private String geneName;
    private KnownVarOutput knownVarOutput;

    public static String getHeader() {
        StringJoiner sj = new StringJoiner(",");

        sj.add("Gene Name");
        sj.add("UpToDate Gene Name");
        sj.add("All Gene Symbols");
        sj.add(KnownVarManager.getGeneHeader());

        return sj.toString();
    }

    public GeneAnnoOutput(String geneName) {
        this.geneName = geneName;

        knownVarOutput = new KnownVarOutput();
        knownVarOutput.init(geneName);
    }

    public StringJoiner getStringJoiner() {
        StringJoiner sj = new StringJoiner(",");

        sj.add(geneName);
        sj.add(GeneManager.getUpToDateGene(geneName));
        sj.add(GeneManager.getAllGeneSymbol(Collections.singleton(geneName)));
        sj.merge(knownVarOutput.getStringJoiner4Gene());

        return sj;
    }

    @Override
    public String toString() {
        return getStringJoiner().toString();
    }
}
