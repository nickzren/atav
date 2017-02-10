import subprocess
import os
import argparse
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt
import seaborn as sns

## Aesthetics for matplotlib
sns.set_style("darkgrid")

def add_outputprefix(outdir,f):
    """ Add output directory path to a file
    outdir : string ; path to the output dir
    f : string ; output file name 

    returns : str; path to the full outputdir
    """
    
    return os.path.join(outdir,f)

def create_parfile(parfile,args):
    """ Creates a parameter file for Eigenstrat
    parfile : string ; full path to the paramfile
    """

    with open(parfile,'w') as IN:
        print >> IN,"genotypename: {0}".format(args.genofile)
        print >> IN,"snpname: {0}".format(args.snpfile)
        print >> IN,"indivname: {0}".format(args.indivfile)
        print >> IN,"evecoutname: {0}".format(add_outputprefix(args.outputdir,args.evecout))
        print >> IN,"evaloutname: {0}".format(add_outputprefix(args.outputdir,args.evalout))
        print >> IN,"outlieroutname: {0}".format(add_outputprefix(args.outputdir,args.outlierout))
        print >> IN,"numoutlieriter: {0}".format(args.numiter)
        print >> IN,"numoutlierevec: {0}".format(args.numevec)
        print >> IN,"outliersigmathresh: {0}".format(args.sigma)
        print >> IN,"usenorm: {0}".format(args.usenorm)
        print >> IN,"familynames: NO"
        
def run_smartpca(parfile,logfile):
    """ Run smartpca
    parfile -> string ; path to the parameter file for smartpca
    logfile -> string ; path to the log file to store smartpca stdout
    """

    smartpca_loc = "/nfs/seqscratch11/rp2801/eigenstrat_stuff/EIG-6.1.4/bin/smartpca"
    logfile = add_outputprefix(args.outputdir,logfile)
    cmd = "{0} -p {1} > {2}".format(smartpca_loc,parfile,logfile)
    proc = subprocess.Popen(cmd,shell=True)
    proc.wait()
    if proc.returncode : ## Non zero returncode
        raise subprocess.CalledProcessError(
            proc.returncode,cmd)

def plot_pcs(args,suffix):
    """Plot the principal components 
    args : a class whose members are the command line arguments parsed by argparse 
    suffix : suffix for the name of the PC plots
    """

    ## Parse the output evec file and store the PCs
    i = 0
    pc1_case = []
    pc2_case = []
    pc3_case = []
    pc1_control = []
    pc2_control = []
    pc3_control = []
    with open(os.path.join(args.outputdir,args.evecout)) as IN:
        for line in IN:
            line=line.strip('\n')
            line=line.strip(' ')
            if i > 1:
                if line.split()[-1] == 'Case':
                    pc1_case.append(float(line.split()[1]))
                    pc2_case.append(float(line.split()[2]))
                    pc3_case.append(float(line.split()[3]))
                elif line.split()[-1] == 'Control':
                    pc1_control.append(float(line.split()[1]))
                    pc2_control.append(float(line.split()[2]))
                    pc3_control.append(float(line.split()[3]))
                else:
                    raise Exception("Incorrect phenotype labels, currently plotting is only supported for Case/Control status")
            i+=1
            
    ## Do the plotting             
    plt.figure(figsize=(25,15))
    plt.subplot(311)
    plt.scatter(pc1_control,pc2_control,facecolors='none',edgecolors='b')
    plt.scatter(pc1_case,pc2_case,facecolors='none',edgecolors='r')
    plt.title('PC1 vs PC2 Plot')
    plt.xlabel('PC1')
    plt.ylabel('PC2')
    plt.legend(['Control','Case'],loc='upper right')    

    plt.subplot(312)
    plt.scatter(pc2_control,pc3_control,facecolors='none',edgecolors='b')
    plt.scatter(pc2_case,pc3_case,facecolors='none',edgecolors='r')
    plt.title('PC2 vs PC3 Plot')
    plt.xlabel('PC2')
    plt.ylabel('PC3')
    plt.legend(['Control','Case'],loc='upper right')    
    
    plt.subplot(313)
    plt.scatter(pc1_control,pc3_control,facecolors='none',edgecolors='b')
    plt.scatter(pc1_case,pc3_case,facecolors='none',edgecolors='r')
    plt.title('PC1 vs PC3 Plot')
    plt.xlabel('PC1')
    plt.ylabel('PC3')
    plt.legend(['Control','Case'],loc='upper right')
    plt.tight_layout()
    plt.savefig(os.path.join(args.outputdir,'%s.pcplots.pdf'%suffix))


def plot_eigenvals_pcs(args,suffix):
    """ Plot eigenvals vs PC,useful for selecting
    the number of principal components    
    
    args : argparse parameter object
    suffix : str ; a identifier to add to plot title 

    returns : nothing
    """

    found = 0
    i = 0
    pc = []
    eigenval = []
    ## Parse the log file to get the values
    with open(os.path.join(args.outputdir,args.log),'r') as LOG:
        for line in LOG:
            line = line.strip('\n').strip(' ')
            if found == 1 and i < 100:
                contents = line.split()
                pc.append(int(contents[0]))
                eigenval.append(float(contents[1]))
                i+=1
            if line[0:2] == '#N': ## Find the begining of the data points
                found = 1

    plt.figure(figsize=(15,10))
    plt.scatter(pc,eigenval)
    plt.title('Eigenvalues corresponding to the first 100 Principal Components')
    plt.xlabel('Principal Components')
    plt.ylabel('Eigenvalues')
    plt.xlim([0,100])
    plt.savefig(os.path.join(args.outputdir,'%s.eigenvalvspcs.pdf'%suffix))

