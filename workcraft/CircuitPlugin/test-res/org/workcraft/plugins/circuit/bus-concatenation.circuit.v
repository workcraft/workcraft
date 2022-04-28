module top(in, out);
    input [3:0] in;
    output [3:0] out;

    Module inst1 (.in({in[2], in[3]}), .out({out[0], out[1]}));
    Module inst2 (.in(in[1:0]), .out(out[3:2]));
endmodule
