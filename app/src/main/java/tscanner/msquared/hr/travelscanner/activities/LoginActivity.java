package tscanner.msquared.hr.travelscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.steamcrafted.loadtoast.LoadToast;
import net.steamcrafted.loadtoast.LoadToastView;

import java.util.List;

import tscanner.msquared.hr.travelscanner.R;
import tscanner.msquared.hr.travelscanner.helpers.Rest.ServerManager;
import tscanner.msquared.hr.travelscanner.models.restModels.AppUser;

//pocetna
public class LoginActivity extends Activity {

    private final String TAG = this.getClass().getName();

    private Button login;
    private EditText username=null;
    private EditText password=null;
    private TextView loginAnonymous;
    private boolean GLOBAL_FAST_ENTRY=true;

    private Button testButton;

    private ServerManager serverManager;
    private LoadToast loadToast;

    private List<AppUser> appUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.referenceViews();
    }

    private void referenceViews(){
        this.login = (Button)findViewById(R.id.login);
        this.username = (EditText)findViewById(R.id.editUsername);
        this.password = (EditText)findViewById(R.id.editPassword);
        this.loginAnonymous=(TextView)findViewById(R.id.loginAnonymous);

        this.testButton = (Button) findViewById(R.id.btnTest);
        this.loadToast = new LoadToast(this);

        this.testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchAllUsers();
            }
        });
    }

    public void login(View view){
        if(username.getText().toString().equals("admin") &&
                password.getText().toString().equals("admin")) {
            //correcct password
            //Toast.makeText(getApplicationContext(),"Redirecting..",Toast.LENGTH_SHORT).show();
            AcceptedLogin();

        }else{
            //wrong password
            Toast.makeText(getApplicationContext(),"sve je: \"admin\" ",Toast.LENGTH_LONG).show();
            Log.d("Login:", "User");
        }
    }

    public void anonymousOnClick(View view){
        Log.d("Login:","Anonymous");
        if(GLOBAL_FAST_ENTRY){
            AcceptedLogin();
        }
    }

    private void AcceptedLogin(){
        Intent intent;
        intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    private void fetchAllUsers(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching all users..");
        loadToast.setTranslationY(200);
        loadToast.show();
        serverManager.getAllAppUsers(new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if(appUsers != null){
                    loadToast.success();
                    for(AppUser user : appUsers){
                        Log.i(TAG, "User -> " + user);
                    }
                }
                else{
                    loadToast.error();
                }
            }
        });
    }




}
