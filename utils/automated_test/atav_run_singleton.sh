. atav_config_paths.sh

atav_beta.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/new/singleton --list-singleton --impact HIGH,MODERATE,LOW --min-ad-alt 3 --qual 30 --gq 20 --filter pass,likely,intermediate --max-default-control-af 0.01 --max-gnomad-exome-af 0.01 --max-gnomad-genome-af 0.01 --include-qc-missing

atav_prod.sh --disable-timestamp-from-out-path --sample $ATAV_TEST_PATH/trio_sample.txt --region $ATAV_TEST_PATH/region --out $ATAV_OUTPUT_PATH/old/singleton --list-singleton --impact HIGH,MODERATE,LOW --min-ad-alt 3 --qual 30 --gq 20 --filter pass,likely,intermediate --max-default-control-af 0.01 --max-gnomad-exome-af 0.01 --max-gnomad-genome-af 0.01 --include-qc-missing