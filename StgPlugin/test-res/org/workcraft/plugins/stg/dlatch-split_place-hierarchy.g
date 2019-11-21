
.inputs D clock.C
.outputs Q
.dummy clock

.graph
D0Q0 D+ clock.C-
D1Q1 D- clock.C-/1
clock.C1 Q+ clock.C- clock.C-/1
clock.C1@1 Q- clock.C- clock.C-/1
D+ Q+
D- Q-
Q+ D1Q1 clock.C1
Q- D0Q0 clock.C1@1
clock.C+ clock.C1 clock.C1@1
clock.C- D0Q0 clock.C+/1
clock.C+/1 clock.C1 clock.C1@1
clock.C-/1 D1Q1 clock.C+

.marking { D0Q0 clock.C1 clock.C1@1 }
.end
