package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.UserSettings;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户设置数据访问接口
 * 提供用户设置表的基础CRUD操作
 *
 * @author notebook
 */
@Mapper
public interface UserSettingsMapper extends BaseMapper<UserSettings> {
}
