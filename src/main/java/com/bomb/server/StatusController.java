package com.bomb.server;

import com.bomb.game.Bomb;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// The analyst's laptop calls this every second to show the time and strikes
@RestController
public class StatusController {

    private final GameService game;

    public StatusController(GameService game) {
        this.game = game;
    }

    @GetMapping("/api/status")
    public Map<String, Object> status() {
        Bomb bomb = game.getBomb();
        if (bomb == null) {
            return Map.of("state", "WAITING");
        }
        int timeLeft = bomb.secondsLeft();
        return Map.of(
                "state", bomb.getState().name(),
                "timeLeft", timeLeft,
                "strikes", bomb.getStrikes(),
                "solved", bomb.getSolved(),
                "total", bomb.getTotal());
    }
}
