package com.l.cloud.job.dao;

import com.l.cloud.job.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskDao extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByFlag(Integer flag);

    @Query("UPDATE Task SET flag = :flag WHERE id = :id")
    @Modifying
    void putFlagById(Integer flag, Long id);
}
