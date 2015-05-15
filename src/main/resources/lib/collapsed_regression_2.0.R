#!/nfs/goldstein/software/R-3.0.1/bin/Rscript
### libraries -----------------------------------------------------------------
suppressPackageStartupMessages(library(yaml))
suppressPackageStartupMessages(library(logistf))
suppressPackageStartupMessages(library(optparse))
suppressPackageStartupMessages(library(GenABEL))

### constants -----------------------------------------------------------------
kCategoricalRegex <- "_cat$"
kNoSignalPval <- "NA"
kInteractive <- F
kDebug <- F
kDebugGenes <- c("AC007394.3", "MIR218-1", "GPR34")
kMinSignal <- 2
kVersion <- 3

### functions -----------------------------------------------------------------
GetOptList <- function() {
  # Get option list.
  #
  # Returns:
  #   Option list to parse options.
  option.list <- list(
    make_option(c("-s", "--samples"), action="store", 
                default="samples.tab", dest="samples.path", 
                help="Phenotype / covariates file path, [default %default]"),

    make_option(c("-c", "--clps"), action="store", 
                default="collapsed.csv", dest="clps.path", 
                help="Collapsed file path, [default %default]"),

    make_option(c("-r", "--delim"), action="store", 
                default="\t", dest="sep", 
                help="Delimiter for collapsed file, [default TAB]"),

    make_option(c("-o", "--out"), action="store", 
                default="samples.regressed.csv", dest="out.path", 
                help="Output file path, [default %default]"),

    make_option(c("-p", "--detailed"), action="store", 
                default=NULL, dest="detailed.path", 
                help="Detailed output file path, [default %default]"),

    make_option(c("-l", "--log"), action="store", 
                default="regress.log", dest="log.path", 
                help="Log file path, [default %default]"),

    make_option(c("-t", "--transpose"), action="store_true", 
                default=FALSE, dest="transpose",
                help=paste("If set, collapsed matrix is read",
                           "as gene by sample, as opposed to the", 
                           "default sample by gene")),

    make_option(c("-i", "--include"), action="store", 
                default=NULL, dest="covars", 
                help="Covariates to include, [default include all covariates]"),

    make_option(c("-m", "--method"), action="store", 
                dest="method", help="Method (linear / logistf) [no default]"),

    make_option(c("-d", "--display-none"), action="store_true", 
                default=FALSE, dest="display.none", 
                help="Display genes with no signal [default %default]"),

    make_option(c("-y", "--count-vars"), action="store_true", 
                default=FALSE, dest="count.vars", 
                help="Show rare variants fraction in output [default %default]"),

    make_option(c("-q", "--qqplot"), action="store_true",
                default=FALSE, dest="qqplot",
                help="Generate QQ plot [default %default]")
  )
}

WriteLog <- function(..., con=stderr()) {
  # Writes to log.
  #
  # Args:
  #   ...: Log message and arguments for paste.
  #   con: File handle to write log to.
  #
  # Returns:
  #   No return value.
  write(paste(Sys.time(), ...), con)
}

GetFactorIdxs <- function(cols, regex=kCategoricalRegex) {
  # Gets factor indexes from column names.
  #
  # Args:
  #   cols: Column names.
  #
  # Returns:
  #   Indexes of column names in cols.
  return(grep(regex, cols))
}

ReadSamples <- function(path, method) {
  # Reads phenotypes and covariates from samples file.
  #
  # Args:
  #   path: Path to samples file.
  #   method: Regression method that will be used for analysis of these data.
  #
  # Returns:
  #   Data frame with family id, ind id, phenotype, covariates.
  
  samples <- na.omit(read.table(path, header=T, comment.char=""))
  colnames(samples)[1:3] <- c("famid", "indid", "pheno")

  samples$famid <- tolower(samples$famid)
  samples$indid <- tolower(samples$indid)

  # subtract 1 to convert phenos from plink's 1,2 to 0,1 for logistf
  if (method == "logistf") {
    samples$pheno <- samples$pheno - 1
  }

  # account for covariates that are factors
  factor.idxs <- GetFactorIdxs(colnames(samples))
  for (factor.idx in factor.idxs) {
       samples[, factor.idx] <- as.factor(samples[, factor.idx])
  }

  return(samples)
}

