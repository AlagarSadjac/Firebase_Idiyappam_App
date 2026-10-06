package com.nature.firebase_idiyappam_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OrderListActivity extends AppCompatActivity {

    TextView displayOrders;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        displayOrders = findViewById(R.id.displayOrders);
        db = FirebaseFirestore.getInstance();

        // இந்தப் பக்கம் திறந்த உடனே டேட்டா வந்துவிடும்
        fetchOrders();
    }

    private void fetchOrders() {
        db.collection("idiyappam_orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder listBuilder = new StringBuilder();
                    int count = queryDocumentSnapshots.size();
                    listBuilder.append("மொத்த ஆர்டர்கள்: ").append(count).append("\n\n");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("customer_name");
                        String qty = document.getString("quantity");
                        String addr = document.getString("delivery_address");

                        listBuilder.append("பெயர்: ").append(name)
                                .append("\nஎண்ணிக்கை: ").append(qty)
                                .append("\nமுகவரி: ").append(addr == null ? "இல்லை" : addr)
                                .append("\n----------------------\n");
                    }
                    displayOrders.setText(listBuilder.toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "பிழை: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}