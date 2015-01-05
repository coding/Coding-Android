package net.coding.program;

import android.content.Intent;
import android.net.Uri;
import android.view.animation.Animation;
import android.widget.ImageView;

import net.coding.program.common.LoginBackground;
import net.coding.program.common.UnreadNotify;
import net.coding.program.common.umeng.UmengActivity;
import net.coding.program.model.AccountInfo;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

import java.io.File;

/**
 * Created by cc191954 on 14-8-14.
 */
@EActivity(R.layout.entrance_image)
public class EntranceActivity extends UmengActivity {

    @ViewById
    ImageView image;

    @AnimationRes
    Animation entrance;

    Uri background = null;

    @AfterViews
    void init() {
        LoginBackground.PhotoItem photoItem = new LoginBackground(this).getPhoto();
        File file = photoItem.getCacheFile(this);
        if (file.exists()) {
            background = Uri.fromFile(file);
            image.setImageURI(background);
        }

        entrance.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                next();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        image.startAnimation(entrance);
    }

    void next() {
        Intent intent;
        String mGlobalKey = AccountInfo.loadAccount(this).global_key;
        if (mGlobalKey.isEmpty()) {
            intent = new Intent(this, LoginActivity_.class);
            if (background != null) {
                intent.putExtra("background", background);
            }
        } else {
            intent = new Intent(this, MainActivity_.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        startActivity(intent);

        UnreadNotify.update(this);
        finish();
    }
}

