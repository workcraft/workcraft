module mutex_test(in1 in2, out1, out2);
    input in1, in2;
    output out1, out2;

    BUF inbuf1 (.I(in1), .O(v1));
    BUF inbuf2 (.I(in2), .O(v2));

    MUTEX_late me (.r1(v1), .g1(w1), .r2(v2), .g2(w2));

    BUF outbuf1 (.I(w1), .O(out1));
    BUF outbuf2 (.I(w2), .O(out2));
endmodule
