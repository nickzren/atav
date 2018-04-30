#Script:	IGM Sequencing Clinic Tier 1 Philosopy post-ATAV Script
#Version:	0.5
#Designed:	Nick Stong, Slavé Petrovski, David B. Goldstein
#Developed:	Nick Stong, Slavé Petrovski, Quanli Wang
#Last Update:	2016-07-01
#Institute:	IGM (Institute for Genomic Medicine)



Filter.by.Denovo.Flag <- function(data, type, strict = FALSE) {
#check for column name
if(!(length(which(colnames(data) == "Denovo.Flag"))>0)){stop("Column missing from denovo flag")}
if (strict) {
result <- data[data["Denovo.Flag"] == type,]
} else {
result <- data[grep(type, data$"Denovo.Flag") ,]
}
# put NA rows back if any
result = rbind(result,data[data["Denovo.Flag"] == "NA",])
return(result)
}

Filter.by.CompHet.Flag <- function(data, type, strict = FALSE) {
#check for column name
if(!(length(which(colnames(data) == "Comp.Het.Flag"))>0)){stop("Column missing from comp het flag")}
if (strict) {
result <- data[data["Comp.Het.Flag"] == type,]
} else {
result <- data[grep(type, data$"Comp.Het.Flag") ,] 
}
# put NA rows back if any
result = rbind(result,data[data["Comp.Het.Flag"] == "NA",])
return(result)
}

normalized.name <- function(names) {
names.new <- gsub("[( )#&-]",".",names)
names.new
}

Filter.for.compound.heterozygote <- function(data) {
return(rbind(
             Filter.for.compound.heterozygote.all(data[grep("DENOVO",data$Comp.Het.Flag, invert = T),]),
             Filter.for.compound.heterozygote.denovo(data[grepl("DENOVO",data$Comp.Het.Flag) & grepl("DE NOVO",data$Denovo.Flag...1.),],1,2),
             Filter.for.compound.heterozygote.denovo(data[grepl("DENOVO",data$Comp.Het.Flag) & grepl("DE NOVO",data$Denovo.Flag...2.),],2,1)
             ))
}

