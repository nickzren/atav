#!/usr/bin/env python

## Check differences between cases' and controls' QV counts.

## 2018-6-11

## Joseph Hostyk



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
	with open(genotypesFilename, "r") as variants:
		header = variants.readline().strip().split(",")
		variant = dict(zip(header, variants.readline().strip().split(",")))

		someCases = int(variant["Covered Case" + comphetSuffix])
		casePercentage = float(variant["Covered Case Percentage" + comphetSuffix])/100.0
		totalCases = int(round(someCases/casePercentage))

		someControls = int(variant["Covered Ctrl" + comphetSuffix])
		controlPercentage = float(variant["Covered Ctrl Percentage" + comphetSuffix])/100.0
		totalControls = int(round(someControls/controlPercentage))
	return totalCases, totalControls



def getQVcountsForDominantModel(genotypesFilename):

	"""
	Read through a _genotypes file and get the variant counts for all samples,
	broken down into cases and controls.
	We keep a tally of how many lines each sample's ID shows up in; keeping track
	of all their variant IDs would take up too much space.

	Args:
		genotypesFilename (str): path to the _genotypes file.

	Returns:
		counts (dict of dicts): In this format: 
			{"case": {"sample1": numVariants, "sample2": numVariants, ...}, "ctrl": {"sample1": numVariants, ...}}
	"""

	counts = defaultdict(Counter)

	with open(genotypesFilename, "r") as variants:
		# Awesome way of using header names, provided by Brett Copeland.
		header = variants.readline().strip().split(",")

		for line in variants:

			line = dict(zip(header, line.strip().split(",")))
			caseOrControl = line["Sample Phenotype"]
			name = line["Sample Name"]
			counts[caseOrControl][name] += 1

	return counts

