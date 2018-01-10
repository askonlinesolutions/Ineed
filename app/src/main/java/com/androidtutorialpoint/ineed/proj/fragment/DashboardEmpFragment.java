package com.androidtutorialpoint.ineed.proj.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.androidtutorialpoint.ineed.R;
import com.androidtutorialpoint.ineed.proj.Utils.Utillity;
import com.androidtutorialpoint.ineed.proj.activities.ProfileViewed;
import com.androidtutorialpoint.ineed.proj.models.EmployerProfileData;
import com.androidtutorialpoint.ineed.proj.models.ImageInputHelper;
import com.androidtutorialpoint.ineed.proj.models.LoginData;
import com.androidtutorialpoint.ineed.proj.models.ProfileDetailMOdel;
import com.androidtutorialpoint.ineed.proj.webservices.ApiList;
import com.androidtutorialpoint.ineed.proj.webservices.CustomRequest;
import com.androidtutorialpoint.ineed.proj.webservices.VolleySingelton;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.mukesh.tinydb.TinyDB;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.helpshift.support.webkit.CustomWebViewClient.TAG;


public class DashboardEmpFragment extends Fragment implements ImageInputHelper.ImageActionListener{
    EditText etEmail,etName,etcontact,etcompany;
    TextView txt_proftitle,txt_personal, txtProfileView, txtPackage, txtExpired, txtCredit, txtLeft, txtUsed;
    LinearLayout ll_savecancel;
    private ImageInputHelper imageInputHelper;
    ImageView imgUser, imgCamera;
    String img,language, userId;
    TinyDB tinyDB;
    LoginData loginData;
    EmployerProfileData profileDetailMOdel = new EmployerProfileData();

    RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_dashboard_emp, container, false);
        imageInputHelper = new ImageInputHelper(this);
        imageInputHelper.setImageActionListener(this);
        loginData = new LoginData();
        tinyDB = new TinyDB(getContext());
        requestQueue= VolleySingelton.getsInstance().getmRequestQueue();

//             find id
        txtCredit = view.findViewById(R.id.credit_point);
        txtExpired = view.findViewById(R.id.et_expired);
        txtPackage = view.findViewById(R.id.et_package);
        txtLeft = view.findViewById(R.id.credit_left);
        ll_savecancel= (LinearLayout)view.findViewById(R.id.ll_savecancel);
        txtProfileView = (TextView) view.findViewById(R.id.txt_profileViewed);
        etName = (EditText) view.findViewById(R.id.et_name);
        etcompany = (EditText) view.findViewById(R.id.et_company);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etcontact = (EditText) view.findViewById(R.id.et_phone);
        txt_personal = (TextView) view.findViewById(R.id.txt_personal);
        txt_proftitle = (TextView) view.findViewById(R.id.etProfile_name);
        imgUser = (ImageView) view.findViewById(R.id.emp_img_profilew) ;
        imgCamera = (ImageView) view.findViewById(R.id.emp_img_camera);

        txtProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ProfileViewed.class));
            }
        });

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Add Photo!");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Photo"))
                        {
                            imageInputHelper.takePhotoWithCamera();

                        }
                        else if (options[item].equals("Choose from Gallery"))
                        {
                            imageInputHelper.selectImageFromGallery();

                        }
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        etcontact.setEnabled(false);
        etEmail.setEnabled(false);
        etcompany.setEnabled(false);
        etName.setEnabled(false);

        txt_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //    Toast.makeText(getApplication(),"Hello",Toast.LENGTH_SHORT).show();
            }
        });
        txt_personal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                clickonDrawable(v,event);
                return false;
            }
        });

        return view;
    }

    void clickonDrawable(View v, MotionEvent event) {
        final int DRAWABLE_LEFT = 0;
        final int DRAWABLE_TOP = 1;
        final int DRAWABLE_RIGHT = 2;
        final int DRAWABLE_BOTTOM = 3;
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if(event.getRawX() >= (txt_personal.getRight() - txt_personal.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                etEmail.setEnabled(true);
                etcontact.setEnabled(true);
                etcompany.setEnabled(true);
                etName.setEnabled(true);
                ll_savecancel.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        imageInputHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onImageSelectedFromGallery(Uri uri, File imageFile) {
        imageInputHelper.requestCropImage(uri, 800, 450, 16, 9);

    }

    @Override
    public void onImageTakenFromCamera(Uri uri, File imageFile) {
        imageInputHelper.requestCropImage(uri, 800, 450, 16, 9);

    }

    @Override
    public void onImageCropped(Uri uri, File imageFile) {
        try {
            // getting bitmap from uri
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);

            // showing bitmap in image view
            Glide.with(this).load(bitmap).apply(RequestOptions.circleCropTransform()).into(imgUser);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Gson gson = new Gson();
    @Override
    public void onResume() {
        super.onResume();
        String loginPrefData = tinyDB.getString("login_data");
        loginData = gson.fromJson(loginPrefData, LoginData.class);
        userId = loginData.getUser_detail().getUser_id();
        language = tinyDB.getString("language_id");

        getProfile();
    }

    public void getProfile(){

        HashMap<String,String> params=new HashMap<>();
        params.put("user_id",userId);
        CustomRequest customRequest=new CustomRequest(Request.Method.POST, ApiList.EMPLOYER_PROFILE,params,
                this.success(),this.error());
        requestQueue.add(customRequest);
    }


    private Response.Listener<JSONObject> success()
    {
        Utillity.showloadingpopup(getActivity());
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utillity.hidepopup();
                Log.d(TAG, "onResponse:data "+response.toString());
                if (response!=null){
                    try {
                        if (!response.getString("status").equals("false")){

                            profileDetailMOdel = gson.fromJson(response.toString(), EmployerProfileData.class);
                            if (profileDetailMOdel.getProfile_detail() != null) {
                                etName.setText(profileDetailMOdel.getProfile_detail().getUser_fname());
                                etEmail.setText(profileDetailMOdel.getProfile_detail().getUser_email());
                                etcontact.setText(profileDetailMOdel.getProfile_detail().getUser_phone());
                                etcompany.setText(profileDetailMOdel.getProfile_detail().getUser_company());
                                txtExpired.setText(profileDetailMOdel.getProfile_detail().getUser_package_expire_date());
                                txtCredit.setText(String.valueOf(profileDetailMOdel.getProfile_detail().getUser_package_credit()));
                                txtLeft.setText(String.valueOf(profileDetailMOdel.getProfile_detail().getUser_credit_use()));
                                txt_proftitle.setText(profileDetailMOdel.getProfile_detail().getUser_fname());
                            }
                        } else {
                            Utillity.message(getContext(), "No data found");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    private Response.ErrorListener error()
    {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "onErrorResponse: "+error.toString());
                Utillity.message(getContext(), getResources().getString(R.string.internetConnection));
                Utillity.hidepopup();

            }
        };
    }

}
