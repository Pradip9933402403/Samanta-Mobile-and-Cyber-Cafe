package com.samanta.repairstock;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    private final ArrayList<StockItem> items = new ArrayList<>();
    private LinearLayout itemList;
    private TextView summary;
    private EditText searchBox;
    private int pad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pad = (int) (16 * getResources().getDisplayMetrics().density);
        loadItems();
        buildScreen();
        renderItems();
    }

    private void buildScreen() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(pad, pad, pad, 0);
        page.setBackgroundColor(0xFFF4F6F8);

        TextView title = new TextView(this);
        title.setText("Mobile Repair Stock");
        title.setTextSize(25);
        title.setTextColor(0xFF14213D);
        title.setTypeface(null, 1);
        page.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Parts inventory · saved on this device");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF667085);
        subtitle.setPadding(0, 4, 0, 14);
        page.addView(subtitle);

        summary = new TextView(this);
        summary.setTextSize(15);
        summary.setTextColor(0xFF14213D);
        summary.setPadding(pad, pad, pad, pad);
        summary.setBackgroundColor(0xFFE5EEF9);
        page.addView(summary, matchWidth());

        Button addButton = new Button(this);
        addButton.setText("+  Add part");
        addButton.setOnClickListener(v -> showItemDialog(null));
        page.addView(addButton, matchWidth());

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setHint("Search parts or categories");
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderItems(); }
            @Override public void afterTextChanged(Editable s) { }
        });
        page.addView(searchBox, matchWidth());

        ScrollView scroll = new ScrollView(this);
        itemList = new LinearLayout(this);
        itemList.setOrientation(LinearLayout.VERTICAL);
        itemList.setPadding(0, 8, 0, pad);
        scroll.addView(itemList);
        page.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(page);
    }

    private void renderItems() {
        if (itemList == null) return;
        itemList.removeAllViews();
        int totalUnits = 0;
        double stockValue = 0;
        String query = searchBox == null ? "" : searchBox.getText().toString().trim().toLowerCase(Locale.ROOT);

        for (StockItem item : items) {
            totalUnits += item.quantity;
            stockValue += item.quantity * item.purchasePrice;
            if (!query.isEmpty() && !(item.name + " " + item.category).toLowerCase(Locale.ROOT).contains(query)) continue;
            itemList.addView(createItemCard(item));
        }
        summary.setText("" + items.size() + " parts    ·    " + totalUnits + " units    ·    Purchase value " + money(stockValue));
        if (itemList.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(items.isEmpty() ? "No parts yet. Tap ‘Add part’ to start your inventory." : "No matching parts.");
            empty.setTextColor(0xFF667085);
            empty.setTextSize(15);
            empty.setPadding(4, 24, 4, 24);
            itemList.addView(empty);
        }
    }

    private View createItemCard(StockItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(pad, pad / 2, pad, pad / 2);
        card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = pad / 2;
        card.setLayoutParams(cardParams);

        TextView name = new TextView(this);
        name.setText(item.name);
        name.setTextSize(18);
        name.setTextColor(0xFF14213D);
        name.setTypeface(null, 1);
        card.addView(name);

        TextView details = new TextView(this);
        details.setText(item.category + "  ·  Stock: " + item.quantity
                + "  ·  Buy: " + money(item.purchasePrice) + "  ·  Sell: " + money(item.sellingPrice));
        details.setTextSize(13);
        details.setTextColor(item.quantity <= 2 ? 0xFFB42318 : 0xFF667085);
        details.setPadding(0, 4, 0, 6);
        card.addView(details);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        Button stockButton = new Button(this);
        stockButton.setText("Adjust stock");
        stockButton.setOnClickListener(v -> showStockDialog(item));
        actions.addView(stockButton);
        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setOnClickListener(v -> showItemDialog(item));
        actions.addView(editButton);
        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> confirmDelete(item));
        actions.addView(deleteButton);
        card.addView(actions);
        return card;
    }

    private void showItemDialog(StockItem existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(pad, 0, pad, 0);
        EditText name = field("Part name (e.g. Phone display)", existing == null ? "" : existing.name);
        EditText category = field("Category (e.g. Display, Battery)", existing == null ? "" : existing.category);
        EditText quantity = field("Quantity", existing == null ? "0" : String.valueOf(existing.quantity));
        quantity.setInputType(2);
        EditText purchase = field("Purchase price", existing == null ? "0" : String.valueOf(existing.purchasePrice));
        purchase.setInputType(8194);
        EditText selling = field("Selling price", existing == null ? "0" : String.valueOf(existing.sellingPrice));
        selling.setInputType(8194);
        form.addView(name); form.addView(category); form.addView(quantity); form.addView(purchase); form.addView(selling);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add part" : "Edit part")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String partName = name.getText().toString().trim();
            String partCategory = category.getText().toString().trim();
            try {
                int qty = Integer.parseInt(quantity.getText().toString().trim());
                double buy = Double.parseDouble(purchase.getText().toString().trim());
                double sell = Double.parseDouble(selling.getText().toString().trim());
                if (partName.isEmpty() || partCategory.isEmpty() || qty < 0 || buy < 0 || sell < 0) {
                    throw new IllegalArgumentException();
                }
                if (existing == null) {
                    items.add(new StockItem(UUID.randomUUID().toString(), partName, partCategory, qty, buy, sell));
                } else {
                    existing.name = partName;
                    existing.category = partCategory;
                    existing.quantity = qty;
                    existing.purchasePrice = buy;
                    existing.sellingPrice = sell;
                }
                saveItems();
                renderItems();
                dialog.dismiss();
            } catch (RuntimeException e) {
                Toast.makeText(this, "Enter a name, category, valid quantity and prices.", Toast.LENGTH_SHORT).show();
            }
        }));
        dialog.show();
    }

    private void showStockDialog(StockItem item) {
        EditText amount = field("Units to add or remove", "1");
        amount.setInputType(2);
        new AlertDialog.Builder(this)
                .setTitle("Adjust " + item.name + " · current stock " + item.quantity)
                .setView(amount)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Remove", (dialog, which) -> changeStock(item, amount, -1))
                .setPositiveButton("Add", (dialog, which) -> changeStock(item, amount, 1))
                .show();
    }

    private void changeStock(StockItem item, EditText amount, int direction) {
        try {
            int units = Integer.parseInt(amount.getText().toString().trim());
            if (units <= 0 || (direction < 0 && units > item.quantity)) throw new IllegalArgumentException();
            item.quantity += direction * units;
            saveItems();
            renderItems();
        } catch (RuntimeException e) {
            Toast.makeText(this, "Enter a positive amount no greater than current stock.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete(StockItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete part?")
                .setMessage("Remove “" + item.name + "” from your inventory?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    items.remove(item);
                    saveItems();
                    renderItems();
                }).show();
    }

    private EditText field(String hint, String value) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        input.setText(value);
        return input;
    }

    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private String money(double value) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(value);
    }

    private void loadItems() {
        String stored = getSharedPreferences("repair_stock", MODE_PRIVATE).getString("items", "[]");
        try {
            JSONArray array = new JSONArray(stored);
            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);
                items.add(new StockItem(row.optString("id"), row.optString("name"), row.optString("category"),
                        row.optInt("quantity"), row.optDouble("purchasePrice"), row.optDouble("sellingPrice")));
            }
        } catch (JSONException ignored) {
            Toast.makeText(this, "Could not read saved stock data.", Toast.LENGTH_LONG).show();
        }
    }

    private void saveItems() {
        JSONArray array = new JSONArray();
        for (StockItem item : items) {
            JSONObject row = new JSONObject();
            try {
                row.put("id", item.id);
                row.put("name", item.name);
                row.put("category", item.category);
                row.put("quantity", item.quantity);
                row.put("purchasePrice", item.purchasePrice);
                row.put("sellingPrice", item.sellingPrice);
                array.put(row);
            } catch (JSONException ignored) { }
        }
        getSharedPreferences("repair_stock", MODE_PRIVATE).edit().putString("items", array.toString()).apply();
    }

    private static class StockItem {
        final String id;
        String name;
        String category;
        int quantity;
        double purchasePrice;
        double sellingPrice;

        StockItem(String id, String name, String category, int quantity, double purchasePrice, double sellingPrice) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.purchasePrice = purchasePrice;
            this.sellingPrice = sellingPrice;
        }
    }
}
