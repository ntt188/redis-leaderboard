package com.example.leaderboard;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class LeaderboardService {

    private static final String KEY = "leaderboard";
    private final StringRedisTemplate redis;

    public LeaderboardService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // ZADD leaderboard <score> <username>
    public void updateScore(String username, double score) {
        redis.opsForZSet().add(KEY, username, score);
    }

    // ZREVRANGE leaderboard 0 n-1 WITHSCORES
    public List<PlayerScore> getTop(int n) {
        Set<TypedTuple<String>> tuples = redis.opsForZSet().reverseRangeWithScores(KEY, 0, n - 1);
        List<PlayerScore> result = new ArrayList<>();
        int rank = 1;
        if (tuples != null) {
            for (TypedTuple<String> t : tuples) {
                result.add(new PlayerScore(rank++, t.getValue(), t.getScore()));
            }
        }
        return result;
    }

    public record PlayerScore(int rank, String username, Double score) {}
}
