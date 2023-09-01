module redundant_inout (in, out, VDD, GND);
    input in;
    output out;
    inout VDD, GND;

    wire g1_O;

    OR2 g0 (.O(out), .A(in), .B(g1_O), .VDD(VDD), .GND(GND));
    LOGIC1 g1 (.O(g1_O), .VDD(VDD), .GND(GND));

    // signal values at the initial state:
    // !in out g1_O
endmodule
