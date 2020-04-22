work = composeStg(null, "cycle.stg.work charge.stg.work");
stat = statStg(work);
write(stat, "composition.stg.stat");

exit();
