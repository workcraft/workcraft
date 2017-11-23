
.name MMU1
.inputs mi ri bi li
.outputs mo bo ro lo
.mode TIMED
.graph
mi+ bo+
mi+ ro+
ro+ ri+
ri+ lo+
ri- ro+
ro- ri-
bo+ bi+
bi+ lo+
bi- bo+
bo- bi-
li- lo+
lo- li-
lo+ ro-
lo+ bo-
lo+ li+
li+ mo+
mo+ lo-
mo+ mi-
mi- mo-
mo- mi+
ro- bo-
bo- lo-
mi+ bi-
bi- ro+
.marking {<lo+,ro-> <lo+,bo-> <mo+,lo-> <mo-,mi+>}
.end
