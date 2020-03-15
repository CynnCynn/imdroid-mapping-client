package com.imdroid.utils;

import com.alibaba.fastjson.JSON;
import com.imdroid.enums.ImageEnum;
import com.imdroid.enums.PointTypeEnum;
import com.imdroid.enums.QuotaEnum;
import com.imdroid.pojo.bo.*;
import com.imdroid.pojo.bo.Point;
import com.imdroid.pojo.entity.BlkPoint;
import com.imdroid.pojo.entity.QuotaData;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import java.awt.*;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.imdroid.pojo.bo.Const.Coordinate;

/**
 * @Description:图片操作工具类
 * @Author: iceh
 * @Date: create in 2018-09-26 16:04
 * @Modified By:
 */
@Slf4j
public class ImageUtil {
    /**
     * 利用jfreechart生成散点图的图片
     */
    public static void scatterPlot(@NonNull Wall wall, int width, int height) {
        String imageUrl = wall.getImagePath();
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        //根据给定二元数组生成图片
        double[][] dataSet = getDataSet(wall.getPoints(), wall.getCoordinate());
        xyDataSet.addSeries("", dataSet);
        //生成无标题、x轴、y轴的报表
        JFreeChart chart = ChartFactory.createScatterPlot("", "", "",
                xyDataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        setBackgroundBlank(chart);
        try {
            //根据指定位置、宽、高生成jpg图片
            ChartUtils.saveChartAsJPEG(new File(imageUrl), chart, width, height);
        } catch (IOException e) {
            log.error("生成图片失败,图片为：" + imageUrl, e);
        }
    }

    /**
     * 默认生成方式：0.25倍，有框线
     *
     * @param chart
     * @param wall
     *
     */
    public static void scatterPlot(@NonNull JFreeChart chart, @NonNull Wall wall, Integer type) {
        scatterPlot(chart, wall, 0.35, false, type);
    }


    /**
     * 利用 jfreechart生成散点图
     *
     * @param chart
     * @param wall
     * @param isBlank
     */
    public static void scatterPlot(@NonNull JFreeChart chart, @NonNull Wall wall, Double proportion, boolean isBlank, Integer type) {
        File imageFile = null;

        if (ImageEnum.FLATNESS.getCode().equals(type)) {
            imageFile = new File(wall.getImagePath());
        } else if (ImageEnum.VERTICAL.getCode().equals(type)) {
            imageFile = new File(wall.getImagePath2());
        }
        try {
            if (isBlank) {
                setBackgroundBlank(chart);
            }
            if (!imageFile.getParentFile().exists()) {
                imageFile.getParentFile().mkdirs();
            }
            //根据墙的边界值及图片比例，设置图片的高和宽
            int width = 1, height = 1;
            double[][] bound = wall.getBound();
            switch (wall.getCoordinate()) {
                case Coordinate.X:
                    width = (int) Math.round((bound[1][1] - bound[1][0]) * 1000 * proportion);
                    height = (int) Math.round((bound[2][1] - bound[2][0]) * 1000 * proportion);
                    break;
                case Coordinate.Y:
                    width = (int) Math.round((bound[0][1] - bound[0][0]) * 1000 * proportion);
                    height = (int) Math.round((bound[2][1] - bound[2][0]) * 1000 * proportion);
                    break;
                case Coordinate.Z:
                    width = (int) Math.round((bound[0][1] - bound[0][0]) * 1000 * proportion);
                    height = (int) Math.round((bound[1][1] - bound[1][0]) * 1000 * proportion);
                    break;
            }
            ChartUtils.saveChartAsJPEG(imageFile, chart, width, height);
        } catch (IOException e) {
            log.error("生成图片失败,图片为：" + imageFile, e);
        }
    }

    /**
     * 将报表的背景设为空白的
     *
     * @param chart 设置的报表
     */
    private static void setBackgroundBlank(JFreeChart chart) {
        //背景颜色
        Color color = ChartColor.WHITE;
        //设置报表边界、背景
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(color);
        chart.setBackgroundImageAlpha(0.0f);
        XYPlot plot = chart.getXYPlot();
        //设置xy轴线
        plot.setOutlinePaint(null);
        plot.setBackgroundPaint(color);
        plot.setRangeGridlinePaint(color);
        //设置x轴与报表边界
        ValueAxis domainAxis = plot.getDomainAxis();
        domainAxis.setVisible(false);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        //设置y轴与报表边界
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setVisible(false);
        rangeAxis.setLowerMargin(0.0);
        rangeAxis.setUpperMargin(0.0);
    }

    //-------------- 数据准备 ----------------

    public static <T extends Point3D> double[][] getDataSet(List<T> point3DList, String coordinate) {
        double[][] dataSet = new double[2][point3DList.size()];
        for (int i = 0; i < point3DList.size(); i++) {
            T point3D = point3DList.get(i);
            if (Coordinate.Z.equals(coordinate)) {
                dataSet[0][i] = point3D.getX();   //x轴
                dataSet[1][i] = point3D.getY();   //y轴
            } else if (Coordinate.X.equals(coordinate)) {
                dataSet[0][i] = point3D.getY();   //y轴
                dataSet[1][i] = point3D.getZ();   //z轴
            } else if (Coordinate.Y.equals(coordinate)) {
                dataSet[0][i] = point3D.getX();   //x轴
                dataSet[1][i] = point3D.getZ();   //z轴
            }
        }
        return dataSet;
    }

    public static <T extends Point2D> double[][] getDataSet(List<T> point2DS){
        double[][] dataSet = new double[2][point2DS.size()];
        for (int i = 0; i < point2DS.size(); i++) {
            T point2D = point2DS.get(i);
            dataSet[0][i] = point2D.getX();   //x轴
            dataSet[1][i] = point2D.getY();   //y轴
        }
        return dataSet;
    }
    public static StandardChartTheme getStandardChartTheme() {
        //创建主题样式
        StandardChartTheme standardChartTheme = new StandardChartTheme("CN");
        //设置标题字体
        standardChartTheme.setExtraLargeFont(new Font("隶书", Font.BOLD, 20));
        //设置图例的字体
        standardChartTheme.setRegularFont(new Font("宋书", Font.PLAIN, 16));
        //设置轴向的字体
        standardChartTheme.setLargeFont(new Font("宋书", Font.PLAIN, 16));
        return standardChartTheme;
    }

    public static void scatterPlot(OverView overView, Double proportion){

        StandardChartTheme standardChartTheme = ImageUtil.getStandardChartTheme();
        ChartFactory.setChartTheme(standardChartTheme);
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
//        门洞
        xyDataSet.addSeries(PointTypeEnum.DOOR_HOLE.getMeaning(), ImageUtil.getDataSet(overView.getHolesPoints()));
//        墙面俯视图数据点
        xyDataSet.addSeries("墙", ImageUtil.getDataSet(overView.getWallsPoint()));
        List<Point2D> scanner = new ArrayList<>();
        scanner.add(new Point2D(0.0,0.0));
        xyDataSet.addSeries("扫描仪", ImageUtil.getDataSet(scanner));


//      设置图片宽度高度和比例
        int width = 1, height = 1;
        double xMax = 0, xMin = xMax;
        double yMax = 0, yMin = yMax;
        for(Text text:overView.getTexts()){
//            System.out.println(text.getText());
            double currentX = text.getX();
            double currentY = text.getY();
            if (xMax < currentX) {
                xMax = currentX;
            }
            if (xMin > currentX) {
                xMin = currentX;
            }
            if (yMax < currentY) {
                yMax = currentY;
            }
            if (yMin > currentY) {
                yMin = currentY;
            }
        }


        width = (int) Math.round((xMax - xMin + 0.5) * 1000 * proportion);
        height = (int) Math.round((yMax - yMin) * 1000 * proportion);

        int fontSize = Math.round(Math.min(width, height))/20;

        JFreeChart chart = ChartFactory.createScatterPlot("俯视图", "X", "Y",
                xyDataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

//        设置点的形状
        XYItemRenderer renderer = (XYItemRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesShape(0, new Rectangle2D.Double(-10D, -10D, 20D, 20D));
        renderer.setSeriesShape(1, new Rectangle2D.Double(-5D, -5D, 10D, 10D));
        renderer.setSeriesShape(2, new Rectangle2D.Double(-5D, -5D, 10D, 10D));
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, Color.BLACK);
        for(Text text: overView.getTexts()){
            XYTextAnnotation t = new XYTextAnnotation(text.getText(), text.getX(), text.getY());
            t.setFont(new Font("宋书", Font.PLAIN, fontSize));
            XYPlot xyplot = (XYPlot) chart.getPlot();
            xyplot.addAnnotation(t);
        }
        File imageFile = new File(overView.getImagePath());
        try {
            if(!imageFile.getParentFile().exists()){
                imageFile.getParentFile().mkdir();
            }
            ChartUtils.saveChartAsJPEG(imageFile, chart, width, height);
            log.info("俯视图生成完成");
        } catch (IOException e) {
            log.error(e.toString());
        }

    }

    public static void generateImage(@NonNull Wall wall) {
        String xAxisLabel = "", yAxisLabel = "", title = "";
        String coordinate = wall.getCoordinate();
        //设置报表字体样式以解决中文乱码
        StandardChartTheme standardChartTheme = ImageUtil.getStandardChartTheme();
        ChartFactory.setChartTheme(standardChartTheme);
        //根据坐标类型 构造chart所需参数
        if (Coordinate.Z.equals(coordinate)) {
            xAxisLabel = Coordinate.X;
            yAxisLabel = Coordinate.Y;
        } else if (Coordinate.X.equals(coordinate)) {
            xAxisLabel = Coordinate.Y;
            yAxisLabel = Coordinate.Z;
        } else if (Coordinate.Y.equals(coordinate)) {
            xAxisLabel = Coordinate.X;
            yAxisLabel = Coordinate.Z;
        }
        //根据指标类型，组拼title
        for (QuotaData quotaData : wall.getQuotaDataList()) {
            Integer code = quotaData.getQuotaType();
            String quotaName = EnumUtil.getByCode(code, QuotaEnum.class).getMeaning();
            if (code.equals(QuotaEnum.SQUARE.getCode())) {
                quotaName = "与" + quotaData.getAssociateName() + quotaName + "为";
            }
            title += quotaName + ":" + quotaData.getQuotaValue() + ";";
        }
        //构造数据集
        DefaultXYDataset xyDataSet = new DefaultXYDataset();
        DefaultXYDataset xyDataSet2 = new DefaultXYDataset();

        List<BlkPoint> raise = new ArrayList<>();
        List<BlkPoint> sag = new ArrayList<>();
        List<BlkPoint> qualified = new ArrayList<>();
        List<BlkPoint> qualified_v = new ArrayList<>();
//        List<BlkPoint> doorHole = new ArrayList<>();
//        List<BlkPoint> windowHole = new ArrayList<>();
//        List<BlkPoint> outpoint = new ArrayList<>();
        List<BlkPoint> raise_v = new ArrayList<>();
        List<BlkPoint> sag_v = new ArrayList<>();
        for (BlkPoint blkPoint : wall.getPoints()) {
            Integer type = blkPoint.getType();
            if (PointTypeEnum.RAISE_FLAT.getCode().equals(type) ||
                    PointTypeEnum.RAISE.getCode().equals(type)) {
                raise.add(blkPoint);
            } else if (PointTypeEnum.SAG_FLAT.getCode().equals(type) ||
                    PointTypeEnum.SAG.getCode().equals(type)) {
                sag.add(blkPoint);
            } else {
                qualified.add(blkPoint);
            }
//            else if(PointTypeEnum.BOUND_POINT.getCode().equals(type))
//                qualified.add(blkPoint);

//            if (PointTypeEnum.QUALIFIED.getCode().equals(type)||
//                    PointTypeEnum.BASIS.getCode().equals(type))

//            else if (PointTypeEnum.DOOR_HOLE.getCode().equals(type)) {
//                doorHole.add(blkPoint);
//            } else if (PointTypeEnum.WINDOW_HOLE.getCode().equals(type)) {
//                windowHole.add(blkPoint);
//            } else if (PointTypeEnum.OUT_POINT.getCode().equals(type)){
//                outpoint.add(blkPoint);
//            }
            if (PointTypeEnum.RAISE_VERTICAL.getCode().equals(type) ||
                    PointTypeEnum.RAISE.getCode().equals(type)) {
                raise_v.add(blkPoint);
            } else if (PointTypeEnum.SAG_VERTICAL.getCode().equals(type) ||
                    PointTypeEnum.SAG.getCode().equals(type)) {
                sag_v.add(blkPoint);
            } else {
                qualified_v.add(blkPoint);
            }
        }


        xyDataSet.addSeries(PointTypeEnum.RAISE.getMeaning(), ImageUtil.getDataSet(raise, coordinate));
        xyDataSet.addSeries(PointTypeEnum.SAG.getMeaning(), ImageUtil.getDataSet(sag, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.RAISE_VERTICAL.getMeaning(), ImageUtil.getDataSet(raise_v, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.SAG_VERTICAL.getMeaning(), ImageUtil.getDataSet(sag_v, coordinate));
        xyDataSet.addSeries(PointTypeEnum.QUALIFIED.getMeaning(), ImageUtil.getDataSet(qualified, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.DOOR_HOLE.getMeaning(), ImageUtil.getDataSet(doorHole, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.WINDOW_HOLE.getMeaning(), ImageUtil.getDataSet(windowHole, coordinate));
//        xyDataSet.addSeries(PointTypeEnum.OUT_POINT.getMeaning(), ImageUtil.getDataSet(outpoint, coordinate));

        JFreeChart chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
                xyDataSet,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        XYItemRenderer renderer = (XYItemRenderer) chart.getXYPlot().getRenderer();
        renderer.setSeriesShape(0, new QuadCurve2D.Double(-5D, 0D, 0D, -20D, 5D, 0D));
        renderer.setSeriesShape(1, new QuadCurve2D.Double(-5D, 0D, 0D, 20D, 5D, 0D));
        renderer.setSeriesShape(2, new Rectangle2D.Double(-2D, -2D, 4D, 4D));
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesPaint(2, Color.GREEN);
        chart.getXYPlot().setRenderer(renderer);

        xyDataSet2.addSeries(PointTypeEnum.RAISE.getMeaning(), ImageUtil.getDataSet(raise_v, coordinate));
        xyDataSet2.addSeries(PointTypeEnum.SAG.getMeaning(), ImageUtil.getDataSet(sag_v, coordinate));
        xyDataSet2.addSeries(PointTypeEnum.QUALIFIED.getMeaning(), ImageUtil.getDataSet(qualified_v, coordinate));
        JFreeChart chart2 = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel,
                xyDataSet2,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart2.getXYPlot().setRenderer(renderer);

        ImageUtil.scatterPlot(chart, wall, ImageEnum.FLATNESS.getCode());
        ImageUtil.scatterPlot(chart2, wall, ImageEnum.VERTICAL.getCode());
    }
}