Regress <- function(gene.name, lm.data, method) {
  # Runs Firth logistic regression or linear regression on lm.data.
  #
  # Args:
  #   gene.name: Name of the gene for the regression.
  #   lm.data: Data frame to run regression on.
  #   method: Regression method. Either logistf or linear.
  #
  # Returns:
  #   logistf / linear model.
  if ((sum(lm.data$gene) < kMinSignal) || # no / all of sample has rare variant
      ((sum(lm.data$gene) == length(lm.data$gene)))) {
    return(NULL)
  }

  if (method == "logistf") { 
   gene.model <- logistf(pheno ~ ., data=lm.data, pl=F)
  } else if (method == "linear") { 
    gene.model <- lm(pheno ~ ., data=lm.data)
  } else {
    stop("Unknown method:", method)
  }
  
  if ((kDebug) && (gene.name %in% kDebugGenes)) {
    browser(gene.name)
  }
  return(gene.model)
}

ExtractPvals <- function(gene.model, method) {
  # Extracts p-values from logistf or linear regression model.
  #
  # Args:
  #   gene.model: regression model.
  #
  # Returns:
  #   Vector of p-values, gene followed by covariates.
  if (method == "logistf") {
    pvals <- (gene.model$prob[-1])
    betas <- (gene.model$coef[-1])
  } else if (method == "linear") {
    pvals <- (summary(gene.model)$coef[, "Pr(>|t|)"][-1])
    betas <- coef(gene.model)[-1]
  } else {
    stop("Unknown method:", method)
  }

  pval.gene <- tail(pvals, n=1) 
  pvals <- c(pval.gene, pvals[-length(pvals)])

  beta.gene <- tail(betas, n=1)
  betas <- c(beta.gene, betas[-length(betas)])

  return(list(pvals=pvals, betas=betas))
}

ReadClps <- function(clps.path, transpose=F, sep=","){
  # Reads collapsed matrix.
  #
  # Args:
  #   clps.path: Path to collapsed matrix.
  #   transpose: If true, matrix is transposed before processing.
  #   sep: Delimiter for collapsed file.
  #
  # Returns:
  #   Data frame of collapsed matrix.
  clps <- read.table(clps.path, header=T, row.names=1, 
                     check.names=F, fill=F, sep=sep)
  if (transpose) {
    clps <- as.data.frame(t(clps))
  }
  rownames(clps) <- tolower(rownames(clps))

  return(as.data.frame(clps))
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
  #e = -log10( 1:length(o)/length(o) )
  e = -log10( ppoints(length(pvector) ))
  plot(e,o,pch=20,cex=1, xlab=expression(Expected~~-log[10](italic(p))),
       ylab=expression(Observed~~-log[10](italic(p))), xlim=c(0,max(e)),
       ylim=c(0,max(o)), ...)
  abline(0,1,col="red")
}

QQPlotPvals <- function(pvals, main) {
  qq(pvals)
  if (!any(pvals==0)) {
    lambda.str <- round(estlambda(pvals)$estimate, digits=5)
  } else {
    pvals.no.0 <- pvals[pvals != 0]
    pvals.0.count <- length(pvals) - length(pvals.no.0)
    lambda.fac <- round(estlambda(pvals.no.0)$estimate, digits=5)
    lambda.est <- paste(lambda.fac, "(excluding", pvals.0.count, 
                        "pvals that equal 0)")
  }
  title(main=main, sub=paste("Lambda factor:", lambda.str))
}

AddSingleQuotes <- function(string) {
  # Add single quotes to a string.
  #
  # Args:
  #   string: string to add single quotes to.
  #
  # Returns:
  #   <string> with a single quote on both sides.
  return (paste("'", string, "'", sep=""))
}

