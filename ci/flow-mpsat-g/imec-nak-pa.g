
.name nackpa
.inputs rejsend ackbus ackhyst busack
.outputs ack reqbus hystreq busreq enableda
.mode SELFTIMED
.graph
rejsend+/0 reqbus+/0 
reqbus+/0 ackbus+/0 
ackbus+/0 hystreq+/0 
ackbus+/0 enableda+/0 
hystreq+/0 ackhyst+/0 
enableda+/0 ackhyst+/0 
ackhyst+/0 busreq+/0 
busreq+/0 busack+/0 
busreq-/0 busack-/0 
busack-/0 reqbus-/0 
busack-/0 ack+/0 
busack-/0 enableda-/0 
busack-/0 hystreq-/0 
reqbus-/0 ackbus-/0 
hystreq-/0 ackhyst-/0 
ack+/0 rejsend-/0 
enableda-/0 ackhyst-/0 
ack-/0 rejsend+/0 
busack+/0 busreq-/0 
ackbus-/0 ack-/0 
rejsend-/0 ack-/0 
ackhyst-/0 ack-/0 

.marking {<ack-/0 ,rejsend+/0 > }
.end
