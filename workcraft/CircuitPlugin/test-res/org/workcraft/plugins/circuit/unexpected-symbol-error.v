module a (o, i0, i1);
    output o;
    input i0;
    input i1;
# comment
    assign o = (i1 & o | i0) & (i1 | o);
endmodule
