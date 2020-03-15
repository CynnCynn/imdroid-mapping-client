package com.imdroid.pojo.bo;

import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.QuotaData;
import com.sun.org.apache.bcel.internal.generic.POP2;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
@Data
public class OverView implements Serializable, Cloneable{
    private String imagePath;
    private List<Point2D> wallsPoint = new ArrayList<>();
    private List<Point2D> holesPoints = new ArrayList<>();
    private List<Text> texts = new ArrayList<>();

//    public void addWallsPoint(Point3D point3D){this.wallsPoint.add(point3D);}
    public void addAllWallsPoint(List<BlkPoint> point3DS) {
        List<Point2D> newPoint3DS = new ArrayList<>();
        for(BlkPoint blkPoint:point3DS){
            Point2D point2D = new Point2D();
            point2D.setX(blkPoint.getX());
            point2D.setY(blkPoint.getY());
            newPoint3DS.add(point2D);
        }
        this.wallsPoint.addAll(newPoint3DS);
    }
    public void addHolePoint(Point2D quotaData){this.holesPoints.add(quotaData);}
    public void addAllHolePoints(List<Point2D> holePoints){
        this.holesPoints.addAll(holePoints);
    }
    public void addTexts(Text text){this.texts.add(text);}
    public void addAllTexts(List<Text> texts){this.texts.addAll(texts);}

//    private static <T> List<T> deepCopy(List<T> src) throws IOException, ClassNotFoundException {
//        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//        ObjectOutputStream out = new ObjectOutputStream(byteOut);
//        out.writeObject(src);
//
//        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
//        ObjectInputStream in = new ObjectInputStream(byteIn);
//        @SuppressWarnings("unchecked")
//        List<T> dest = (List<T>) in.readObject();
//        return dest;
//    }
}
