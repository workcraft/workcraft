.name seq8
.inputs a0 b1 c1 d1 e1 f1 g1 k1 j1
.outputs a1 b0 c0 d0 e0 f0 g0 k0 j0
.graph
a0+ b0+
b0+ b1+
b1+ b0-
b0- b1-
b1- c0+
c0+ c1+
c1+ c0-
c0- c1-
c1- d0+
d0+ d1+
d1+ d0-
d0- d1-
d1- e0+
e0+ e1+
e1+ e0-
e0- e1-
e1- f0+
f0+ f1+
f1+ f0-
f0- f1-
f1- g0+
g0+ g1+
g1+ g0-
g0- g1-
g1- j0+
j0+ j1+
j1+ j0-
j0- j1-
j1- k0+
k0+ k1+
k1+ a1+
a1+ a0-
a0- k0-
k0- k1-
k1- a1-
a1- a0+
.marking {<a1-,a0+>}
.end
