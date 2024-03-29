package ir.tildaweb.tildastoryviewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import ir.tildaweb.tildastoryview.StoryView;
import ir.tildaweb.tildastoryview.callback.OnStoryChangedCallback;
import ir.tildaweb.tildastoryview.callback.StoryClickListeners;
import ir.tildaweb.tildastoryview.model.MyStory;
import ir.tildaweb.tildastoryview.model.StoryLink;
import ir.tildaweb.tildastoryviewapp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String file = "https://ipom.nazmenovin.com/upload/chats/file_16874734380721007106059_72039557.jpg";

        ArrayList<MyStory> myStories = new ArrayList<>();
        MyStory myStory = new MyStory(file);
        MyStory myStory3 = new MyStory(file);
        myStories.add(myStory);
        myStories.add(myStory3);
        myStories.add(myStory3);
        myStories.add(myStory3);

        new StoryView.Builder(getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText("عنوان")
                .setSubtitleText("username")
                .setRtl(true)
                .setTitleLogoUrl(file)//logo
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
                    }

                    @Override
                    public void onActionButtonClickListener(MyStory story) {
                        Log.d(TAG, "onActionButtonClickListener: " + story.getStoryLink().getLink());
                    }

                    @Override
                    public void onTitleIconClickListener(int position) {

                    }
                }).setOnStoryChangedCallback(new OnStoryChangedCallback() {
                    @Override
                    public void storyChanged(int position) {

                    }

                    @Override
                    public void onStoriesFinish() {
                        Toast.makeText(MainActivity.this, "finish", Toast.LENGTH_SHORT).show();
                    }
                })
                .build() // Must be called before calling show method
                .show();


    }
}