./workcraft -nogui -noconfig -dir:${test_dir} -exec:mpsat-vme.js >${log_file}

# Filter out fanin/fanout statistics as it often changes on different runs
for f in ${test_dir}/mpsat-vme-*.circuit.stat; do
    [[ -f $f ]] || continue
    # OS X `sed -i` requires backup extension, e.g. `sed -i.bak`
#    sed  -n -i.bak '/Literal count combinational \/ sequential (set + reset)/!p' $f
    sed  -n -i.bak '/Max fanin \/ fanout/!p' $f
    sed  -n -i.bak '/Fanin distribution/!p' $f
    sed  -n -i.bak '/Fanout distribution/!p' $f
    rm -f ${f}.bak
done

# Clean up
rm -f ${test_dir}/vme-csc.stg.work
