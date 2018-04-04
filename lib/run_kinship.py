#!/nfs/goldstein/software/python2.7.7/bin/python
"""
Take a KING kinship file, PED/FAM/sample file (for getting phenotype),
and optional coverage summary file in order to generate a list of samples
to remove iteratively as follows:
    1. remove affected that is related to the most other affecteds
        a. break ties by removing the affected distantly related to
            the most other affecteds 
        b. break ties by removing the affected related to the most unaffecteds
        c. break ties by removing the affected distantly related
            to the most unaffecteds
        d. (optional) break ties by removing the affected with the least
            coverage
    2. remove unaffected that is related to the most other unaffecteds
        a. break ties by removing the unaffected that is related to the most
            affecteds
        b. break ties by removing the unaffected that is distantly related to
            the most affecteds
        c. break ties by removing the unaffected that is distantly related to
            the most unaffecteds
        d. (optional) break ties by removing the unaffected with the least
            coverage
    3. remove unaffected that is related the most affecteds
        a. break ties by removing the unaffected that is distantly related to
            the most affecteds
        b. break ties by removing the unaffected that is distantly related to
            the most unaffecteds
        c. (optional) break ties by removing the unaffected with the least
            coverage

KING should be run like: king -b <bed_infile> --kinship --related --degree 3

Written by Brett Copeland <bc2675@cumc.columbia.edu>
"""

import argparse
import os
import sys
from collections import defaultdict, Counter
from random import seed as set_seed
from UndirectedSampleGraph import UndirectedSampleGraph
from CustomFormatter import CustomFormatter

def get_samples_with_min_coverage(coverage_by_sample, sample_list):
    """return a subset of the samples with the min coverage
    """
    samples_min_coverage = defaultdict(list)
    min_coverage = sys.maxint
    for sample in sample_list:
        if coverage_by_sample[sample] <= min_coverage:
            min_coverage = coverage_by_sample[sample]
            samples_min_coverage[min_coverage].append(sample)
    return samples_min_coverage[min_coverage]

def process_ped_file(ped_fh, log_fh, verbose=False):
    """get the affectation status for all samples and return as a dict
    """
    phenotypes = {}
    counter = Counter()
    ped_lines = []
    if verbose:
        log_fh.write("Parsing PED/FAM/ATAV sample file\n")
    for line in ped_fh:
        fields = line.strip().split("\t")
        ped_lines.append(fields)
        phenotype = int(fields[5]) - 1
        assert phenotype in (0, 1)
        phenotypes[fields[1]] = phenotype
        counter[phenotype] += 1
    ped_fh.close()
    if verbose:
        log_fh.write("Done with PED/FAM file: found {} samples\n\n".format(
            len(phenotypes)))
        for phenotype in counter.iterkeys():
            log_fh.write("Phenotype {}: found {} samples\n".format(
                phenotype, counter[phenotype]))
    return phenotypes, ped_lines

def process_coverage_summary_file(coverage_summary_fh, log_fh, verbose=False):
    """get the number of bases covered for all samples and return as a dict
    """
    if verbose:
        log_fh.write("Parsing coverage summary file\n")
    header = coverage_summary_fh.next().strip().split(",")
    sample_idx = header.index("Sample")
    coverage_idx = header.index("Total_Covered_Base")
    coverage_by_sample = {}
    for line in coverage_summary_fh:
        fields = line.strip().split(",")
        coverage_by_sample[fields[sample_idx]] = int(fields[coverage_idx])
    coverage_summary_fh.close()
    if verbose:
        log_fh.write("Done with coverage summary file; found {} entries\n\n".
                     format(len(coverage_by_sample)))
    return coverage_by_sample

