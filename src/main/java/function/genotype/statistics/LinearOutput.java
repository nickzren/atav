package function.genotype.statistics;

import function.external.evs.EvsManager;
import function.genotype.base.SampleManager;
import function.external.exac.ExacManager;
import function.annotation.base.GeneManager;
import function.external.genomes.GenomesManager;
import function.external.gerp.GerpManager;
import function.external.kaviar.KaviarManager;
import function.external.knownvar.KnownVarManager;
import function.external.mgi.MgiManager;
import function.external.rvis.RvisManager;
import function.external.subrvis.SubRvisManager;
import function.genotype.base.CalledVariant;
import function.genotype.base.Sample;
import global.Data;
import global.Index;
import utils.FormatManager;
import utils.LogManager;
import org.apache.commons.math3.distribution.TDistribution;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import utils.MathManager;

/**
 *
 * @author nick
 */
public class LinearOutput extends StatisticOutput {

    double beta1 = 0;

    public static String getTitle() {
        return "Variant ID,"
                + "Variant Type,"
                + "Rs Number,"
                + "Ref Allele,"
                + "Alt Allele,"
                + "CADD Score Phred,"
                + GerpManager.getTitle()
                + "Is Minor Ref,"
                + "Major Hom Ctrl,"
                + "Het Ctrl,"
                + "Minor Hom Ctrl,"
                + "Minor Hom Ctrl Freq,"
                + "Het Ctrl Freq,"
                + "Missing Ctrl,"
                + "QC Fail Ctrl,"
                + "Ctrl Maf,"
                + "Ctrl HWE_P,"
                + "P value,"
                + "Beta1,"
                + EvsManager.getTitle()
                + "Polyphen Humdiv Score,"
                + "Polyphen Humdiv Prediction,"
                + "Polyphen Humvar Score,"
                + "Polyphen Humvar Prediction,"
                + "Function,"
                + "Gene Name,"
                + "Artifacts in Gene,"
                + "Codon Change,"
                + "Gene Transcript (AA Change),"
                + ExacManager.getTitle()
                + KaviarManager.getTitle()
                + KnownVarManager.getTitle()
                + RvisManager.getTitle()
                + SubRvisManager.getTitle()
                + GenomesManager.getTitle()
                + MgiManager.getTitle();
    }

    public LinearOutput(CalledVariant c) {
        super(c);
    }

    public boolean isValid(String model) {
        if (model.equals("recessive")) {
            if (!isRecessive()) {
                return false;
            }
        }

        if (isValid()) {
            return true;
        }

        return false;
    }

    public void doMVRegression() { //for genotypic model
        OLSMultipleLinearRegression mr = new OLSMultipleLinearRegression();
        mr.setNoIntercept(false);

        int max_size = SampleManager.getListSize();
        int nvars = 1;
        int ncols = nvars + 1;
        double[] data = new double[max_size * ncols];
        int nobs = 0;
        SimpleRegression sr = new SimpleRegression(true);
        for (Sample sample : SampleManager.getList()) {
            int geno = calledVar.getGenotype(sample.getIndex());
            if (geno >= 0) {
                int base = nobs * ncols;
                double y = sample.getQuantitativeTrait();
                data[base] = y;
                if (isMinorRef) {
                    if (geno == Index.REF) {
                        sr.addData(2, y);
                        data[base + 1] = 2; //data[base+2] = 0;
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                        data[base + 1] = 1; //data[base+2] = 1;
                    } else if (geno == Index.HOM) {
                        sr.addData(0, y);
                        data[base + 1] = 0; //data[base+2] = 0;
                    } else if (geno == Index.HOM_MALE) {
                        sr.addData(0, y);
                        data[base + 1] = 0; //data[base+2] = 0;
                    } else if (geno == Index.REF_MALE) {
                        sr.addData(1, y);
                        data[base + 1] = 1; //data[base+2] = 0;
                    }
                } else if (geno == Index.REF) {
                    sr.addData(0, y);
                    data[base + 1] = 0; //data[base+2] = 0;
                } else if (geno == Index.HET) {
                    sr.addData(1, y);
                    data[base + 1] = 1; //data[base+2] = 1;
                } else if (geno == Index.HOM) {
                    sr.addData(2, y);
                    data[base + 1] = 2; //data[base+2] = 0;
                } else if (geno == Index.HOM_MALE) {
                    sr.addData(1, y);
                    data[base + 1] = 1; //data[base+2] = 0;
                } else if (geno == Index.REF_MALE) {
                    sr.addData(0, y);
                    data[base + 1] = 0; //data[base+2] = 0;
                }
                nobs++;
            }
        }
        if (nobs > ncols) {
            mr.newSampleData(data, nobs, nvars);
            TDistribution td = new TDistribution(nobs - nvars);
            double[] parameters = mr.estimateRegressionParameters();
            double[] stds = mr.estimateRegressionParametersStandardErrors();

            beta1 = parameters[1];
            double t = MathManager.abs(beta1, stds[1]);
            pValue = 2 * td.cumulativeProbability(t);

        } else {
            pValue = Data.NA;
            beta1 = Data.NA;
        }
        pValue = sr.getSignificance();
        if (Double.isNaN(pValue)) {
            pValue = Data.NA;
        }
        beta1 = sr.getSlope();
        if (Double.isNaN(beta1)) {
            beta1 = Data.NA;
        }
    }

