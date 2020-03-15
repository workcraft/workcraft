block1Work = load("block1.stg.work");
block2Work = load("block2.stg.work");
block3Work = load("block3.stg.work");
result1 = check();

closeWork(block1Work);
block1Work = load("block1-bad.stg.work");
result2 = check();

closeWork(block2Work);
closeWork(block3Work);
result3 = check();

write(result1 + result2 + result3, "nway-conformation.result");
exit();

function check() {
    result = "";
    for each (work in getWorks()) {
        result += work.getTitle() + " ";
    }
    result += ": " + checkStgNwayConformation(null) + "\n";
    return result
}
