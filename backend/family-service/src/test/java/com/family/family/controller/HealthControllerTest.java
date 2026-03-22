package com.family.family.controller;

import com.family.family.entity.WaterRecord;
import com.family.family.service.StatsService;
import com.family.family.service.WaterRecordService;
import com.family.family.support.SaTokenTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthControllerTest {

    private StubWaterRecordService waterRecordService;
    private StubStatsService statsService;
    private HealthController controller;

    @BeforeEach
    void setUp() {
        waterRecordService = new StubWaterRecordService();
        statsService = new StubStatsService();
        controller = new HealthController(statsService, waterRecordService);
    }

    @AfterEach
    void tearDown() {
        SaTokenTestHelper.clear();
    }

    @Test
    void getTodayWaterShouldIgnoreRequestedUserId() {
        SaTokenTestHelper.login(7L);

        Map<String, Object> response = controller.getTodayWater(99L);

        assertEquals(200, response.get("code"));
        assertEquals(7L, waterRecordService.lastUserId);
    }

    @Test
    void deleteWaterRecordShouldUseCurrentUserId() {
        SaTokenTestHelper.login(7L);

        Map<String, Object> response = controller.deleteWaterRecord(12L);

        assertEquals(200, response.get("code"));
        assertEquals(7L, waterRecordService.deletedUserId);
        assertEquals(12L, waterRecordService.deletedRecordId);
    }

    private static class StubWaterRecordService implements WaterRecordService {
        private Long lastUserId;
        private Long deletedUserId;
        private Long deletedRecordId;

        @Override
        public Map<String, Object> getTodayWaterInfo(Long userId) {
            lastUserId = userId;
            return Map.of("todayAmount", 500, "targetAmount", 2000);
        }

        @Override
        public WaterRecord addRecord(Long userId, Integer amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<WaterRecord> getRecordsByDate(Long userId, LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Integer getTotalAmountByDate(Long userId, LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteRecord(Long userId, Long recordId) {
            deletedUserId = userId;
            deletedRecordId = recordId;
            return true;
        }

        @Override
        public Integer getUserWaterTarget(Long userId) {
            return 2000;
        }

        @Override
        public Integer saveUserWaterTarget(Long userId, Integer targetAmount) {
            return targetAmount;
        }
    }

    private static class StubStatsService implements StatsService {
        @Override
        public Map<String, Object> getFamilyStats(Long familyId, String type) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getUserStats(Long userId) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getPersonalStats(Long userId, String type, String date) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getTaskStats(Long familyId, String startDate, String endDate) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getDietStats(Long userId, String type) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getYearlyStats(Long familyId, int year) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getTodayOverview(Long userId, Long familyId) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getFamilyMonthlyStats(Long familyId) {
            return new HashMap<>();
        }

        @Override
        public Map<String, Object> getDailyDietStats(Long userId, LocalDate date) {
            return new HashMap<>();
        }
    }
}
