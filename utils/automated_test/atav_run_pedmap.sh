. atav_config_paths.sh

atav_beta.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/pedmap --ped-map --min-coverage 10 --var-status pass

atav.sh --sample $ATAV_TEST_PATH/ped --function $ATAV_TEST_PATH/function --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/pedmap --ped-map --min-coverage 10 --var-status pass
