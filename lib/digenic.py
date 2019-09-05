#!/usr/bin/env python

'''
Digenic Analysis.
~~ Get summary statistics on the digenic hits in a case/control population.
~~ 2019-1-30
~~ Joseph Hostyk, Gundula Povysil

~~ Overview:
	~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	Traditional collapsing searches for QVs (qualifying variants - variants that
	pass certain filters) in every gene. Here, we explore the digenic model:
	searching for QGPs (qualifying gene pairs - two genes in both of which a sample
	has a QV).
	We first calculate how many total QGPs there are in the dataset.
	Because that list is pretty large, we demonstrate thresholds necessary to get
	that amount down to a manageable number.

	A collapsing matrix marks how many QVs each sample has in each gene.
	Our file format is a tab-separated file with sample names as column headers
	and gene names as row headers.
	~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Usage:
	digenic.py <matrixFilePath> <geneListFilePath>

Options:
	matrixFilePath   	Path to the gzipped matrix.
	geneListFilePath   	Path to file with each gene on a new line.
	-h --help           Show these options.

  '''

import os
import time
import gzip
import numpy as np
from collections import defaultdict
from scipy import sparse, stats
import math
from docopt import docopt


###################################################################################################

### Helper functions to get data.

###################################################################################################


def getGenesFromFile(geneListFilePath):

	"""
	Read genes from the given file.

	Args:
		geneListFilePath (str): full path to the gene list file.

	Returns:
		list of genes
	"""

	return [gene.strip() for gene in open(geneListFilePath, "r")]


def readMatrixAndGenesFromFile(matrixFilePath):

	"""
	Read through a matrix file and return the genes in it, and the matrix as a scipy object.

	Args:
		matrixFilePath (str): full path to the matrix file.

	Returns:
		genesInMatrix (numpy 1-D array): row headers for the matrix. The list of genes,
			in the order they are in the matrix.
		numpyMatrix (numpy 2-D array): The collapsing matrix, read into a numpy object.
	"""

	print "Reading matrix header..."

	with gzip.open(matrixFilePath, "r") as header:
		samples = header.readline().strip().split("\t")[1:] # Header starts with "sample/gene	".
		numberSamples = len(samples)

	print "There are {} total samples.".format(numberSamples)
	print "Reading matrix..."


	# Because each line starts with a gene name and ends in a tab, we only read the columns in between.
	samplesRange = range(1, numberSamples + 1)
	genesInMatrix = np.genfromtxt(matrixFilePath, delimiter = "\t", skip_header = 1, usecols = 0, dtype = 'U') # Read the first column
	numpyMatrix = np.genfromtxt(matrixFilePath, delimiter = "\t", skip_header = 1, usecols = samplesRange) # Get the full matrix.


	return numpyMatrix, genesInMatrix


def filterMatrixWithGeneList(matrix, genesInMatrix, desiredGenesList):

	"""
	Filter a matrix file so that the digenic summary becomes more tractable.

	Args:
		matrix (numpy matrix): the genes x samples matrix that was read from the file.
		genesInMatrix (numpy array): the genes in the matrix, in the order they are in the matrix.
		desiredGenesList (list of strings): the list of genes with which to filter/

	Returns:
		filteredMatrix (numpy matrix): the matrix subset to only the desired genes.
	"""

	if desiredGenesList:
		print "Filtering: keeping {} genes...".format(len(desiredGenesList))
		desiredIndices = np.where(np.in1d(genesInMatrix, desiredGenesList))
		filteredMatrix = matrix[desiredIndices]
		desiredGenesInMatrix = genesInMatrix[desiredIndices]
	else:
		filteredMatrix = np.copy(matrix)
		desiredGenesInMatrix = genesInMatrix

	return filteredMatrix


def makeSparseMatrix(matrix):

	"""
	Turn a numpy matrix into a scipy sparse array, since the majority of elements will be 0.
	(Samples do not have QVs in most genes.)

	Args:
		matrix (numpy matrix): the genes x samples matrix.

	Returns:
		The sparse matrix.
	"""

	return sparse.csr_matrix(matrix)

###################################################################################################

### Exploratory function.

###################################################################################################

def analyzeSparseMatrix(sparseMatrix):

	"""
	Get summary stats about our matrix.

	Args:
		sparseMatrix (scipy sparse matrix): the non-zero entries of our genes x samples matrix.
	"""

	# Matrix stores total number of QVs. Change to ones to get indicator variable.
	sparseMatrix.data[sparseMatrix.data > 1] = 1

	# By multiplying the genes x samples matrix by itself, we get the digenic matrix:
	# Entry i,j is the number of samples that have a QV in both gene i and gene j.
	product = sparseMatrix.dot(sparseMatrix.transpose())

	totalGenes = len(np.nonzero(product.diagonal())[0])

	totalQGPs =  math.factorial(totalGenes) / math.factorial(2) / math.factorial(totalGenes-2)
	print "There are {} non-zero genes, meaning there can be at most {} possible QGPs.".format(totalGenes, totalQGPs)

	# Because the digenic matrix is symmetric, we only need the lower triangular half.
	goodVals = sparse.tril(product, k = -1)

	# Any non-zero element of the lower half of the digenic matrix, is a valid QGP.
	print "Total QGPs:", goodVals.nnz
	
	# Get the values out of matrix:
	samplesPerQGP = goodVals.data

	print "The max number of samples that share the same QGP: {}".format(max(samplesPerQGP))
	print "The mean number of samples that share the same QGP: {}".format(np.mean(samplesPerQGP))
	print "The median number of samples that share the same QGP: {}".format(np.median(samplesPerQGP))

	print "Number of QGPs present in at least 5 samples: {}".format(len(goodVals.data[goodVals.data > 5]))
	print "Number of QGPs present in at least 10 samples: {}".format(len(goodVals.data[goodVals.data > 10]))
	print "Number of QGPs present in at least 15 samples: {}".format(len(goodVals.data[goodVals.data > 15]))
	print "Number of QGPs present in at least 50 samples: {}".format(len(goodVals.data[goodVals.data > 50]))


def handler(matrixFilePath, geneListFilePath):
	
	"""
	Our main function, to get things running.

	Args:
		matrixFilePath (str): full path to the matrix file.
		geneListFilePath (str): full path to the gene list file.
	"""

	geneList = getGenesFromFile(geneListFilePath)

	matrix, genesInMatrix = readMatrixAndGenesFromFile(matrixFilePath)
		
	filteredMatrix = filterMatrixWithGeneList(matrix, genesInMatrix, desiredGenesList = geneList)

	sparseMatrix = makeSparseMatrix(filteredMatrix)

	analyzeSparseMatrix(sparseMatrix)
	
	return


if __name__ == '__main__':

	arguments = docopt(__doc__)
	handler(arguments["<matrixFilePath>"], arguments["<geneListFilePath>"])

