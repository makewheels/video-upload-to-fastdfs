package com.eg.videouploadtofastdfs.repository;

import com.eg.videouploadtofastdfs.bean.MakeM3u8Result;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @time 2020-02-02 00:03
 */
@Repository
public interface TsRepository extends MongoRepository<MakeM3u8Result.Ts, String> {

}
