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

}
