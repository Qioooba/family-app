package com.family.family.service.scene;

import com.family.family.entity.Reminder;
import com.family.family.service.SceneCacheService;
import com.family.family.service.WaterRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SceneIntervalHandlersTest {

    private StubSceneCacheService sceneCacheService;
    private StubWaterRecordService waterRecordService;

    @BeforeEach
    void setUp() {
        sceneCacheService = new StubSceneCacheService();
        waterRecordService = new StubWaterRecordService();
    }

    @Test
    void waterHandlerShouldNotTriggerWhenTargetCompleted() {
        WaterHandler handler = new WaterHandler();
        ReflectionTestUtils.setField(handler, "sceneCacheService", sceneCacheService);
        ReflectionTestUtils.setField(handler, "waterRecordService", waterRecordService);

        waterRecordService.todayWaterInfo = Map.of(
            "todayAmount", 2000,
            "targetAmount", 2000,
            "percent", 100
        );

        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setCreateBy(7L);
        reminder.setBusinessData("{\"sceneType\":\"WATER\",\"intervalMinutes\":60,\"workHours\":[\"00:00\",\"23:59\"],\"workDaysOnly\":false}");

        assertFalse(handler.shouldTrigger(reminder));
    }

    @Test
    void waterHandlerShouldRespectCooldown() {
        WaterHandler handler = new WaterHandler();
        ReflectionTestUtils.setField(handler, "sceneCacheService", sceneCacheService);
        ReflectionTestUtils.setField(handler, "waterRecordService", waterRecordService);

        waterRecordService.todayWaterInfo = Map.of(
            "todayAmount", 500,
            "targetAmount", 2000,
            "percent", 25
        );
        sceneCacheService.shouldNotifyAgain = false;

        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setCreateBy(7L);
        reminder.setBusinessData("{\"sceneType\":\"WATER\",\"intervalMinutes\":60,\"workHours\":[\"00:00\",\"23:59\"],\"workDaysOnly\":false}");

        assertFalse(handler.shouldTrigger(reminder));
    }

    @Test
    void eyeRestHandlerShouldRespectCooldown() {
        EyeRestHandler handler = new EyeRestHandler();
        ReflectionTestUtils.setField(handler, "sceneCacheService", sceneCacheService);
        sceneCacheService.shouldNotifyAgain = true;

        Reminder reminder = new Reminder();
        reminder.setId(2L);
        reminder.setBusinessData("{\"sceneType\":\"EYE_REST\",\"screenTime\":45,\"workHours\":[\"00:00\",\"23:59\"],\"workDaysOnly\":false}");

        assertTrue(handler.shouldTrigger(reminder));
    }

    @Test
    void sedentaryHandlerShouldRespectCooldown() {
        SedentaryHandler handler = new SedentaryHandler();
        ReflectionTestUtils.setField(handler, "sceneCacheService", sceneCacheService);
        sceneCacheService.shouldNotifyAgain = false;

        Reminder reminder = new Reminder();
        reminder.setId(3L);
        reminder.setBusinessData("{\"sceneType\":\"SEDENTARY\",\"sitDuration\":60,\"workHours\":[\"00:00\",\"23:59\"],\"workDaysOnly\":false}");

        assertFalse(handler.shouldTrigger(reminder));
    }

    private static class StubSceneCacheService extends SceneCacheService {
        private boolean shouldNotifyAgain = true;

        @Override
        public boolean shouldNotifyAgain(Long reminderId, int intervalMinutes) {
            return shouldNotifyAgain;
        }
    }

    private static class StubWaterRecordService implements WaterRecordService {
        private Map<String, Object> todayWaterInfo = Map.of(
            "todayAmount", 0,
            "targetAmount", 2000,
            "percent", 0
        );

        @Override
        public Map<String, Object> getTodayWaterInfo(Long userId) {
            return todayWaterInfo;
        }

        @Override
        public com.family.family.entity.WaterRecord addRecord(Long userId, Integer amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.List<com.family.family.entity.WaterRecord> getRecordsByDate(Long userId, LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer getTotalAmountByDate(Long userId, LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteRecord(Long userId, Long recordId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer getUserWaterTarget(Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer saveUserWaterTarget(Long userId, Integer targetAmount) {
            throw new UnsupportedOperationException();
        }
    }
}
