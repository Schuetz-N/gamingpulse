package dev.gamingpulse.repository;

import dev.gamingpulse.model.ServiceHealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ServiceHealthRecordRepository extends JpaRepository<ServiceHealthRecord, Long> {

    List<ServiceHealthRecord> findByCheckedAtAfterOrderByCheckedAtAsc(Instant since);

    List<ServiceHealthRecord> findByServiceAndCheckedAtAfterOrderByCheckedAtAsc(
            String service, Instant since
    );

    @Modifying
    @Query("DELETE FROM ServiceHealthRecord s WHERE s.checkedAt < :cutoff")
    void deleteOlderThan(@Param("cutoff") Instant cutoff);
}