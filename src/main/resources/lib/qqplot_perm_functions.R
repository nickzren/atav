QQ.plot <- function(P.perm, P.observed,addjust.xy = FALSE) {
  #do QQ plot
  e <- -log10(P.perm)
  o <- -log10(P.observed)
  
  if (addjust.xy) {
    xlim <- c(0,round(max(e) + 1))
    ylim <- c(0,round(max(o) + 1))
  } else {
    max_lim <- max(max(e), max(o)) + 1
    xlim <- c(0, max_lim)
    ylim <- c(0, max_lim)
  }
  
  def_args <- list(pch=16, xlim=xlim, ylim=ylim, 
                   xlab=expression(Expected~~-log[10](italic(p))), 
                   ylab=expression(Observed~~-log[10](italic(p)))
  )
  ## And call the plot function passing NA, your ... arguments, and the default
  ## arguments that were not defined in the ... arguments.
  tryCatch(do.call("plot", c(list(x=e, y=o), def_args)), warn=stop)
  # Add diagonal
  abline(0,1,col="red")
}

QQ.permutation <-function(sample.file, matrix.file,n.permutations = 1000, filter.list = NULL) {
  #get sample ans phenotype into
  ped <- read.table(sample.file,header = FALSE, sep = '\t', as.is = TRUE)
  ped = ped[,c(2,6)]
  rownames(ped) <- ped[,1]
  
  data <- read.table(matrix.file,header = TRUE, sep = '\t', as.is = TRUE)
  genes <- data[,1]
  
  samples <- colnames(data)
  samples <- samples[-1]
  
  #find common samples 
  samples <-  intersect(rownames(ped),colnames(data))
  
  data <-data[,samples]
  data[data > 0] <- 1
  
  ped = ped[samples,]
  is.case <- as.matrix(ped[,2] ==2)
  rownames(is.case) <- samples
 
  #convert data to matrix
  matrix <- as.matrix(data[])
  rownames(matrix)<-genes
  
  if (!is.null(filter.list)) {
    genes.common <- intersect(genes,filter.list)
    matrix <- matrix[genes.common,]
  }
  
  #Number of cases and controls
  n.samples <- length(is.case)
  n.cases <- sum(is.case)
  n.controls <- n.samples - n.cases
  
  #pre-compute all possible contingency.table for perofrmance
  #this will create a look up table
  contingency.table = matrix(0,2,2)
  max_cases= min(max(rowSums(matrix)), n.cases)
  max_ctrls = min(max(rowSums(matrix)), n.controls)
  Fisher.precompute <- matrix(0, max_cases + 1, max_ctrls + 1)
  for (i in 1: dim(Fisher.precompute)[1]) {
    for (j in 1:dim(Fisher.precompute)[2]) {
      contingency.table[1,1] <- i-1
      contingency.table[1,2] <- n.cases - contingency.table[1,1]
      contingency.table[2,1] <- j -1
      contingency.table[2,2] <- n.controls - contingency.table[2,1]
      Fisher.precompute[i,j] <- min(fisher.test(contingency.table)$p.value,1.0)
      
    }
  }
  
  #permutaiton, save all p-values just in case median will be needed later on
  P.Values <- matrix(1,dim(matrix)[1],n.permutations)
  total.1 <- rowSums(matrix)
  for (i in 1: n.permutations) {
    K <- sample.int(n.samples, size = n.cases, replace = FALSE)  
    Labels.1.1 <- rowSums(matrix[,K])
    Labels.0.1 <- total.1 - Labels.1.1
    P.Values[,i] <- sort(Fisher.precompute[cbind(Labels.1.1+1,Labels.0.1+1)])  
  }
  P.perm <- rowMeans(P.Values)
  
  
  #compute observed p values
  K <- which(is.case)
  Labels.1.1 <- rowSums(matrix[,K])
  Labels.0.1 <- total.1 - Labels.1.1
  P.observed <- sort(Fisher.precompute[cbind(Labels.1.1+1,Labels.0.1+1)])
  
  out <- list()
  out$perm <- P.perm
  out$observed <- P.observed
  out
}


estlambda.permutation <-function (p.o, p.e, plot = FALSE, filter = TRUE, addjust.xy = FALSE, ...) {
  p.o <- p.o[which(!is.na(p.o))]
  p.e <- p.e[which(!is.na(p.e))]
  ntp <-length(p.o)
  if (ntp != length(p.e)) {
    stop("data does not match")
  }
  if (ntp == 1) {
    warning(paste("One measurement, lambda = 1 returned"))
    return(list(estimate = 1, se = 999.99))
  }
  if (ntp < 10) 
    warning(paste("number of points is too small:", ntp))
  
  p.o[p.o>1] = 1
  p.e[p.e>1] = 1

  p.o <- qchisq(p.o, 1, lower.tail = FALSE)
  p.e <- qchisq(p.e, 1, lower.tail = FALSE)
  
  
  if (filter) {
    to.be.removed <- which((abs(p.o) < 1e-08) | (abs(p.e) < 1e-08))
    p.o[to.be.removed] <- NA
    p.e[to.be.removed] <- NA
  }
  
  p.o <- sort(p.o)
  p.e <- sort(p.e)

  out <- list()
    
  s <- summary(lm(p.o ~ 0 + p.e))$coeff
  out$estimate <- s[1, 1]
  out$se <- s[1, 2]
  
  if (plot) {
    lim <- c(0, max(p.o, p.e, na.rm = TRUE))
    if (addjust.xy) {
      xlim <- c(0,round(max(p.e) + 1))
      ylim <- c(0,round(max(p.o) + 1))
    } else {
      max_lim <- max(max(p.e), max(p.o)) + 1
      xlim <- c(0, max_lim)
      ylim <- c(0, max_lim)
    }
    
    plot(p.e, p.o, xlab = expression("Expected " ~ chi^2), 
         ylab = expression("Observed " ~ chi^2), pch=16,xlim = xlim, ylim = ylim,...)
    abline(a = 0, b = 1)
    abline(a = 0, b = out$estimate, col = "red")
    rp = vector('expression',1)
    if (out$estimate >= 1.0) {
      dig<- 5
    } else {
      dig<- 4
    }
    rp[1] = substitute(expression(lambda == MYVALUE), 
                       list(MYVALUE = format(out$estimate,dig=dig)))[2]
    legend('top', legend = rp, bty = 'n')
  }
  out
}
