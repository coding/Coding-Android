package net.coding.program.common.url;

import net.coding.program.common.Global;

/**
 * Created by chenchao on 15/5/26.
 */
public class UrlCreate {

    public static String gitTree(String projectPath, String version, String path) {
        final String HOST_GIT_TREE = Global.HOST_API + "%s/git/tree/%s%s";

        String filePath = pathEncode2(path);
        return String.format(HOST_GIT_TREE,
                projectPath, version, filePath);
    }

    public static String gitTreeinfo(String projectPath, String version, String path) {
        final String HOST_GIT_TREEINFO = Global.HOST_API + "%s/git/treeinfo/%s%s";
        String filePath = pathEncode2(path);
        return String.format(HOST_GIT_TREEINFO,
                projectPath, version, filePath);
    }

    public static String gitTreeCommit(String projectPath, String version, String path) {
        final String HOST_GIT_TREEINFO = Global.HOST_API + "%s/git/commits/%s%s";
        String filePath = encode2Pager(path);
        return String.format(HOST_GIT_TREEINFO,
                projectPath, version, filePath);
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
    public static String pathEncode2(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String dest = Global.encodeUtf8("/" + path);
        dest = Global.encodeUtf8(dest);
        return dest;
    }


    // 重编码2次，git服务器那边要求的
    public static String pathEncode2NoSplite(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        String dest = Global.encodeUtf8(path);
        dest = Global.encodeUtf8(dest);
        return dest;
    }
}
