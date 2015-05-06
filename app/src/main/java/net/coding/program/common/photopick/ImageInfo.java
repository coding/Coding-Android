package net.coding.program.common.photopick;

import java.io.Serializable;

/**
 * Created by chenchao on 15/5/6.
 */
public class ImageInfo implements Serializable {
    public String path;
    public long photoId;
    public int width;
    public int height;

    public ImageInfo(String path) {
        this.path = path;
    }

    public static String pathAddPreFix(String path) {
        final String prefix = "file://";
        if (!path.startsWith(prefix)) {
            path = prefix + path;
        }
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageInfo imageInfo = (ImageInfo) o;

        if (photoId != imageInfo.photoId) return false;
        if (width != imageInfo.width) return false;
        if (height != imageInfo.height) return false;
        return !(path != null ? !path.equals(imageInfo.path) : imageInfo.path != null);

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (int) (photoId ^ (photoId >>> 32));
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
}
