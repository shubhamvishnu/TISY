package com.projects.shubhamkhandelwal.tisy;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutUsActivity extends FragmentActivity {
    CircleImageView shubham, mervin, shweta, anugraha, nadeem, jitendar, radha, shriram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        shubham = (CircleImageView) findViewById(R.id.shubham_image_button);
        mervin = (CircleImageView) findViewById(R.id.mervin_image_button);
        shweta = (CircleImageView) findViewById(R.id.shweta_image_button);
        anugraha = (CircleImageView) findViewById(R.id.anugraha_image_button);
        nadeem = (CircleImageView) findViewById(R.id.nadeem_image_button);
        jitendar = (CircleImageView) findViewById(R.id.jitendar_image_button);
        radha = (CircleImageView) findViewById(R.id.radha_image_button);
        shriram = (CircleImageView) findViewById(R.id.shriram_image_button);


        final Map<Integer, String> urls = new HashMap<>();
        urls.put(0,"https://lh5.googleusercontent.com/-50bCElOTL54/AAAAAAAAAAI/AAAAAAAAAY0/w8-ENiV9khk/photo.jpg");
        urls.put(1, "https://lh3.googleusercontent.com/-83d8F7-ax4Q/AAAAAAAAAAI/AAAAAAAAELY/k8EBR3yzsfo/photo.jpg");
        urls.put(2,"https://lh3.googleusercontent.com/-IzFIEBtrn4M/AAAAAAAAAAI/AAAAAAAAAAA/AdA25JYhpXA/s64-c/118216924685027059670.jpg");
        urls.put(3,"https://lh3.googleusercontent.com/-xXnU5IoiORw/AAAAAAAAAAI/AAAAAAAAAAA/kanHS336Fjc/s64-c/110223691325257952559.jpg");
        urls.put(4,"https://lh5.googleusercontent.com/-g_1kt0LmkW0/AAAAAAAAAAI/AAAAAAAAAOg/TA7iqhRBRic/photo.jpg");
        urls.put(5,"https://lh6.googleusercontent.com/-c_YaMz8zJBg/AAAAAAAAAAI/AAAAAAAACXE/urmHsxj1pTw/photo.jpg");
        urls.put(6,"https://lh5.googleusercontent.com/-_VrOTeqq5mY/AAAAAAAAAAI/AAAAAAAAAfw/25I5y-cWVak/photo.jpg");
        urls.put(7,"https://lh3.googleusercontent.com/-rdE2Z1u92Pc/AAAAAAAAAAI/AAAAAAAAAAA/-PHpT-LNDmY/s64-c/107611578865601721410.jpg");

        Picasso.with(this).load(Uri.parse(urls.get(0))).networkPolicy(NetworkPolicy.OFFLINE).into(shubham, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(0))).error(R.drawable.default_profile_image_icon).into(shubham);
            }
        });

        // mervin
        Picasso.with(this).load(Uri.parse(urls.get(1))).networkPolicy(NetworkPolicy.OFFLINE).into(mervin, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(1))).error(R.drawable.default_profile_image_icon).into(mervin);
            }
        });

        // shweta
        Picasso.with(this).load(Uri.parse(urls.get(2))).networkPolicy(NetworkPolicy.OFFLINE).into(shweta, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(2))).error(R.drawable.default_profile_image_icon).into(shweta);
            }
        });

        // anugraha
        Picasso.with(this).load(Uri.parse(urls.get(3))).networkPolicy(NetworkPolicy.OFFLINE).into(anugraha, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(3))).error(R.drawable.default_profile_image_icon).into(anugraha);
            }
        });

        // nad
        Picasso.with(this).load(Uri.parse(urls.get(4))).networkPolicy(NetworkPolicy.OFFLINE).into(nadeem, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(4))).error(R.drawable.default_profile_image_icon).into(nadeem);
            }
        });

        // jitu
        Picasso.with(this).load(Uri.parse(urls.get(5))).networkPolicy(NetworkPolicy.OFFLINE).into(jitendar, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(5))).error(R.drawable.default_profile_image_icon).into(jitendar);
            }
        });

        // radhu
        Picasso.with(this).load(Uri.parse(urls.get(6))).networkPolicy(NetworkPolicy.OFFLINE).into(radha, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(6))).error(R.drawable.default_profile_image_icon).into(radha);
            }
        });
        // shriram
        Picasso.with(this).load(Uri.parse(urls.get(7))).networkPolicy(NetworkPolicy.OFFLINE).into(shriram, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {

                Picasso.with(AboutUsActivity.this).load(Uri.parse(urls.get(7))).error(R.drawable.default_profile_image_icon).into(shriram);
            }
        });

    }




    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AboutUsActivity.this, UserInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
