package tscanner.msquared.hr.travelscanner.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.gson.Gson;
import com.kogitune.activity_transition.ActivityTransitionLauncher;
import com.squareup.picasso.Picasso;

import net.steamcrafted.loadtoast.LoadToast;

import java.util.List;

import tscanner.msquared.hr.travelscanner.InternetConnectionCheck;
import tscanner.msquared.hr.travelscanner.R;
import tscanner.msquared.hr.travelscanner.helpers.PrefsHelper;
import tscanner.msquared.hr.travelscanner.helpers.Rest.ServerManager;
import tscanner.msquared.hr.travelscanner.models.restModels.AppUser;
import tscanner.msquared.hr.travelscanner.models.restModels.Purchase;
import tscanner.msquared.hr.travelscanner.models.restModels.ResponseMessage;
import tscanner.msquared.hr.travelscanner.models.restModels.TravelDestination;
import tscanner.msquared.hr.travelscanner.models.restModels.Traveler;

//pocetna
public class LoginActivity extends Activity {

    private final String TAG = this.getClass().getSimpleName();

    private FloatingActionButton loginButton;
    private EditText username;
    private EditText password;
    private FloatingActionButton loginAnonymous;
    private boolean GLOBAL_FAST_ENTRY=true;

    private CheckBox rememberMeCheckbox;

    private ServerManager serverManager;
    private LoadToast loadToast;

    private PrefsHelper prefsHelper;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.prefsHelper = new PrefsHelper(this);
        this.gson = new Gson();

