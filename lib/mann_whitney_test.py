#!/usr/bin/env python

'''
Mann-Whitney Test.
~~ Check differences between cases' and controls' QV counts.
~~ 2018-6-11
~~ Joseph Hostyk

Usage:
	mann_whitney_test.py <genotypesFileName> [<pedFilePath>]

# [(--total_cases=<cases> --total_controls=<controls>)]

Requirements:
	docopt is installed on this version of Python:
		/nfs/goldstein/software/python2.7.9-x86_64_shared/python2.7.9_shared-ENV.sh

Options:
	genotypesFileName   Path to the file.
	pedFilePath   		(Optional, but recommended) path to the sample/ped file.
	-h --help           Show these options.
  '''


import sys
from collections import defaultdict, Counter
import numpy as np
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import seaborn as sns
import sys
from scipy import stats
import datetime
import math
from docopt import docopt
import csv


def getCaseAndControlListsFromPedFile(pedFilePath):
	"""
	Read through a ped/samples file and get the names of the cases and controls.

	Args:
		pedFilePath (str): path to the ped file.

	Returns:
		total cases, total controls (ints)
	"""	
	CASE = "2"
	CONTROL = "1"
	statusIndex = 5
	nameIndex = 1
	names = defaultdict(list)
	with open(pedFilePath, "r") as samples:

		for sample in samples:
			names[sample.split("\t")[statusIndex]].append(sample.split("\t")[nameIndex]) # The second spot is the ID/name.

	return names[CASE], names[CONTROL]

def getTotalCaseAndControlCounts(genotypesFilename):
	"""
	Read through the first two lines of a _genotypes file and calculate
	how many total cases and controls were in the initial ATAV job.
	(If a sample does not have a variant, they won't show up in the file, so this
	total needs to be calculated separately.)
	Args:
		genotypesFilename (str): path to the _genotypes file.
	Returns:
		totalCases and totalControls (both ints)
	"""

	comphetSuffix = ""
	if "comphet" in genotypesFilename:
		comphetSuffix = " (#1)"

	# We read through the whole file. Might take a while, but easier than dealing with all edge cases.
	maxCoveredCasePercentage = 0
	maxCoveredControlPercentage = 0
	reader = csv.reader(open(genotypesFilename, "r"))
	header = next(reader)

	for variant in reader:

		variant = dict(zip(header, variant))
		casePercentage = float(variant["Covered Case Percentage" + comphetSuffix])/100.0
		if casePercentage > maxCoveredCasePercentage:
			maxCoveredCasePercentage = casePercentage
			coveredCases = int(variant["Covered Case" + comphetSuffix])
			totalCases = int(round(coveredCases/casePercentage))

		controlPercentage = float(variant["Covered Ctrl Percentage" + comphetSuffix])/100.0
		if controlPercentage > maxCoveredControlPercentage:
			maxCoveredControlPercentage = controlPercentage
			coveredControls = int(variant["Covered Ctrl" + comphetSuffix])
			totalControls = int(round(coveredControls/controlPercentage))
	return totalCases, totalControls


def getQVcounts(genotypesFilename, caseNames, controlNames):
	"""
	Read through a genotypes file and get the number of QVs per sample.
	
	Args:
		genotypesFilename (str): path to the _genotypes file.
	Returns:
		caseNames, controlNames (lists of strings): the actual names/IDs of the cases/controls.
	"""

	if "genotypes" in genotypesFilename:
		caseCounts, controlCounts = getQVcountsForDominantModel(genotypesFilename, caseNames, controlNames)
	if "comphet" in genotypesFilename:
		caseCounts, controlCounts = getQVsForComphetModel(genotypesFilename, caseNames, controlNames)
		
	return caseCounts, controlCounts

def getQVcountsForDominantModel(genotypesFilename, caseNames, controlNames):

	"""
	Read through a _genotypes file and get the variant counts for all samples,
	broken down into cases and controls.

	Args:
		genotypesFilename (str): path to the _genotypes file.
		caseNames, controlNames (lists of strings): the actual names/IDs of the cases/controls.

	Returns:
		caseCounts, controlCounts (dicts): In this format: {id1: numQVs, id2: numQvs, ...}
	"""

	# If we have a sample file, then we have everyone's names:
	if len(caseNames) != 0 or len(controlNames) != 0:
		caseCounts = {name: 0 for name in caseNames}
		controlCounts = {name: 0 for name in controlNames}

		counts = {"case": caseCounts, "ctrl": controlCounts}

	# Otherwise, we work just from the genotypes file and get names from there as we go.
	else:
		counts = defaultdict(Counter)

	reader = csv.reader(open(genotypesFilename, "r"))
	header = next(reader)

	for line in reader:

		line = dict(zip(header, line))
		caseOrControl = line["Sample Phenotype"]
		name = line["Sample Name"]
		counts[caseOrControl][name] += 1

	return counts["case"], counts["ctrl"]

