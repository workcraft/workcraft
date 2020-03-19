
.name master_read0
.inputs ari pri bprn xack di
.outputs aro pro breq busyo mrdc do 
.mode SELFTIMED
.graph
aro+ ari+
ari+ aro- 
ari+ pro-
aro- ari-
ari- aro+
pri- pro- 
pri- aro+ 
pri- breq-
pro- pri+
pri+ pro+
pro+ pri-
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
di- aro+
do- di-
di- do+
.marking {<ari-,aro+> <pri-,aro+ > <bprn+,breq-> <busyo+,breq-> <xack+,mrdc-> <di-,do+> <pri-,pro-> <di-,aro+>}
.end
