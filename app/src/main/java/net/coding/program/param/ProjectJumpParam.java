package net.coding.program.param;

import net.coding.program.common.GlobalData;
import net.coding.program.common.model.ProjectObject;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenchao on 2017/4/17.
 * 携带了 project 的 创建者 和 项目名
 */
public class ProjectJumpParam implements Serializable {
    public String project = "";
    public String user = "";

    public ProjectJumpParam(String user, String project) {
        this.user = user;
        this.project = project;
    }

    public ProjectJumpParam(String path) {
        path = GlobalData.transformEnterpriseUri(path);
        String[] regexs = new String[]{
                "^/u/(.*?)/p/(.*?)(?:/git)?$",
                "^/t/(.*?)/p/(.*?)(?:/git)?$",
                "^/(?:u|user|team)/(.*)/(?:project|p)/(.*)$"
        };
        for (String item : regexs) {
            if (isMatch(path, item)) {
                return;
            }
        }
    }

    public ProjectJumpParam(ProjectObject projectObject) {
        project = projectObject.name;
        user = projectObject.owner_user_name;
    }

    private boolean isMatch(String path, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            this.user = matcher.group(1);
            this.project = matcher.group(2);
            return true;
        }

        return false;
    }

    public String toPath() {
        return String.format("/user/%s/project/%s", user, project);
    }
}
