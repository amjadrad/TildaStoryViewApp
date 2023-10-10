package ir.tildaweb.tildastoryview.callback;

import ir.tildaweb.tildastoryview.model.MyStory;

public interface StoryCallbacks {

    void startStories();

    void pauseStories();

    void resumeStories();

    void waitForLoad();

    void mediaIsLoaded();

    void nextStory();

    void onDescriptionClickListener(int position);

    void onActionButtonClickListener(MyStory story);

}
