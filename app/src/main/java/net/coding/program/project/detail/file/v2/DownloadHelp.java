package net.coding.program.project.detail.file.v2;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;

import net.coding.program.network.FileDownloadCallback;
import net.coding.program.network.model.file.CodingFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by chenchao on 2017/5/17.
 * 使用第三方的下载模块
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

    Set<FileDownloadCallback> observers = new HashSet<>();
    private FileDownloadQueueSet queueSet;

    private DownloadHelp() {
        queueSet = new FileDownloadQueueSet(downloadListener);
        queueSet.setAutoRetryTimes(1);
//        queueSet.setCallbackProgressTimes(1000);
//        queueSet.setCallbackProgressMinInterval(1000);
    }

    private FileDownloadListener downloadListener = new FileDownloadSampleListener() {
        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            for (FileDownloadCallback item : observers) {
                item.progress(task, soFarBytes, totalBytes);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
            for (FileDownloadCallback item : observers) {
                item.completed(task);
            }
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            for (FileDownloadCallback item : observers) {
                item.error(task, e);
            }
        }
    };

    public void register(FileDownloadCallback callback) {
        if (callback != null) {
            observers.add(callback);
        }
    }

    public void unregister(FileDownloadCallback callback) {
        if (callback != null) {
            observers.remove(callback);
        }
    }

    public void addTask(Set<CodingFile> files, String cookie, String basePath, int projectId) {
        Set<CodingFile> set = new HashSet<>();
        set.addAll(files);

        final List<BaseDownloadTask> tasks = new ArrayList<>();
        for (CodingFile item : set) {
            String url = item.url;
            String path = basePath + "/" + item.getSaveName(projectId);
            BaseDownloadTask task = FileDownloader.getImpl()
                    .create(url)
                    .setPath(path)
                    .addHeader("Cookie", cookie);
            item.task = task;

            tasks.add(task);
        }

        queueSet.downloadSequentially(tasks);
        queueSet.start();

        downloads.addAll(set);
    }

}
