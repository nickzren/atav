#read the R library implemented by Quanli Wang
source("/nfs/goldstein/software/atav_home/lib/qqplot_perm_functions.R")

#parse input parameters
args <- commandArgs(T)
sample.file <- args[1]
matrix.file <- args[2]
n.permutations<- as.integer(args[3])
out.path <- args[4]

#do permutation
Ps <- QQ.permutation(sample.file, matrix.file,n.permutations) 

#output plot
pdf(out.path)
QQ.plot(Ps$perm, Ps$observed)
lambda <-estlambda.permutation(Ps$observed,Ps$perm, plot = TRUE)
invisible(dev.off())
