script_file=${test_dir}/mpsat-vme.js

[[ -e $script_file ]] || error "Script file ${script_file} is missing"

./workcraft -nogui -dir:${test_dir} -exec:${script_file} >/dev/null

# Filter out fanin/fanout statistics as it often changes on different runs
for f in ${test_dir}/mpsat-vme-*.circuit.stat; do
    [[ -f $f ]] || continue
    sed  -n -i '/Max fanin \/ fanout/!p' $f
    sed  -n -i '/Fanin distribution/!p' $f
    sed  -n -i '/Fanout distribution/!p' $f
done
