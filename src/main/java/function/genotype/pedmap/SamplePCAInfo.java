package function.genotype.pedmap;

import java.util.HashSet;
import function.genotype.base.Sample;
import utils.ErrorManager;

/**
 *
 * @author macrina
 */
public class SamplePCAInfo {

    private double[] evec;
    private double[] pcs;
    private boolean outlier = false;
    private Sample sample;
    private boolean toFilter = true;
    
    SamplePCAInfo(Sample sample, int nDim) {
        this.sample = sample;
        this.evec = new double[nDim];
        this.pcs = new double[nDim];
    }

    public void setEvec(int ndim, String[] evecVal) {
        for (int count_i = 0; count_i < ndim; count_i++) {
            evec[count_i] = Double.parseDouble(evecVal[2 + count_i]);
        }
    }

    public byte getPheno() {
        return sample.getPheno();
    }

    public Sample getSample() {
        return sample;
    }

    public void setPcs(int ndim, String[] pcsVal) {
        for (int count_i = 0; count_i < ndim; count_i++) {
            pcs[count_i] = Double.parseDouble(pcsVal[2 + count_i]);
        }
    }

    public boolean isOutlier() {
        return outlier;
    }

    public void setToFilter(boolean toFilter_ip){
        toFilter = toFilter_ip;
    }
    
    public boolean getToFilter(){
        return toFilter;
    }
    
    public void setOutlier(HashSet<String> outlierSet) {
        //check if outlier according to its presence in hashset
        outlier = outlierSet.contains(sample.getName());
    }

    public double getPCAInfoEvec(int dim) {
        try {
            if (dim > evec.length) {
                throw new IllegalArgumentException("dimension " + dim + " accessed is greater than eigvec length " + evec.length);
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        }
        return (evec[dim]);
    }

    public double getPCAInfoPcs(int dim) {
        try {
            if (dim > evec.length) {
                throw new IllegalArgumentException("dimension " + dim + " accessed is greater than eigvec length " + evec.length);
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        }
        return (pcs[dim]);
    }

    public double[] getPCAInfoEvec(int dim1, int dim2) {
        try {
            if (dim1 > evec.length || dim2 > evec.length) {
                throw new IllegalArgumentException("dimensions " + dim1 + ", " + dim2 + " accessed is greater than eigvec length " + evec.length);
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        }
        return (new double[]{evec[dim1], evec[dim2]});
    }

    public double[] getPCAInfoPcs(int dim1, int dim2) {
        try {
            if (dim1 > evec.length || dim2 > evec.length) {
                throw new IllegalArgumentException("dimensions " + dim1 + ", " + dim2 + " accessed is greater than eigvec length " + evec.length);
            }
        } catch (IllegalArgumentException e) {
            ErrorManager.send(e);
        }
        return (new double[]{pcs[dim1], pcs[dim2]});
    }
}
