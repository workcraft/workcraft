#Memory management unit (from C. Myers)
.name mmu
.inputs mdli rai bi lsri
.outputs mdlo rao bo lsro
.mode SELFTIMED
.graph
mdli+/0 rao+/0 
rao+/0 rai+/0 
rai+/0 lsro+/0 
mdli+/0 bo+/0 
bo+/0 bi+/0 
bi+/0 lsro+/0 
lsro+/0 lsri+/0 
lsri+/0 mdlo+/0 
mdlo+/0 mdli-/1 
mdli-/1 mdlo-/1 
mdlo-/1 mdli+/0 
rao-/1 rai-/1 
rai-/1 rao+/0 
lsro+/0 bo-/1 
bo-/1 bi-/1 
bi-/1 bo+/0 
mdlo+/0 lsro-/1 
lsro-/1 lsri-/1 
lsri-/1 lsro+/0 
lsro+/0 rao-/1 

.marking {<mdlo-/1 ,mdli+/0 > <rao-/1 ,rai-/1 > <bi-/1 ,bo+/0 > <lsri-/1 ,lsro+/0 > }
.end
