do_comp_het_tier <- function(comphetFile){
    ### comphetFile is the fully qualified path to ATAV's denovo output
    ### atavOutput is the directory to which ATAV will write the output files
    source("/nfs/goldstein/software/atav_home/lib/r0.5_trio_filters.R")
    atav.output = read.csv(comphetFile,na.strings="NA")
    atavOutput <- dirname(comphetFile)
    data <- Filter.by.CompHet.Flag(atav.output,'COMPOUND HETEROZYGOTE', strict = FALSE)
    data <- Filter.for.compound.heterozygote(data)
    write.csv(data, file = paste(atavOutput,"/compound.heterozygote.csv", sep=""))
    data <- Filter.by.CompHet.Flag(atav.output,'POSSIBLY COMPOUND HETEROZYGOTE', strict = TRUE)  
    poschet <- Filter.for.compound.heterozygote(data)
    write.csv(poschet, file = paste(atavOutput,"/possibly.compound.heterozygote.csv", sep=""))
    data <- Filter.by.CompHet.Flag(atav.output,'DENOVO WITH INHERITED VARIANT', strict = TRUE)  
    dnmchet <- Filter.for.compound.heterozygote(data)
    write.csv(dnmchet, file = paste(atavOutput,"/possibly.denovo.compound.heterozygote.csv", sep=""))

    input.data = read.csv(comphetFile,na.strings="NA",stringsAsFactors=FALSE)

    data <- Filter.by.CompHet.Flag(input.data,'COMPOUND HETEROZYGOTE', strict = FALSE)
    data <- Filter.by.HemiHomo.Count(data,9, is.comphet = TRUE)
    data <- Filter.for.tier2(data, is.comphet = TRUE)
    write.csv(data, file = paste(atavOutput,"/compound.heterozygote_tier2.csv", sep=""))
}

### MAIN ###
args <- commandArgs(T)
comphet.comphetFile <- args[1]
comphet.atavOutput <-args[2]
do_comp_het_tier(comphet.comphetFile)
