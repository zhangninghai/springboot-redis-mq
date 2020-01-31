package cn.com.action;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * java类简单作用描述
 *
 * @ProjectName: walkincq
 * @Package: com.example.cq.util
 * @Description: java类作用描述
 * @Author: 张宁海
 * @create: 2019-04-26 10:33
 * <p>Copyright: Copyright (c) 2019</p>
 */
@RestController
public class FileUtils {
    @RequestMapping("/file")
    public String file(HttpServletRequest request, HttpServletResponse response,  MultipartFile file) {

        if (!file.isEmpty()) {
            try {
                // 文件保存路径
                String filePath = "/Users/y/Desktop/6666.txt";
                // 转存文件
                file.transferTo(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @RequestMapping("/downloadFile")
    public String downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        StringBuffer sb=new StringBuffer();
        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(request.getInputStream(),"UTF-8"));
        String str=null;
        while ((str= bufferedReader.readLine())!=null){
            sb.append(str);
        }
        System.out.println(sb.toString());
        //设置文件路径
        File file = new File("/Users/y/Desktop/6666.txt");

        // 如果文件名存在，则进行下载
        if (file.exists()) {

            // 配置文件下载
            response.setHeader("content-type", "application/force-download");
            response.setContentType("application/force-download");
            // 下载文件能正常显示中文
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("6666.txt", "UTF-8"));

            // 实现文件下载
            byte[] buffer = new byte[1024];
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                System.out.println("Download the song successfully!");
            } catch (Exception e) {
                System.out.println("Download the song failed!");
            } finally {
                if (bis != null) {
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
