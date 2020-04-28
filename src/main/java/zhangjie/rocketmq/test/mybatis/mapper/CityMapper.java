package zhangjie.rocketmq.test.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import zhangjie.rocketmq.test.mybatis.City;

/**
 * @autor zhangjie
 * @date 2020/4/27 18:50
 */
@Mapper
public interface CityMapper {

    @Select("SELECT * FROM CITY WHERE state = #{state}")
    City findByState(@Param("state") String state);

    @Insert("INSERT INTO city (name, state, country) VALUES(#{name}, #{state}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(City city);

    @Select("SELECT id, name, state, country FROM city WHERE id = #{id}")
    City findById(long id);

}
