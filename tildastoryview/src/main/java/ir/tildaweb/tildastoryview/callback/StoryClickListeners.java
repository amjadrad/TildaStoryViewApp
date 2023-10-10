package ir.tildaweb.tildastoryview.callback;

import ir.tildaweb.tildastoryview.model.MyStory;

public interface StoryClickListeners {

    void onDescriptionClickListener(int position);
    void onActionButtonClickListener(MyStory story);

    void onTitleIconClickListener(int position);

}
