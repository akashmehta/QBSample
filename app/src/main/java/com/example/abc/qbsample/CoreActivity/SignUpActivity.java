package com.example.abc.qbsample.CoreActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.abc.qbsample.R;
import com.example.abc.qbsample.Utils.Helper;
import com.example.abc.qbsample.Utils.UserCustomData;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener,View.OnFocusChangeListener,AdapterView.OnItemSelectedListener {
    EditText username,password,birthDate,age,confirm_password;
    Spinner gender,city,state,country;
    ArrayAdapter  genderadapter , cityadapter,stateadapter,countryadapter;
    Button submit;
    ImageView profile_Img;
    Dialog chooseImage;
    Bitmap userDp;
    Boolean gender_init = false ,state_init = false,country_init = false ,city_init =false;
    View username_ul,age_ul,password_ul,confirm_password_ul;
    TextView username_err,age_err,password_err,confirm_password_err,birthdate_err,gender_err,city_err,country_err,state_err;
    DatePickerDialog birthDateDialog;
    File image_file;

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()){
            case R.id.username:
                if(hasFocus){
                    username_err.setVisibility(View.GONE);
                    username_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    username_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if(username.getText().toString().equals("")){
                        username_err.setVisibility(View.VISIBLE);
                        username_ul.setBackgroundColor(Color.RED);
                        username_err.setText(getResources().getString(R.string.empty_username));
                    }
                }
                break;
            case R.id.age:
                if(hasFocus){
                    age_err.setVisibility(View.GONE);
                    age_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    age_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if(age.getText().toString().equals("")){
                        age_err.setVisibility(View.VISIBLE);
                        age_ul.setBackgroundColor(Color.RED);
                        age_err.setText(getResources().getString(R.string.empty_age));
                    }
                }

                break;
            case R.id.password:
                if(hasFocus){
                    password_err.setVisibility(View.GONE);
                    password_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    password_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if(password.getText().toString().equals("")){
                        password_err.setVisibility(View.VISIBLE);
                        password_ul.setBackgroundColor(Color.RED);
                        password_err.setText(getResources().getString(R.string.empty_password));
                    }else if (password.getText().toString().length()<8){
                        password_err.setVisibility(View.VISIBLE);
                        password_ul.setBackgroundColor(Color.RED);
                        password_err.setText(getResources().getString(R.string.invalid_password));
                    }
                }

                break;
            case R.id.confirm_password:
                if(hasFocus){
                    confirm_password_err.setVisibility(View.GONE);
                    confirm_password_ul.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }else{
                    confirm_password_ul.setBackgroundColor(getResources().getColor(R.color.text_color));
                    if(confirm_password.getText().toString().equals("")){
                        confirm_password_err.setVisibility(View.VISIBLE);
                        confirm_password_ul.setBackgroundColor(Color.RED);
                        confirm_password_err.setText(getResources().getString(R.string.empty_confirm_password));
                    }
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Helper.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            Uri imageUri = data.getData();

            try
            {
                userDp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                image_file = new File(Helper.getRealPathFromURI(this, imageUri));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(requestCode == Helper.CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            userDp = (Bitmap) data.getExtras().get("data");
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = Helper.getImageUri(getApplicationContext(), userDp);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            image_file = new File(Helper.getRealPathFromURI(this,tempUri));


        }
        if(userDp!=null){
            profile_Img.setPadding(Helper.dp_padding,Helper.dp_padding,Helper.dp_padding,Helper.dp_padding);
            profile_Img.setImageBitmap(Helper.getRoundedCornerBitmap(userDp));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        QBAuth.createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle params) {
                // success
                System.out.println("_______session is started");
            }

            @Override
            public void onError(QBResponseException error) {
                // errors

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        username  = (EditText) findViewById(R.id.username);
        username.setOnFocusChangeListener(this);
        password = (EditText) findViewById(R.id.password);
        password.setOnFocusChangeListener(this);
        birthDate = (EditText) findViewById(R.id.birthDate);
        birthDate.setOnFocusChangeListener(this);
        age = (EditText) findViewById(R.id.age);
        age.setOnFocusChangeListener(this);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        confirm_password.setOnFocusChangeListener(this);

        username_ul = findViewById(R.id.username_ul);
        password_ul = findViewById(R.id.password_ul);
        age_ul = findViewById(R.id.age_ul);
        confirm_password_ul  = findViewById(R.id.confirm_password_ul);

        username_err = (TextView)findViewById(R.id.username_err);
        password_err = (TextView)findViewById(R.id.password_err);
        age_err = (TextView)findViewById(R.id.age_err);
        confirm_password_err  = (TextView)findViewById(R.id.confirm_password_err);
        birthdate_err = (TextView) findViewById(R.id.birthdate_err);
        gender_err = (TextView) findViewById(R.id.gender_err);
        city_err = (TextView) findViewById(R.id.city_err);
        state_err = (TextView) findViewById(R.id.state_err);
        country_err = (TextView) findViewById(R.id.country_err);

        birthDate.setOnClickListener(this);
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(this);

        gender = (Spinner) findViewById(R.id.gender);
        city = (Spinner) findViewById(R.id.city);
        state = (Spinner) findViewById(R.id.state);
        country = (Spinner) findViewById(R.id.country);

        genderadapter = new ArrayAdapter (this,R.layout.spinner_row,R.id.spinner_row_title,Helper.gender_list);
        cityadapter = new ArrayAdapter (this,R.layout.spinner_row,R.id.spinner_row_title,Helper.city_list);
        stateadapter = new ArrayAdapter (this,R.layout.spinner_row,R.id.spinner_row_title,Helper.state_list);
        countryadapter = new ArrayAdapter (this,R.layout.spinner_row,R.id.spinner_row_title,Helper.country_list);

        gender.setAdapter(genderadapter);
        city.setAdapter(cityadapter);
        state.setAdapter(stateadapter);
        country.setAdapter(countryadapter);

        gender.setSelection(0);city.setSelection(0);state.setSelection(0);country.setSelection(0);
        
        gender.setOnItemSelectedListener(this);city.setOnItemSelectedListener(this);state.setOnItemSelectedListener(this);country.setOnItemSelectedListener(this);

        profile_Img = (ImageView) findViewById(R.id.profile_Img);
        profile_Img.setOnClickListener(this);

    }

    private void chatLogin(final QBUser current_user){
        QBChatService chatService = QBChatService.getInstance();
        chatService.login(current_user, new QBEntityCallback() {
            @Override
            public void onSuccess(Object o, Bundle bundle) {
                Intent it = new Intent(SignUpActivity.this, CallActivity.class);
                System.out.println(".........user object is : "+current_user);
                it.putExtra("CurrentUser",current_user);
                startActivity(it);
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.submit:

                if(!username.getText().toString().equals("")
                        && !age.getText().toString().equals("")
                        && !password.getText().toString().equals("")
                        && !confirm_password.getText().toString().equals("")
                        && gender.getSelectedItemPosition()!=0
                        && city.getSelectedItemPosition()!=0
                        && state.getSelectedItemPosition()!=0
                        && country.getSelectedItemPosition()!=0
                        ){
                    if(!password.getText().toString().equals(confirm_password.getText().toString())){
                        confirm_password_err.setVisibility(View.VISIBLE);
                        confirm_password_ul.setBackgroundColor(Color.RED);
                        confirm_password_err.setText(getResources().getString(R.string.mismatch_password));
                    }else{
                        if(password.getText().toString().length()<8){
                            password_err.setVisibility(View.VISIBLE);
                            password_ul.setBackgroundColor(Color.RED);
                            password_err.setText(getResources().getString(R.string.invalid_password));
                        }else{
                            final QBUser current_user = new QBUser(username.getText().toString(), password.getText().toString());

                            QBUsers.signUp(current_user, new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser user, Bundle args) {

                                    QBAuth.createSession(current_user, new QBEntityCallback<QBSession>() {
                                        @Override
                                        public void onSuccess(QBSession qbSession, Bundle bundle) {

                                            System.out.println(".........user object is : " + current_user);
                                            if (image_file != null) {
                                                QBContent.uploadFileTask(image_file, true, null, new QBEntityCallback<QBFile>() {
                                                    @Override
                                                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                                                        current_user.setFileId(qbFile.getId());
                                                        UserCustomData customData = new UserCustomData();
                                                        customData.setGender((String) gender.getSelectedItem());
                                                        customData.setCity((String) city.getSelectedItem());
                                                        customData.setCountry((String) country.getSelectedItem());
                                                        customData.setState((String) state.getSelectedItem());
                                                        customData.setAge(Integer.parseInt(age.getText().toString()));
                                                        customData.setBirthdate(birthDate.getText().toString());
                                                        customData.setProfile_picture_url(qbFile.getPublicUrl());
                                                        String data = Helper.getJsonString(customData);
                                                        current_user.setCustomData(data);

                                                        current_user.setOldPassword(password.getText().toString());
                                                        QBUsers.updateUser(current_user, new QBEntityCallback<QBUser>() {
                                                            @Override
                                                            public void onSuccess(QBUser user, Bundle bundle) {
                                                                chatLogin(current_user);
                                                            }

                                                            @Override
                                                            public void onError(QBResponseException e) {
                                                                e.printStackTrace();
                                                                System.out.println("________________---file upload error");
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void onError(QBResponseException e) {
                                                        System.out.println("-------------file not uploaded error" + e.getMessage());
                                                        e.printStackTrace();
                                                    }
                                                });
                                            } else {
                                                chatLogin(current_user);
                                            }

                                        }

                                        @Override
                                        public void onError(QBResponseException e) {
                                            e.printStackTrace();
                                        }
                                    });
                                }

                                @Override
                                public void onError(QBResponseException error) {
                                    // error
                                    System.out.println("-------------error" + error.getMessage());
                                    error.printStackTrace();
                                }
                            });
                        }

                    }
                }else{
                    if(username.getText().toString().equals("")){
                        username_err.setVisibility(View.VISIBLE);
                        username_ul.setBackgroundColor(Color.RED);
                        username_err.setText(getResources().getString(R.string.empty_username));
                    }if(age.getText().toString().equals("")){
                        age_err.setVisibility(View.VISIBLE);
                        age_ul.setBackgroundColor(Color.RED);
                        age_err.setText(getResources().getString(R.string.empty_age));
                    }if(password.getText().toString().equals("")){
                        password_err.setVisibility(View.VISIBLE);
                        password_ul.setBackgroundColor(Color.RED);
                        password_err.setText(getResources().getString(R.string.empty_password));
                    }if(confirm_password.getText().toString().equals("")){
                        confirm_password_err.setVisibility(View.VISIBLE);
                        confirm_password_ul.setBackgroundColor(Color.RED);
                        confirm_password_err.setText(getResources().getString(R.string.empty_confirm_password));
                    }if(gender.getSelectedItemPosition()==0 ){

                        gender_err.setVisibility(View.VISIBLE);
                        gender_err.setText(getResources().getString(R.string.gender_not_selected));
                    }if(city.getSelectedItemPosition()==0){

                        city_err.setVisibility(View.VISIBLE);
                        city_err.setText(getResources().getString(R.string.city_not_selected));
                    }if(state.getSelectedItemPosition()==0){

                        state_err.setVisibility(View.VISIBLE);
                        state_err.setText(getResources().getString(R.string.state_not_selected));
                    }if(country.getSelectedItemPosition()==0){

                        country_err.setVisibility(View.VISIBLE);
                        country_err.setText(getResources().getString(R.string.country_not_selected));
                    }
                }

                break;
            case R.id.profile_Img:
                try
                {
                    chooseImage= Helper.getHelperInstance().createDialog(SignUpActivity.this,false,Helper.chooseImageDialog);
                    Button btnPositive = (Button)chooseImage.findViewById(R.id.positive_option);
                    Button btnNegative = (Button) chooseImage.findViewById(R.id.negative_option);
                    btnPositive.setOnClickListener(this);
                    btnNegative.setOnClickListener(this);
                    chooseImage.show();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
                break;
            case R.id.positive_option:
                chooseImage.dismiss();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), Helper.PICK_IMAGE_REQUEST);
                break;
            case R.id.negative_option:
                chooseImage.dismiss();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent,Helper.CAMERA_REQUEST);
                break;
            case R.id.birthDate:
                int date = Calendar.getInstance().get(Calendar.DATE);
                int month = Calendar.getInstance().get(Calendar.MONTH);
                int year = Calendar.getInstance().get(Calendar.YEAR);
                birthdate_err.setVisibility(View.GONE);
                System.out.println("________Current Date is : "+date+"/"+month+"/"+year);
                birthDateDialog = new DatePickerDialog(SignUpActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        birthDate.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
                        if(!age.getText().toString().equals("")
                                && Calendar.getInstance().get(Calendar.YEAR) != (year+Integer.parseInt(age.getText().toString()))
                                && Calendar.getInstance().get(Calendar.YEAR) != (year+Integer.parseInt(age.getText().toString())+1)
                                ){
                            birthdate_err.setVisibility(View.VISIBLE);
                            birthdate_err.setText(getResources().getString(R.string.improper_birthdate));
                        }else{
                            birthdate_err.setVisibility(View.GONE);
                        }
                    }
                },year,month,date);
                birthDateDialog.updateDate(year, month, date);
                birthDateDialog.show();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){
            switch (parent.getId()){
                case R.id.gender:
                    if(gender_init){
                        System.out.println("_______________ Gender is selected ");
                        gender_err.setVisibility(View.VISIBLE);
                        gender_err.setText(getResources().getString(R.string.gender_not_selected));
                    }
                    gender_init = true;
                    break;
                case R.id.city:
                    if(city_init){

                        System.out.println("_______________ City is selected ");
                        city_err.setVisibility(View.VISIBLE);
                        city_err.setText(getResources().getString(R.string.city_not_selected));
                    }
                    city_init = true;
                    break;
                case R.id.state:
                    if(state_init){

                        System.out.println("_______________ State is selected ");
                        state_err.setVisibility(View.VISIBLE);
                        state_err.setText(getResources().getString(R.string.state_not_selected));
                    }
                    state_init = true;
                    break;
                case R.id.country:
                    if(country_init){
                        System.out.println("_______________ Country is selected ");
                        country_err.setVisibility(View.VISIBLE);
                        country_err.setText(getResources().getString(R.string.country_not_selected));
                    }
                    country_init = true;
                    break;
            }
        }else{
            switch (parent.getId()){
                case R.id.gender:
                    gender_err.setVisibility(View.GONE);
                    gender_init = true;
                    break;
                case R.id.city:
                    city_err.setVisibility(View.GONE);
                    city_init = true;
                    break;
                case R.id.state:
                    state_err.setVisibility(View.GONE);
                    state_init= true;
                    break;
                case R.id.country:
                    country_err.setVisibility(View.GONE);
                    country_init= true;
                    break;
            }
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
