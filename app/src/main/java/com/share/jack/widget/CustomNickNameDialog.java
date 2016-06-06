package com.share.jack.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.share.jack.swingtravel.R;


/**
 * Created by Jack on 2015/9/5.
 * 自定义修改昵称的dialog
 */
public class CustomNickNameDialog extends Dialog {

    private OnCustomDialogListener onCustomDialogListener;
    private Button btn_cancel;
    private Button btn_confirm;
    private EditText et_name;

    //定义回调事件，用于dialog的点击事件
    public interface OnCustomDialogListener {
        public void setEt(EditText et_name);
        public void back(String name);
    }

    public CustomNickNameDialog(Context context, OnCustomDialogListener customDialogListener) {
        super(context);
        this.onCustomDialogListener = customDialogListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nickname);

        initeView();
    }

    private void initeView() {
        btn_cancel = (Button) findViewById(R.id.dn_btn_cancel);
        btn_confirm = (Button) findViewById(R.id.dn_btn_confirm);
        et_name = (EditText) findViewById(R.id.dn_et_nickname);

        onCustomDialogListener.setEt(et_name);
        btn_cancel.setOnClickListener(clickListener);
        btn_confirm.setOnClickListener(clickListener);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dn_btn_cancel:
                    CustomNickNameDialog.this.dismiss();
                    break;
                case R.id.dn_btn_confirm:
                    onCustomDialogListener.back(String.valueOf(et_name.getText()));
                    CustomNickNameDialog.this.dismiss();
                    break;
            }
        }
    };
}
