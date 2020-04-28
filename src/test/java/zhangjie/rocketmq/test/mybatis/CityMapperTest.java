package zhangjie.rocketmq.test.mybatis;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.junit4.SpringRunner;
import zhangjie.rocketmq.test.mybatis.mapper.CityMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @autor zhangjie
 * @date 2020/4/27 18:54
 */
@RunWith(SpringRunner.class)
@MybatisTest
public class CityMapperTest {

    @SpringBootApplication(scanBasePackages = {"zhangjie.rocketmq.test.mybatis.mapper"})
    static class InnerConfig {}

    @Autowired
    private CityMapper cityMapper;

    @Test
    public void findByStateTest() {
        City city = cityMapper.findByState("CA");
        assertThat(city.getName()).isEqualTo("San Francisco");
        assertThat(city.getState()).isEqualTo("CA");
        assertThat(city.getCountry()).isEqualTo("US");
    }

}
