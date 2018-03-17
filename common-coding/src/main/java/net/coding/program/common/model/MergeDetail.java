package net.coding.program.common.model;

import net.coding.program.common.HtmlContent;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by chenchao on 15/6/4.
 */
public class MergeDetail implements Serializable {
    String merge_request_description = "";
    boolean can_edit_src_branch;
    boolean can_edit;
    boolean author_can_edit;
    Merge merge_request;
    private boolean canMerge;

    public MergeDetail(JSONObject json) {
        merge_request_description = json.optString("merge_request_description", "");
        if (merge_request_description.isEmpty()) {
            merge_request_description = json.optString("pull_request_description", "");
        }

        can_edit_src_branch = json.optBoolean("can_edit_src_branch");
        can_edit = json.optBoolean("can_edit");
        author_can_edit = json.optBoolean("author_can_edit");

        canMerge = json.optBoolean("can_merge", false);

        if (json.has("pull_request")) {
            merge_request = new Merge(json.optJSONObject("pull_request"));
        } else {
            merge_request = new Merge(json.optJSONObject("merge_request"));
        }
    }

    public boolean isCanEditSrcBranch() {
        return merge_request.authorIsMe();
    }

    public boolean isCan_edit_src_branch() {
        return can_edit_src_branch;
    }

    public boolean authorCanEdit() {
        return author_can_edit;
    }

    public boolean isCanEdit() {
        return can_edit;
    }

    public boolean isCanMerge() {
        return canMerge;
    }


    public String getContent() {
        String content = merge_request_description;
        if (content.isEmpty()) {
            content = merge_request.getContent();
        }
        return HtmlContent.parseReplacePhoto(content).text;
    }

    public String getBody() {
        String body = merge_request_description;
        if (body.isEmpty()) {
            body = merge_request.getBody();
        }
        return body;
    }

    public RequestData getHttpMerge(String message, boolean delSrc) {
        return merge_request.getHttpMerge(message, delSrc);
    }

    public String generalMergeMessage() {
        return merge_request.generalMergeMessage();
    }

    public Merge getMerge() {
        return merge_request;
    }
}
