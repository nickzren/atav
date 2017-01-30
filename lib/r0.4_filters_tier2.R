#Script:	IGM Sequencing Clinic Tier 2 Philosopy post-ATAV Script
#Version:	0.3
#Designed:	Slav� Petrovski, David B. Goldstein
#Developed:	Slav� Petrovski, Quanli Wang
#Last Update:	2016-07-01
#Institute:	IGM (Institute for Genomic Medicine)


Filter.by.DenovoFlag <- function(data, type, strict = FALSE) {
  #check for column name
  stopifnot(length(which(colnames(data) == "Denovo.Flag"))>0)
  if (strict) {
    result <- data[data["Denovo.Flag"] == type,]
  } else {
    result <- data[grep(type, data$"Denovo.Flag") ,] 
  }
  # put NA rows back if any
  result = rbind(result,data[data["Denovo.Flag"] == "NA",])
}

Filter.by.CompHetFlag <- function(data, type, strict = FALSE) {
  #check for column name
  stopifnot(length(which(colnames(data) == "Comp.Het.Flag"))>0)
  if (strict) {
    result <- data[data["Comp.Het.Flag"] == type,]
  } else {
    result <- data[grep(type, data$"Comp.Het.Flag") ,] 
  }
  # put NA rows back if any
  result = rbind(result,data[data["Comp.Het.Flag"] == "NA",])
}

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

