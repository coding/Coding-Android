package net.coding.program.project.detail;

import net.coding.program.common.Global;

import java.io.Serializable;

/**
 * Created by Neutra on 2015/4/23.
 */
public class TopicLabelApi implements Serializable {

    private String user, project;
    public TopicLabelApi(String user, String project) {
        this.user = user;
        this.project = project;
    }

    public void getLabels(){
        String url = String.format("%s/user/%s/project/%s/topics/labels", Global.HOST, user, project);
    }

    public void addLabel(String label){
        String url = String.format("%s/user/%s/project/%s/topics/label", Global.HOST, user, project);
    }

    public void removeLabel(int labelId){
        String url = String.format("%s/user/%s/project/%s/topics/label/%s", Global.HOST, user, project, labelId);
    }

    public void renameLabel(int labelId, String newName){
        String url = String.format("%s/user/%s/project/%s/topics/label/%s", Global.HOST, user, project, labelId);
    }

    public void getLabels(int topicId){
        String url = String.format("%s/user/%s/project/%s/topics/%s/label", Global.HOST, user, project, topicId);
    }

    public void addLabel(int topicId, int labelId){
        String url = String.format("%s/user/%s/project/%s/topics/%s/label/%s", Global.HOST, user, project, topicId,labelId);
    }

    public void removeLabel(int topicId, int labelId){
        String url = String.format("%s/user/%s/project/%s/topics/%s/label/%s", Global.HOST, user, project,topicId, labelId);
    }
}
