package net.coding.program.common.enter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import net.coding.program.Global;
import net.coding.program.R;
import net.coding.program.common.CustomDialog;
import net.coding.program.common.EmojiTranslate;

/**
 * Created by chaochen on 14-10-28.
 */
public class EnterLayout {

    private Activity mActivity;

    public TextView sendText;
    public ImageButton send;
    public EditText content;

    public enum Type {
        Default, TextOnly
    }

    private Type mType = Type.Default;

    public EnterLayout(Activity activity, View.OnClickListener sendTextOnClick, Type type) {
        mType = type;

        mActivity = activity;

        sendText = (TextView) activity.findViewById(R.id.sendText);
        sendText.setOnClickListener(sendTextOnClick);
        if (mType == Type.TextOnly) {
            sendText.setVisibility(View.VISIBLE);
        }

        send = (ImageButton) activity.findViewById(R.id.send);
        if (mType == Type.Default) {
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    AlertDialog dialog = builder.setTitle("")
                            .setItems(R.array.camera_gallery, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mActivity instanceof CameraAndPhoto) {
                                        CameraAndPhoto cameraAndPhoto = (CameraAndPhoto) mActivity;
                                        if (which == 0) {
                                            cameraAndPhoto.camera();
                                        } else {
                                            cameraAndPhoto.photo();
                                        }
                                    }
                                }
                            }).show();
                    CustomDialog.dialogTitleLineColor(mActivity, dialog);
                }
            });
        } else {
            send.setVisibility(View.GONE);
        }

        content = (EditText) activity.findViewById(R.id.comment);

        if (mType == Type.Default) {
            content.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        sendText.setVisibility(View.VISIBLE);
                        send.setVisibility(View.GONE);
                    } else {
                        sendText.setVisibility(View.GONE);
                        send.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    sendText.setBackgroundResource(R.drawable.edit_send_green);
                    sendText.setTextColor(0xffffffff);
                } else {
                    sendText.setBackgroundResource(R.drawable.edit_send);
                    sendText.setTextColor(0xff999999);
                }
            }
        });
        content.setText("");

        content.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1 || count == 2) {
                    String newString = s.subSequence(start, start + count).toString();
                    String imgName = EmojiTranslate.sEmojiMap.get(newString);
                    if (imgName != null) {
                        final String format = ":%s:";
                        String replaced = String.format(format, imgName);

                        Editable editable = content.getText();
                        editable.replace(start, start + count, replaced);
                        editable.setSpan(new EmojiconSpan(mActivity, imgName), start, start + replaced.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public EnterLayout(Activity activity, View.OnClickListener sendTextOnClick) {
        this(activity, sendTextOnClick, Type.Default);
    }

    public void hideKeyboard() {
        Global.popSoftkeyboard(mActivity, content, false);
    }

    public void popKeyboard() {
        content.requestFocus();
        Global.popSoftkeyboard(mActivity, content, true);
    }

    public void insertText(String s) {
        String enter = content.getText().toString() + s + " ";
        content.setText(enter);

        content.requestFocus();
        content.setSelection(enter.length());
    }

    public void insertEmoji(String s) {
        int insertPos = content.getSelectionStart();
        final String format = ":%s:";
        String replaced = String.format(format, s);

        Editable editable = content.getText();
        editable.insert(insertPos, String.format(format, s));
        editable.setSpan(new EmojiconSpan(mActivity, s), insertPos, insertPos + replaced.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void deleteOneChar() {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        content.dispatchKeyEvent(event);
    }

    public void clearContent() {
        content.setText("");
    }

    public String getContent() {
        return content.getText().toString();
    }

    public void hide() {
        View root = mActivity.findViewById(R.id.commonEnterRoot);
        root.setVisibility(View.GONE);
    }

    public void show() {
        View root = mActivity.findViewById(R.id.commonEnterRoot);
        root.setVisibility(View.VISIBLE);
    }

    public interface CameraAndPhoto {
        void camera();

        void photo();
    }
}
