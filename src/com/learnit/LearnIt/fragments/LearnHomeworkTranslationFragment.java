/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */

/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */


package com.learnit.LearnIt.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.activities.HomeworkActivity;
import com.learnit.LearnIt.controllers.LearnHomeworkTranslationController;
import com.learnit.LearnIt.data_types.ArticleWordId;
import com.learnit.LearnIt.interfaces.IWorkerJobInput;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.MyAnimationHelper;
import com.learnit.LearnIt.utils.StringUtils;
import com.learnit.LearnIt.views.WordButton;

import java.util.ArrayList;


public class LearnHomeworkTranslationFragment extends LearnFragment {
	public static final String TAG = "homework_frag";

	private int[] _btnIds = Constants.btnIdsTranslations;

	@Override
	protected int[] btnIds() {
		return _btnIds;
	}

	public LearnHomeworkTranslationFragment(IWorkerJobInput worker) {
		super();
		_listener = new LearnHomeworkTranslationController(this, worker, btnIds());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!restoreFromPreferences()) {
			_listener.showNext();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		saveToPreferences();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.homework, container, false);
		updateDirectionOfTranslation();
		_wordToAsk = (TextView) v.findViewById(R.id.word_to_ask);
		_wordToAsk.setMovementMethod(new ScrollingMovementMethod());
		for (int id: _btnIds) {
			(v.findViewById(id)).setOnClickListener(_listener);
		}
		setAll(View.INVISIBLE);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle extras = getArguments();
		if (_listener instanceof LearnHomeworkTranslationController) {
			((LearnHomeworkTranslationController) _listener).getEverythingFromExtras(extras, this.getActivity());
		}
	}

	@Override
	public void openButtons() {
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] viewsBottom = {
				v.findViewById(R.id.second_button_layout),
				v.findViewById(R.id.right_bottom_button),
				v.findViewById(R.id.left_bottom_button)};
		View[] viewsTop = {
				v.findViewById(R.id.first_button_layout),
				v.findViewById(R.id.left_top_button),
				v.findViewById(R.id.right_top_button)};
		animationHelper.invokeForAllViews(viewsBottom, R.anim.float_in_up_first_row, _listener);
		animationHelper.invokeForAllViews(viewsTop, R.anim.float_in_up_second_row, _listener);
	}

	@Override
	public void closeButtons() {
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] viewsBottom = {
				v.findViewById(R.id.second_button_layout),
				v.findViewById(R.id.right_bottom_button),
				v.findViewById(R.id.left_bottom_button)};
		View[] viewsTop = {
				v.findViewById(R.id.first_button_layout),
				v.findViewById(R.id.left_top_button),
				v.findViewById(R.id.right_top_button)};
		animationHelper.invokeForAllViews(viewsBottom, R.anim.float_away_down_first_row, _listener);
		animationHelper.invokeForAllViews(viewsTop, R.anim.float_away_down_second_row, _listener);
	}

	@Override
	public void setButtonTexts(ArrayList<ArticleWordId> words, int direction) {
		if (direction > 0) { _direction = direction; }
		int showOnButton;
		switch (_direction) {
			case Constants.FROM_FOREIGN_TO_MY:
				showOnButton = WordButton.SHOW_TRANSLATION;
				break;
			case Constants.FROM_MY_TO_FOREIGN:
				showOnButton = WordButton.SHOW_WORD;
				break;
			default:
				showOnButton = 0; //won't show anything on button
		}

		// actually set the texts to the buttons
		for (int i = 0; i < btnIds().length; ++i) {
			WordButton tempButton = (WordButton) v.findViewById(btnIds()[i]);
			if (words != null && i < words.size() && showOnButton > 0) {
				tempButton.setEnabled(true);
				tempButton.setText(words.get(i), showOnButton);
			} else {
				tempButton.setEnabled(false);
				tempButton.setText("");
			}
		}
	}

	@Override
	public void setQueryWordText(ArticleWordId struct, int direction) {
		TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
		queryWord = struct.word;
		if (direction > 0) { _direction = direction; }
		switch (_direction) {
			case Constants.FROM_FOREIGN_TO_MY:
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
				String learnLang = sp.getString(getString(R.string.key_language_from), "null");
				if (null != struct.article) {
					// a small hack for German and capitalization of all nouns
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

	public void stopActivity() {
		this.getActivity().finish();
	}

	public void askActivityToSwitchFragments(int homeworkType) {
		if (this.getActivity() instanceof HomeworkActivity) {
			((HomeworkActivity) this.getActivity()).replaceFragment(homeworkType);
		}
	}
}