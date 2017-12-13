package net.coding.program.common.model;

import java.io.Serializable;

/**
 * Created by chenchao on 2017/12/13.
 */

public interface ShareParam extends Serializable {

    RequestData getHttpShareLinkOn(ProjectObject projectObject);

    String getHttpShareLinkOff();

    boolean isShared();

    String getShareLink();

    void setShereLink(String link);
}
