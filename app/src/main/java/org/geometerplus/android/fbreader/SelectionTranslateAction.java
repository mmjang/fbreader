/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.fbreader.*;

import org.geometerplus.zlibrary.text.view.ZLTextElement;
import org.geometerplus.zlibrary.text.view.ZLTextWord;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

public class SelectionTranslateAction extends FBAndroidAction {
	SelectionTranslateAction(FBReader baseActivity, FBReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	static final private String INTENT_ANKIHELPER_TARGET_WORD = "com.mmjang.ankihelper.target_word";
	static final private String INTENT_ANKIHELPER_URL = "com.mmjang.ankihelper.url";
	static final private String INTENT_ANKIHELPER_FBREADER_BOOKMARK_ID = "com.mmjang.ankihelper.fbreader.bookmark.id";
	static final private String INTENT_ANKIHELPER_NOTE = "com.mmjang.ankihelper.note";

	@Override
	protected void run(Object ... params) {
		final FBView fbview = Reader.getTextView();
		boolean isAnkiHelper = isAppInstalled(BaseActivity, "com.mmjang.ankihelper");
		boolean isQuizHelper = isAppInstalled(BaseActivity, "com.mmjang.quizhelper");
		//Bookmark bookmark = Reader.addInvisibleSelectionBookmark();
		//long bookMarkId = Reader.Collection.saveBookmarkAndReturnId(bookmark);
		String title = Reader.getCurrentBook().getTitle();
		String progress = Math.round(100 * Reader.getCurrentBook().getProgress().toFloat()) + "%";
		String noteHtml = "from <i>" + title + "</i> - " + progress;

		//hijack this method to call ankihelper
		int count = Reader.getTextView().getCountOfSelectedWords();
		//if the selection is longer than 2 words, don't search for sentence;
		if(count > 2){
			String sentence = Reader.getTextView().getSelectedSnippet().getText();
			Intent intent = new Intent();
			if(isAnkiHelper) {
				intent.setClassName("com.mmjang.ankihelper", "com.mmjang.ankihelper.ui.popup.PopupActivity");
			}
			if(isQuizHelper){
				intent.setClassName("com.mmjang.quizhelper", "com.mmjang.quizhelper.ui.popup.PopupActivity");
			}
			if(!isAnkiHelper && !isQuizHelper){
				return;
			}
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, sentence);
			intent.putExtra(INTENT_ANKIHELPER_NOTE, noteHtml);
			intent.setType("text/plain");
			BaseActivity.startActivity(intent);
			fbview.clearSelection();
			return;
		}
		//int paraIndex = Reader.BookTextView.getSelectionStartPosition().getParagraphIndex();
		//int eleIndex = Reader.BookTextView.getSelectionStartPosition().getElementIndex();
		final ZLTextWordCursor cur = new ZLTextWordCursor(Reader.getTextView().getStartCursor());
		//cur.moveToParagraph(paraIndex);
		cur.moveTo(Reader.getTextView().getSelectionStartPosition());
		ZLTextElement targetEle =  cur.getElement();
//		cur.moveToParagraphStart();
		StringBuilder sb = new StringBuilder();
		sb.append(targetEle.toString());
		while(!cur.isStartOfParagraph()){
			cur.previousWord();
			ZLTextElement element = cur.getElement();
			if(element instanceof ZLTextWord){
				String word = ((ZLTextWord) element).getString();
				if(word.matches(".*[.?;!]")){
					break;
				}else{
					sb.insert(0, " ");
					sb.insert(0, word);
				}
			}
		}
		cur.moveTo(Reader.BookTextView.getSelectionStartPosition());
		while(!cur.isEndOfParagraph()){
			cur.nextWord();
			ZLTextElement element = cur.getElement();
			if(element instanceof ZLTextWord){
				String word = ((ZLTextWord) element).getString();
				if(word.matches(".*[.?;!]")){
					sb.append(" ");
					sb.append(word);
					break;
				}else{
					sb.append(" ");
					sb.append(word);
				}
			}
		}
//		while(!cur.isEndOfParagraph()){
//			ZLTextElement element = cur.getElement();
//			if(element instanceof ZLTextWord){
//				if(element == targetEle){
//					sb.append("****" + element.toString() + "****");
//				}else{
//					sb.append(element.toString());
//				}
//				sb.append(" ");
//			}
//			cur.nextWord();
//		}
		String text = sb.toString();
		String url = "";
		String target = targetEle.toString();
		target = target.replaceAll("[?!.,;\"]", "");
		target = target.replaceAll("'s$", "");
		target = target.replaceAll("'$","");
		Intent intent = new Intent();
		if(isAnkiHelper) {
			intent.setClassName("com.mmjang.ankihelper", "com.mmjang.ankihelper.ui.popup.PopupActivity");
		}
		if(isQuizHelper){
			intent.setClassName("com.mmjang.quizhelper", "com.mmjang.quizhelper.ui.popup.PopupActivity");
		}
		if(!isAnkiHelper && !isQuizHelper){
			return;
		}		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		if(!target.isEmpty()) {
			intent.putExtra(INTENT_ANKIHELPER_TARGET_WORD, target);
		}
		if(!url.isEmpty()) {
			intent.putExtra(INTENT_ANKIHELPER_URL, url);
		}
		intent.putExtra(INTENT_ANKIHELPER_NOTE, noteHtml);
		intent.setType("text/plain");
		BaseActivity.startActivity(intent);
		fbview.clearSelection();
		return;
		//
//		final DictionaryHighlighting dictionaryHilite = DictionaryHighlighting.get(fbview);
//		final TextSnippet snippet = fbview.getSelectedSnippet();
//
//		if (dictionaryHilite == null || snippet == null) {
//			return;
//		}
//
//		DictionaryUtil.openTextInDictionary(
//			BaseActivity,
//			snippet.getText(),
//			fbview.getCountOfSelectedWords() == 1,
//			fbview.getSelectionStartY(),
//			fbview.getSelectionEndY(),
//			new Runnable() {
//				public void run() {
//					fbview.addHighlighting(dictionaryHilite);
//					Reader.getViewWidget().repaint();
//				}
//			}
//		);
//		fbview.clearSelection();
	}

	public static boolean isAppInstalled(Context context, String packageName) {
		try {
			context.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
	}
}