def getQVsForComphetModel(comphetVariantsFilename, caseNames, controlNames):

	"""
	Read through a _comphet file and get the variant counts for all samples,
	broken down into cases and controls.
	If a sample has 2 variants in one gene, they'll be printed to the same line as
	"Variant (#1)" and "Variant (#2)".
	If a sample has 3 variants in one gene, three lines will be printed: 1 with 2,
	1 with 3, and 2 with 3.
	If a sample has 2 variants in one gene and 2 variants in another gene, they'll
	be printed as two separate lines.
	We therefore have to keep track of all variant IDs, in order to get the true
	count of how many variants the sample has.

	Args:
		genotypesFilename (str): path to the _genotypes file.
		caseNames, controlNames (lists of strings): the actual names/IDs of the cases/controls.

	Returns:
		caseCounts, controlCounts (dicts): In this format: {id1: numQVs, id2: numQvs, ...}
	"""

	# If we have a sample file, then we have everyone's names:
	if len(caseNames) != 0 or len(controlNames) != 0:

		caseCounts = {name: set() for name in caseNames}
		controlCounts = {name: set() for name in controlNames}

		variantIDs = {"case": caseCounts, "ctrl": controlCounts}

	# Otherwise, we work just from the genotypes file and get names from there as we go.
	else:
		variantIDs = {"case": defaultdict(set), "ctrl": defaultdict(set)}

	reader = csv.reader(open(comphetVariantsFilename, "r"))
	header = next(reader)

	for line in reader:

		line = dict(zip(header, line))

		caseOrControl = line["Sample Phenotype (#1)"]
		name = line["Sample Name (#1)"]
		variantID1 = line["Variant ID (#1)"]
		variantIDs[caseOrControl][name].add(variantID1)
		# The comphet file also includes homozygous mutations, in which case there is no Variant #2.
		if line["Sample Phenotype (#1)"] == "het":
			variantID2 = line["Variant ID (#2)"]
			variantIDs[caseOrControl][name].add(variantID2)

	caseCounts = {name: len(variants) for name, variants in variantIDs["case"].items()}
	controlCounts = {name: len(variants) for name, variants in variantIDs["ctrl"].items()}
	return caseCounts, controlCounts


def writeToLog(logName, message, writeOrAppend):

	"""
	Our general logger function.
	"""

	with open(logName, writeOrAppend) as out:
		out.write(message)


def writeOutQVcounts(qvCountsLogName, casesToTheirQVcounts, controlsToTheirQVcounts, missingCases, missingControls):

	"""
	Write out the sample name and the number of QVs they have to a log.
	Args:
		qvCountsLogName (str): full path to the log file.
		casesToTheirQVcounts, controlsToTheirQVcounts (dicts): Sample names and their counts.
		missingCases, missingControls (ints): the number of cases/controls that were included in the run,
			but aren't in the genotypes file. Necessary if no sample file was submitted.

	"""

	log = ""
	if missingCases > 0 or missingControls > 0:
		log += "### A sample file may have not been submitted, and so there are some samples that do not have\n"
		log += "### any QVs, but we don't know their IDs.\n"
		if missingCases > 0:
			log += "### There are {} unknown cases with no QVs.\n".format(missingCases)
		if missingControls > 0:
			log += "### There are {} unknown controls with no QVs.\n".format(missingControls)
	log += "Name\tCount\tCase/Control\n"
	log += "\n".join("{name}\t{count}\tCase".format(name = name, count = count) for name, count in casesToTheirQVcounts.items())
	log += "\n"
	log += "\n".join("{name}\t{count}\tControl".format(name = name, count = count) for name, count in controlsToTheirQVcounts.items())

	writeToLog(qvCountsLogName, log, writeOrAppend = "w")


def runMannWhitney(mannWhitneyLogName, qvsPerCase, qvsPerControl):

	"""
	Run Mann Whitney and write out the results.
	Args:
		qvsPerCase, qvsPerControl (lists of ints): The lists of the actual counts of qvs.

	"""

	log = ""
	log += "Checking amounts of called variant for cases vs. controls. Date: {}.\n".format(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))
	log += "Cases (n = {}): Median: {}; mean: {}.\n".format(len(qvsPerCase), np.median(qvsPerCase), np.mean(qvsPerCase))
	log += "Controls (n = {}): Median: {}; mean: {}.\n".format(len(qvsPerControl), np.median(qvsPerControl), np.mean(qvsPerControl))

	log += "Null hypothesis: the cases' variants and controls' variants follow the same distribution.\n"
	
	value, pvalue = stats.mannwhitneyu(qvsPerCase, qvsPerControl)
	if pvalue == 0.0:
		log += "The test could not be run. (You may have run with no cases, or no controls.)"
	elif pvalue < 0.05:
		log += "The p-value ({0:.3}) is less than 0.05. We reject the null, meaning there's a significant difference\n".format(pvalue)
		log += "between the number of variants that the cases have than do the controls."
	else:
		log += "The p-value ({0:.3}) is greater than 0.05. We do not reject the null, meaning that there's no significant\n".format(pvalue)
		log += "difference between the number of variants that the cases have than do the controls."
	writeToLog(mannWhitneyLogName, log, writeOrAppend = "w")

	return