def getQVsForComphetModel(comphetVariantsFilename):

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
		comphetVariantsFilename (str): path to the _genotypes file.

	Returns:
		variantIDs (dict of dicts): In this format: 
			{"case": {"sample1": set(variant1, variant2), "sample2": set(), ...}, "ctrl": {"sample1": set(), ...}}
	"""

	variantIDs = defaultdict(lambda: defaultdict(set))

	with open(comphetVariantsFilename, "r") as variants:
		# Awesome way of using header names, provided by Brett Copeland.
		header = variants.readline().strip().split(",")

		for line in variants:

			line = dict(zip(header, line.strip().split(",")))
			caseOrControl = line["Sample Phenotype (#1)"]
			name = line["Sample Name (#1)"]
			variantID1 = line["Variant ID (#1)"]
			variantIDs[caseOrControl][name].add(variantID1)
			# The comphet file also includes homozygous mutations, in which case there is no Variant #2.
			if line["Sample Phenotype (#1)"] == "het":
				variantID2 = line["Variant ID (#2)"]
				variantIDs[caseOrControl][name].add(variantID2)

	return variantIDs

def graphQVcounts(casesCounts, controlsCounts, outputName):

	"""
	Create and plot a histogram of variant counts, broken into cases and controls.
	
	Args:
		casesCounts and controlsCounts (dict): In this format:
			{"sample1": numVariants, "sample2": numVariants, ...}
		outputName (str): Path to which the graph will be written.
	"""

	plt.figure(1)
	binwidth = 1.0
	mostMin = min(min(casesCounts), min(controlsCounts))-0.5
	mostMax = max(max(casesCounts), max(controlsCounts))-0.5
	caseBins = controlBins = np.arange(mostMin, mostMax + binwidth, binwidth)
	plt.hist(casesCounts, caseBins, facecolor='green', alpha=0.35, label="Cases", ec='white', normed=True)
	plt.hist(controlsCounts, controlBins, facecolor='blue', alpha=0.35, label="Controls", ec='white', normed=True)
	plt.title("QVCounts")
	plt.xlabel('QV Amounts')
	plt.ylabel('Percentage of [Case/Control] that\nhave this amount of QVs')
	plt.legend(loc='upper right')
	plt.grid(True)

	plt.savefig(outputName, bbox_inches='tight')
	plt.close()


def writeToLog(logName, message, writeOrAppend):

	"""
	Our general logger function.
	"""

	with open(logName, writeOrAppend) as out:
		out.write(message)

def initializeLog(logName, casesCounts, controlsCounts):
	
	"""
	Our first logging function, which we always want run.
	"""
	log = ""
	log += "Checking amounts of called variant for cases vs. controls. Date: {}.\n".format(datetime.datetime.now().strftime("%Y-%m-%d %H:%M"))
	log += "Cases (n = {}): Median: {}; mean: {}.\n".format(len(casesCounts), np.median(casesCounts), np.mean(casesCounts))
	log += "Controls (n = {}): Median: {}; mean: {}.\n".format(len(controlsCounts), np.median(controlsCounts), np.mean(controlsCounts))
	writeToLog(logName, log, writeOrAppend = "w")

def writeMannWhitneyResultsToLog(logName, pvalue):

	"""
	If the Mann Whitney function didn't error out, we log the results.
	"""
	log = ""
	log += "Null hypothesis: the cases' variants and controls' variants follow the same distribution.\n"
	if pvalue < 0.05:
		log += "The p-value ({0:.3}) is less than 0.05. We reject the null, meaning there's a significant difference\n".format(pvalue)
		log += "between the number of variants that the cases have than do the controls."
	else:
		log += "The p-value ({0:.3}) is greater than 0.05. We do not reject the null, meaning that there's no significant\n".format(pvalue)
		log += "difference between the number of variants that the cases have than do the controls."
	writeToLog(logName, log, writeOrAppend = "a")

	return

def handler(genotypesFilename):

	"""
	Our main function, that works just from the path to a _genotypes file.
	"""
	fileName = genotypesFilename.split("/")[-1].replace(".csv", "")

	folderWithoutFilename = genotypesFilename.split("/")[:-1]
	folder = "/".join(folderWithoutFilename)
	# Making sure we write either to the given directory, or the current one.
	if len(folder) > 0:
		folder += "/"
	else:
		folder += "./"

	# Get the number of QVs per sample.
	# These don't include the zero-counts. (Samples that had no variants.)

	# The dominant model returns the actual counts.
	if "genotypes" in genotypesFilename:
		counts = getQVcountsForDominantModel(genotypesFilename)
		qvsPerCase = counts["case"].values()
		qvsPerControl = counts["ctrl"].values()
	# The comphet model returns the variantIDs, which we need to count up.
	if "comphet" in genotypesFilename:
		qvs = getQVsForComphetModel(genotypesFilename)
		qvsPerCase = [len(setOfVariants) for setOfVariants in qvs["case"].values()]
		qvsPerControl = [len(setOfVariants) for setOfVariants in qvs["ctrl"].values()]

	totalCases, totalControls = getTotalCaseAndControlCounts(genotypesFilename)



	# Add on the zero-counts.
	numCasesWithoutQV = totalCases - len(qvsPerCase)
	casesWithoutQV = [0] * numCasesWithoutQV
	qvsPerCase += casesWithoutQV

	numControlsWithoutQV = totalControls - len(qvsPerControl)
	controlsWithoutQV = [0] * numControlsWithoutQV
	qvsPerControl += controlsWithoutQV

	suffix = fileName.split("_")[-1]
	logName = folder + fileName.replace(suffix, "qv_counts.log")
	initializeLog(logName, qvsPerCase, qvsPerControl)

	try:
		value, pvalue = stats.mannwhitneyu(qvsPerCase, qvsPerControl)
		writeMannWhitneyResultsToLog(logName, pvalue)
	except ValueError as e: # If either array is empty.
		pvalue = None
		writeToLog(logName, "The test could not be run. (You may have run with no cases, or no controls.)", writeOrAppend = "a")
	
	qvPlotName = folder + fileName.replace(suffix, "qv_counts.png")
	graphQVcounts(qvsPerCase, qvsPerControl, qvPlotName)
	



if __name__ == '__main__':

	if len(sys.argv) != 2:
		raise ValueError("This script only takes in a _genotypes filename as an argument.")
	else:
		genotypesFilename = sys.argv[1]
		handler(genotypesFilename)

