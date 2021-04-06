package it.pgp.squerez;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.aelitis.azureus.core.util.dns.DNSJavaImpl;

import java.util.List;

public class DNSTestActivity extends Activity {

    EditText editText;
    TextView results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dnstest);
        editText = findViewById(R.id.testdnsedittext);
        results = findViewById(R.id.resultsview);
    }

    public void dnstest(View view) {
        DNSJavaImpl impl = new DNSJavaImpl();
        List<String> l = impl.getTXTRecords(editText.getText().toString());
        String res;
        if (l == null) res = "@@@NULL@@@";
        else {
            res = "";
            for (String s : l) {
                res += s+"\n";
            }
            if (res.isEmpty()) res = "@@@EMPTY@@@";
        }
        results.setText(res);
    }
}
