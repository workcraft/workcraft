
.inputs D C
.outputs Q

.graph
D0Q0 D+ C-
D1Q1 D- C-/1
C1@2 Q+ C- C-/1
C1 Q- C- C-/1
D+ Q+
D- Q-
Q+ D1Q1 C1@2
Q- D0Q0 C1
C+ C1@2 C1
C- D0Q0 C+/1
C+/1 C1@2 C1
C-/1 D1Q1 C+

.marking { D0Q0 C1@2 C1 }
.end
