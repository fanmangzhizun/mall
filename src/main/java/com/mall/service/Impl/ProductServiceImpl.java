package com.mall.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mall.common.Const;
import com.mall.common.ResponseCode;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.dao.ProductMapper;
import com.mall.pojo.Category;
import com.mall.pojo.Product;
import com.mall.service.ICategoryService;
import com.mall.service.IProductService;
import com.mall.util.DateTimeUtil;
import com.mall.util.PropertiesUtil;
import com.mall.vo.ProductDetailVo;
import com.mall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by faithpercious on 2017/10/18.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ICategoryService iCategoryService;
    public ServerResponse SaveOrUpdateProduct(Product product) {
        //需要进行图片的主页图的设置，默认选第一张
        if (product != null) {
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImagesArray = product.getSubImages().split(",");
                if (subImagesArray.length > 0) {
                    product.setMainImage(subImagesArray[0]);
                }
            }
            if (product.getId() != null) {
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("更新产品成功");
                } else return ServerResponse.createByErrorMessage("更新产品失败");
            } else {
                int rowCount = productMapper.insert(product);
                if (rowCount > 0) {
                    return ServerResponse.createBySuccessMessage("添加产品成功");
                } else return ServerResponse.createByErrorMessage("添加产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("产品品名参数不正确");
    }

    public ServerResponse setProductStatus(Integer productId, Integer status) {
        Product product;
        product = productMapper.selectByPrimaryKey(productId);
        product.setStatus(status);
        int rowCount = productMapper.updateByPrimaryKey(product);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }


    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        Product product;
        product = productMapper.selectByPrimaryKey(productId);
        if (product != null) {
            ProductDetailVo productDetailVo = assembleProductDetailVo(product);
            return ServerResponse.createBySuccessData(productDetailVo);
        } else return ServerResponse.createByErrorMessage("产品已下架或已删除");
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://image.faithmall.com/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if (category == null) {
            productDetailVo.setParentCategoryId(0);//默认根节点
        } else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        productDetailVo.setCreatetime(DateTimeUtil.DateTostr(product.getCreateTime()));
        productDetailVo.setUpdatetime(DateTimeUtil.DateTostr(product.getUpdateTime()));
        return productDetailVo;
    }



    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize){
        //startpage-->start
        //sql逻辑
        //pageHelper-->收尾
        PageHelper.startPage(pageNum,pageSize);
        List<Product> productList=productMapper.selectList();
        List<ProductListVo> productListVoList= Lists.newArrayList();
        for (Product productItem:productList){
            ProductListVo productListVo=assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult=new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccessData(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo=new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://image.faithmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName =new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList=productMapper.selectByNameAndProductId(productName,productId);
        List<ProductListVo> productListVoList=Lists.newArrayList();
        for (Product productItem:productList){
            ProductListVo productListVo=assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult=new PageInfo(productList);
        pageResult.setList(productListVoList);
        return  ServerResponse.createBySuccessData(pageResult);

    }



    public ServerResponse<ProductDetailVo> productDetail(Integer productId){
        if (productId==null){
            return  ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product=productMapper.selectByPrimaryKey(productId);
        if (product==null){
            return  ServerResponse.createByErrorMessage("产品已下架");
        }
        ProductDetailVo productDetailVo=assembleProductDetailVo(product);
        //用枚举表示销售的状态
        //todo 使用枚举进行迭代
        if (productDetailVo.getStatus()!= Const.ProductStatusItem.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架");
        }
        return ServerResponse.createBySuccessData(productDetailVo);
    }





    public ServerResponse<PageInfo> productList( String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy){
        if (StringUtils.isBlank(keyword)&&categoryId==null){//若关键词以及商品Id都为空返回非法数据
            return ServerResponse.createByErrorcodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList=new ArrayList<>();
        if (categoryId!=null){
            Category category=categoryMapper.selectByPrimaryKey(categoryId);
            if (category==null&&StringUtils.isBlank(keyword)){
                PageHelper.startPage(pageNum,pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();//使用pageInfo进行返回需要的数据
                PageInfo pageInfo=new PageInfo(productListVoList);
                return ServerResponse.createBySuccessData(pageInfo);
            }
            categoryIdList=iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();//找出分类下的所有子分类商品
        }
        if (StringUtils.isNotBlank(keyword)){
            //查询keyword进行转义
            keyword=new StringBuilder().append("%").append(keyword).append("%").toString();
        }
        //进行有趣的排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String orderByArray[]=orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+"   "+orderByArray[1]);
            }
        }
        //生成对象
        List<Product> productList=productMapper.selectByNameAndCategoryId(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);
        List <ProductListVo> productListVoList=Lists.newArrayList();
        for (Product product:productList){
                ProductListVo productListVo=assembleProductListVo(product);
                productListVoList.add(productListVo);
        }
        PageInfo pageInfo=new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccessData(pageInfo);
    }








}
