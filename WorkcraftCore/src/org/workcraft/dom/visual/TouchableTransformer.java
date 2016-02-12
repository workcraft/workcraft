/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.util.Geometry;

public class TouchableTransformer implements Touchable {

    private final AffineTransform transformation;
    private final Touchable toTransform;
    private final AffineTransform inverseTransformation;

    public TouchableTransformer(Touchable toTransform, AffineTransform transformation) {
        this.toTransform = toTransform;
        this.transformation = transformation;
        this.inverseTransformation = Geometry.optimisticInverse(transformation);
    }

    private void minMax(double[] x, double[] minMax) {
        minMax[0] = minMax[1] = x[0];
        for(int i=1; i<x.length; i++) {
            if(x[i]>minMax[1])
                minMax[1] = x[i];
            if(x[i]<minMax[0])
                minMax[0] = x[i];
        }
    }

    @Override
    public Rectangle2D getBoundingBox() {
        Rectangle2D bb = toTransform.getBoundingBox();

        if(bb == null)
            return null;

        Point2D[] corners = new Point2D[4];
        corners[0] = new Point2D.Double(bb.getMinX(), bb.getMinY());
        corners[1] = new Point2D.Double(bb.getMaxX(), bb.getMinY());
        corners[2] = new Point2D.Double(bb.getMinX(), bb.getMaxY());
        corners[3] = new Point2D.Double(bb.getMaxX(), bb.getMaxY());

        transformation.transform(corners, 0, corners, 0, 4);

        double[] minMaxY = new double[2];
        double[] minMaxX = new double[2];

        double[] x = new double[]{corners[0].getX(), corners[1].getX(), corners[2].getX(), corners[3].getX()};
        double[] y = new double[]{corners[0].getY(), corners[1].getY(), corners[2].getY(), corners[3].getY()};

        minMax(x, minMaxX);
        minMax(y, minMaxY);

        return new Rectangle2D.Double(minMaxX[0], minMaxY[0], minMaxX[1]-minMaxX[0], minMaxY[1]-minMaxY[0]);
    }

    @Override
    public boolean hitTest(Point2D point) {
        Point2D transformed = new Point2D.Double();
        inverseTransformation.transform(point, transformed);
        return toTransform.hitTest(transformed);
    }

    @Override
    public Point2D getCenter() {
        return transformation.transform(toTransform.getCenter(), null);
    }
}
