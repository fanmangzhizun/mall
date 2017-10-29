package com.mall.controller.backend;

import com.google.common.collect.Maps;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.pojo.User;
import com.mall.service.IFileService;
import com.mall.service.IUserService;
import com.mall.service.IProductService;
import com.mall.util.PropertiesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by faithpercious on 2017/10/18.
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IProductService iProductService;
    @Autowired
    private IFileService iFileService;

    @RequestMapping(value = "save_or_insert_product.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iProductService.SaveOrUpdateProduct(product);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }


    //设置产品状态
    @RequestMapping(value = "set_product_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse SetProductStatus(HttpSession session, Integer productId,Integer status){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iProductService.setProductStatus(productId,status);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }

    //获取产品细节
    @RequestMapping(value = "detail.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse getProduct(HttpSession session,Integer productId){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iProductService.manageProductDetail(productId);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }






    //获取后台列表的接口
    @RequestMapping(value = "List.do",method = RequestMethod.POST)
    @ResponseBody
  public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iProductService.getProductList(pageNum,pageSize);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }

    //搜索接口
    @RequestMapping(value = "search.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse searchProduct(HttpSession session, String productName,Integer productId,@RequestParam(value = "pageNum",defaultValue = "1")int pageNum,@RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }





    //文件的上传操作
    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user= (User) session.getAttribute(Const.CURRENT_USER);

        if (user==null){
            return ServerResponse.createByErrorcodeMessage(ResponseCode.NEED_LOGN.getCode(),"用户未登录，请登录管理员");
        }
        if (iUserService.checkAdmin(user).isSuccess()){
            String path=request.getSession().getServletContext().getRealPath("upload");
            String targetFileName=iFileService.upload(file,path);
            String url=PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            Map fileMap= Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccessData(fileMap);
        }
        else return ServerResponse.createByErrorMessage("无权限操作");
    }
    @RequestMapping(value = "upload_rich_text_Img.do",method = RequestMethod.POST)
    @ResponseBody
    //富文本的上传操作,格式与普通文件要求不一致
    public  Map richTextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap=Maps.newHashMap();
        User user= (User) session.getAttribute(Const.CURRENT_USER);
        if (user==null){
            resultMap.put("success",false);
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }
        if (iUserService.checkAdmin(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file, path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow-Headers", "X-File-Name");
            return resultMap;
        }
        else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }








































































}
