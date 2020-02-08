package com.eg.videouploadtofastdfs;

import lombok.Data;

import java.io.File;
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
