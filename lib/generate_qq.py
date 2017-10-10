"""
Reads collapsing matrix and summary file from summarize.py and performs
FET on all genes, along with permutation based QQ plot
"""

from functools import partial
from operator import le, lt
from scipy import stats
import argparse
import ctypes
import matplotlib
matplotlib.use('pdf')
import matplotlib.pyplot as plt
import multiprocessing as mp
import numpy as np
import random
import seaborn as sns
import sys

sns.set_style('darkgrid')

def valid_numerical_argument(
    arg, arg_name, arg_type=int, min_value=0, max_value=sys.maxint,
    left_op=lt, right_op=le):
    """
    Confirm that the specified value is valid in the range
    (minimum_value, maximum_value] (by default)
    :param arg: the value to be tested
    :param arg_name: the name of the parameter
    :param arg_type: the type of the parameter, e.g. int or float
    :param min_value: the minimum value for the parameter, exclusive
    :param max_value: the maximum value for the parameter, inclusive
    :param left_op: the operator for testing left_op(min_value, value)
    :param right_op: the operator testing right_op(value, max_value)
    :return: arg_type(arg) if arg is valid
    """
    try:
        value = arg_type(arg)
        if left_op(min_value, value) and right_op(value, max_value):
            return value
        else:
            raise argparse.ArgumentTypeError(
                "{arg_name} ({arg}) is not in the range "
                "{left_endpoint}{min_value}, {max_value}{right_endpoint}".format(
                    arg_name=arg_name, arg=arg, min_value=min_value,
                    max_value=max_value, left_endpoint="(" if left_op == lt else "[",
                    right_endpoint="]" if right_op == le else ")"))
    except TypeError:
        raise argparse.ArgumentTypeError(
            "{arg_name} ({arg}) is not a valid {arg_type}".format(
                arg_name=arg_name, arg=arg, arg_type=arg_type.__name__))

def precalc(pair, num_case, num_ctrl):
    """
    Calculate FET p-value for pair of qual case + qual ctrl
    :param pair: (tuple) number of qualified cases and controls
    :param num_case: (int) number of case samples
    :param num_ctrl: (int) number of control samples
    """
    odds, pval = stats.fisher_exact([[pair[0], num_case - pair[0]],
                                     [pair[1], num_ctrl - pair[1]]])
    return (pair[0], pair[1], pval)

def shared_mem(result):
    """
    Populate lookup table with pre-calculated p-values
    :param result: (tuple) num. qual. cases, num. qual ctrls, associated FET p-value
    """
    lookup[result[0],result[1]] = result[2]

def permute(counter, statuses):
    """
    Permute affection statuses and look up p-vals for this configuration
    :param counter: (int) iteration counter for multiprocessing to keep track
    :param statuses: (list) randomized list of case/control statuses
    """
    # randomize the statuses
    perm_statuses = np.array(random.sample(statuses, len(statuses)))
    case_indices = np.where(perm_statuses == '2')[0]
    ctrl_indices = np.where(perm_statuses == '1')[0]
    # make contingency table and calculate pvals for each gene
    pvals = []
    for row in range(col_matrix.shape[0]):
        q_case = np.where(col_matrix[row,case_indices] == 1)[0].shape[0]
        q_ctrl = np.where(col_matrix[row,ctrl_indices] == 1)[0].shape[0]
        # fisher exact test
        # if number of qualified samples is in the lookup table, use that
        if q_case + 1 <= lookup.shape[0] and q_ctrl + 1 <= lookup.shape[1]:
            pvalue = lookup[q_case, q_ctrl]
        # otherwise just do the FET
        else:
            odds, pvalue = stats.fisher_exact([[q_case, len(case_indices) - q_case],
                                               [q_ctrl, len(ctrl_indices) - q_ctrl]])
        pvals.append(pvalue)
    pvals = sorted(pvals)
    return pvals

