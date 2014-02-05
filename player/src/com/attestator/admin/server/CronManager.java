package com.attestator.admin.server;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.attestator.common.shared.vo.CronTaskLockVO;
import com.attestator.common.shared.vo.CronTaskVO;
import com.attestator.common.shared.vo.TenantableCronTaskVO;
import com.attestator.common.shared.vo.UpdateMetatestsSharingTaskVO;
import com.attestator.player.server.Singletons;

public class CronManager {
    private static final Logger logger = Logger
            .getLogger(CronManager.class);
    
    public static final long TASK_POLLING_INTERVAL = 60 * 1000;
    
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static Runnable pollCronTasks = new Runnable() {
        @Override
        public void run() {
            logger.debug("Polling for active cron tasks");            
            List<CronTaskVO> tasks = Singletons.sl().getActiveCronTasks();
            logger.debug("Find " + tasks.size() + " active cron tasks");
            for (CronTaskVO cronTask: tasks) {
                CronTaskLockVO lock = new CronTaskLockVO(cronTask.getId());
                if (LockManager.lock(lock)) {
                    logger.debug("Locked. Executing cron task: " + cronTask.toString());
                    try {
                        doTask(cronTask);
                    }
                    catch (Throwable e) {
                        logger.error("Error while executing CronTask: " + cronTask.toString(), e);                        
                    } finally {
                        Singletons.sl().removeCronTask(cronTask.getId());
                        LockManager.releaseLock(lock);
                    }
                }
                else {
                    logger.debug("Can't lock. Skip this task " + cronTask.toString());
                }
            }
        }
    };
    
    
    private static void doTask(CronTaskVO task) {
        try {
            if (task instanceof TenantableCronTaskVO) {
                LoginManager.login(null, ((TenantableCronTaskVO) task).getTenantId());
            }
            
            if (task instanceof UpdateMetatestsSharingTaskVO) {
                doSharingMetatestTask((UpdateMetatestsSharingTaskVO) task);
            }
        }
        finally {
            if (task instanceof TenantableCronTaskVO) {
                LoginManager.logout(null);
            }
        }
    }
    
    private static void doSharingMetatestTask(UpdateMetatestsSharingTaskVO task) {
        Date now = new Date();
        Singletons.al().updateAllMetatestsVisibilityOnDate(now);
    }
    
    public static void start() {
        scheduler.scheduleAtFixedRate(pollCronTasks, (int)Math.random() * TASK_POLLING_INTERVAL, TASK_POLLING_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    public static void stop() {
        try {            
            scheduler.shutdownNow();
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
}
