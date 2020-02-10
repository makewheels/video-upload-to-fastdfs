package com.eg.videouploadtofastdfs.run;

import com.eg.videouploadtofastdfs.bean.MakeM3u8Result;
import com.eg.videouploadtofastdfs.bean.Ts;
import com.eg.videouploadtofastdfs.bean.Video;
import com.eg.videouploadtofastdfs.repository.TsRepository;
import com.eg.videouploadtofastdfs.repository.VideoRepository;
import com.eg.videouploadtofastdfs.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 上传到我自己搭建的fastdfs服务器
 *
 * @time 2020-02-08 11:12
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UploadToFastdfs {
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    TsRepository tsRepository;

    /**
     * 上传ts碎片
     *
     * @param makeM3u8Result
     * @param title
     * @param videoId
     * @return
     */
    private List<Ts> uploadTsList(MakeM3u8Result makeM3u8Result,
                                  String title, String videoId) {
        //为了用文件大小显示进度，先统计所有碎片总大小
        long totalSize = 0;
        long uploadSize = 0;
        List<File> tsFileList = makeM3u8Result.getTsFileList();
        for (File tsFile : tsFileList) {
            totalSize += tsFile.length();
        }
        List<Ts> tsList = new ArrayList<>();
        //执行上传
        long beforeUploadTime = System.currentTimeMillis();
        for (int i = 0; i < tsFileList.size(); i++) {
            //上传到文件服务器
            File tsFile = tsFileList.get(i);
            String fastdfsFileId = null;
            try {
                fastdfsFileId = FastdfsUtil.upload(tsFile);
            } catch (IOException | MyException e) {
                e.printStackTrace();
                try {
                    fastdfsFileId = FastdfsUtil.upload(tsFile);
                } catch (IOException | MyException ex) {
                    ex.printStackTrace();
                    try {
                        fastdfsFileId = FastdfsUtil.upload(tsFile);
                    } catch (IOException | MyException exc) {
                        exc.printStackTrace();
                    }
                }
            }

            //显示进度
            //ts文件大小
            long tsFileSize = tsFile.length();
            uploadSize += tsFileSize;
            double percent = uploadSize * 1.0 / totalSize * 100;
            String format = String.format("%.2f", percent);
            //花费时间，单位转化为秒
            double spendTime = (System.currentTimeMillis() - beforeUploadTime) / 1000;
            if (spendTime == 0) {
                spendTime = 0.001;
            }
            //上传速度
            double speed = uploadSize / spendTime;
            String speedString = FileUtil.getSizeString((long) (speed)) + "/s ";
            //剩余时间
            long remainSize = totalSize - uploadSize;
            int remainSecond = (int) (remainSize / speed);
            int minute = remainSecond / 60;
            int second = remainSecond % 60;
            System.out.println(minute + ":" + second + " " + speedString + format + "% (" + (i + 1)
                    + "/" + tsFileList.size() + ") " + FileUtil.getSizeString(tsFileSize) + " "
                    + title + " " + fastdfsFileId);

            //保存ts
            Ts ts = new Ts();
            ts.setCreateTime(new Date());
            ts.setFastdfsFileId(fastdfsFileId);
            ts.setVideoId(videoId);
            ts.setFilename(tsFile.getName());
            ts.setFilesize(tsFileSize);
            //设置索引，为了改m3u8文件的时候用
            String baseName = FilenameUtils.getBaseName(tsFile.getName());
            ts.setIndex(Integer.parseInt(baseName.split("-")[1]));
            tsRepository.save(ts);
            //加入到tsList中
            tsList.add(ts);
        }
        return tsList;
    }

    /**
     * 处理m3u8
     *
     * @param video
     * @param makeM3u8Result
     * @param tsList
     * @return m3u8文件url
     */
    private String handleM3u8File(Video video, MakeM3u8Result makeM3u8Result, List<Ts> tsList) {
        //修改m3u8文件
        File m3u8File = makeM3u8Result.getM3u8File();
        List<String> lines = null;
        try {
            lines = FileUtils.readLines(m3u8File, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //遍历m3u8文件每一行
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) {
                continue;
            }
            //找到匹配的index
            int index = Integer.parseInt(FilenameUtils.getBaseName(line).split("-")[1]);
            for (Ts ts : tsList) {
                if (index == ts.getIndex()) {
                    //设置新的url
                    lines.set(i, Constants.FASTDFS_SERVER + "/" + ts.getFastdfsFileId());
                }
            }
        }
        //重写m3u8文件
        try {
            FileUtils.writeLines(m3u8File, lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //上传m3u8文件
        String m3u8FastdfsFileId = "";
        try {
            m3u8FastdfsFileId = FastdfsUtil.upload(m3u8File);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
        video.setM3u8FastdfsFileId(m3u8FastdfsFileId);
        String m3u8Url = Constants.FASTDFS_SERVER + "/" + m3u8FastdfsFileId;
        video.setM3u8Url(m3u8Url);
        return m3u8Url;
    }

    /**
     * 准备单个视频文件
     *
     * @param videoFile
     */
    private Video prepareSingleVideo(File videoFile) {
        Video video = new Video();
        video.setCreateTime(new Date());
        //标题
        String title = FilenameUtils.getBaseName(videoFile.getName());
        video.setTitle(title);
        //生成视频id
        String videoId = UuidUtil.getUuid();
        video.setVideoId(videoId);
        //转码
        MakeM3u8Result makeM3u8Result = null;
        try {
            makeM3u8Result = VideoUtil.makeM3u8(videoFile, videoId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(title + " 转码完成，开始上传，ts碎片总共 "
                + makeM3u8Result.getTsFileList().size() + " 个");
        //上传ts碎片
        List<Ts> tsList = uploadTsList(makeM3u8Result, title, videoId);
        //修改m3u8文件，上传到服务器
        String m3u8Url = handleM3u8File(video, makeM3u8Result, tsList);
        //制作html
        Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("m3u8Url", m3u8Url);
        File htmlFile = null;
        try {
            htmlFile = FreemakerUtil.createHtmlByMode("video.html.ftl",
                    "video.html", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //上传html文件
        String htmlFastdfsFileId = null;
        try {
            htmlFastdfsFileId = FastdfsUtil.upload(htmlFile);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
        video.setHtmlFastdfsFileId(htmlFastdfsFileId);
        String htmlUrl = Constants.FASTDFS_SERVER + "/" + htmlFastdfsFileId;
        video.setHtmlUrl(htmlUrl);
        //删除html文件
        htmlFile.delete();
        //删除m3u8文件
        makeM3u8Result.getM3u8File().delete();
        //删除所有ts
        for (File tsFile : makeM3u8Result.getTsFileList()) {
            tsFile.delete();
        }
        //删除文件夹
        makeM3u8Result.getFolder().delete();
        //保存video
        videoRepository.save(video);
        return video;
    }

    @Test
    public void runUploadSingleVideo() {
//        String folder = "C:\\Users\\Administrator\\Videos\\Desktop";
//        String filename = "2020.02.09演示youtube-dl和音视频混流.mp4";
        String filePath = "D:\\FFOutput\\E23大破天幕杀机].James.Bond.007.Skyfall.2012.mp4";
        File videoFile = new File(filePath);
        Video video = prepareSingleVideo(videoFile);
        String htmlUrl = video.getHtmlUrl();
        System.out.println(htmlUrl);
    }

    @Test
    public void runUploadFolder() {
        File folder = new File("D:\\VDR");
        File[] files = folder.listFiles();
        for (File videoFile : files) {
            Video video = prepareSingleVideo(videoFile);
            String htmlUrl = video.getHtmlUrl();
            System.out.println(htmlUrl);
        }
    }
}
