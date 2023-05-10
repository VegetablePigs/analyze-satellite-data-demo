package com.jyd.demo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: jyd
 * @Date: 2023/05/09 10:10
 * @Company: 广州风雨雷科技有限公司
 * @Version: 1.0
 * @Desc:
 **/
@Component
public class AnalyzingHDF5File {
    public static void main (String[] args) throws IOException, InvalidRangeException {
        String path = "src/main/resources/Z_SATE_C_RJTD_20230427053000_HD_H08_20230427_0530_B16_FLDK_R20_PGPFD.hdf5";
        File file = new File(path);
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
        JFreeChart chart = ChartFactory.createXYLineChart("", "", "", new DefaultXYDataset(), PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.white);
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
        // 将数据转换为HSB颜色
        for (int i = 0; i < shape[0]; i++) {
            for (int j = 0; j < shape[1]; j++) {
                float value = (dataArray[i][j] - min) / range;
                Color color = Color.getHSBColor(0, 0, value);
                image.setRGB(j, i, color.getRGB());
            }
        }
        // 将数据映射到HSB颜色 不是灰度
//        BufferedImage image = new BufferedImage(shape[1], shape[0], BufferedImage.TYPE_INT_RGB);
//        for (int i = 0; i < shape[0]; i++) {
//            for (int j = 0; j < shape[1]; j++) {
//                float hue = (dataArray[i][j] - 200) / 200 * 0.66f;
//                float saturation = 1.0f;
//                float brightness = 1.0f;
//                Color color = Color.getHSBColor(hue, saturation, brightness);
//                image.setRGB(j, i, color.getRGB());
//            }
//        }
        // 保存卫星图
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        ImageIO.write(image, "png", new File("satellite"+ time +".png"));

    }
}
