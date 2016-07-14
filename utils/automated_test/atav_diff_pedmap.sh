. atav_config_paths.sh

diff $ATAV_OUTPUT_PATH/new/pedmap/pedmap_output.ped $ATAV_OUTPUT_PATH/old/pedmap/pedmap_output.ped > $ATAV_DIFF_PATH/ped.diff

diff $ATAV_OUTPUT_PATH/new/pedmap/pedmap_output.map $ATAV_OUTPUT_PATH/old/pedmap/pedmap_output.map > $ATAV_DIFF_PATH/map.diff
