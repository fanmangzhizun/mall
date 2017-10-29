package com.mall.service;

import com.github.pagehelper.PageInfo;
import com.mall.common.ServerResponse;
import com.mall.pojo.Product;
import com.mall.vo.ProductDetailVo;
import org.springframework.stereotype.Service;

/**
 * Created by faithpercious on 2017/10/18.
 */

public interface IProductService {
    ServerResponse SaveOrUpdateProduct(Product product);
    ServerResponse setProductStatus(Integer productId,Integer status);
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);
    ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum, int pageSize);
    ServerResponse<ProductDetailVo> productDetail(Integer productId);
    ServerResponse<PageInfo> productList( String keyword, Integer categoryId, Integer pageNum, Integer pageSize, String orderBy);
}
