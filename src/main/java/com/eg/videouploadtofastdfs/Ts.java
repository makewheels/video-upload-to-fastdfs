package com.eg.videouploadtofastdfs;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @time 2020-02-08 12:12
 */
@Data
@Document
public class Ts {
    @Id
    private String _id;
    private int index;          //视频中的索引
    private String videoId;     //视频id
    private String fastdfsFileId;//文件id
    private String filename;    //文件名
    private Date createTime;
}
