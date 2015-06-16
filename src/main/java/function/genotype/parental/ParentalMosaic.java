package function.genotype.parental;

import function.genotype.base.AnalysisBase4CalledVar;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import utils.CommandValue;
import utils.ErrorManager;
import utils.FormatManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.math3.stat.inference.AlternativeHypothesis;
import org.apache.commons.math3.stat.inference.BinomialTest;

/**
 *
 * @author nick
 */
public class ParentalMosaic extends AnalysisBase4CalledVar {

    BufferedWriter bwOutput = null;
    BinomialTest BT = new BinomialTest();
    double BT_p = 0.5;

    final String outputFilePath = CommandValue.outputPath + "parental.mosaic.csv";

    @Override
    public void initOutput() {
        try {
            bwOutput = new BufferedWriter(new FileWriter(outputFilePath));
            bwOutput.write(ParentalOutput.title);
            bwOutput.newLine();
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
            bwOutput.flush();
            bwOutput.close();
        } catch (Exception ex) {
            ErrorManager.send(ex);
        }
    }

    @Override
    public void doAfterCloseOutput() {
    }

    @Override
    public void beforeProcessDatabaseData() {
        FamilyManager.init();
    }

    @Override
    public void afterProcessDatabaseData() {
    }

    @Override
    public void processVariant(CalledVariant calledVar) {
        try {
            ParentalOutput output = new ParentalOutput(calledVar);
            output.countSampleGenoCov();
            output.calculate();

            if (output.isValid()) {

                for (Family family : FamilyManager.getList()) {

                    for (Sample child : family.getChildList()) {

                        int childGeno = calledVar.getGenotype(child.getIndex());

                        if (output.isQualifiedGeno(childGeno)
                                && isChildValid(childGeno, child, calledVar)) {

                            for (Sample parent : family.getParentList()) {

                                int parentGeno = calledVar.getGenotype(parent.getIndex());

                                if (output.isQualifiedGeno(parentGeno)
                                        && isParentValid(parent, calledVar)) {
                                    doOutput(output.getString(child, childGeno, parent, parentGeno));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ErrorManager.send(e);
        }
    }

    private boolean isChildValid(int childGeno, Sample child, CalledVariant calledVar) {
        // --child-qd
        float childQD = calledVar.getQualByDepthQD(child.getId());

        if (!isChildQdValid(childQD)) {
            return false;
        }

//         --child-het-percent-alt-read
        if (childGeno == 1) {
            int readsAlt = calledVar.getReadsAlt(child.getId());
            int gatkFilteredCoverage = calledVar.getGatkFilteredCoverage(child.getId());

            double percAltRead = FormatManager.devide(readsAlt, gatkFilteredCoverage);

            if (!isChildHetPercentAltReadValid(percAltRead)) {
                return false;
            }
        }

        // --child-binomial
        double childBinomial = getBinomial(calledVar.getReadsAlt(child.getId()),
                calledVar.getReadsRef(child.getId()));

        if (!isBinomialValid(childBinomial, CommandValue.childBinomial)) {
            return false;
        }

        return true;
    }

    private boolean isParentValid(Sample parent, CalledVariant calledVar) {
        // --parent-binomial
        double parentBinomial = getBinomial(calledVar.getReadsAlt(parent.getId()),
                calledVar.getReadsRef(parent.getId()));

        if (!isBinomialValid(parentBinomial, CommandValue.parentBinomial)) {
            return false;
        }

        return true;
    }

    private boolean isChildQdValid(float value) {
        if (CommandValue.childQD == Data.NO_FILTER) {
            return true;
        }

        if (value == Data.NA) {
            if (CommandValue.isQcMissingIncluded) {
                return true;
            }
        } else {
            if (value >= CommandValue.childQD) {
                return true;
            }
        }

        return false;
    }

    private boolean isChildHetPercentAltReadValid(double value) {
        if (CommandValue.childHetPercentAltRead == null) {
            return true;
        }

        if (value != Data.NA) {
            if (value >= CommandValue.childHetPercentAltRead[0]
                    && value <= CommandValue.childHetPercentAltRead[1]) {
                return true;
            }
        }

        return false;
    }

    private boolean isBinomialValid(double value, double filterValue) {
        if (filterValue == Data.NO_FILTER) {
            return true;
        }

        if (value >= filterValue) {
            return true;
        }

        return false;
    }

    private double getBinomial(int alt, int ref) {
        return BT.binomialTest(alt + ref, alt, BT_p,
                AlternativeHypothesis.LESS_THAN);
    }

    private void doOutput(String str) throws IOException {
        bwOutput.write(str);
        bwOutput.newLine();
    }

    @Override
    public String toString() {
        return "It is running a parental mosaic function...";
    }
}