def read_summary_file(summary_file):
    """
    Read in partial summary and add gene info to dictionary
    :param summary_file: (string) name of partial summary file
    """
    genes = {}
    case_qual = []
    ctrl_qual = []
    with open(summary_file, 'r') as infile:
        header = infile.readline().strip().split(',')
        gene_index = header.index('Gene Name')
        qcase_index = header.index('Qualified Case')
        uqcase_index = header.index('Unqualified Case')
        qctrl_index = header.index('Qualified Ctrl')
        uqctrl_index = header.index('Unqualified Ctrl')
        # read first line to determine number of cases and ctrls
        first = infile.readline().strip().split(',')
        genes[first[gene_index]] = [x for x in first[2:]]
        ncase = int(first[qcase_index]) + int(first[uqcase_index])
        nctrl = int(first[qctrl_index]) + int(first[uqctrl_index])
        case_qual.append(int(first[qcase_index]))
        ctrl_qual.append(int(first[qctrl_index]))
        for line in infile:
            line = line.strip().split(',')
            genes[line[gene_index]] = [x for x in line[2:]]
            case_qual.append(int(line[qcase_index]))
            ctrl_qual.append(int(line[qctrl_index]))
    return (genes, ncase, nctrl, case_qual, ctrl_qual)

def read_matrix_file(matrix_file, ngenes):
    """
    Read in matrix file
    :param matrix_file: (string) name of matrix file
    :param ngenes: (int) number of genes (or general collapsing units)
    """
    with open(matrix_file, 'r') as infile:
        samps = infile.readline().strip().split('\t')
        nsamps = len(samps) - 1
        col_matrix = np.zeros((ngenes, nsamps))
        for i, line in enumerate(infile):
            line = line.strip().split('\t')
            col_matrix[i,:] = line[1:]
    return col_matrix

