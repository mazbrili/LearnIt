/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */

/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */


package com.learnit.LearnIt.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.data_types.ArticleWordId;
import com.learnit.LearnIt.data_types.DBHelper;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.MyAnimationHelper;
import com.learnit.LearnIt.utils.StringUtils;
import com.learnit.LearnIt.utils.Utils;
import com.learnit.LearnIt.views.WordButton;

import java.util.ArrayList;
import java.util.Random;

public class LearnFragment extends MySmartFragment
		implements MyAnimationHelper.OnAnimationActionListener {

    View v;
    String queryWord = null;
    int numOfWrongAnswers = 0;
    int direction = 0;
    final String LOG_TAG = "my_logs";
    DBHelper dbHelper;
    int[] btnIds = {R.id.left_top_button,
            R.id.right_top_button,
            R.id.left_bottom_button,
            R.id.right_bottom_button};



	@Override
    public void onResume() {
        super.onResume();
        Pair<String, String> langPair = Utils.getCurrentLanguages(this.getActivity());
        Log.d(LOG_TAG, "onResume learn fragment: from - " + langPair.first + " to " + langPair.second);
        if (null!=v)
        {
            openWord();
            openButtons();
        }
    }

    private void setAll(int visibilityState)
    {
        v.findViewById(R.id.left_top_button).setVisibility(visibilityState);
        v.findViewById(R.id.right_bottom_button).setVisibility(visibilityState);
        v.findViewById(R.id.left_bottom_button).setVisibility(visibilityState);
        v.findViewById(R.id.right_top_button).setVisibility(visibilityState);
//        v.findViewById(R.id.word_to_ask).setVisibility(visibilityState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (null!=v)
        {
            if (isVisibleToUser)
            {
                if (View.INVISIBLE == v.findViewById(R.id.word_to_ask).getVisibility())
                {
                    openWord();
                    openButtons();
//                    setAll(View.VISIBLE);
                }
                Utils.hideSoftKeyboard(this.getActivity());
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.homework, container, false);
	    TextView textView = (TextView) v.findViewById(R.id.word_to_ask);
	    textView.setMovementMethod(new ScrollingMovementMethod());
        fetchNewWords();
//        setAll(View.INVISIBLE);
        return v;
    }

    private void fetchNewWords() {
        Random random = new Random();
        dbHelper = new DBHelper(this.getActivity(), DBHelper.DB_WORDS);
        Log.d(LOG_TAG, "DB+WORDS=" + DBHelper.DB_WORDS);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String strDirection = sp.getString(getString(R.string.key_direction_of_trans), null);
        if (null != strDirection) {
            direction = Integer.parseInt(strDirection);
            if (direction == Constants.MIXED) {
                direction = random.nextInt(2) + 1;
            }
        }
        int nouns = random.nextInt(2) + 1;
        ArrayList<ArticleWordId> words = dbHelper.getRandomWords(btnIds.length, null, nouns);
        // in case we are learning english and alike where there are no nouns
        // explicitly, we look another time
        if (words.size()==0)
        {
            nouns = (nouns+1)%3 +1;
            words = dbHelper.getRandomWords(btnIds.length, null, nouns);
        }
        int correctIdx = 0;
        if (words.size() == 0) {
            TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
            queryWordTextView.setText(getString(R.string.learn_no_words));
        } else {
            correctIdx = random.nextInt(words.size());
            setQueryWordTxt(words.get(correctIdx));
        }

        MyButtonOnClick myButtonOnClick = new MyButtonOnClick();
        myButtonOnClick.correct = btnIds[correctIdx];
        (v.findViewById(R.id.left_top_button))
                .setOnClickListener(myButtonOnClick);
        (v.findViewById(R.id.right_bottom_button))
                .setOnClickListener(myButtonOnClick);
        (v.findViewById(R.id.left_bottom_button))
                .setOnClickListener(myButtonOnClick);
        (v.findViewById(R.id.right_top_button))
                .setOnClickListener(myButtonOnClick);
        setBtnTexts(words);
    }

    private void setQueryWordTxt(ArticleWordId struct) {
        TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
        queryWord = struct.word;
        switch (direction) {
            case Constants.FROM_FOREIGN_TO_MY:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                String learnLang = sp.getString(getString(R.string.key_language_from), "null");
                if (null != struct.article) {
                    if ("de".equals(learnLang)) {
                        queryWordTextView.setText(struct.article + "\n" + StringUtils.capitalize(queryWord));
                    } else {
                        queryWordTextView.setText(struct.article + "\n" + queryWord);
                    }
                } else if (null != struct.prefix) {
                    queryWordTextView.setText(struct.prefix + " " + queryWord);
                } else {
                    queryWordTextView.setText(queryWord);
                }
                break;
            case Constants.FROM_MY_TO_FOREIGN:
                queryWordTextView.setText(StringUtils.splitOnRegex(struct.translation, ",|\\s"));
                break;
        }

    }

    private void setBtnTexts(ArrayList<ArticleWordId> words) {
        int showOnButton;
        switch (direction) {
            case Constants.FROM_FOREIGN_TO_MY:
                showOnButton = WordButton.SHOW_TRANSLATION;
                break;
            case Constants.FROM_MY_TO_FOREIGN:
                showOnButton = WordButton.SHOW_WORD;
                break;
            default:
                showOnButton = 0; //won't show anything on button
        }
        for (int i = 0; i < 4; ++i) {
            WordButton tempButton = (WordButton) v.findViewById(btnIds[i]);
            if (i < words.size()) {
                tempButton.setEnabled(true);
                tempButton.setText(words.get(i), showOnButton);
            } else {
                tempButton.setEnabled(false);
                tempButton.setText("");
            }
        }


    }

    private void closeWord() {
	    MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
	    animationHelper.invokeForView(queryWordTextView, R.anim.close_word, this);
    }

    private void openWord() {
	    MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
	    animationHelper.invokeForView(queryWordTextView, R.anim.open_word, this);
    }

	private void shakeView(View v) {
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		animationHelper.invokeForView(v, R.anim.shake, this);
	}

    private void openButtons() {
	    MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    View[] viewsBottom = {
			    v.findViewById(R.id.second_button_layout),
			    v.findViewById(R.id.right_bottom_button),
			    v.findViewById(R.id.left_bottom_button)};
	    View[] viewsTop = {
			    v.findViewById(R.id.first_button_layout),
			    v.findViewById(R.id.left_top_button),
			    v.findViewById(R.id.right_top_button)};
	    animationHelper.invokeForAllViews(viewsBottom, R.anim.float_in_up_first_row, this);
	    animationHelper.invokeForAllViews(viewsTop, R.anim.float_in_up_second_row, this);
    }

    private void closeButtons() {
	    MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
	    View[] viewsBottom = {
			    v.findViewById(R.id.second_button_layout),
			    v.findViewById(R.id.right_bottom_button),
			    v.findViewById(R.id.left_bottom_button)};
	    View[] viewsTop = {
			    v.findViewById(R.id.first_button_layout),
			    v.findViewById(R.id.left_top_button),
			    v.findViewById(R.id.right_top_button)};
	    animationHelper.invokeForAllViews(viewsBottom, R.anim.float_away_down_first_row, this);
	    animationHelper.invokeForAllViews(viewsTop, R.anim.float_away_down_second_row, this);
    }

	@Override
	public void onAnimationFinished(int id, boolean ignore) {
		if (ignore)
			return;
		Log.d(LOG_TAG,"got animation id = "+id);
		switch (id)
		{
			case (R.anim.float_away_down_second_row):
				setAll(View.INVISIBLE);
				break;
			case (R.anim.close_word):
				fetchNewWords();
				setAll(View.VISIBLE);
				openButtons();
				openWord();
				break;
		}
	}

	private class MyButtonOnClick implements View.OnClickListener {
        public int correct = 0;

        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (correct == id) {
                updateWordWeight();
                numOfWrongAnswers = 0;
                closeWord();
                closeButtons();
            } else {
                numOfWrongAnswers++;
	            shakeView(v);
//                showDialogWrong();
            }
        }
    }

    private void updateWordWeight() {
        Log.d(LOG_TAG, "word to be updated " + queryWord);
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
}