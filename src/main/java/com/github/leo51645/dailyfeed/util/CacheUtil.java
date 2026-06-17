package com.github.leo51645.dailyfeed.util;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

@Component
public class CacheUtil {

    public Path getCacheURI(String date) {
        return Paths.get("cache/day_" + date + ".json");
    }
}
