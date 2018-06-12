module test(in, out);
    input [1:0] in;
    output [1:0] out;

    BUF buf1 (.I(in[1]), .O(out[1]));
    BUF buf0 (.I(in[0]), .O(out[0]));
endmodule
