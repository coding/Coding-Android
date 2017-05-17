package net.coding.program.project.detail.file.v2;

import net.coding.program.network.model.file.CodingFile;

public interface UploadCallback {

    void onSuccess(CodingFile codingFile);
}
