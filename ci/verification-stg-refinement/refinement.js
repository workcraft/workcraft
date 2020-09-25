work_csc = load("mode_selector-csc.stg.work");
work_cr = load("mode_selector-cr.stg.work");
work_ta = load("read_completion-ta.stg.work");

write(
    "Refinement checks:\n" +
    "  * CSC strict:  " + checkStgRefinement(work_csc, "mode_selector.stg.work") + "\n" +
    "  * CSC relaxed: " + checkStgRefinementRelaxed(work_csc, "mode_selector.stg.work") + "\n" +
    "  * CR strict:   " + checkStgRefinement(work_cr, "mode_selector.stg.work") + "\n" +
    "  * CR relaxed:  " + checkStgRefinementRelaxed(work_cr, "mode_selector.stg.work") + "\n" +
    "  * TA strict:   " + checkStgRefinement(work_ta, "read_completion.stg.work") + "\n" +
    "  * TA relaxed:  " + checkStgRefinementRelaxed(work_ta, "read_completion.stg.work") + "\n" +
    "", "refinement.result");

exit();
