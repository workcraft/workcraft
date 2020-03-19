
.name allocoutbound
.inputs req ackctl ackbus nakbus
.outputs ack busctl reqbus
.mode SELFTIMED
.graph
req+/0 busctl+/0 
busctl+/0 OR0 
OR0 ackctl+/0 
ackctl+/0 reqbus+/0 
reqbus+/0 OR1 
OR1 ackbus+/0 
OR1 nakbus+/0 
reqbus-/1 ackbus-/1 
ackctl-/1 ack+/0 
ack+/0 req-/1 
ack-/1 req+/0 
reqbus-/2 nakbus-/1 
busctl+/1 OR0 
nakbus-/1 busctl-/2 
busctl-/2 ackctl-/2 
ackbus-/1 busctl-/3 
busctl-/3 ackctl-/1 
ackbus+/0 reqbus-/1 
nakbus+/0 reqbus-/2 
ackctl-/2 busctl+/1 
req-/1 ack-/1 

.marking {OR0 }
.end
