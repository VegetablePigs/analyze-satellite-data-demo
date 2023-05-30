package com.jyd.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AnalyzingHDF5FileUtil {

    public static void analyzeHDF5File(String filePath,String storagePath) throws IOException {
        try {
            File file = new File(filePath);
            NetcdfFile ncFile = NetcdfFile.open(file.getPath());
            // 获取数据集
            Variable dataVar = ncFile.findVariable("channel16_13300nm_2km");
            // 读取数据
            Array data = dataVar.read();
            ncFile.close();
            // 将数据转换为Java数组
            int[] shape = dataVar.getShape();
            float[][] dataArray = new float[shape[0]][shape[1]];
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    dataArray[i][j] = data.getFloat(i * shape[1] + j);
                }
            }
            // 绘制卫星图 灰度图
            JFreeChart chart = ChartFactory.createXYLineChart("", "", "", new DefaultXYDataset(), PlotOrientation.VERTICAL, false, true, false);
            chart.setBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0));
            BufferedImage image = chart.createBufferedImage(shape[1], shape[0]);
            float max = -Float.MAX_VALUE;
            float min = Float.MAX_VALUE;
            // 找到最大值和最小值
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    if (dataArray[i][j] > max) {
                        max = dataArray[i][j];
                    }
                    if (dataArray[i][j] < min) {
                        min = dataArray[i][j];
                    }
                }
            }
            float range = max - min;
            // 将数据转换为白色HSB颜色
            for (int i = 0; i < shape[0]; i++) {
                for (int j = 0; j < shape[1]; j++) {
                    float value = (dataArray[i][j] - min) / range;
                    value *= 100.0f;
                    Color color = new Color(1.0f, 1.0f, 1.0f, value/100.0f);
                    image.setRGB(j, i, color.getRGB());
                }
            }
            // 切出1058×840的图片，以圆心切（+,+）坐标系的图片
            int x = 1058;
            int y = 840;
            // 中心点
            int x0 = shape[1] / 2;
            int y0 = shape[0] / 2;
            // 正坐标点
            int x1 = x0 - x / 2;
            int y1 = y0 - y / 2;
            BufferedImage image1 = new BufferedImage(x, y, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image1.createGraphics();
            graphics.drawImage(image, 0, 0, x, y, x1, y1, x1 + x, y1 + y, null);
            graphics.dispose();
            image = image1;


            // 保存卫星图
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            ImageIO.write(image, "png", new File(storagePath + "/" + time + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
