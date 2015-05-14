# Deploy ATAV

cd to ATAV work dir

mvn clean compile assembly:single

scp PATH/atav-?-jar-with-dependencies.jar ???@zr2180@10.73.50.42:/nfs/goldstein/software/atav_home/atav-?.jar
