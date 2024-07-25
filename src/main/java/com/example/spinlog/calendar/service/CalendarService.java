package com.example.spinlog.calendar.service;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.calendar.dto.*;
import com.example.spinlog.calendar.repository.CalenderRepository;
import com.example.spinlog.global.error.exception.user.UserNotFoundException;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final UserRepository userRepository;
    private final CalenderRepository calenderRepository;

    public TotalCalendarResponseDto requestTotal(String userName, String selectDate) {
        User user = getUser(userName);
        List<Article> articles = user.getArticles();

        LocalDate parsedDate = DateUtils.parseStringToDate(selectDate);

        BudgetDto budgetDto = BudgetDto.of(user, parsedDate);

        List<MonthSpend> monthSpendList = createMonthSpendList(parsedDate, articles);

        List<DaySpend> daySpendList = articles.stream()
                .filter(article -> article.getSpendDate().toLocalDate().equals(parsedDate))
                .map(this::mapToDaySpend)
                .toList();

        return TotalCalendarResponseDto.builder()
                .budgetDto(budgetDto)
                .monthSpendList(monthSpendList)
                .daySpendList(daySpendList)
                .build();
    }

    public DailyCalendarResponseDto requestDaily(String userName, String selectDate) {
        List<Article> articles = getArticlesFromUser(userName);

        LocalDate parsedDate = DateUtils.parseStringToDate(selectDate);

        List<DaySpend> daySpendList = articles.stream()
                .filter(article -> article.getSpendDate().toLocalDate().equals(parsedDate))
                .map(this::mapToDaySpend)
                .toList();

        return DailyCalendarResponseDto.builder()
                .daySpendList(daySpendList)
                .build();
    }

    private User getUser(String userName) {
        return userRepository.findByAuthenticationName(userName).stream()
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userName));
    }

    private List<Article> getArticlesFromUser(String userName) {
        User user = getUser(userName);
        return user.getArticles();
    }

    private List<MonthSpend> createMonthSpendList(LocalDate parsedDate, List<Article> articles) {
        return articles.stream()
                .filter(article -> isSameMonth(parsedDate, article.getSpendDate()))
                .collect(Collectors.groupingBy(article -> article.getSpendDate().toLocalDate()))
                .entrySet()
                .stream()
                .map(entry -> createMonthSpendsFromEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

    private boolean isSameMonth(LocalDate parsedDate, LocalDateTime date) {
        return date.getMonth() == parsedDate.getMonth();
    }

    private MonthSpend createMonthSpendsFromEntry(LocalDate date, List<Article> articlesOnDate) {
        int totalDaySpend = calculateTotalAmountByType(articlesOnDate, RegisterType.SPEND);
        int totalDaySave = calculateTotalAmountByType(articlesOnDate, RegisterType.SAVE);

        return MonthSpend.builder()
                .date(date.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .daySpend(totalDaySpend)
                .daySave(totalDaySave)
                .build();
    }

    private DaySpend mapToDaySpend(Article article) {
        return DaySpend.builder()
                .articleId(article.getArticleId())
                .registerType(article.getRegisterType())
                .amount(article.getAmount())
                .content(article.getContent())
                .satisfaction(article.getSatisfaction())
                .emotion(article.getEmotion())
                .build();
    }

    private int calculateTotalAmountByType(List<Article> articles, RegisterType registerType) {
        return articles.stream()
                .filter(article -> article.getRegisterType() == registerType)
                .mapToInt(Article::getAmount)
                .sum();
    }
}
