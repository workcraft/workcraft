
.name duplicator
.inputs a b
.outputs r s
.mode SELFTIMED
.graph
s+/1 b+/1 
b+/1 s-/1 
s-/1 b-/1 
b-/1 r+/1 
s+/2 b+/2 
b+/2 s-/2 
r+/1 a-/1 
a-/1 s-/2 
s-/2 b-/2 
b-/2 r-/1 
a+/1 s-/1 
r-/1 a+/1 
b-/2 s+/1 
b-/1 s+/2 

.marking {<s-/2 ,b-/2 > }
.end
