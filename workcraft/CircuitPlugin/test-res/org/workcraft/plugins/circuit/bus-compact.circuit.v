module test(input [1:0] in, output [1:0] out);

    assign w[1] = in[1];
    assign w[0] = ~in[0];

    BUF buf (.I(w[1]), .O(out[1]));
    INV inv (.I(w[0]), .ON(out[0]));

    // signal values at the initial state:
    // !in[1] !in[0] !w[1] w[0] !out[1] !out[0]
endmodule
