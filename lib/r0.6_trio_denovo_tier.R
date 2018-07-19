do_denovo_tier <- function(denovoFile){
### denovoFile is the fully qualified path to ATAV's denovo output
### atavOutput is the directory to which ATAV will write the output files
###rm(list = ls())
#### Test change 
source("/nfs/goldstein/software/atav_home/lib/r0.6_trio_filters.R")
atav.output = read.csv(denovoFile,na.strings="NA")
atavOutput <- dirname(denovoFile)
data <- Filter.by.Denovo.Flag(atav.output,'DE NOVO', strict = TRUE)
data <- Filter.for.denovo(data)
write.csv(data, file = paste(atavOutput,"/denovo.csv", sep=""))

data <- Filter.by.Denovo.Flag(atav.output,'NEWLY HEMIZYGOUS', strict = TRUE)
data <- rbind(data,Filter.by.Denovo.Flag(atav.output,'COMPOUND DELETION', strict = TRUE))
data <- Filter.for.hemizygous(data)
write.csv(data, file = paste(atavOutput,"/hemizygous.csv", sep=""))

data <- Filter.by.Denovo.Flag(atav.output,'NEWLY HOMOZYGOUS', strict = TRUE)
data <- Filter.for.homozygous(data)
write.csv(data, file = paste(atavOutput,"/homozygous.csv", sep=""))

###rm(list = ls())
input.data = read.csv(denovoFile,na.strings="NA",stringsAsFactors=FALSE)

data <- Filter.by.Denovo.Flag(input.data,'DE NOVO', strict = FALSE)
data <- Filter.by.Allele.Count(data,19)
data <- Filter.for.tier2(data)
write.csv(data, file = paste(atavOutput,"/denovo_tier2.csv", sep=""))

data <- Filter.by.Denovo.Flag(input.data,'NEWLY HEMIZYGOUS', strict = FALSE)
data <- rbind(data,Filter.by.Denovo.Flag(input.data,'COMPOUND DELETION', strict = TRUE))
data <- Filter.by.HemiHomo.Count(data,9)
data <- Filter.for.tier2(data)
write.csv(data, file = paste(atavOutput,"/hemizygous_tier2.csv", sep=""))

data <- Filter.by.Denovo.Flag(input.data,'NEWLY HOMOZYGOUS', strict = FALSE)
data <- Filter.by.HemiHomo.Count(data,9)
data <- Filter.for.tier2(data)
write.csv(data, file = paste(atavOutput,"/homozygous_tier2.csv", sep=""))
}
### MAIN ###
args <- commandArgs(T)
denovo.denovoFile <- args[1]
deno.atavOutput <-args[2]
do_denovo_tier(denovo.denovoFile)
