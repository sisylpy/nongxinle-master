-- 优果团 Phase 1A 本地开发 mock 数据清理脚本
-- 仅限本地/测试库手工执行；生产不要执行。

DELETE FROM ygt_order_candidate_item
WHERE ygt_oci_candidate_id IN (
  SELECT ygt_order_candidate_id FROM ygt_order_candidate
  WHERE ygt_oc_corp_id = 'ww9778dea409045fe6'
    AND (ygt_oc_chat_id = 'wrOgQhDgAAMYQiS5ol9G7gK9JVAAAA'
      OR ygt_oc_message_id IN (
        SELECT ygt_chat_message_id FROM ygt_chat_message
        WHERE ygt_cm_corp_id = 'ww9778dea409045fe6'
          AND ygt_cm_msg_id LIKE 'mock_msg%'
      ))
);

DELETE FROM ygt_order_candidate
WHERE ygt_oc_corp_id = 'ww9778dea409045fe6'
  AND (ygt_oc_chat_id = 'wrOgQhDgAAMYQiS5ol9G7gK9JVAAAA'
    OR ygt_oc_message_id IN (
      SELECT ygt_chat_message_id FROM ygt_chat_message
      WHERE ygt_cm_corp_id = 'ww9778dea409045fe6'
        AND ygt_cm_msg_id LIKE 'mock_msg%'
    ));

DELETE FROM ygt_chat_message
WHERE ygt_cm_corp_id = 'ww9778dea409045fe6'
  AND ygt_cm_msg_id LIKE 'mock_msg%';

DELETE FROM ygt_archive_cursor
WHERE ygt_ac_corp_id = 'ww9778dea409045fe6'
  AND ygt_ac_chat_id IN ('*', 'wrOgQhDgAAMYQiS5ol9G7gK9JVAAAA');
