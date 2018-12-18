#Script:	IGM Sequencing Clinic diagnostic non-trio post-ATAV Script
#Version:	0.5
#Designed:	Nick Stong, Slavé Petrovski, David B. Goldstein
#Developed:	Nick Stong, Slavé Petrovski, Quanli Wang
#Last Update:	2016-07-01
#Institute:	IGM (Institute for Genomic Medicine)



normalized.name <- function(names) {
  names.new <- gsub("[=+( )#&-/]",".",names)
  for (i in 1:length(names)) {
    suppressWarnings(ind <- as.numeric(substr(names.new[i],1,1)))

    if (!is.na(ind)) {
      names.new[i] <- paste("X",names.new[i],sep = "")
    }
  }
  names.new
}

Filter.by.Effect <- function(x) {
return ((x=="stop_gained") | (x =="stop_lost") | (x =="start_lost")
      | (x=="frameshift_variant") | (x== "splice_donor_variant") | (x=="splice_acceptor_variant") |   (x=="exon_loss_variant"))
}

Filter.by.Effect.exclude <- function(x) {
  return ((x!= "synonymous_variant") & (x !="splice_region_variant"))
}


Filter.for.tier2.general <- function(data) {

  #check for correct columns
  columns <- c("HGMD Class","ClinVar ClinSig",
               "Effect","HGMD indel 9bpflanks","ClinVar pathogenic indels",
               "ClinGen","ClinVar Pathogenic Indel Count",
               "Clinvar Pathogenic CNV Count","ClinVar Pathogenic SNV Splice Count",
               "ClinVar Pathogenic SNV Nonsense Count"
               )

  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier2 general")}

  #inclusion rule 1:
  R1 <- sapply(data[normalized.name("HGMD Class")], as.character)
  R1 <- sapply(R1, function(x) length(grep("DM",x)) > 0)

  #inclusion rule 2:
  temp <- sapply(data[normalized.name("ClinVar ClinSig")], as.character)
  temp[is.na(temp)] <- 'NA'
  R2 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

  Functional <- Filter.by.Effect(sapply(data[normalized.name("Effect")], as.character))

  #inclusion rule 3:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar pathogenic indels")], as.numeric))
  temp[is.na(temp)] <- 0
  R3 <- Functional & (temp > 0)

  #inclusion rule 4:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinGen")], as.numeric))
  temp[is.na(temp)] <- 0
  R4 <- Functional & (temp > 0)

  #inclusion rule 5:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic Indel Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R5 <- Functional & (temp > 0)

  #inclusion rule 6:
  suppressWarnings(temp <- sapply(data[normalized.name("Clinvar Pathogenic CNV Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R6 <- Functional & (temp > 0)

  #inclusion rule 7:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Splice Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R7 <- Functional & (temp > 0)

  #inclusion rule 8:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Nonsense Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R8 <- Functional & (temp > 0)

  R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8
  data <- data[R.all,]
  data
}

Parse.Allele.Count.Exac <- function(s) {
  if (is.na(s)) {
    return(0)
  } else {
    has.4.genotypes <- (length(grep("NA",s))>0)
    fields <- strsplit(gsub("NA/","",gsub("'","",s)),"/")[[1]]
    fields <- as.numeric(fields)
    if (has.4.genotypes) { #NA/heterozygous/hemizygous/homozygous
      return (fields[1] + fields[2] + 2* fields[3])
    } else { #hom-ref/het/hom
      return (min(2*fields[1]+fields[2], fields[2]+ 2* fields[3]))
    }
  }
}
Parse.Allele.Count.EVS <- function(s) {
  if (is.na(s)) {
    return(0)
  } else {
    fields <- strsplit(s,"/")[[1]]
    if (length(grep('R',s)) == 0) { #snv, change the string so it has the same format as Indels
      alt.allele <- substr(s,1,1)
      s <- gsub(alt.allele,"_",s) # make it a non alpha first
      s <- gsub("[[:alpha:]]","R",s) # change the other one to ref
      s <- gsub("_","A",s) # change the alt to "A".
      fields <- strsplit(s,"/")[[1]] #split again since s has been normlized
    }

    hom.allele.count <- 0
    het.allele.count <- 0
    hom.ref.allele.count <- 0
    for (i in 1: length(fields)) {
      current.record <- strsplit(fields[i],"=")[[1]]
      current.type = gsub("[[:digit:]]","",current.record[1]) #works for indels only, no effect on snv
      current.count <- as.numeric(current.record[2])
      if (length(grep("RR",current.type)) > 0) {
        hom.ref.allele.count <- hom.ref.allele.count + 2 * current.count
      } else if (length(grep("AA",current.type)) > 0) {
        hom.allele.count <- hom.allele.count + 2 * current.count
      } else if (length(grep("AR",current.type)) > 0){
        het.allele.count <- het.allele.count + current.count
      } else if (length(grep("R",current.type)) > 0) {
        hom.ref.allele.count <- hom.ref.allele.count + current.count
      } else { # A
        hom.allele.count <- hom.allele.count + current.count
      }
    }
    return (min(het.allele.count + hom.allele.count, het.allele.count +  hom.ref.allele.count ))
  }
}

Filter.by.Allele.Count <- function(data, threshold) {
  if (dim(data)[1] ==0) { return(data)}
  #Get IGM allele count
  stopifnot(length(which(colnames(data) == "Hom.Ref.Ctrl"))>0)
  stopifnot(length(which(colnames(data) == "Het.Ctrl"))>0)
  stopifnot(length(which(colnames(data) == "Hom.Ctrl"))>0)
  AlleleCount <- cbind(2*data["Hom.Ref.Ctrl"] + data["Het.Ctrl"], data["Het.Ctrl"] + 2*data["Hom.Ctrl"] )
  AlleleCount <- apply(AlleleCount,1,function(x) min(x))

  #get Exac allele count
  stopifnot(length(which(colnames(data) == "Evs.All.Genotype.Count"))>0)

  tmp <- sapply(data["Evs.All.Genotype.Count"], as.character)
  EVS.allelecount <- sapply(tmp, Parse.Allele.Count.EVS)

  tmp <- sapply(data["ExAC.global.gts"], as.character)
  Exac.allelecount <- sapply(tmp, Parse.Allele.Count.Exac)

  QC.fail.ctrl <- sapply(data["QC.Fail.Ctrl"], as.numeric)
  All <- ((AlleleCount + EVS.allelecount + Exac.allelecount + QC.fail.ctrl) <= threshold)
  result <- data[All,]
}

fun.12 <- function(x.1,x.2,...) {
  x.1p <- do.call("paste", x.1)
  x.2p <- do.call("paste", x.2)
  x.1[! x.1p %in% x.2p, ]
}

Filter.for.tier2.pdnm.kv <- function(data) {

  #check for correct columns
  columns <- c("ExAC global gts","MQ", "QD", "Qual","DP",
               "Percent Alt Read","Effect", "HGMD Class", "HGMDm2site",
               "HGMDm1site", "HGMDp1site", "HGMDp2site", "HGMD indel 9bpflanks", "ClinVar pathogenic indels",
               "ClinVar ClinSig"
  )
  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier 2 pdnm kv")}
  #Exclusion rule 2:
  #Index <- is.na(data[normalized.name("ExAC global gts")])
  #data <- data[Index,]

  #Exclusion rule 3:
  suppressWarnings(temp <- sapply(data[normalized.name("MQ")], as.numeric))
  temp[is.na(temp)] <- 0
  ER31 <- temp < 40

  suppressWarnings(temp <- sapply(data[normalized.name("QD")], as.numeric))
  temp[is.na(temp)] <- 0
  ER32 <- temp < 2

  suppressWarnings(temp <- sapply(data[normalized.name("Qual")], as.numeric))
  temp[is.na(temp)] <- 0
  ER33 <- temp < 30

  suppressWarnings(temp <- sapply(data[normalized.name("DP")], as.numeric))
  temp[is.na(temp)] <- 0
  ER34 <- temp < 3

  ER3 <- !(ER31 | ER32 | ER33 | ER34)
  data <- data[ER3,]

  #Exclusion rule 4:
  suppressWarnings(temp <- sapply(data[normalized.name("Percent Alt Read")], as.numeric))
  temp[is.na(temp)] <- 0
  ER4 <- temp >= 0.2
  data <- data[ER4,]

  #Exclusion rule 5:
  Functional  <-Filter.by.Effect.exclude(sapply(data[normalized.name("Effect")], as.character))
  data <- data[Functional,]
  if (dim(data)[1] ==0) { return(data)}

  #inclusion rule 1:
  R1 <- sapply(data[normalized.name("HGMD Class")], as.character)
  R1 <- sapply(R1, function(x) length(grep("DM",x)) > 0)

  #inclusion rule 2:
  temp<- sapply(data[normalized.name("HGMDm2site")], as.character)
  R21 <- is.na(temp)
  temp<- sapply(data[normalized.name("HGMDm1site")], as.character)
  R22 <- is.na(temp)
  temp<- sapply(data[normalized.name("HGMDp1site")], as.character)
  R23 <- is.na(temp)
  temp<- sapply(data[normalized.name("HGMDp2site")], as.character)
  R24 <- is.na(temp)
  R2 <- !(R21 & R22 & R23 & R24)


  #inclusion rule 3:
  suppressWarnings(temp <- sapply(data[normalized.name("HGMD indel 9bpflanks")], as.numeric))
  temp[is.na(temp)] <- 0
  R31 <- temp > 0

  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar pathogenic indels")], as.numeric))
  temp[is.na(temp)] <- 0
  R32 <- temp > 0
  R3 <- R31 | R32

  #inclusion rule 4:
  temp <- sapply(data[normalized.name("ClinVar ClinSig")], as.character)
  temp[is.na(temp)] <- 'NA'
  R4 = (temp == "Pathogenic") | (temp == "Likely Pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

  R.all <- R1 | R2 | R3 | R4
  data <- data[R.all,]
  data
}

Filter.for.tier2.pdnm.lof <- function(data) {

  #check for correct columns
  columns <- c("Effect", "HGMD Class", "HGMD indel 9bpflanks", "ClinVar pathogenic indels",
               "ClinVar ClinSig","ClinGen", "ClinVar Pathogenic Indel Count",
               "ClinVar Pathogenic SNV Splice Count","ClinVar Pathogenic SNV Nonsense Count",
               "Clinvar Pathogenic CNV Count")

  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier 2 pdnm lof")}

  fun.include <- function(x) {
    return ((x=="stop_gained") | (x =="stop_lost") | (x =="start_lost") | (x=="frameshift_variant") 
            | (x== "splice_donor_variant") | (x=="splice_acceptor_variant") 
            | (x=="exon_loss_variant"))
    }

  #Exclusion rule 2:
  Functional  <-fun.include(sapply(data[normalized.name("Effect")], as.character))
  data <- data[Functional,]
  if (dim(data)[1] ==0) { return(data)}

  #inclusion rule 1:
  R1 <- sapply(data[normalized.name("HGMD Class")], as.character)
  R1 <- sapply(R1, function(x) length(grep("DM",x)) > 0)


  #inclusion rule 2:
  suppressWarnings(temp <- sapply(data[normalized.name("HGMD indel 9bpflanks")], as.numeric))
  temp[is.na(temp)] <- 0
  R21 <- temp > 0

  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar pathogenic indels")], as.numeric))
  temp[is.na(temp)] <- 0
  R22 <- temp > 0
  R2 <- R21 | R22

  #inclusion rule 3:
  temp <- sapply(data[normalized.name("ClinVar ClinSig")], as.character)
  temp[is.na(temp)] <- 'NA'
  R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

  #inclusion rule 4:
  R4 <- (sapply(data[normalized.name("ClinGen")], as.character) == '1')

  #inclusion rule 5:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic Indel Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R5 <-  (temp > 0)

  #inclusion rule 6:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Splice Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R6 <- (temp > 0)

  #inclusion rule 7:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Nonsense Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R7 <- (temp > 0)

  #inclusion rule 8:
  suppressWarnings(temp <- sapply(data[normalized.name("Clinvar Pathogenic CNV Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R8 <-  (temp > 0)

  R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8
  data <- data[R.all,]
  data
}
Filter.for.tier2.pdnm.lof.depl <- function(data) {

  #check for correct columns
  columns <- c("Effect", "HGMD Class", "HGMD indel 9bpflanks", "ClinVar pathogenic indels",
               "ClinVar ClinSig","ClinGen", "ClinVar Pathogenic Indel Count",
               "ClinVar Pathogenic SNV Splice Count","ClinVar Pathogenic SNV Nonsense Count",
               "Clinvar Pathogenic CNV Count")

  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier 2 pdnm lof depl")}

  fun.include <- function(x) {
    return ((x=="stop_gained") | (x =="stop_lost") | (x =="start_lost") | (x=="frameshift_variant") 
            | (x== "splice_donor_variant") | (x=="splice_acceptor_variant") 
            | (x=="exon_loss_variant"))
  }

  #Exclusion rule 2:
  Functional  <-fun.include(sapply(data[normalized.name("Effect")], as.character))
  data <- data[Functional,]
  if (dim(data)[1] ==0) { return(data)}

  data <- data[which(data$LoF.FDR.ExAC. < 0.01 | data$LoF.pLI.ExAC. >= 0.9 | data$LoF.pRec.ExAC. >= 0.9),]
  data
}

Parse.HemiHomo.Count.EVS <- function(s) {
  if (is.na(s)) {
    return(0)
  } else {
    fields <- strsplit(s,"/")[[1]]
    if (length(grep('R',s)) == 0) { #snv, change the string so it has the same format as Indels
      alt.allele <- substr(s,1,1)
      s <- gsub(alt.allele,"_",s) # make it a non alpha first
      s <- gsub("[[:alpha:]]","R",s) # change the other one to ref
      s <- gsub("_","A",s) # change the alt to "A".
      fields <- strsplit(s,"/")[[1]] #split again since s has been normlized
    }
    hemi.hom.count <- 0
    hemi.hom.count2 <- 0
    for (i in 1: length(fields)) {
      current.record <- strsplit(fields[i],"=")[[1]]
      current.type = gsub("[[:digit:]]","",current.record[1]) #works for indels only, no effect on snv
      current.count <- as.numeric(current.record[2])
      if (length(grep("R",current.type)) == 0) { #counting only AA or A
        hemi.hom.count <- hemi.hom.count +  current.count
      }
      if (length(grep("A",current.type)) == 0) { #counting only RR or R
        hemi.hom.count2 <- hemi.hom.count2 +  current.count
      }
    }
    if (hemi.hom.count2 < hemi.hom.count) {
      hemi.hom.count <- hemi.hom.count2
    }
    return(hemi.hom.count)
  }
}

Parse.HemiHomo.Count.Exac <- function(s) {
  if (is.na(s)) {
    return(0)
  } else {
    has.4.genotypes <- (length(grep("NA",s))>0)
    fields <- strsplit(gsub("NA/","",gsub("'","",s)),"/")[[1]]
    fields <- as.numeric(fields)
    if (has.4.genotypes) { #NA/heterozygous/hemizygous/homozygous
      return (fields[2] + min(fields[3],fields[1]))
    } else { #hom-ref/het/hom
      return(min(fields[3],fields[1]))
    }
  }
}

Filter.by.HemiHomo.Count <- function(data,threshold,is.comphet = FALSE) {
  #Get IGM genotype count
  if (is.comphet) {
    stopifnot(length(which(colnames(data) == normalized.name("Hom Ref Ctrl (#1)")))>0)
    stopifnot(length(which(colnames(data) ==  normalized.name("Hom Ctrl (#1)")))>0)
    GenoTypeCount.1 <- cbind(data[normalized.name("Hom Ref Ctrl (#1)")] , data[normalized.name("Hom Ctrl (#1)")] )
    GenoTypeCount.1 <- apply(GenoTypeCount.1,1,function(x) min(x))

    stopifnot(length(which(colnames(data) == normalized.name("Hom Ref Ctrl (#2)")))>0)
    stopifnot(length(which(colnames(data) ==  normalized.name("Hom Ctrl (#2)")))>0)
    GenoTypeCount.2 <- cbind(data[normalized.name("Hom Ref Ctrl (#2)")] , data[normalized.name("Hom Ctrl (#2)")] )
    GenoTypeCount.2 <- apply(GenoTypeCount.2,1,function(x) min(x))

    #get EVS GT count
    column.name = normalized.name("Evs.All.Genotype.Count (#1)")
    stopifnot(length(which(colnames(data) == column.name))>0)
    tmp <- sapply(data[column.name], as.character)
    EVS.GenoTypeCount.1<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    column.name = normalized.name("Evs.All.Genotype.Count (#2)")
    stopifnot(length(which(colnames(data) == column.name))>0)
    tmp <- sapply(data[column.name], as.character)
    EVS.GenoTypeCount.2<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    #get Exac GT count
    column.name = normalized.name("ExAC.global.gts (#1)")
    tmp <- sapply(data[column.name], as.character)
    Exac.GenoTypeCount.1 <- sapply(tmp, Parse.HemiHomo.Count.Exac)

    column.name = normalized.name("ExAC.global.gts (#2)")
    tmp <- sapply(data[column.name], as.character)
    Exac.GenoTypeCount.2 <- sapply(tmp, Parse.HemiHomo.Count.Exac)

    All <- ((GenoTypeCount.1 + EVS.GenoTypeCount.1 + Exac.GenoTypeCount.1) <= threshold)
    All <- All & ((GenoTypeCount.2 + EVS.GenoTypeCount.2 + Exac.GenoTypeCount.2) <= threshold)
    #All <- (GenoTypeCount.2 + EVS.GenoTypeCount.2 + Exac.GenoTypeCount.2 + GenoTypeCount.1 + EVS.GenoTypeCount.1 + Exac.GenoTypeCount.1) <= threshold
  } else {
    stopifnot(length(which(colnames(data) == "Hom.Ref.Ctrl"))>0)
    stopifnot(length(which(colnames(data) == "Hom.Ctrl"))>0)
    GenoTypeCount <- cbind(data["Hom.Ref.Ctrl"] , data["Hom.Ctrl"] )
    GenoTypeCount <- apply(GenoTypeCount,1,function(x) min(x))

    #get EVS GT count
    stopifnot(length(which(colnames(data) == "Evs.All.Genotype.Count"))>0)
    tmp <- sapply(data["Evs.All.Genotype.Count"], as.character)
    EVS.GenoTypeCount<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    #get Exac GT count
    stopifnot(length(which(colnames(data) == "ExAC.global.gts"))>0)
    tmp <- sapply(data["ExAC.global.gts"], as.character)
    Exac.GenoTypeCount <- sapply(tmp, Parse.HemiHomo.Count.Exac)

    All <- ((GenoTypeCount + EVS.GenoTypeCount + Exac.GenoTypeCount) <= threshold)
  }

  result <- data[All,]
}

Filter.for.tier2.prec.kv.lof <- function(data) {

  #check for correct columns
  columns <- c("GT","Effect", "HGMD Class", "HGMD indel 9bpflanks", "ClinVar pathogenic indels",
               "ClinVar ClinSig","ClinGen", "ClinVar Pathogenic Indel Count",
               "ClinVar Pathogenic SNV Splice Count","ClinVar Pathogenic SNV Nonsense Count",
               "Clinvar Pathogenic CNV Count")

  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier 2 prec kv")}

  #rule 1
  R1 <- data[normalized.name("GT")] == "hom" | data[normalized.name("GT")]=="hom ref"
  data <- data[R1,]

  fun.include <- function(x) {
    return (!((x== "synonymous_variant") | (x =="splice_region_variant")))
  }

  #rule 2:
  Functional  <-fun.include(sapply(data[normalized.name("Effect")], as.character))
  data <- data[Functional,]
  if (dim(data)[1] ==0) { return(data)}


  #inclusion rule 1:
  R1 <- sapply(data[normalized.name("HGMD Class")], as.character)
  R1 <- sapply(R1, function(x) length(grep("DM",x)) > 0)


  #inclusion rule 2:
  suppressWarnings(temp <- sapply(data[normalized.name("HGMD indel 9bpflanks")], as.numeric))
  temp[is.na(temp)] <- 0
  R21 <- temp > 0 

  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar pathogenic indels")], as.numeric))
  temp[is.na(temp)] <- 0
  R22 <- temp > 0
  R2 <- R21 | R22

  #inclusion rule 3:
  temp <- sapply(data[normalized.name("ClinVar ClinSig")], as.character)
  temp[is.na(temp)] <- 'NA'
  R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")


  fun.functional <- function(x) {
    return ((x=="stop_gained") | (x =="stop_lost") | (x =="start_lost") | (x=="frameshift_variant") 
            | (x== "splice_donor_variant") | (x=="splice_acceptor_variant") 
            | (x=="exon_loss_variant"))
  }
  Functional  <-fun.functional(sapply(data[normalized.name("Effect")], as.character))
  #data <- data[Functional,]
  #if (dim(data)[1] ==0) { return(data)}

  #inclusion rule 4:
  R4 <- Functional & (sapply(data[normalized.name("ClinGen")], as.character) == '1')

  #inclusion rule 5:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic Indel Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R5 <-  Functional & (temp > 0)

  #inclusion rule 6:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Splice Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R6 <- Functional & (temp > 0)

  #inclusion rule 7:
  suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Nonsense Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R7 <- Functional & (temp > 0)

  #inclusion rule 8:
  suppressWarnings(temp <- sapply(data[normalized.name("Clinvar Pathogenic CNV Count")], as.numeric))
  temp[is.na(temp)] <- 0
  R8 <-  Functional & (temp > 0)

  R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8
  data <- data[R.all,]
  data
}

Filter.for.ClinVarExact <- function(data) {

  #check for correct columns
  columns <- c("ExAC global gts","MQ", "QD", "Qual","DP",
               "Percent Alt Read","Effect", "HGMD Class", "HGMDm2site",
               "HGMDm1site", "HGMDp1site", "HGMDp2site", "HGMD indel 9bpflanks", "ClinVar pathogenic indels",
               "ClinVar ClinSig"
  )
  #make sure all columns are present
  if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at ClinVar Exact")}
  #Exclusion rule 2:
  if (dim(data)[1] ==0) { return(data)}
  Index <- is.na(data[normalized.name("ExAC global af")]) | data[normalized.name("ExAC global af")] < 0.01
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}
  

  #Exclusion rule 3:
  if (dim(data)[1] ==0) { return(data)}
  suppressWarnings(temp <- sapply(data[normalized.name("MQ")], as.numeric))
  temp[is.na(temp)] <- 0
  ER31 <- temp < 40

  suppressWarnings(temp <- sapply(data[normalized.name("QD")], as.numeric))
  temp[is.na(temp)] <- 0
  ER32 <- temp < 2

  suppressWarnings(temp <- sapply(data[normalized.name("Qual")], as.numeric))
  temp[is.na(temp)] <- 0
  ER33 <- temp < 30

  suppressWarnings(temp <- sapply(data[normalized.name("DP")], as.numeric))
  temp[is.na(temp)] <- 0
  ER34 <- temp < 3

  ER3 <- !(ER31 | ER32 | ER33 | ER34)
  data <- data[ER3,]

  #Exclusion rule 4:
  suppressWarnings(temp <- sapply(data[normalized.name("Percent Alt Read")], as.numeric))
  temp[is.na(temp)] <- 0
  ER4 <- temp >= 0.2
  data <- data[ER4,]

  #Exclusion rule 5:
  ER5 <- !grepl("?Site",data['ClinVar.DiseaseName'][,])
  data <- data[ER5,]

  #inclusion rule 4:
  temp <- sapply(data[normalized.name("ClinVar ClinSig")], as.character)
  temp[is.na(temp)] <- 'NA'
  R4 = (temp == "Pathogenic") | (temp == "Likely Pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

  R.all <- R4
  data <- data[R.all,]
  data
}

