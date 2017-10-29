package com.mall.service;

import com.mall.common.ServerResponse;
import com.mall.pojo.Category;

import java.util.List;

/**
 * Created by faithpercious on 2017/10/17.
 */
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);
    ServerResponse updateCategoryName(String CategoryNewName,Integer categoryId);
    ServerResponse<List<Category>> getCategory(Integer id);
    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
