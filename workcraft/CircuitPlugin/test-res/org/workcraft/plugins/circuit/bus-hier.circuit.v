module top (input [2:0] in, output [1:0] out);
  wire [1:0] w;

  // Bus connects of instA are expanded as follows:
  // INPUTS: in[2] -> instA.i[1], in[1] -> instA.i[0], in[0] -> instA.i[2]
  // OUTPUTS: instA.o[1] -> w[0], instA.o[0] -> w[1]
  ModuleA instA (.i({in[0], in[2:1]}), .o(w[0:1]));

  // Bus connects of instB are expanded as follows:
  // INPUTS: w[1] -> instB.i[0], w[0] -> instB.i[1]
  // OUTPUTS: instB.o -> out[1]
  ModuleB instB (.i(w), .o(out[1]));

  INV inst_inv (.I(w[1]), .ON(out[0]));

  // signal values at the initial state:
  // !in[2] !in[1] !in[0] w[1] !w[0] !out[1] !out[0]
endmodule


module ModuleA (i, o);
  input [2:0] i;
  output [1:0] o;

  AND2 inst_and2 (.A(i[2]), .B(i[1]), .O(o[1]));
  NOR2 inst_nor2 (.A(i[1]), .B(i[0]), .ON(o[0]));

  // signal values at the initial state:
  // !i[2] !i[1] !i[0] !o[1] o[0]
endmodule


module ModuleB (i, o);
  input [0:1] i;
  output o;

  NAND2B inst_nand2b (.AN(i[1]), .B(i[0]), .ON(o));

  // signal values at the initial state:
  // !i[1] i[0] !o
endmodule

