write(
    "N-way conformation checks:\n" +
    "  * block1:                   " +
    checkStgNwayConformation(null, "block1-bad.stg.work") + "\n" +
    "  * block1 block2:            " +
    checkStgNwayConformation(null, "block1.stg.work block3.stg.work") + "\n" +
    "  * block1 block2 block3:     " +
    checkStgNwayConformation(null, "block1.stg.work block2.stg.work block3.stg.work") + "\n" +
    "  * block1-bad block2 block3: " +
    checkStgNwayConformation(null, "block1-bad.stg.work block2.stg.work block3.stg.work") + "\n" +
    "", "nway-conformation.result");

exit();
