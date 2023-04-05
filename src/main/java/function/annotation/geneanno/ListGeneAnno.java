package function.annotation.geneanno;

import function.annotation.base.AnalysisBase4AnnotatedGene;
import function.external.gevir.GeVIRManager;
import function.external.gnomad.GnomADManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.synrvis.SynRvisManager;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *
 * @author nick
 */
public class ListGeneAnno extends AnalysisBase4AnnotatedGene {

    BufferedWriter bwAnnotations = null;
    final String annotationsFilePath = CommonCommand.outputPath + "gene_annotations.csv";

    @Override
    public void initOutput() {
        try {
            KnownVarManager.init4Gene();
            GnomADManager.initGeneMap();
            RvisManager.initRvisMap();
            GeVIRManager.initGeVIRMap();
            SynRvisManager.initSynRvisMap();
            MgiManager.initMgiMap();
            
            bwAnnotations = new BufferedWriter(new FileWriter(annotationsFilePath));
            bwAnnotations.write(GeneAnnoOutput.getHeader());
            bwAnnotations.newLine();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void closeOutput() {
        try {
            bwAnnotations.flush();
            bwAnnotations.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
    }

    @Override
    public void afterProcessDatabaseData() {
    }
    
    @Override
    public void processGene(String gene) {
        try {
            GeneAnnoOutput output = new GeneAnnoOutput(gene);
            bwAnnotations.write(output.toString());
            bwAnnotations.newLine();
        }catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    @Override
    public String toString() {
        return "Start running list gene annotation function";
    }
}
