package net.coding.program.common.url;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 15/5/26.
 */
public class UrlCreate {

    public static String gitTree(String owner, String project, String version, String path) {
        final String HOST_GIT_TREE = Global.HOST + "/api/user/%s/project/%s/git/tree/%s%s";

        String filePath = encode2(path);
        return String.format(HOST_GIT_TREE,
                owner, project, version, filePath);
    }

    public static String gitTreeinfo(String owner, String project, String version, String path) {
        final String HOST_GIT_TREEINFO = Global.HOST + "/api/user/%s/project/%s/git/treeinfo/%s%s";
        String filePath = encode2(path);
        return String.format(HOST_GIT_TREEINFO,
                owner, project, version, filePath);
    }

    public static String gitTreeCommit(String owner, String project, String version, String path) {
        final String HOST_GIT_TREEINFO = Global.HOST + "/api/user/%s/project/%s/git/commits/%s%s";
        String filePath = encode2Pager(path);
        return String.format(HOST_GIT_TREEINFO,
                owner, project, version, filePath);
    }

    // 重编码2次，git服务器那边要求的
    private static String encode2Pager(String path) {
        if (path == null || path.isEmpty()) {
            path = "";
        }

        String dest = Global.encodeUtf8("/" + path);
        dest = Global.encodeUtf8(dest);
        return dest + "?";
    }

    // 重编码2次，git服务器那边要求的
    private static String encode2(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String dest = Global.encodeUtf8("/" + path);
        dest = Global.encodeUtf8(dest);
        return dest;
    }
}
