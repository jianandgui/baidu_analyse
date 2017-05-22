package cn.edu.swpu.cins.event.analyse.platform.dao;

import cn.edu.swpu.cins.event.analyse.platform.model.persistence.HandledEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lp-deepin on 17-5-14.
 */
@Mapper
@Repository
public interface HandledEventDao {
    static String TABLE_NAME=" handled_event ";
    static String JOIN_TABLE_NAME=" daily_event ";
    static String JOIN_SELECT_FIELD=" de.theme,de.main_view,de.url," +
            "he.id,he.handled_condition,he.feedback_condition,he.collected_time,he.handled_time,he.detail,he.remark,he.recorder ";
    static String INSERT_FIELD=" handled_condition,feedback_condition,collected_time,handled_time,detail,remark,daily_event_id ";

    @Insert({"insert into ",TABLE_NAME, " ( ",INSERT_FIELD," ) " +
            "value(#{handledCondition},#{feedbackCondition},#{collectedTime}" +
            ",#{handledTime},#{detail},#{remark},#{dailyEventId})"})
    int insertHandledEvent(HandledEvent handledEvent);

    @Select({"SELECT ",JOIN_SELECT_FIELD
            ," FROM ",TABLE_NAME," as he "
            ," LEFT JOIN ",JOIN_TABLE_NAME," as de "
            ," ON he.daily_event_id = de.id"
            ," LIMIT #{offset} , #{limit}"})
    List<HandledEvent> selectAll(@Param("offset") int offset ,@Param("limit") int limit);

    @Select({" SELECT id " +
            "FROM ",TABLE_NAME,
            " WHERE daily_event_id = #{dailyEventId} LIMIT 1"})
    HandledEvent selectByDailyEvent(int dailyEventId);
}