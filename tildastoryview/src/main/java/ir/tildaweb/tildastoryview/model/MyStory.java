package ir.tildaweb.tildastoryview.model;

import java.io.Serializable;
import java.util.Date;

public class MyStory implements Serializable {

    enum StoryLinkType {
        DANGER,
        SUCCESS,
        INFO,
        LIGHT,
        VLIGHT,
        WARNING,
        PRIMARY
    }

    private String url;
    private Date date;
    private String description;
    private StoryLink storyLink;


    public MyStory(String url, Date date, String description) {
        this.url = url;
        this.date = date;
        this.description = description;
    }

    public MyStory(String url, Date date) {
        this.url = url;
        this.date = date;
    }

    public StoryLink getStoryLink() {
        return storyLink;
    }

    public void setStoryLink(StoryLink storyLink) {
        this.storyLink = storyLink;
    }

    public MyStory(String url) {
        this.url = url;
    }

    public MyStory() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
