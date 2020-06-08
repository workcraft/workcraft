.model seq_mix
.inputs  c1_activate_rq c2_out_ac
.outputs  c1_activate_ac c2_out_rq
.graph
c1_activate_ac+ c1_activate_rq-
c1_activate_rq- c1_activate_ac-
c1_activate_ac- c1_activate_rq+
c1_activate_rq+ p0 c2_out_ac-/3
c2_out_rq+ c2_out_ac+
c2_out_ac+ c2_out_rq-
c2_out_rq- c2_out_ac-/1
c2_out_ac- c2_out_rq+
c2_out_ac-/1 c1_activate_ac+
c2_out_rq-/1 c2_out_ac-
c2_out_ac+/1 c2_out_rq-/1
c2_out_rq+/1 c2_out_ac+/1
c2_out_ac-/2 c2_out_rq+/1
c2_out_rq-/2 p1
c2_out_ac+/2 c2_out_rq-/2
c2_out_rq+/2 c2_out_ac+/2
c2_out_ac-/3 p0 c2_out_ac-/2
p0 c2_out_rq+/2
p1 c2_out_ac-/2 c2_out_ac-/3
.marking { <c1_activate_ac-,c1_activate_rq+> }
.end
