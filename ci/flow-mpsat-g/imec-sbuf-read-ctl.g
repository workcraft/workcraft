
.name sbufreadctl
.inputs ackread busack
.outputs ack ramrdsbuf busreq req
.mode SELFTIMED
.graph

ackread+/0 busreq+/0 
busreq+/0 busack+/0 
busack+/0 busreq-/0 
busreq-/0 busack-/0 
busack-/0 ramrdsbuf-/0 
busack-/0 ack+/0 
ramrdsbuf-/0 req-/0 
ack+/0 req-/0 
req-/0 ack-/0 
ack-/0 ackread-/1 req+/1
req+/1 ramrdsbuf+/1 
ackread-/1 ramrdsbuf+/1 
ramrdsbuf+/1 ackread+/0 

.marking {<ackread+/0 ,busreq+/0 > }
.end
