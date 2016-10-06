#Script:	IGM Sequencing Clinic Tier 1 Philosopy post-ATAV Script
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
  names.new <- gsub("[( )#&-]",".",names)
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
                | data[normalized.name("Ctrl MAF (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("Evs Eur Maf (#1)")])
                  | data[normalized.name("Evs Eur Maf (#1)")] < 0.005)        & 
                 (is.na(data[normalized.name("Evs Afr Maf (#1)")])
                  | data[normalized.name("Evs Afr Maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC global maf (#1)")])
                  | data[normalized.name("ExAC global maf (#1)")] < 0.005)    &
                 (is.na(data[normalized.name("ExAC afr maf (#1)")])
                  | data[normalized.name("ExAC afr maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC amr maf (#1)")])
                  | data[normalized.name("ExAC amr maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC eas maf (#1)")])
                  | data[normalized.name("ExAC eas maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC sas maf (#1)")])
                  | data[normalized.name("ExAC sas maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC fin maf (#1)")])
                  | data[normalized.name("ExAC fin maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC nfe maf (#1)")])
                  | data[normalized.name("ExAC nfe maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC oth maf (#1)")])
                  | data[normalized.name("ExAC oth maf (#1)")] < 0.005)        &
                 (is.na(data[normalized.name("Ctrl MAF (#2)")])
                  | data[normalized.name("Ctrl MAF (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("Evs Eur Maf (#2)")])
                  | data[normalized.name("Evs Eur Maf (#2)")] < 0.005)        & 
                 (is.na(data[normalized.name("Evs Afr Maf (#2)")])
                  | data[normalized.name("Evs Afr Maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC global maf (#2)")])
                  | data[normalized.name("ExAC global maf (#2)")] < 0.005)    &
                 (is.na(data[normalized.name("ExAC afr maf (#2)")])
                  | data[normalized.name("ExAC afr maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC amr maf (#2)")])
                  | data[normalized.name("ExAC amr maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC eas maf (#2)")])
                  | data[normalized.name("ExAC eas maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC sas maf (#2)")])
                  | data[normalized.name("ExAC sas maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC fin maf (#2)")])
                  | data[normalized.name("ExAC fin maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC nfe maf (#2)")])
                  | data[normalized.name("ExAC nfe maf (#2)")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC oth maf (#2)")])
                  | data[normalized.name("ExAC oth maf (#2)")] < 0.005)
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
      not.done <- (length(grep("A1A1=[[123456789]]",str))==0) & (length(grep("A2A2=[[123456789]]",str))==0) & 
        (length(grep("A3A3=[[123456789]]",str))==0) 
      
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
      not.done <- (length(grep("A1A1=[[123456789]]",str))==0) & (length(grep("A2A2=[[123456789]]",str))==0) & 
        (length(grep("A3A3=[[123456789]]",str))==0) 
      
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
                 | data[normalized.name("Percent Read Alt (child)")] > 0.799,]
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
                | data[normalized.name("Ctrl MAF")] < 0.005)        &
                 (is.na(data[normalized.name("Evs Eur Maf")])
                  | data[normalized.name("Evs Eur Maf")] < 0.005)        & 
                 (is.na(data[normalized.name("Evs Afr Maf")])
                  | data[normalized.name("Evs Afr Maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC global maf")])
                  | data[normalized.name("ExAC global maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC afr maf")])
                  | data[normalized.name("ExAC afr maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC amr maf")])
                  | data[normalized.name("ExAC amr maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC eas maf")])
                  | data[normalized.name("ExAC eas maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC sas maf")])
                  | data[normalized.name("ExAC sas maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC fin maf")])
                  | data[normalized.name("ExAC fin maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC nfe maf")])
                  | data[normalized.name("ExAC nfe maf")] < 0.005)        &
                 (is.na(data[normalized.name("ExAC oth maf")])
                  | data[normalized.name("ExAC oth maf")] < 0.005)  
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
      not.done <- (length(grep("A1A1=[[123456789]]",str))==0) & (length(grep("A2A2=[[123456789]]",str))==0) & 
        (length(grep("A3A3=[[123456789]]",str))==0)
      
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
  if (dim(data)[1] ==0) { return(data)}

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
                 | data[normalized.name("Percent Read Alt (child)")] > 0.7999,]
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
                  | data[normalized.name("Samtools Raw Coverage (mother)")] > 9) &
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
                | data[normalized.name("Ctrl MAF")] < 0.005)        &
                 (is.na(data[normalized.name("Evs Eur Maf")])
                | data[normalized.name("Evs Eur Maf")] < 0.005)        & 
                 (is.na(data[normalized.name("Evs Afr Maf")])
                | data[normalized.name("Evs Afr Maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC global maf")])
                | data[normalized.name("ExAC global maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC afr maf")])
                | data[normalized.name("ExAC afr maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC amr maf")])
                | data[normalized.name("ExAC amr maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC eas maf")])
                | data[normalized.name("ExAC eas maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC sas maf")])
                | data[normalized.name("ExAC sas maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC fin maf")])
                | data[normalized.name("ExAC fin maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC nfe maf")])
                | data[normalized.name("ExAC nfe maf")] < 0.005)        &
                (is.na(data[normalized.name("ExAC oth maf")])
                | data[normalized.name("ExAC oth maf")] < 0.005)  
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
        (length(grep("A3A3=0",str))==0) & (length(grep("/A=\\d/",str))==0) &
        (length(grep("/C=\\d/",str))==0) & (length(grep("/G=\\d/",str))==0)    & 
        (length(grep("/T=\\d/",str))==0)  
      
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
                 | data[normalized.name("Percent Read Alt (child)")] > 0.1499,]
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
                  | data[normalized.name("Samtools Raw Coverage (child)")] > 9) &
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
