package ro.utcn.foodapp.presentation.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import ro.utcn.foodapp.R;
import ro.utcn.foodapp.access.database.DatabaseManager;
import ro.utcn.foodapp.model.Product;
import ro.utcn.foodapp.model.Registration;
import ro.utcn.foodapp.utils.Constants;

/**
 * Created by coponipi on 16.05.2015.
 */
public class ProductListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Date> listDataHeader;
    private TreeMap<Date, List<Registration>> listDataChild;

    public ProductListAdapter(Context context) {
        this.context = context;
        listDataChild = new TreeMap<>();
        listDataHeader = new ArrayList<>();
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int headerPosition) {
        if (!listDataHeader.isEmpty()) {
            return listDataChild.get(listDataHeader.get(headerPosition)).size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getGroup(int headerPosition) {
        return listDataHeader.get(headerPosition);
    }

    @Override
    public Object getChild(int headerPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(headerPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int headerPosition) {
        return headerPosition;
    }

    @Override
    public long getChildId(int headerPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int headerPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Calendar headerDate = Calendar.getInstance();
        headerDate.setTime((Date) getGroup(headerPosition));

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_product_header, null);
        }

        TextView productDateHeader = (TextView) convertView.findViewById(R.id.products_listHeader);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        productDateHeader.setText(simpleDateFormat.format(headerDate.getTime()));

        return convertView;
    }

    @Override
    public View getChildView(int headerPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Registration registration = (Registration) getChild(headerPosition, childPosition);
        Product product = DatabaseManager.getInstance().getProduct(registration.getProductId());
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.setTime(product.getExpirationDate());

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.item_product, null);
        }

        TextView productTitle = (TextView) convertView.findViewById(R.id.item_product_title);
        TextView productNumberOfPieces = (TextView) convertView.findViewById(R.id.item_product_pieces_number);
        TextView productExpirationDate = (TextView) convertView.findViewById(R.id.item_product_expiration_date);
        TextView productIngredients = (TextView) convertView.findViewById(R.id.item_product_ingredients);
        productTitle.setText(product.getName().replace("\n", " ").replace("\r", " "));
        productNumberOfPieces.setText(String.valueOf(registration.getItemsNumber()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        productExpirationDate.setText(simpleDateFormat.format(product.getExpirationDate()));
        productIngredients.setText(product.getIngredients());

        if (product.getExpirationStatus().equals(Constants.PRODUCT_EXPIRATION_STATUS_EXPIRED)) {
            productExpirationDate.setTextColor(context.getResources().getColor(R.color.red));
        } else {
            productExpirationDate.setTextColor(context.getResources().getColor(R.color.colorTextLight));
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void updateHeaderData(List<Date> listProductRegistrationDate) {
        this.listDataHeader = listProductRegistrationDate;
    }

    public void updateAllItems(TreeMap<Date, List<Registration>> productsGroupedByDate) {
        this.listDataChild = productsGroupedByDate;
    }

    public void clearItems() {
        listDataHeader.clear();
        listDataChild.clear();
    }

    public List<Date> getHeaders() {
        return listDataHeader;
    }
}
