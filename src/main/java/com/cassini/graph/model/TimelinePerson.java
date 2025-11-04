package com.cassini.graph.model;

import java.util.Date;

/**
 * 历史人物实体类
 * 对应timeline_persons表
 */
public class TimelinePerson {

    private Integer id;
    private String name;
    private Date birth_date;
    private Date death_date;
    private String bio;
    private Integer life_span;

    // getter和setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(Date birth_date) {
        this.birth_date = birth_date;
    }

    public Date getDeath_date() {
        return death_date;
    }

    public void setDeath_date(Date death_date) {
        this.death_date = death_date;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getLife_span() {
        return life_span;
    }

    public void setLife_span(Integer life_span) {
        this.life_span = life_span;
    }
}