Filter.for.compound.heterozygote <- function(data) {
  #check for correct columns, just in case the format has been changed
  columns <- c("Var Ctrl Freq #1 & #2 (co-occurance)","Genotype (mother) (#1)",
               "Genotype (father) (#1)","Genotype (mother) (#2)","Genotype (father) (#2)",
                "Function (#1)","Function (#2)",
               "Ctrl MAF (#1)", "Evs Eur Maf (#1)", "Evs Afr Maf (#1)",
               "ExAC global maf (#1)", "ExAC afr maf (#1)", "ExAC amr maf (#1)",
               "ExAC eas maf (#1)","ExAC sas maf (#1)","ExAC fin maf (#1)",
               "ExAC nfe maf (#1)","ExAC oth maf (#1)",
               "Ctrl MAF (#2)", "Evs Eur Maf (#2)", "Evs Afr Maf (#2)",
               "ExAC global maf (#2)", "ExAC afr maf (#2)", "ExAC amr maf (#2)",
               "ExAC eas maf (#2)","ExAC sas maf (#2)","ExAC fin maf (#2)",
               "ExAC nfe maf (#2)","ExAC oth maf (#2)",
               "Minor Hom Ctrl (#1)","Evs All Genotype Count (#1)","ExAC global gts (#1)",
               "Evs Filter Status (#1)","ExAC global gts (#1)",
               "Minor Hom Ctrl (#2)","Evs All Genotype Count (#2)","ExAC global gts (#2)",
               "Evs Filter Status (#2)","ExAC global gts (#2)"
              )

  #make sure all columns are present
  stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

  #step 1:
  data   <- data[is.na(data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")])
                 | data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")] == 0,]
  if (dim(data)[1] ==0) { return(data)}

  #step 2:
  data <- data[(is.na(data[normalized.name("Genotype (mother) (#1)")])
                | data[normalized.name("Genotype (mother) (#1)")] != "hom")          &
                (is.na(data[normalized.name("Genotype (father) (#1)")])
                | data[normalized.name("Genotype (father) (#1)")] != "hom")          &
                (is.na(data[normalized.name("Genotype (mother) (#2)")])
                | data[normalized.name("Genotype (mother) (#2)")] != "hom")          &
                (is.na(data[normalized.name("Genotype (father) (#2)")])
                | data[normalized.name("Genotype (father) (#2)")] != "hom")
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 3:
  Index <- grep("^SYNONYMOUS",data[normalized.name("Function (#1)")][,1])
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  Index <- grep("^INTRON_EXON",data[normalized.name("Function (#1)")][,1])
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  Index <- grep("^SYNONYMOUS",data[normalized.name("Function (#2)")][,1])
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  Index <- grep("^INTRON_EXON",data[normalized.name("Function (#2)")][,1])
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}


  #step 4:
  data <- data[(is.na(data[normalized.name("Ctrl MAF (#1)")])
                | data[normalized.name("Ctrl MAF (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Eur Maf (#1)")])
                  | data[normalized.name("Evs Eur Maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Afr Maf (#1)")])
                  | data[normalized.name("Evs Afr Maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC global maf (#1)")])
                  | data[normalized.name("ExAC global maf (#1)")] < 0.01)    &
                 (is.na(data[normalized.name("ExAC afr maf (#1)")])
                  | data[normalized.name("ExAC afr maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC amr maf (#1)")])
                  | data[normalized.name("ExAC amr maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC eas maf (#1)")])
                  | data[normalized.name("ExAC eas maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC sas maf (#1)")])
                  | data[normalized.name("ExAC sas maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC fin maf (#1)")])
                  | data[normalized.name("ExAC fin maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC nfe maf (#1)")])
                  | data[normalized.name("ExAC nfe maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC oth maf (#1)")])
                  | data[normalized.name("ExAC oth maf (#1)")] < 0.01)        &
                 (is.na(data[normalized.name("Ctrl MAF (#2)")])
                  | data[normalized.name("Ctrl MAF (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Eur Maf (#2)")])
                  | data[normalized.name("Evs Eur Maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Afr Maf (#2)")])
                  | data[normalized.name("Evs Afr Maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC global maf (#2)")])
                  | data[normalized.name("ExAC global maf (#2)")] < 0.01)    &
                 (is.na(data[normalized.name("ExAC afr maf (#2)")])
                  | data[normalized.name("ExAC afr maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC amr maf (#2)")])
                  | data[normalized.name("ExAC amr maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC eas maf (#2)")])
                  | data[normalized.name("ExAC eas maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC sas maf (#2)")])
                  | data[normalized.name("ExAC sas maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC fin maf (#2)")])
                  | data[normalized.name("ExAC fin maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC nfe maf (#2)")])
                  | data[normalized.name("ExAC nfe maf (#2)")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC oth maf (#2)")])
                  | data[normalized.name("ExAC oth maf (#2)")] < 0.01)
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 5:
  data <- data[(is.na(data[normalized.name("Minor Hom Ctrl (#1)")])
                | data[normalized.name("Minor Hom Ctrl (#1)")] == 0),]
  if (dim(data)[1] ==0) {return(data)}

  Index <- is.na(data[normalized.name("Evs All Genotype Count (#1)")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <-as.character(data[i,normalized.name("Evs All Genotype Count (#1)")])
      not.done <- (length(grep("A1A1=0",str))==0) & (length(grep("A2A2=0",str))==0) &
        (length(grep("A3A3=0",str))==0)

      if (not.done) { #not found
        genotypes <- strsplit(str,"/")[[1]]
        genotype.count <- strsplit(genotypes,"=")[[1]][2]
        not.done <- genotype.count == "0"
      }
      if (not.done) { #the last condition has been checked
        Index[i] <- TRUE
      }
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  Index <- is.na(data[normalized.name("ExAC global gts (#1)")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <- as.character(data[i,normalized.name("ExAC global gts (#1)")])
      not.done <- !is.na(str)
      if (not.done) {
        not.done <- (length(grep("/0'$",str))>0)
      }

      if (not.done) {
        #print(str)
        genotype.count <- strsplit(str,"/")[[1]]
        if ((length(genotype.count) ==4) & (length(grep("NA",genotype.count[1])) >0)) {
          not.done <- (genotype.count[3] == "0") & (length(grep("^0",genotype.count[4])) >0)
        }
      }
    }
    if (not.done) { #the last condition has been checnke
      Index[i] <- TRUE
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  data <- data[((is.na(data[normalized.name("Evs Filter Status (#1)")])
                  | data[normalized.name("Evs Filter Status (#1)")] != "FAIL"))
               ,]
  if (dim(data)[1] ==0) { return(data)}

  data <- data[(is.na(data[normalized.name("Minor Hom Ctrl (#2)")])
                | data[normalized.name("Minor Hom Ctrl (#2)")] == 0),]
  if (dim(data)[1] ==0) {return(data)}

  Index <- is.na(data[normalized.name("Evs All Genotype Count (#2)")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <-as.character(data[i,normalized.name("Evs All Genotype Count (#2)")])
      not.done <- (length(grep("A1A1=0",str))==0) & (length(grep("A2A2=0",str))==0) &
        (length(grep("A3A3=0",str))==0)

      if (not.done) { #not found
        genotypes <- strsplit(str,"/")[[1]]
        genotype.count <- strsplit(genotypes,"=")[[1]][2]
        not.done <- genotype.count == "0"
      }
      if (not.done) { #the last condition has been checked
        Index[i] <- TRUE
      }
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  Index <- is.na(data[normalized.name("ExAC global gts (#2)")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <- as.character(data[i,normalized.name("ExAC global gts (#2)")])
      not.done <- !is.na(str)
      if (not.done) {
        not.done <- (length(grep("/0'$",str))>0)
      }

      if (not.done) {
        #print(str)
        genotype.count <- strsplit(str,"/")[[1]]
        if ((length(genotype.count) ==4) & (length(grep("NA",genotype.count[1])) >0)) {
          not.done <- (genotype.count[3] == "0") & (length(grep("^0",genotype.count[4])) >0)
        }
      }
    }
    if (not.done) { #the last condition has been checnke
      Index[i] <- TRUE
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  data <- data[((is.na(data[normalized.name("Evs Filter Status (#2)")])
                 | data[normalized.name("Evs Filter Status (#2)")] != "FAIL"))
                ,]
  data
}

Filter.for.homozygous <- function(data) {
  #check for correct columns
  columns <- c("Percent Read Alt (child)","Genotype Qual GQ (child)",
               "Rms Map Qual MQ (child)","Evs Filter Status",
               "Samtools Raw Coverage (mother)", "Genotype (mother)", "Genotype (father)",
               "Function", "Ctrl MAF", "Evs Eur Maf", "Evs Afr Maf","ExAC global maf",
               "ExAC afr maf","ExAC amr maf","ExAC eas maf","ExAC sas maf",
               "ExAC fin maf","ExAC nfe maf","ExAC oth maf","Minor Hom Ctrl",
               "Evs All Genotype Count", "ExAC global gts")

  #make sure all columns are present
  stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

  #step 1:
  data   <- data[is.na(data[normalized.name("Percent Read Alt (child)")])
                 | data[normalized.name("Percent Read Alt (child)")] > 0.8,]
  if (dim(data)[1] ==0) { return(data)}
  #step 2:
  data <- data[(is.na(data[normalized.name("Genotype Qual GQ (child)")])
                | data[normalized.name("Genotype Qual GQ (child)")] > 20)        &
                 (is.na(data[normalized.name("Rms Map Qual MQ (child)")])
                  | data[normalized.name("Rms Map Qual MQ (child)")] > 40)       &
                 (is.na(data[normalized.name("Evs Filter Status")])
                  | data[normalized.name("Evs Filter Status")] != "FAIL")
               ,]
  if (dim(data)[1] ==0) { return(data)}
  #step 3:
  data <- data[(is.na(data[normalized.name("Genotype (mother)")])
                  | data[normalized.name("Genotype (mother)")] == "het")          &
                 (is.na(data[normalized.name("Genotype (father)")])
                  | data[normalized.name("Genotype (father)")] == "het")
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 4:
  Index <- grep("^SYNONYMOUS",data$"Function")
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  Index <- grep("^INTRON_EXON",data$"Function")
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  #step 5:
  data <- data[(is.na(data[normalized.name("Ctrl MAF")])
                | data[normalized.name("Ctrl MAF")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Eur Maf")])
                  | data[normalized.name("Evs Eur Maf")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Afr Maf")])
                  | data[normalized.name("Evs Afr Maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC global maf")])
                  | data[normalized.name("ExAC global maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC afr maf")])
                  | data[normalized.name("ExAC afr maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC amr maf")])
                  | data[normalized.name("ExAC amr maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC eas maf")])
                  | data[normalized.name("ExAC eas maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC sas maf")])
                  | data[normalized.name("ExAC sas maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC fin maf")])
                  | data[normalized.name("ExAC fin maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC nfe maf")])
                  | data[normalized.name("ExAC nfe maf")] < 0.01)        &
                 (is.na(data[normalized.name("ExAC oth maf")])
                  | data[normalized.name("ExAC oth maf")] < 0.01)
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 6:
  data <- data[(is.na(data[normalized.name("Minor Hom Ctrl")])
                | data[normalized.name("Minor Hom Ctrl")] == 0),]
  if (dim(data)[1] ==0) {return(data)}

  Index <- is.na(data[normalized.name("Evs All Genotype Count")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <-as.character(data[i,normalized.name("Evs All Genotype Count")])
      not.done <- (length(grep("A1A1=0",str))==0) & (length(grep("A2A2=0",str))==0) &
        (length(grep("A3A3=0",str))==0)

      if (not.done) { #not found
        genotypes <- strsplit(str,"/")[[1]]
        genotype.count <- strsplit(genotypes,"=")[[1]][2]
        not.done <- genotype.count == "0"
      }
      if (not.done) { #the last condition has been checked
        Index[i] <- TRUE
      }
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  Index <- is.na(data[normalized.name("ExAC global gts")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <- as.character(data[i,normalized.name("ExAC global gts")])
      not.done <- !is.na(str)
      if (not.done) {
        not.done <- (length(grep("/0'$",str))>0)
      }

      if (not.done) {
        #print(str)
        genotype.count <- strsplit(str,"/")[[1]]
        if ((length(genotype.count) ==4) & (length(grep("NA",genotype.count[1])) >0)) {
          not.done <- (genotype.count[3] == "0") & (length(grep("^0",genotype.count[4])) >0)
        }
      }
    }
    if (not.done) { #the last condition has been checnke
      Index[i] <- TRUE
    }
  }
  data <- data[Index,]
  data
}

Filter.for.hemizygous <- function(data) {
  #check for correct columns
  columns <- c("Percent Read Alt (child)","Genotype Qual GQ (child)",
               "Rms Map Qual MQ (child)","Evs Filter Status",
               "Samtools Raw Coverage (mother)", "Genotype (mother)", "Genotype (father)",
               "Function", "Ctrl MAF", "Evs Eur Maf", "Evs Afr Maf","ExAC global maf",
               "ExAC afr maf","ExAC amr maf","ExAC eas maf","ExAC sas maf",
               "ExAC fin maf","ExAC nfe maf","ExAC oth maf","Minor Hom Ctrl",
               "Evs All Genotype Count", "ExAC global gts")

  #make sure all columns are present
  stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

  #step 1:
  data   <- data[is.na(data[normalized.name("Percent Read Alt (child)")])
                 | data[normalized.name("Percent Read Alt (child)")] > 0.8,]
  if (dim(data)[1] ==0) { return(data)}
  #step 2:
  data <- data[(is.na(data[normalized.name("Genotype Qual GQ (child)")])
                | data[normalized.name("Genotype Qual GQ (child)")] > 20)        &
                 (is.na(data[normalized.name("Rms Map Qual MQ (child)")])
                  | data[normalized.name("Rms Map Qual MQ (child)")] > 40)       &
                 (is.na(data[normalized.name("Evs Filter Status")])
                  | data[normalized.name("Evs Filter Status")] != "FAIL")
               ,]
  if (dim(data)[1] ==0) { return(data)}
  #step 3:
  data <- data[(is.na(data[normalized.name("Samtools Raw Coverage (mother)")])
                  | data[normalized.name("Samtools Raw Coverage (mother)")] > 10) &
                 (is.na(data[normalized.name("Genotype (mother)")])
                  | data[normalized.name("Genotype (mother)")] == "het")          &
                 (is.na(data[normalized.name("Genotype (father)")])
                  | data[normalized.name("Genotype (father)")] == "hom ref")
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 4:
  Index <- grep("^SYNONYMOUS",data$"Function")
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  Index <- grep("^INTRON_EXON",data$"Function")
  if (length(Index) >0) {
    data <- data[-Index,]
  }
  if (dim(data)[1] ==0) { return(data)}

  #step 5:
  data <- data[(is.na(data[normalized.name("Ctrl MAF")])
                | data[normalized.name("Ctrl MAF")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Eur Maf")])
                | data[normalized.name("Evs Eur Maf")] < 0.01)        &
                 (is.na(data[normalized.name("Evs Afr Maf")])
                | data[normalized.name("Evs Afr Maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC global maf")])
                | data[normalized.name("ExAC global maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC afr maf")])
                | data[normalized.name("ExAC afr maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC amr maf")])
                | data[normalized.name("ExAC amr maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC eas maf")])
                | data[normalized.name("ExAC eas maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC sas maf")])
                | data[normalized.name("ExAC sas maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC fin maf")])
                | data[normalized.name("ExAC fin maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC nfe maf")])
                | data[normalized.name("ExAC nfe maf")] < 0.01)        &
                (is.na(data[normalized.name("ExAC oth maf")])
                | data[normalized.name("ExAC oth maf")] < 0.01)
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 6:
  data <- data[(is.na(data[normalized.name("Minor Hom Ctrl")])
                | data[normalized.name("Minor Hom Ctrl")] == 0),]
  if (dim(data)[1] ==0) {return(data)}

  Index <- is.na(data[normalized.name("Evs All Genotype Count")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <-as.character(data[i,normalized.name("Evs All Genotype Count")])
      not.done <- (length(grep("A1A1=0",str))==0) & (length(grep("A2A2=0",str))==0) &
        (length(grep("A3A3=0",str))==0) & (length(grep("/A=",str))==0) &
        (length(grep("/C=",str))==0) & (length(grep("/G=",str))==0)    &
        (length(grep("/T=",str))==0)

      if (not.done) { #not found
        genotypes <- strsplit(str,"/")[[1]]
        genotype.count <- strsplit(genotypes,"=")[[1]][2]
        not.done <- genotype.count == "0"
      }
      if (not.done) { #the last condition has been checked
        Index[i] <- TRUE
      }
    }
  }
  data <- data[Index,]
  if (dim(data)[1] ==0) { return(data)}

  Index <- is.na(data[normalized.name("ExAC global gts")])
  for (i in 1:length(Index)) {
    if (!Index[i]) { #not is NA
      str <- as.character(data[i,normalized.name("ExAC global gts")])
      not.done <- !is.na(str)
      if (not.done) {
        not.done <- (length(grep("/0'$",str))>0)
      }

      if (not.done) {
        #print(str)
        genotype.count <- strsplit(str,"/")[[1]]
        if ((length(genotype.count) ==4) & (length(grep("NA",genotype.count[1])) >0)) {
          not.done <- (genotype.count[3] == "0") & (length(grep("^0",genotype.count[4])) >0)
        }
      }
    }
    if (not.done) { #the last condition has been checnke
      Index[i] <- TRUE
    }
  }
  data <- data[Index,]
  data
}

Filter.for.denovo <- function(data) {
  #check for correct columns
  columns <- c("Percent Read Alt (child)","Genotype Qual GQ (child)", "Qual By Depth QD (child)",
               "Rms Map Qual MQ (child)","Genotype Qual GQ (child)","Qual (child)",
               "Samtools Raw Coverage (child)","Evs Filter Status", "Samtools Raw Coverage (mother)",
               "Samtools Raw Coverage (father)","QC Fail Ctrl","QC Fail Ctrl","Ctrl MAF",
               "Evs All Genotype Count","ExAC global gts")

  #make sure all columns are present
  stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

  #steps can be merged or splitted
  #but merged steps can usually perform fasted

  #step 4:
  data <- data[(is.na(data[normalized.name("QC Fail Ctrl")])
                | data[normalized.name("QC Fail Ctrl")] == 0)                   &
                (is.na(data[normalized.name("Ctrl MAF")])
                  | data[normalized.name("Ctrl MAF")]  == 0)                    &
                is.na(data[normalized.name("Evs All Genotype Count")])          &
                is.na(data[normalized.name("ExAC global gts")])
               ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 1:
  data   <- data[is.na(data[normalized.name("Percent Read Alt (child)")])
                 | data[normalized.name("Percent Read Alt (child)")] > 0.3,]
  if (dim(data)[1] ==0) { return(data)}

  #step 2:
  data <- data[(is.na(data[normalized.name("Genotype Qual GQ (child)")])
                  | data[normalized.name("Genotype Qual GQ (child)")] > 20)      &
               (is.na(data[normalized.name("Qual By Depth QD (child)")])
                  | data[normalized.name("Qual By Depth QD (child)")] > 2)       &
               (is.na(data[normalized.name("Rms Map Qual MQ (child)")])
                  | data[normalized.name("Rms Map Qual MQ (child)")] > 40)       &
               (is.na(data[normalized.name("Genotype Qual GQ (child)")])
                  | data[normalized.name("Genotype Qual GQ (child)")] > 20)      &
               (is.na(data[normalized.name("Qual (child)")])
                  | data[normalized.name("Qual (child)")] > 50)                  &
               (is.na(data[normalized.name("Samtools Raw Coverage (child)")])
                  | data[normalized.name("Samtools Raw Coverage (child)")] > 10) &
               (is.na(data[normalized.name("Evs Filter Status")])
                  | data[normalized.name("Evs Filter Status")] != "FAIL")
              ,]
  if (dim(data)[1] ==0) { return(data)}

  #step 3: //de novo flag is not filtered here. Which can be filtered separately
  data <- data[(!is.na(data[normalized.name("Samtools Raw Coverage (mother)")])
                & data[normalized.name("Samtools Raw Coverage (mother)")] > 9)      &
                (!is.na(data[normalized.name("Samtools Raw Coverage (father)")])
                  & data[normalized.name("Samtools Raw Coverage (father)")] > 9)
               ,]

  data
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

#temp <- data[data["Variant.Type"] == "indel" & !is.na(data["Evs.All.Genotype.Count"]),c("Ref.Allele","Alt.Allele","Evs.All.Genotype.Count")]

Filter.by.Function <- function(x) {
  return ((x=="STOP_GAINED") | (x =="STOP_LOST") | (x =="START_LOST")
          | (x=="FRAME_SHIFT") | (x== "SPLICE_SITE_DONOR") | (x=="SPLICE_SITE_ACCEPTOR") | (x=="CODON_DELETION") | (x=="CODON_INSERTION") | (x=="CODON_CHANGE_PLUS_CODON_DELETION") | (x=="CODON_CHANGE_PLUS_CODON_INSERTION") | (x=="EXON_DELETED"))
}

Filter.by.HemiHomo.Count <- function(data,threshold,is.comphet = FALSE) {
  if (dim(data)[1] ==0) { return(data)}

  #Get IGM genotype count
  if (is.comphet) {
    stopifnot(length(which(colnames(data) == normalized.name("Major Hom Ctrl (#1)")))>0)
    stopifnot(length(which(colnames(data) ==  normalized.name("Minor Hom Ctrl (#1)")))>0)
    GenoTypeCount.1 <- cbind(data[normalized.name("Major Hom Ctrl (#1)")] , data[normalized.name("Minor Hom Ctrl (#1)")] )
    GenoTypeCount.1 <- apply(GenoTypeCount.1,1,function(x) min(x))

    stopifnot(length(which(colnames(data) == normalized.name("Major Hom Ctrl (#2)")))>0)
    stopifnot(length(which(colnames(data) ==  normalized.name("Minor Hom Ctrl (#2)")))>0)
    GenoTypeCount.2 <- cbind(data[normalized.name("Major Hom Ctrl (#2)")] , data[normalized.name("Minor Hom Ctrl (#2)")] )
    GenoTypeCount.2 <- apply(GenoTypeCount.2,1,function(x) min(x))

    #get EVS Genotype count
    column.name = normalized.name("Evs.All.Genotype.Count (#1)")
    stopifnot(length(which(colnames(data) == column.name))>0)
    tmp <- sapply(data[column.name], as.character)
    EVS.GenoTypeCount.1<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    column.name = normalized.name("Evs.All.Genotype.Count (#2)")
    stopifnot(length(which(colnames(data) == column.name))>0)
    tmp <- sapply(data[column.name], as.character)
    EVS.GenoTypeCount.2<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    #get Exac Genotype count
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
    stopifnot(length(which(colnames(data) == "Major.Hom.Ctrl"))>0)
    stopifnot(length(which(colnames(data) == "Minor.Hom.Ctrl"))>0)
    GenoTypeCount <- cbind(data["Major.Hom.Ctrl"] , data["Minor.Hom.Ctrl"] )
    GenoTypeCount <- apply(GenoTypeCount,1,function(x) min(x))

    #get EVS Genotype count
    stopifnot(length(which(colnames(data) == "Evs.All.Genotype.Count"))>0)
    tmp <- sapply(data["Evs.All.Genotype.Count"], as.character)
    EVS.GenoTypeCount<- sapply(tmp, Parse.HemiHomo.Count.EVS)

    #get Exac Genotype count
    stopifnot(length(which(colnames(data) == "ExAC.global.gts"))>0)
    tmp <- sapply(data["ExAC.global.gts"], as.character)
    Exac.GenoTypeCount <- sapply(tmp, Parse.HemiHomo.Count.Exac)

    All <- ((GenoTypeCount + EVS.GenoTypeCount + Exac.GenoTypeCount) <= threshold)
  }

  result <- data[All,]
}

Filter.by.Allele.Count <- function(data, threshold) {
  #Get IGM allele count
  stopifnot(length(which(colnames(data) == "Major.Hom.Ctrl"))>0)
  stopifnot(length(which(colnames(data) == "Het.Ctrl"))>0)
  stopifnot(length(which(colnames(data) == "Minor.Hom.Ctrl"))>0)
  AlleleCount <- cbind(2*data["Major.Hom.Ctrl"] + data["Het.Ctrl"], data["Het.Ctrl"] + 2*data["Minor.Hom.Ctrl"] )
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


Filter.for.tier2 <- function(data, is.comphet = FALSE) {
  if (is.comphet) {
    #check for correct columns
    columns <- c("HGMD Class (#1)","HGMD Class (#2)", "HGMD indel 9bpflanks (#1)", "HGMD indel 9bpflanks (#2)",
                 "ClinVar pathogenic indels (#1)", "ClinVar pathogenic indels (#2)", "ClinVar Clinical Significance (#1)", "ClinVar Clinical Significance (#2)",
                 "Function (#1)", "Function (#2)", "ClinGen (#1)", "ClinVar Pathogenic Indel Count (#1)", "Clinvar Pathogenic CNV Count (#1)", "ClinVar Pathogenic SNV Splice Count (#1)", "ClinVar Pathogenic SNV Nonsense Count (#1)")

    #make sure all columns are present
    stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

    #inclusion rule 1:
    R1.1 <- sapply(data[normalized.name("HGMD Class (#1)")], as.character)
    R1.1 <- sapply(R1.1, function(x) length(grep("DM",x)) > 0)
    R1.2 <- sapply(data[normalized.name("HGMD Class (#2)")], as.character)
    R1.2 <- sapply(R1.2, function(x) length(grep("DM",x)) > 0)

    R1 <- R1.1 | R1.2

    #inclusion rule 2:
    suppressWarnings(temp1 <- sapply(data[normalized.name("HGMD indel 9bpflanks (#1)")], as.numeric))
    temp1[is.na(temp1)] <- 0
    suppressWarnings(temp2 <- sapply(data[normalized.name("HGMD indel 9bpflanks (#2)")], as.numeric))
    temp2[is.na(temp2)] <- 0
    suppressWarnings(temp3 <- sapply(data[normalized.name("ClinVar pathogenic indels (#1)")], as.numeric))
    temp3[is.na(temp3)] <- 0
    suppressWarnings(temp4 <- sapply(data[normalized.name("ClinVar pathogenic indels (#2)")], as.numeric))
    temp4[is.na(temp4)] <- 0
    R2 <- temp1 > 0 | temp2 > 0 | temp3 > 0 | temp4 > 0

    #inclusion rule 3:
    temp <- sapply(data[normalized.name("ClinVar Clinical Significance (#1)")], as.character)
    temp[is.na(temp)] <- "0"
    R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Likely pathogenic;Pathogenic")

    temp <- sapply(data[normalized.name("ClinVar Clinical Significance (#2)")], as.character)
    temp[is.na(temp)] <- "0"
    R3 = R3 | (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Likely pathogenic;Pathogenic")

    Functional.1 <- Filter.by.Function(sapply(data[normalized.name("Function (#1)")], as.character))
    Functional.2 <- Filter.by.Function(sapply(data[normalized.name("Function (#2)")], as.character))

    #inclusion rule 4:
    R4 <- (Functional.1 | Functional.2) & (sapply(data[normalized.name("ClinGen (#1)")], as.character) == '1')

    #inclusion rule 5:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic Indel Count (#1)")], as.numeric))
    temp[is.na(temp)] <- 0
    R5 <- (Functional.1 | Functional.2) & (temp > 0)

    #inclusion rule 6:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Splice Count (#1)")], as.numeric))
    temp[is.na(temp)] <- 0
    R6 <- (Functional.1 | Functional.2) & (temp > 0)

    #inclusion rule 7:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar Pathogenic SNV Nonsense Count (#1)")], as.numeric))
    temp[is.na(temp)] <- 0
    R7 <- (Functional.1 | Functional.2) & (temp > 0)

    #inclusion rule 8:
    suppressWarnings(temp <- sapply(data[normalized.name("Clinvar Pathogenic CNV Count (#1)")], as.numeric))
    temp[is.na(temp)] <- 0
    R8 <- (Functional.1 | Functional.2) & (temp > 0)

  } else {
    #check for correct columns
    columns <- c("HGMD.Class", "HGMD.indel.9bpflanks", "ClinVar.pathogenic.indels",
                 "ClinVar.Clinical.Significance", "Function", "ClinGen",
                 "ClinVar.Pathogenic.Indel.Count", "Clinvar.Pathogenic.CNV.Count", "ClinVar.Pathogenic.SNV.Splice.Count", "ClinVar.Pathogenic.SNV.Nonsense.Count")

    #make sure all columns are present
    stopifnot(length(setdiff(normalized.name(columns),colnames(data))) ==0)

    #inclusion rule 1:
    R1 <- sapply(data["HGMD.Class"], as.character)
    R1 <- sapply(R1, function(x) length(grep("DM",x)) > 0)

    #inclusion rule 2:
    suppressWarnings(temp1 <- sapply(data[normalized.name("HGMD.indel.9bpflanks")], as.numeric))
    temp1[is.na(temp1)] <- 0

    suppressWarnings(temp2 <- sapply(data[normalized.name("ClinVar.pathogenic.indels")], as.numeric))
    temp2[is.na(temp2)] <- 0
    R2 <- temp1 > 0 | temp2 > 0

    #inclusion rule 3:
    temp <- sapply(data[normalized.name("ClinVar.Clinical.Significance")], as.character)
    temp[is.na(temp)] <- "0"
    R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Likely pathogenic;Pathogenic")

    Functional <- Filter.by.Function(sapply(data[normalized.name("Function")], as.character))

    #inclusion rule 4:
    R4 <- Functional & (sapply(data[normalized.name("ClinGen")], as.character) == '1')

    #inclusion rule 5:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar.Pathogenic.Indel.Count")], as.numeric))
    temp[is.na(temp)] <- 0
    R5 <- Functional & (temp > 0)

    #inclusion rule 6:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar.Pathogenic.SNV.Splice.Count")], as.numeric))
    temp[is.na(temp)] <- 0
    R6 <- Functional & (temp > 0)

    #inclusion rule 7:
    suppressWarnings(temp <- sapply(data[normalized.name("ClinVar.Pathogenic.SNV.Nonsense.Count")], as.numeric))
    temp[is.na(temp)] <- 0
    R7 <- Functional & (temp > 0)

    #inclusion rule 8:
    suppressWarnings(temp <- sapply(data[normalized.name("Clinvar.Pathogenic.CNV.Count")], as.numeric))
    temp[is.na(temp)] <- 0
    R8 <- Functional & (temp > 0)

  }

  R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8
  data <- data[R.all,]
  data
}

fun.12 <- function(x.1,x.2,...) {
  x.1p <- do.call("paste", x.1)
  x.2p <- do.call("paste", x.2)
  x.1[! x.1p %in% x.2p, ]
}
