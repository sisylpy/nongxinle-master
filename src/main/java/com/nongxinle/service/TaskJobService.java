package com.nongxinle.service;

import org.springframework.stereotype.Service;

import java.util.TimerTask;

/** GB 日结定时任务已移除（GB-2a）；保留类占位，避免调度器 bean 缺失。 */
@Service
public class TaskJobService extends TimerTask {

    @Override
    public void run() {
        // GB department goods daily rollover removed with GB-2a admin closure.
    }
}
