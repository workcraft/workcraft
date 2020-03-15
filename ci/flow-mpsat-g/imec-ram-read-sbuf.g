
.name ramreadsbuf
.inputs req precharged prnotin wenin wsldin
.outputs ack wsen prnot wen wsld
.mode SELFTIMED
.graph
req+/0 prnot+/0 
precharged+/0 prnot+/0 
prnot+/0 prnotin+/0 
prnotin+/0 wen+/0 
wen+/0 precharged-/0 
wen+/0 wenin+/0 
precharged-/0 ack+/0 
wenin+/0 ack+/0 
ack+/0 req-/0 
req-/0 wen-/0 
req-/0 wsen-/0 
wen-/0 wenin-/0 
wsen-/0 wenin-/0 
wsld+/0 wsldin+/0 
wsld+/0 precharged+/0 
prnot-/0 prnotin-/0 
prnot-/0 precharged+/0 
wsld-/0 wsldin-/0 
wsldin-/0 ack-/0 
wsldin-/0 wsen+/0 
ack-/0 req+/0 
wsen+/0 req+/0 
wenin-/0 wsld+/0 
wenin-/0 prnot-/0 
wsldin+/0 wsld-/0 
prnotin-/0 wsld-/0 

.marking {<req+/0 ,prnot+/0 > <precharged+/0 ,prnot+/0 > }
.end
