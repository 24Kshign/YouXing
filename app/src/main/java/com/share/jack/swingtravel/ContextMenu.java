
package com.share.jack.swingtravel;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.easemob.chat.EMMessage;

public class ContextMenu extends BaseActivity {

	private int position;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int type = getIntent().getIntExtra("type", -1);
		if (type == EMMessage.Type.TXT.ordinal()) {
		    setContentView(R.layout.context_menu_for_text);
		} else if (type == EMMessage.Type.IMAGE.ordinal()) {
		    setContentView(R.layout.context_menu_for_image);
		} else if (type == EMMessage.Type.VOICE.ordinal()) {
		    setContentView(R.layout.context_menu_for_voice);
		}
		position = getIntent().getIntExtra("position", -1);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	public void copy(View view){
		setResult(ChatActivity.RESULT_CODE_COPY, new Intent().putExtra("position", position));
		finish();
	}
	public void delete(View view){
		setResult(ChatActivity.RESULT_CODE_DELETE, new Intent().putExtra("position", position));
		finish();
	}
	public void forward(View view){
		setResult(ChatActivity.RESULT_CODE_FORWARD, new Intent().putExtra("position", position));
		finish();
	}
	
	public void open(View v){
	    setResult(ChatActivity.RESULT_CODE_OPEN, new Intent().putExtra("position", position));
        finish();
	}
	
}
