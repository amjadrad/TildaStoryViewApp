package ir.tildaweb.tildastoryview.utils;

import android.animation.Animator;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import ir.tildaweb.tildastoryview.R;
import ir.tildaweb.tildastoryview.callback.StoryCallbacks;
import ir.tildaweb.tildastoryview.databinding.LayoutStoryItemBinding;
import ir.tildaweb.tildastoryview.model.MyStory;

public class ViewPagerAdapter extends PagerAdapter {

    private final String TAG = this.getClass().getName();
    private final ArrayList<MyStory> images;
    private final Context context;
    private final StoryCallbacks storyCallbacks;
    private boolean storiesStarted = false;

    public ViewPagerAdapter(ArrayList<MyStory> images, Context context, StoryCallbacks storyCallbacks) {
        this.images = images;
        this.context = context;
        this.storyCallbacks = storyCallbacks;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, final int position) {

        storyCallbacks.waitForLoad();
        LayoutInflater inflater = LayoutInflater.from(context);
        MyStory currentStory = images.get(position);
        LayoutStoryItemBinding binding = LayoutStoryItemBinding.inflate(inflater, collection, false);
        binding.imageViewLoading.setAnimation(AnimationUtils.loadAnimation(context, R.anim.rotate));
        if (!TextUtils.isEmpty(currentStory.getDescription())) {
            binding.tvDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(currentStory.getDescription());
            binding.tvDescription.setOnClickListener(v -> storyCallbacks.onDescriptionClickListener(position));
        }
        if (currentStory.getStoryLink() != null) {
            binding.btnAction.setVisibility(View.VISIBLE);
            binding.btnAction.setText(currentStory.getStoryLink().getTitle());
            binding.btnAction.setOnClickListener(v -> {
                storyCallbacks.onActionButtonClickListener(currentStory);
            });
        }
        binding.relativeLayoutLoadingContainer.setVisibility(View.VISIBLE);
        Glide.with(context)
                .load(currentStory.getUrl())
                .listener(new RequestListener<>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        storyCallbacks.waitForLoad();
                        storyCallbacks.mediaIsLoaded();
                        binding.relativeLayoutLoadingContainer.setVisibility(View.GONE);
                        storyCallbacks.nextStory();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        storyCallbacks.waitForLoad();
                        storyCallbacks.mediaIsLoaded();
                        binding.relativeLayoutLoadingContainer.setVisibility(View.GONE);
                        if (resource != null) {
                            PaletteExtraction pe = new PaletteExtraction(binding.relativeLayout,
                                    ((BitmapDrawable) resource).getBitmap());
                            pe.execute();
                        }
                        if (!storiesStarted) {
                            storiesStarted = true;
                            storyCallbacks.startStories();
                        }
                        return false;
                    }
                })
                .into(binding.imageView);

        collection.addView(binding.getRoot());

        return binding.getRoot();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((View) object);
    }
}
