package com.imdroid.utils;

import com.imdroid.pojo.bo.BusinessException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Description:
 * @Author: iceh
 * @Date: create in 2018-12-14 17:27
 * @Modified By:
 */
@Slf4j
public class FileUtil {
    private static final int BUFFER_SIZE = 2 * 1024;

    /**
     * 将file转换为MultipartFile
     *
     * @param file
     * @return
     */
    public static MultipartFile tranfromFile(File file) {
        FileItemFactory factory = new DiskFileItemFactory(16, null);
        String textFieldName = "textField";
        FileItem item = factory.createItem(textFieldName, "text/plain", true, "MyFileName");
        int bytesRead;
        byte[] buffer = new byte[8192];
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = item.getOutputStream()) {
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("文件转换失败", e);
        }
        return new CommonsMultipartFile(item);
    }

    /**
     * 压缩成ZIP 方法1
     *
     * @param sourceFile           压缩文件夹路径
     * @param out              压缩文件输出流
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws BusinessException 压缩失败会抛出运行时异常
     */

    public static void toZip(File sourceFile, OutputStream out, boolean KeepDirStructure) {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            if (sourceFile.exists()) {
                compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
            } else {
                throw new BusinessException("待压缩的文件：" + sourceFile + "不存在");
            }
            log.info("文件压缩完成");
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (Exception e) {
            log.error("文件压缩失败", e);
        }
    }


    /**
     * 压缩成ZIP 方法2
     *
     * @param srcFiles 需要压缩的文件路径列表
     * @param out      压缩文件输出流
     * @throws RuntimeException 压缩失败会抛出运行时异常
     */

    public static void toZip(List<File> srcFiles, OutputStream out, boolean KeepDirStructure) {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (File srcFile : srcFiles) {
                if (srcFile.exists()) {
                    compress(srcFile, zos, srcFile.getName(), KeepDirStructure);
                } else {
                    throw new BusinessException("待压缩的文件目录：" + srcFile + "不存在");
                }
            }
            log.info("文件压缩完成");
        } catch (IOException e) {
            log.error("IOException", e);
        } catch (Exception e) {
            log.error("文件压缩失败", e);
        }
    }

    /**
     * 根据file创建文件，将内容写入该文件
     *
     * @param content
     * @param file
     */
    public static void write(String content, File file) {
        byte[] bytes = content.getBytes();
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (OutputStream os = new FileOutputStream(file)) {
            file.createNewFile();
            os.write(bytes, 0, content.length());
            log.info("生成文件成功:" + file);
        } catch (IOException e) {
            log.error("生成文件失败:" + file, e);
        }
    }


    /**
     * 递归压缩方法
     *
     * @param sourceFile       源文件
     * @param zos              zip输出流
     * @param name             压缩后的名称
     * @param KeepDirStructure 是否保留原来的目录结构,true:保留目录结构;
     *                         <p>
     *                         false:所有文件跑到压缩包根目录下(注意：不保留目录结构可能会出现同名文件,会压缩失败)
     * @throws Exception
     */
    private static void compress(File sourceFile, ZipOutputStream zos, String name,
                                 boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            // 向zip输出流中添加一个zip实体，构造器中name为zip实体的文件的名字
            zos.putNextEntry(new ZipEntry(name));
            // copy文件到zip输出流中
            int len;
            try (FileInputStream in = new FileInputStream(sourceFile)) {
                while ((len = in.read(buf)) != -1) {
                    zos.write(buf, 0, len);
                }
                // Complete the entry
                zos.closeEntry();
            }
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                // 需要保留原来的文件结构时,需要对空文件夹进行处理
                if (KeepDirStructure) {
                    // 空文件夹的处理
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    // 没有文件，不需要文件的copy
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    // 判断是否需要保留原来的文件结构
                    if (KeepDirStructure) {
                        // 注意：file.getName()前面需要带上父文件夹的名字加一斜杠,

                        // 不然最后压缩包中就不能保留原来的文件结构,即：所有文件都跑到压缩包根目录下了
                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }
                }
            }
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (!dir.exists()) {
            throw new BusinessException("待删除的文件目录：" + dir + "不存在");
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * 通过文件路径直接修改文件名
     *
     * @param file        需要修改的文件
     * @param newFileName 要变为的名称
     * @return
     */
    public static boolean rename(File file, @NonNull String newFileName) {
        if (!file.exists()) { // 判断原文件是否存在
            throw new BusinessException("文件不存在:" + file);
        }
        newFileName = newFileName.trim();
        if ("".equals(newFileName) || null == newFileName) {
            throw new BusinessException("新文件名不能为空");
        }
        String newFilePath = null;
        if (file.isDirectory()) { // 判断是否为文件夹
            newFilePath = file.getParentFile() + "/" + newFileName;
        } else {
            String fileName = file.getName();
            newFilePath = file.getParentFile() + "/" + newFileName + fileName.substring(fileName.lastIndexOf("."));
        }
        File newFile = new File(newFilePath);
        if (newFile.exists()) { // 判断需要修改为的文件是否存在（防止文件名冲突）
            throw new BusinessException("文件" + newFileName + "已在文件夹" + file.getParentFile() + "下");
        }
        // 修改文件名
        return file.renameTo(newFile);
    }


}
