package org.workcraft.plugins.circuit.routing.impl;

import java.util.List;
import java.util.PriorityQueue;

import org.workcraft.plugins.circuit.routing.basic.IndexedPoint;
import org.workcraft.plugins.circuit.routing.basic.PointToVisit;

public class DijkstraRouter extends AbstractRoutingAlgorithm {

    private double[][] scores;
    private boolean[][] visited;

    private IndexedPoint[][] sourceCells;

    private IndexedPoint source;
    private IndexedPoint destination;

    @Override
    protected List<IndexedPoint> findPath(IndexedPoint source, IndexedPoint destination) {
        this.source = source;
        this.destination = destination;
        visited = new boolean[width][height];
        scores = new double[width][height];
        sourceCells = new IndexedPoint[width][height];
        solve();
        return buildPath(source, sourceCells);
    }


    private void solve() {
        final PriorityQueue<PointToVisit> visitQueue = new PriorityQueue<PointToVisit>();
        visitQueue.add(new PointToVisit(1.0, destination));

        while (!visitQueue.isEmpty()) {
            final PointToVisit visitPoint = visitQueue.poll();
            visited[visitPoint.getLocation().getX()][visitPoint.getLocation().getY()] = true;
            if (visitPoint.getLocation().equals(source)) {
                return;
            }

            IndexedPoint lastPoint = sourceCells[visitPoint.getLocation().getX()][visitPoint.getLocation().getY()];
            if (lastPoint == null) {
                lastPoint = visitPoint.getLocation();
            }

            checkDirection(visitQueue, visitPoint.getScore(), lastPoint, visitPoint.getLocation(), 1, 0);
            checkDirection(visitQueue, visitPoint.getScore(), lastPoint, visitPoint.getLocation(), -1, 0);
            checkDirection(visitQueue, visitPoint.getScore(), lastPoint, visitPoint.getLocation(), 0, 1);
            checkDirection(visitQueue, visitPoint.getScore(), lastPoint, visitPoint.getLocation(), 0, -1);
        }
    }

    private void checkDirection(PriorityQueue<PointToVisit> visitQueue, double score, IndexedPoint lastPoint,
            IndexedPoint point, int dx, int dy) {

        final int newX = point.getX() + dx;
        final int newY = point.getY() + dy;
        Double newScore = analyser.getMovementCost(lastPoint.getX(), lastPoint.getY(), point.getX(), point.getY(), dx, dy);

        if (newScore != null) {
            if (visited[newX][newY]) {
                return;
            }

            newScore += score;
            if (scores[newX][newY] == 0 || newScore < scores[newX][newY]) {
                scores[newX][newY] = newScore;
                sourceCells[newX][newY] = point;
                visitQueue.add(new PointToVisit(newScore, IndexedPoint.create(newX, newY)));
            }
        }
    }

}
