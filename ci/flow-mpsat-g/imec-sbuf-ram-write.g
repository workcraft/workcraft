
.name sbuframwrite
.inputs req precharged done wenin wsldin
.outputs ack prbar wsen wen wsld
.mode SELFTIMED
.graph
prbar+/0 precharged-/0 
precharged-/0 wen+/0 
wen+/0 done+/0 
wen+/0 wenin+/0 
wen-/0 wenin-/0 
wsen-/0 wenin-/0 
ack+/0 req-/0 
wenin-/0 wsld+/0 
wsld+/0 wsldin+/0 
wsld-/0 wsldin-/0 
wsen+/0 done-/0 
prbar-/0 precharged+/0 
ack-/0 req+/0 
req+/0 prbar+/0 
precharged+/0 prbar+/0 
done-/0 prbar+/0 
done+/0 wsen-/0 
done+/0 wen-/0 
done+/0 ack+/0 
wenin+/0 wsen-/0 
wenin+/0 wen-/0 
wenin+/0 ack+/0 
wsldin+/0 wsld-/0 
wsldin-/0 ack-/0 
wsldin-/0 prbar-/0 
wsldin-/0 wsen+/0 
req-/0 ack-/0 
req-/0 prbar-/0 
req-/0 wsen+/0 

.marking {<wsen+/0 ,done-/0 > <prbar-/0 ,precharged+/0 > <ack-/0 ,req+/0 > }
.end
