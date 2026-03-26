package com.fpl.repository;

import com.fpl.model.PlayerGameweekScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerGameweekScoreRepository extends JpaRepository<PlayerGameweekScoreEntity, Long> {

    @Query("SELECT SUM(s.points) FROM PlayerGameweekScoreEntity s WHERE s.player.id = :playerId")
    Long sumPointsForPlayer(@Param("playerId") Long playerId);

    @Query("SELECT SUM(s.points) FROM PlayerGameweekScoreEntity s WHERE s.player.id = :playerId AND s.gameweek BETWEEN :fromGw AND :toGw")
    Long sumPointsForPlayerBetweenGameweeks(
            @Param("playerId") Long playerId,
            @Param("fromGw") int fromGw,
            @Param("toGw") int toGw);
}
