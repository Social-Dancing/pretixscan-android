/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.socialdancing.sdscan.droid.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import eu.socialdancing.sdscan.droid.R;


/**
 * A transition that morphs a rectangle into a circle, changing it's background color.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MorphDialogToView extends ChangeBounds {

    private static final String PROPERTY_COLOR = "rectMorph:color";
    private static final String PROPERTY_CORNER_RADIUS = "rectMorph:cornerRadius";
    private static final String[] TRANSITION_PROPERTIES = {
            PROPERTY_COLOR,
            PROPERTY_CORNER_RADIUS
    };
    private int backgroundColorStart;
    private int backgroundColorEnd;
    private int duration;

    public MorphDialogToView(int backgroundColorStart, int backgroundColorEnd, int duration) {
        super();
        this.backgroundColorStart = backgroundColorStart;
        this.backgroundColorEnd = backgroundColorEnd;
        this.duration = duration;
    }

    public MorphDialogToView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPERTY_COLOR, backgroundColorStart);
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, view.getResources()
                .getDimensionPixelSize(R.dimen.default_dialog_corners));
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPERTY_COLOR, backgroundColorEnd);
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, view.getResources().getDimensionPixelSize(
                R.dimen.default_card_corners));
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
        if (startValues == null || endValues == null || changeBounds == null) {
            return null;
        }

        Integer startColor = (Integer) startValues.values.get(PROPERTY_COLOR);
        Integer startCornerRadius = (Integer) startValues.values.get(PROPERTY_CORNER_RADIUS);
        Integer endColor = (Integer) endValues.values.get(PROPERTY_COLOR);
        Integer endCornerRadius = (Integer) endValues.values.get(PROPERTY_CORNER_RADIUS);

        if (startColor == null || startCornerRadius == null || endColor == null || endCornerRadius == null) {
            return null;
        }

        MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
        endValues.view.setBackground(background);

        Animator color = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, endColor);
        Animator corners = ObjectAnimator.ofFloat(background, MorphDrawable.CORNER_RADIUS, endCornerRadius);

        // hide child views (offset down & fade out)
        if (endValues.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) endValues.view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.animate().alpha(0f).translationY(v.getHeight() / 3).setStartDelay(0L)
                        .setDuration(duration / 6)
                        .setInterpolator(AnimationUtils.loadInterpolator(vg.getContext(), android.R.interpolator.fast_out_linear_in))
                        .start();
            }
        }

        AnimatorSet transition = new AnimatorSet();

        View root = sceneRoot.findViewById(R.id.root);
        if (root != null && root.getBackground() instanceof ColorDrawable) {
            MorphDrawable rootbg = new MorphDrawable(((ColorDrawable) root.getBackground()).getColor(), 0);
            root.setBackground(rootbg);
            Animator rootColor = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, Color.TRANSPARENT);
            transition.playTogether(rootColor, changeBounds, corners, color);
        } else {
            transition.playTogether(changeBounds, corners, color);
        }

        transition.setInterpolator(AnimationUtils.loadInterpolator(sceneRoot.getContext(), android.R.interpolator.fast_out_slow_in));
        transition.setDuration(duration);
        return transition;
    }
}