.name seqmix
.inputs a0 b1 c1 d1
.outputs a1 b0 c0 d0
.mode SELFTIMED
.initial state !a0 !b1 !c1 !d1 !a1 !b0 !c0 !d0
.graph

OR0 a0+/0
a0+/0 b0+/0
b0+/0 b1+/0
b1+/0 b0-/0
b0-/0 b1-/0
b1-/0 c0+/0
c0+/0 c1+/0
c1+/0 c0-/0
c0-/0 c1-/0
c1-/0 b0+/1
b0+/1 b1+/1
b1+/1 b0-/1
b0-/1 b1-/1
b1-/1 d0+/0
d0+/0 d1+/0
d1+/0 a1+/0
a1+/0 a0-/0
a0-/0 d0-/0
d0-/0 d1-/0
d1-/0 a1-/0
a1-/0 OR0

.marking {OR0} 
.end
