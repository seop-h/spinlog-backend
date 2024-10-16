package com.example.spinlog.statistics.service.cache.scheduled.startup;

import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
class GenderStatisticsCacheStartupService {
    private final CacheHashRepository cacheHashRepository;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    @EventListener(ApplicationReadyEvent.class)
    public void initGenderStatisticsCache() {
        log.info("Start initializing Caching");

        statisticsPeriodManager.setStatisticsPeriodImmediatelyAfterSpringBootIsStarted();

        // have to lock
        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        AllGenderStatisticsCacheData allData = genderStatisticsRepositoryFetchService.getGenderStatisticsAllData(startDate, endDate);
        allData = StatisticsZeroPaddingUtils.zeroPaddingAllStatisticsMap(allData, period);

        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.genderEmotionAmountSpendSumsAndCounts().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                allData.genderEmotionAmountSpendSumsAndCounts().countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.genderEmotionAmountSaveSumsAndCounts().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE),
                allData.genderEmotionAmountSaveSumsAndCounts().countsMap());

        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                allData.genderDailyAmountSpendSums());
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE),
                allData.genderDailyAmountSaveSums());

        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                allData.genderSatisfactionSpendSumsAndCounts().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                allData.genderSatisfactionSpendSumsAndCounts().countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE),
                allData.genderSatisfactionSaveSumsAndCounts().sumsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE),
                allData.genderSatisfactionSaveSumsAndCounts().countsMap());

        log.info("Finish initializing Caching\ncacheDate = {}\n", allData);
    }
}
