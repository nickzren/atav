. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/trio_denovo --include-noflag --list-trio-denovo --min-coverage 10 --var-status pass --ctrl-maf 0.3 --ccds-only --evs-maf 0.1

atav.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/trio_denovo --include-noflag --list-trio-denovo --min-coverage 10 --var-status pass --ctrl-maf 0.3 --ccds-only --evs-maf 0.1
