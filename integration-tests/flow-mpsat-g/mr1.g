
.name master_read1
.inputs bprn xack di pack
.outputs breq busyo mrdc do pdo 
.mode TIMED
.graph
breq- bprn-
bprn- busyo-
busyo- breq+ 
busyo- mrdc-
breq+ bprn+ 
breq+ busyo+
bprn+ breq-
busyo+ breq-
mrdc- xack-
xack- mrdc+ 
xack- do+
mrdc+ xack+ 
mrdc+ breq+
xack+ mrdc-
do+ di+
di+ do- 
di+ mrdc+ 
di+ pdo-
do- di-
di- do+
pdo- pack-
pack- pdo+
pdo+ pack+
pack+ pdo- 
pack+ breq-
.marking {<bprn+,breq-> <busyo+,breq-> <xack+,mrdc-> <di-,do+> <pack+,pdo-> <pack+,breq->}
.end
