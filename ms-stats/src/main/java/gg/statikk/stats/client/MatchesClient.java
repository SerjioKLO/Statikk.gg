package gg.statikk.stats.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Fuente de verdad de las partidas. ms-stats NO guarda partidas: ante cada cambio
 * las vuelve a leer y recalcula, así los eventos pueden ser mínimos y el cálculo
 * es idempotente (recalcular dos veces da el mismo resultado).
 */
@FeignClient(name = "ms-matches")
public interface MatchesClient {

    @GetMapping("/api/matches")
    List<MatchClientResponse> listByPlayer(@RequestParam("playerProfileId") Long playerProfileId);
}
