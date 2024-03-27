import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button connectButton;
    private DroneCommunicationManager droneCommunicationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectButton = findViewById(R.id.connectButton);
        droneCommunicationManager = new DroneCommunicationManager(this);

        connectButton.setOnClickListener(v -> connectAndRequestData());
    }

    private void connectAndRequestData() {
        droneCommunicationManager.connectToDrone();
    }
}
