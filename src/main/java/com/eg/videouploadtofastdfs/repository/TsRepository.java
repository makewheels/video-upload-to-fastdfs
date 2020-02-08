package com.eg.videouploadtofastdfs.repository;

import com.eg.videouploadtofastdfs.bean.Ts;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @time 2020-02-02 00:03
 */
@Repository
public interface TsRepository extends MongoRepository<Ts, String> {
    List<Ts> findByVideoId(String videoId);
}
