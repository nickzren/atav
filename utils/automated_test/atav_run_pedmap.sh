. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/trio_sample.txtmap --ped-map --min-coverage 10 --filter PASS

atav_latest.sh --sample $ATAV_TEST_PATH/trio_sample.txt --effect $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/trio_sample.txtmap --ped-map --min-coverage 10 --filter PASS
