package com.eg.videouploadtofastdfs;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @time 2020-02-02 00:03
 */
@Repository
public interface TsRepository extends MongoRepository<Ts, String> {

}
