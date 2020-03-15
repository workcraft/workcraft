.name par
.inputs a0 b1 c1 d1 e1
.outputs a1 b0 c0 d0 e0
.initial state !a0 !b1 !c1 !d1 !e1 !a1 !b0 !c0 !d0 !e0
.mode SELFTIMED
.graph
a0+ b0+ c0+ d0+ e0+
b0+ b1+
b1+ b0-
b0- b1-
c0+ c1+
c1+ c0-
c0- c1-
d0+ d1+
d1+ d0-
d0- d1-
e0+ e1+
e1+ e0-
e0- e1-
c1- a1+
b1- a1+
d1- a1+
e1- a1+
a1+ a0-
a0- a1-
a1- a0+
.marking { <a1-, a0+> }
.end
