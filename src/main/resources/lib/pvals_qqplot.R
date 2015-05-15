#!/usr/bin/Rscript
### libraries -----------------------------------------------------------------
suppressPackageStartupMessages(library(GenABEL))

### functions -----------------------------------------------------------------
ReadPvals <- function(path, column) {
  # Reads pvalues from file.
  #
  # Args:
  #   path: Path to pvals file.
  #
  # Returns:
  #   Pvals in vector.
  
  pvals.table <- read.table(path, header=T, sep=",")
  return(pvals.table[, column])
}

qq = function(pvector, ...) {
  # Generate QQ plot for pvals (from http://gettinggeneticsdone.blogspot.com)
  #
  # Args:
  #   pvector: vector of p-values.
  #   ...: graphical parameters for plotting.
  #
  # Returns:
  #   NULL.
  if (!is.numeric(pvector)) stop("P value vector is not numeric.")
  pvector <- pvector[!is.na(pvector) & pvector<1 & pvector>0]
  o = -log10(sort(pvector,decreasing=F))
  e = -log10( ppoints(length(pvector) ))
  plot(e,o,pch=20,cex=1, xlab=expression(Expected~~-log[10](italic(p))),
       ylab=expression(Observed~~-log[10](italic(p))), xlim=c(0,max(e)),
       ylim=c(0,max(o)), ...)
  abline(0,1,col="red")
}

QQPlotPvals <- function(pvals, main){
  qq(pvals)
  if (!any(pvals==0)) {
    lambda.str <- round(estlambda(pvals)$estimate, digits=5)
  } else {
    pvals.no.0 <- pvals[pvals != 0]
    pvals.0.count <- length(pvals) - length(pvals.no.0)
    lambda.fac <- round(estlambda(pvals.no.0)$estimate, digits=5)
    lambda.str <- paste(lambda.fac, "(excluding", pvals.0.count, 
                        "pvals that equal 0)")
  }
  title(main=main, sub=paste("Lambda factor:", lambda.str))
}

### main ----------------------------------------------------------------------
args <- commandArgs(T)
pvals.path <- args[1]
pvals.col <- as.integer(args[2])
out.path <- args[3]
pvals <- ReadPvals(pvals.path, pvals.col)
pvals <- pvals[!is.na(pvals)]
if (!((length(pvals) == 0) || all(pvals==1))) {
  pdf(out.path)
  QQPlotPvals(pvals, "QQ Plot")
  invisible(dev.off())
}
