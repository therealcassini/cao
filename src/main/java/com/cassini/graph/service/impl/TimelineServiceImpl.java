package com.cassini.graph.service.impl;

import com.cassini.graph.model.TimelineEvent;
import com.cassini.graph.model.TimelinePerson;
import com.cassini.graph.service.TimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史时间线服务实现类
 */
@Service
public class TimelineServiceImpl implements TimelineService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<TimelinePerson> getAllPersons() {
        // 按出生日期升序排序，这样年龄大的人会排在前面
        String sql = "SELECT * FROM timeline_persons ORDER BY birth_date ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TimelinePerson.class));
    }

    @Override
    public List<TimelineEvent> getAllEvents() {
        String sql = "SELECT * FROM timeline_events ORDER BY start_date";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TimelineEvent event = new TimelineEvent();
            event.setId(rs.getInt("id"));
            event.setEvent_name(rs.getString("event_name"));
            
            // 直接设置String类型的日期字段，避免时区转换
            event.setStart_date(rs.getString("start_date"));
            event.setEnd_date(rs.getString("end_date"));
            
            event.setDescription(rs.getString("description"));
            event.setEvent_category(rs.getString("event_category"));
            
            // 创建时间和更新时间仍然使用Date类型
            try {
                event.setCreate_time(rs.getTimestamp("create_time"));
                event.setUpdate_time(rs.getTimestamp("update_time"));
            } catch (Exception e) {
                // 记录异常但不中断处理
                System.err.println("日期时间转换异常: " + e.getMessage());
            }
            
            return event;
        });
    }

    @Override
    public List<TimelineEvent> getEventsByYear(Integer year) {
        String sql = "SELECT * FROM timeline_events WHERE YEAR(start_date) = ? OR (end_date IS NOT NULL AND YEAR(end_date) = ?) ORDER BY start_date";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TimelineEvent event = new TimelineEvent();
            event.setId(rs.getInt("id"));
            event.setEvent_name(rs.getString("event_name"));
            
            // 直接设置String类型的日期字段，避免时区转换
            event.setStart_date(rs.getString("start_date"));
            event.setEnd_date(rs.getString("end_date"));
            
            event.setDescription(rs.getString("description"));
            event.setEvent_category(rs.getString("event_category"));
            
            // 创建时间和更新时间仍然使用Date类型
            try {
                event.setCreate_time(rs.getTimestamp("create_time"));
                event.setUpdate_time(rs.getTimestamp("update_time"));
            } catch (Exception e) {
                // 记录异常但不中断处理
                System.err.println("日期时间转换异常: " + e.getMessage());
            }
            
            return event;
        }, year, year);
    }

    @Override
    public List<TimelineEvent> getEventsByPerson(Integer personId) {
        // 由于数据库表结构中未建立人物和事件的直接关联
        // 此方法暂时返回空列表
        // 如果后续需要实现，可以考虑增加关联表或通过其他方式（如事件描述中包含人物名称）进行匹配
        return new ArrayList<>();
    }

    @Override
    public List<TimelineEvent> getEventsByType(String type) {
        String sql = "SELECT * FROM timeline_events WHERE event_category = ? ORDER BY start_date";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(TimelineEvent.class), type);
    }
}