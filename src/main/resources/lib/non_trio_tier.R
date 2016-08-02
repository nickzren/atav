do_non_trio_tier <- function(varGenoFile){

### Get the non trio script
source("/nfs/goldstein/software/atav_home/lib/r0.3_filters_nontrio.R")
outputDirect <- dirname(varGenoFile)
input.data = read.csv(paste(outputDirect,"/ATAV_output.csv", sep=""),na.strings="NA",stringsAsFactors=FALSE)

#private KV
data <- Filter.by.Allele.Count(input.data,0)
data.new <-Filter.for.tier2.pdnm.kv(data)
write.csv(data.new, file = paste(outputDirect,"/kv.csv", sep=""))


#private KV_5alleles
data <- Filter.by.Allele.Count(input.data,5)
data.new <-Filter.for.tier2.pdnm.kv(data)
write.csv(data.new, file = paste(outputDirect,"/kv_5alleles.csv", sep=""))

#private LoF
data <- Filter.by.Allele.Count(input.data,5)
data.new <-Filter.for.tier2.pdnm.lof(data)
write.csv(data.new, file = paste(outputDirect,"/private_lof.csv", sep=""))


#REC_KV_LoF
data <- Filter.by.HemiHomo.Count(input.data,0)
data.new <-Filter.for.tier2.prec.kv.lof(data)
write.csv(data.new, file = paste(outputDirect,"/rec_kv_lof.csv", sep=""))


}

#### Main ######
rm(list = ls())
args <- commandArgs(T)
atavOutputFile <- args[1]
do_non_trio_tier(atavOutputFile)