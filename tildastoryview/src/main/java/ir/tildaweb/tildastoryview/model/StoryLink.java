package ir.tildaweb.tildastoryview.model;

public class StoryLink {
    private String link;
    private MyStory.StoryLinkType type = MyStory.StoryLinkType.PRIMARY;
    private String title;

    public StoryLink(String link, String title) {
        this.link = link;
        this.title = title;
    }

    public StoryLink(String link, MyStory.StoryLinkType type, String title) {
        this.link = link;
        this.type = type;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public MyStory.StoryLinkType getType() {
        return type;
    }

    public void setType(MyStory.StoryLinkType type) {
        this.type = type;
    }
}

