package com.eg.videouploadtofastdfs.run;

import com.eg.videouploadtofastdfs.bean.Ts;
import com.eg.videouploadtofastdfs.bean.Video;
import com.eg.videouploadtofastdfs.repository.TsRepository;
import com.eg.videouploadtofastdfs.repository.VideoRepository;
import com.eg.videouploadtofastdfs.util.FastdfsUtil;
import org.csource.common.MyException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

/**
 * @time 2020-02-08 23:54
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DeleteUtil {
    @Autowired
    VideoRepository videoRepository;
    @Autowired
    TsRepository tsRepository;

    public void deleteVideo(String videoId) throws IOException, MyException {
        //先删ts碎片
        List<Ts> tsList = tsRepository.findByVideoId(videoId);
        for (Ts ts : tsList) {
            String fastdfsFileId = ts.getFastdfsFileId();
            int delete = FastdfsUtil.delete(fastdfsFileId);
            System.out.println(delete + " " + fastdfsFileId);
            tsRepository.delete(ts);
        }
        //再删video
        Video video = videoRepository.findByVideoId(videoId);
        FastdfsUtil.delete(video.getHtmlFastdfsFileId());
        FastdfsUtil.delete(video.getM3u8FastdfsFileId());
        videoRepository.delete(video);
    }

    @Test
    public void testDeleteVideo() {
        String videoId = "1c14ee36bd814aeea9c5a8ad1f93351c";
        try {
            deleteVideo(videoId);
        } catch (IOException | MyException e) {
            e.printStackTrace();
        }
    }
}
