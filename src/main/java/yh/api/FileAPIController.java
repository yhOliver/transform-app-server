package yh.api;

import com.jfinal.aop.Before;
import com.jfinal.upload.UploadFile;
import yh.common.bean.BaseResponse;
import yh.common.bean.Code;
import yh.common.utils.FileUtils;
import yh.common.utils.StringUtils;
import yh.config.AppProperty;
import yh.interceptor.FileTokenInterceptor;
import yh.interceptor.POST;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传总的控制器，所有文件上传类表单均拆分成文件上传和文本提交
 * => POST /api/fs/upload
 *
 * @author zhuqi259
 */

@Before({POST.class, FileTokenInterceptor.class})
public class FileAPIController extends BaseAPIController {

    /**
     * 处理单文件或多文件上传，上传成功后，返回url集合
     * <p>
     * 一般是发帖时使用
     * <p>
     * POST、登陆状态
     */
    public void upload() {
        Map<String, String> urls = new HashMap<>();//用于保存上传成功的文件地址
        try {
            List<UploadFile> fileList = getFiles();//已接收到的文件
            if (fileList != null && !fileList.isEmpty()) {
                List<String> failedFiles = new ArrayList<>(); //用于保存未成功上传的文件名
                for (UploadFile uploadFile : fileList) {
                    File file = uploadFile.getFile();
                    String fileExtension = FileUtils.getExtension(file.getName());
                    String newFileName = "";
                    if (fileExtension == null) {
                        newFileName += System.currentTimeMillis();
                    } else {
                        newFileName += System.currentTimeMillis() + "." + fileExtension;
                    }
                    String urlPath = FileUtils.saveUploadFile(file, newFileName);
                    if (StringUtils.isEmpty(urlPath)) {
                        failedFiles.add(uploadFile.getParameterName());//标记为上传失败
                    } else {
                        //返回相对路径,用于响应
                        // url 中 /attached
                        urls.put(uploadFile.getParameterName(), "/" + AppProperty.me().uploadRootPath() + urlPath + newFileName);
                    }
                }
                if (failedFiles.size() > 0) {
                    renderJson(new BaseResponse(Code.FAILURE, "some files were upload failed", failedFiles));
                    return;
                }
            } else {
                renderJson(new BaseResponse(Code.FAILURE, "uploadFileName can not be null"));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            renderFailed("something is wrong");
            return;
        }
        renderJson(new BaseResponse(Code.SUCCESS, "", urls));
    }
}