package com.imdroid.algorithm.dbscan;

import com.imdroid.pojo.bo.Point3D;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.utils.PointUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class  DBscan {
    private final static double radius = 0.04;
    private final static int minp = 35;
    private double distance;
    public List<BlkPoint> process(List<BlkPoint> points, double distance) {
        this.distance = distance;
        int size = points.size();
        int idx = 0;
        int cluster = 1;
        while (idx<size) {
            BlkPoint p = points.get(idx++);
            //choose an unvisited point
            if (!p.isVisited()) {
                p.setVisited(true);//set visited
                ArrayList<BlkPoint> adjacentPoints = getAdjacentPoints(p, points);
                //set the point which adjacent points less than minPts noised
                if (adjacentPoints != null && adjacentPoints.size() < minp) {
                    p.setNoised(true);
                } else {
                    p.setCluster(cluster);
                    for (int i = 0; i < adjacentPoints.size(); i++) {
                        BlkPoint adjacentPoint = adjacentPoints.get(i);
                        //only check unvisited point, cause only unvisited have the chance to add new adjacent points
                        if (!(adjacentPoint).isVisited()) {
                            ( adjacentPoint).setVisited(true);
                            ArrayList<BlkPoint> adjacentAdjacentPoints = getAdjacentPoints(adjacentPoint, points);
                            //add point which adjacent points not less than minPts noised
                            if (adjacentAdjacentPoints != null && adjacentAdjacentPoints.size() >= minp) {
                                adjacentPoints.addAll(adjacentAdjacentPoints);
                            }
                        }
                        //add point which doest not belong to any cluster
                        if (adjacentPoint.getCluster() == 0) {
                            adjacentPoint.setCluster(cluster);
                            //set point which marked noised before non-noised
                            if (adjacentPoint.isNoised()) {
                                adjacentPoint.setNoised(false);
                            }
                        }
                    }
                    cluster++;
                }
            }
        }
        Iterator<BlkPoint> iterator = points.iterator();
        while (iterator.hasNext()){
            Point3D p = iterator.next();
            if (p.isNoised()){
                iterator.remove();
            }
        }
        return points;
    }

    private ArrayList<BlkPoint> getAdjacentPoints(BlkPoint centerPoint,List<BlkPoint> points) {
        ArrayList<BlkPoint> adjacentPoints = new ArrayList<BlkPoint>();
        int size = points.size();
        for (BlkPoint p:points) {
            //include centerPoint itself
            double distance = centerPoint.getDistance(p);
            if (size < 10000)
            {
                if (distance <= PointUtil.getRadiusByDistance(p, this.distance)) {
                    adjacentPoints.add(p);
                }
            }
            else {
                if (distance<=radius) {
                    adjacentPoints.add(p);
                }
            }
        }
        return adjacentPoints;
    }


}

