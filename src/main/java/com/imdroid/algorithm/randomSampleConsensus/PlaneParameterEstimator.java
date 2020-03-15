package com.imdroid.algorithm.randomSampleConsensus;

import Jama.Matrix;
import com.imdroid.pojo.bo.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-11-15 19:55
 * @Modified By:
 */
public class PlaneParameterEstimator implements ParameterEstimator<Point3D, Double> {
    private double deltaSquared;

    public PlaneParameterEstimator(double delta) {
        this.deltaSquared = delta * delta;
    }

    @Override
    public List<Double> estimate(List<Point3D> data) {
        if (data.size() < 3) {
            return null;
        }
        double nX = (data.get(1).getY() - data.get(0).getY()) * (data.get(2).getZ() - data.get(0).getZ())
                - (data.get(2).getY() - data.get(0).getY()) * (data.get(1).getZ() - data.get(0).getZ());
        double nY = (data.get(1).getX() - data.get(0).getX()) * (data.get(2).getZ() - data.get(0).getZ())
                - (data.get(2).getX() - data.get(0).getX()) * (data.get(1).getZ() - data.get(0).getZ());
        double nZ = (data.get(1).getX() - data.get(0).getX()) * (data.get(2).getY() - data.get(0).getY())
                - (data.get(2).getX() - data.get(0).getX()) * (data.get(1).getY() - data.get(0).getY());
        double norm = Math.sqrt(nX * nX + nY * nY + nZ * nZ);
        List<Double> params = new ArrayList<Double>();
        params.add(nX / norm);
        params.add(nY / norm);
        params.add(nZ / norm);
        params.add(data.get(0).getX());
        params.add(data.get(0).getY());
        params.add(data.get(0).getZ());
        return params;
    }

    @Override
    public List<Double> leastSquaresEstimate(List<Point3D> data) {
        int dataSize = data.size();
        if (dataSize < 3) {
            return null;
        }
        if (dataSize == 3) {
            return estimate(data);
        }
        double nX, nY, nZ, norm;
        double meanX = 0.0;
        double meanY = 0.0;
        double meanZ = 0.0;
        // The entries of the symmetric covariance matrix
        double covMat11 = 0.0;
        double covMat12 = 0.0;
        double covMat13 = 0.0;
        double covMat21 = 0.0;
        double covMat22 = 0.0;
        double covMat23 = 0.0;
        double covMat31 = 0.0;
        double covMat32 = 0.0;
        double covMat33 = 0.0;
        for (int i = 0; i < dataSize; i++) {
            meanX += data.get(i).getX();
            meanY += data.get(i).getY();
            meanZ += data.get(i).getZ();

            covMat11 += data.get(i).getX() * data.get(i).getX();
            covMat12 += data.get(i).getX() * data.get(i).getY();
            covMat13 += data.get(i).getX() * data.get(i).getZ();
            covMat22 += data.get(i).getY() * data.get(i).getY();
            covMat23 += data.get(i).getY() * data.get(i).getZ();
            covMat33 += data.get(i).getZ() * data.get(i).getZ();
        }

        meanX /= dataSize;
        meanY /= dataSize;
        meanZ /= dataSize;

        covMat11 -= dataSize * meanX * meanX;
        covMat12 -= dataSize * meanX * meanY;
        covMat13 -= dataSize * meanX * meanZ;
        covMat22 -= dataSize * meanY * meanY;
        covMat23 -= dataSize * meanY * meanZ;
        covMat33 -= dataSize * meanZ * meanZ;
        covMat21 = covMat12;
        covMat31 = covMat13;
        covMat32 = covMat23;

        if (covMat11 < 1e-12) {
            nX = 1.0;
            nY = 0.0;
            nZ = 0.0;
        } else if (covMat22 < 1e-12) {
            nX = 0.0;
            nY = 1.0;
            nZ = 0.0;
        } else if (covMat33 < 1e-12) {
            nX = 0.0;
            nY = 0.0;
            nZ = 1.0;
        } else {
            // lamda1 is the largest eigen-value of the covariance matrix
            // and is used to compute the eigen-vector corresponding to the
            // smallest eigenvalue, which isn't computed explicitly.
            double[][] array = {
                    {covMat11 / dataSize, covMat12 / dataSize, covMat13 / dataSize},
                    {covMat21 / dataSize, covMat22 / dataSize, covMat23 / dataSize},
                    {covMat31 / dataSize, covMat32 / dataSize, covMat33 / dataSize}};
//			double [][] array = new double[dataSize][3];
//			for(int i = 0; i < dataSize; i++) {
//				array[i][0] = data.get(i).getX() - meanX;
//				array[i][1] = data.get(i).getY() - meanY;
//				array[i][2] = data.get(i).getZ() - meanZ;
//			}
            Matrix parameter = new Matrix(array);
            //对矩阵做特征分解,并取其特征向量构成的矩阵
            Matrix feature = parameter.svd().getV();
//            parameter.svd().getS().print(3, 3);
//            parameter.svd().getV().print(3, 3);
            //取奇异值最小对应的v的右奇异向量
            double[] coefficient = feature.getMatrix(0, 2, 2, 2).getColumnPackedCopy();
            nX = coefficient[0];
            nY = coefficient[1];
            nZ = coefficient[2];
        }
        ArrayList<Double> parameters = new ArrayList<Double>();
        parameters.add(nX);
        parameters.add(nY);
        parameters.add(nZ);
        parameters.add(meanX);
        parameters.add(meanY);
        parameters.add(meanZ);
        return parameters;
    }

    @Override
    public boolean agree(List<Double> parameters, Point3D data) {
        double nX = parameters.get(0);
        double nY = parameters.get(1);
        double nZ = parameters.get(2);
        double aX = parameters.get(3);
        double aY = parameters.get(4);
        double aZ = parameters.get(5);
        double pX = data.getX();
        double pY = data.getY();
        double pZ = data.getZ();
        double d = nX * (pX - aX) + nY * (pY - aY) + nZ * (pZ - aZ);
        return ((d * d) < deltaSquared);
    }
}
