package com.cassini.graph.service;

import com.cassini.graph.model.TimelineEvent;
import com.cassini.graph.model.TimelinePerson;

import java.util.List;

/**
 * 历史时间线服务接口
 * 提供历史人物和事件相关的业务逻辑
 */
public interface TimelineService {

    /**
     * 获取所有历史人物
     */
    List<TimelinePerson> getAllPersons();

    /**
     * 获取所有历史事件
     */
    List<TimelineEvent> getAllEvents();

    /**
     * 根据年份获取历史事件
     */
    List<TimelineEvent> getEventsByYear(Integer year);

    /**
     * 根据人物ID获取相关事件
     * 注：当前表结构未直接关联人物和事件，此方法暂时返回空列表
     */
    List<TimelineEvent> getEventsByPerson(Integer personId);

    /**
     * 根据事件类型获取事件
     */
    List<TimelineEvent> getEventsByType(String type);
}