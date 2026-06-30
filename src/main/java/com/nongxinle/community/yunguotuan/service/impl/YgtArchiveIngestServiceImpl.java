package com.nongxinle.community.yunguotuan.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nongxinle.dao.QyGbDisCorpMsgAuditDao;
import com.nongxinle.dao.YgtArchiveCursorDao;
import com.nongxinle.dao.YgtChatMessageDao;
import com.nongxinle.dao.YgtWecomGroupDao;
import com.nongxinle.entity.QyGbDisCorpMsgAuditEntity;
import com.nongxinle.utils.WeworkFinanceSdkUtil;
import com.nongxinle.community.yunguotuan.entity.YgtArchiveCursorEntity;
import com.nongxinle.community.yunguotuan.entity.YgtChatMessageEntity;
import com.nongxinle.community.yunguotuan.entity.YgtWecomGroupEntity;
import com.nongxinle.community.yunguotuan.service.YgtArchiveIngestService;
import com.nongxinle.community.yunguotuan.service.YgtMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class YgtArchiveIngestServiceImpl implements YgtArchiveIngestService {
    private static final String GLOBAL_CURSOR_CHAT = "*";

    @Autowired
    private QyGbDisCorpMsgAuditDao qyGbDisCorpMsgAuditDao;

    @Autowired
    private YgtArchiveCursorDao ygtArchiveCursorDao;

    @Autowired
    private YgtWecomGroupDao ygtWecomGroupDao;

    @Autowired
    private YgtChatMessageDao ygtChatMessageDao;

    @Autowired
    private WeworkFinanceSdkUtil weworkFinanceSdkUtil;

    @Autowired
    private YgtMessageService ygtMessageService;

    @Override
    @Transactional
    public Map<String, Object> pullArchive(String corpId, Integer limit) {
        if (isBlank(corpId)) {
            throw new IllegalArgumentException("corpId不能为空");
        }
        int fetchLimit = limit == null || limit < 1 ? 100 : Math.min(limit, 1000);
        Date now = new Date();
        YgtArchiveCursorEntity globalCursor = getOrCreateCursor(corpId, GLOBAL_CURSOR_CHAT);
        globalCursor.setYgtAcLastPullTime(now);
        globalCursor.setYgtAcLastPullStatus("RUNNING");
        globalCursor.setYgtAcLastError(null);
        ygtArchiveCursorDao.update(globalCursor);

        try {
            QyGbDisCorpMsgAuditEntity config = qyGbDisCorpMsgAuditDao.queryByCorpId(corpId);
            // macOS mock 模式：不检查真实配置，用占位符初始化
            boolean isMacMock = System.getProperty("os.name", "").toLowerCase().contains("mac");
            if (isMacMock) {
                System.out.println("[YGT ArchivePull] 🍎 macOS mock 模式，跳过配置检查");
                if (config == null) {
                    config = new QyGbDisCorpMsgAuditEntity();
                }
                if (isBlank(config.getQyGbDisCorpMsgAuditSecret())) {
                    config.setQyGbDisCorpMsgAuditSecret("MOCK_SECRET");
                }
                if (isBlank(config.getQyGbDisCorpMsgAuditPrivateKey())) {
                    config.setQyGbDisCorpMsgAuditPrivateKey("MOCK_PRIVATE_KEY");
                }
            } else if (config == null || isBlank(config.getQyGbDisCorpMsgAuditSecret()) || isBlank(config.getQyGbDisCorpMsgAuditPrivateKey())) {
                throw new IllegalStateException("会话存档配置不完整");
            }

            Map<String, Object> groupQuery = new HashMap<>();
            groupQuery.put("corpId", corpId);
            List<YgtWecomGroupEntity> enabledGroups = ygtWecomGroupDao.queryEnabledGroups(groupQuery);
            Map<String, YgtWecomGroupEntity> enabledGroupMap = new HashMap<>();
            for (YgtWecomGroupEntity group : enabledGroups) {
                enabledGroupMap.put(group.getYgtWgChatId(), group);
            }

            if (!weworkFinanceSdkUtil.init(corpId, config.getQyGbDisCorpMsgAuditSecret())) {
                throw new IllegalStateException("会话存档SDK初始化失败");
            }

            String chatData = weworkFinanceSdkUtil.getChatData(globalCursor.getYgtAcLastSeq(), fetchLimit);
            int fetched = 0;
            int inserted = 0;
            int candidates = 0;
            int duplicateSkipped = 0;
            int skipped = 0;
            int failed = 0;
            long maxSeq = globalCursor.getYgtAcLastSeq() == null ? 0L : globalCursor.getYgtAcLastSeq();

            if (!isBlank(chatData)) {
                JSONObject root = JSON.parseObject(chatData);
                JSONArray chatList = root.getJSONArray("chatdata");
                if (chatList != null) {
                    fetched = chatList.size();
                    for (int i = 0; i < chatList.size(); i++) {
                        JSONObject encrypted = chatList.getJSONObject(i);
                        long seq = encrypted.getLongValue("seq");
                        maxSeq = Math.max(maxSeq, seq);
                        int publicKeyVer = encrypted.getIntValue("publickey_ver");
                        String msgId = encrypted.getString("msgid");

                        if ((msgId != null && ygtChatMessageDao.queryByMsgId(corpId, msgId) != null)
                                || ygtChatMessageDao.queryBySeq(corpId, seq) != null) {
                            duplicateSkipped++;
                            continue;
                        }

                        if (!isMacMock) {
                            weworkFinanceSdkUtil.addPrivateKey(publicKeyVer, config.getQyGbDisCorpMsgAuditPrivateKey());
                        }
                        String plain = weworkFinanceSdkUtil.decryptData(
                                encrypted.getString("encrypt_random_key"),
                                encrypted.getString("encrypt_chat_msg"),
                                publicKeyVer);
                        if (isBlank(plain)) {
                            skipped++;
                            continue;
                        }

                        JSONObject messageJson = JSON.parseObject(plain);
                        String chatId = messageJson.getString("roomid");
                        YgtWecomGroupEntity group = enabledGroupMap.get(chatId);
                        if (group == null) {
                            skipped++;
                            continue;
                        }

                        YgtChatMessageEntity message = buildMessage(corpId, group, encrypted, messageJson, seq, publicKeyVer);
                        if ((message.getYgtCmMsgId() != null && ygtChatMessageDao.queryByMsgId(corpId, message.getYgtCmMsgId()) != null)
                                || ygtChatMessageDao.queryBySeq(corpId, seq) != null) {
                            duplicateSkipped++;
                            continue;
                        }
                        try {
                            ygtChatMessageDao.save(message);
                        } catch (DuplicateKeyException duplicate) {
                            duplicateSkipped++;
                            continue;
                        } catch (Exception itemError) {
                            failed++;
                            continue;
                        }
                        inserted++;
                        updateGroupCursor(corpId, chatId, seq, now, null);

                        if ("text".equals(message.getYgtCmMsgType()) && "send".equals(message.getYgtCmAction())) {
                            try {
                                Map<String, Object> parseResult = ygtMessageService.parseMessage(message.getYgtChatMessageId());
                                if ("CANDIDATE_CREATED".equals(parseResult.get("status"))) {
                                    candidates++;
                                }
                            } catch (Exception parseError) {
                                failed++;
                            }
                        }
                    }
                }
            }

            globalCursor.setYgtAcLastSeq(maxSeq);
            globalCursor.setYgtAcLastPullStatus("SUCCESS");
            globalCursor.setYgtAcLastError(null);
            globalCursor.setYgtAcLastPullTime(now);
            globalCursor.setYgtAcLastSuccessTime(new Date());
            ygtArchiveCursorDao.update(globalCursor);

            Map<String, Object> result = new HashMap<>();
            result.put("fetched", fetched);
            result.put("processed", fetched);
            result.put("inserted", inserted);
            result.put("savedMessages", inserted);
            result.put("createdCandidates", candidates);
            result.put("duplicateSkipped", duplicateSkipped);
            result.put("duplicates", duplicateSkipped);
            result.put("skipped", skipped);
            result.put("failed", failed);
            result.put("lastSeq", maxSeq);
            return result;
        } catch (Exception e) {
            globalCursor.setYgtAcLastPullStatus("FAILED");
            globalCursor.setYgtAcLastError(safeError(e));
            globalCursor.setYgtAcLastPullTime(now);
            ygtArchiveCursorDao.update(globalCursor);
            throw new IllegalStateException(safeError(e), e);
        }
    }

    @Override
    public List<Map<String, Object>> archiveStatus(String corpId) {
        Map<String, Object> params = new HashMap<>();
        params.put("corpId", corpId);
        List<YgtArchiveCursorEntity> cursors = ygtArchiveCursorDao.queryList(params);
        List<Map<String, Object>> result = new ArrayList<>();
        for (YgtArchiveCursorEntity cursor : cursors) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", cursor.getYgtArchiveCursorId());
            row.put("corpId", cursor.getYgtAcCorpId());
            row.put("chatId", cursor.getYgtAcChatId());
            row.put("lastSeq", cursor.getYgtAcLastSeq());
            row.put("status", cursor.getYgtAcLastPullStatus());
            row.put("lastError", cursor.getYgtAcLastError());
            row.put("lastPullTime", cursor.getYgtAcLastPullTime());
            row.put("lastSuccessTime", cursor.getYgtAcLastSuccessTime());
            result.add(row);
        }
        return result;
    }

    private YgtChatMessageEntity buildMessage(String corpId, YgtWecomGroupEntity group, JSONObject encrypted,
                                              JSONObject messageJson, long seq, int publicKeyVer) {
        Date now = new Date();
        YgtChatMessageEntity message = new YgtChatMessageEntity();
        message.setYgtCmCorpId(corpId);
        message.setYgtCmGroupId(group.getYgtWecomGroupId());
        message.setYgtCmChatId(group.getYgtWgChatId());
        message.setYgtCmMsgId(messageJson.getString("msgid") == null ? encrypted.getString("msgid") : messageJson.getString("msgid"));
        message.setYgtCmSeq(seq);
        message.setYgtCmPublicKeyVer(publicKeyVer);
        message.setYgtCmAction(messageJson.getString("action"));
        message.setYgtCmFromUser(messageJson.getString("from"));
        message.setYgtCmMsgTime(messageJson.getLong("msgtime"));
        message.setYgtCmMsgType(messageJson.getString("msgtype"));
        message.setYgtCmContent(extractTextContent(messageJson));
        message.setYgtCmRawJson(messageJson.toJSONString());
        message.setYgtCmParseStatus("NEW");
        message.setYgtCmCreateTime(now);
        message.setYgtCmUpdateTime(now);
        return message;
    }

    private String extractTextContent(JSONObject messageJson) {
        if (!"text".equals(messageJson.getString("msgtype"))) {
            return null;
        }
        JSONObject text = messageJson.getJSONObject("text");
        if (text != null) {
            return text.getString("content");
        }
        String content = messageJson.getString("content");
        if (!isBlank(content)) {
            try {
                JSONObject contentJson = JSON.parseObject(content);
                return contentJson.getString("content");
            } catch (Exception ignored) {
                return content;
            }
        }
        return null;
    }

    private YgtArchiveCursorEntity getOrCreateCursor(String corpId, String chatId) {
        YgtArchiveCursorEntity cursor = ygtArchiveCursorDao.queryByCorpAndChatId(corpId, chatId);
        if (cursor != null) {
            return cursor;
        }
        Date now = new Date();
        cursor = new YgtArchiveCursorEntity();
        cursor.setYgtAcCorpId(corpId);
        cursor.setYgtAcChatId(chatId);
        cursor.setYgtAcLastSeq(0L);
        cursor.setYgtAcLastPullStatus("INIT");
        cursor.setYgtAcCreateTime(now);
        cursor.setYgtAcUpdateTime(now);
        ygtArchiveCursorDao.save(cursor);
        return cursor;
    }

    private void updateGroupCursor(String corpId, String chatId, long seq, Date pullTime, String error) {
        YgtArchiveCursorEntity cursor = getOrCreateCursor(corpId, chatId);
        cursor.setYgtAcLastSeq(Math.max(cursor.getYgtAcLastSeq() == null ? 0L : cursor.getYgtAcLastSeq(), seq));
        cursor.setYgtAcLastPullStatus(error == null ? "SUCCESS" : "FAILED");
        cursor.setYgtAcLastError(error);
        cursor.setYgtAcLastPullTime(pullTime);
        if (error == null) {
            cursor.setYgtAcLastSuccessTime(new Date());
        }
        ygtArchiveCursorDao.update(cursor);
    }

    private String safeError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getSimpleName();
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