Filter.for.compound.heterozygote.denovo <- function(data,dnum,cnum) {
#check for correct columns, just in case the format has been changed
columns <- c("Var Ctrl Freq #1 & #2 (co-occurance)","GT (mother) (#1)", 
           "GT (father) (#1)","GT (mother) (#2)","GT (father) (#2)",
            "Effect (#1)","Effect (#2)",
           "Ctrl AF (#1)", "Evs All Maf (#1)", 
           "ExAC global af (#1)", "ExAC afr af (#1)", "ExAC amr af (#1)",
           "ExAC eas af (#1)","ExAC sas af (#1)","ExAC fin af (#1)",
           "ExAC nfe af (#1)","ExAC oth af (#1)",
           "Ctrl AF (#2)", "Evs All Maf (#2)", 
           "ExAC global af (#2)", "ExAC afr af (#2)", "ExAC amr af (#2)",
           "ExAC eas af (#2)","ExAC sas af (#2)","ExAC fin af (#2)",
           "ExAC nfe af (#2)","ExAC oth af (#2)",
           "Hom Ctrl (#1)","Evs All Genotype Count (#1)","ExAC global gts (#1)",
           "Evs Filter Status (#1)","ExAC global gts (#1)",
           "Hom Ctrl (#2)","Evs All Genotype Count (#2)","ExAC global gts (#2)",
           "Evs Filter Status (#2)","ExAC global gts (#2)" 
          )

#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at denovo comp het")}

if (dim(data)[1] ==0) { return(data)}
#step 1: 
data   <- data[is.na(data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")])
             | data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")] == 0,]
if (dim(data)[1] ==0) { return(data)}

#step 2: 
data <- data[(is.na(data[normalized.name(paste0("GT (mother) (#",dnum,")"))])
            | data[normalized.name(paste0("GT (mother) (#",dnum,")"))] != "hom")          &
            (is.na(data[normalized.name(paste0("GT (father) (#",dnum,")"))])
            | data[normalized.name(paste0("GT (father) (#",dnum,")"))] != "hom")          &
            (is.na(data[normalized.name(paste0("GT (mother) (#",dnum,")"))])
            | data[normalized.name(paste0("GT (mother) (#",dnum,")"))] != "het")          &
            (is.na(data[normalized.name(paste0("GT (father) (#",dnum,")"))])
            | data[normalized.name(paste0("GT (father) (#",dnum,")"))] != "het")          &
            (is.na(data[normalized.name(paste0("GT (mother) (#",cnum,")"))])
            | data[normalized.name(paste0("GT (mother) (#",cnum,")"))] != "hom")          &
            (is.na(data[normalized.name(paste0("GT (father) (#",cnum,")"))])
            | data[normalized.name(paste0("GT (father) (#",cnum,")"))] != "hom") 
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 3:
Index <- which(grepl("^synonymous",data[normalized.name(paste0("Effect (#",dnum,")"))][,1]) & (data[normalized.name(paste0("TraP Score (#",dnum,")"))] < 0.4 | is.na(data[normalized.name(paste0("TraP Score (#",dnum,")"))])))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data[normalized.name(paste0("Effect (#",dnum,")"))][,1]) & (data[normalized.name(paste0("TraP Score (#",dnum,")"))] < 0.4 | is.na(data[normalized.name(paste0("TraP Score (#",dnum,")"))])))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^synonymous",data[normalized.name(paste0("Effect (#",cnum,")"))][,1]) & (data[normalized.name(paste0("TraP Score (#",cnum,")"))] < 0.4 | is.na(data[normalized.name(paste0("TraP Score (#",cnum,")"))])))

if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data[normalized.name(paste0("Effect (#",cnum,")"))][,1]) & (data[normalized.name(paste0("TraP Score (#",cnum,")"))] < 0.4 | is.na(data[normalized.name(paste0("TraP Score (#",cnum,")"))])))

if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}


