package com.github.leo51645.dailyfeed;

import com.github.leo51645.dailyfeed.domain.dto.response.AssetResponseDto;
import com.github.leo51645.dailyfeed.domain.enums.Assets;
import com.github.leo51645.dailyfeed.service.YahooFinanceService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;

@SpringBootApplication
public class DailyFeedApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(DailyFeedApplication.class, args);
        YahooFinanceService service = ctx.getBean(YahooFinanceService.class);

        Map<Assets, List<AssetResponseDto>> all = service.getAllAssets();
        all.forEach((asset, dtos) -> {
            System.out.println("=== " + asset.name() + " ===");
            dtos.forEach(dto -> System.out.println(dto.getDate() + " | " + dto.getCurrentPrice() + " | " + dto.getChangePercentClosedMarket()));
        });
    }

}
