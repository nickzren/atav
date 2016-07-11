package function.genotype.trio;

import function.genotype.base.CalledVariant;
import function.genotype.base.AnalysisBase4CalledVar;
import utils.CommonCommand;
import utils.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import utils.ThirdPartyToolManager;

/**
 *
 * @author nick
 */
public class ListTrioDenovo extends AnalysisBase4CalledVar {
    
    BufferedWriter bwDenovo = null;
    BufferedWriter bwDenovoNoFlag = null;
    final String denovoFilePath = CommonCommand.outputPath + "denovoandhom.csv";
    final String denovoNoFlagFilePath = CommonCommand.outputPath + "denovoandhom_noflag.csv";
    
    @Override
    public void initOutput() {
        try {
            bwDenovo = new BufferedWriter(new FileWriter(denovoFilePath));
            bwDenovo.write(DenovoOutput.getTitle());
            bwDenovo.newLine();
            
            if (TrioCommand.isIncludeNoflag) {
                bwDenovoNoFlag = new BufferedWriter(new FileWriter(denovoNoFlagFilePath));
                bwDenovoNoFlag.write(DenovoOutput.getTitle());
                bwDenovoNoFlag.newLine();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }
    
    @Override
    public void doOutput() {
    }
    
    @Override
    public void closeOutput() {
        try {
            bwDenovo.flush();
            bwDenovo.close();
            
            if (TrioCommand.isIncludeNoflag) {
                bwDenovoNoFlag.flush();
                bwDenovoNoFlag.close();
            }
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }
    
    @Override
    public void doAfterCloseOutput() {
        if (TrioCommand.isRunTier) {
            ThirdPartyToolManager.runTrioDenovoTier(denovoFilePath);
        }
    }
    
    @Override
    public void beforeProcessDatabaseData() {
        TrioManager.init();
    }
    
    @Override
    public void afterProcessDatabaseData() {
    }
    
    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            DenovoOutput output = new DenovoOutput(calledVar);
            
            output.countSampleGeno();
            
            for (Trio trio : TrioManager.getList()) {
                output.initTrioFamilyData(trio);
                
                output.deleteParentGeno(trio);
                
                output.calculate();
                
                if (output.isValid()) {
                    
                    int geno = output.getCalledVariant().getGenotype(trio.getChildIndex());
                    
                    if (output.isQualifiedGeno(geno)) {
                        
                        output.initFlag(trio.getChildId());
                        
                        output.initAvgCov();
                        
                        output.initGenoZygo(trio.getChildIndex());
                        
                        doOutput(output, trio);
                    }
                }
                
                output.addParentGeno(trio);
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }
    
    private void doOutput(DenovoOutput output, Trio trio) throws Exception {
        if (!output.flag.equals("no flag") && !output.flag.equals("unknown")) {
            bwDenovo.write(output.getString(trio));
            bwDenovo.newLine();
        } else if (TrioCommand.isIncludeNoflag) {
            bwDenovoNoFlag.write(output.getString(trio));
            bwDenovoNoFlag.newLine();
        }
    }
    
    @Override
    public String toString() {
        return "It is running a list trio denovo function...";
    }
}