def get_samples(outlier_removed_file):
    """ Read samples from the outlier removed file

    outlier_removed_file : str ; the outlier removed file 
    
    returns : list ; the sample names of the outliers 
    """

    outlier_samples = []
    with open(outlier_removed_file,'r') as IN:
        for line in IN:
            line = line.strip('\n')
            sample = line.split(' ')[2].split(':')[0]
            outlier_samples.append(sample)
            
    return outlier_samples

def create_samplefile(samplefile,outlier_samples,outputfile):
    """ Iterate over the famfile and output all the outlier samples to a new samplefile

    samplefile : str ; atav's sample file
    outlier_samples : list  ; list of outlier samples
    outputfile : the output sample file name 

    returns : does not return anything
    """

    with open(samplefile,'r') as IN, open(outputfile,'w') as OUT:
        for line in IN:
            line = line.strip('\n')
            sample_name = line.split('\t')[1]
            if sample_name not in outlier_samples:
                print >> OUT,'\t'.join(line.split('\t'))

def main(args):

    ## Create the output directory if it does not exist
    if not os.path.exists(args.outputdir):
        os.makedirs(args.outputdir)
    
    ## Create the parameter file
    parfile = os.path.join(args.outputdir,'eigenstrat_outlier_removed.smartpca.par')
    create_parfile(parfile,args)
    
    ## Run smartpca
    run_smartpca(parfile,args.log)
    ## Plot principal components 
    plot_pcs(args,'eigenstrat_outlier_removed')
    plot_eigenvals_pcs(args,'eigenstrat_outlier_removed')
    
    ## Output a samplefile with outlier's removed
    if args.prune:
        outlier_samples = get_samples(add_outputprefix(args.outputdir,args.outlierout))
        create_samplefile(args.inputsamples,outlier_samples,add_outputprefix(args.outputdir,'eigenstrat_pruned_sample.txt'))

    ## Repeat the above steps with outlier removal iterations set to 0
    ## to get a plot without any outliers removed
    ## Change output file names 
    args.numiter = 0
    args.evecout= 'eigenstrat_outlier_included.evec'
    args.evalout = 'eigenstrat_outlier_included.eval'
    args.log = 'eigenstrat_outlier_included.log'
    args.outlierout = 'eigenstrat_outlier_included.txt'
    parfile = os.path.join(args.outputdir,'eigenstrat_outlier_included.smartpca.par')
    create_parfile(parfile,args)
    ## Run smartpca
    run_smartpca(parfile,args.log)
    ## Plot principal components 
    plot_pcs(args,'eigenstrat_outlier_included')
    plot_eigenvals_pcs(args,'eigenstrat_outlier_included')
    
if __name__ == "__main__":
    parser = argparse.ArgumentParser("Wrapper for calling Eigenstrat",description="Creates a parameter file and calls Eigenstrat twice, once with outlier removal iterations set to 5(default) and once without any outlier removal iterations")
    parser.add_argument("-genotypefile","--genotypefile",dest="genofile",help="A ped file is sufficient here",required=True)
    parser.add_argument("-snpfile","--snpfile",dest="snpfile",help="A map file is sufficient here",required=True)
    parser.add_argument("-indivfile","--indivfile",dest="indivfile",help="A ped file is sufficient here",required=True)
    parser.add_argument("-evecoutname","--evecoutname",dest="evecout",help="The output file with the eigenvectors",required=False,default="eigenstrat_outlier_removed.evec")
    parser.add_argument("-evaloutname","--evaloutname",dest="evalout",help="The output file with the eigenvalues",required=False,default="eigenstrat_outlier_removed.eval")
    parser.add_argument("-outlieroutname","--outlieroutname",dest="outlierout",help="The output file containing the outliers which were removed",required=False,default="eigenstrat_outlier_removed.txt")
    parser.add_argument("-numoutevec","--numoutevec",dest="numevec",help="The number of eigenvectors to output",default=10,type=int)
    parser.add_argument("-numoutlieriter","--numoutlieriter",dest="numiter",help="The number of outlier removal iterations",default=5,type=int)
    parser.add_argument("-outliersigmathresh","--outliersigmathresh",dest="sigma",help="The number of standard deviations which an individual must exceed along a principal component to be removed as an outlier",default=6,type=int)
    parser.add_argument("-usenorm","--usenorm",dest="usenorm",help="Whether to normalize each SNP by a quanitity related to allele freq. , set to NO for microsatellite data, default is YES",default="YES")    
    parser.add_argument('-outputdir','--outputdir',dest="outputdir",help="Directory to save the output files(will be created if it does not exist)",required=True)
    parser.add_argument('-logfile','--logfile',dest="log",help="File to store smartpca output",required=False,default="eigenstrat_outlier_removed.log")
    parser.add_argument('--prune-sample','--prune',dest="prune",action='store_true')
    parser.add_argument('--sample','--sample',dest="inputsamples",help="The input atav samplefile used for this run",required=False)
    args = parser.parse_args()
    
    main(args)
