
/*
 * Copyright (C) 2014  Igor Bogoslavskyi
 * This file is part of LearnIt.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.learnit.LearnIt.fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.data_types.ArticleWordId;
import com.learnit.LearnIt.data_types.DBHelper;
import com.learnit.LearnIt.data_types.FactoryDbHelper;
import com.learnit.LearnIt.interfaces.ILearnFragmentUpdate;
import com.learnit.LearnIt.interfaces.IListenerLearn;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.MyAnimationHelper;
import com.learnit.LearnIt.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

public abstract class LearnFragment
		extends MySmartFragment
		implements ILearnFragmentUpdate {
	public static final String TAG = "learn_words_frag";
	public static final String BUTTON_PREFIX_TAG = "button_";
	public static final String WORD_TAG = "query_word";
	public static final String CORRECT_INDEX_TAG = "correct_idx";
	final String LOG_TAG = "my_logs";


	protected View v;
    protected String queryWord = null;
    protected int _direction = 0;
	protected IListenerLearn _listener;
	protected TextView _wordToAsk;

	protected abstract int[] btnIds();

	public LearnFragment() {
		super();
	}

    public void setAll(int visibilityState)
    {
	    for (int id: btnIds()) {
		    v.findViewById(id).setVisibility(visibilityState);
	    }
	    v.findViewById(R.id.word_to_ask).setVisibility(visibilityState);
    }

    @SuppressLint("NewApi")
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!this.isAdded()) { return; }
        if (null!=v) {
            if (isVisibleToUser) {
                Utils.hideSoftKeyboard(this.getActivity());
            }
        }
    }

	@Override
	public void setQueryWordTextFail() {
		_wordToAsk.setText(R.string.learn_no_words);
	}

	@Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState);

	@Override
	public void updateDirectionOfTranslation() {
		Random random = new Random();
        if (!this.isAdded()) { return; }
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		String strDirection = sp.getString(getString(R.string.key_direction_of_trans), null);
		if (null != strDirection) {
			_direction = Integer.parseInt(strDirection);
			if (_direction == Constants.MIXED) {
				_direction = random.nextInt(2) + 1;
			}
		}
	}

	@Override
    public void closeWord() {
        if (!this.isAdded()) { return; }
        MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
	    animationHelper.invokeForView(queryWordTextView, R.anim.close_word, _listener);
    }

	@Override
    public void openWord() {
        if (!this.isAdded()) { return; }
	    MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
	    animationHelper.invokeForView(queryWordTextView, R.anim.open_word, _listener);
    }

	@Override
	public void shakeView(View v) {
        if (!this.isAdded()) { return; }
        MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		animationHelper.invokeForView(v, R.anim.shake, _listener);
	}

    public abstract void openButtons();
    public abstract void closeButtons();
	public abstract void setButtonTexts(ArrayList<ArticleWordId> words, int direction);
	public abstract void setQueryWordText(ArticleWordId struct, int direction);


	public void updateWordWeight(int numOfWrongAnswers) {
        Log.d(LOG_TAG, "word to be updated " + queryWord);
		if (queryWord == null) return;
	    DBHelper dbHelper = FactoryDbHelper.createDbHelper(this.getActivity(), DBHelper.DB_WORDS);
        switch (numOfWrongAnswers) {
            case 0:
                dbHelper.updateWordWeight(queryWord.toLowerCase(), DBHelper.WEIGHT_CORRECT);
                break;
            case 1:
                dbHelper.updateWordWeight(queryWord.toLowerCase(), DBHelper.WEIGHT_ONE_WRONG);
                break;
            case 2:
                dbHelper.updateWordWeight(queryWord.toLowerCase(), DBHelper.WEIGHT_TWO_WRONG);
                break;
            case 3:
                dbHelper.updateWordWeight(queryWord.toLowerCase(), DBHelper.WEIGHT_THREE_WRONG);
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (_wordToAsk != null && _wordToAsk.getText() != null) {
            outState.putString(WORD_TAG, _wordToAsk.getText().toString());
        }
        outState.putInt(CORRECT_INDEX_TAG, _listener.getCorrectWordId());
        // add all the button texts to shared preferences
        TextView currentButtonText;
        for (int i = 0; i < btnIds().length; ++i) {
            currentButtonText = ((TextView) v.findViewById(btnIds()[i]));
            if (currentButtonText == null
                    || currentButtonText.getText() == null
                    || currentButtonText.getText().toString().isEmpty()) { continue; }
            outState.putString(BUTTON_PREFIX_TAG + i, currentButtonText.getText().toString());
        }
    }

    protected void saveToPreferences() {
        if (!this.isAdded()) { return; }
        boolean problemOccured = false;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		SharedPreferences.Editor editor = sp.edit();
		// add word text to shared preferences
		if (_wordToAsk == null || _wordToAsk.getText() == null) { problemOccured = true; }
		editor.putString(WORD_TAG, _wordToAsk.getText().toString());

		// add correct id to shared preferences
		editor.putInt(CORRECT_INDEX_TAG, _listener.getCorrectWordId());

		// add all the button texts to shared preferences
		TextView currentButtonText;
		for (int i = 0; i < btnIds().length; ++i) {
			currentButtonText = ((TextView) v.findViewById(btnIds()[i]));
			if (currentButtonText == null
					|| currentButtonText.getText() == null
					|| currentButtonText.getText().toString().isEmpty()) { problemOccured = true; }
			editor.putString(BUTTON_PREFIX_TAG + i, currentButtonText.getText().toString());
		}

		// commit the transaction
		editor.commit();
		if (problemOccured) {
			Utils.removeOldSavedValues(sp, Constants.btnIdsTranslations);
		}
	}


	protected boolean restoreFromPreferences() {
        if (!this.isAdded()) { return false; }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		String word = sp.getString(WORD_TAG, null);
		if (word == null) { return false; }
		((TextView) v.findViewById(R.id.word_to_ask)).setText(word);
		String[] split = word.split("\n");
		queryWord = split[split.length - 1];

		int correctId = sp.getInt(CORRECT_INDEX_TAG, 0);
		if (correctId == 0) { return false; }
		_listener.setCorrectWordIdFromPrefs(correctId);

		String buttonText;
		for (int i = 0; i < btnIds().length; ++i) {
			buttonText = sp.getString(BUTTON_PREFIX_TAG + i, null);
			if (buttonText == null || buttonText.isEmpty()) {
				((TextView) v.findViewById(btnIds()[i])).setText("");
				(v.findViewById(btnIds()[i])).setEnabled(false);
			} else {
				((TextView) v.findViewById(btnIds()[i])).setText(buttonText);
			}
		}
		Utils.removeOldSavedValues(sp, btnIds());
		setAll(View.VISIBLE);
		return true;
	}
}