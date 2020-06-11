package com.isgm.camreport.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ib.custom.toast.CustomToastView;
import com.isgm.camreport.BuildConfig;
import com.isgm.camreport.R;
import com.isgm.camreport.model.Area;
import com.isgm.camreport.model.Circuit;
import com.isgm.camreport.model.Route;
import com.isgm.camreport.network.APIService;
import com.isgm.camreport.network.RetrofitAgent;
import com.isgm.camreport.utility.Utilities;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CircuitAreaAndRouteActivity extends BaseActivity {
    private ArrayAdapter<Circuit> circuitArrayAdapter; // A Circuit Array Adapter for ListView
    private ArrayAdapter<Route> routeArrayAdapter;  // A Route Array Adapter for ListView
    private ArrayAdapter<String> areaArrayAdapter; // An Area Array Adapter for ListView
    private int areaId = 0; // An Area ID
    private int circuitId = 0; // A Circuit ID
    private int fetchType = 0; // Defines which Data to fetch [Circuit or Area or Route]
    private int routeId = 0; // A Route ID
    private int routeIdStore = 0; // A Route ID Store to Solve the problem of
    // routeId getting 0 when "RE-SEARCH" clicked, and then "NEXT" is clicked.
    private List<Area> areas; // A List of Areas
    private List<Circuit> circuits; // A List of Circuits
    private List<Route> routes; // A List of Routes
    private List<Route> routeList; // A buffer for Routes Search Result
    private List<Route> routeStore; // An Original Route List getting by choosing A specific Area
    private List<String> areaListString; // An Area List String to Show in List View
    private String areaString; // An Area String
    private String circuitString; // A Circuit String
    private String fiberOperationType; // May be one of (FTTX, B2B, MOBI, B2B PREMIUM)
    private String json; // An API's Result Json String
    private String routeString; // A Route String
    private String subtypeString1 = ""; // A Sub Type 1 String
    private String subtypeString2 = ""; // A Sub Type 2 String
    private String surveyOrPAC; // Survey or PAC
    private String typeString = ""; // A Type String
    private String[] type;
    private String[] b2BSurvey;
    private String[] b2BPac;
    private String[] mobiSurvey;
    private String[] mobiPac;
    private String[] b2BPremiumSurvey;
    private String[] b2BPremiumPac;
    private String[] subType1;
    private String[] subType2;
    private TextView areaTextView; // An Area Text View
    private TextView circuitTextView; // A Circuit Text View
    @BindView(R.id.indicator)
    TextView indicator; // Fiber Operation and Survey or PAC
    @BindView(R.id.typeSpinner)
    Spinner typeSpinner; // A Type Spinner
    @BindView(R.id.subType1Spinner)
    Spinner subType1Spinner; // A Sub Type 1 Spinner
    @BindView(R.id.subtype2Spinner)
    Spinner subType2Spinner; // A Sub Type 2 Spinner
    @BindView(R.id.subType)
    TextView subType1TextView; // A Sub Type 1 Text View
    @BindView(R.id.subType2)
    TextView subType2TextView; // A Sub Type 2 Text View
    @BindView(R.id.code)
    EditText code; // EditText for Code
    @BindView(R.id.routeTextView)
    TextView routeTextView; // A Text View for Route
    @BindView(R.id.searchView)
    SearchView searchView; // A Search View for Circuits, Areas and Routes
    @BindView(R.id.listView)
    ListView listView; // A List View for Circuits, Areas and Routes

    @OnItemSelected(R.id.typeSpinner)
    void onItemSelected(int position) {
        if (fiberOperationType.equals("FTTX")) {
            typeString = getTypeAbbreviation(type[position]);
        } else if (fiberOperationType.equals("B2B")) {
            if (surveyOrPAC.equals("Survey")) {
                typeString = getTypeAbbreviation(b2BSurvey[position]);
            } else {
                typeString = getTypeAbbreviation(b2BPac[position]);
            }
        } else if (fiberOperationType.equals("MOBI")) {
            if (surveyOrPAC.equals("Survey")) {
                typeString = getTypeAbbreviation(mobiSurvey[position]);
            } else {
                typeString = getTypeAbbreviation(mobiPac[position]);
            }
        } else {
            if (surveyOrPAC.equals("Survey")) {
                typeString = getTypeAbbreviation(b2BPremiumSurvey[position]);
            } else {
                typeString = getTypeAbbreviation(b2BPremiumPac[position]);
            }
        }
        String typeStringInSpinner = type[position];
        if (fiberOperationType.equals("FTTX")) {
            if (type[position].equals("PL (Pole)") || type[position].equals("CL (Cable Laying)") ||
                    type[position].equals("SPLICE (Splicing)") || type[position].equals("MH (ManHole)") ||
                    type[position].equals("HH (HandHole)") || type[position].equals("UG (UG Trenching)")) {
                subType1 = loadSubType1(typeStringInSpinner);
                initializeSecondSpinner(subType1);
            } else {
                forbid(subType1Spinner, subType1TextView);
                forbid(subType2Spinner, subType2TextView);
                subtypeString1 = "";
                subtypeString2 = "";
            }
        } else {
            forbid(subType1Spinner, subType1TextView);
            forbid(subType2Spinner, subType2TextView);
            subtypeString1 = "";
            subtypeString2 = "";
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @OnClick(R.id.next)
    void nextButtonClick() {
        routeString = routeTextView.getText().toString();
        if (fiberOperationType.equals("FTTX")) {
            circuitString = circuitTextView.getText().toString();
            areaString = areaTextView.getText().toString();
            if (!circuitString.equals("")) {
                if (!areaString.equals("")) {
                    if (!routeString.equals("")) {
                        Utilities.saveIntToPreference(this, "circuitId", circuitId);
                        Utilities.saveIntToPreference(this, "areaId", areaId);
                        Utilities.saveToPreference(this, "circuit", circuitString);
                        Utilities.saveToPreference(this, "area", areaString);
                        Utilities.saveToPreference(this, "shouldSearchFromRoute", "true");
                        sendDataToCameraActivity();
                    } else {
                        CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.WARNING,
                                "Please choose the route name!", false).show();
                    }
                } else {
                    CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.WARNING,
                            "Please choose the area name!", false).show();
                }
            } else {
                CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.WARNING,
                        "Please choose the circuit name!", false).show();
            }
        } else {
            if (!routeString.equals("")) {
                sendDataToCameraActivity();
            } else {
                CustomToastView.makeText(this, Toast.LENGTH_SHORT, CustomToastView.WARNING,
                        "Please choose the route name!", false).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent getFromPreviousActivity = getIntent(); // An Intent got from Previous Activity

        fiberOperationType = getFromPreviousActivity.getStringExtra("fiberOperationType");
        surveyOrPAC = getFromPreviousActivity.getStringExtra("surveyOrPAC");
        if (fiberOperationType.equals("FTTX")) {
            fetchType = 1;
            setContentView(R.layout.activity_fttx);
            searchView = findViewById(R.id.searchView);
            searchView.setQueryHint("Search Circuits");
        } else {
            setContentView(R.layout.activity_circuit_area_and_route);
        }
        routes = new ArrayList<>();
        circuits = new ArrayList<>();
        areas = new ArrayList<>();
        areaListString = new ArrayList<>();
        routeList = new ArrayList<>();
        routeStore = new ArrayList<>();
        initialize();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initialize() {
        type = this.getResources().getStringArray(R.array.type);
        b2BSurvey = this.getResources().getStringArray(R.array.b2BSurvey);
        b2BPac = this.getResources().getStringArray(R.array.b2BPac);
        mobiSurvey = this.getResources().getStringArray(R.array.mobiSurvey);
        mobiPac = this.getResources().getStringArray(R.array.mobiPac);
        b2BPremiumSurvey = this.getResources().getStringArray(R.array.b2BPremiumSurvey);
        b2BPremiumPac = this.getResources().getStringArray(R.array.b2BPremiumPac);
        subType1 = this.getResources().getStringArray(R.array.subType1);
        subType2 = this.getResources().getStringArray(R.array.subType2);
        ButterKnife.bind(this);
        String previousRoute = Utilities.getFromPreference(this, "route");
        if (!previousRoute.equals("")) {
            routeTextView.setText(previousRoute);
            routeString = previousRoute;
            code.requestFocus();
        }
        int previousRouteId = Utilities.getIntFromPreference(this, "routeId");

        if (previousRouteId != 0) {
            routeId = previousRouteId;
            routeIdStore = previousRouteId;
        }
        switch (fiberOperationType) {
            case "FTTX":
                fetchType = 1;  // Means to fetch Circuits from Server
                circuitTextView = findViewById(R.id.circuitTextView);
                areaTextView = findViewById(R.id.areaTextView);
                String previousCircuitName = Utilities.getFromPreference(this, "circuit");
                int previousCircuitId = Utilities.getIntFromPreference(this, "circuitId");
                String previousAreaName = Utilities.getFromPreference(this, "area");
                int previousAreaId = Utilities.getIntFromPreference(this, "areaId");
                if (previousCircuitName != null && !previousCircuitName.equals("")) {
                    circuitTextView.setText(previousCircuitName);
                    circuitString = previousCircuitName;
                }
                if (previousCircuitId != 0) {
                    circuitId = previousCircuitId;
                }
                if (previousAreaName != null && !previousAreaName.equals("")) {
                    areaTextView.setText(previousAreaName);
                    areaString = previousAreaName;
                }
                if (previousAreaId != 0) {
                    areaId = previousAreaId;
                    Utilities.saveToPreference(CircuitAreaAndRouteActivity.this, "shouldSearchFromRoute", "true");
                }

                // Search Again Button
                Button button = findViewById(R.id.reSearch);
                button.setOnClickListener(v -> {
                    fetchType = 1;
                    circuitId = 0;
                    areaId = 0;
                    routeId = 0;
                    Utilities.saveToPreference(CircuitAreaAndRouteActivity.this, "shouldSearchFromRoute", "false");
                    fetchCircuitsAndAreas("");
                    searchView.setQueryHint("Search Circuits");
                });
                ArrayAdapter<CharSequence> fttxArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, type);
                typeSpinner.setAdapter(fttxArrayAdapter);
                break;
            case "B2B":
                if (surveyOrPAC.equals("Survey")) {
                    ArrayAdapter<CharSequence> b2bSurveyArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, b2BSurvey);
                    typeSpinner.setAdapter(b2bSurveyArrayAdapter);
                } else {
                    ArrayAdapter<CharSequence> b2bPacArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, b2BPac);
                    typeSpinner.setAdapter(b2bPacArrayAdapter);
                }
                break;
            case "MOBI":
                if (surveyOrPAC.equals("Survey")) {
                    ArrayAdapter<CharSequence> mobiSurveyArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, mobiSurvey);
                    typeSpinner.setAdapter(mobiSurveyArrayAdapter);
                } else {
                    ArrayAdapter<CharSequence> mobiPacArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, mobiPac);
                    typeSpinner.setAdapter(mobiPacArrayAdapter);
                }
                break;
            default:
                if (surveyOrPAC.equals("Survey")) {
                    ArrayAdapter<CharSequence> b2bPremiumSurveyArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, b2BPremiumSurvey);
                    typeSpinner.setAdapter(b2bPremiumSurveyArrayAdapter);
                } else {
                    ArrayAdapter<CharSequence> b2bPremiumPacArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, b2BPremiumPac);
                    typeSpinner.setAdapter(b2bPremiumPacArrayAdapter);
                }
                break;
        }
        setIndicator();
        initializeAPIs();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        this.recreate();
    }

    private void setIndicator() {
        if (surveyOrPAC != null) {
            String indicatorString = fiberOperationType + " " + surveyOrPAC;
            indicator.setText(indicatorString);
            if (fiberOperationType.equals("B2B_PREMIUM")) {
                String b2bPremium = "B2B Premium";
                String indicatorStringB2B = b2bPremium + " " + surveyOrPAC;
                indicator.setText(indicatorStringB2B);
            }
        }
    }

    private String[] loadSubType1(String type) {
        switch (type) {
            case "PL (Pole)":
                return this.getResources().getStringArray(R.array.subType1List1);
            case "MH (ManHole)":
                return this.getResources().getStringArray(R.array.subType1List2);
            case "HH (HandHole)":
                return this.getResources().getStringArray(R.array.subType1List3);
            case "CL (Cable Laying)":
                return this.getResources().getStringArray(R.array.subType1List4);
            case "SPLICE (Splicing)":
                return this.getResources().getStringArray(R.array.subType1List5);
            case "MC/12C Pole Mout ODB":
                return this.getResources().getStringArray(R.array.subType1List6);
            case "Exchange ODF":
                return this.getResources().getStringArray(R.array.subType1List7);
            default:
                return this.getResources().getStringArray(R.array.subType1List8);
        }
    }

    private String[] loadSubType2(String subType1) {
        if (subType1.equals("RB (Pole Restanding)")) {
            return this.getResources().getStringArray(R.array.subType2List2);
        } else {
            return this.getResources().getStringArray(R.array.subType2List1);
        }
    }

    private void initializeSecondSpinner(String[] subType1StringArray) {
        show(subType1Spinner, subType1TextView);
        try {
            ArrayAdapter<String> secondSpinnerArrayAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_custom_layout, subType1StringArray);
            subType1Spinner.setAdapter(secondSpinnerArrayAdapter);
            subType1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    subtypeString1 = getSubType1Abbreviation(subType1StringArray[position]);
                    if (subtypeString1.equals("NP") || subtypeString1.equals("RP") ||
                            subtypeString1.equals("RB") || subtypeString1.equals("GIP")) {
                        subType2 = loadSubType2(subType1StringArray[position]);
                        initializeThirdSpinner(subType2);
                    } else {
                        forbid(subType2Spinner, subType2TextView);
                        subtypeString2 = "";
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            subType1Spinner.setEnabled(true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            forbid(subType1Spinner, subType1TextView);
        }
    }

    private void initializeThirdSpinner(String[] subType2StringArray) {
        show(subType2Spinner, subType2TextView);
        try {
            ArrayAdapter<String> thirdSpinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_custom_layout, subType2StringArray);
            subType2Spinner.setAdapter(thirdSpinnerArrayAdapter);
            subType2Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    subtypeString2 = getSubType2Abbreviation(subType2StringArray[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initializeAPIs() {
        if (fiberOperationType.equals("FTTX")) {
            if (circuitId != 0 && areaId != 0 && routeId != 0) {
                searchView.setQueryHint("Search Routes");
                fetchType = 2;
                fetchCircuitsAndAreas("");
            }
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                if (fiberOperationType.equals("FTTX")) {
                    if (fetchType == 3) {
                        int sizeOfRoute = routeStore.size();
                        routeList.clear();
                        for (int i = 0; i < sizeOfRoute; i++) {
                            if (routeStore.get(i).getText().toLowerCase().contains(newText.toLowerCase())) {
                                routeList.add(routeStore.get(i));
                            }
                        }
                        routes.clear();
                        routes.addAll(routeList);
                        routeArrayAdapter.notifyDataSetChanged();
                    } else {
                        fetchCircuitsAndAreas(newText);
                    }
                } else {
                    fetchRoutes(newText);
                }
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return true;
            }
        });
    }

    // Fetch Circuits and Areas
    private void fetchCircuitsAndAreas(String string) {
        if (Utilities.haveNetworkConnection(this)) {
            RetrofitAgent retrofit = RetrofitAgent.getInstance();
            APIService service = retrofit.getApiService();
            Call<ResponseBody> call;
            if (fetchType == 1) {
                call = service.fetchCircuits(string);
            } else if (fetchType == 2) {
                call = service.fetchAreas(circuitId); // circuitId
            } else {
                call = service.fetchAreas(circuitId);
            }
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            try {
                                json = response.body().string();
                                circuits.clear();
                                areas.clear();
                                routes.clear();
                                routeStore.clear();
                                areaListString.clear();
                                if (fiberOperationType.equals("FTTX")) {
                                    if (fetchType == 1) {
                                        circuits.addAll(fetchCircuitsFromJson(json));
                                        loadCircuitsAreasAndRoutesToListView();
                                        circuitArrayAdapter.notifyDataSetChanged();
                                    } else if (fetchType == 2) {
                                        String shouldSearchFromRoute = Utilities.getFromPreference(
                                                CircuitAreaAndRouteActivity.this, "shouldSearchFromRoute");

                                        if (shouldSearchFromRoute.equals("") || shouldSearchFromRoute.equals("false")) {
                                            areas.addAll(fetchAreasFromJson(json));
                                            areaListString.addAll(fetchAreaStringsFromJson(json));
                                            loadCircuitsAreasAndRoutesToListView();
                                            areaArrayAdapter.notifyDataSetChanged();
                                        } else {
                                            areas.addAll(fetchAreasFromJson(json));
                                            areaListString.addAll(fetchAreaStringsFromJson(json));
                                            for (int i = 0; i < areas.size(); i++) {
                                                Area area = areas.get(i);
                                                if (area.getAreaId() == areaId) {
                                                    routes.addAll(area.getRouteList());
                                                    routeStore.addAll(area.getRouteList());
                                                }
                                            }
                                            fetchType = 3;
                                            loadCircuitsAreasAndRoutesToListView();
                                            routeArrayAdapter.notifyDataSetChanged();
                                        }

                                    } else {
                                        areas.addAll(fetchAreasFromJson(json));
                                        areaListString.addAll(fetchAreaStringsFromJson(json));
                                        for (int i = 0; i < areas.size(); i++) {
                                            Area area = areas.get(i);
                                            if (area.getAreaId() == areaId) {
                                                routes.addAll(area.getRouteList());
                                                routeStore.addAll(area.getRouteList());
                                            }
                                        }
                                        fetchType = 3;
                                        loadCircuitsAreasAndRoutesToListView();
                                        routeArrayAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    routes.addAll(fetchRoutesFromJson(json));
                                    loadCircuitsAreasAndRoutesToListView();
                                    routeArrayAdapter.notifyDataSetChanged();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_SHORT, CustomToastView.ERROR, "Server Error", false).show();
                        }
                    } else if (response.code() == 500) {
                        AlertDialog dialog = new AlertDialog.Builder(CircuitAreaAndRouteActivity.this).create();
                        dialog.setTitle("Server Error");
                        dialog.setIcon(R.drawable.ic_error_black_24dp);
                        dialog.setMessage("Server's Internal Error!");
                        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, index) -> dialog.dismiss());
                        dialog.show();
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(CircuitAreaAndRouteActivity.this).create();
                        dialog.setTitle("App Version");
                        dialog.setIcon(R.drawable.ic_error_black_24dp);
                        dialog.setMessage("You need to upgrade the application to latest version!");
                        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, index) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setData(Uri.parse("http://167.172.70.248:8000"));
                            startActivity(intent);
                        });
                        dialog.show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                }
            });
        } else {
            CustomToastView.makeText(this, Toast.LENGTH_LONG, CustomToastView.WARNING, "This app requires internet!",
                    false).show();
        }
    }

    //  Get Routes
    private void fetchRoutes(String string) {
        if (Utilities.haveNetworkConnection(this)) {
            RetrofitAgent retrofit = RetrofitAgent.getInstance();
            APIService service = retrofit.getApiService();
            Call<ResponseBody> call = service.fetchRoutes(string, BuildConfig.VERSION_NAME);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            try {
                                json = response.body().string();
                                routes.clear();
                                routes.addAll(fetchRoutesFromJson(json));
                                loadCircuitsAreasAndRoutesToListView();
                                routeArrayAdapter.notifyDataSetChanged();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_SHORT, CustomToastView.ERROR, "Server Error", false).show();
                        }
                    } else if (response.code() == 500) {
                        AlertDialog dialog = new AlertDialog.Builder(CircuitAreaAndRouteActivity.this).create();
                        dialog.setTitle("Server Error");
                        dialog.setIcon(R.drawable.ic_error_black_24dp);
                        dialog.setMessage("Server's Internal Error!");
                        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, index) -> dialog.dismiss());
                        dialog.show();
                    } else {
                        AlertDialog dialog = new AlertDialog.Builder(CircuitAreaAndRouteActivity.this).create();
                        dialog.setTitle("App Version");
                        dialog.setIcon(R.drawable.ic_error_black_24dp);
                        dialog.setMessage("You need to upgrade the application to latest version!");
                        dialog.setButton(Dialog.BUTTON_POSITIVE, "OK", (dialogInterface, index) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setData(Uri.parse("http://167.172.70.248:8000"));
                            startActivity(intent);
                        });
                        dialog.show();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                }
            });
        } else {
            CustomToastView.makeText(this, Toast.LENGTH_LONG, CustomToastView.WARNING, "This app requires internet!",
                    false).show();
        }
    }

    private void loadCircuitsAreasAndRoutesToListView() {
        if (fiberOperationType.equals("FTTX")) {
            if (fetchType == 1) {
                circuitArrayAdapter = new ArrayAdapter<>(this, R.layout.listview_custom_layout, circuits);
                listView.setAdapter(circuitArrayAdapter);
            } else if (fetchType == 2) {
                areaArrayAdapter = new ArrayAdapter<>(this, R.layout.listview_custom_layout, areaListString);
                listView.setAdapter(areaArrayAdapter);
            } else {
                routeArrayAdapter = new ArrayAdapter<>(this, R.layout.listview_custom_layout, routes);
                listView.setAdapter(routeArrayAdapter);
            }
        } else {
            routeArrayAdapter = new ArrayAdapter<>(this, R.layout.listview_custom_layout, routes);
            listView.setAdapter(routeArrayAdapter);
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (fiberOperationType.equals("FTTX")) {
                if (fetchType == 1) {
                    Object listViewItemAtPosition = listView.getItemAtPosition(position);
                    try {
                        Circuit circuit = (Circuit) listViewItemAtPosition;
                        circuitTextView.setText(circuit.getCircuit());
                        circuitId = circuit.getId();
                        areaTextView.setText("");
                        routeTextView.setText("");
                        circuitId = circuit.getId();
                        fetchType = 2;
                        searchView.setQueryHint("Search Areas");
                        fetchCircuitsAndAreas(circuitTextView.getText().toString());
                    } catch (Exception exception) {
                        CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_SHORT,
                                CustomToastView.WARNING, "Please wait...", false).show();
                    }

                } else if (fetchType == 2) {
                    fetchType = 3;
                    Object listViewItemAtPosition = listView.getItemAtPosition(position);
                    try {
                        String listViewItemAtPositionString = (String) listViewItemAtPosition;
                        areaTextView.setText(listViewItemAtPositionString);
                        searchView.setQueryHint("Search Routes");
                        if (!areas.isEmpty()) { // To Prevent Monkey Click Error
                            Area area2 = areas.get(position);
                            areaId = areas.get(position).getAreaId();
                            routes.addAll(area2.getRouteList());
                            routeStore.addAll(area2.getRouteList());
                            loadCircuitsAreasAndRoutesToListView();
                            routeArrayAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception exception) {

                        CustomToastView.makeText(getApplicationContext(), Toast.LENGTH_SHORT,
                                CustomToastView.WARNING, "Please wait...", false).show();
                        fetchType = 1;
                        circuitId = 0;
                        areaId = 0;
                        routeId = 0;
                        Utilities.saveToPreference(CircuitAreaAndRouteActivity.this, "shouldSearchFromRoute", "false");
                        fetchCircuitsAndAreas("");
                        searchView.setQueryHint("Search Circuits"); // Re search in Circuits when Monkey Click Error Occurs.
                    }
                } else {
                    Route route = (Route) listView.getItemAtPosition(position);
                    routeTextView.setText(route.getText());
                    routeId = route.getId();
                }
            } else {
                Route route = (Route) listView.getItemAtPosition(position);
                routeTextView.setText(route.getText());
                routeId = route.getId();
            }

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            if (inputMethodManager != null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });
    }

    private void forbid(Spinner spinner, TextView textview) {
        textview.setVisibility(View.GONE);
        spinner.setEnabled(false);
        spinner.setVisibility(View.GONE);
    }

    private void show(Spinner spinner, TextView textview) {
        textview.setVisibility(View.VISIBLE);
        spinner.setEnabled(true);
        spinner.setVisibility(View.VISIBLE);
    }

    private String getTypeAbbreviation(String string) {
        switch (string) {
            case "PL (Pole)":
                return "PL";
            case "CL (Cable Laying)":
                return "CL";
            case "SPLICE (Splicing)":
                return "SPLICE";
            case "Wr_Pipe (Warning Pipe)":
                return "Wr_Pipe";
            case "FAT (2nd Splitter Open & Closed)":
                return "FAT";
            case "1st_SP (1st Splitter)":
                return "1st_SP";
            case "M/C (Mechanical Closure)":
                return "M/C";
            case "Fusion (Fusion Point)":
                return "Fusion";
            case "JC (Joint Closure)":
                return "JC";
            case "MH (ManHole)":
                return "MH";
            case "HH (HandHole)":
                return "HH";
            case "Exchange ODB Open & Closed":
                return "EX_ODB";
            case "Exchange ODF Open & Closed":
                return "EX_ODF";
            case "Customer ODB Open & Closed":
                return "C_ODB";
            case "Customer ODF Open & Closed":
                return "C_ODF";
            case "E2E (E2E Testing Photo)":
                return "E2E";
            case "B2B_Test (Ping/ Speed Testing Photo)":
                return "B2B_Test";
            case "Wall":
                return "WL";
            case "Activation (Activation Photo)":
                return "Activation";
            case "UG (UG Trenching)":
                return "UG";
            case "P (Pole Photo)":
                return "P";
            case "EX (Exchange Photo)":
                return "EX";
            case "CU (Customer Photo)":
                return "CU";
            case "MC (MC Photo)":
                return "MC";
            case "HD (Hand Drawing)":
                return "HD";
            case "Letter":
                return "Letter";
            case "Laying (Cable Laying Photo)":
                return "Laying";
            case "MC (Splicing/ Label Photo)":
                return "MC";
            case "CU (Customer Splicing/ Label Photo)":
                return "CU";
            case "EX (Label/ Splicing)":
                return "EX";
            case "P&S (Ping And Speed Test)":
                return "P&S";
            case "OPM (Optical Power Meter)":
                return "OPM";
            case "Redline (Pole Photo)":
                return "Redline";
            case "RD (Redline Drawing)":
                return "RD";
            case "Letter (Letter File)":
                return "Letter";
            case "SiteA":
                return "SiteA";
            case "SiteB":
                return "SiteB";
            case "HD (Hand Drawing Layout)":
                return "HD";
            case "SiteA (Splicing/ Label Photo)":
                return "SiteA";
            case "Laying (Cable Laying)":
                return "Laying";
            case "SiteB (Photo & Layout)":
                return "SiteB";
            case "Letter File":
                return "Letter";
            case "SubTrunk ODB (Photo)":
                return "SubODB";
            case "HD (HandDrawing)":
                return "HD";
            case "1st Spliter (Splicing /Label)":
                return "1stSPLT";
            case "2nd Spliter (Splicing /Label)":
                return "2ndFAT";
            case "Customer (Splicing /label)":
                return "CU";
            default:
                return "MC_ODB";
        }
    }

    private String getSubType1Abbreviation(String string) {
        switch (string) {
            case "EP (Existing Pole)":
                return "EP";
            case "NP (New Pole)":
                return "NP";
            case "RP (Replacement Pole)":
                return "RP";
            case "RB (Pole Restanding)":
                return "RB";
            case "GIP (GI Pole)":
                return "GIP";
            case "Drum (Drum Photo)":
                return "Drum";
            case "DrumJet (Drum Jet Photo)":
                return "DrumJet";
            case "Cablying (Cable Laying Photo with Safety)":
                return "Cablying";
            case "MC JC(M/C Joint Closure)":
                return "MC JC";
            case "MC 12c Box(M/C 12c Box)":
                return "MC 12c Box";
            case "Splicer (Splicer Photo)":
                return "Splicer";
            case "Exchange_ODB":
                return "Exchange_ODB";
            case "M/C":
                return "M/C";
            case "JC":
                return "JC";
            case "1st_SP":
                return "1st_SP";
            case "2nd_SP":
                return "2nd_SP";
            case "Fusion":
                return "Fusion";
            case "Customer_ODB":
                return "Customer_ODB";
            case "Closed_Label (Cover Photo)":
                return "Closed_Label";
            case "Open_Label (Cover Photo)":
                return "Open_Label";
            case "Closed (Cover Photo)":
                return "Closed";
            case "Open (Cover Photo)":
                return "Open";
            case "Closed_L (Closed Cover Label)":
                return "Closed_L";
            case "Open_L (Open Cover Label)":
                return "Open_L";
            case "EMH (Existing Manhole)":
                return "EMH";
            case "NMH (New Manhole)":
                return "NMH";
            case "RBMH (Rebuild Manhole)":
                return "RBMH";
            case "NHH (New Handhole)":
                return "NHH";
            case "EHH (Existing Handhole)":
                return "EHH";
            case "Excav (Excavation Photo)":
                return "Excav";
            case "Trenching (Trenching with Depth Board)":
                return "Trenching";
            case "BackF (Backfilling with warning tape)":
                return "BackF";
            default:
                return "";
        }
    }

    private String getSubType2Abbreviation(String string) {
        switch (string) {
            case "PHole (Pole Hole Photo with Board)":
                return "PHole";
            case "BF (Basement Footing)":
                return "BF";
            case "AllView (Pole General View with Footing)":
                return "AllView";
            case "Before (Before Photo)":
                return "Before";
            case "After_Footing (After Photo with Footing)":
                return "After_Footing";
            default:
                return "";
        }
    }

    private ArrayList<Circuit> fetchCircuitsFromJson(String response) {
        ArrayList<Circuit> arraylist = new ArrayList<>();
        try {
            //getting the whole json object from the response
            JSONObject jsonobject = new JSONObject(response);
            JSONArray jsonarray = jsonobject.getJSONArray("results");
            for (int i = 0; i < jsonarray.length(); i++) {
                Circuit circuit = new Circuit();
                JSONObject circuitJsonObject = jsonarray.getJSONObject(i);
                circuit.setId(circuitJsonObject.getInt("id"));
                circuit.setCircuit(circuitJsonObject.getString("circuit"));
                arraylist.add(circuit);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private ArrayList<Area> fetchAreasFromJson(String response) {
        ArrayList<Area> arraylist = new ArrayList<>();
        try {
            //getting the whole json object from the response
            JSONObject jsonobject = new JSONObject(response);
            JSONArray jsonarray = jsonobject.getJSONArray("results");
            for (int i = 0; i < jsonarray.length(); i++) {
                Area area = new Area();
                JSONObject areaJsonObject = jsonarray.getJSONObject(i);
                area.setAreaId(areaJsonObject.getInt("id"));
                area.setAreaName(areaJsonObject.getString("area_name"));
                JSONArray routeJsonArray = areaJsonObject.getJSONArray("routes");
                List<Route> routeList = new ArrayList<>();
                for (int j = 0; j < routeJsonArray.length(); j++) {
                    Route route = new Route();
                    JSONObject routeJsonObject = routeJsonArray.getJSONObject(j);
                    route.setId(routeJsonObject.getInt("id"));
                    route.setText(routeJsonObject.getString("text"));
                    routeList.add(route);
                }
                area.setRouteList(routeList);
                arraylist.add(area);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private ArrayList<String> fetchAreaStringsFromJson(String response) {
        ArrayList<String> arraylist = new ArrayList<>();
        try {
            //getting the whole json object from the response
            JSONObject jsonobject = new JSONObject(response);
            JSONArray jsonarray = jsonobject.getJSONArray("results");
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject areaStringJsonObject = jsonarray.getJSONObject(i);
                String area = (areaStringJsonObject.getString("area_name"));
                arraylist.add(area);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private ArrayList<Route> fetchRoutesFromJson(String response) {
        ArrayList<Route> arraylist = new ArrayList<>();
        try {
            //getting the whole json object from the response
            JSONObject jsonobject = new JSONObject(response);
            JSONArray jsonarray = jsonobject.getJSONArray("results");
            for (int i = 0; i < jsonarray.length(); i++) {
                Route route = new Route();
                JSONObject routeJsonObject = jsonarray.getJSONObject(i);
                route.setId(routeJsonObject.getInt("id"));
                route.setText(routeJsonObject.getString("text"));
                arraylist.add(route);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arraylist;
    }

    private void sendDataToCameraActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To remove History when Back Button is pressed in Next Activity
        // finish() remains History Data
        intent.putExtra("surveyOrPAC", surveyOrPAC);
        if (routeId == 0) { // To Solve the problem of routeId getting 0 when "RE-SEARCH" clicked,
            // and then "NEXT" is clicked.
            routeId = routeIdStore;
        }
        intent.putExtra("routeId", routeId);
        intent.putExtra("routeName", routeString);
        intent.putExtra("type", typeString);
        intent.putExtra("subType1", subtypeString1);
        intent.putExtra("subType2", subtypeString2);
        intent.putExtra("code", code.getText().toString().trim());
        EditText remark = findViewById(R.id.remark);
        intent.putExtra("remark", remark.getText().toString().trim());
        intent.putExtra("fiberOperationType", fiberOperationType);
        startActivity(intent);
    }
}