def process_kinship_file(
    kinship_files, relatedness_threshold, distant_relatedness_threshold,
    phenotypes, log_fh, verbose=False):
    """process all entries in the kinship files into one of the six defined
    categories and return as a list of graphs
    """
    affecteds_related_graph = UndirectedSampleGraph(
        desc="related affecteds")
    affecteds_unrelated_graph = UndirectedSampleGraph(
        desc="distantly related affecteds")
    mixed_related_graph = UndirectedSampleGraph(
        desc="related mixed affectation samples", reciprocal=False)
    mixed_unrelated_graph = UndirectedSampleGraph(
        desc="distantly related mixed affectation samples", reciprocal=False)
    mixed_affecteds_related_graph = UndirectedSampleGraph(
        desc="related mixed affectation samples", reciprocal=False)
    mixed_affecteds_unrelated_graph = UndirectedSampleGraph(
        desc="distantly related mixed affectation samples", reciprocal=False)
    unaffecteds_related_graph = UndirectedSampleGraph(
        desc="related unaffecteds")
    unaffecteds_unrelated_graph = UndirectedSampleGraph(
        desc="distantly related unaffecteds")
    if verbose:
        log_fh.write("Parsing kinship files\n")
    for fh in kinship_files:
        header = fh.readline().strip().split("\t")
        id1_index = header.index("ID1")
        id2_index = header.index("ID2")
        kinship_index = header.index("Kinship")
        for line in fh:
            fields = line.strip().split("\t")
            sample_one = fields[id1_index]
            sample_two = fields[id2_index]
            kinship_coefficient = float(fields[kinship_index])
            if kinship_coefficient < distant_relatedness_threshold:
                # ignore any records below the minimal threshold
                continue
            related = kinship_coefficient >= relatedness_threshold
            if phenotypes[sample_one] == phenotypes[sample_two]:
                if phenotypes[sample_one] == 1:
                    if related:
                        graph = affecteds_related_graph
                    else:
                        graph = affecteds_unrelated_graph
                else:
                    if related:
                        graph = unaffecteds_related_graph
                    else:
                        graph = unaffecteds_unrelated_graph
            else:
                # switch order of samples if sample_one is affected
                if phenotypes[sample_one] == 1:
                    sample_one, sample_two = sample_two, sample_one
                if related:
                    graph = mixed_related_graph
                    mixed_affecteds_related_graph.add_edge(sample_two, sample_one)
                else:
                    graph = mixed_unrelated_graph
                    mixed_affecteds_unrelated_graph.add_edge(
                        sample_two, sample_one)
            graph.add_edge(sample_one, sample_two)
        fh.close()
    log_fh.write(
        "Done with kinship files; found:\n{} affected relationships\n{} "
        "affected distant relationships\n{} mixed relationships\n{} mixed "
        "distant relationships\n{} unaffected relationships\n{} "
        "unaffected distant relationships\n\n".format(
            affecteds_related_graph.get_total_edges(),
            affecteds_unrelated_graph.get_total_edges(),
            mixed_related_graph.get_total_edges(),
            mixed_unrelated_graph.get_total_edges(),
            unaffecteds_related_graph.get_total_edges(),
            unaffecteds_unrelated_graph.get_total_edges()))
    return (affecteds_related_graph, affecteds_unrelated_graph,
            mixed_related_graph, mixed_unrelated_graph,
            mixed_affecteds_related_graph, mixed_affecteds_unrelated_graph,
            unaffecteds_related_graph, unaffecteds_unrelated_graph)

def remove_samples(graph, tie_break_graphs, log_fh,
                   coverage_by_sample=None, verbose=False):
    """iteratively remove a sample from graph until it has no connections; ties
    are broken by the number of connections a sample has in tie_break_graphs in
    order and coverage_by_sample if specified
    samples are removed and returned as a list
    """
    if verbose:
        log_fh.write("-" * 80 + "\n")
        log_fh.write("Removing samples from {} graph\n".format(graph.desc))
        num_samples_to_start = graph.get_total_samples()
    samples_to_remove = []
    while not graph.empty():
        candidates_to_remove = (
            graph.get_all_most_frequent_samples())
        if len(candidates_to_remove) == 1:
            if verbose:
                log_fh.write(
                    "Removing {} as it alone has the most connections\n".format(
                        candidates_to_remove[0]))
        else:
            for secondary_graph in tie_break_graphs:
                candidates_to_remove = (
                    secondary_graph.get_most_frequent_samples_from_list(
                        candidates_to_remove))
                if len(candidates_to_remove) == 1:
                    if verbose:
                        log_fh.write(
                            "Removing {} as it alone has the most connections "
                            "in the {} graph\n".format(
                                candidates_to_remove[0], secondary_graph.desc))
                    break
        if len(candidates_to_remove) > 1 and coverage_by_sample:
            candidates_to_remove = get_samples_with_min_coverage(
                coverage_by_sample, candidates_to_remove)
            if verbose and len(candidates_to_remove) == 1:
                log_fh.write(
                    "Removing {} as it alone has the least coverage\n".format(
                        candidates_to_remove[0]))
        if len(candidates_to_remove) > 1:
            sample_to_remove = UndirectedSampleGraph.select_random_sample(
                candidates_to_remove)
            if verbose:
                log_fh.write("Tie broken randomly amongst: {} - {}\n".format(
                    candidates_to_remove, sample_to_remove))
        else:
            sample_to_remove = candidates_to_remove[0]
        for g in all_graphs:
            g.remove_sample(sample_to_remove)
        samples_to_remove.append(sample_to_remove)

    if verbose:
        log_fh.write("Done with {} graph; started with {} samples and "
                     "removed {}\n".format(
                         graph.desc, num_samples_to_start,
                         len(samples_to_remove)))
        log_fh.write("-" * 80 + "\n\n")

    return samples_to_remove

