/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */

package com.learnit.LearnIt.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.data_types.MyTextWatcher;
import com.learnit.LearnIt.interfaces.FragmentUiInterface;
import com.learnit.LearnIt.listeners.MyButtonOnClickListener;
import com.learnit.LearnIt.listeners.MyOnFocusChangeListener;
import com.learnit.LearnIt.listeners.MyOnListItemClickListener;
import com.learnit.LearnIt.listeners.MyOnListItemLongClickListener;

import java.util.List;
import java.util.Map;

public class DictFragment extends MySmartFragment implements FragmentUiInterface<Map<String,String>>{
    private EditText _edtWord;
    private ImageButton _btnClear;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
	    _view = inflater.inflate(R.layout.dict_fragment, container, false);

        _edtWord = (EditText) _view.findViewById(R.id.edv_search_word);
        _edtWord.clearFocus();
        _edtWord.addTextChangedListener(new MyTextWatcher(_edtWord, _callback, this.getId()));
	    _edtWord.setOnFocusChangeListener(new MyOnFocusChangeListener(_callback, this.getId()));
        _btnClear = (ImageButton) _view.findViewById(R.id.btn_search_clear);
        _btnClear.setOnClickListener(new MyButtonOnClickListener(_callback, this.getId()));
        _btnClear.setVisibility(View.INVISIBLE);
        final ListView listView = (ListView) _view.findViewById(R.id.list_of_words);
        listView.setOnItemLongClickListener(new MyOnListItemLongClickListener(_callback, this.getId()));
        listView.setOnItemClickListener(new MyOnListItemClickListener(_callback, this.getId()));
	    return _view;
    }

	@Override
	public void setViewFocused(int id) {

	}

	@Override
	public void setViewText(int id, String text) {
		TextView edit = (TextView) _view.findViewById(id);
		edit.setText(text);
	}

	@Override
	public void addTextToView(int id, String text) {

	}

	@Override
	public void setListEntries(List<Map<String,String>> words, int id) {
		if (id != R.id.list_of_words)
			return;
		SimpleAdapter adapter;
		if (words==null)
		{
			((ListView) this.getView().findViewById(R.id.list_of_words))
					.setAdapter(null);
			return;
		}
		adapter = new SimpleAdapter(this.getActivity(), words,
				android.R.layout.simple_list_item_2,
				new String[]{"word", "translation"},
				new int[]{android.R.id.text1, android.R.id.text2});
		((ListView) this.getView().findViewById(R.id.list_of_words))
				.setAdapter(adapter);
	}

	@Override
	public void setViewVisibility(int id, int visibility) {
		_btnClear.setVisibility(visibility);
	}

	@Override
	public Integer getFocusedId() {
		return null;
	}

	@Override
	public String getTextFromView(int id) {
		return _edtWord.getText().toString();
	}
}