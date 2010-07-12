module Full_BF (
  i1_0r, i1_0a, i1_0d,
  i2_0r, i2_0a, i2_0d,
  o_0r, o_0a, o_0d
);
  wire go_r;
  wire go_a;
  wire dummy_o_a;
  wire dummy_i1_r;
  wire dummy_i2_r;

  output i1_0r;
  input i1_0a;
  input [7:0] i1_0d;
  output i2_0r;
  input i2_0a;
  input [7:0] i2_0d;
  input o_0r;
  output o_0a;
  output [7:0] o_0d;
  Balsa_BF I0 (go_r, go_a, o_0r, dummy_o_a, o_0d[7:0], dummy_i1_r, i1_0a, i1_0d[7:0], dummy_i2_r, i2_0a, i2_0d[7:0]);
  Control_BF I1 (go_r, go_a, o_0r, o_0a, i1_0r, i1_0a, i2_0r, i2_0a);
endmodule
