package net.coding.program.network;

import com.liulishuo.filedownloader.BaseDownloadTask;

/**
 * Created by chenchao on 2017/5/17.
 */

public interface FileDownloadCallback {
    void progress(final BaseDownloadTask task, final int soFarBytes, final int totalBytes);

    void completed(final BaseDownloadTask task);

    void error(final BaseDownloadTask task, final Throwable e);
}