def find_samples_to_remove(
    ped_fh, kin0_fh, kin_fh, relatedness_threshold,
    distant_relatedness_threshold, output_fh,
    coverage_summary_fh=None, seed=None, verbose=False):
    if output_fh is sys.stdout:
        log_fh = sys.stderr
    else:
        log_fh = open(os.path.splitext(output_fh.name)[0] + ".log", "w")
    try:
        if seed is not None:
            set_seed(seed)
        phenotypes, ped_lines = process_ped_file(ped_fh, log_fh, verbose)
        global all_graphs
        kinship_files = [kin0_fh, kin_fh]
        all_graphs = process_kinship_file(
            kinship_files, relatedness_threshold, distant_relatedness_threshold,
            phenotypes, log_fh, verbose)
        (affecteds_related_graph, affecteds_unrelated_graph, mixed_related_graph,
         mixed_unrelated_graph, mixed_affecteds_related_graph,
         mixed_affecteds_unrelated_graph, unaffecteds_related_graph,
         unaffecteds_unrelated_graph) = all_graphs
        break_ties_with_coverage = bool(coverage_summary_fh)
        if break_ties_with_coverage:
            coverage_by_sample = process_coverage_summary_file(
                coverage_summary_fh, log_fh, verbose)
        else:
            coverage_by_sample = None

        samples_to_remove = []
        samples_to_remove.extend(
            remove_samples(affecteds_related_graph,
                           [affecteds_unrelated_graph, mixed_affecteds_related_graph,
                            mixed_affecteds_unrelated_graph], log_fh,
                            coverage_by_sample=coverage_by_sample, verbose=verbose))
        samples_to_remove.extend(
            remove_samples(unaffecteds_related_graph,
                           [mixed_related_graph, mixed_unrelated_graph,
                            unaffecteds_unrelated_graph], log_fh,
                           coverage_by_sample=coverage_by_sample, verbose=verbose))
        samples_to_remove.extend(
            remove_samples(mixed_related_graph,
                           [mixed_unrelated_graph, unaffecteds_unrelated_graph],
                           log_fh, coverage_by_sample=coverage_by_sample, verbose=verbose))
        if verbose:
            log_fh.write("Pruning finished; will be removing {} samples:\n".format(
                len(samples_to_remove)))
            for sample in samples_to_remove:
                log_fh.write("{}\n".format(sample))
            log_fh.write("\n")
        samples_to_remove = set(samples_to_remove)
        for line_fields in ped_lines:
            if line_fields[1] not in samples_to_remove:
                output_fh.write("\t".join(line_fields) + "\n")
    finally:
        if output_fh is not sys.stdout:
            output_fh.close()
        if log_fh is not sys.stderr:
            log_fh.close()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=CustomFormatter)
    parser.add_argument("PED_FILE", type=argparse.FileType("r"),
                        help="a PED/MAP/ATAV sample file to get phenotype")
    parser.add_argument("KIN0_FILE", type=argparse.FileType("r"),
                        help="the KING unrelated kinship output file to read")
    parser.add_argument("KIN_FILE", type=argparse.FileType("r"), 
                        help="the KING related kinship output file to read")
    parser.add_argument("-r", "--relatedness_threshold", default=0.0884,
                        type=float, help="consider kinship coefficients "
                        "above this value to be related")
    parser.add_argument("-d", "--distantly_related_threshold", default=0.0442,
                        type=float, help="consider kinship coefficients above "
                        "this value to be distantly related")
    parser.add_argument("--sample_coverage_summary", type=argparse.FileType("r"),
                        help="break ties by removing the sample with the "
                        "lowest coverage as indicated in this file")
    parser.add_argument("--seed", type=int, help="set a random seed to "
                        "guarantee the same results each time")
    parser.add_argument("-v", "--verbose", default=False, action="store_true",
                        help="verbose mode")
    parser.add_argument("-o", "--output", type=argparse.FileType("w"),
                        default=sys.stdout, help="the output file")
    args = parser.parse_args()
    find_samples_to_remove(
        args.PED_FILE, args.KIN0_FILE, args.KIN_FILE, args.relatedness_threshold,
        args.distantly_related_threshold, args.output,
        args.sample_coverage_summary, args.seed, args.verbose)
