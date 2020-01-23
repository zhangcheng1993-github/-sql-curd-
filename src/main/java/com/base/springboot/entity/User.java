package com.base.springboot.entity;


import com.base.springboot.common.config.SystemConstant;
import com.base.springboot.common.macb.query.ObjectQueryImpl;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "T_USER")
public class User extends ObjectQueryImpl<User,Integer>{

    @Id
    @GeneratedValue
    @Column(name = "PK_ID",nullable = false,columnDefinition = SystemConstant.INT+"(11) default 0 comment '主键,自增'")
    private  int id;

    @Column(name = "NAME",nullable = false,columnDefinition = SystemConstant.VARCHAR+"(100) default '' comment '名称'")
    private String name;


    @Column(name = "ADD_DATE",nullable = false,columnDefinition = SystemConstant.DATE+"  comment '名称'")
    private Date addDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }


}
