
.name mod4_counter
.inputs a
.outputs p q
.mode SELFTIMED
.graph
a+/0 p+/0 
p+/0 a-/1 
a-/1 p-/1 
p-/1 a+/2 
a+/2 p+/2 
p+/2 a-/3 
a-/3 q+/0 
q+/0 a+/4 
a+/4 p-/3 
p-/3 a-/5 
a-/5 p+/4 
p+/4 a+/6 
a+/6 p-/5 
p-/5 a-/7 
a-/7 q-/1 
q-/1 a+/0 

.marking {<q-/1 ,a+/0 > }
.end
