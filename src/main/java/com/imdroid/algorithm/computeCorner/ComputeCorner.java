package com.imdroid.algorithm.computeCorner;

import com.imdroid.algorithm.findHole.FindHole;
import com.imdroid.enums.AssociateEnum;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.enums.QuotaEnum;
import com.imdroid.pojo.bo.Const;
import com.imdroid.pojo.bo.Plane;
import com.imdroid.pojo.bo.Wall;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.QuotaData;
import com.imdroid.utils.BigDecimalUtil;
import com.imdroid.utils.PlaneUtil;
import com.imdroid.utils.PointUtil;
import com.imdroid.utils.WallUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 计算阴阳角
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ComputeCorner {
    public static List<QuotaData> computeCorner(List<Wall> walls, List<BlkPoint> datumPoints){
        List<QuotaData> cornerList = new ArrayList<>();
        Map <String, List<BlkPoint>> cornerPoints = getCornerPoints(walls);

        String[] keySet = cornerPoints.keySet().toArray(new String[0]);
        for(String keyS2:keySet){
            String[] strings2 = keyS2.split("-");
            if(strings2[0].contains("S2.")) {
                double phi = Double.parseDouble(strings2[1]);
                for (String keyS3:keySet){
                    String[] strings3 = keyS3.split("-");
                    if(strings3[0].contains("S3.") && Math.abs(Double.parseDouble(strings3[1])-phi)<1){
//                        计算keyS3和keyS2的夹角
                        Plane plane1 = PlaneUtil.planeFitting(cornerPoints.get(keyS2));
                        Plane plane2 = PlaneUtil.planeFitting(cornerPoints.get(keyS3));
                        double angle = plane1.getAngle(plane2);

//                        判断是阴角还是阳角
                        double intersectR = datumPoints.get((int)(BigDecimalUtil.scale(Double.parseDouble(strings3[1]),1)*10)).getR();
                        double sumR = 0.0;
                        for(BlkPoint blkPoint:cornerPoints.get(keyS3)){
                            sumR += blkPoint.getR();
                        }
                        double averR = sumR / cornerPoints.get(keyS3).size();
                        if(averR < intersectR){
//                            阴角
                            QuotaData quotaData = new QuotaData(angle, QuotaEnum.INTERNAL_CORNER.getCode(), 1L, 1L, AssociateEnum.WALL_DATA.getCode(), (strings2[0] + " " + strings3[0]));
                            cornerList.add(quotaData);
                        }
                        else{
                            QuotaData quotaData = new QuotaData(360-angle, QuotaEnum.EXTERNAL_CORNER.getCode(), 1L, 1L, AssociateEnum.WALL_DATA.getCode(), (strings2[0] + " " + strings3[0]));
                            cornerList.add(quotaData);
                        }
                        //S2.1-115.6 S3.2-115.5 90.72742017566176Plane(normalVector=Vector3D(x=0.9999173818526255, y=-0.012596967612073413, z=-0.002558490955860854), intercept=1.2110520730924696)Plane(normalVector=Vector3D(x=-9.623796476155155E-5, y=0.9999995757733252, z=9.160739183194683E-4), intercept=-2.4712505452866877)
                        //S2.1-233.9 S3.1-234.1 92.4929784635413Plane(normalVector=Vector3D(x=-0.9999302898528997, y=-0.011804010164913307, z=2.8421597842537657E-4), intercept=-1.198091716286318)Plane(normalVector=Vector3D(x=0.031701320088916504, y=0.9994970623072086, z=8.054463855456052E-4), intercept=1.6573939332346554)
                        //S2.2-69.4 S3.2-69.9 90.82771679203343Plane(normalVector=Vector3D(x=-0.9999841539322298, y=-0.004923422508539086, z=0.002729797656326138), intercept=0.9253081600258374)Plane(normalVector=Vector3D(x=0.009525237433693073, y=0.9999542464512938, z=8.802589716163008E-4), intercept=-2.4764762702649588)
                        //S2.3-321.4 S3.1-321.1 88.14461302236607Plane(normalVector=Vector3D(x=-0.9999272506662117, y=0.011725596830953342, z=-0.002829090678846812), intercept=2.0416476668567594)Plane(normalVector=Vector3D(x=-0.020654901350946952, y=0.999786651635991, z=-1.620514622617349E-4), intercept=1.664743480249843)
                    }
                }
            }
        }

        return cornerList;
    }

    private static Map <String, List<BlkPoint>> getCornerPoints(List<Wall> walls){
        Map <String, List<BlkPoint>> cornerPoints = new TreeMap<>();
        for(Wall wall:walls){

//            WallUtil.symmetry(wall);
            Map<Double, List<BlkPoint>> map;
            if(wall.getCoordinate().equals(Const.Coordinate.X)) {
                map = PointUtil.groupByCoordinate(wall.getPoints(), Const.Coordinate.Y);
            }
            else if(wall.getCoordinate().equals(Const.Coordinate.Y)){
                map = PointUtil.groupByCoordinate(wall.getPoints(), Const.Coordinate.X);
            }
            else
                continue;
            List<BlkPoint> startList = new ArrayList<>();
            List<BlkPoint> endList = new ArrayList<>();
            String keyString = wall.getName();
            double startPhi = 0, endPhi = 0, startCoorVal = 0, endCoorVal = 0;

//            先找出边界
            int flag = 0;
            for(Double key:map.keySet()) {
                if(flag == 0){
                    startPhi = map.get(key).get(0).getPhi();
                    startCoorVal = key;
                    flag = 1;
                }
                endCoorVal = key;
                endPhi = map.get(key).get(0).getPhi();
            }

            for(Double key:map.keySet()) {
                if (key < startCoorVal + 0.1) {
//                    List<BlkPoint> subList = cornerPoints.get(wall.getName());
//                    if (null == subList) {
//                        subList = new ArrayList<>();
//                    }
                    startList.addAll(map.get(key));
//                    cornerPoints.addAll(map.get(key));
                }
                if(key>endCoorVal - 0.1)
                    endList.addAll(map.get(key));
            }
            cornerPoints.put(keyString+"-"+startPhi, startList);
            cornerPoints.put(keyString+"-"+endPhi, endList);


//            WallUtil.symmetry(wall);
        }
//        for(String key:cornerPoints.keySet()) {
//            System.out.println(key);
//            FindHole.pointsToFile(cornerPoints.get(key), "D:\\study\\python\\fof\\cornerPoints");
//        }
        return cornerPoints;
    }

}

//S2.1-115.6
//S2.1-233.9
//S2.2-54.4
//S2.2-69.5
//S2.3-31.9
//S2.3-321.2
//S3.1-234.1
//S3.1-321.1
//S3.2-115.5
//S3.2-69.8