#step 4:
data <- data[(is.na(data[normalized.name(paste0("Ctrl AF (#",dnum,")"))])
            | data[normalized.name(paste0("Ctrl AF (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("Evs All Maf (#",dnum,")"))])
              | data[normalized.name(paste0("Evs All Maf (#",dnum,")"))] == 0)        & 
             (is.na(data[normalized.name(paste0("ExAC global af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC global af (#",dnum,")"))] == 0)    &
             (is.na(data[normalized.name(paste0("ExAC afr af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC afr af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC amr af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC amr af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC eas af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC eas af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC sas af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC sas af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC fin af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC fin af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC nfe af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC nfe af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("ExAC oth af (#",dnum,")"))])
              | data[normalized.name(paste0("ExAC oth af (#",dnum,")"))] == 0)        &
             (is.na(data[normalized.name(paste0("Ctrl AF (#",cnum,")"))])
              | data[normalized.name(paste0("Ctrl AF (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("Evs All Maf (#",cnum,")"))])
              | data[normalized.name(paste0("Evs All Maf (#",cnum,")"))] < 0.01)        & 
             (is.na(data[normalized.name(paste0("ExAC global af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC global af (#",cnum,")"))] < 0.01)    &
             (is.na(data[normalized.name(paste0("ExAC afr af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC afr af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC amr af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC amr af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC eas af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC eas af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC sas af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC sas af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC fin af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC fin af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC nfe af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC nfe af (#",cnum,")"))] < 0.01)        &
             (is.na(data[normalized.name(paste0("ExAC oth af (#",cnum,")"))])
              | data[normalized.name(paste0("ExAC oth af (#",cnum,")"))] < 0.01)
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 5:
#denovo filters
data <- data[(is.na(data[normalized.name(paste0("QC Fail Ctrl (#",dnum,")"))])
            | data[normalized.name(paste0("QC Fail Ctrl (#",dnum,")"))] == 0)                   &
            (is.na(data[normalized.name(paste0("Ctrl AF (#",dnum,")"))])
              | data[normalized.name(paste0("Ctrl AF (#",dnum,")"))]  == 0)                    &
            is.na(data[normalized.name(paste0("Evs All Genotype Count (#",dnum,")"))])          &
            is.na(data[normalized.name(paste0("ExAC global gts (#",dnum,")"))])
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 1: 
data   <- data[is.na(data[normalized.name(paste0("Percent Alt Read (#",dnum,")"))])
             | data[normalized.name(paste0("Percent Alt Read (#",dnum,")"))] >= 0.2
             | (data[normalized.name(paste0("FILTER (#",dnum,")"))] == "pass"
             & (data[normalized.name(paste0("Variant Type (#",dnum,")"))] == "snv"
               |data[normalized.name(paste0("Percent Alt Read Binomial P (#",dnum,")"))] > 0.01)),]

if (dim(data)[1] ==0) { return(data)}

#step 2: 
data <- data[(is.na(data[normalized.name(paste0("GQ (#",dnum,")"))])
              | data[normalized.name(paste0("GQ (#",dnum,")"))] > 20)      &
           (is.na(data[normalized.name(paste0("QD (#",dnum,")"))]) 
              | data[normalized.name(paste0("QD (#",dnum,")"))] > 2)       &
           (is.na(data[normalized.name(paste0("MQ (#",dnum,")"))])
              | data[normalized.name(paste0("MQ (#",dnum,")"))] > 40)       &
           (is.na(data[normalized.name(paste0("GQ (#",dnum,")"))]) 
              | data[normalized.name(paste0("GQ (#",dnum,")"))] > 20)      &
           (is.na(data[normalized.name(paste0("Qual (#",dnum,")"))]) 
              | data[normalized.name(paste0("Qual (#",dnum,")"))] > 50)                  &
           (is.na(data[normalized.name(paste0("DP Bin (#",dnum,")"))])
              | data[normalized.name(paste0("DP Bin (#",dnum,")"))] > 9) &
           (is.na(data[normalized.name(paste0("Evs Filter Status (#",dnum,")"))])
              | data[normalized.name(paste0("Evs Filter Status (#",dnum,")"))] != "FAIL") 
          ,]
if (dim(data)[1] ==0) { return(data)}

#step 3: //de novo flag is not filtered here. Which can be filtered separately
data <- data[(!is.na(data[normalized.name(paste0("DP Bin (mother) (#",dnum,")"))])
            & data[normalized.name(paste0("DP Bin (mother) (#",dnum,")"))] > 9)      &
            (!is.na(data[normalized.name(paste0("DP Bin (father) (#",dnum,")"))])
              & data[normalized.name(paste0("DP Bin (father) (#",dnum,")"))] > 9)
           ,]
#Other chet filters
data <- data[(is.na(data[normalized.name(paste0("Hom Ctrl (#",cnum,")"))])
            | data[normalized.name(paste0("Hom Ctrl (#",cnum,")"))] == 0),]
if (dim(data)[1] ==0) {return(data)}

Index <- is.na(data[normalized.name(paste0("Evs All Genotype Count (#",cnum,")"))]) 
for (i in 1:length(Index)) {
if (!Index[i]) { #not is NA
  str <-as.character(data[i,normalized.name(paste0("Evs All Genotype Count (#",cnum,")"))])
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

Index <- is.na(data[normalized.name(paste0("ExAC global gts (#",cnum,")"))]) 
for (i in 1:length(Index)) {
if (!Index[i]) { #not is NA
  str <- as.character(data[i,normalized.name(paste0("ExAC global gts (#",cnum,")"))])
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
if (not.done) { #the last condition has been checnke
  Index[i] <- TRUE
}
}
}
data <- data[Index,]
if (dim(data)[1] ==0) { return(data)}

data <- data[((is.na(data[normalized.name(paste0("Evs Filter Status (#",cnum,")"))])
             | data[normalized.name(paste0("Evs Filter Status (#",cnum,")"))] != "FAIL"))
            ,]
data
}
Filter.for.compound.heterozygote.all <- function(data) {
#check for correct columns, just in case the format has been changed
columns <- c("GT (mother) (#1)", 
           "GT (father) (#1)","GT (mother) (#2)","GT (father) (#2)",
            "Effect (#1)","Effect (#2)",
           "Ctrl AF (#1)", "Evs All Maf (#1)", 
           "ExAC global af (#1)", "ExAC afr af (#1)", "ExAC amr af (#1)",
           "ExAC eas af (#1)","ExAC sas af (#1)","ExAC fin af (#1)",
           "ExAC nfe af (#1)","ExAC oth af (#1)",
           "Ctrl AF (#2)", "Evs All Maf (#2)", 
           "ExAC global af (#2)", "ExAC afr af (#2)", "ExAC amr af (#2)",
           "ExAC eas af (#2)","ExAC sas af (#2)","ExAC fin af (#2)",
           "ExAC nfe af (#2)","ExAC oth af (#2)",
           "Hom Ctrl (#1)","Evs All Genotype Count (#1)","ExAC global gts (#1)",
           "Evs Filter Status (#1)","ExAC global gts (#1)",
           "Hom Ctrl (#2)","Evs All Genotype Count (#2)","ExAC global gts (#2)",
           "Evs Filter Status (#2)","ExAC global gts (#2)" 
          )

#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at comp het")}

if (dim(data)[1] ==0) { return(data)}
#step 1: 
data   <- data[is.na(data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")])
             | data[normalized.name("Var Ctrl Freq #1 & #2 (co-occurance)")] == 0,]
if (dim(data)[1] ==0) { return(data)}

#step 2: 
data <- data[(is.na(data[normalized.name("GT (mother) (#1)")])
            | data[normalized.name("GT (mother) (#1)")] != "hom")          &
            (is.na(data[normalized.name("GT (father) (#1)")])
            | data[normalized.name("GT (father) (#1)")] != "hom")          &
            (is.na(data[normalized.name("GT (mother) (#2)")])
            | data[normalized.name("GT (mother) (#2)")] != "hom")          &
            (is.na(data[normalized.name("GT (father) (#2)")])
            | data[normalized.name("GT (father) (#2)")] != "hom") 
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 3:
Index <- which(grepl("^synonymous",data[normalized.name("Effect (#1)")][,1]) & (data[normalized.name("TraP Score (#1)")] < 0.4 | is.na(data[normalized.name("TraP Score (#1)")])))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data[normalized.name("Effect (#1)")][,1]) & (data[normalized.name("TraP Score (#1)")] < 0.4 | is.na(data[normalized.name("TraP Score (#1)")])))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^synonymous",data[normalized.name("Effect (#2)")][,1]) & (data[normalized.name("TraP Score (#2)")] < 0.4 | is.na(data[normalized.name("TraP Score (#2)")])))

if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data[normalized.name("Effect (#2)")][,1]) & (data[normalized.name("TraP Score (#2)")] < 0.4 | is.na(data[normalized.name("TraP Score (#2)")])))

if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}


#step 4:
data <- data[(is.na(data[normalized.name("Ctrl AF (#1)")])
            | data[normalized.name("Ctrl AF (#1)")] < 0.01)        &
             (is.na(data[normalized.name("Evs All Maf (#1)")])
              | data[normalized.name("Evs All Maf (#1)")] < 0.01)        & 
             (is.na(data[normalized.name("ExAC global af (#1)")])
              | data[normalized.name("ExAC global af (#1)")] < 0.01)    &
             (is.na(data[normalized.name("ExAC afr af (#1)")])
              | data[normalized.name("ExAC afr af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC amr af (#1)")])
              | data[normalized.name("ExAC amr af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC eas af (#1)")])
              | data[normalized.name("ExAC eas af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC sas af (#1)")])
              | data[normalized.name("ExAC sas af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC fin af (#1)")])
              | data[normalized.name("ExAC fin af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC nfe af (#1)")])
              | data[normalized.name("ExAC nfe af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC oth af (#1)")])
              | data[normalized.name("ExAC oth af (#1)")] < 0.01)        &
             (is.na(data[normalized.name("Ctrl AF (#2)")])
              | data[normalized.name("Ctrl AF (#2)")] < 0.01)        &
             (is.na(data[normalized.name("Evs All Maf (#2)")])
              | data[normalized.name("Evs All Maf (#2)")] < 0.01)        & 
             (is.na(data[normalized.name("ExAC global af (#2)")])
              | data[normalized.name("ExAC global af (#2)")] < 0.01)    &
             (is.na(data[normalized.name("ExAC afr af (#2)")])
              | data[normalized.name("ExAC afr af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC amr af (#2)")])
              | data[normalized.name("ExAC amr af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC eas af (#2)")])
              | data[normalized.name("ExAC eas af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC sas af (#2)")])
              | data[normalized.name("ExAC sas af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC fin af (#2)")])
              | data[normalized.name("ExAC fin af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC nfe af (#2)")])
              | data[normalized.name("ExAC nfe af (#2)")] < 0.01)        &
             (is.na(data[normalized.name("ExAC oth af (#2)")])
              | data[normalized.name("ExAC oth af (#2)")] < 0.01)
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 5:
data <- data[(is.na(data[normalized.name("Hom Ctrl (#1)")])
            | data[normalized.name("Hom Ctrl (#1)")] == 0),]
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
if (not.done) { #the last condition has been checnke
  Index[i] <- TRUE
}
}
}
data <- data[Index,]
if (dim(data)[1] ==0) { return(data)}

data <- data[((is.na(data[normalized.name("Evs Filter Status (#1)")])
              | data[normalized.name("Evs Filter Status (#1)")] != "FAIL"))
           ,]
if (dim(data)[1] ==0) { return(data)}

data <- data[(is.na(data[normalized.name("Hom Ctrl (#2)")])
            | data[normalized.name("Hom Ctrl (#2)")] == 0),]
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
if (not.done) { #the last condition has been checnke
  Index[i] <- TRUE
}
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
columns <- c("Percent Alt Read","GQ", 
           "MQ","Evs Filter Status",
           "DP Bin (mother)", "GT (mother)", "GT (father)",
           "Effect", "Ctrl AF", "Evs All Maf","ExAC global af",
           "ExAC afr af","ExAC amr af","ExAC eas af","ExAC sas af",
           "ExAC fin af","ExAC nfe af","ExAC oth af","Hom Ctrl",
           "Evs All Genotype Count", "ExAC global gts")

#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at homozygous")}

if (dim(data)[1] ==0) { return(data)}
#step 1: 
data   <- data[is.na(data[normalized.name("Percent Alt Read")])
             | data[normalized.name("Percent Alt Read")] > 0.799,]
if (dim(data)[1] ==0) { return(data)}
#step 2: 
data <- data[(is.na(data[normalized.name("GQ")])
            | data[normalized.name("GQ")] > 20)        &
             (is.na(data[normalized.name("MQ")])
              | data[normalized.name("MQ")] > 40)       &
             (is.na(data[normalized.name("Evs Filter Status")])
              | data[normalized.name("Evs Filter Status")] != "FAIL") 
           ,]
if (dim(data)[1] ==0) { return(data)}
#step 3:
data <- data[(is.na(data[normalized.name("GT (mother)")])
              | data[normalized.name("GT (mother)")] == "het")          &
             (is.na(data[normalized.name("GT (father)")])
              | data[normalized.name("GT (father)")] == "het")  
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 4:
Index <- which(grepl("^splice_region_variant",data$"Effect") & (data$TraP.Score < 0.4 | is.na(data$TraP.Score)))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data$"Effect") & (data$TraP.Score < 0.4 | is.na(data$TraP.Score)))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

#step 5:
data <- data[(is.na(data[normalized.name("Ctrl AF")])
            | data[normalized.name("Ctrl AF")] < 0.01)        &
             (is.na(data[normalized.name("Evs All Maf")])
              | data[normalized.name("Evs All Maf")] < 0.01)        & 
             (is.na(data[normalized.name("ExAC global af")])
              | data[normalized.name("ExAC global af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC afr af")])
              | data[normalized.name("ExAC afr af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC amr af")])
              | data[normalized.name("ExAC amr af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC eas af")])
              | data[normalized.name("ExAC eas af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC sas af")])
              | data[normalized.name("ExAC sas af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC fin af")])
              | data[normalized.name("ExAC fin af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC nfe af")])
              | data[normalized.name("ExAC nfe af")] < 0.01)        &
             (is.na(data[normalized.name("ExAC oth af")])
              | data[normalized.name("ExAC oth af")] < 0.01)  
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 6:
data <- data[(is.na(data[normalized.name("Hom Ctrl")])
            | data[normalized.name("Hom Ctrl")] == 0),]
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
if (not.done) { #the last condition has been checnke
  Index[i] <- TRUE
}
}
}
data <- data[Index,]
data
}

