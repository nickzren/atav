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
	with open(genotypesFilename, "r") as variants:
		header = variants.readline().strip().split(",")
		variant = dict(zip(header, variants.readline().strip().split(",")))

		someCases = int(variant["Covered Case"])
		casePercentage = float(variant["Covered Case Percentage"])/100.0
		totalCases = int(round(someCases/casePercentage))

		someControls = int(variant["Covered Ctrl"])
		controlPercentage = float(variant["Covered Ctrl Percentage"])/100.0
		totalControls = int(round(someControls/controlPercentage))
	return totalCases, totalControls



def getQVcounts(genotypesFilename):

	"""
	Read through a _genotypes file and get the variant counts for all samples,
	broken down into cases and controls.

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
			if "_genotypes" in genotypesFilename:
				caseOrControl = line["Sample Phenotype"]
				name = line["Sample Name"]
			elif "_comphet" in genotypesFilename:
				caseOrControl = line["Sample Phenotype (#1)"]
				name = line["Sample Name (#1)"]
			counts[caseOrControl][name] += 1

	return counts


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

	counts = getQVcounts(genotypesFilename)
	totalCases, totalControls = getTotalCaseAndControlCounts(genotypesFilename)

	# These don't include the zero-counts. (Samples that had no variants.)
	qvsPerCase = counts["case"].values()
	qvsPerControl = counts["ctrl"].values()

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