def plot_qq(qq_file, exp, obs, lower, upper, sorted_genes):
    """
    Create QQ-plot of observed and expected FET p-values, with 2.5%ile and 97.5%ile
        bounds.
    Lambda = slope of regression on chi-sq transformed obs and exp p-values, after
         removal of p-values of 1 and genome-wide sig. p-values
    :param qq_file: (string) name of QQ-plot file to be created
    :param exp: (np array) expected p-values
    :param obs: (np array) observed p-values
    :param lower: (np array) 2.5%ile exp p-values
    :param upper: (np array) 97.5%ile exp p-values
    :param sorted_genes: (list) genes sorted by descending rank
    """
    # change p-values > 1 to 1
    exp[exp > 1] = 1
    obs[obs > 1] = 1

    # calculate lambda by regression method
    # first remove p-values of 1 and < genome-wide significance
    gws = 0.05 / len(obs)
    reg_exp = exp[(obs > gws) & (exp > gws) & (exp < 1) & (obs < 1)]
    reg_obs = obs[(obs > gws) & (exp > gws) & (exp < 1) & (obs < 1)]
    reg_exp = stats.chi2.ppf(1-reg_exp, 1)
    reg_obs = stats.chi2.ppf(1-reg_obs, 1)
    # for lstsq, explanatory vars must be in column form
    reg_exp = reg_exp[:,np.newaxis]
    # this least squares regression forces intercept to be 0
    slope = np.linalg.lstsq(reg_exp, reg_obs)[0][0]
    lambda_factor = slope
    lambda_factor = np.around(lambda_factor, 5)

    # transform p-values to log10
    exp = -np.log10(exp)
    obs = -np.log10(obs)
    lower = -np.log10(lower)
    upper = -np.log10(upper)

    # set limits of qq plot axes
    axisMax_obs = np.ceil(max(obs))
    axisMax_exp = np.ceil(max(exp))

    # initialize qqplot with axes and labels
    fig = plt.figure(figsize=(12,12))
    plt.xlim([0, axisMax_exp])
    plt.xlabel('Expected -log10(p)', fontsize=20)
    plt.ylim([0, axisMax_obs])
    plt.ylabel('Observed -log10(p)', fontsize=20)
    plt.title('QQ Plot: Observed vs. expected p-values. Lambda = {l}'.format(
              l = lambda_factor), fontsize=20)

    # change size of axis tick labels
    plt.tick_params(axis='both', which='major', labelsize=12)

    # plot the points, exp on x axis, obs on y axis
    dataAx = fig.add_subplot(1,1,1)
    dataAx.plot(exp, obs, 'r.', label='_nolegend_', markersize=12)

    # plot a diagonal line for comparison
    lineAx = fig.add_subplot(1,1,1)
    lineAx.plot([0,max(axisMax_obs,axisMax_exp)], [0,max(axisMax_obs,axisMax_exp)],
                 'b-', label='_nolegend_')
    uppAx = fig.add_subplot(1,1,1)
    uppAx.plot(exp, upper, 'g-')
    lowAx = fig.add_subplot(1,1,1)
    lowAx.plot(exp, lower, 'y-')
    plt.tight_layout()
    plt.savefig(qq_file)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.ArgumentDefaultsHelpFormatter)
    parser.add_argument('partial_summary_file',
                        help='Specify name of summary file generated by summarize.py')
    parser.add_argument('matrix_file',
                        help='Specify name of matrix file generated by summarize.py')
    parser.add_argument('output', help='Specify prefix for output files')
    parser.add_argument('--nperms',
                        type=partial(valid_numerical_argument, arg_name='nperms',
                                     min_value=0, max_value=1000, arg_type=int),
                        default=1000,
                        help='Specify number of permutations')
    args = parser.parse_args()

    # name the output files
    qq_file = args.output + '.qqplot.pdf'

    # read summary file to get dictionary of gene-level info
    ## num of cases, controls, and maximum number of qualified samples in any gene
    genes, ncase, nctrl, case_qual, ctrl_qual = read_summary_file(
                                         args.partial_summary_file)

    # determine 99th percentile of qualified
    case_qual = np.array(case_qual)
    ctrl_qual = np.array(ctrl_qual)
    qual_sum = np.sort(case_qual + ctrl_qual)
    high_qual = np.percentile(qual_sum, 95, interpolation='higher')

    # calculate number of genes
    ngenes = len(genes)

    # read matrix file to build collapsing matrix
    col_matrix = read_matrix_file(args.matrix_file, ngenes)

    # calculate number of samples
    nsamps = col_matrix.shape[1]

    # create shared memory version of collapsing matrix
    shared_array_base1 = mp.Array(ctypes.c_int, ngenes * nsamps)
    shared_col_matrix = np.ctypeslib.as_array(shared_array_base1.get_obj())
    shared_col_matrix = shared_col_matrix.reshape(ngenes, nsamps)

    # calculate necessary dimensions of FET lookup table
    max_case = min([high_qual, ncase])
    max_ctrl = min([high_qual, nctrl])

    # create shared memory FET lookup table
    shared_array_base2 = mp.Array(ctypes.c_double, (max_case+1)*(max_ctrl+1))
    lookup = np.ctypeslib.as_array(shared_array_base2.get_obj())
    lookup = lookup.reshape(max_case+1, max_ctrl+1)

    # make list of qual case/qual ctrl pairs that need to be calculated for lookup
    pairs = []

    # make list of (qual case, qual ctrl) tuples for FET lookup table
    for i in xrange(max_case + 1):
        for j in xrange(max_ctrl + 1):
            pairs.append((i,j))

    # generate FET p-values for lookup table
    pool = mp.Pool(processes=12)
    fisher_results = pool.map(partial(precalc, num_case=ncase, num_ctrl=nctrl), pairs)
    pool.close()
    pool.join()

    # populate shared memory FET lookup table
    pool = mp.Pool(processes=12)
    pool.map(shared_mem, fisher_results)
    pool.close()
    pool.join()

    # perform permutations to generate expected p-values
    status_l = ['2'] * ncase
    status_l.extend(['1'] * nctrl)
    pool = mp.Pool(processes=12)
    permute_results = pool.map(partial(permute, statuses=status_l), range(args.nperms))
    pool.close()
    pool.join()

    # populate array of permuted p-values
    perm_pvals = np.ones((ngenes, args.nperms))
    for i, pvals in enumerate(permute_results):
        perm_pvals[:,i] = pvals

    # calculate 2.5%ile and 97.5%ile of permuted pvals
    bottom_perc = np.percentile(perm_pvals, 2.5, axis=1)
    top_perc = np.percentile(perm_pvals, 97.5, axis=1)

    # calculate observed p-values
    for gene, v in genes.iteritems():
        qcase = int(v[3])
        qctrl = int(v[6])
        if qcase + 1 <= lookup.shape[0] and qctrl + 1 <= lookup.shape[1]:
            v.append(lookup[qcase, qctrl])
        else:
            v.append(stats.fisher_exact([[qcase, ncase-qcase],[qctrl, nctrl-qctrl]])[1])
    ordered = sorted(genes.items(), key=lambda x:x[1][-1])

    # create sorted gene list
    sorted_genes = [gene for gene, info in ordered]

    # make arrays of ordered observed and expected p-values
    obs_pvals = np.array([x[1][-1] for x in ordered])
    exp_pvals = np.mean(perm_pvals, axis=1)

    # generate qq plot
    plot_qq(qq_file, exp_pvals, obs_pvals, bottom_perc, top_perc, sorted_genes)
