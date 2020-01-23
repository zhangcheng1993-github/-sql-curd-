package com.base.springboot.common.macb.query;

import java.util.List;
/**
 * @title 泛型接口,查询接口
 * @description 
 * @author zhangCheng
 * @updateTime 2019/10/13  下午 12:52
 * @throws 
 */
public interface IObjectQuery<T> {


    /**
     * @title id
     * @description 拼接id查询条件
     * @author zhangCheng
     * @updateTime 2019/10/13 0013 下午 9:31
     * @throws
     */
    public void id(Object id);


   /**
    * @title equalsParam
    * @description 拼接其它查询条件
    * @author zhangCheng
    * @updateTime 2019/10/13 0013 下午 4:00
    * @param filedName 实体字段名称
    * @param obj 查询条件
    * @throws
    */
    public void equalsParam(String filedName,Object obj);


    /**
     * @title sort
     * @description 排序方法
     * @author zhangCheng
     * @param sortFiledName 排序字段
     * @param sortType 排序字段类型(desc 倒序 asc顺序)
     * @updateTime 2019/10/13 0013 下午 9:31
     * @throws
     */
    public void sort(String sortFiledName,String sortType);


    /**
     * @title list
     * @description 查询list集合
     * @author zhangCheng
     * @updateTime 2019/10/13  下午 12:49
     * @throws
     */
    public List<T> list();

    /**
     * @title page
     * @description 查询list集合(分页)
     * @author zhangCheng
     * @param start 开始页
     * @param limit 每页多少行
     * @updateTime 2019/10/13 0013 下午 10:10
     * @throws
     */
    public ListVo<T> page(String start,String limit);


    /**
     * @title singleResultful
     * @description  查询单条数据
     * @author zhangCheng
     * @updateTime 2019/10/13 0013 下午 10:22
     * @throws
     */
    public T singleResultful();
}
