package com.base.springboot.common.macb.query;

import com.base.springboot.common.config.SystemConstant;
import com.base.springboot.common.exception.DaoException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author zc
 * @version 1.0.0
 * @ClassName ObjectQueryImpl.java
 * @Description 泛型接口实现类(查询BaseDao)
 * @createTime 2019/10/13/ 12:53:00
 */
@Service
@Component
public class ObjectQueryImpl<T, ID extends Serializable> implements IObjectQuery<T> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static ObjectQueryImpl object;

    /**
     * 实例化之后的class对象(泛型T的class对象)
     */
    private Class<T> clazz = null;


    //查询参数map
    private Map<String,Object> paramMap=new HashMap<>();

    //排序字段
    private String sortFiled=null;

    //排序方式,默认为DESC
    private String sortType=null;

    /**
     * 注入sqlSessionTemplate
     */
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    //实体id属性
    private Object id = "";

    //属性和数据量字段Map
    private Map<String,Object> annotFiledMap=new HashMap<>();

    @PostConstruct
    private void initialize() {
        object = this;
        object.sqlSessionTemplate = this.sqlSessionTemplate;
    }

    public ObjectQueryImpl() {
        try {
            ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
            Type typed = type.getActualTypeArguments()[0];
            this.clazz = (Class) typed;
            //开始初始化属性和数据量字段Map
            Field[] fields = this.clazz.getDeclaredFields();
            boolean flag = this.clazz.isAnnotationPresent(Entity.class);
            if (flag){
                for (int i = 0; i < fields.length; i++){
                    if (fields[i].isAnnotationPresent(javax.persistence.Column.class)) {
                        annotFiledMap.put(fields[i].getName(),fields[i].getAnnotation(javax.persistence.Column.class));
                    }
                }
            }
        } catch (Exception var3) {
            this.log.warn("init query object!");
        }
    }

    private int toInt(Object string) {
        if (string != null && !"".equals(string.toString())) {
            return Integer.parseInt(string.toString());
        } else {
            throw new DaoException("a empty value can not cast to a int!");
        }
    }

    /**
     * 根据实体类和当前字段名获取对应的数据库名称
     * @param clazz
     * @param filedName
     * @return
     */
    private String getFiledByName(String filedName){
        if (annotFiledMap.get(filedName)!=null){
            javax.persistence.Column filedAnnotation =(javax.persistence.Column)annotFiledMap.get(filedName);
            return (String) filedAnnotation.name();
        }else {
            throw new DaoException("没有此属性!");
        }
    }





    /**
     * 拼接hqlStr语句
     *
     * @return String
     */
    private String hqlStr() {

        if (this.clazz == null) {
            throw new DaoException("class type no found!");
        } else {
            //flag判断是否有@Entity注解
            boolean flag = this.clazz.isAnnotationPresent(Entity.class);
            if (!flag) {
                throw new DaoException("please point the entity annotion!");
            } else {
                StringBuffer hql = new StringBuffer();
                hql.append("select ");
                //获取当前类的字段数组
                Field[] fields = this.clazz.getDeclaredFields();
                //flagToColumn判断是否有@Column注解
                boolean flagToColumn = false;
                //循环数组,拼接hql查询语句部分(select o.字段名 as 属性名称)
                for (int i = 0; i < fields.length; i++) {

                    if (fields[i].isAnnotationPresent(javax.persistence.Id.class)) {
                        hql.append("o.PK_ID");
                    } else {
                        flagToColumn = fields[i].isAnnotationPresent(javax.persistence.Column.class);
                        //如果有@Column注解
                        if (flagToColumn) {
                            //获取当前字段的@Column注解
                            javax.persistence.Column filedAnnotation = (javax.persistence.Column) fields[i].getAnnotation(javax.persistence.Column.class);
                            //如果@Column注解的columnDefinition属性包含SystemConstant.DATE
                            if (filedAnnotation.columnDefinition().contains(SystemConstant.DATE)) {
                                hql.append("date_format(o." + filedAnnotation.name() + ",'%Y-%m-%d')");
                            } else if (filedAnnotation.columnDefinition().contains(SystemConstant.DATETIME)) {
                                hql.append("date_format(o." + filedAnnotation.name() + ",'%Y-%m-%d %H:%i:%s')");
                            } else {
                                hql.append("o." + filedAnnotation.name());
                            }
                        }
                    }
                    hql.append(" as " + fields[i].getName());
                    //如果不是最后一个需要拼接的字段
                    if (i != fields.length-1) {
                        hql.append(",");
                    }
                }
                //拼接from部分(from 表名)
                hql.append(" FROM " + this.clazz.getAnnotation(Entity.class).name()+" o");
                hql.append(" WHERE 1=1 ");

                //拼接id查询条件
                if (!this.id.equals("")){
                    hql.append(" AND o.PK_ID="+this.toInt(this.id)+" ");
                }

                //拼接其它查询条件
                Iterator iterator;
                Map.Entry entry;
                //注释上的columnDefinition属性(描述字段类型和长度)
                String columnDefinition=null;
                //数据库上的字段名称
                String dataFiled=null;
                if (paramMap!=null){
                    for (iterator =this.paramMap.entrySet().iterator();iterator.hasNext();){
                        entry=(Map.Entry)iterator.next();
                        if (annotFiledMap.get(entry.getKey())==null){
                            throw new DaoException(entry.getKey()+"不能作为条件查询,请查看当前实体被@Column注解的是否存在此属性!");
                        }
                        javax.persistence.Column filedAnnotation =(javax.persistence.Column)annotFiledMap.get(entry.getKey());
                        columnDefinition=filedAnnotation.columnDefinition();
                        dataFiled=((Column) annotFiledMap.get(entry.getKey())).name();
                        //如果@Column注解的columnDefinition属性包含SystemConstant.DATE
                        if (columnDefinition.contains(SystemConstant.DATE)) {
                            hql.append(" AND date_format(o."+dataFiled+",'%Y-%m-%d')='"+entry.getValue()+"' ");
                        } else if (columnDefinition.contains(SystemConstant.DATETIME)) {
                            hql.append(" AND date_format(o."+dataFiled+",'%Y-%m-%d %H:%i:%s')='"+entry.getValue()+"' ");
                        } else if (columnDefinition.contains(SystemConstant.VARCHAR)) {
                            hql.append(" AND o."+dataFiled+"='"+entry.getValue()+"' ");
                        }  else {
                            hql.append(" AND o."+dataFiled+"="+entry.getValue()+" ");
                        }
                    }
                }

                //拼接排序
                if (StringUtils.isNotBlank(this.sortFiled)){
                    hql.append(" ORDER BY o."+this.getFiledByName(this.sortFiled));
                    if (StringUtils.isNotBlank(this.sortType)){
                        hql.append(" "+this.sortType);
                    }else {
                        hql.append(" ASC");
                    }
                }

                return hql.toString();
            }
        }
    }



    /**
     * 获取hqlMap
     *
     * @return
     */
    private Map<String, Object> hqlMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("hql", this.hqlStr());
        return map;
    }



    /****************************************************接口实现类向外部提供的方法*******************************************/


    /**
     * 增加查询条件 id
     * @param id
     */
    public void id(Object id){
        this.id=id;
    }

    /**
     * @title equalsParam
     * @description  拼接其它查询条件
     * @author zhangCheng
     * @updateTime 2019/10/13 0013 下午 4:02
     * @throws
     */
    @Override
    public void equalsParam(String filedName, Object obj) {
        paramMap.put(filedName,obj);
    }

    @Override
    public void sort(String sortFiledName, String sortType) {
        this.sortFiled=sortFiledName;
        this.sortType=sortType;
    }

    /**
     * 查询所有数据列表
     * @return List<T>
     */
    @Override
    public List<T> list() {

        List<T> list=null;
        try {
            list=object.sqlSessionTemplate.selectList("common.select",this.hqlMap());
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ListVo<T> page(String start, String limit) {
        ListVo<T> listVo = new ListVo<T>();
        PageHelper.startPage(Integer.parseInt(start),Integer.parseInt(limit),true);
        List<T> list=this.list();
        listVo.setList(list);
        Page<T> page = (Page<T>)list;
        listVo.setTotalSize((int)page.getTotal());
        return listVo;
    }


    @Override
    public T singleResultful() {
        List<T> list=null;
        T t=null;
        try {
            list=object.sqlSessionTemplate.selectList("common.select",this.hqlMap());
            if (list.size()!=1){
                throw new DaoException("查询结果不唯一!");
            }else {
                t=(T)list.get(0);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return t;
    }
}