def graphQVcounts(caseCounts, controlCounts, outputName):

	"""
	Create and plot a histogram of variant counts, broken into cases and controls.
	
	Args:
		caseCounts and controlCounts (list of ints): The lists of the actual counts of qvs.
		outputName (str): Path to which the graph will be written.
	"""

	plt.figure(1)

	caseCounter = Counter(caseCounts)
	controlCounter = Counter(controlCounts)

	totalCases = float(len(caseCounts))
	totalControls = float(len(controlCounts))

	plt.bar(caseCounter.keys(), np.array(caseCounter.values())/totalCases, facecolor='green', alpha=0.35, label="Cases", ec='white')
	if len(controlCounter.keys()) > 0: # Sometimes only cases are submitted.
		plt.bar(controlCounter.keys(), np.array(controlCounter.values())/totalControls, facecolor='blue', alpha=0.35, label="Controls", ec='white')

	plt.title("QVCounts")
	plt.xlabel('QV Amounts')
	plt.ylabel('Percentage of [Case/Control] that\nhave this amount of QVs')
	plt.legend(loc='upper right')
	plt.grid(True)

	plt.savefig(outputName, bbox_inches='tight')
	plt.close()


def quickReturnQVamounts(genotypesFilename, pedFilePath):

	# If we have the full sample list, we can write out each sample's QV counts.
	hasPedFile = bool(pedFilePath)
	if hasPedFile:
		caseNames, controlNames = getCaseAndControlListsFromPedFile(pedFilePath)
		totalCases = len(caseNames)
		totalControls = len(controlNames)
	# Otherwise, we work from the genotypes file exclusively
	else:
		caseNames = []
		controlNames = []
		totalCases, totalControls = getTotalCaseAndControlCounts(genotypesFilename)

	casesToTheirQVcounts, controlsToTheirQVcounts = getQVcounts(genotypesFilename, caseNames, controlNames)

	# If have ped file, this shouldn't change. If only have genotypes file, now have list of names in file.
	caseNames = casesToTheirQVcounts.keys()
	controlNames = controlsToTheirQVcounts.keys()

	# Should be 0 if have a ped file.
	missingCases = totalCases - len(caseNames)
	missingControls = totalControls - len(controlNames)

	qvsPerCase = casesToTheirQVcounts.values() + [0] * missingCases
	qvsPerControl = controlsToTheirQVcounts.values() + [0] * missingControls

	return qvsPerCase, qvsPerControl


def handler(genotypesFilename, pedFilePath):

	"""
	Our main function.
	"""
	fileName = genotypesFilename.split("/")[-1].replace(".csv", "")

	folderWithoutFilename = genotypesFilename.split("/")[:-1]
	folder = "/".join(folderWithoutFilename)
	# Making sure we write either to the given directory, or the current one.
	if len(folder) > 0:
		folder += "/"
	else:
		folder += "./"

	# If we have the full sample list, we can write out each sample's QV counts.
	hasPedFile = bool(pedFilePath)
	if hasPedFile:
		caseNames, controlNames = getCaseAndControlListsFromPedFile(pedFilePath)
		totalCases = len(caseNames)
		totalControls = len(controlNames)
	# Otherwise, we work from the genotypes file exclusively
	else:
		caseNames = []
		controlNames = []
		totalCases, totalControls = getTotalCaseAndControlCounts(genotypesFilename)

	casesToTheirQVcounts, controlsToTheirQVcounts = getQVcounts(genotypesFilename, caseNames, controlNames)

	# If have ped file, this shouldn't change. If only have genotypes file, now have list of names in file.
	caseNames = casesToTheirQVcounts.keys()
	controlNames = controlsToTheirQVcounts.keys()

	# Should be 0 if have a ped file.
	missingCases = totalCases - len(caseNames)
	missingControls = totalControls - len(controlNames)

	suffix = fileName.split("_")[-1]
	qvCountsLogName = folder + fileName.replace(suffix, "qv_counts.tsv")
	writeOutQVcounts(qvCountsLogName, casesToTheirQVcounts, controlsToTheirQVcounts, missingCases, missingControls)

	qvsPerCase = casesToTheirQVcounts.values() + [0] * missingCases
	qvsPerControl = controlsToTheirQVcounts.values() + [0] * missingControls

	mannWhitneyLogName = folder + fileName.replace(suffix, "mann_whitney.log")
	runMannWhitney(mannWhitneyLogName, qvsPerCase, qvsPerControl)

	qvPlotName = folder + fileName.replace(suffix, "qv_counts.png")
	graphQVcounts(qvsPerCase, qvsPerControl, qvPlotName)

	return

if __name__ == '__main__':

	# if len(sys.argv) != 2:
	# 	raise ValueError("This script only takes in a _genotypes filename as an argument.")
	# else:
	# 	genotypesFilename = sys.argv[1]
	# 	handler(genotypesFilename)

	arguments = docopt(__doc__)
	# Can access the values like: arguments["genotypesFilename"]
	# handler(*arguments.values())
	handler(arguments["<genotypesFileName>"], arguments["<pedFilePath>"])
