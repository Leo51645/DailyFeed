package com.github.leo51645.dailyfeed.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class CacheService {

    ObjectMapper objectMapper = new ObjectMapper();

    public void save(String aiResponseString) {


    }
}
