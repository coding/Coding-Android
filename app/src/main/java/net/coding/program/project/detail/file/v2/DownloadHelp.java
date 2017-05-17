package net.coding.program.project.detail.file.v2;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;

import net.coding.program.network.model.file.CodingFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chenchao on 2017/5/17.
 */

public class DownloadHelp {

    private static DownloadHelp sDownloadHelp;

    public static DownloadHelp instance() {
        if (sDownloadHelp == null) {
            sDownloadHelp = new DownloadHelp();
        }
        return sDownloadHelp;
    }

    Set<CodingFile> downloads = new HashSet<>();

    private DownloadHelp() {
    }

    public void addTask(Set<CodingFile> files, String cookie, String basePath, int projectId, FileDownloadListener listener) {
        Set<CodingFile> set = new HashSet<>();
        set.addAll(files);

        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(listener);

        final List<BaseDownloadTask> tasks = new ArrayList<>();
        for (CodingFile item : set) {
            String url = item.url;
            String path = basePath + "/" + item.getSaveName(projectId);
            tasks.add(FileDownloader.getImpl()
                    .create(url)
                    .setPath(path)
                    .addHeader("Cookie", cookie));
        }
        queueSet.setAutoRetryTimes(1);

        queueSet.downloadSequentially(tasks);
        queueSet.start();

        downloads.addAll(set);
    }

}
