# version got by e-mail on Oct 28 1991
.name master_read
.inputs ari pri bprn xack di pack
.outputs aro pro breq busy mrdc do pdo
.initial state !ari !pri !bprn !xack !di !pack aro pro !breq !busy !mrdc do pdo
.mode TIMED
.graph
ari+ pro-
ari- pro+
pri+ breq+
pri- aro+ breq-
bprn+ breq-
bprn- busy-
xack+ mrdc-
xack- do+
di+ pdo- mrdc+
di- pdo+ mrdc-
pack+ pdo-
pack- pdo+
aro+ ari+
aro- ari-
pro+ pri-
pro- aro- pri+
breq+ pro+ busy+ bprn+
breq- pro- bprn-
busy+ breq-
busy- mrdc- breq+
mrdc+ do- busy+ xack+
mrdc- xack-
do+ di+
do- di-
pdo+ do+ pack+
pdo- do- pack-
.marking { <aro+,ari+> <breq-,pro-> <busy-,breq+> <pdo+,pack+> <do+, di+> }
.end
