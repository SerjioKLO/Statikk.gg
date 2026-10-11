package gg.statikk.excel.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ms-player")
public interface PlayerClient {

    @GetMapping("/api/players/{id}")
    PlayerProfileClientResponse getProfile(@PathVariable("id") Long id);
}
