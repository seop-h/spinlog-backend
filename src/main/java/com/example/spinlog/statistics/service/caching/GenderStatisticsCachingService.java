package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCachingService {
    private final CacheService cacheService;

    // todo Last30Days 이름 변경
    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountSumKeyName(registerType));

        Map<String, Object> hashEntriesCount = cacheService.getHashEntries(
                getGenderEmotionStatisticsAmountCountKeyName(registerType));

        // todo null check -> DB 조회로 대체

        hashEntriesSum.forEach((key, value) -> {
            if (!hashEntriesCount.containsKey(key)) {
                throw new IllegalStateException("Key " + key + " is missing in hashEntriesCount");
            }
        });

        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        hashEntriesSum.forEach((k,v) -> {
                    long amount = Long.parseLong(v.toString());
                    long count = Long.parseLong(hashEntriesCount.get(k).toString());
                    long average =  amount / count;
                    genderEmotionAmountAverage.put(k, average);
                });

        return genderEmotionAmountAverage.entrySet().stream()
                .map(e -> {
                    String[] key = e.getKey().split("::");
                    assert key.length == 2;
                    return new GenderEmotionAmountAverageDto(
                            Gender.valueOf(key[0]),
                            Emotion.valueOf(key[1]),
                            Long.parseLong(e.getValue().toString()));
                }).toList();
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDayLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = cacheService.getHashEntries(
                getGenderDailyStatisticsAmountSumKeyName(registerType));

        return hashEntriesSum.entrySet().stream()
                .map(e -> {
                    String[] key = e.getKey().split("::");
                    assert key.length == 2;
                    // todo exception handling
                    LocalDate date = LocalDate.parse(key[1]);
                    return new GenderDailyAmountSumDto(
                            Gender.valueOf(key[0]),
                            date,
                            Long.parseLong(e.getValue().toString()));
                }).toList();
    }

    public List<MemoDto> getAllMemosByGenderLast30Days(RegisterType registerType, Gender gender) {
        return null;
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionSumKeyName(registerType));

        Map<String, Object> hashEntriesCount = cacheService.getHashEntries(
                getGenderStatisticsSatisfactionCountKeyName(registerType));

        // tood 메서드로 분리
        hashEntriesSum.forEach((key, value) -> {
            if (!hashEntriesCount.containsKey(key)) {
                throw new IllegalStateException("Key " + key + " is missing in hashEntriesCount");
            }
        });

        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        hashEntriesSum.forEach((k,v) -> {
            double satisfactionSum = Double.parseDouble(v.toString());
            long count = Long.parseLong(hashEntriesCount.get(k).toString());
            float average = (float)(satisfactionSum / (double) count);
            genderSatisfactionAverage.put(k, average);
        });

        return genderSatisfactionAverage.entrySet().stream()
                .map(e -> GenderSatisfactionAverageDto.builder()
                        .gender(Gender.valueOf(e.getKey()))
                        .satisfactionAverage(e.getValue())
                        .build())
                .toList();
    }
}
