
.name nowick
.inputs c b a
.outputs y x
.mode SELFTIMED
.graph
b+/1 y+/1 
b+/1 x+/1 
a+/1 y+/1 
a+/1 x+/1 
y-/2 b+/1 
y-/2 a+/1 
y+/2 a-/1 
x-/2 a-/1 
b-/1 y+/2 
b-/1 x-/2 
y-/1 b-/1 
x+/2 b-/1 
x-/1 c-/1 
y+/1 c+/1 
c+/1 x-/1 
x+/1 c+/1 
c-/1 y-/1 
c-/1 x+/2 
a-/1 y-/2 

.marking {<y-/2 ,b+/1 > <y-/2 ,a+/1 > }
.end