Filter.for.hemizygous <- function(data) {
#check for correct columns
columns <- c("Percent Alt Read","GQ", 
           "MQ","Evs Filter Status",
           "DP Bin (mother)", "GT (mother)", "GT (father)",
           "Effect", "Ctrl AF", "Evs All Maf","ExAC global af",
           "ExAC afr af","ExAC amr af","ExAC eas af","ExAC sas af",
           "ExAC fin af","ExAC nfe af","ExAC oth af","Hom Ctrl",
           "Evs All Genotype Count", "ExAC global gts")

#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop(setdiff(normalized.name(columns),colnames(data)))}

if (dim(data)[1] ==0) { return(data)}
#step 1: 
data   <- data[is.na(data[normalized.name("Percent Alt Read")])
             | data[normalized.name("Percent Alt Read")] > 0.7999,]
if (dim(data)[1] ==0) { return(data)}
#step 2: 
data <- data[(is.na(data[normalized.name("GQ")])
            | data[normalized.name("GQ")] > 20)        &
             (is.na(data[normalized.name("MQ")])
              | data[normalized.name("MQ")] > 40)       &
             (is.na(data[normalized.name("Evs Filter Status")])
              | data[normalized.name("Evs Filter Status")] != "FAIL") 
           ,]
if (dim(data)[1] ==0) { return(data)}
#step 3:
xdata <- data[grep("^X",data$Variant.ID),]
adata <- data[grep("^X",data$Variant.ID,invert=T),]
if(dim(xdata)[1] > 0){
  xdata <- xdata[(is.na(xdata[normalized.name("DP Bin (mother)")])
                  | xdata[normalized.name("DP Bin (mother)")] > 9) &
                 (is.na(xdata[normalized.name("GT (mother)")])
                  | xdata[normalized.name("GT (mother)")] == "het")          &
                 (is.na(xdata[normalized.name("GT (father)")])
                  | xdata[normalized.name("GT (father)")] == "hom ref")  
               ,]
}
if(dim(adata)[1] > 0){
  adata <- adata[(is.na(adata[normalized.name("DP Bin (mother)")])
              | adata[normalized.name("DP Bin (mother)")] > 9) &
            (is.na(adata[normalized.name("DP Bin (father)")])
              | adata[normalized.name("DP Bin (father)")] > 9)
           ,]
}
data <- rbind(xdata,adata)
if (dim(data)[1] ==0) { return(data)}

#step 4:
Index <- which(grepl("^synonymous",data$"Effect") & (data$TraP.Score < 0.4 | is.na(data$TraP.Score)))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

Index <- which(grepl("^splice_region_variant",data$"Effect") & (data$TraP.Score < 0.4 | is.na(data$TraP.Score)))
if (length(Index) >0) {
data <- data[-Index,]
}
if (dim(data)[1] ==0) { return(data)}

#step 5:
data <- data[(is.na(data[normalized.name("Ctrl AF")])
            | data[normalized.name("Ctrl AF")] < 0.01)        &
             (is.na(data[normalized.name("Evs All Maf")])
            | data[normalized.name("Evs All Maf")] < 0.01)        & 
            (is.na(data[normalized.name("ExAC global af")])
            | data[normalized.name("ExAC global af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC afr af")])
            | data[normalized.name("ExAC afr af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC amr af")])
            | data[normalized.name("ExAC amr af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC eas af")])
            | data[normalized.name("ExAC eas af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC sas af")])
            | data[normalized.name("ExAC sas af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC fin af")])
            | data[normalized.name("ExAC fin af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC nfe af")])
            | data[normalized.name("ExAC nfe af")] < 0.01)        &
            (is.na(data[normalized.name("ExAC oth af")])
            | data[normalized.name("ExAC oth af")] < 0.01)  
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 6:
data <- data[(is.na(data[normalized.name("Hom Ctrl")])
            | data[normalized.name("Hom Ctrl")] == 0),]
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
if (not.done) { #the last condition has been checnke
  Index[i] <- TRUE
}
}
}
data <- data[Index,]
data
}

