work = load("cycle-mutex.stg.work");

write(
    "Conformation checks:\n" +
    "  * null:                " + checkStgConformation(work, null) + "\n" +
    "  * incorrect-file:      " + checkStgConformation(work, "incorrect-file") + "\n" +
    "  * charge.stg.work:     " + checkStgConformation(work, "charge.stg.work") + "\n" +
    "  * charge-bad.stg.work: " + checkStgConformation(work, "charge-bad.stg.work") + "\n" +
    "", "conformation.result");

exit();
