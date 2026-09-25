package com.example.leaderboard;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    static final int MAX_TOP = 100;

    private final LeaderboardService service;

    public LeaderboardController(LeaderboardService service) {
        this.service = service;
    }

    // POST /leaderboard/{username}   body: {"score": 1500}
    @PostMapping("/{username}")
    public ResponseEntity<Map<String, Object>> updateScore(@PathVariable String username,
                                                           @Valid @RequestBody ScoreRequest request) {
        service.updateScore(username, request.score());
        return ResponseEntity.ok(Map.of("username", username, "score", request.score()));
    }

    // GET /leaderboard/top/{n}   (1 <= n <= MAX_TOP)
    @GetMapping("/top/{n}")
    public ResponseEntity<?> getTop(@PathVariable int n) {
        if (n < 1 || n > MAX_TOP) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "n must be between 1 and " + MAX_TOP));
        }
        return ResponseEntity.ok(service.getTop(n));
    }

    // Double (không phải double) để phát hiện khi thiếu "score"
    public record ScoreRequest(
            @NotNull(message = "score is required")
            @PositiveOrZero(message = "score must be >= 0")
            Double score) {}
}