    public void doRegression(String model) {
        //if (model.equals("genotypic")) {
        //    return;
        //}
        SimpleRegression sr = new SimpleRegression(true);
        for (Sample sample : SampleManager.getList()) {
            int geno = calledVar.getGenotype(sample.getIndex());
            if (geno >= 0) {
                double y = sample.getQuantitativeTrait();
                if (model.equals("allelic")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            sr.addData(1, y);
                            sr.addData(1, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                            sr.addData(0, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(0, y);
                            sr.addData(0, y);
                        }
                    } else if (geno == Index.REF) {
                        sr.addData(0, y);
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                        sr.addData(0, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                        sr.addData(1, y);
                    }
                } else if (model.equals("dominant")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            sr.addData(1, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM_MALE) {
                            sr.addData(0, y);
                        } else if (geno == Index.REF_MALE) {
                            sr.addData(1, y);
                        }
                    } else if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM_MALE) {
                        sr.addData(1, y);
                    } else if (geno == Index.REF_MALE) {
                        sr.addData(0, y);
                    }
                } else if (model.equals("recessive")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            sr.addData(1, y);
                        } else if (geno == Index.HET) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM_MALE) {
                            sr.addData(0, y);
                        } else if (geno == Index.REF_MALE) {
                            sr.addData(1, y);
                        }
                    } else if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(0, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM_MALE) {
                        sr.addData(1, y);
                    } else if (geno == Index.REF_MALE) {
                        sr.addData(0, y);
                    }
                } else if (model.equals("genotypic")) { // not complete yet, to be finished a bit later
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            sr.addData(2, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM_MALE) {
                            sr.addData(0, y);
                        } else if (geno == Index.REF_MALE) {
                            sr.addData(1, y);
                        }
                    } else if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(2, y);
                    } else if (geno == Index.HOM_MALE) {
                        sr.addData(1, y);
                    } else if (geno == Index.REF_MALE) {
                        sr.addData(0, y);
                    }
                } else if (model.equals("additive")) {
                    if (isMinorRef) {
                        if (geno == Index.REF) {
                            sr.addData(2, y);
                        } else if (geno == Index.HET) {
                            sr.addData(1, y);
                        } else if (geno == Index.HOM) {
                            sr.addData(0, y);
                        } else if (geno == Index.HOM_MALE) {
                            sr.addData(0, y);
                        } else if (geno == Index.REF_MALE) {
                            sr.addData(1, y);
                        }
                    } else if (geno == Index.REF) {
                        sr.addData(0, y);
                    } else if (geno == Index.HET) {
                        sr.addData(1, y);
                    } else if (geno == Index.HOM) {
                        sr.addData(2, y);
                    } else if (geno == Index.HOM_MALE) {
                        sr.addData(1, y);
                    } else if (geno == Index.REF_MALE) {
                        sr.addData(0, y);
                    }
                } else {
                    LogManager.writeAndPrint("model is not recognized");
                }
            }
        }
        pValue = sr.getSignificance();
        if (Double.isNaN(pValue)) {
            pValue = Data.NA;
        }
        beta1 = sr.getSlope();
        if (Double.isNaN(beta1)) {
            beta1 = Data.NA;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(calledVar.getVariantIdStr()).append(",");
        sb.append(calledVar.getType()).append(",");
        sb.append(calledVar.getRsNumber()).append(",");
        sb.append(calledVar.getRefAllele()).append(",");
        sb.append(calledVar.getAllele()).append(",");
        sb.append(FormatManager.getDouble(calledVar.getCscore())).append(",");
        sb.append(calledVar.getGerpScore());
        sb.append(isMinorRef).append(",");
        sb.append(majorHomCount[Index.CTRL]).append(",");
        sb.append(genoCount[Index.HET][Index.CTRL]).append(",");
        sb.append(minorHomCount[Index.CTRL]).append(",");
        sb.append(FormatManager.getDouble(minorHomFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hetFreq[Index.CTRL])).append(",");
        sb.append(genoCount[Index.MISSING][Index.CTRL]).append(",");
        sb.append(calledVar.getQcFailSample(Index.CTRL)).append(",");
        sb.append(FormatManager.getDouble(minorAlleleFreq[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(hweP[Index.CTRL])).append(",");
        sb.append(FormatManager.getDouble(pValue)).append(",");
        sb.append(FormatManager.getDouble(beta1)).append(",");
        sb.append(calledVar.getEvsStr());
        sb.append(calledVar.getPolyphenHumdivScore()).append(",");
        sb.append(calledVar.getPolyphenHumdivPrediction()).append(",");
        sb.append(calledVar.getPolyphenHumvarScore()).append(",");
        sb.append(calledVar.getPolyphenHumvarPrediction()).append(",");
        sb.append(calledVar.getFunction()).append(",");
        sb.append("'").append(calledVar.getGeneName()).append("'").append(",");
        sb.append(FormatManager.getInteger(GeneManager.getGeneArtifacts(calledVar.getGeneName()))).append(",");
        sb.append(calledVar.getCodonChange()).append(",");
        sb.append(calledVar.getTranscriptSet()).append(",");
        sb.append(calledVar.getExacStr());
        sb.append(calledVar.getKaviarStr());
        sb.append(calledVar.getKnownVarStr());
        sb.append(calledVar.getRvis());
        sb.append(calledVar.getSubRvis());
        sb.append(calledVar.get1000Genomes());
        sb.append(calledVar.getMgi());

        return sb.toString();
    }
}
