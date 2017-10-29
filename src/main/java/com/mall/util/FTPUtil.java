package com.mall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by faithpercious on 2017/10/19.
 */
public class FTPUtil {
    private static final Logger logger= LoggerFactory.getLogger(FTPUtil.class);
    private static String ftpIp=PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser=PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass=PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }

    public FTPUtil(String ip, int port, String user, String pwd) {
        this.ip=ip;
        this.port=port;
        this.user=user;
        this.pwd=pwd;
    }

    //准备连接服务器的操作
    private boolean connectServer(String ip, int port, String user, String pwd){
        boolean isSuccess=false;
        ftpClient=new FTPClient();
        try {
            ftpClient.connect(ip);
            //连接成功
            isSuccess=ftpClient.login(user,pwd);
        } catch (IOException e) {
           logger.error("连接服务器异常",e);
        }
        return isSuccess;
    }

    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded =true;
        FileInputStream fls=null;
        if (connectServer(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);//转移传输路径
                ftpClient.setBufferSize(1024);//设置大小
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File fileItem:fileList){
                    fls=new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fls);
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded=false;
                e.printStackTrace();
            }
            finally {
                fls.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    //暴露出的使用方法
    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil=new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接ftp服务器");
        boolean result=ftpUtil.uploadFile("img",fileList);
        logger.info("结束ftp服务器，结束上传。上传结果{}");
        return result;
    }

}
