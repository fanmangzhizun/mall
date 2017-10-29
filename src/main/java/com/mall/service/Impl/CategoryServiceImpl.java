package com.mall.service.Impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mall.common.ServerResponse;
import com.mall.dao.CategoryMapper;
import com.mall.pojo.Category;
import com.mall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.List;
import java.util.Set;

/**
 * Created by faithpercious on 2017/10/17.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private  Logger loggger= LoggerFactory.getLogger(CategoryServiceImpl.class);//记录日志
    @Autowired
    private CategoryMapper  categoryMapper;
    public ServerResponse addCategory(String categoryName,Integer parentId){
        if (parentId==null|| StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category=new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//此分类可用
       int rowCount= categoryMapper.insert(category);
        //检测Category是否加入了category
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    public ServerResponse updateCategoryName(String CategoryNewName,Integer CategoryId){
        if (CategoryId==null||StringUtils.isBlank(CategoryNewName)){
            return ServerResponse.createByErrorMessage("更新品类名称错误");
        }
        Category category=new Category();
        category.setId(CategoryId);
        category.setName(CategoryNewName);
        int rowCount=categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount>0){
            return ServerResponse.createBySuccessMessage("更新品类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名称失败");
    }

    public ServerResponse<List<Category>> getCategory(Integer parentId){
        //通过父节点找到同级的所有子节点（广度遍历）
        List<Category> categoryList=categoryMapper.selectCategoryByParentId(parentId);
        if (CollectionUtils.isEmpty(categoryList)){
            loggger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccessData(categoryList);
    }


    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet= Sets.newHashSet();
        findChildCategory(categorySet,categoryId);
        List<Integer> categoryList= Lists.newArrayList();
        if(categoryId!=null){
            for (Category categoryItem:categorySet){
                categoryList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccessData(categoryList);
    }

    //递归算法，算出子节点
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category=categoryMapper.selectByPrimaryKey(categoryId);
        if (category!=null){
        categorySet.add(category);
        }
        //查子节点
        List<Category> categoryList=categoryMapper.selectCategoryByParentId(categoryId);
        for (Category categoryItem:categoryList){
            findChildCategory(categorySet,categoryItem.getId());
        }
        return categorySet;
    }














}
