module TOP (in, out);
    input in;
    output out;

    INV inv1 (.I(in), .ON());
    INV inv2 (out, );
endmodule
