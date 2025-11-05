package com.twojz.y_kit.policy.repository;


import com.twojz.y_kit.policy.entity.PolicyDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyDetailRepository extends JpaRepository<PolicyDetailEntity, Long> {
    Optional<PolicyDetailEntity> findByPlcyNo(String plcyNo);

    @Query("SELECT d FROM PolicyDetailEntity d JOIN FETCH d.policy WHERE d.plcyNo = :plcyNo")
    Optional<PolicyDetailEntity> findByPlcyNoWithPolicy(@Param("plcyNo") String plcyNo);

}
