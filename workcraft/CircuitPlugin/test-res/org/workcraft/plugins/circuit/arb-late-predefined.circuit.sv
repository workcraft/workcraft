// Verilog netlist generated by Workcraft 3
module test (sig, ctrl, san1, san0);
    input sig, ctrl;
    output san1, san0;
    wire wait1_san, wait0_san, wait1_sig, wait0_sig;

    assign #1 {san1, san0} = {san1, san0, wait1_san, wait0_san} == 4'b0011 ? 2'b01 : {wait1_san & (~san0 | ~wait0_san), wait0_san & (~san1 | ~wait1_san)};
    assign #(1ps * $urandom_range(20, 50)) wait1_sig = (sig == 1'b0) || (sig == 1'b1) ? sig : 1'b1;
    assign #1 wait1_san = ctrl & (wait1_sig | wait1_san);
    assign #(1ps * $urandom_range(20, 50)) wait0_sig = (sig == 1'b0) || (sig == 1'b1) ? sig : 1'b1;
    assign #1 wait0_san = ctrl & (~wait0_sig | wait0_san);

    // signal values at the initial state:
    // !ctrl !san0 !san1 !sig !wait0_san !wait1_san
endmodule
