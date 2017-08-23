package yh.common.utils;

import yh.config.AppProperty;
import yh.config.Context;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author zhuqi259
 */
public final class FileUtils {

    /**
     * 获取文件扩展名*
     *
     * @param fileName 文件名
     * @return 扩展名
     */
    public static String getExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i < 0) return null;
        return fileName.substring(i + 1);
    }

    /**
     * 获取文件扩展名*
     *
     * @param file 文件对象
     * @return 扩展名
     */
    public static String getExtension(File file) {
        if (file == null) return null;
        if (file.isDirectory()) return null;
        String fileName = file.getName();
        return getExtension(fileName);
    }

    /**
     * 读取文件*
     *
     * @param filePath 文件路径
     * @return 文件对象
     */
    public static File readFile(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) return null;
        if (!file.exists()) return null;
        return file;
    }

    /**
     * 复制文件
     *
     * @param oldFilePath 源文件路径
     * @param newFilePath 目标文件毒经
     * @return 是否成功
     */
    public static boolean copyFile(String oldFilePath, String newFilePath) {
        try {
            int byteRead;
            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldFilePath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newFilePath);
                byte[] buffer = new byte[1444];
                while ((byteRead = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteRead);
                }
                inStream.close();
                fs.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            // System.out.println("复制单个文件操作出错 ");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除文件
     *
     * @param filePath 文件地址
     * @return 是否成功
     */
    public static boolean delFile(String filePath) {
        return delFile(new File(filePath));
    }

    /**
     * 删除文件
     *
     * @param fileRelativePath 文件相对地址
     * @return 是否成功
     */
    public static boolean delFileRelative(String fileRelativePath) {
        String saveFilePath = getSaveFilePath();
        return delFile(new File(saveFilePath + fileRelativePath));
    }

    /**
     * 删除文件
     *
     * @param file 文件对象
     * @return 是否成功
     */
    public static boolean delFile(File file) {
        return file.exists() && file.delete();
    }

    /**
     * png图片转jpg*
     *
     * @param pngImage png图片对象
     * @param jpegFile jpg图片对象
     * @return 转换是否成功
     */
    public static boolean png2jpeg(File pngImage, File jpegFile) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(pngImage);
            BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
            ImageIO.write(bufferedImage, "jpg", jpegFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断文件是否是图片*
     *
     * @param imgFile 文件对象
     * @return boolean
     */
    public static boolean isImage(File imgFile) {
        try {
            BufferedImage image = ImageIO.read(imgFile);
            return image != null;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断文件是否是视频*
     *
     * @param videoFile 文件对象
     * @return boolean
     */
    public static boolean isVideo(File videoFile) {
        try {
            FileType type = getType(videoFile);
            return type == FileType.AVI ||
                    type == FileType.RAM ||
                    type == FileType.RM ||
                    type == FileType.MOV ||
                    type == FileType.ASF ||
                    type == FileType.MPG;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据系统当前时间，返回时间层次的文件夹结果，如：upload/2015/01/18/0.jpg
     *
     * @return String
     */
    public static String getTimeFilePath() {
        return new SimpleDateFormat("/yyyy/MM/dd").format(new Date()) + "/";
    }

    /**
     * 将文件头转换成16进制字符串
     *
     * @param src 原生byte
     * @return 16进制字符串
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 得到文件头
     *
     * @param file 文件
     * @return 文件头
     * @throws IOException
     */
    private static String getFileContent(File file) throws IOException {
        byte[] b = new byte[28];
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            inputStream.read(b, 0, 28);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return bytesToHexString(b);
    }


    /**
     * 判断文件类型
     *
     * @param file 文件
     * @return 文件类型
     */
    public static FileType getType(File file) throws IOException {
        String fileHead = getFileContent(file);
        if (fileHead == null || fileHead.length() == 0) {
            return null;
        }
        fileHead = fileHead.toUpperCase();
        FileType[] fileTypes = FileType.values();
        for (FileType type : fileTypes) {
            if (fileHead.startsWith(type.getValue())) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取保存文件前缀
     *
     * @return String
     */
    private static String getSaveFilePath() {
        String saveFilePath;
        //表示存放在tomcat应用目录中
        if (AppProperty.me().appPath() == 1) {
            saveFilePath = Context.me().getRequest().getSession().getServletContext().getRealPath("/");
        } else {
            // 上层目录
            saveFilePath = new File(Context.me().getRequest().getSession().getServletContext().getRealPath("/")).getParent();// 当前WEB环境的上层目录
        }
        if (!saveFilePath.endsWith("/") && !saveFilePath.endsWith("\\")) {
            saveFilePath += "/";
        }
        saveFilePath += AppProperty.me().uploadRootPath();
        return saveFilePath;
    }

    /**
     * 保存上传的文件*
     *
     * @param file 文件对象
     * @return 文件相对路径, 供请求使用
     */
    public static String saveUploadFile(File file, String newFileName) {
        String saveFilePath = getSaveFilePath();
        String timeFilePath = FileUtils.getTimeFilePath();
        String urlPath;
        if (FileUtils.isImage(file)) {//保存图片
            urlPath = AppProperty.me().imagePath() + timeFilePath;
            saveFilePath += urlPath;
        } else if (FileUtils.isVideo(file)) {//保存视频
            urlPath = AppProperty.me().videoPath() + timeFilePath;
            saveFilePath += urlPath;
        } else {//其他文件(如果是)
            urlPath = AppProperty.me().otherPath() + timeFilePath;
            saveFilePath += urlPath;
        }
        // System.out.println("saveFilePath => " + saveFilePath);
        File saveFileDir = new File(saveFilePath);
        if (!saveFileDir.exists()) {
            saveFileDir.mkdirs();
        }
        //保存 文件
        if (FileUtils.copyFile(file.getAbsolutePath(), saveFilePath + newFileName)) {
            //删掉临时文件
            file.delete();
            return urlPath;
        } else {
            return null;
        }
    }
}

