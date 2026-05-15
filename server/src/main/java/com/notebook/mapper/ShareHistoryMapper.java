package com.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notebook.entity.ShareHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分享浏览历史 Mapper
 */
@Mapper
public interface ShareHistoryMapper extends BaseMapper<ShareHistory> {
}