        InternetConnectionCheck check = new InternetConnectionCheck(this);
        check.setCheckCallback(new InternetConnectionCheck.OnCheckCallback() {
            @Override
            public void onCheck(boolean hasConnection) {
                if (hasConnection) {
                    referenceViews();
                } else {
                    finish();
                }
            }
        });
        check.checkConnection();
    }

    private void referenceViews() {
        this.loginButton = (FloatingActionButton) findViewById(R.id.btnLogin);
        this.username = (EditText) findViewById(R.id.editUsername);
        this.password = (EditText) findViewById(R.id.editPassword);
        this.loginAnonymous = (FloatingActionButton) findViewById(R.id.btnSkip);
        this.rememberMeCheckbox = (CheckBox) findViewById(R.id.cbRememberMe);

        this.loadToast = new LoadToast(this);
        this.loadToast.setTranslationY(300);

        this.tryRememberMeLogin();

        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginTry();
            }
        });
    }

    private void tryRememberMeLogin(){
        if(prefsHelper.getBoolean(PrefsHelper.REMEMBER_USER_LOGIN_SKIP, false)){
            if(prefsHelper.getString(PrefsHelper.LOGGED_IN_USER_APPUSER_DATA, null) != null){
                AppUser user = gson.fromJson(prefsHelper.getString(PrefsHelper.LOGGED_IN_USER_APPUSER_DATA, null), AppUser.class);
                this.username.setText(user.getEmail());
                this.rememberMeCheckbox.setChecked(true);
            }
        }
    }

    public void loginTry(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Logging in..").show();
        serverManager.getAppUsersWithPassword(password.getText().toString(), new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if (appUsers != null) {
                    String emailToEvaluate = username.getText().toString();
                    for (AppUser user : appUsers) {
                        if (emailToEvaluate.equals(user.getEmail())) {
                            loadToast.success();
                            AcceptedLogin(user);
                            return;
                        }
                    }
                    loadToast.error();
                } else {
                    loadToast.error();
                }
            }
        });
    }

    public void anonymousOnClick(View view){
        Log.d("Login:", "Anonymous");
        if(GLOBAL_FAST_ENTRY){
            AcceptedLogin(null);
        }
    }

    public void registrationOnClick(View view){
        Intent intent;
        intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void AcceptedLogin(AppUser loggingInUser){
        this.prefsHelper.putString(PrefsHelper.LOGGED_IN_USER_APPUSER_DATA, (loggingInUser != null) ? gson.toJson(loggingInUser) : null);
        this.prefsHelper.putBoolean(PrefsHelper.FIRST_TIME_TAKING_PHOTO_HINT, true);
        this.prefsHelper.putBoolean(PrefsHelper.FIRST_TIME_TRAVELPOINTS_HINT, true);
        if(this.rememberMeCheckbox.isChecked()){
            this.prefsHelper.putBoolean(PrefsHelper.REMEMBER_USER_LOGIN_SKIP, true);
        }
        else{
            this.prefsHelper.putBoolean(PrefsHelper.REMEMBER_USER_LOGIN_SKIP, false);
        }
        Intent intent;
        intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
       // finish();
    }

    private void fetchUserWithPassword(String password){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Logging in..").show();
        serverManager.getAppUsersWithPassword(password, new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if (appUsers != null) {
                    loadToast.success();
                    for (AppUser user : appUsers) {
                        Log.i(TAG, "User -> " + user);
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchAllUsers(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching all users..").show();
        serverManager.getAllAppUsers(new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if (appUsers != null) {
                    loadToast.success();
                    for (AppUser user : appUsers) {
                        Log.i(TAG, "User -> " + user);
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchUserById(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching user..").show();
        serverManager.getAppUserById(id, new ServerManager.Callback<AppUser>() {
            @Override
            public void requestResult(AppUser appUser) {
                if (appUser != null) {
                    loadToast.success();
                    Log.i(TAG, appUser.toString());
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchSalesmanUsers(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching salesmans..").show();
        serverManager.getAllSalesmanUsers(new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if (appUsers != null) {
                    loadToast.success();
                    for (AppUser appUser : appUsers) {
                        Log.i(TAG, appUser.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchNonSalesmanUsers(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching regulars..").show();
        serverManager.getAllNonSalesmanUsers(new ServerManager.Callback<List<AppUser>>() {
            @Override
            public void requestResult(List<AppUser> appUsers) {
                if (appUsers != null) {
                    loadToast.success();
                    for (AppUser appUser : appUsers) {
                        Log.i(TAG, appUser.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchAllTravelDestinations(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching destinations..").show();
        serverManager.getAllTravelDestinations(new ServerManager.Callback<TravelDestination[]>() {
            @Override
            public void requestResult(TravelDestination[] travelDestinations) {
                if (travelDestinations != null) {
                    loadToast.success();
                    for (TravelDestination travelDestination : travelDestinations) {
                        Log.i(TAG, travelDestination.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchDestinationById(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching destiantion..").show();
        serverManager.getTravelDestinationById(id, new ServerManager.Callback<TravelDestination>() {
            @Override
            public void requestResult(TravelDestination travelDestination) {
                if (travelDestination != null) {
                    loadToast.success();
                    Log.i(TAG, travelDestination.toString());
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchAllPurchases(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching purchases..").show();
        serverManager.getAllPurchases(new ServerManager.Callback<List<Purchase>>() {
            @Override
            public void requestResult(List<Purchase> purchases) {
                if (purchases != null) {
                    loadToast.success();
                    for (Purchase purchase : purchases) {
                        Log.i(TAG, purchase.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchPurchaseById(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching purchase..").show();
        serverManager.getPurchaseById(id, new ServerManager.Callback<Purchase>() {
            @Override
            public void requestResult(Purchase purchase) {
                if (purchase != null) {
                    loadToast.success();
                    Log.i(TAG, purchase.toString());
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchPurchaseWithSignature(String signature){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching purchase..").show();
        serverManager.getPurchaseWithSignature(signature, new ServerManager.Callback<Purchase>() {
            @Override
            public void requestResult(Purchase purchase) {
                if (purchase != null) {
                    loadToast.success();
                    Log.i(TAG, purchase.toString());
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchPurchasesMadeByUserWithId(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching purchases..").show();
        serverManager.getAllPurchasesFromUserId(id, new ServerManager.Callback<List<Purchase>>() {
            @Override
            public void requestResult(List<Purchase> purchases) {
                if (purchases != null) {
                    loadToast.success();
                    for (Purchase purchase : purchases) {
                        Log.i(TAG, purchase.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchAllTravelers(){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching travelers..").show();
        serverManager.getAllTravelers(new ServerManager.Callback<List<Traveler>>() {
            @Override
            public void requestResult(List<Traveler> travelers) {
                if (travelers != null) {
                    loadToast.success();
                    for (Traveler traveler : travelers) {
                        Log.i(TAG, traveler.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchTravelerById(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching traveler..").show();
        serverManager.getTravelerById(id, new ServerManager.Callback<Traveler>() {
            @Override
            public void requestResult(Traveler traveler) {
                if (traveler != null) {
                    loadToast.success();
                    Log.i(TAG, traveler.toString());
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void fetchTravelersWitchPurchaseId(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Fetching travelers..").show();
        serverManager.getTravelersWithPurchaseId(id, new ServerManager.Callback<List<Traveler>>() {
            @Override
            public void requestResult(List<Traveler> travelers) {
                if (travelers != null) {
                    loadToast.success();
                    for (Traveler traveler : travelers) {
                        Log.i(TAG, traveler.toString());
                    }
                } else {
                    loadToast.error();
                }
            }
        });
    }

    private void addNewAppUser(AppUser appUser){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Adding user..").show();
        serverManager.addNewAppUser(appUser, new ServerManager.Callback<ResponseMessage>() {
            @Override
            public void requestResult(ResponseMessage responseMessage) {
                if (responseMessage.getError() == null) {
                    loadToast.success();
                } else {
                    Log.e(TAG, responseMessage.getError());
                    loadToast.error();
                }
            }
        });
    }

    private void addNewPurchase(Purchase purchase){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        purchase.setPurchaseDate(null);
        loadToast.setText("Adding purchase..").show();
        serverManager.addNewPurchase(purchase, new ServerManager.Callback<ResponseMessage>() {
            @Override
            public void requestResult(ResponseMessage responseMessage) {
                if (responseMessage.getError() == null) {
                    loadToast.success();
                } else {
                    Log.e(TAG, responseMessage.getError());
                    loadToast.error();
                }
            }
        });
    }

    private void addNewTraveler(Traveler traveler){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Adding traveler..").show();
        serverManager.addNewTraveler(traveler, new ServerManager.Callback<ResponseMessage>() {
            @Override
            public void requestResult(ResponseMessage responseMessage) {
                if (responseMessage.getError() == null) {
                    loadToast.success();
                } else {
                    Log.e(TAG, responseMessage.getError());
                    loadToast.error();
                }
            }
        });
    }

    private void deleteAppUserWithId(int id){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Removing user..").show();
        serverManager.deleteAppUserWithId(id, new ServerManager.Callback<ResponseMessage>() {
            @Override
            public void requestResult(ResponseMessage responseMessage) {
                if (responseMessage.getError() == null) {
                    loadToast.success();
                } else {
                    Log.e(TAG, responseMessage.getError());
                    loadToast.error();
                }
            }
        });
    }

    private void updateUser(AppUser user){
        if(serverManager == null){
            serverManager = new ServerManager();
        }
        loadToast.setText("Updating user..").show();
        serverManager.updateAppUserWithId(user.getId(), user, new ServerManager.Callback<ResponseMessage>() {
            @Override
            public void requestResult(ResponseMessage responseMessage) {
                if (responseMessage.getError() == null) {
                    loadToast.success();
                } else {
                    Log.e(TAG, responseMessage.getError());
                    loadToast.error();
                }
            }
        });
    }


}