### debug ---------------------------------------------------------------------
#debug(ReadClps)

### main ----------------------------------------------------------------------

# parse opts
option.list <- GetOptList()

# get command line options
if (!kInteractive) {
  opt <- parse_args(OptionParser(option_list=option.list))
}

# open log
if (!kDebug) {
  log.con <- file(opt$log.path, "w")
} else {
  log.con <- ""
}

# write configuration to log
WriteLog("Command line:", 
         paste(commandArgs(trailingOnly = FALSE), collapse=" "),
         con=log.con)
WriteLog("Version:", kVersion, con=log.con)
WriteLog("Samples path:", opt$samples.path, con=log.con)
WriteLog("Collapsed matrix path:", opt$clps.path, con=log.con)
WriteLog("Output path:", opt$out.path, con=log.con)
WriteLog("Log path:", opt$log.path, con=log.con)
WriteLog("Method:", opt$method, con=log.con)
if (is.null(opt$covars)) {  
  WriteLog("Including all covariates supplied", con=log.con)
} else {
  WriteLog("Covariates to include:", opt$covars, con=log.con)
}

# read collapsed data
WriteLog("Reading collapsed data", con=log.con)
collapsed <- ReadClps(opt$clps.path, opt$transpose, opt$sep)

# extract gene names
WriteLog("Extracting gene names", con=log.con)
genes <- colnames(collapsed)
genes.count <- length(genes)
WriteLog(genes.count, "genes read", con=log.con)

# read phenotypes and covariates
WriteLog("Reading samples file", con=log.con)
samples <- ReadSamples(opt$samples.path, opt$method)
covar.cols <- colnames(samples)[-c(1:3)] # -(famid, indid, pheno)
covar.count <- length(covar.cols)

WriteLog(nrow(samples), "samples read", con=log.con)
WriteLog(covar.count, "covariates per sample read", con=log.con)
if (opt$method == "logistf") {
  WriteLog(sum(samples$pheno), "cases and", 
           sum(samples$pheno==0), "controls", con=log.con)
}

# determine covariates
if (is.null(opt$covars)) {
  covars.exclude <- c()
  WriteLog("Including all covariates", con=log.con)
} else {
  opt$covars <- strsplit(opt$covars, ",")[[1]]
  covars.exclude <- setdiff(covar.cols, opt$covars)
  not.in.samples <- setdiff(opt$covars, covar.cols)
  if (length(not.in.samples) != 0) {
    # covars option had covariates that are not in samples file
    stop("Following covariates not in samples file:", 
         paste(not.in.samples, collapse=" "))
  }
  WriteLog("Excluding the following covariates:", 
           paste(covars.exclude, collapse="; "),
           con=log.con)
}
samples <- samples[, !colnames(samples) %in% covars.exclude]

# write missing samples
missing.inds <- setdiff(samples$indid, rownames(collapsed))
if (length(missing.inds > 0)) {
  WriteLog("The following", length(missing.inds),  
           "samples are not in the collapsed matrix and",
           "will be excluded:", paste(missing.inds, collapse=", "), 
           con=log.con)
} else {
  WriteLog("All samples present in collapsed matrix", con=log.con)
}

