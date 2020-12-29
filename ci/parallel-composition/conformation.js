work = composeStg(null, "cycle-mutex.stg.work charge.stg.work");
stat = statStg(work);
write(stat, "composition.stg.stat");

exit();
