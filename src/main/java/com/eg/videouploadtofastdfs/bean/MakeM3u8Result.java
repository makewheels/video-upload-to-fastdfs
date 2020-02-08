package com.eg.videouploadtofastdfs.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @time 2020-02-03 19:26
 */
@Data
public class MakeM3u8Result {
    private String id;
    private File folder;
    private File m3u8File;
    private List<File> tsFileList;

    /**
     * @time 2020-02-08 12:12
     */
    @Data
    @Document
    public static class Ts {
        @Id
        private String _id;
        private int index;          //视频中的索引
        private String videoId;     //视频id
        private String fastdfsFileId;//文件id
        private String filename;    //文件名
        private long filesize;      //文件大小
        private Date createTime;
    }
}
