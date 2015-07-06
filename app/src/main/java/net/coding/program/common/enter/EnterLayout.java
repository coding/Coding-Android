package net.coding.program.common.enter;

import android.app.Activity;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import net.coding.program.R;
import net.coding.program.common.CommentBackup;
import net.coding.program.common.EmojiTranslate;
import net.coding.program.common.Global;

/**
 * Created by chaochen on 14-10-28.
 */
public class EnterLayout {

    public TextView sendText;
    public ImageButton send;
    public EditText content;
    private Activity mActivity;
    private Type mType = Type.Default;
    private TextWatcher restoreWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            Object tag = content.getTag();
            if (tag == null) {
                return;
            }

            CommentBackup.getInstance().save(CommentBackup.BackupParam.create(tag), s.toString());
        }
    };

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
                    if (mActivity instanceof CameraAndPhoto) {
                        CameraAndPhoto cameraAndPhoto = (CameraAndPhoto) mActivity;
                        cameraAndPhoto.photo();
                    }
                }
            });
        } else {
            send.setVisibility(View.GONE);
        }

        content = (EditText) activity.findViewById(R.id.comment);


        content.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updateSendButtonStyle();
            }
        });
        content.setText("");

        content.addTextChangedListener(new SimpleTextWatcher() {
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
                } else {
                    int emojiStart = 0;
                    int emojiEnd;
                    boolean startFinded = false;
                    int end = start + count;
                    for (int i = start; i < end; ++i) {
                        if (s.charAt(i) == ':') {
                            if (!startFinded) {
                                emojiStart = i;
                                startFinded = true;
                            } else {
                                emojiEnd = i;
                                if (emojiEnd - emojiStart < 2) { // 指示的是两个：的位置，如果是表情的话，间距肯定大于1
                                    emojiStart = emojiEnd;
                                } else {
                                    String newString = s.subSequence(emojiStart + 1, emojiEnd).toString();
                                    EmojiconSpan emojiSpan = new EmojiconSpan(mActivity, newString);
                                    if (emojiSpan.getDrawable() != null) {
                                        Editable editable = content.getText();
                                        editable.setSpan(emojiSpan, emojiStart, emojiEnd + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        startFinded = false;
                                    } else {
                                        emojiStart = emojiEnd;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public EnterLayout(Activity activity, View.OnClickListener sendTextOnClick) {
        this(activity, sendTextOnClick, Type.Default);
    }

    public static void insertText(EditText edit, String s) {
        edit.requestFocus();
        int insertPos = edit.getSelectionStart();

        String insertString = s + " ";
        Editable editable = edit.getText();
        editable.insert(insertPos, insertString);
    }

    public void updateSendButtonStyle() {
        if (mType == Type.Default) {
            if (sendButtonEnable()) {
                sendText.setVisibility(View.VISIBLE);
                send.setVisibility(View.GONE);
            } else {
                sendText.setVisibility(View.GONE);
                send.setVisibility(View.VISIBLE);
            }
        }

        if (sendButtonEnable()) {
            sendText.setBackgroundResource(R.drawable.edit_send_green);
            sendText.setTextColor(0xffffffff);
        } else {
            sendText.setBackgroundResource(R.drawable.edit_send);
            sendText.setTextColor(0xff999999);
        }
    }

    protected boolean sendButtonEnable() {
        return content.getText().length() > 0;
    }

    public void hideKeyboard() {
        Global.popSoftkeyboard(mActivity, content, false);
    }

    public void popKeyboard() {
        content.requestFocus();
        Global.popSoftkeyboard(mActivity, content, true);
    }

    public void insertText(String s) {
        insertText(content, s);
    }

    public void setText(String s) {
        content.requestFocus();
        Editable editable = content.getText();
        editable.clear();
        editable.insert(0, s);
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

    public boolean isShow() {
        View root = mActivity.findViewById(R.id.commonEnterRoot);
        return root.getVisibility() == View.VISIBLE;
    }

    public void restoreSaveStart() {
        content.addTextChangedListener(restoreWatcher);
    }

    public void restoreSaveStop() {
        content.removeTextChangedListener(restoreWatcher);
    }

    public void restoreDelete(Object comment) {
        CommentBackup.getInstance().delete(CommentBackup.BackupParam.create(comment));
    }

    public void restoreLoad(Object object) {
        if (object == null) {
            return;
        }

        restoreSaveStop();
        clearContent();
        String lastInput = CommentBackup.getInstance().load(CommentBackup.BackupParam.create(object));
        content.getText().append(lastInput);
        restoreSaveStart();
    }

    public enum Type {
        Default, TextOnly
    }


    public interface CameraAndPhoto {
        void photo();
    }

}