# calculate p-vals and write to file
WriteLog("Starting regression", con=log.con)
pvals <- NULL
null.genes <- c()
tryCatch({
  count <- 0
  for (gene in genes) {
    genos <- collapsed[gene]
    genos <- ifelse(genos > 0, 1, 0) # convert from ATAV format
    colnames(genos) <- 'gene'
    lm.data <- merge(samples, genos, by.x=1, by.y=0)[, -c(1:2)]
    gene.model <- Regress(gene, lm.data, opt$method)

    # add single quotes
    gene.out <- AddSingleQuotes(gene)

    if (!is.null(gene.model)) {
      
      model.results <- ExtractPvals(gene.model, opt$method)
      pvals.vec <- model.results[["pvals"]]
      betas.vec <- model.results[["betas"]]

      # format output file matrix
      if (is.null(pvals)) {
        WriteLog("Allocating pvals matrix", con=log.con)
        pvals.names <- names(pvals.vec)
        betas.names <- paste(names(betas.vec), "_beta", sep="")
        header <- c(pvals.names, betas.names)
        simple.cols <- c(pvals.names[1], betas.names[1])
        if (opt$count.vars) {
          if (opt$method == "logistf") {
            count.cols <- c("ctrls_fraction_rare", "cases_fraction_rare", 
                            "ctrl_enriched")
          } else if (opt$method == "linear") {
            count.cols <- c("fraction_rare")
          } else {
            stop("Unknown method:", method)
          }

          header <- c(header, count.cols)
          simple.cols <- c(simple.cols, count.cols)
        }
        
        gene.count <- length(genes)
        col.count <- length(header)
        pvals <- matrix(data=rep(NA, gene.count * col.count),
                        nrow=gene.count, ncol=col.count,
                        dimnames=list(genes=genes, covariates=header))
        rownames(pvals) <- vapply(genes, AddSingleQuotes, "")
        colnames(pvals) <- header
        WriteLog("pvals matrix allocated", con=log.con)
      }

      out.vec <- c(pvals.vec, betas.vec)
       
      if (opt$count.vars) {
        if (opt$method == "logistf") {
          # get direction
          ctrls.rare <- sum(subset(lm.data, pheno==0)$gene) / nrow(subset(lm.data, pheno==0))
          cases.rare <- sum(subset(lm.data, pheno==1)$gene) / nrow(subset(lm.data, pheno==1))
          ctrls.enriched <- ctrls.rare > cases.rare
          out.vec <- c(out.vec, ctrls.rare, cases.rare, ctrls.enriched)
        }

        if (opt$method == "linear") {
          all.rare <- sum(lm.data$gene) / nrow(lm.data)
          out.vec <- c(out.vec, all.rare)
        }
      }
      
      # add to output
      pvals[gene.out, ] <- out.vec

    } else {
      null.genes <- c(null.genes, gene.out)
    }
    

    count <- count + 1
    if (count %% 1000 == 0) {
      WriteLog(count, "genes complete out of", genes.count, "genes", 
               con=log.con)
    }
  }
  WriteLog("Run completed successfully, writing to files", con=log.con)
  if (is.null(pvals)) {
    WriteLog("No pvals extracted", con=log.con)
  } else {
    out.con <- file(opt$out.path, open="w")
    pvals <- na.omit(pvals)
    WriteLog(nrow(pvals), "pvals calculated", con=log.con)
    pvals.srt <- pvals[names(sort(pvals[, "gene"])), ]
    write.csv(pvals.srt[, simple.cols], file=out.con, quote=F)

    # write empty genes if display.none is set
    if (opt$display.none && length(null.genes) > 0) {
      null.pval.str <- paste(rep(kNoSignalPval, length(simple.cols)), collapse=",")
      for (null.gene in null.genes) {
        null.str <- paste(c(null.gene, null.pval.str), collapse=",")
        write(null.str, sep=",", file=out.con)
      }
    }

    if (!is.null(opt$detailed.path)) { # write detailed output to separate file
      out.detailed.con <- file(opt$detailed.path, open="w")
      write.csv(pvals.srt, file=out.detailed.con, quote=F)
    }

    WriteLog("Written to file", con=log.con)

    # qq plot  
    if (opt$qqplot) {
      WriteLog("Generating QQ plot", con=log.con)
      qq.path <- paste(opt$out.path, "pdf", sep=".")
      pdf(qq.path)
      QQPlotPvals(pvals.srt[, "gene"], basename(opt$samples.path))
      dev.off()
      WriteLog("QQ plot generated", con=log.con)
    }
  }
},
finally = {
  while (sink.number() != 0) {
    sink() # close all open sinks
  }
  WriteLog("run ending", con=log.con)
  closeAllConnections()
}) # end tryCatch
