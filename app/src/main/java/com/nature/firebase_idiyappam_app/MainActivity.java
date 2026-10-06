package com.nature.firebase_idiyappam_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText nameInput, qtyInput;
    Button orderBtn;
    FirebaseFirestore db;
    TextView displayOrders;
    Button showBtn;
    EditText delivery;
    Button showOrdersBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Link to XML Id
        nameInput = findViewById(R.id.customerName);
        qtyInput = findViewById(R.id.quantityInput);
        orderBtn = findViewById(R.id.orderBtn);
        delivery = findViewById(R.id.delivery);
        showBtn = findViewById(R.id.showOrdersBtn);
        displayOrders = findViewById(R.id.displayOrders);
        showOrdersBtn = findViewById(R.id.showOrdersBtn);

        db = FirebaseFirestore.getInstance();

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // அடுத்த பக்கத்திற்குப் போகும் கோடு (Intent)
                Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
                startActivity(intent);
            }
        });
        // 3. பட்டன் கிளிக் செய்யும் போது நடக்கும் வேலை----------------------------------------------------------------------------
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString();
                String qty = qtyInput.getText().toString();

                // காலியாக இருந்தால் எச்சரிக்கை செய்தல்
                if (name.isEmpty() || qty.isEmpty()) {
                    Toast.makeText(MainActivity.this, "தயவுசெய்து விவரங்களை நிரப்பவும்!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveToFirebase(name, qty);
            }
        });
    }

    // 4. டேட்டாவை Firebase-க்கு அனுப்பும் முறை
    private void saveToFirebase(String name, String qty) {
        Map<String, Object> order = new HashMap<>();
        order.put("customer_name", name);
        order.put("quantity", qty);
        order.put("timestamp", System.currentTimeMillis());
        String address = delivery.getText().toString();
        order.put("delivery_address", address);


        db.collection("idiyappam_orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MainActivity.this, "ஆர்டர் வெற்றிகரமாகச் சேமிக்கப்பட்டது!", Toast.LENGTH_SHORT).show();
                    // டேட்டா அனுப்பிய பிறகு பாக்ஸ்களை காலியாக்குதல்
                    nameInput.setText("");
                    qtyInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "பிழை: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // 2. டேட்டாவை Firebase-லிருந்து எடுக்கும் மெத்தட்
    private void showOrders() {
        db.collection("idiyappam_orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String allData = "";
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("customer_name");
                        String qty = document.getString("quantity");
                        allData += "பெயர்: " + name + " | எண்ணிக்கை: " + qty + "\n\n";
                    }

                    displayOrders.setText(allData);// ஒரு TextView-வில் தற்காலிகமாக எல்லா டேட்டாவையும் காட்டுகிறோம்
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "டேட்டா எடுக்க முடியவில்லை!", Toast.LENGTH_SHORT).show();
                });
    }
}
