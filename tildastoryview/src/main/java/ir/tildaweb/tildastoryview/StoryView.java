package ir.tildaweb.tildastoryview;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Calendar;

import ir.tildaweb.tildastoryview.callback.OnStoryChangedCallback;
import ir.tildaweb.tildastoryview.callback.StoryCallbacks;
import ir.tildaweb.tildastoryview.callback.StoryClickListeners;
import ir.tildaweb.tildastoryview.callback.TouchCallbacks;
import ir.tildaweb.tildastoryview.databinding.DialogStoriesBinding;
import ir.tildaweb.tildastoryview.model.MyStory;
import ir.tildaweb.tildastoryview.progress.StoriesProgressView;
import ir.tildaweb.tildastoryview.utils.PullDismissLayout;
import ir.tildaweb.tildastoryview.utils.StoryViewHeaderInfo;
import ir.tildaweb.tildastoryview.utils.ViewPagerAdapter;

import static ir.tildaweb.tildastoryview.utils.Utils.getDurationBetweenDates;

public class StoryView extends DialogFragment implements StoriesProgressView.StoriesListener,
        StoryCallbacks,
        PullDismissLayout.Listener,
        TouchCallbacks {

    private final String TAG = this.getClass().getName();
    private DialogStoriesBinding binding;

    private ArrayList<MyStory> storiesList = new ArrayList<>();

    private final static String IMAGES_KEY = "IMAGES";

    private long duration = 2000; //Default Duration

    private static final String DURATION_KEY = "DURATION";

    private static final String HEADER_INFO_KEY = "HEADER_INFO";

    private static final String STARTING_INDEX_TAG = "STARTING_INDEX";

    private static final String IS_RTL_TAG = "IS_RTL";


    private int counter = 0;

    private int startingIndex = 0;

    private boolean isHeadlessLogoMode = false;


    //Touch Events
    private boolean isDownClick = false;
    private long elapsedTime = 0;
    private Thread timerThread;
    private boolean isPaused = false;
    private int width, height;
    private float xValue = 0, yValue = 0;


    private StoryClickListeners storyClickListeners;
    private OnStoryChangedCallback onStoryChangedCallback;

    private boolean isRtl;

    private StoryView() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        binding = DialogStoriesBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        width = displaymetrics.widthPixels;
        height = displaymetrics.heightPixels;
        // Get field from view
        readArguments();
        setupViews(view);
        setupStories();

    }

    private void setupStories() {
        binding.progressView.setStoriesCount(storiesList.size());
        binding.progressView.setStoryDuration(duration);
        updateHeading();
        binding.viewPager.setAdapter(new ViewPagerAdapter(storiesList, getContext(), this));
    }

    private void readArguments() {
        assert getArguments() != null;
        storiesList = new ArrayList<>((ArrayList<MyStory>) getArguments().getSerializable(IMAGES_KEY));
        duration = getArguments().getLong(DURATION_KEY, 2000);
        startingIndex = getArguments().getInt(STARTING_INDEX_TAG, 0);
        isRtl = getArguments().getBoolean(IS_RTL_TAG, false);
    }

    private void setupViews(View view) {
        binding.pullDismissLayout.setListener(this);
        binding.pullDismissLayout.setmTouchCallbacks(this);
        binding.progressView.setStoriesListener(this);
        binding.viewPager.setOnTouchListener((v, event) -> true);
        binding.imageButtonClose.setOnClickListener(v -> dismissAllowingStateLoss());
        if (storyClickListeners != null) {
            binding.cardViewTitle.setOnClickListener(v -> storyClickListeners.onTitleIconClickListener(counter));
        }

        if (onStoryChangedCallback != null) {
            binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    onStoryChangedCallback.storyChanged(position);
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        if (isRtl) {
            binding.progressView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            binding.progressView.setRotation(180);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onNext() {
        binding.viewPager.setCurrentItem(++counter, false);
        updateHeading();
    }

    @Override
    public void onPrev() {
        if (counter <= 0) return;
        binding.viewPager.setCurrentItem(--counter, false);
        updateHeading();
    }

    @Override
    public void onComplete() {
        dismissAllowingStateLoss();
        if (onStoryChangedCallback != null) {
            onStoryChangedCallback.onStoriesFinish();
        }
    }

    @Override
    public void startStories() {
        counter = startingIndex;
        binding.progressView.startStories(startingIndex);
        binding.viewPager.setCurrentItem(startingIndex, false);
        updateHeading();
    }

    @Override
    public void pauseStories() {
        binding.progressView.pause();
    }

    private void previousStory() {
        if (counter - 1 < 0) return;
        binding.viewPager.setCurrentItem(--counter, false);
        binding.progressView.setStoriesCount(storiesList.size());
        binding.progressView.setStoryDuration(duration);
        binding.progressView.startStories(counter);
        updateHeading();
    }

    @Override
    public void nextStory() {
        if (counter + 1 >= storiesList.size()) {
            dismissAllowingStateLoss();
            return;
        }
        binding.viewPager.setCurrentItem(++counter, false);
        binding.progressView.startStories(counter);
        updateHeading();
    }

    @Override
    public void onDescriptionClickListener(int position) {
        if (storyClickListeners == null) return;
        storyClickListeners.onDescriptionClickListener(position);
    }

    @Override
    public void onDestroy() {
        timerThread = null;
        storiesList = null;
        binding.progressView.destroy();
        super.onDestroy();
    }

    private void updateHeading() {

        Object object = getArguments().getSerializable(HEADER_INFO_KEY);

        StoryViewHeaderInfo storyHeaderInfo = null;

        if (object instanceof StoryViewHeaderInfo) {
            storyHeaderInfo = (StoryViewHeaderInfo) object;
        } else if (object instanceof ArrayList) {
            storyHeaderInfo = ((ArrayList<StoryViewHeaderInfo>) object).get(counter);
        }

        if (storyHeaderInfo == null) return;

        if (storyHeaderInfo.getTitleIconUrl() != null) {
            binding.cardViewTitle.setVisibility(View.VISIBLE);
            if (getContext() == null) return;
            Glide.with(getContext())
                    .load(storyHeaderInfo.getTitleIconUrl())
                    .into(binding.imageViewTitle);
        } else {
            binding.cardViewTitle.setVisibility(View.GONE);
            isHeadlessLogoMode = true;
        }

        if (storyHeaderInfo.getTitle() != null) {
            binding.tvTitle.setVisibility(View.VISIBLE);
            binding.tvTitle.setText(storyHeaderInfo.getTitle());
        } else {
            binding.tvTitle.setVisibility(View.GONE);
        }

        if (storyHeaderInfo.getSubtitle() != null) {
            binding.tvSubTitle.setVisibility(View.VISIBLE);
            binding.tvSubTitle.setText(storyHeaderInfo.getSubtitle());
        } else {
            binding.tvSubTitle.setVisibility(View.GONE);
        }

        if (storiesList.get(counter).getDate() != null) {
            binding.tvTitle.setText(String.format("%s %s", binding.tvTitle.getText(),
                    getDurationBetweenDates(storiesList.get(counter).getDate(), Calendar.getInstance().getTime())));
        }

        if (isRtl) {
            binding.tvTitle.setGravity(Gravity.RIGHT);
            binding.tvSubTitle.setGravity(Gravity.RIGHT);
        } else {
            binding.tvTitle.setGravity(Gravity.LEFT);
            binding.tvSubTitle.setGravity(Gravity.LEFT);
        }
    }

    private void setHeadingVisibility(int visibility) {
        if (isHeadlessLogoMode && visibility == View.VISIBLE) {
            binding.tvTitle.setVisibility(View.GONE);
            binding.cardViewTitle.setVisibility(View.GONE);
            binding.tvSubTitle.setVisibility(View.GONE);
        } else {
            binding.tvTitle.setVisibility(visibility);
            binding.cardViewTitle.setVisibility(visibility);
            binding.tvSubTitle.setVisibility(visibility);
        }

        binding.imageButtonClose.setVisibility(visibility);
        binding.progressView.setVisibility(visibility);
    }


    private void createTimer() {
        timerThread = new Thread(() -> {
            while (isDownClick) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                elapsedTime += 100;
                if (elapsedTime >= 500 && !isPaused) {
                    isPaused = true;
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        binding.progressView.pause();
                        setHeadingVisibility(View.GONE);
                    });
                }
            }
            isPaused = false;
            if (getActivity() == null) return;
            if (elapsedTime < 500) return;
            getActivity().runOnUiThread(() -> {
                setHeadingVisibility(View.VISIBLE);
                binding.progressView.resume();
            });
        });
    }

    private void runTimer() {
        isDownClick = true;
        createTimer();
        timerThread.start();
    }

    private void stopTimer() {
        isDownClick = false;
    }

    @Override
    public void onDismissed() {
        dismissAllowingStateLoss();
    }

    @Override
    public boolean onShouldInterceptTouchEvent() {
        return false;
    }

    @Override
    public void touchPull() {
        elapsedTime = 0;
        stopTimer();
        binding.progressView.pause();
    }

    @Override
    public void touchDown(float xValue, float yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
        if (!isDownClick) {
            runTimer();
        }
    }

    @Override
    public void touchUp() {
        if (isDownClick && elapsedTime < 500) {
            stopTimer();
            if (((int) (height - yValue) <= 0.8 * height)) {
                if ((!TextUtils.isEmpty(storiesList.get(counter).getDescription())
                        && ((int) (height - yValue) >= 0.2 * height)
                        || TextUtils.isEmpty(storiesList.get(counter).getDescription()))) {
                    if ((int) xValue <= (width / 2)) {
                        //Left
                        if (isRtl) {
                            nextStory();
                        } else {
                            previousStory();
                        }
                    } else {
                        //Right
                        if (isRtl) {
                            previousStory();
                        } else {
                            nextStory();
                        }
                    }
                }
            }
        } else {
            stopTimer();
            setHeadingVisibility(View.VISIBLE);
            binding.progressView.resume();
        }
        elapsedTime = 0;
    }

    public void setStoryClickListeners(StoryClickListeners storyClickListeners) {
        this.storyClickListeners = storyClickListeners;
    }

    public void setOnStoryChangedCallback(OnStoryChangedCallback onStoryChangedCallback) {
        this.onStoryChangedCallback = onStoryChangedCallback;
    }

    public static class Builder {

        private final String TAG = this.getClass().getName();

        private StoryView storyView;
        private FragmentManager fragmentManager;
        private Bundle bundle;
        private StoryViewHeaderInfo storyViewHeaderInfo;
        private ArrayList<StoryViewHeaderInfo> headingInfoList;
        private StoryClickListeners storyClickListeners;
        private OnStoryChangedCallback onStoryChangedCallback;

        public Builder(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            this.bundle = new Bundle();
            this.storyViewHeaderInfo = new StoryViewHeaderInfo();
        }

        public Builder setStoriesList(ArrayList<MyStory> storiesList) {
            bundle.putSerializable(IMAGES_KEY, storiesList);
            return this;
        }

        public Builder setTitleText(String title) {
            storyViewHeaderInfo.setTitle(title);
            return this;
        }

        public Builder setSubtitleText(String subtitle) {
            storyViewHeaderInfo.setSubtitle(subtitle);
            return this;
        }

        public Builder setTitleLogoUrl(String url) {
            storyViewHeaderInfo.setTitleIconUrl(url);
            return this;
        }

        public Builder setStoryDuration(long duration) {
            bundle.putLong(DURATION_KEY, duration);
            return this;
        }

        public Builder setStartingIndex(int index) {
            bundle.putInt(STARTING_INDEX_TAG, index);
            return this;
        }

        public Builder build() {
            if (storyView != null) {
                Log.e(TAG, "The StoryView has already been built!");
                return this;
            }
            storyView = new StoryView();
            bundle.putSerializable(HEADER_INFO_KEY, headingInfoList != null ? headingInfoList : storyViewHeaderInfo);
            storyView.setArguments(bundle);
            if (storyClickListeners != null) {
                storyView.setStoryClickListeners(storyClickListeners);
            }
            if (onStoryChangedCallback != null) {
                storyView.setOnStoryChangedCallback(onStoryChangedCallback);
            }
            return this;
        }

        public Builder setOnStoryChangedCallback(OnStoryChangedCallback onStoryChangedCallback) {
            this.onStoryChangedCallback = onStoryChangedCallback;
            return this;
        }

        public Builder setRtl(boolean isRtl) {
            this.bundle.putBoolean(IS_RTL_TAG, isRtl);
            return this;
        }

        public Builder setHeadingInfoList(ArrayList<StoryViewHeaderInfo> headingInfoList) {
            this.headingInfoList = headingInfoList;
            return this;
        }

        public Builder setStoryClickListeners(StoryClickListeners storyClickListeners) {
            this.storyClickListeners = storyClickListeners;
            return this;
        }

        public void show() {
            storyView.show(fragmentManager, TAG);
        }

        public void dismiss() {
            storyView.dismiss();
        }

        public Fragment getFragment() {
            return storyView;
        }

    }

}
