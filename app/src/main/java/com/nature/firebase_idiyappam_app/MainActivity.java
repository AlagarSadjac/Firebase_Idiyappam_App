package com.nature.firebase_idiyappam_app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText nameInput, qtyInput, delivery;
    Button orderBtn, showOrdersBtn, myOrderBtn;
    FirebaseFirestore db;
    TextView displayOrders;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. XML ஐடிகளுடன் இணைத்தல்
        nameInput = findViewById(R.id.customerName);
        qtyInput = findViewById(R.id.quantityInput);
        delivery = findViewById(R.id.delivery);
        orderBtn = findViewById(R.id.orderBtn);
        showOrdersBtn = findViewById(R.id.showOrdersBtn);
        myOrderBtn = findViewById(R.id.myOrderBtn);
        displayOrders = findViewById(R.id.displayOrders);

        db = FirebaseFirestore.getInstance();

        // 2. வாடிக்கையாளர் தன் சொந்த ஆர்டரை மட்டும் பார்க்கும் பட்டன்
        myOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = getSharedPreferences("UserOrderPrefs", MODE_PRIVATE);
                String savedName = sp.getString("last_name", "");
                String savedQty = sp.getString("last_qty", "");
                String savedAddress = sp.getString("last_address", "");

                if (savedName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "நீங்கள் இன்னும் ஆர்டர் எதுவும் செய்யவில்லை!", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("உங்கள் ஆர்டர் விவரம் (Your Order)")
                            .setMessage("பெயர்: " + savedName + "\n" +
                                    "எண்ணிக்கை: " + savedQty + "\n" +
                                    "முகவரி: " + savedAddress)
                            .setPositiveButton("சரி (OK)", null)
                            .show();
                }
            }
        });

        // 3. அட்மின் மட்டும் அனைத்து ஆர்டர்களையும் பார்க்கும் பட்டன்

        showOrdersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText inputPin = new EditText(MainActivity.this);
                inputPin.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                inputPin.setHint("4 இலக்க PIN உள்ளிடவும்");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("அட்மின் அனுமதி (Admin Only)")
                        .setMessage("ஆர்டர்களைப் பார்க்க கடவுச்சொல்லை உள்ளிடவும்:")
                        .setView(inputPin)
                        .setPositiveButton("திற (Open)", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String enteredPin = inputPin.getText().toString().trim();

                                if (enteredPin.isEmpty()) {
                                    Toast.makeText(MainActivity.this, "PIN உள்ளிடவும்!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Firebase-ல் நாம் உருவாக்கிய app_settings > admin ஆவணத்திலிருந்து pin-ஐ எடுத்தல்
                                db.collection("app_settings").document("admin")
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                String correctPin = documentSnapshot.getString("pin");

                                                // பயனர் அடித்த பின்னும் ஃபயர்பேஸ் பின்னும் சரியாக இருந்தால்
                                                if (enteredPin.equals(correctPin)) {
                                                    Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(MainActivity.this, "தவறான கடவுச்சொல்!", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(MainActivity.this, "அட்மின் தரவு கிடைக்கவில்லை!", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this, "பிழை: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .setNegativeButton("ரத்து (Cancel)", null)
                        .show();
            }
        });

        // 4. ஆர்டர் செய்யும் பட்டன்
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String qty = qtyInput.getText().toString().trim();
                String address = delivery.getText().toString().trim();

                if (name.isEmpty() || qty.isEmpty() || address.isEmpty()) {
                    Toast.makeText(MainActivity.this, "தயவுசெய்து அனைத்து விவரங்களையும் நிரப்பவும்!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveToFirebase(name, qty, address);
            }
        });
    }

    // 5. ஆர்டரை Firebase மற்றும் போனின் மெமரியில் சேமிக்கும் மெத்தட்
    private void saveToFirebase(String name, String qty, String address) {
        Map<String, Object> order = new HashMap<>();
        order.put("customer_name", name);
        order.put("quantity", qty);
        order.put("delivery_address", address);
        order.put("timestamp", System.currentTimeMillis());

        db.collection("idiyappam_orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    // வாடிக்கையாளரின் சொந்த மொபைல் மெமரியில் சேமித்தல்
                    SharedPreferences sp = getSharedPreferences("UserOrderPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("last_name", name);
                    editor.putString("last_qty", qty);
                    editor.putString("last_address", address);
                    editor.apply();

                    // உடனடி உறுதிப்படுத்தல் பாப்-அப்
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("ஆர்டர் உறுதியானது! (Order Confirmed)")
                            .setMessage("பெயர்: " + name + "\n" +
                                    "எண்ணிக்கை: " + qty + "\n" +
                                    "டெலிவரி முகவரி: " + address)
                            .setCancelable(false)
                            .setPositiveButton("சரி (OK)", null)
                            .show();

                    // பெட்டிகளை காலியாக்குதல்
                    nameInput.setText("");
                    qtyInput.setText("");
                    delivery.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "பிழை: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}