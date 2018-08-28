module mutex(in1 in2, out1, out2);
    input in1, in2;
    output out1, out2;

    INV inv1 (.I(in1), .ON(v1));
    INV inv2 (.I(in2), .ON(v2));

    MUTEX me1 (.r1(v1), .g1(w1), .r2(v2), .g2(w2));

    BUF buf1 (.I(w1), .O(out1));
    BUF buf2 (.I(w2), .O(out2));
endmodule
