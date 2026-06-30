-- 优果团：存储企微群成员身份匹配结果
ALTER TABLE ygt_member_join_source
    ADD COLUMN ygt_mjs_wecom_external_user_id VARCHAR(64) NULL COMMENT '企微外部联系人 external_userid' AFTER ygt_mjs_union_id,
    ADD COLUMN ygt_mjs_wecom_user_id VARCHAR(64) NULL COMMENT '企微内部成员 userid' AFTER ygt_mjs_wecom_external_user_id,
    ADD COLUMN ygt_mjs_match_method VARCHAR(64) NULL COMMENT '群身份匹配方式' AFTER ygt_mjs_bind_source;
