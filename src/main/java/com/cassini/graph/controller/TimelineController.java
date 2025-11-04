package com.cassini.graph.controller;

import com.cassini.graph.model.TimelineEvent;
import com.cassini.graph.model.TimelinePerson;
import com.cassini.graph.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 历史时间线控制器
 * 提供历史人物和事件的API接口
 */
@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    /**
     * 获取所有历史人物数据
     */
    @GetMapping("/persons")
    public List<TimelinePerson> getAllPersons() {
        return timelineService.getAllPersons();
    }

    /**
     * 获取所有历史事件数据
     */
    @GetMapping("/events")
    public List<TimelineEvent> getAllEvents() {
        return timelineService.getAllEvents();
    }

    /**
     * 根据年份获取历史事件
     */
    @GetMapping("/events/year/{year}")
    public List<TimelineEvent> getEventsByYear(@PathVariable Integer year) {
        return timelineService.getEventsByYear(year);
    }

    /**
     * 根据人物ID获取相关事件
     * 注：当前表结构未直接关联人物和事件，此接口仅返回空列表
     */
    @GetMapping("/events/person/{personId}")
    public List<TimelineEvent> getEventsByPerson(@PathVariable Integer personId) {
        // 由于数据库表结构中未建立人物和事件的直接关联
        // 此接口暂时返回空列表
        return timelineService.getEventsByPerson(personId);
    }

    /**
     * 根据事件类型获取事件
     */
    @GetMapping("/events/type/{type}")
    public List<TimelineEvent> getEventsByType(@PathVariable String type) {
        return timelineService.getEventsByType(type);
    }
}