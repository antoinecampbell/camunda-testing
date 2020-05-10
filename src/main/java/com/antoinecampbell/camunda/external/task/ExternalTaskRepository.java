package com.antoinecampbell.camunda.external.task;

import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ExternalTaskRepository extends Repository<ExternalTaskEntity, String> {

    @Query("SELECT distinct topic_name_ " +
            "FROM act_ru_ext_task " +
            "WHERE lock_exp_time_ IS NULL OR lock_exp_time_ < NOW()")
    List<String> getAvailableTopics();

    @Query("SELECT t.id_ " +
            "FROM act_ru_ext_task t " +
            "JOIN act_ru_execution e ON t.proc_inst_id_ = e.proc_inst_id_ " +
            "WHERE e.business_key_ = :businessKey " +
            "AND t.topic_name_ = :topicName " +
            "LIMIT 1")
    String getExternalTaskId(String businessKey, String topicName);
}
