module gc (o, a, b);
    output o;
    input a;
    input b;
    assign o = a | o & ~b;
endmodule
