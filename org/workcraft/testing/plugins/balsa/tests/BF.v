/*
    `BF.v'
    Balsa Verilog netlist file
    Created: Thu Apr 22 18:40:25 BST 2010
    By: dell@dell-laptop (Linux)
    With net-verilog (balsa-netlist) version: 3.5.1
    Using technology: example/four_b_rb
    Command line : (balsa-netlist -v BF.breeze)
*/

module AND3 (
  out,
  in0,
  in1,
  in2
);
  output out;
  input in0;
  input in1;
  input in2;
endmodule

module BALSA_FA (
  nStart,
  A,
  B,
  nCVi,
  Ci,
  nCVo,
  Co,
  sum
);
  input nStart;
  input A;
  input B;
  input nCVi;
  input Ci;
  output nCVo;
  output Co;
  output sum;
endmodule

module BUFF (
  Z,
  A
);
  output Z;
  input A;
endmodule

module INV (
  out,
  in
);
  output out;
  input in;
endmodule

module NOR2 (
  out,
  in0,
  in1
);
  output out;
  input in0;
  input in1;
endmodule

module NOR3 (
  out,
  in0,
  in1,
  in2
);
  output out;
  input in0;
  input in1;
  input in2;
endmodule

module BrzBinaryFunc__Data_8_8_8_s3_Add_s5_False__m7m (
  go_0r, go_0a,
  out_0r, out_0a, out_0d,
  inpA_0r, inpA_0a, inpA_0d,
  inpB_0r, inpB_0a, inpB_0d
);
  input go_0r;
  output go_0a;
  input out_0r;
  output out_0a;
  output [7:0] out_0d;
  output inpA_0r;
  input inpA_0a;
  input [7:0] inpA_0d;
  output inpB_0r;
  input inpB_0a;
  input [7:0] inpB_0d;
  wire [2:0] internal_0n;
  wire start_0n;
  wire nStart_0n;
  wire [8:0] nCv_0n;
  wire [8:0] c_0n;
  wire [7:0] eq_0n;
  wire [7:0] addOut_0n;
  wire [7:0] w_0n;
  wire [7:0] n_0n;
  wire v_0n;
  wire z_0n;
  wire nz_0n;
  wire nxv_0n;
  wire done_0n;
  supply0 gnd;
  NOR3 I0 (internal_0n[0], nCv_0n[1], nCv_0n[2], nCv_0n[3]);
  NOR3 I1 (internal_0n[1], nCv_0n[4], nCv_0n[5], nCv_0n[6]);
  NOR2 I2 (internal_0n[2], nCv_0n[7], nCv_0n[8]);
  AND3 I3 (done_0n, internal_0n[0], internal_0n[1], internal_0n[2]);
  BUFF I4 (out_0d[0], addOut_0n[0]);
  BUFF I5 (out_0d[1], addOut_0n[1]);
  BUFF I6 (out_0d[2], addOut_0n[2]);
  BUFF I7 (out_0d[3], addOut_0n[3]);
  BUFF I8 (out_0d[4], addOut_0n[4]);
  BUFF I9 (out_0d[5], addOut_0n[5]);
  BUFF I10 (out_0d[6], addOut_0n[6]);
  BUFF I11 (out_0d[7], addOut_0n[7]);
  BALSA_FA I12 (nStart_0n, n_0n[0], w_0n[0], nCv_0n[0], c_0n[0], nCv_0n[1], c_0n[1], addOut_0n[0]);
  BALSA_FA I13 (nStart_0n, n_0n[1], w_0n[1], nCv_0n[1], c_0n[1], nCv_0n[2], c_0n[2], addOut_0n[1]);
  BALSA_FA I14 (nStart_0n, n_0n[2], w_0n[2], nCv_0n[2], c_0n[2], nCv_0n[3], c_0n[3], addOut_0n[2]);
  BALSA_FA I15 (nStart_0n, n_0n[3], w_0n[3], nCv_0n[3], c_0n[3], nCv_0n[4], c_0n[4], addOut_0n[3]);
  BALSA_FA I16 (nStart_0n, n_0n[4], w_0n[4], nCv_0n[4], c_0n[4], nCv_0n[5], c_0n[5], addOut_0n[4]);
  BALSA_FA I17 (nStart_0n, n_0n[5], w_0n[5], nCv_0n[5], c_0n[5], nCv_0n[6], c_0n[6], addOut_0n[5]);
  BALSA_FA I18 (nStart_0n, n_0n[6], w_0n[6], nCv_0n[6], c_0n[6], nCv_0n[7], c_0n[7], addOut_0n[6]);
  BALSA_FA I19 (nStart_0n, n_0n[7], w_0n[7], nCv_0n[7], c_0n[7], nCv_0n[8], c_0n[8], addOut_0n[7]);
  BUFF I20 (nCv_0n[0], nStart_0n);
  BUFF I21 (c_0n[0], gnd);
  INV I22 (nStart_0n, start_0n);
  BUFF I23 (n_0n[0], inpB_0d[0]);
  BUFF I24 (n_0n[1], inpB_0d[1]);
  BUFF I25 (n_0n[2], inpB_0d[2]);
  BUFF I26 (n_0n[3], inpB_0d[3]);
  BUFF I27 (n_0n[4], inpB_0d[4]);
  BUFF I28 (n_0n[5], inpB_0d[5]);
  BUFF I29 (n_0n[6], inpB_0d[6]);
  BUFF I30 (n_0n[7], inpB_0d[7]);
  BUFF I31 (w_0n[0], inpA_0d[0]);
  BUFF I32 (w_0n[1], inpA_0d[1]);
  BUFF I33 (w_0n[2], inpA_0d[2]);
  BUFF I34 (w_0n[3], inpA_0d[3]);
  BUFF I35 (w_0n[4], inpA_0d[4]);
  BUFF I36 (w_0n[5], inpA_0d[5]);
  BUFF I37 (w_0n[6], inpA_0d[6]);
  BUFF I38 (w_0n[7], inpA_0d[7]);
  BUFF I39 (inpB_0r, inpB_0a);
  BUFF I40 (inpA_0r, inpA_0a);
  BUFF I41 (out_0a, out_0r);
  BUFF I42 (go_0a, done_0n);
  BUFF I43 (start_0n, go_0r);
endmodule

module Balsa_BF (
  go_0r, go_0a,
  i1_0r, i1_0a, i1_0d,
  i2_0r, i2_0a, i2_0d,
  o_0r, o_0a, o_0d
);
  input go_0r;
  output go_0a;
  output i1_0r;
  input i1_0a;
  input [7:0] i1_0d;
  output i2_0r;
  input i2_0a;
  input [7:0] i2_0d;
  input o_0r;
  output o_0a;
  output [7:0] o_0d;
  BrzBinaryFunc__Data_8_8_8_s3_Add_s5_False__m7m I0 (go_0r, go_0a, o_0r, o_0a, o_0d[7:0], i1_0r, i1_0a, i1_0d[7:0], i2_0r, i2_0a, i2_0d[7:0]);
endmodule

