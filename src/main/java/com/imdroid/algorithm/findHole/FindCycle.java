package com.imdroid.algorithm.findHole;

import com.imdroid.enums.PointTypeEnum;
import com.imdroid.pojo.entity.BlkPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FindCycle {
    public static List<BlkPoint> findRawEdge(Map<Double, List<BlkPoint>> map, double safeThresholdUp, double safeThresholdLow, List<BlkPoint> datumPoints){
        List<BlkPoint> nonZero = new ArrayList<>();

//        安全距离区间ceiling-threshold~threshold1
        for (Double key:map.keySet()){
//            上沿高度
            for(int i=0; i<map.get(key).size(); i++)
            {
                double z = map.get(key).get(i).getZ();

//                if(z < safeThresholdLow )
//                    break;
//              不要外飘点
                if(map.get(key).get(i).getType().equals(PointTypeEnum.OUT_POINT.getCode()))
                    continue;

                if(z < safeThresholdUp && z > safeThresholdLow)
                {
                    datumPoints.get((int)(key*10)).setX(map.get(key).get(i).getX());
                    datumPoints.get((int)(key*10)).setY(map.get(key).get(i).getY());
                    datumPoints.get((int)(key*10)).setZ(map.get(key).get(i).getZ());
                    datumPoints.get((int)(key*10)).setR(map.get(key).get(i).getR());
                    nonZero.add(datumPoints.get((int)(key*10)));
                    break;
                }
            }
        }
        return nonZero;
    }
    public static List<BlkPoint> findCycle(Map<Double, List<BlkPoint>> map, double safeThresholdUp, double safeThresholdLow){
        List<BlkPoint> nonZero = new ArrayList<>();
//        安全距离区间ceiling-threshold~threshold1
        for (Double key:map.keySet()){
//            上沿高度
            for(int i=0; i<map.get(key).size(); i++)
            {
                if(map.get(key).get(i).getZ() < safeThresholdLow )
                    break;
//              不要外飘点
                if(map.get(key).get(i).getType().equals(PointTypeEnum.OUT_POINT.getCode()))
                    continue;

                if(map.get(key).get(i).getZ() < safeThresholdUp)
                {
                    nonZero.add(map.get(key).get(i));
                    break;
                }
            }
        }
        return nonZero;
    }
}