Filter.for.denovo <- function(data) {
#check for correct columns
columns <- c("Percent Alt Read","GQ", "QD",
           "MQ","GQ","Qual",
           "DP Bin","Evs Filter Status", "DP Bin (mother)",
           "DP Bin (father)","QC Fail Ctrl","QC Fail Ctrl","Ctrl AF",
           "Evs All Genotype Count","ExAC global gts")

if (dim(data)[1] ==0) { return(data)}
#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at denovo")}

#steps can be merged or splitted
#but merged steps can usually perform fasted

#step 4:
data <- data[((is.na(data[normalized.name("QC Fail Ctrl")])
            | data[normalized.name("QC Fail Ctrl")] == 0)                   &
            (is.na(data[normalized.name("Ctrl AF")])
              | data[normalized.name("Ctrl AF")]  == 0)                    &
            is.na(data[normalized.name("Evs All Genotype Count")])          &
            is.na(data[normalized.name("ExAC global gts")]))|
            !is.na(data$OMIM.Disease)
           ,]
if (dim(data)[1] ==0) { return(data)}

#step 1: 
data   <- data[is.na(data[normalized.name("Percent Alt Read")])
             | data[normalized.name("Percent Alt Read")] >= 0.2 
             | (data[normalized.name("FILTER")] == "pass"
             &  (data[normalized.name("Variant Type")] == "snv"
               | data[normalized.name("Percent Alt Read Binomial P")] > 0.01)),]
if (dim(data)[1] ==0) { return(data)}

#step 2: 
data <- data[(is.na(data[normalized.name("GQ")])
              | data[normalized.name("GQ")] > 20)      &
           (is.na(data[normalized.name("QD")]) 
              | data[normalized.name("QD")] > 2)       &
           (is.na(data[normalized.name("MQ")])
              | data[normalized.name("MQ")] > 40)       &
           (is.na(data[normalized.name("GQ")]) 
              | data[normalized.name("GQ")] > 20)      &
           (is.na(data[normalized.name("Qual")]) 
              | data[normalized.name("Qual")] > 50)                  &
           (is.na(data[normalized.name("DP Bin")])
              | data[normalized.name("DP Bin")] > 9) &
           (is.na(data[normalized.name("Evs Filter Status")])
              | data[normalized.name("Evs Filter Status")] != "FAIL") 
          ,]
if (dim(data)[1] ==0) { return(data)}

#step 3: //de novo flag is not filtered here. Which can be filtered separately
data <- data[(!is.na(data[normalized.name("DP Bin (mother)")])
            & data[normalized.name("DP Bin (mother)")] > 9)      &
            (!is.na(data[normalized.name("DP Bin (father)")])
              & data[normalized.name("DP Bin (father)")] > 9)
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

Filter.by.Effect <- function(x) {
return ((x=="stop_gained") | (x =="stop_lost") | (x =="start_lost")
      | (x=="frameshift_variant") | (x== "splice_donor_variant") | (x=="splice_acceptor_variant") |  (x=="exon_loss_variant"))
}

Filter.by.HemiHomo.Count <- function(data,threshold,is.comphet = FALSE) {
if (dim(data)[1] ==0) { return(data)}
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

Filter.by.Allele.Count <- function(data, threshold) {
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


Filter.for.tier2 <- function(data, is.comphet = FALSE, is.denovo = FALSE) {
if (is.comphet) {
#check for correct columns
columns <- c("HGMD Class (#1)","HGMD Class (#2)", "HGMD indel 9bpflanks (#1)", "HGMD indel 9bpflanks (#2)",
             "ClinVar pathogenic indels (#1)", "ClinVar pathogenic indels (#2)", "ClinVar Clinical Significance (#1)", "ClinVar Clinical Significance (#2)",
             "Effect (#1)", "Effect (#2)", "ClinGen (#1)", "ClinVar Pathogenic Indel Count (#1)", "Clinvar Pathogenic CNV Count (#1)", "ClinVar Pathogenic SNV Splice Count (#1)", "ClinVar Pathogenic SNV Nonsense Count (#1)")
if (dim(data)[1] ==0) { return(data)}
#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier 2 comphet")}

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
R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

temp <- sapply(data[normalized.name("ClinVar Clinical Significance (#2)")], as.character)
temp[is.na(temp)] <- "0"
R3 = R3 | (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")

#inclusion rule 3.1:
diseasePheno <- "congenital|developmental_disorder|intellectualDisability|epilepsy"
R3 = R3 | grepl(diseasePheno,data$DenovoDB.Phenotype...1.)
R3 = R3 | grepl(diseasePheno,data$DenovoDB.Phenotype...2.)

Functional.1 <- Filter.by.Effect(sapply(data[normalized.name("Effect (#1)")], as.character))
Functional.2 <- Filter.by.Effect(sapply(data[normalized.name("Effect (#2)")], as.character))

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

R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8

} else {
#check for correct columns
columns <- c("HGMD.Class", "HGMD.indel.9bpflanks", "ClinVar.pathogenic.indels",
             "ClinVar.Clinical.Significance", "Effect", "ClinGen",
             "ClinVar.Pathogenic.Indel.Count", "Clinvar.Pathogenic.CNV.Count", "ClinVar.Pathogenic.SNV.Splice.Count", "ClinVar.Pathogenic.SNV.Nonsense.Count")

if (dim(data)[1] ==0) { return(data)}
#make sure all columns are present
if(!(length(setdiff(normalized.name(columns),colnames(data))) ==0)){stop("Columns missing at tier2")}

if (dim(data)[1] ==0) { return(data)}
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
R3 = (temp == "Pathogenic") | (temp == "Likely pathogenic") | (temp == "Pathogenic|Likely Pathogenic")
#inclusion rule 3.1:
diseasePheno <- "congenital|developmental_disorder|intellectualDisability|epilepsy"
R3 = R3 | grepl(diseasePheno,data$DenovoDB.Phenotype) 

Functional <- Filter.by.Effect(sapply(data[normalized.name("Effect")], as.character))

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

R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8

#inclusion rule de novo
if(is.denovo){
    R9 <- !is.na(data$OMIM.Disease)
    R.all <- R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 | R9
}
}
data <- data[R.all,]
data
}

fun.12 <- function(x.1,x.2,...) {
  x.1p <- do.call("paste", x.1)
  x.2p <- do.call("paste", x.2)
  x.1[! x.1p %in% x.2p, ]
}
