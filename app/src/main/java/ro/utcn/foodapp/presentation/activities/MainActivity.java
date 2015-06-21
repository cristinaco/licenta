package ro.utcn.foodapp.presentation.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.business.StockManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;
import ro.utcn.foodapp.presentation.adapters.ProductListAdapter;
import ro.utcn.foodapp.presentation.customViews.StyledExpandableListView;
import ro.utcn.foodapp.utils.Constants;


public class MainActivity extends ActionBarActivity {

    private StyledExpandableListView expandableListView;
    private ProductListAdapter productListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton registerProductBtn;
    private TextView numberExpiredProductsEditText;
    private EditText searchEditText;
    private List<Date> registrationsHeaderList;
    private TreeMap<Date, List<Registration>> registrationsGroupedByDate;
    private ActionMode actionMode;
    private boolean isInEditMode;
    private int numberOfExpiredProducts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        expandableListView = (StyledExpandableListView) findViewById(R.id.main_activity_products_expandable_list);
        registerProductBtn = (FloatingActionButton) findViewById(R.id.main_activity_register_product);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_products);
        numberExpiredProductsEditText = (TextView) findViewById(R.id.main_activity_no_expired_products);
        searchEditText = (EditText) findViewById(R.id.main_activity_search_value);

        productListAdapter = new ProductListAdapter(MainActivity.this);
        expandableListView.setAdapter(productListAdapter);
        numberExpiredProductsEditText.setText(String.valueOf(numberOfExpiredProducts));

        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateProductsList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 4000);
        updateProductsList();
    }


    /**
     * This method is called every time UI needs to be updated with the products list
     */
    private void updateProductsList() {
        registrationsHeaderList = new ArrayList<>();
        registrationsGroupedByDate = new TreeMap<>();
        numberOfExpiredProducts = 0;
        List<Product> productsForReg = new ArrayList<>();
        List<Registration> registrations = StockManager.getInstance().getAllRegistrations();

        if (registrations.size() > 0) {
            for (Registration registration : registrations) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(registration.getRegistrationDate());
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR, 0);
                if (!registrationsHeaderList.contains(calendar.getTime())) {
                    registrationsHeaderList.add(calendar.getTime());
                }
                Product product = StockManager.getInstance().getProduct(registration.getProductId());
                productsForReg.add(product);
                if (product.getExpirationStatus().equals(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED)) {

                    numberOfExpiredProducts++;
                }
            }
            registrationsGroupedByDate = StockManager.getInstance().groupRegistrationsByDate(registrations);

            productListAdapter.clearItems();
            productListAdapter.updateHeaderData(registrationsHeaderList);
            productListAdapter.updateAllItems(registrationsGroupedByDate);

            productListAdapter.notifyDataSetChanged();

            for (int i = 0; i < productListAdapter.getHeaders().size(); ++i) {
                expandableListView.expandGroup(i);
            }
        } else {
            productListAdapter.clearItems();
            productListAdapter.updateHeaderData(new ArrayList<Date>());
            productListAdapter.updateAllItems(new TreeMap<Date, List<Registration>>());

            productListAdapter.notifyDataSetChanged();
        }
        numberExpiredProductsEditText.setText(String.valueOf(numberOfExpiredProducts));
    }

    private void setListeners() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String hint = s.toString();
                updateProductsList(hint);
            }
        });
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long l) {

                //deselectChild();
                final Registration registration = registrationsGroupedByDate.get(registrationsHeaderList.get(groupPosition)).get(childPosition);
                view.setSelected(true);

                actionMode = startActionMode(new ActionMode.Callback() {
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        mode.setTitle("1 Selected");

                        MenuInflater inflater = mode.getMenuInflater();
                        inflater.inflate(R.menu.menu_action_mode, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete_product:
                                deleteRegistration(registration);
                                actionMode.finish();
                                return true;
                            case R.id.action_edit_product:
                                editRegistration(registration);
                                actionMode.finish();
                                return true;
                            default:
                                //doneClicked();
                                return false;
                        }
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        //doneClicked();
                    }
                });

                return true;
            }
        });

        registerProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInEditMode = false;
                Intent intent = new Intent(MainActivity.this, RegisterProductActivity.class);
                intent.putExtra(Constants.PRODUCT_IS_IN_EDIT_MODE, isInEditMode);
                startActivity(intent);
            }
        });
    }

    private void updateProductsList(String hint) {
        registrationsHeaderList = new ArrayList<>();
        registrationsGroupedByDate = new TreeMap<>();
        numberOfExpiredProducts = 0;
        List<Registration> registrations = StockManager.getInstance().searchRegistrations(hint);

        if (registrations.size() > 0) {
            for (Registration registration : registrations) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(registration.getRegistrationDate());
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR, 0);
                if (!registrationsHeaderList.contains(calendar.getTime())) {
                    registrationsHeaderList.add(calendar.getTime());
                }
                Product product = StockManager.getInstance().getProduct(registration.getProductId());
                if (product.getExpirationStatus().equals(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED)) {
                    numberOfExpiredProducts++;
                }

            }
            registrationsGroupedByDate = StockManager.getInstance().groupRegistrationsByDate(registrations);

            productListAdapter.clearItems();
            productListAdapter.updateHeaderData(registrationsHeaderList);
            productListAdapter.updateAllItems(registrationsGroupedByDate);

            productListAdapter.notifyDataSetChanged();

            for (int i = 0; i < productListAdapter.getHeaders().size(); ++i) {
                expandableListView.expandGroup(i);
            }
        } else {
            productListAdapter.clearItems();
            productListAdapter.updateHeaderData(new ArrayList<Date>());
            productListAdapter.updateAllItems(new TreeMap<Date, List<Registration>>());

            productListAdapter.notifyDataSetChanged();
        }
        numberExpiredProductsEditText.setText(String.valueOf(numberOfExpiredProducts));
    }

    private void editRegistration(Registration registration) {
        //Registration registration = registrationsGroupedByDate.get(registrationsHeaderList.get(groupPosition)).get(childPosition);
        isInEditMode = true;
        Intent intent = new Intent(MainActivity.this, RegisterProductActivity.class);
        intent.putExtra(Constants.PRODUCT_IS_IN_EDIT_MODE, isInEditMode);
        intent.putExtra(Constants.REGISTRATION, registration);
        startActivity(intent);

    }

    private void deleteRegistration(Registration registration) {

        StockManager.getInstance().deleteProduct(registration.getProductId());
        StockManager.getInstance().deleteRegistration(registration.getId());
        updateProductsList();
    }

}
