package com.imdroid.pojo.bo;

import com.imdroid.pojo.entity.BlkPoint;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-12-11 12:51
 * @Modified By:
 */
@Getter
@ToString(exclude = "remainPoints")
public class StationCalculation {
    private List<Wall> walls = new ArrayList<>();
    private List<BlkPoint> remainPoints = new ArrayList<>();
}
